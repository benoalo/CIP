package invizio.cip.misc;


import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import invizio.cip.MetadataCIP;
import invizio.cip.RAI_CIP;
import net.imagej.ImageJ;

import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imagej.ops.Op;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */

	@Plugin(type = Op.class, name="SliceCIP", headless = true)
	public class SliceCIP  < T extends RealType<T> > extends AbstractOp 
	{
		
		
		
		@Parameter (type = ItemIO.INPUT)
		private RAI_CIP<T> inputImage;
		
		@Parameter( label="dimensions", persist=false ) 
		private Integer[] dimensions;

		@Parameter( label="position", persist=false ) 
		private Long[] position;

		@Parameter( label="method", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String method = "shallow";

		@Parameter (type = ItemIO.OUTPUT)
		private	RAI_CIP<T> outputImage;
		
		
		@Parameter
		OpService op;
		
		
		
		@Override
		public void run() {
			
			if( inputImage == null )
			{	//TODO: error message
				return;
			}
			
			int nDim = inputImage.numDimensions();
			long[] min = new long[nDim];
			long [] max = new long[nDim];
			inputImage.min(min);
			inputImage.max(max);
			
			if( dimensions != null && position != null  && dimensions.length == position.length )
			{	
				for( int i=0; i<dimensions.length; i++)
				{
					int d = dimensions[i];
					long x = position[i];
					min[d] = x;
					max[d] = x;
				}
			}
			else
			{
				// TODO: error message, dimensions and position should exist and have the same size 
				return;
			}
			
			
			RandomAccessibleInterval<T> temp = Views.offsetInterval( inputImage, new FinalInterval( min , max ) );
			temp = Views.dropSingletonDimensions( temp );
			
			RandomAccessibleInterval<T> raiTmp;
			if( method.toLowerCase().equals("deep") )
			{
				raiTmp= op.copy().rai( temp);
			}
			else {
				raiTmp = temp;
			}
			
			
			// adapt input metadata for the output
			MetadataCIP metadata = new MetadataCIP( inputImage );
			if ( dimensions != null && position != null )
			{
				// remove unecessary dimensions information
				metadata.dropDimensions( dimensions );
				
				// remove unecessary lut if slicing is channel dim
				for(int i=0; i<dimensions.length; i++ ) {
					int d = dimensions[i];
					long pos = position[i];
					if( d == metadata.channelDim )
						metadata.dropLutsOutOfRange((int)pos, (int)pos);
				}
			}
			
			outputImage = new RAI_CIP<T>(raiTmp , metadata );
			
			
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
