package invizio.cip.filter;


import java.util.List;

import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import net.imagej.ImageJ;
import net.imagej.ops.Op;

import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.Erosion;
import net.imglib2.algorithm.morphology.MorphologyUtils;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * 
 * @author Benoit Lombardot
 *
 */


	// Comments and todos : see dilationCIP
	
	@Plugin(type = Op.class, name="Erosion", headless = true)
	public class ErosionCIP  < T extends RealType<T> & NativeType< T > > extends AbstractMathMorphoCIP<T> 
	{
		
				
		@Override
		public void processInput(RandomAccessibleInterval<T> source, List< Shape > strels , OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outOfBoundFactory, int nThread )
		{
			///////////////////////////////////////////////////////////////////////
			// process the input image											
			///////////////////////////////////////////////////////////////////////
			
			RandomAccessibleInterval<T> target = source;
			for ( final Shape strel : strels )
			{
				target = erodeFull( target, strel, nThread, outOfBoundFactory );	
			}

			if( outputType.equals("same") )
			{
				ImgFactory<T> imgFactory = Util.getArrayOrCellImgFactory( target, target.randomAccess().get().createVariable() );
				Img<T> target2 = ImgView.wrap( target, imgFactory);
				outputImage = TempUtils.copyCropped( target2, inputImage, nThread );
			}
			else
				outputImage = target;

		}

		
		
		static < T extends RealType<T> & NativeType< T > > RandomAccessibleInterval<T> erodeFull(  RandomAccessibleInterval<T> source, Shape strel, int nThread, OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outOfBoundFactory  )
		{
			final long[][] dimAndOffset = MorphologyUtils.computeTargetImageDimensionsAndOffset( source, strel );
			final long[] targetDims = dimAndOffset[ 0 ];
			final long[] offset = dimAndOffset[ 1 ];
			
			//final Img< T > target = source.factory().create( targetDims, source.randomAccess().get().copy() );
			T valueT = source.randomAccess().get().createVariable();
			final RandomAccessibleInterval<T> target = Util.getArrayOrCellImgFactory( new FinalDimensions( targetDims ) , valueT ).create(targetDims, valueT);
			final IntervalView< T > offsetTarget = Views.offset( target, offset );
			
			// next 3 lines to replace MorphologyUtils.createVariable( source, source ) that is not visible
			// Get an instance of type T from source that is positioned at the min of interval (cf. utils Javadoc)
			final RandomAccess< T > a = source.randomAccess();
			source.min( a );
			final T minVal = a.get().createVariable();
			minVal.setReal( minVal.getMinValue() );
			
			
			final ExtendedRandomAccessibleInterval< T, RandomAccessibleInterval< T >> extended = Views.extend(source, outOfBoundFactory );

			Erosion.erode( extended, offsetTarget, strel, nThread );
			
			return target;
		}

		

		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			ImagePlus imp = IJ.openImage(	"C:/Users/Ben/workspace/testImages/blobs32.tif" 	);
			
			//ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
			ij.ui().show(imp);
			
			
			Img<FloatType> img = ImageJFunctions.wrap(imp);
			List<Object> pixelSize = CIP.list( 1, 1 );
			List<Object> radius = CIP.list( 5 , 5 );
			String boundary = "same";
			String shape = "disk";
			//String outputType = "same";
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			@SuppressWarnings("unchecked")
			
			
			RandomAccessibleInterval<FloatType> output = (RandomAccessibleInterval<FloatType>)
						cip.erode(img, radius, "shape", shape, "boundary", boundary, "pixelSize", pixelSize);
			
			String str = output==null ? "null" : output.toString();
			
			System.out.println("hello erosion:" + str );
			ij.ui().show(output);
			
			System.out.println("done!");
		}

		
	
}
