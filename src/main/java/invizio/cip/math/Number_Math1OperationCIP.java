package invizio.cip.math;


import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;


/**
 * 
 * @author Benoit Lombardot
 *
 */


	
	@Plugin(type = CIP.MathBinary.class, name="Number_Math1OperationCIP", headless = true)
	public class Number_Math1OperationCIP  < T extends RealType<T> > extends AbstractOp 
	{
		
		@Parameter (type = ItemIO.INPUT, persist=false)
		private  String operationType;
		
		@Parameter (type = ItemIO.INPUT, persist=false)
		private Double input;
		
		@Parameter (type = ItemIO.OUTPUT)
		private Double output;

		
		@Override
		public void run() {
			
			// check input parameters
			
			if ( Double.isNaN(input) ) {
				output = input;
				return;
			}
						
			switch( operationType )
			{
			case "cos":
				output = Math.cos(input);
				break;
			case "sin":
				output = Math.sin(input);
				break;
			case "tan":
				output = Math.tan(input);
				break;
			case "acos":
				output = Math.acos(input);
				break;
			case "asin":
				output = Math.asin(input);
				break;
			case "atan":
				output = Math.atan(input);
				break;
			case "log":
				output = Math.log(input);
				break;
			case "exp":
				output = Math.exp(input);
				break;
			case "sqrt":
				output = Math.sqrt(input);
				break;
			case "abs":
				output = Math.abs(input);
				break;
			case "round":
				output = (double) Math.round(input);
				break;
			case "floor":
				output = Math.floor(input);
				break;
			case "ceil":
				output = Math.ceil(input);
				break;
			case "sign":
				output = Math.signum(input);
				break;
			default :
				output = null;
				break;
			}
			
			
		}

		
		
		public static void main(final String... args)
		{
			
			ImageJ ij = new ImageJ();
			ij.ui().showUI();
			
			ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
			//ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs32.tif");
			ij.ui().show(imp);
			
			
			Img<FloatType> img = ImageJFunctions.wrap(imp);
			Img<IntType> img2 = ij.op().convert().int32( img );
			
			CIP cip = new CIP();
			cip.setContext( ij.getContext() );
			cip.setEnvironment( ij.op() );
			//@SuppressWarnings("unchecked")
			RandomAccessibleInterval<?> sumImg = (RandomAccessibleInterval<?>)
						cip.add( img , img2  );
			
			String str = sumImg==null ? "null" : sumImg.toString();
			
			System.out.println("hello addImage:" + str );
			ij.ui().show(sumImg);
			
			System.out.println("done!");
		}
		
	
}
