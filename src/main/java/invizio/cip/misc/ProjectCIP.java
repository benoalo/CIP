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
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */

	@Deprecated
	@Plugin(type = Op.class, name="ProjectCIP", headless = true)
	public class ProjectCIP  < T extends RealType<T> & NativeType<T> > extends AbstractOp 
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
		private	RandomAccessibleInterval<T> projImage;
		
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
			
			long[] projSize = new long[nDim-1];
			long projMax = 1;
			for( int d=0; d<nDim ; d++)
			{
				if( d != dimension ) {
					projSize[d] = inputImage.dimension(d);
					projMax *= inputImage.dimension(d);
				}
			}
			
			T valT =  inputImage.randomAccess().get();
			projImage = op.create().img( FinalDimensions.wrap(projSize) , valT );
			
			Cursor<IntType> cArgProj = null ;
			outputType = outputType.toLowerCase();
			if( outputType.equals("argument") || outputType.equals("both") )
			{
				argProjImage = op.create().img( FinalDimensions.wrap(projSize) , new IntType() );
				cArgProj = Views.flatIterable( argProjImage ).cursor();
				
			}
			
			
			Tester<T> projector = null;
			method = method.toLowerCase();
			
			if ( method.equals("max") ) {
				projector = new MaxTester<T>();
			}
			else if ( method.equals("min") ) {
				projector = new MinTester<T>();
			}
			else if ( method.equals("add") || method.equals("sum") ) {
				projector = new SumTester<T>();
			}
			
			
			Cursor<T> cIn = Views.flatIterable( inputImage ).cursor();
			Cursor<T> cProj = Views.flatIterable( projImage ).cursor();
			
			long sliceSwitch = 1;
			for( int d=0; d<dimension; d++)
				sliceSwitch *= inputImage.dimension(d);
			long sliceMax = inputImage.dimension( dimension );
			
			long slice = 0;
			long countToSwitchSlice = 0;
			long projCount = 0;
			
			// TODO: 	implement for dimension=0 or last dim could be optimized
			//			implement could be simpler with randomAccess
			//			no argument image
			//			optimization possible if argument image not needed 
			
			// implement for dimension>0
			while( cIn.hasNext() )
			{
				
				// in = cIn.next()
				// proj = cProj.next()
				
				// 		if countToSwitchSlice==sliceSwitch
				//			countToSwitchSlice=0;
				//			slice++;
				//			if slice==sliceMax
				//				slice=0
				//			else
				//				cProj.moveback from sliceSwitch	
				//				cArgProj.moveback from sliceSwitch	
				//				projCount -= sliceSwitch
				//			
				//		if projCount==projMax // only needed for last dim  // not needed with random access
				//			cProj.reset()
				//			cArgProj.reset()
				//			projCount==0
				//	
				//		if slice==0 || newVal> maxVal
				//			update projImage
				//			update argImage
				//
				// 		countToSwitchSlice++;
				// 		projCount++
				
				
				// get input pos
				// set proj pos
				//
				// compare value
				//		update value
				
				if ( countToSwitchSlice == sliceMax ) {
					countToSwitchSlice=0;
						slice++;
						if ( slice == sliceMax ) {
							slice=0;
						}
						else {
							cProj.jumpFwd( -sliceSwitch ); // this prob doesn't work, should find an alternative
							cArgProj.jumpFwd( -sliceSwitch );
							projCount -= sliceSwitch;
						}
				}
				if ( projCount == projMax ) { // only needed for last dim
					cProj.reset();
					cArgProj.reset();
					projCount = 0;
				}
				
				T in  = cIn.next();
				T proj = cProj.next();
				IntType argProj = cArgProj.next();
				
				if ( slice == 0 || projector.test(in, proj) ) {
					proj.set( in );
					argProj.setInteger( slice );
				}
				
				countToSwitchSlice++;
				projCount++;
				
			}
			
			
			
		}


		
		
		interface Tester<U extends RealType<U> >
		{	
			void update(U in, U proj);
			boolean test(U in , U proj);
		}

		
		class MaxTester<U extends RealType<U>> implements Tester<U>
		{
			@Override
			public void update(U in, U proj) {
				proj.set(in);
			}

			@Override
			public boolean test(U in, U proj) {
				return proj.compareTo(in) > 0;
			}
		}
		
		
		class MinTester<U extends RealType<U>> implements Tester<U>
		{
			@Override
			public void update(U in, U proj) {
				proj.set(in);
			}

			@Override
			public boolean test(U in, U proj) {
				return proj.compareTo(in) < 0;
			}
		}
		
		
		class SumTester<U extends RealType<U>> implements Tester<U>
		{
			@Override
			public void update(U in, U proj) {
				proj.add( in );
			}

			@Override
			public boolean test(U in, U proj) {
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
			RandomAccessibleInterval<UnsignedByteType> output = (RandomAccessibleInterval<UnsignedByteType>)
									cip.slice( img , cip.list(2,3) , cip.list(1,2)  );
					
			//cip.create( cip.aslist(100, 50, 2) , 10, "double"  );
			
			String str = output==null ? "null" : output.toString();
			
			System.out.println("hello create image:" + str );
			ij.ui().show(output);
			
			System.out.println("done!");
		}
		
	
}
