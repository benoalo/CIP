package invizio.cip.segment;

import java.util.Arrays;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.BooleanType;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;


/**
 * 
 * @author Benoit Lombardot
 *
 */


@Plugin(type = Op.class, name="manual threshold", headless = true)
public class ThresholdManualCIP < T extends RealType<T> & NativeType<T>> extends AbstractOp 
{
	
	
	@Parameter (type = ItemIO.INPUT)
	private RandomAccessibleInterval<T> inputImage;
	
	@Parameter(label="Intensity threshold", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float threshold;
		
	@Parameter (type = ItemIO.OUTPUT)
	private	RandomAccessibleInterval<BitType> mask;
	
	@Parameter OpService op;
	
		
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		
		/////////////////////////////////////////////////////////////////////
		// Check parameters   ///////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////

		if (inputImage == null || threshold==null){
			return;
		} // both parameter are required
		
		
		T T_threshold = inputImage.randomAccess().get().createVariable();
		T_threshold.setReal( threshold );
		mask = (RandomAccessibleInterval<BitType>) op.threshold().apply( Views.iterable(inputImage), T_threshold);
	}
	
	
	
	

	public static void main(final String... args)
	{
		
		ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		//ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
		ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs32.tif");
		Img<FloatType> img = ImageJFunctions.wrap(imp);
		float threshold = 150;
		
		CIP cip = new CIP();
		cip.setContext( ij.getContext() );
		cip.setEnvironment( ij.op() );
		@SuppressWarnings("unchecked")
		RandomAccessibleInterval<BitType> labelMap = (RandomAccessibleInterval<BitType>) cip.threshold( img , threshold );
		
		ij.ui().show(labelMap);
		
		System.out.println("done!");
	}
	
	
}



