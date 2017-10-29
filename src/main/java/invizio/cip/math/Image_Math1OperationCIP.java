package invizio.cip.math;


import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imagej.ops.special.computer.AbstractBinaryComputerOp;
import net.imagej.ops.special.computer.AbstractUnaryComputerOp;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 */


/*
	 * 
	 * TODO:
	 * 	[-] type casting is not generic, int64 or double would be downcasted to int32 or float32
	 * 
	 */

	
	@Plugin(type = CIP.MathBinary.class, name="Image_Math1OperationCIP", headless = true)
	public class Image_Math1OperationCIP  < T extends RealType<T> , U extends RealType<U> > extends AbstractOp 
	{
		
		@Parameter (type = ItemIO.INPUT, persist=false)
		private  String operationType;
		
		@Parameter (type = ItemIO.INPUT, persist=false)
		private RandomAccessibleInterval<T> inputImage;
		
		@Parameter (type = ItemIO.OUTPUT)
		private RandomAccessibleInterval outputImage;

		@Parameter
		private OpService opService;
		
		@Override
		public void run() {
			
			// check input parameters
			
			if ( inputImage == null ){
				//TODO: Error! no image was provided
				return;
			}
			
			final T valueT = inputImage.randomAccess().get().createVariable();
			AbstractUnaryComputerOp<T, ? extends RealType<?> > mapper = null;
			switch( operationType )
			{
			case "cos":
				mapper = new Math1OperationCIP.Cos<T,FloatType>();
				outputImage = opService.create().img( inputImage , new FloatType() );
				break;
			case "sin":
				mapper = new Math1OperationCIP.Sin<T,FloatType>();
				outputImage = opService.create().img( inputImage , new FloatType() );
				break;
			case "tan":
				mapper = new Math1OperationCIP.Tan<T,FloatType>();
				outputImage = opService.create().img( inputImage , new FloatType() );
				break;
			case "acos":
				mapper = new Math1OperationCIP.ACos<T,FloatType>();
				outputImage = opService.create().img( inputImage , new FloatType() );
				break;
			case "asin":
				mapper = new Math1OperationCIP.ASin<T,FloatType>();
				outputImage = opService.create().img( inputImage , new FloatType() );
				break;
			case "atan":
				mapper = new Math1OperationCIP.ATan<T,FloatType>();
				outputImage = opService.create().img( inputImage , new FloatType() );
				break;
			case "log":
				mapper = new Math1OperationCIP.Log<T,FloatType>();
				outputImage = opService.create().img( inputImage , new FloatType() );
				break;
			case "exp":
				mapper = new Math1OperationCIP.Exp<T,FloatType>();
				outputImage = opService.create().img( inputImage , new FloatType() );
				break;
			case "sqrt":
				mapper = new Math1OperationCIP.Sqrt<T,FloatType>();
				outputImage = opService.create().img( inputImage , new FloatType() );
				break;
			case "abs":
				mapper = new Math1OperationCIP.Abs<T,T>();
				outputImage = opService.create().img( inputImage , new IntType() );
				break;
			case "round":
				mapper = new Math1OperationCIP.Round<T,T>();
				outputImage = opService.create().img( inputImage , new IntType() );
				break;
			case "floor":
				mapper = new Math1OperationCIP.Floor<T,T>();
				outputImage = opService.create().img( inputImage , new IntType()  );
				break;
			case "ceil":
				mapper = new Math1OperationCIP.Ceil<T,T>();
				outputImage = opService.create().img( inputImage , new IntType()  );
				break;
			case "sign":
				mapper = new Math1OperationCIP.Sign<T,ByteType>();
				outputImage = opService.create().img( inputImage , new ByteType() );
				break;
			default :
				mapper = null;
				break;
			}
			
			
			
			IterableInterval<T> in1Iter = Views.iterable(inputImage);
			opService.map(outputImage, in1Iter, mapper );
			
			
			
		}

		
		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			//ImagePlus imp = IJ.openImage("F:\\projects\\blobs.tif");
			ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs.tif");
			ij.ui().show(imp);
			
			
			Img<ByteType> img = ImageJFunctions.wrap(imp);
			//Img<IntType> img2 = ij.op().convert().int32( img );
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			//@SuppressWarnings("unchecked")
			RandomAccessibleInterval<?> sumImg = (RandomAccessibleInterval<?>)
						cip.cos( img );
			
			String str = sumImg==null ? "null" : sumImg.toString();
			
			System.out.println("hello cos:" + str );
			ij.ui().show(sumImg);
			
			System.out.println("done!");
		}
		
	
}
