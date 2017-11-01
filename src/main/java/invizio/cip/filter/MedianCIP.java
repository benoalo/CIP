package invizio.cip.filter;

import java.util.ArrayList;
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
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Map;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.StructuringElements;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */


	// TODO:
	// 	[-] faster implementation
	//		[-] 2d image could rely on ij1
	//		[-] histogram implementation could be extended to 2D
	//	Current implementation is the naive one, processing is inspired from ops.filter architecture  
	
	
	@Plugin(type = Op.class, name="MedianCIP", headless = true)
	public class MedianCIP  < T extends RealType<T> & NativeType< T > > extends AbstractOp 
	{
		
		
		
		@Parameter (type = ItemIO.INPUT)
		protected RandomAccessibleInterval<T> inputImage;
		
		@Parameter( label="radius/radii", persist=false, required=false ) // with persist and required set to false the parameter become optional
		protected Float[] radius;

		@Parameter( label="Shape", persist=false, required=false ) // with persist and required set to false the parameter become optional
		protected String shape;
		
		String[] shapes = new String[] {"disk", "rectangle"};

		
		@Parameter( label="Boundary handling", persist=false, required=false ) // with persist and required set to false the parameter become optional
		protected String boundaryMethod;
		
		String[] boundaryMethods = new String[] {"min", "max", "same" , "periodic", "mirror"};
		
		
		@Parameter( label="Pixel size", persist=false, required=false ) // with persist and required set to false the parameter become optional
		protected Float[] pixelSize;
		
		
		
		@Parameter (type = ItemIO.OUTPUT)
		protected	RandomAccessibleInterval<T> outputImage;
		
		
		@Parameter
		private OpService opService;
		
		
		
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
				boundaryMethod = "same";
			
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

			
			
			
			/////////////////////////////////////////////////////////////////////////////////////
			// build the structuring element as required by user (cf. shape, pixRadius )
			/////////////////////////////////////////////////////////////////////////////////////
			
			boolean isotropic=true;
			for(int d=1 ; d<nDim; d++)
				if (  (int)pixRadius[d-1] != (int)pixRadius[d]  )
					isotropic = false;
			
			if ( shape == null )
				shape = "rectangle";
			shape = shape.toLowerCase();
			
			List< Shape > strels = null;
			int[] intPixRadius = new int[nDim];
			boolean decompose = false;
			
			if ( shape.equals("square") || shape.equals("cube") || shape.equals("hyperrectangle") )
				shape = "rectangle";
			//
			//else if ( shape.equals("sphere") || shape.equals("hypersphere") || shape.equals("ellipsoid") || shape.equals("hyperEllipsoid") )
			//	shape = "disk";
			
			if( shape.equals("disk") ) // only works in 2D at the moment
				if (nDim != 2 && !isotropic )
					shape="rectangle";

			
			switch( shape ) {
			
			case "rectangle" :
				for(int d=0; d<nDim; d++)
					intPixRadius[d] = (int) pixRadius[d];
				strels = StructuringElements.rectangle( intPixRadius , decompose );
				break;
					
			case "disk" : // only in isotropic 2D
				if( decompose )
					strels = StructuringElements.disk( (long)pixRadius[0], nDim );
				else
					strels = new ArrayList<Shape>();
					strels.add( new HyperSphereShape( (long) pixRadius[0]) );
				break;
				
			default : // rectangle
				for(int d=0; d<nDim; d++)
					intPixRadius[d] = (int) pixRadius[d];
				strels = StructuringElements.rectangle( intPixRadius , decompose );
				break;
				
			}
			
			OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outOfBoundFactory =
					Format.outOfBoundFactory( boundaryMethod, valueT );
			
			
			
			
			///////////////////////////////////////////////////////////////////////
			// process the input image											
			///////////////////////////////////////////////////////////////////////
			
			
			outputImage = opService.create().img( inputImage );
			//Iterable<T> outputIter = Views.flatIterable(outputImage);
			
			valueT = outputImage.randomAccess().get();
			@SuppressWarnings({ "unchecked", "rawtypes" })
			UnaryComputerOp< Iterable<T> , T > filterOp = (UnaryComputerOp)
					Computers.unary( opService, Ops.Stats.Median.class, valueT.getClass(), Iterable.class);
			
			UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> map = 
					Computers.unary( opService, Map.class, outputImage, inputImage, strels.get(0), filterOp);
			
			map.compute(Views.interval(Views.extend(inputImage, outOfBoundFactory), inputImage), outputImage);
			
			
			
		}

		
		
		T minT;
		T maxT;
		
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
			List<Double> pixelSize = CIP.list( 0.2, 1 );
			List<Double> radius = CIP.list( 2 , 2 );
			String boundary = "min";
			String shape = "rectangle";
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			@SuppressWarnings("unchecked")
			
			
			RandomAccessibleInterval<FloatType> output = (RandomAccessibleInterval<FloatType>)
						cip.median(img, radius, "shape", shape, "boundary", boundary, "pixelSize", pixelSize);
			
			String str = output==null ? "null" : output.toString();
			
			System.out.println("hello erosion:" + str );
			ij.ui().show(output);
			
			System.out.println("done!");
		}
		
	

}
