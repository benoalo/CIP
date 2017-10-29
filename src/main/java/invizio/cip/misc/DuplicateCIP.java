package invizio.cip.misc;


import java.io.IOException;
import java.util.Arrays;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import net.imagej.Dataset;
import net.imagej.ImageJ;

import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imagej.ops.Op;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;



/**
 * 
 * @author Benoit Lombardot
 *
 */


	@Plugin(type = Op.class, name="DuplicateCIP", headless = true)
	public class DuplicateCIP  < T extends RealType<T> > extends AbstractOp 
	{
		
		
		
		@Parameter (type = ItemIO.INPUT)
		private RandomAccessibleInterval<T> inputImage;
		
		@Parameter( label="origin", persist=false, required=false  ) 
		private Long[] origin;

		@Parameter( label="size", persist=false, required=false  ) 
		private Long[] size;

		@Parameter( label="method", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String method = "shallow";

		@Parameter (type = ItemIO.OUTPUT)
		private	RandomAccessibleInterval<T> outputImage;
		
		
		@Parameter
		OpService op;
		
		
		
		@Override
		public void run() {
			
			if( inputImage == null )
			{	//TODO: error message
				return;
			}
			
			int nDim = inputImage.numDimensions();

			if ( origin == null ) {
				origin = new Long[nDim];
			}
			
			long[] min = new long[nDim];
			inputImage.min(min);
			long[] max = new long[nDim];
			inputImage.max(max);

			if ( size == null ) {
				size = new Long[nDim];
				for (int d=0; d<nDim; d++)
					size[d] = max[d] - min[d] + 1 ;
			}
			
			RandomAccessibleInterval<T> temp = null;
			
			if( origin != null && size != null  && origin.length == size.length )
			{	
				long[] min2 = new long[nDim];
				long[] max2 = new long[nDim];
				for( int i=0; i<origin.length; i++)
				{
					min2[i] = origin[i] + min[i];
					max2[i] = min2[i] + size[i] - 1;
				}
				
//				System.out.println("input min "+ Arrays.toString(min) );
//				System.out.println("input max "+ Arrays.toString(max3) );
//				System.out.println("target min "+ Arrays.toString(origin2) );
//				System.out.println("target max "+ Arrays.toString(max2) );
				
				//temp = Views.interval( inputImage, new FinalInterval(origin2 , max2)  );
				
				temp = Views.offsetInterval( inputImage, new FinalInterval(min2 , max2)  );
			}
//			else if( origin == null && size == null )
//			{
//				long[] min = new long[nDim];
//				long[] max = new long[nDim];
//				inputImage.min(min);
//				inputImage.max(max);
//				temp = Views.interval( inputImage, min , max  );
//
//			}
			else
			{
				// TODO: error message, dimensions and position should exist and have the same size 
				return;
			}
			
			
			temp = Views.dropSingletonDimensions( temp);
			
			
			if( method.toLowerCase().equals("deep") )
			{
				outputImage = op.copy().rai( temp);
			}
			else {
				outputImage = temp;
			}
			
			
			long[] min4 = new long[nDim];
			outputImage.min(min4);
			long[] max4 = new long[nDim];
			outputImage.max(max4);
			System.out.println("output min "+ Arrays.toString(min4) );
			System.out.println("output max "+ Arrays.toString(max4) );
			
			
		}



		
		
		public static void main(final String... args) throws IOException
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			//ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
			ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs.tif");
			Img<UnsignedByteType> img = ImageJFunctions.wrap(imp);
			
			//Dataset dataset = (Dataset) ij.io().open("C:/Users/Ben/workspace/testImages/blobs.tif");
			//Img<?> img2 = dataset.getImgPlus().getImg(); 
			ij.ui().show(img);
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			
			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<UnsignedByteType> output = (RandomAccessibleInterval<UnsignedByteType>)
									cip.duplicate( img, cip.aslist(50,50), cip.aslist(150,150));
					
			
			String str = output==null ? "null" : output.toString();
			
			//System.out.println("hello duplicate image:" + str );
			ij.ui().show(output);
			
			ImageJFunctions.show(output);
			
			System.out.println("done!");
		}
		
	
}
