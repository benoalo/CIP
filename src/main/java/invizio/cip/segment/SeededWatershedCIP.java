package invizio.cip.segment;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;


import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import invizio.imgalgo.label.HMaxima;
import invizio.imgalgo.label.SeededWatershed;
import ij.IJ;
import ij.ImagePlus;
import invizio.cip.CIP;



/**
 * 
 * @author Benoit Lombardot
 *
 */


@Plugin(type = CIP.WATERSHED.class, name=CIP.WATERSHED.NAME, headless = true)
public class SeededWatershedCIP < T extends RealType<T> & NativeType<T>, U extends IntegerType<U>> extends AbstractOp implements CIP.WATERSHED 
{
	@Parameter (type = ItemIO.INPUT)
	private RandomAccessibleInterval<T> inputImage;
	
	@Parameter (type = ItemIO.INPUT)
	private RandomAccessibleInterval<U> inputSeed;
	
	@Parameter( label="Intensity threshold", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float threshold;
	
	@Parameter (type = ItemIO.OUTPUT)
	private	RandomAccessibleInterval<IntType> labelMap;
	
	
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
		
		if ( threshold == null ){
			// if thresh is not provided, set it to the minimum of the image
			computeMinMax();
			threshold = min.getRealFloat();
		}
		
		/////////////////////////////////////////////////////////////////////
		// process image  ///////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		
		SeededWatershed<T,U> seededWatershed = new SeededWatershed<T,U>(
													inputImage,
													inputSeed,
													threshold, 
													SeededWatershed.WatershedConnectivity.FACE
													); 
		
		labelMap =  seededWatershed.getLabelMap();
		//ImageJFunctions.show(labelMap);
		
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
		
		ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
		ij.ui().show(imp);
		
		Img<FloatType> img = ImageJFunctions.wrap(imp);
		HMaxima<FloatType> maxima = new HMaxima<FloatType>( img, 100, 10 );
		RandomAccessibleInterval<IntType> seeds = maxima.getLabelMap();
		ij.ui().show(seeds);
		
		float threshold = 50;
		
		
		CIP cip = new CIP();
		cip.setContext( ij.getContext() );
		cip.setEnvironment( ij.op() );
		@SuppressWarnings("unchecked")
		RandomAccessibleInterval<IntType> labelMap = (RandomAccessibleInterval<IntType>) cip.watershed( img , seeds, 100 );
		
		ij.ui().show(labelMap);
		
		System.out.println("done!");
	}
	
	
}



