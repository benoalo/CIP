package invizio.cip.misc;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import invizio.cip.CIP;
import invizio.cip.MetadataCIP2;
import invizio.cip.RaiCIP2;
import invizio.cip.Regions;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imagej.ops.Op;

import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */

	@Plugin(type = Op.class, name="Project2CIP", headless = true)
	public class Project2CIP  < T extends RealType<T> & NativeType<T> , U extends RealType<U> & NativeType<U> > extends AbstractOp 
	{
		
		
		
		@Parameter (type = ItemIO.INPUT)
		private RaiCIP2<T> inputImage;
		
		@Parameter( label="dimension", persist=false ) 
		private Integer dimension;

		@Parameter( label="method", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String method = "max";
		
		@Parameter( label="method", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String outputType = "projection"; // "projection" , "argument" , "both"
		
		
		@Parameter (type = ItemIO.OUTPUT)
		private	RaiCIP2<U> projImageCIP;
		
		@Parameter (type = ItemIO.OUTPUT)
		private	RaiCIP2<IntType> argProjImageCIP;
		
		
		@Parameter
		OpService op;
		
		
		
		@Override
		public void run() {
			
			if( inputImage == null )
			{	//TODO: error message
				return;
			}
			
			int nDim = inputImage.numDimensions();
			
			if (dimension < 0 || dimension>=nDim )
				return;
			
			int[] projDimIndex = new int[nDim-1];
			long[] projSize = new long[nDim-1];
			int count=0;
			for( int d=0; d<nDim ; d++)
			{
				if( d != dimension ) {
					projSize[count] = inputImage.dimension(d);
					projDimIndex[count] = d;
					count++;
				}
			}
			
			
			
			Tester<T,U> projector = null;
			method = method.toLowerCase();
			RandomAccessibleInterval<U> projImageRAI;
			if ( method.equals("max") ) {
				projector = new MaxTester<T,U>();
				U valU =  (U)inputImage.randomAccess().get();
				projImageRAI = op.create().img( FinalDimensions.wrap(projSize) , valU );

			}
			else if ( method.equals("min") ) {
				projector = new MinTester<T,U>();
				U valU =  (U)inputImage.randomAccess().get();
				projImageRAI = op.create().img( FinalDimensions.wrap(projSize) , valU );

			}
			else if ( method.equals("add") || method.equals("sum") ) {
				projector = new SumTester<T,U>();
				U valU =  (U)new FloatType();
				projImageRAI = op.create().img( FinalDimensions.wrap(projSize) , valU );

				outputType = "projection";
			}
			else {
				return;
			}
			
			
			long[] pos = new long[nDim];
			long[] pos2 = new long[nDim-1];
			count=0;
			RandomAccess<U> projImageRA = projImageRAI.randomAccess();
			Cursor<T> cIn = Views.flatIterable( inputImage ).cursor();
			
			RandomAccessibleInterval<IntType> argProjImageRAI;
			
			outputType = outputType.toLowerCase();
			if( outputType.equals("argument") || outputType.equals("both") )
			{
				argProjImageRAI = op.create().img( FinalDimensions.wrap(projSize) , new IntType() );
				RandomAccess<IntType> argProjImageRA = argProjImageRAI.randomAccess();
	
				while( cIn.hasNext() )
				{
					final T in = cIn.next();
					cIn.localize( pos );				
					
					count = 0;
					for( int d :  projDimIndex ) {
						pos2[count] = pos[d];
						count++;
					}
					
					projImageRA.setPosition( pos2 );
					final U proj = projImageRA.get();
					
					final long slice = pos[dimension];
					if( slice == 0 || projector.test(in, proj) )
					{
						projector.update(in, proj);
						
						argProjImageRA.setPosition( pos2 );
						IntType argProj = argProjImageRA.get();
						argProj.setInteger( slice );
					}	
				}
				
				
				// add metadata to argprojImage and projImage
				argProjImageCIP = toRaiCIP( argProjImageRAI );
				if ( outputType.toLowerCase().equals("argument") )
					projImageCIP = null;
				else
					projImageCIP = toRaiCIP( projImageRAI );
			}
			else
			{
				while( cIn.hasNext() )
				{
					final T in = cIn.next();
					cIn.localize( pos );				
					
					count = 0;
					for( int d :  projDimIndex ) {
						pos2[count] = pos[d];
						count++;
					}
					
					projImageRA.setPosition( pos2 );
					final U proj = projImageRA.get();
					
					final long slice = pos[dimension];
					if( slice == 0 || projector.test(in, proj) )
						projector.update(in, proj);

				}
				
				// add metadata to projImage
				projImageCIP = toRaiCIP( projImageRAI );
				argProjImageCIP = null;
			}
			
			
			
			
			
		}

		
		private <V extends RealType<V>> RaiCIP2<V> toRaiCIP( RandomAccessibleInterval<V> rai ){
			
			// adapt input metadata for the output
			MetadataCIP2 metadata = new MetadataCIP2( inputImage.metadata() );
			if ( dimension != null )
				metadata.dropDimension( dimension );	
			
			return new RaiCIP2<V>(rai , metadata );
		}
		
		
		
		interface Tester<W extends RealType<W> , V extends RealType<V> >
		{	
			void update(W in, V proj);
			boolean test(W in , V proj);
		}

		
		class MaxTester<W extends RealType<W> , V extends RealType<V>> implements Tester<W,V>
		{
			@Override
			public void update(W in, V proj) {
				proj.setReal( in.getRealFloat() );
			}

			@Override
			public boolean test(W in, V proj) {
				return proj.getRealFloat() < in.getRealFloat();
			}
		}
		
		
		class MinTester<W extends RealType<W> , V extends RealType<V>> implements Tester<W,V>
		{
			@Override
			public void update(W in, V proj) {
				proj.setReal( in.getRealFloat() );
			}

			@Override
			public boolean test(W in, V proj) {
				return proj.getRealFloat() > in.getRealFloat();
			}
		}
		
		
		class SumTester<W extends RealType<W> , V extends RealType<V>> implements Tester<W,V>
		{
			@Override
			public void update(W in, V proj) {
				proj.setReal( proj.getRealFloat() + in.getRealFloat() );
			}
			
			@Override
			public boolean test(W in, V proj) {
				return true;
			}
		}
		
		
		
		
		public static void main(final String... args) throws IOException
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			String file = CIP.class.getResource( "/mitosis_t1.tif" ).getFile(); 
			ImagePlus imp = IJ.openImage(	file	);
			//ImagePlus imp = IJ.openImage(	"C:/Users/Ben/workspace/testImages/blobs32.tif" 	);
			//ImagePlus imp2 = IJ.openImage(	"C:/Users/Ben/workspace/testImages/mitosis_t1.tif" 	);
			//ij.ui().show(imp);
			
			
			
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			
			List<Object> output = (List<Object>) cip.project( imp, 3 , "max", "output","both" );
			cip.show(output.get(0));
			cip.show(output.get(1));
			
			Object output2 = cip.project( imp, 3 , "max" );
			cip.show(output2);
			
			Object output3 = cip.project( imp, 3 , "max", "output", "argument" );
			cip.show(output3);
			
			
//			ImageJ ij = new ImageJ();
//			ij.ui().showUI();
//			
//			//String file = CIP.class.getResource( "/mitosis_t1.tif" ).getFile(); 
//			ImagePlus imp = IJ.openImage(	"C:/Users/Ben/workspace/testImages/blobs32.tif" 	);
//			ImagePlus imp2 = IJ.openImage(	"C:/Users/Ben/workspace/testImages/mitosis_t1.tif" 	);
//			//ij.ui().show(imp);
//			
//			
//			
//			
//			CIP cip = new CIP();
//			cip.setContext( ij.getContext() );
//			cip.setEnvironment( ij.op() );
//			
//			//RandomAccessibleInterval<?> output = (RandomAccessibleInterval<?>) cip.project( imp, 3 , "max", "projection"  );
//			//RandomAccessibleInterval<?> output = (RandomAccessibleInterval<?>) cip.project( img1 , 2 , "max", "projection"  );
//					
//			
//			//String str = output==null ? "null" : output.toString();
//			
//			//System.out.println("hello projection:" + str );
//			//ij.ui().show(output);
//			
//			//Dataset dataset = (Dataset) ij.io().open("C:/Users/Ben/workspace/testImages/mitosis_t1.tif");
//			
//			//cip.toIJ1(output).show();
//			//System.out.println("hello toIJ2 " + cip.toIJ2(output) );
//			//ij.ui().show( cip.toIJ2(output) );
//			//cip.show(output, "gw");
//			
//			
//			
//			
//			String rtName = "my measures";
//
//			// measures in an image
//			Object measNames = CIP.list( "test", "size", "boundary", "position");
//			Object measures = cip.measure(imp , measNames);
//			cip.show( measures , rtName);
//
//			Object measNames2 = CIP.list( "median", "size", "position");
//			Object measures2 = cip.measure(imp , measNames2);
//			cip.show( measures2 , rtName);
//			
//			
//			
//			// measure in a region
//			Object impSeg1 = cip.threshold( imp , 100 );
//			Object region1 = cip.region( impSeg1 );
//			Object measures1 = cip.measure(region1 , measNames, "prefix","thresh_");
//			
//			System.out.println("above threshold:");
//			System.out.println(measures1);
//			System.out.println("\n");
//			
//			
//			// measure in a region with a source image
//			Object measures11 = cip.measure(region1 , measNames, imp, "prefix","thresh_");
//			
//			System.out.println("above threshold, measure with source:");
//			System.out.println(measures11);
//			System.out.println("\n");
//			
//			
//			// measure of the cc
//			Object impSeg2 = cip.label( imp , 100 );
//			Object regions = cip.region( impSeg2 );
//			Object measures3 = cip.measure(regions , measNames, "prefix","reg_");
//			Object measures33 = cip.measure(regions , measNames, imp, true ,"reg_");
//			cip.show( measures33 , "region measures");
//			cip.show( measures3  , "region measures 2");
//			
			//System.out.println("regions above threshold:");
			//System.out.println(measures2);
			//System.out.println("\n");

			//System.out.println("region above threshold with source:");
			//System.out.println(measures22);
			//System.out.println("\n");

			//String h = cip.show( imp );
			//cip.show(h, regions, "fire", "width", 1.0 );

			
			//Object impSeg2 = cip.threshold( imp , 100 );
			//Object region2 = cip.region( impSeg2 );

			//Object imp3 = cip.slice(imp2,2,0);
			//Object impSeg3 = cip.threshold( imp3 , 7000 );

			//Object regionFromImg = cip.region( impSeg3 );
			//Object regionIJ1 = cip.toIJ1(regionFromImg);
			//Object regionIJ2 = cip.toIJ2(regionIJ1);
			
			//Object h1 = cip.show( imp3, "g" );
			//cip.show( h1, regionFromImg, "cyan" );

			//Object h2 = cip.show( imp3, "g" );
			//cip.show( h2, regionIJ1, "yellow" );

			//Object h3 = cip.show( imp3, "g" );
			//cip.show( h3, regionIJ2, "magenta" );

			//Roi region3 = new Roi(50,50, 100, 50);

			//List<Roi> region4 = new ArrayList<Roi>();
			//region4.add( new Roi(100,100, 20, 20) );
			//region4.add( new Roi(110,110, 40, 20) );
			

			//String h1 = cip.show( imp2 , "rb");
			
			//String h2 = cip.show( imp2 , cip.list("red", "green"));

			//String h3 = cip.show( imp , "cyan");
			//String h4 = cip.show( imp );
			//String h5 = cip.show( imp );
			//String h6 = cip.show( imp );

			//cip.show(h3, region1, "fire", "width", 2.0 );
			//cip.show(h4, region2, "red", "width", 2.0 );
			
			//cip.show(h5, region3, "green", 5 );
			
			//Object region5 = Regions.toIterableRegion(region3);
			//cip.show(h5, region5, "magenta", 1 );
			
			//System.out.println("region:" + region5.toString() );
			
			
			//cip.show(h6, region4, "fire", "width", 2);
			
			//String h2 = cip.show( imp , "green");
			
			//System.out.println("h2 -> "+h2);
			
			
			System.out.println("done!");
		}
		
	
}
