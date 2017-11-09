package invizio.cip.segment;

import java.util.ArrayList;

import java.util.List;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ops.OpUtils;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;
import invizio.cip.CIPService;


/**
 * 
 * @author Benoit Lombardot
 *
 */


@Plugin(type = Op.class, name="automatic threshold", headless = true)
public class ThresholdAutoCIP < T extends RealType<T> & NativeType<T>> extends AbstractOp 
{
	
	List<String> methods2;
	/*String[] methods = {"huang",
						"ij1",
						"intermodes",
						"isoData",
						"li",
						"maxEntropy",
						"maxLikelihood",
						"mean",
						"minError",
						"minimum",
						"moments",
						"otsu",
						"percentile",
						"renyiEntropy",
						"rosin",
						"shanbhag",
						"triangle",
						"yen"};
	 */
	
	@Parameter (type = ItemIO.INPUT)
	private RandomAccessibleInterval<T> inputImage;
	
	// Could there be a callback or init setting the list of methods after the name of the threshold ops
	@Parameter( label="Method", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private String method;
	
	@Parameter( label="Output type", choices = {"Image","Value","Both"}, persist=false, required=false ) // with persist and required set to false the parameter become optional
	private String outputType;
	
	@Parameter (type = ItemIO.OUTPUT)
	private	RandomAccessibleInterval<BitType> mask;
	
	@Parameter (type = ItemIO.OUTPUT)
	private Double threshold;
	
	@Parameter OpService op;
	
	@Parameter CIPService cipService;
	

	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		
		/////////////////////////////////////////////////////////////////////
		// Check parameters   ///////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////

		if (inputImage == null){
			return;
		}
		//inputImage = (RandomAccessibleInterval<T>) inputImage0;
		
		// by default we apply the threshold or calculated threshold to the provided image
		if ( outputType == null)
			outputType="Image";
		
		
		methods2 = new ArrayList<String>();
		op.ops().forEach(
		opStr ->{
			String namespace = OpUtils.getNamespace(opStr);
			if ( namespace==null)
				return;
			if ( namespace.equals("threshold") ) {
				String opName = OpUtils.stripNamespace(opStr);
				if( !opName.contains("local") && !opName.contains("apply") ) {
					methods2.add(opName.toLowerCase());
				}
			}
		}
		);
		
		//System.out.println( methods2.toString() );
		
		T threshold_T=null;
		if ( methods2.contains(method.toLowerCase()) ) {
			// Calculate the threshold
			Histogram1d<T> histogram = op.image().histogram( Views.iterable(inputImage) );
			
			threshold_T = (T) op.run("threshold."+method.toLowerCase(), histogram );
			threshold = threshold_T.getRealDouble();
		}
		else {
			System.err.println("\nMethod " + method.toLowerCase() +" is not a valid thresholding method name.");
			System.err.println("Possible methods are:");
			for( String str : methods2)
				System.err.println(" * " + str.toLowerCase() );
			return;
		}
		
		
		switch( outputType.toLowerCase() ) {
			case "image" :
				mask = (RandomAccessibleInterval<BitType>) cipService.cip().threshold( inputImage , threshold);
				//mask = (RandomAccessibleInterval<BitType>) op.threshold().apply( Views.iterable(inputImage), threshold_T);
				threshold = null;
				break;
				
			case "value" :
				// nothing to do, set Mask to null
				break;
			
			default : // "both"
				//mask = (RandomAccessibleInterval<BitType>) op.threshold().apply( Views.iterable(inputImage), threshold_T);
				mask = (RandomAccessibleInterval<BitType>) cipService.cip().threshold( inputImage , threshold);
				break;
				
		}
		
	}
	
	
	
	

	@SuppressWarnings("unchecked")
	public static void main(final String... args)
	{
		
		ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
		Img<FloatType> img = ImageJFunctions.wrap(imp);
		
		CIP cip = new CIP();
		cip.setContext( ij.getContext() );
		cip.setEnvironment( ij.op() );
		
		
		RandomAccessibleInterval<BoolType> labelMap = (RandomAccessibleInterval<BoolType>) cip.threshold( img , "Otsu" );
		System.out.println( "mask : " + labelMap );

//		RandomAccessibleInterval<BoolType> labelMap2 = (RandomAccessibleInterval<BoolType>) cip.threshold( img , "huang", "image" );
//		System.out.println( "mask2 : " + labelMap2 );
//
//		Double threshold = (Double) cip.threshold( img , "huang" , "value" );
//		System.out.println( "threshold : " + threshold );
//
//		ArrayList<Object> results  = (ArrayList<Object>) cip.threshold( img , "huang" , "both" );
//		System.out.println( "both : " + results );
		
		ij.ui().show(labelMap);
		
		System.out.println("done!");
		
	}
	
	
}



