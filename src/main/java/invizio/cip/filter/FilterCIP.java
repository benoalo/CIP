package invizio.cip.filter;

import java.util.List;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import invizio.cip.parameters.Format;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imagej.ops.Op;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;


/**
 * 
 * @author Benoit Lombardot
 *
 */

	
	@Plugin(type = Op.class, name="filter", headless = true)
	public class FilterCIP  < T extends RealType<T> & NativeType< T > > extends AbstractOp 
	{
		
		String[] methods = new String[] {"gauss", "erode", "dilate", "tophat", "open" , "close"} ;
		
		
		@Parameter (type = ItemIO.INPUT)
		private RandomAccessibleInterval<T> inputImage;
		
		@Parameter( label="radius/radii", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private Float[] radius;

		@Parameter( label="Pixel size", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private Float[] pixelSize;
		
		@Parameter( label="Method", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String method;
		
		
		@Parameter (type = ItemIO.OUTPUT)
		private	RandomAccessibleInterval<T> outputImage;
		
		
		@Parameter
		OpService op;
		
		
		
		@Override
		public void run() {
			
			if ( inputImage == null){
				//TODO: Error! no image was provided
				return;
			}
			
			
			

			int nDim = inputImage.numDimensions();
			
			pixelSize = Format.perDim(pixelSize, nDim);
			if (pixelSize == null )
				return;

			radius = Format.perDim(radius, nDim);
			if (radius == null )
				return;
			
			// radius is assumed to in the same unit as pixelSize
			for(int d=0; d<nDim; d++ ) {
				radius[d] /= pixelSize[d];
			}
			
			
			
			
			
			
			
		}

		
		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
			//ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs32.tif");
			ij.ui().show(imp);
			
			
			Img<FloatType> img = ImageJFunctions.wrap(imp);
			float threshold = 100;
			//Float[] pixelSize = new Float[] { 1f , 0.1f};
			//Float pixelSize = 0.5f;
			List<Double> pixelSize = CIP.list( 1, 0.5 );
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<IntType> distMap = (RandomAccessibleInterval<IntType>)
						cip.distance(img, threshold, CIP.asimg( 1, 0.5)  );
			
			String str = distMap==null ? "null" : distMap.toString();
			
			System.out.println("hello distmap:" + str );
			ij.ui().show(distMap);
			
			System.out.println("done!");
		}
		
	
}
