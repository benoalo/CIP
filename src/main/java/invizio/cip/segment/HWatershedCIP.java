package invizio.cip.segment;



import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import invizio.imgalgo.label.HWatershed;
import invizio.imgalgo.util.RAI;
import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;


/**
 * 
 * @author Benoit Lombardot
 *
 */




@Plugin(type = CIP.WATERSHED.class, name=CIP.WATERSHED.NAME, headless = true)
public class HWatershedCIP  < T extends RealType<T> & NativeType< T > > extends AbstractOp implements CIP.WATERSHED 
{
	@Parameter (type = ItemIO.INPUT)
	private RandomAccessibleInterval<T> inputImage;
	
	@Parameter( label="Intensity threshold", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float threshold;
	
	@Parameter( label="Seed dynamics", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float hMin;
	
	@Parameter( label="Peak flooding (in %)", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float peakFlooding = 100f;
	
	@Parameter( label="Method", choices = {"binary","grey"} , persist=false, required=false ) // with persist and required set to false the parameter become optional
	private String method = "grey";
	
	@Parameter (type = ItemIO.OUTPUT)
	private	RandomAccessibleInterval<IntType> labelMap;
	
	@Parameter
	OpService op;
	
	
	//RandomAccessibleInterval<T> inputImage;
	
	
	@Override
	public void run() {
		
		/////////////////////////////////////////////////////////////////////
		// Check parameters   ///////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////

		if (inputImage == null){
			return;
		}
		//inputImage = (RandomAccessibleInterval<T>) inputImage0;
		
		if ( hMin == null ){
			// if hMin is not provided set it to 5% of the image range
			computeMinMax();
			hMin =  0.05f * ( max.getRealFloat() - min.getRealFloat() ) ;
		}
		
		if ( threshold == null ){
			// if thresh is not provided, set it to the minimum of the image
			computeMinMax();
			threshold =  min.getRealFloat();
		}
		
		
		/////////////////////////////////////////////////////////////////////
		// process Image  ///////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		if( method.toLowerCase().contains("gray") || method.toLowerCase().contains("grey") )
		{
			
			HWatershed<T> hWatershed = new HWatershed<T>( inputImage );
			hWatershed.sethMin((float)(double)hMin);
			hWatershed.setThreshold((float)((double)threshold));
			hWatershed.setPeakFlooding((float)(double)peakFlooding);
			
			labelMap = (Img<IntType>) hWatershed.getLabelMap();
			
		
		}
		else if( method.toLowerCase().contains("binary") )
		{
			// duplicate the original image
			RandomAccessibleInterval<T> inputCopy = RAI.duplicate( inputImage );
			
			// threshold the input image
			T T_threshold = inputCopy.randomAccess().get().createVariable();
			T_threshold.setReal( threshold );
			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<BitType> inputThresholded = (RandomAccessibleInterval<BitType>) op.threshold().apply(Views.iterable(inputCopy), T_threshold);
			
			// build a distance map
			RandomAccessibleInterval<FloatType> distanceMap = op.image().distancetransform( inputThresholded );
			
			//ImageJFunctions.show(inputThresholded);
			//ImageJFunctions.show(distanceMap);
			
			// calibration could be used as a second parameter
			
			// run HWatershed in place (no image duplication)
			HWatershed<FloatType> hWatershedBin = new HWatershed<FloatType>( distanceMap );
			hWatershedBin.sethMin((float)(double)hMin);
			hWatershedBin.setThreshold( 0.000001f );
			hWatershedBin.setPeakFlooding((float)(double)peakFlooding);
			
			labelMap = (Img<IntType>) hWatershedBin.getLabelMap();
			
			
		}
		else
		{
			labelMap = null;
			
			
			
		}
		
		
	}
	
	
	T min;
	T max;
	
	private void computeMinMax(){
		
		if( min==null ) {
			min = inputImage.randomAccess().get().createVariable();
			max = min.createVariable();
			ComputeMinMax.computeMinMax(inputImage, min, max);
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
		float threshold = 100;
		Float hMin = 50f;
		//Float peakFlooding = null;
		
		CIP cip = new CIP();
		cip.setContext( ij.getContext() );
		cip.setEnvironment( ij.op() );
		@SuppressWarnings("unchecked")
		RandomAccessibleInterval<IntType> labelMap = (RandomAccessibleInterval<IntType>) cip.watershed(img, 50, 50, 75 );
		
		ij.ui().show(labelMap);
		
		System.out.println("done!");
	}
	

	
	
}




