package invizio.cip.segment;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import invizio.imgalgo.RealPoint_A;
import invizio.imgalgo.label.AreaMaxima;
import invizio.imgalgo.label.DefaultLabelAlgorithm;
import invizio.imgalgo.label.HMaxima;
import invizio.imgalgo.label.Maxima;
import invizio.imgalgo.label.MultiScaleMaxima;
import invizio.imgalgo.label.WindowMaxima;

import ij.IJ;
import ij.ImagePlus;

import invizio.cip.CIP;

import net.imagej.ImageJ;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.OpService;
import net.imagej.ops.Op;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
//import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import ij.gui.Overlay;
import ij.gui.OvalRoi;



/**
 * 
 * @author Benoit Lombardot
 *
 */



// idea: why not returning regions nDim sphere rather than measures ? 
// That would facilitate visualisation and parameter could still be estimated with cip.measure 


@Plugin(type = Op.class, name="MaximaCIP", headless = true)
public class MaximaCIP  < T extends RealType<T> & NativeType< T > > extends AbstractOp
{

	
	@Parameter (type = ItemIO.INPUT)
	private RandomAccessibleInterval<T> inputImage;
	
	@Parameter( label="Intensity threshold", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float threshold = null;
	
	@Parameter( label="minimum peak Height", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float hMin = null;
	
	@Parameter( label="minimum peak Area", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float aMin = null;
	
	@Parameter( label="minimum peak Distance", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float dMin = null;
	
	@Parameter( label="minimum peak Scale", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float sMin = null;
	
	@Parameter( label="maximum peak Scale", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float sMax = null;
	
	@Parameter( label="Method", choices = {"classic","height","area","distance","multiScale"} , persist=false, required=false ) // with persist and required set to false the parameter become optional
	private String method = null;
	
	// can be useful both to specify window size and minScale maxScale
	@Parameter( label="Pixel size", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float[] pixelSize = null;
	
	@Parameter( label="Output", choices= {"image","measure","both"}, persist=false, required=false ) // with persist and required set to false the parameter become optional
	private String output = null;
	
	@Parameter( label="n scale per octave", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Integer nScalePerOctave = 3;
	
	@Parameter( label="anisotropy", persist=false, required=false ) // with persist and required set to false the parameter become optional
	private Float anisotropy = 10f;
	
	
	
	@Parameter (type = ItemIO.OUTPUT)
	private	RandomAccessibleInterval<IntType> labelMap;

	@Parameter (type = ItemIO.OUTPUT)
	private	Map<String,List<Object>> measures;

	@Parameter
	private OpService op;
	
	
	@Override
	public void run() {
		
		//////////////////////////////////////
		// check inputs
		//////////////////////////////////////
		
		// Image
		if (inputImage == null){
			return;
		}
		
		if( output == null )
		{
			output = "image";
		}
		
		
		// threshold
		if ( threshold == null ){
			// if thresh is not provided, set it to the minimum of the image
			computeMinMax();
			threshold =  min.getRealFloat();
		}
		
		// method
		if( method == null) {
			
			method = "classic";
			if (hMin != null ) {
				if( hMin>0 ) {
					method = "height";
				}
				// else method = "classic";
			}
			else if (aMin != null ) {
				method = "area";
			}
			else if (dMin != null) {
				method = "distance";
			}
			
			if (sMin!= null || sMax!= null) {
				method = "multiscale";
			}
		}
		
		
		// pixelSize
		int nDim = inputImage.numDimensions();
		if( pixelSize == null ) {
			pixelSize = new Float[nDim];
			for(int d=0 ; d<nDim ; d++) {
				pixelSize[d] = 1f;
			}
		}
		else if( pixelSize.length == 1 )
		{
			Float pixelSize0 = pixelSize[0];
			pixelSize = new Float[nDim];
			for(int d=0 ; d<nDim ; d++) {
				pixelSize[d] = pixelSize0;
			}
		}
		else if( pixelSize.length < nDim )
		{
			//TODO: Error, the pixelSize is not consistent with the image dimension ( pixelSize.length vs. nDim )
		}
		else if( pixelSize.length > nDim )
		{
			Float[] pixelSize0 = pixelSize;
			pixelSize = new Float[nDim];
			for(int d=0 ; d<nDim ; d++) {
				pixelSize[d] = pixelSize0[d];
			}
			
			//TODO: Warning! to many elements in pixelSize, only the nDim first will be used 
		}
		
		
		
		////////////////////////////////////////////////////////////////
		// process the maxima according to the parameters
		////////////////////////////////////////////////////////////////
		
		DefaultLabelAlgorithm<T> labeler = null;
		MultiScaleMaxima<FloatType> msLabeler=null;
		switch( method.toLowerCase() ) {
		
		case "height":
			// hMin
			if ( hMin==null ){
				// if hMin is not provided set it to 5% of the image range
				computeMinMax();
				hMin =  0.05f * ( max.getRealFloat() - min.getRealFloat() ) ;
			}
			// setup Hmaxima labeler
			labeler = new HMaxima<T>( inputImage, threshold, hMin );
			break;

		case "area":
			// aMin
			if ( aMin==null ){
				aMin = (float) Math.pow(3, inputImage.numDimensions() ) + 1 ;
			}
			// express aMin in pixel, if pixelSize was provided it is assumed to be express in the same unit as the pixelSize
			for(float size : pixelSize )
				aMin /= size;
			
			// setup AreaMaxima labeler
			labeler = new AreaMaxima<T>( inputImage, threshold, aMin );
			
			break;

		case "distance":
			// window radius 
			if ( dMin==null ){
				dMin =  1f ;
			}
			
			//express span in pixel if pixelSize was provided, dMin is assumed to be expressed in the same unit as the pixelSize
			int[] span = new int[nDim]; 
			for( int d=0 ; d<nDim ; d++ )
				span[d] = (int) ( dMin/pixelSize[d] ) ;
			
			// setup WindowMaxima labeler
			labeler = new WindowMaxima<T>( inputImage, threshold, span , WindowMaxima.ExtremaType.MAXIMA );
			break;

		case "multiscale":
			
			Img<FloatType> inputBlur = op.convert().float32( Views.iterable(inputImage) );
			inputBlur = (Img<FloatType>) op.filter().gauss(inputBlur, 1.6);
			//Img<T> inputBlur = (Img<T>) op.filter().gauss(inputImage, 1.0);

			// hMin
			if ( hMin==null ){
				// if hMin is not provided set it to 5% of the image range
				computeMinMax();
				hMin =  0.05f * ( max.getRealFloat() - min.getRealFloat() ) ;
			}
			
			double[] pSize_d = new double[pixelSize.length];
			for(int d=0; d<pixelSize.length; d++)
				pSize_d[d] = (double)(float)pixelSize[d]; 
			
			//int nScalePerOctave = 3;
			msLabeler = new MultiScaleMaxima<FloatType>( inputBlur, pSize_d, 1, 2, nScalePerOctave );

			// minScale
			if ( sMin==null ){
				sMin =  (float) msLabeler.getMinPhysicalScale();
			}
			
			if ( sMax==null ){
				sMax =  (float) msLabeler.getMaxPhysicalScale();
			}
			
			msLabeler.setThreshold(threshold);
			msLabeler.sethMin(hMin);
			msLabeler.setMaxAnisotropy(anisotropy); 
			 
			msLabeler.setMinScale(Math.max( sMin, msLabeler.getMinPhysicalScale() )  );
			msLabeler.setMaxScale(Math.min( sMax, msLabeler.getMaxPhysicalScale() )  );
						
			break;
		
		default : // "classic"
			
			// setup Maxima labeler
			labeler = new Maxima<T>( inputImage, threshold );
			break;
			
		}
		
		
		
		
		
		if( method.toLowerCase().equals("multiscale") )
		{
			String[] dimName = new String[] {"X","Y","Z"}; 
			
			if( output.equals("both") || output.equals("measure") )
			{
				//MultiScaleMaxima<FloatType> msLabeler = (MultiScaleMaxima<FloatType>) labeler;
				List<RealPoint_A> extremas = msLabeler.getRealExtrema();
				int nExtremas = extremas.size();
				measures = new LinkedHashMap<String,List<Object>>();
				for( int d=0; d<nDim; d++)
					measures.put(dimName[d], new ArrayList<Object>() );
				measures.put("scale", new ArrayList<Object>() );
				measures.put("intensity", new ArrayList<Object>() );
				measures.put("intensity_dog", new ArrayList<Object>() );
				measures.put("anisotropy", new ArrayList<Object>() );
				
				for(int i=0; i<nExtremas; i++)
				{
					RealPoint_A pt = extremas.get(i);
					double scale = pt.getAttribute("scale");
					double intensity = pt.getAttribute("intensity");
					double intensity_dog = pt.getAttribute("value");
					double anisotropyCriteria = pt.getAttribute("anisoCrit");
					measures.get("scale").add(scale);
					measures.get("intensity").add(intensity);
					measures.get("intensity_dog").add(intensity_dog);
					measures.get("anisotropy").add(anisotropyCriteria);
					for( int d=0;d<nDim; d++)
						measures.get(dimName[d]).add( pt.getDoublePosition(d) );
				}
				if( output.equals("both") )
				{
					labelMap = msLabeler.getLabelMap();
				}
			}
			else if( output.equals("image")  )
			{
				labelMap = msLabeler.getLabelMap();
			}
		}
		else
		{
			
			labelMap = labeler.getLabelMap();
			
		}
		//
		
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
		
		ImagePlus imp = IJ.openImage("F:\\projects\\blobs.tif");
		//ImagePlus imp = IJ.openImage(	CIP.class.getResource( "/blobs32.tif" ).getFile()	);
		ij.ui().show(imp);
		
		
		//Img<FloatType> img = ImageJFunctions.wrap(imp);
		float threshold = 100;
		//Float[] pixelSize = new Float[] { 1f , 0.1f};
		//Float pixelSize = 0.5f;
		//List<Double> pixelSize = CIP.asList( 1, 1 );
		
		CIP cip = new CIP();
		cip.setContext( ij.getContext() );
		cip.setEnvironment( ij.op() );
		
		//@SuppressWarnings("unchecked")
		//RandomAccessibleInterval<IntType> labelMap = (RandomAccessibleInterval<IntType>)
		//			cip.maxima(imp, threshold, "sMin", 4,"sMax",16, "hMin", 50 );
		
		
		@SuppressWarnings("unchecked")
		Map<String,List<Object>> measures = (Map<String,List<Object>>) cip.maxima(imp, threshold, "sMin", 3,"sMax",16, "hMin", 0, "output", "measure");
		
		
		

		Overlay ov = new Overlay();
		
		int nObj = measures.get("X").size();
		for(int i=0; i<nObj; i++)
		{
			double x = (Double) measures.get("X").get(i);
			double y = (Double) measures.get("Y").get(i);
			double r = (Double) measures.get("scale").get(i);
			ov.add( new OvalRoi(x-r, y-r, 2*r, 2*r));
		}

		imp.setOverlay(ov);
		
		cip.show(measures);
		
		System.out.println("done!");
	}
	
	
	
	
	
}
