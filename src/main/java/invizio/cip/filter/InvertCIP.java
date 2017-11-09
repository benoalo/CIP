package invizio.cip.filter;


import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ops.special.computer.AbstractBinaryComputerOp;
import net.imagej.ops.special.computer.AbstractUnaryComputerOp;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */

	
	@Plugin(type = CIP.MathBinary.class, name="Image_Image_MathOperationCIP", headless = true)
	public class InvertCIP  < T extends RealType<T> > extends AbstractOp 
	{
		
		
		
		@Parameter (type = ItemIO.INPUT, persist=false)
		private RandomAccessibleInterval<T> inputImage;
		
		@Parameter (type = ItemIO.OUTPUT)
		private RandomAccessibleInterval<T> outputImage;

		@Parameter
		private OpService opService;
		
		
		
		@Override
		public void run() {
			
			// check input parameters
			
			if ( inputImage == null ){
				//TODO: Error! no image was provided
				return;
			}
						
			
			computeMinMax();
			AbstractUnaryComputerOp<T, T> mapper =  new InvertComputationCIP<T>( minT , maxT );
			
			IterableInterval<T> inputIter = Views.iterable(inputImage);
			outputImage = (Img<T>) opService.create().img(inputImage);
			opService.map(outputImage, inputIter, mapper );
			
			
			
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
		
		
		
		@Plugin(type = Op.class , name = "InvertComputationCIP")
		public static class InvertComputationCIP< T extends RealType<T> > extends AbstractUnaryComputerOp<T, T>
		{
			final double max;
			final double min;
			
			public InvertComputationCIP(T maxT, T minT )
			{
				super();
				max = maxT.getRealDouble();
				min = minT.getRealDouble();
			}
			
			@Override
			public void compute(final T input, final T output) {
				output.setReal(  max + min - input.getRealDouble()  );
			}
		}
		
		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
			ij.ui().show(imp);
			
			Img<FloatType> img = ImageJFunctions.wrap(imp);
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			//@SuppressWarnings("unchecked")
			RandomAccessibleInterval<?> sumImg = (RandomAccessibleInterval<?>)
						cip.invert( img );
			
			String str = sumImg==null ? "null" : sumImg.toString();
			
			System.out.println("hello invert:" + str );
			ij.ui().show(sumImg);
			
			System.out.println("done!");
		}
		
	
}
