package invizio.cip.filter;

import java.util.Arrays;
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
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;


/**
 * 
 * @author Benoit Lombardot
 *
 */


// TODO
//	[x] control the number of threads used -> it uses threadService
//	[x] ops implementation relies on imglib2 gauss3
	
	@Plugin(type = Op.class, name="Gauss Convolution", headless = true)
	public class GaussCIP  < T extends RealType<T> & NativeType< T > > extends AbstractOp 
	{
		
		
		
		@Parameter (type = ItemIO.INPUT)
		private RandomAccessibleInterval<T> inputImage;
		
		@Parameter( label="radius/radii", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private Float[] radius;

		@Parameter( label="Boundary handling", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private String boundaryMethod;
		
		String[] boundaryMethods = new String[] {"zero", "one", "min", "max", "same", "mirror", "periodic"};
		
		
		
		@Parameter( label="Pixel size", persist=false, required=false ) // with persist and required set to false the parameter become optional
		private Float[] pixelSize;
				
		
		@Parameter (type = ItemIO.OUTPUT)
		private	RandomAccessibleInterval<FloatType> outputImage;
		
		
		@Parameter
		OpService op;
		
		
		
		T minT;
		T maxT;
		
		
		
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
			double[] pixRadius = new double[nDim];
			for(int d=0; d<nDim; d++ ) {
				pixRadius[d] = radius[d] / pixelSize[d];
			}
			
			
			
			
			
			if( boundaryMethod == null  ||  !Arrays.asList( boundaryMethods ).contains( boundaryMethod )  )
				boundaryMethod = "mirror";
			
			if ( boundaryMethod.equals("min") || boundaryMethod.equals("max") )
				computeMinMax();
			
			T valueT = inputImage.randomAccess().get().createVariable();
			
			if ( boundaryMethod.equals( "min" ) ){
				valueT = minT;
				boundaryMethod="value";
			}
			else if ( boundaryMethod.equals( "max" ) ){
				valueT = maxT;
				boundaryMethod="value";
			}
			else if ( boundaryMethod.equals( "zero" ) ){
				valueT.setZero();
				boundaryMethod="value";
			}
			else if ( boundaryMethod.equals( "one" ) ){
				valueT.setOne();
				boundaryMethod="value";
			}

			OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outOfBoundFactory =
					Format.outOfBoundFactory( boundaryMethod, valueT );
			
			
			ImgFactory<FloatType> imgFactory = Util.getArrayOrCellImgFactory( inputImage, new FloatType(0) );
			outputImage = imgFactory.create( inputImage, new FloatType(0) );
			
			
			op.filter().gauss(outputImage, inputImage, pixRadius, outOfBoundFactory );
			
			
			
			
			
		}


		private void computeMinMax(){
			
			if( minT==null ) {
				minT = inputImage.randomAccess().get().createVariable();
				maxT = minT.createVariable();
				ComputeMinMax.computeMinMax(inputImage, minT, maxT);
			}
		}
		
		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			//ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
			ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs32.tif");
			ij.ui().show(imp);
			
			
			Img<FloatType> img = ImageJFunctions.wrap(imp);
			float threshold = 100;
			//Float[] pixelSize = new Float[] { 1f , 0.1f};
			//Float pixelSize = 0.5f;
			List<Double> pixelSize = CIP.list( 1, 1 );
			List<Double> radius = CIP.list( 10 , 10 );
			String boundary = "mirror";
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<FloatType> output = (RandomAccessibleInterval<FloatType>)
						cip.gauss(img, radius, "boundary", "min"  );
			
			String str = output==null ? "null" : output.toString();
			
			System.out.println("hello gauss blur:" + str );
			ij.ui().show(output);
			
			System.out.println("done!");
		}
		
	
}
