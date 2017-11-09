package invizio.cip.segment;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;


import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.BooleanType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import invizio.imgalgo.label.RleCCL;
import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;


/**
 * 
 * @author Benoit Lombardot
 *
 */


@Plugin(type = CIP.WATERSHED.class, name="label", headless = true)
public class LabelCIP < T extends RealType<T> & NativeType<T> > extends AbstractOp 
{
	@Parameter (type = ItemIO.INPUT)
	private RandomAccessibleInterval<T> inputImage;
	
	@Parameter( label="Intensity threshold", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float threshold;
	
	@Parameter (type = ItemIO.OUTPUT)
	private	RandomAccessibleInterval<IntType> labelMap;
	
	
	
	
	@Override
	public void run() {
		
		/////////////////////////////////////////////////////////////////////
		// Check parameters   ///////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////

		if (inputImage == null){
			return;
		}
		//inputImage = (RandomAccessibleInterval<T>) inputImage0;
		
		if ( threshold == null ){
			T t = inputImage.randomAccess().get();
			if ( t instanceof BooleanType )
				threshold = 0.5f; // if the image is logic value will translate to 0, 1
			else
				return;
		}
		
		/////////////////////////////////////////////////////////////////////
		// process image  ///////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		
		RleCCL<T> labeler = new RleCCL<T>(	inputImage , threshold ); 
		
		labelMap =  labeler.getLabelMap();
		//ImageJFunctions.show(labelMap);
		
	}
	
	
	
	

	public static void main(final String... args)
	{
		
		ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
		Img<FloatType> img = ImageJFunctions.wrap(imp);
		float threshold = 100;
		
		CIP cip = new CIP();
		cip.setContext( ij.getContext() );
		cip.setEnvironment( ij.op() );
		@SuppressWarnings("unchecked")
		RandomAccessibleInterval<IntType> labelMap = (RandomAccessibleInterval<IntType>) cip.label( img , threshold );
		
		ij.ui().show(labelMap);
		
		System.out.println("done!");
	}
	
	
}



