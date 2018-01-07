package invizio.cip.filter;

import java.util.Arrays;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;

import invizio.cip.parameters.Format;
import net.imagej.ops.AbstractOp;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.StructuringElements;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgView;

import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;


	// TODO:
	//	[x] create an abstract class morphology op to centralize parameter handling (should be the same for erode, dilate, open, close, tophat )
	// 	[-] are all the parameters needed ?
	//	[x] implement open
	//	[x] implement close
	//	[x] implement top hat
	

	//Current implementation relies on imglib2-algorithms
	// plan to implement wilkinson/urbach strategy for random structuring element in ImgAlgo, possibly fast rectangle implementation by 
	
	/**
	 * 
	 * @author Benoit Lombardot
	 *
	 */
	
	abstract class AbstractMathMorphoCIP  < T extends RealType<T> & NativeType< T > > extends AbstractOp 
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
		
		
		
		//Parameter( label="output size", persist=false, required=false ) // with persist and required set to false the parameter become optional
		protected String outputType="same"; // "same" for same dimension of the image, "full" the output will be larger by the strel diameter - 1
		String[] outputTypes = new String[] { "same" , "full" }; // how about adding "valid"?
		
		
		
		@Parameter( label="Pixel size", persist=false, required=false ) // with persist and required set to false the parameter become optional
		protected Float[] pixelSize;
		
		
		@Parameter( label="number of Threads", persist=false, required=false ) // with persist and required set to false the parameter become optional
		protected Integer nThread = 1 ;
		
		
		
		@Parameter (type = ItemIO.OUTPUT)
		protected	RandomAccessibleInterval<T> outputImage;
		
		
		
		T minT;
		T maxT;
		
		public abstract void processInput(RandomAccessibleInterval<T> source, List< Shape > strels , OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outOfBoundFactory, int nThread );
		
		
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

			
			
			if ( outputType == null )
				outputType = "same";
			if ( ! Arrays.asList(outputTypes).contains(outputType) )
				outputType = "same";
			
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
			boolean decompose = true;
			
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
				strels = StructuringElements.disk( (long)pixRadius[0], nDim );
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
			
			processInput( inputImage , strels , outOfBoundFactory , nThread );
			
			
			
			
		}

		
		
		private void computeMinMax(){
			
			if( minT==null ) {
				minT = inputImage.randomAccess().get().createVariable();
				maxT = minT.createVariable();
				ComputeMinMax.computeMinMax(inputImage, minT, maxT);
			}
		}
		
		
		
	
}
