package invizio.cip.filter;


import java.util.List;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import invizio.cip.CIPService;
import net.imagej.ImageJ;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
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
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */


	// Comments and todos : see AbstractMathMorphoCIP
	
	@Plugin(type = Op.class, name="Tophat", headless = true)
	public class TophatCIP  < T extends RealType<T> & NativeType< T > > extends AbstractMathMorphoCIP<T> 
	{
		
		
		@Parameter
		private OpService opService;
				
		@Override
		public void processInput(RandomAccessibleInterval<T> source, List< Shape > strels , OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outOfBoundFactory, int nThread )
		{
			
			///////////////////////////////////////////////////////////////////////
			// process the input image											
			///////////////////////////////////////////////////////////////////////
			
			RandomAccessibleInterval<T> target = source;
			for ( final Shape strel : strels )
			{
				target = ErosionCIP.erodeFull( target, strel, nThread, outOfBoundFactory );	
			}

			for ( final Shape strel : strels )
			{
				target = DilationCIP.dilateFull( target, strel, nThread, outOfBoundFactory );	
			}
			
			// output type parameter is ignored, output is always the size of the input
			ImgFactory<T> imgFactory = Util.getArrayOrCellImgFactory( target, target.randomAccess().get().createVariable() );
			Img<T> target2 = ImgView.wrap( target, imgFactory);
			target2 = TempUtils.copyCropped( target2, inputImage, nThread );
			
			
			IterableInterval<T> inputIterable = Views.iterable(inputImage);
			//IterableInterval<T> outputIterable = null;
			if( inputIterable.iterationOrder().equals( target2.iterationOrder() ) )
			{
				System.out.println("hello iteration orders");
				outputImage = (Img<T>) opService.math().subtract( inputIterable, (IterableInterval<T>)target2 );
			}
			else
			{
				outputImage = (Img<T>) opService.math().subtract( inputIterable, (RandomAccessibleInterval<T>)target2 );
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
			List<Double> pixelSize = CIP.list( 1, 1 );
			List<Double> radius = CIP.list( 5 , 5 );
			String boundary = "same";
			String shape = "disk";
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			@SuppressWarnings("unchecked")
			
			
			RandomAccessibleInterval<FloatType> output = (RandomAccessibleInterval<FloatType>)
						cip.tophat(img, radius, "shape", shape, "boundary", boundary, "pixelSize", pixelSize);
			
			String str = output==null ? "null" : output.toString();
			
			System.out.println("hello top hat:" + str );
			ij.ui().show(output);
			
			System.out.println("done!");
		}

		
	
}
