package invizio.cip.filter;

import java.util.List;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import invizio.imgalgo.util.RAI;
import ij.IJ;
import ij.ImagePlus;

import invizio.cip.CIP;

import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imagej.ops.Op;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.BooleanType;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */

	
	@Plugin(type = Op.class, name="DistanceCIP", headless = true)
	public class DistanceCIP  < T extends RealType<T> & NativeType< T > > extends AbstractOp 
	{
		@Parameter (type = ItemIO.INPUT)
		private RandomAccessibleInterval<T> inputImage;
		
		@Parameter( label="Intensity threshold", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private Float threshold;
		
		@Parameter( label="Pixel size", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private Float[] pixelSize;
		
		@Parameter (type = ItemIO.OUTPUT)
		private	RandomAccessibleInterval<IntType> distanceMap;
		
		@Parameter
		OpService op;
		
		
		String imageType = "numeric";
		
		@Override
		public void run() {
			
			if ( inputImage == null){
				//TODO: Error! no image was provided
				return;
			}
			
			
			if ( inputImage.randomAccess().get() instanceof BooleanType ){
				imageType = "boolean";
				
			}

			if( imageType.equals("boolean") && threshold!=null )
			{
				//TODO: Warnin! the image is logical, the threshold value will not be used 
			}
			else if( imageType.equals("numeric") && threshold==null )
			{
				//TODO: Error! the image is numeric but no threshold is provided
				return;
			}

			int nDim = inputImage.numDimensions();
			if( pixelSize == null ) {
				pixelSize = new Float[nDim];
				for(int d=0 ; d<nDim ; d++) {
					pixelSize[d] = 1f;
				}
			}
			else if( pixelSize.length == 1 )
			{
				Float pixelSize0 = pixelSize[0];
				pixelSize = new Float[nDim];
				for(int d=0 ; d<nDim ; d++) {
					pixelSize[d] = pixelSize0;
				}
			}
			else if( pixelSize.length < nDim )
			{
				//TODO: Error, the pixelSize is not consistent with the image dimension ( pixelSize.length vs. nDim )
			}
			else if( pixelSize.length > nDim )
			{
				Float[] pixelSize0 = pixelSize;
				pixelSize = new Float[nDim];
				for(int d=0 ; d<nDim ; d++) {
					pixelSize[d] = pixelSize0[d];
				}
				
				//TODO: Warning! to many elements in pixelSize, only the nDim first will be used 
			}
			
			RandomAccessibleInterval<BitType> mask;
			
			if( imageType.equals("numeric") )
			{
				// duplicate the original image
				RandomAccessibleInterval<T> inputCopy = RAI.duplicate( inputImage );
				
				// threshold the input image
				T T_threshold = inputCopy.randomAccess().get().createVariable();
				T_threshold.setReal( threshold );
				
				
				mask = (RandomAccessibleInterval<BitType>) op.threshold().apply(Views.iterable(inputCopy), T_threshold);
			}
			else if(inputImage.randomAccess().get() instanceof BitType)
			{
				mask = (RandomAccessibleInterval) inputImage;
			}else{
                throw new IllegalArgumentException("unsupported image type" + inputImage.randomAccess().get());
            }
			
			// build a distance map
			if( nDim==1)
				distanceMap = op.image().distancetransform( mask , pixelSize[0]);
			else if( nDim==2)
				distanceMap = op.image().distancetransform( mask , pixelSize[0], pixelSize[1]);
			else if( nDim==3)
				distanceMap = op.image().distancetransform( mask , pixelSize[0], pixelSize[1], pixelSize[2]);
			else if( nDim==4)
				distanceMap = op.image().distancetransform( mask , pixelSize[0], pixelSize[1], pixelSize[2], pixelSize[3]);
			
			
		}

		
		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
			ij.ui().show(imp);
			
			
			Img<FloatType> img = ImageJFunctions.wrap(imp);
			float threshold = 100;
			//Float[] pixelSize = new Float[] { 1f , 0.1f};
			//Float pixelSize = 0.5f;
			//List<Double> pixelSize = CIP.list( 1, 0.5 );
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<IntType> distMap = (RandomAccessibleInterval<IntType>)
						cip.distance(img, threshold, CIP.list( 1, 0.5)  );
			
			String str = distMap==null ? "null" : distMap.toString();
			
			System.out.println("hello distmap:" + str );
			ij.ui().show(distMap);
			
			System.out.println("done!");
		}
		
	
}
