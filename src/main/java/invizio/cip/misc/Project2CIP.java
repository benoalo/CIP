package invizio.cip.misc;


import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imagej.ops.Op;

import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
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
		private RandomAccessibleInterval<T> inputImage;
		
		@Parameter( label="dimension", persist=false ) 
		private Integer dimension;

		@Parameter( label="method", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String method = "max";
		
		@Parameter( label="method", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String outputType = "projection"; // "projection" , "argument" , "both"
		
		
		@Parameter (type = ItemIO.OUTPUT)
		private	RandomAccessibleInterval<U> projImage;
		
		@Parameter (type = ItemIO.OUTPUT)
		private	RandomAccessibleInterval<IntType> argProjImage;
		
		
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
					projSize[d] = inputImage.dimension(d);
					projDimIndex[count] = d;
					count++;
				}
			}
			
			
			
			Tester<T,U> projector = null;
			method = method.toLowerCase();
			
			if ( method.equals("max") ) {
				projector = new MaxTester<T,U>();
				U valU =  (U)inputImage.randomAccess().get();
				projImage = op.create().img( FinalDimensions.wrap(projSize) , valU );

			}
			else if ( method.equals("min") ) {
				projector = new MinTester<T,U>();
				U valU =  (U)inputImage.randomAccess().get();
				projImage = op.create().img( FinalDimensions.wrap(projSize) , valU );

			}
			else if ( method.equals("add") || method.equals("sum") ) {
				projector = new SumTester<T,U>();
				U valU =  (U)new FloatType();
				projImage = op.create().img( FinalDimensions.wrap(projSize) , valU );

				outputType = "projection";
			}
			
			
			
			long[] pos = new long[nDim];
			long[] pos2 = new long[nDim-1];
			count=0;
			RandomAccess<U> projImageRA = projImage.randomAccess();
			Cursor<T> cIn = Views.flatIterable( inputImage ).cursor();
			
			outputType = outputType.toLowerCase();
			if( outputType.equals("argument") || outputType.equals("both") )
			{
				argProjImage = op.create().img( FinalDimensions.wrap(projSize) , new IntType() );
				RandomAccess<IntType> argProjImageRA = argProjImage.randomAccess();
	
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
			}
			
			if ( outputType.toLowerCase().equals("argument") )
				projImage = null;
			
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
		
		
		
		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			//ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
			ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/mitosis_t1.tif");
			ij.ui().show(imp);
			
			
			Img<UnsignedByteType> img = ImageJFunctions.wrap(imp);
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			
			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<IntType> output = (RandomAccessibleInterval<IntType>)
									cip.project( img , 3 , "add", "projection"  );
					
			//cip.create( cip.aslist(100, 50, 2) , 10, "double"  );
			
			String str = output==null ? "null" : output.toString();
			
			System.out.println("hello projection:" + str );
			ij.ui().show(output);
			
			System.out.println("done!");
		}
		
	
}
