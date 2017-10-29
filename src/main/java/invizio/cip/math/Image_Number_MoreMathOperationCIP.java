package invizio.cip.math;


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
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */

	
	@Plugin(type=CIP.MathBinary.class, name="Image_Number_MoreMathOperationCIP", headless = true)
	public class Image_Number_MoreMathOperationCIP  < T extends RealType<T> > extends AbstractOp 
	{
		@Parameter (type = ItemIO.INPUT, persist=false)
		private  String operationType;
		
		@Parameter (type = ItemIO.INPUT, persist=false)
		private RandomAccessibleInterval<T> inputImage;
		
		@Parameter ( label="value", persist=false )
		private T valueT;
		

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
			
			int nDim1 = inputImage.numDimensions();
			long[] dims1 = new long[nDim1];
			inputImage.dimensions(dims1);
						
			
			////////////////////////////////////////////////
			// process the image 
			////////////////////////////////////////////////
			
			//IterableInterval<T> inIter = Views.iterable(inputImage);
			//outputImage = (Img<T>) opService.run("math."+OperationType, inIter, valueT );
			
			AbstractBinaryComputerOp<T, T, T> mapper = null;
			switch( operationType )
			{
			case "min":
				mapper = new MoreMathOperationCIP.Min<T>();
				break;
			case "max":
				mapper = new MoreMathOperationCIP.Max<T>();
				break;
			case "pow":
				mapper = new MoreMathOperationCIP.Pow<T>();
				break;
			default :
				mapper = null;
				break;
			}
			
			IterableInterval<T> inIter = Views.iterable(inputImage);
			
			IterableInterval<T> valueIter = (Img<T>)opService.create().img(inputImage);
			valueIter =  (Img<T>) opService.run("math.add", valueIter, valueT);
			
			outputImage = (Img<T>) opService.create().img(inputImage);
			opService.map(outputImage, inIter, valueIter, mapper );
			
			
		}

		
		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
			//ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs32.tif");
			ij.ui().show(imp);
			
			
			Img<FloatType> img = ImageJFunctions.wrap(imp);
			Img<ShortType> img2 = ij.op().convert().int16( img );
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			FloatType valT = new FloatType();
			valT.set(10.5f);
			RandomAccessibleInterval<FloatType> sumImg = (RandomAccessibleInterval<FloatType>) cip.min( img , 100  );
			//RandomAccessibleInterval<ShortType> sumImg = (RandomAccessibleInterval<ShortType>) cip.max( img2 , 100 );
			
			String str = sumImg==null ? "null" : sumImg.toString();
			//String str2 = sumImg2==null ? "null" : sumImg2.toString();
			
			System.out.println("hello maxImage:" + str );
			//System.out.println("hello addImage:" + str2 );
			ij.ui().show(sumImg);
			//ij.ui().show(sumImg2);
			
			System.out.println("done!");
		}
		
	
}
