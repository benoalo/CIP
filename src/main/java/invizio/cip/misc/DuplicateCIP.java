package invizio.cip.misc;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import invizio.cip.MetadataCIP;
import invizio.cip.MetadataCIP2;
import invizio.cip.RaiCIP;
import invizio.cip.RaiCIP2;
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
import net.imglib2.type.numeric.real.FloatType;
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
		private RaiCIP2<T> inputImage;
		
		@Parameter( label="origin", persist=false, required=false  ) 
		private Long[] origin;

		@Parameter( label="size", persist=false, required=false  ) 
		private Long[] size;

		@Parameter( label="method", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String method = "shallow";

		@Parameter (type = ItemIO.OUTPUT)
		private	RaiCIP2<T> outputImage;
		
		
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
				for (int d=0; d<nDim; d++)
					origin[d] = new Long(0) ;
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
					max2[i] = min2[i] + Math.max(size[i],1) - 1;
				}
				
				
				temp = Views.offsetInterval( inputImage, new FinalInterval(min2 , max2)  );
			}
			else
			{
				// TODO: error message, dimensions and position should exist and have the same size 
				return;
			}
			
			
			temp = Views.dropSingletonDimensions( temp);
			
			
			RandomAccessibleInterval<T> outputRAI;
			if( method.toLowerCase().equals("deep") )
			{
				outputRAI = op.copy().rai( temp);
			}
			else {
				outputRAI = temp;
			}
			
			// adapt metadata for the output image
			MetadataCIP2 metadata = new MetadataCIP2( inputImage );
			//remove the singleton dimensions
			List<Integer> dimsToDrop = new ArrayList<Integer>();
			for(int i=0 ; i<size.length ; i++ ) {
				if( size[i]<=1 )
					dimsToDrop.add(i);
			}
			Integer[] dims = dimsToDrop.toArray(new Integer[0]);
			metadata.dropDimensions( dims );	
			
			
			outputImage = new RaiCIP2<T>(outputRAI , metadata );
			
			
//			long[] min4 = new long[nDim];
//			outputImage.min(min4);
//			long[] max4 = new long[nDim];
//			outputImage.max(max4);
//			System.out.println("output min "+ Arrays.toString(min4) );
//			System.out.println("output max "+ Arrays.toString(max4) );
			
			
		}



		
		
		public static void main(final String... args) throws IOException
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
			Img<FloatType> img = ImageJFunctions.wrap(imp);
			
			//Dataset dataset = (Dataset) ij.io().open("C:/Users/Ben/workspace/testImages/blobs.tif");
			//Img<?> img2 = dataset.getImgPlus().getImg(); 
			ij.ui().show(img);
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			
			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<FloatType> output = (RandomAccessibleInterval<FloatType>)
									cip.duplicate( img);//, cip.aslist(50,50), cip.aslist(150,150));
					
			
			String str = output==null ? "null" : output.toString();
			
			System.out.println("hello duplicate image:" + str );
			ij.ui().show(output);
			
			ImageJFunctions.show(output);
			
			System.out.println("done!");
		}
		
	
}
