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


/**
 * 
 * @author Benoit Lombardot
 *
 */


	// Comments and todos : see dilationCIP
	
	@Plugin(type = Op.class, name="Closing", headless = true)
	public class ClosingCIP  < T extends RealType<T> & NativeType< T > > extends AbstractMathMorphoCIP<T> 
	{
		
		@Parameter
		private CIPService cipService;
				
		@Override
		public void processInput(RandomAccessibleInterval<T> source, List< Shape > strels , OutOfBoundsFactory<T,RandomAccessibleInterval<T>> outOfBoundFactory, int nThread )
		{
			///////////////////////////////////////////////////////////////////////
			// process the input image											
			///////////////////////////////////////////////////////////////////////
			
			RandomAccessibleInterval<T> target = source;
			for ( final Shape strel : strels )
			{
				target = DilationCIP.dilateFull( target, strel, nThread, outOfBoundFactory );	
			}

			for ( final Shape strel : strels )
			{
				target = ErosionCIP.erodeFull( target, strel, nThread, outOfBoundFactory );	
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

		
		
	
		

		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
			ij.ui().show(imp);
			
			
			Img<FloatType> img = ImageJFunctions.wrap(imp);
			List<Object> pixelSize = CIP.list( 1, 1 );
			List<Object> radius = CIP.list( 5 , 5 );
			String boundary = "same";
			String shape = "disk";
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			@SuppressWarnings("unchecked")
			
			
			RandomAccessibleInterval<FloatType> output = (RandomAccessibleInterval<FloatType>)
						cip.closing(img, radius, "shape", shape, "boundary", boundary, "pixelSize", pixelSize);
			
			String str = output==null ? "null" : output.toString();
			
			System.out.println("hello closing:" + str );
			ij.ui().show(output);
			
			System.out.println("done!");
		}

		
	
}
