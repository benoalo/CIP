package invizio.cip;


import java.util.ArrayList;
import java.util.List;

import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.misc.ShowCIPService;
import invizio.cip.parameters.DefaultParameter2;
import invizio.cip.parameters.FunctionParameters2;
import invizio.cip.region.Regions;
import invizio.cip.parameters.DefaultParameter2.Type;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractNamespace;
import net.imagej.ops.Namespace;
import net.imagej.ops.Op;
import net.imagej.ops.OpMethod;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;



/*
 * The goal of that class is to provide access to classic image processing algorithms
 * with minimal hassle (no import, no conversion, 2D/3D) and maximum ease of use in scripts
 * 
 * Remarks: function should never modify the input !
 *   
 *  
 *  TODO:
 *  	
 *  	[-] check what is happening if a dataset or an ImagePlus are passed to the cip functions
 *  		do the necessary conversion if it does not work from scratch
 *  
 *		[x] HWatershed
 *  	[x] SeededWatershed
 *  	[x] Binary watershed (with binary input)
 *  	[x] implement maxima
 *  	[x] implement label
 *  	[x] implement threshold
 *  	[-] implement skeleton
 *  	[-] implement edge detector
 *  	[-] implement filters (adding a 'valid' option for output type could be nice )
 *  		[x] implement distance
 *  		[x] implement gauss
 *  		[x] implement dilation
 *  		[x] implement erosion
 *  		[x] implement opening
 *  		[x] implement closing
 *  		[x] implement tophat
 *  		[x] implement invert
 *  		[x] implement median
 *  		[-] implement gradient
 *  		[-] implement laplacian
 *  		[-] implement hessian
 *  	[-] implement math operations
 *  		[x] binary operation (add, mul, div, sub, min, max)
 *  		[x] unary operations (trigo, log, exp, pow, sqrt, abs, round, floor, ceil  )
 *  		[-] and, or, not, logic sub, >, >=, ==, <, <=, !=
 *  	[-] implement miscellaneous
 *  		[x] create 
 *  		[x] duplicate/slice
 *			[-] projection ( min, max, sum, median, stdev )
 *  		[-] concat (repeat the same image along a dim, or concat image along a dim)
 *  		[-] resample
 *  
 *  	[-] implement toPoints
 *  	[-] implement toRegions
 *  	[-] study measures
 */





/**
 * 
 * ImageJ Op namespace for a collection of classic image processing function: segment, filter, transform
 * 
 * @author Benoit Lombardot
 * 
 */
@Plugin(type = Namespace.class)
public class CIP extends AbstractNamespace{

	int nThread; // if the function called can be multithreaded, this is the number of thread that will be used
	
	@Parameter
	private CIPService cipService;
	
	@Parameter
	private ShowCIPService showCipService;
	
	@Parameter
	private UIService uiService;
	
	//@Parameter
	//private LUTService lutService;
	
	@Parameter
	private DisplayService displayService;
	
	
	public CIP() {
		super();
		nThread = Runtime.getRuntime().availableProcessors();
	}
	
	
	@Override
	public String getName() {
		return "CIP";
	}
	
	public interface WATERSHED extends Op {
		// Note that the name and aliases are prepended with Namespace.getName
		String NAME = "watershed";
		String ALIASES = "ws";
	}
	
	
	
	/********************************************************************************
	 * 	Watershed interface															*
	 ********************************************************************************/
	
	// General Watershed Op methods, should have lowest priority
	// should handle all HWatershed and SeededWatershed without conflict
	// Binary Watershed is handled earlier with specific signatures
	
	@OpMethod(op = invizio.cip.CIP.WATERSHED.class )
	public Object watershed(final Object... args) {
		
		Object results = null;
		
		// depending on the input we'll orient toward:
		//	* HWatershedOp
		//	* SeededWatershedOp
		// rk the parameter should be added in the same order they are declared in the op
		
		
		FunctionParameters2 paramsHWS = new FunctionParameters2("HWatershed");
		paramsHWS.addRequired("inputImage", 	Type.image 	);
		paramsHWS.addOptional("threshold", 		Type.scalar , 	null	);
		paramsHWS.addOptional("hMin", 			Type.scalar , 	null	);
		paramsHWS.addOptional("PeakFlooding", 	Type.scalar , 	100f 	);
		paramsHWS.addOptional("Method", 		Type.string , 	"gray"	);
		
		
		FunctionParameters2 paramsSeededWS = new FunctionParameters2("Seeded Watershed");
		paramsSeededWS.addRequired("inputImage",Type.image 	);
		paramsSeededWS.addRequired("Seed", 		Type.image 	);
		paramsSeededWS.addOptional("threshold", Type.scalar , 	null	);
		
		if ( paramsHWS.parseInput( args ) )
		{
			cipService.toRaiCIP( paramsHWS.get("inputImage") );
			results = ops().run(invizio.cip.segment.HWatershedCIP.class, paramsHWS.getParsedInput() );
			results = cipService.setMetadata( results, paramsHWS.get("inputImage"), "ws_" );
			
		}
		else if ( paramsSeededWS.parseInput( args ) )
		{
			cipService.toRaiCIP( paramsSeededWS.get("inputImage") );
			results = ops().run(invizio.cip.segment.SeededWatershedCIP.class, paramsSeededWS.getParsedInput() );
			results = cipService.setMetadata( results, paramsSeededWS.get("inputImage"), "ws_" );

		}
		else
		{	
			// print reason why the input parsing failed
			paramsHWS.printFeedback();
			paramsSeededWS.printFeedback();
		}
		
		return results;
	}
	

	
	
	/********************************************************************************
	 * 	Distance map construction interface											*
	 ********************************************************************************/
	
	
	// distance method
	@OpMethod(op = invizio.cip.filter.DistanceCIP.class)
	public Object distance(final Object... args) {
		
		
		Object results = null;
		
		FunctionParameters2 params = new FunctionParameters2("Distance");
		params.addRequired("inputImage", 	Type.image 	);
		params.addOptional("threshold", 	Type.scalar , 		null	);
		params.addOptional("pixelSize", 	Type.scalars , 		1f		);
		
		if ( params.parseInput( args ) )
		{
			// convert image to RaiCIP 
			cipService.toRaiCIP( params.get("inputImage") ); // similar as toImglib2Image but also collect spacing, axes name
			results = ops().run(invizio.cip.filter.DistanceCIP.class, params.getParsedInput() );
			results = cipService.setMetadata( results, params.get("inputImage"), "dist_");

		}
		return results; 
	}

	
	
	
	
	
	
	
	
	
	
	
	/********************************************************************************
	 * 	Maxima detection interface													*
	 ********************************************************************************/
	
	
	// distance method
	@OpMethod(op = invizio.cip.segment.MaximaCIP.class)
	public Object maxima( final Object... args ) {
		
		Object results = null;
		
		FunctionParameters2 params = new FunctionParameters2("Maxima");
		params.addRequired("inputImage", 	Type.image 	);
		params.addOptional("threshold", 	Type.scalar , 	null	);
		params.addOptional("HeightMin", 	Type.scalar , 	null	);
		params.get( "HeightMin" ).aliases.add( "hmin" );
		params.addOptional("AreaMin",		Type.scalar , 	null 	);
		params.get( "AreaMin" ).aliases.add( "amin" );
		params.addOptional("DistanceMin",	Type.scalar , 	null 	);
		params.get( "DistanceMin" ).aliases.add( "dmin" );
		params.addOptional("ScaleMin",		Type.scalar , 	null 	);
		params.get( "ScaleMin" ).aliases.add( "smin" );
		params.addOptional("ScaleMax",		Type.scalar , 	null 	);
		params.get( "ScaleMax" ).aliases.add( "smax" );
		params.addOptional("Method", 		Type.string , 	null	);
		params.addOptional("PixelSize", 	Type.scalars, 	null	);
		
		if ( params.parseInput( args ) )
		{
			cipService.toRaiCIP( params.get("inputImage") );
			results = ops().run( invizio.cip.segment.MaximaCIP.class, params.getParsedInput() );
			results = cipService.setMetadata( results, params.get("inputImage"), "max_" );
		}
		return results; 
	}
	
		

    
    
    
    
    
    
    
    
    
    /********************************************************************************
	 * 	image labeling 													*
	 ********************************************************************************/
 	
    @OpMethod(op = invizio.cip.segment.LabelCIP.class)
 	public Object label( final Object... args ) {
 		
 		Object results = null;
 	
 		FunctionParameters2 params = new FunctionParameters2("Label");
		params.addRequired("inputImage", 	Type.image 	);
		params.addOptional("threshold", 	Type.scalar , 	null	); // not needed if the image is of boolean type
		
		if ( params.parseInput( args ) )
		{
			cipService.toRaiCIP( params.get("inputImage") );
			results = ops().run( invizio.cip.segment.LabelCIP.class, params.getParsedInput() );
			results = cipService.setMetadata( results, params.get("inputImage") , "label_");
		}
		return results; 
	}

    
    
    
    
    
    
    
    /********************************************************************************
	 * 	image thresholding 													*
	 ********************************************************************************/
    
    
    // TODO: it would be nice to make the output parsing generic and put it in an independent class 
    
    
    @OpMethod(ops = { 	invizio.cip.segment.ThresholdManualCIP.class 	,
    					invizio.cip.segment.ThresholdAutoCIP.class 	})
 	public Object threshold( final Object... args ) {
 		
 		Object results = null;
 	
 		FunctionParameters2 params1 = new FunctionParameters2("Manual threshold");
		params1.addRequired("inputImage", 	Type.image 	);
		params1.addRequired("threshold", 	Type.scalar ); 
		

 		FunctionParameters2 params2 = new FunctionParameters2("Auto threshold");
		params2.addRequired("inputImage", 	Type.image 	);
		params2.addRequired("method",	 	Type.string ); 
		params2.addOptional("output",	 	Type.string , 	null	); // not needed if the image is of boolean type
		
		
		if ( params1.parseInput( args ) )
		{
			cipService.toRaiCIP( params1.get("inputImage") );
			results = ops().run( invizio.cip.segment.ThresholdManualCIP.class, params1.getParsedInput() );
			results = cipService.setMetadata( results, params1.get("inputImage"), "thresh_");
		}
		else if ( params2.parseInput( args ) )
		{
			cipService.toRaiCIP( params2.get("inputImage") );
			List<Object> resultsTemp = (List<Object>) ops().run( invizio.cip.segment.ThresholdAutoCIP.class, params2.getParsedInput() );
			Object image = cipService.setMetadata( resultsTemp.get(0), params2.get("inputImage"), "thresh_"); // assumes 0 is the output image
			resultsTemp.set( 0, image );
			results = cipService.discardNullValue( resultsTemp );
		}
		
		return results; 
	}
    

    
    
    
    
    
    
    
    
    
    /********************************************************************************
   	 * 	gauss filtering																*
   	 ********************************************************************************/
    	
       @OpMethod(op = invizio.cip.filter.GaussCIP.class)
    	public Object gauss( final Object... args ) {
    		
    		Object results = null;
    	
    	FunctionParameters2 params = new FunctionParameters2("gaussian convolution");
   		params.addRequired("inputImage", 	Type.image 	);
   		params.addRequired("radius", 		Type.scalars ); 
   		params.addOptional("boundary", 		Type.string  , 	null	); 
   		params.addOptional("pixelSize", 	Type.scalars , 	null	); 
   		
   		if ( params.parseInput( args ) )
   		{
   			cipService.toRaiCIP( params.get("inputImage") );
   			results = ops().run( invizio.cip.filter.GaussCIP.class, params.getParsedInput() );
   			results = cipService.setMetadata( results, params.get("inputImage"), "gauss_" );
   		}
   		return results; 
   	}

    
    
   @OpMethod(op = invizio.cip.filter.MedianCIP.class)
   public Object median( final Object... args ) {
  		
  		Object results = null;
  	
  		FunctionParameters2 params = new FunctionParameters2("median");
 		params.addRequired("inputImage", 	Type.image 	);
 		params.addRequired("radius", 		Type.scalars );
 		params.addOptional("shape", 		Type.string  , 	null	); 
 		params.addOptional("boundary", 		Type.string  , 	null	); 
 		params.addOptional("pixelSize", 	Type.scalars , 	null	); 
 		
 		
 		if ( params.parseInput( args ) )
 		{
 			cipService.toRaiCIP( params.get("inputImage") );
 			results = ops().run( invizio.cip.filter.MedianCIP.class, params.getParsedInput() );
 			results = cipService.setMetadata( results, params.get("inputImage"), "median_" );
 		}
 		return results; 
 	}
    
       
       
   @OpMethod(op = invizio.cip.filter.InvertCIP.class)
   public Object invert( final Object... args ) {
  		
  		Object results = null;
  	
  		FunctionParameters2 params = new FunctionParameters2("invert");
 		params.addRequired("inputImage", 	Type.image 	);
 		
 		
 		if ( params.parseInput( args ) )
 		{
 			cipService.toRaiCIP( params.get("inputImage") );
 			results = ops().run( invizio.cip.filter.InvertCIP.class, params.getParsedInput() );
 			results = cipService.setMetadata( results, params.get("inputImage"), "invert_" );
 		}
 		return results; 
 	}
       
       
       
       
       
       
       
   /*********************************************************************************
  	* basic  mathematical morphology : erode, dilate, open, close, tophat			*
  	*********************************************************************************/
   	
    @OpMethod(op = invizio.cip.filter.DilationCIP.class)
   	public Object dilate( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("dilation");
  		params.addRequired("inputImage", 	Type.image 	);
  		params.addRequired("radius", 		Type.scalars );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.scalars , 	null	); 
  		params.addOptional("nthread", 		Type.scalars ,	null	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			cipService.toRaiCIP( params.get("inputImage") );
  			results = ops().run( invizio.cip.filter.DilationCIP.class, params.getParsedInput() );
  			results = cipService.setMetadata( results, params.get("inputImage"), "dilate_" );
  		}
  		return results; 
  	}
     
 
    
    @OpMethod(op = invizio.cip.filter.ErosionCIP.class)
   	public Object erode( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("erosion");
  		params.addRequired("inputImage", 	Type.image 	);
  		params.addRequired("radius", 		Type.scalars );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.scalars , 	null	); 
  		params.addOptional("nthread", 		Type.scalars ,	nThread	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			cipService.toRaiCIP( params.get("inputImage") );
  			results = ops().run( invizio.cip.filter.ErosionCIP.class, params.getParsedInput() );
  			results = cipService.setMetadata( results, params.get("inputImage"), "erode_" );
  		}
  		return results; 
  	}
 
       
    @OpMethod(op = invizio.cip.filter.OpeningCIP.class)
   	public Object opening( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("opening");
  		params.addRequired("inputImage", 	Type.image 	);
  		params.addRequired("radius", 		Type.scalars );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.scalars , 	null	); 
  		params.addOptional("nthread", 		Type.scalars ,	nThread	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			cipService.toRaiCIP( params.get("inputImage") );
  			results = ops().run( invizio.cip.filter.OpeningCIP.class, params.getParsedInput() );
  			results = cipService.setMetadata( results, params.get("inputImage"), "open_" );
  		}
  		return results; 
  	}
       
       
      
       
    @OpMethod(op = invizio.cip.filter.OpeningCIP.class)
   	public Object closing( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("closing");
  		params.addRequired("inputImage", 	Type.image 	);
  		params.addRequired("radius", 		Type.scalars );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.scalars , 	null	); 
  		params.addOptional("nthread", 		Type.scalars ,	nThread	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			cipService.toRaiCIP( params.get("inputImage") );
  			results = ops().run( invizio.cip.filter.ClosingCIP.class, params.getParsedInput() );
  			results = cipService.setMetadata( results, params.get("inputImage"), "close_" );
  		}
  		return results; 
  	}
       
       
       
    @OpMethod(op = invizio.cip.filter.TophatCIP.class)
   	public Object tophat( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("tophat");
  		params.addRequired("inputImage", 	Type.image 	);
  		params.addRequired("radius", 		Type.scalars );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.scalars , 	null	); 
  		params.addOptional("nthread", 		Type.scalars ,	nThread	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			cipService.toRaiCIP( params.get("inputImage") );
  			results = ops().run( invizio.cip.filter.TophatCIP.class, params.getParsedInput() );
  			results = cipService.setMetadata( results, params.get("inputImage"), "tophat_" );
  		}
  		return results; 
  	}
       
       
       
    /********************************************************************************
  	* math : add, mul, sub, div, min, max 											*
  	*********************************************************************************/
   	
    public interface MathBinary extends Op {
		// Note that the name and aliases are prepended with Namespace.getName
		String NAME = "math binary";
		
	}
    
    public Object add( final Object... args ) {
    	
    	return math2Operation("add", args );
    }
    
    public Object sub( final Object... args ) {
    	
    	return math2Operation("subtract", args );
    }

    public Object mul( final Object... args ) {
    	
    	return math2Operation("multiply", args );
    }

    public Object div( final Object... args ) {
    	
    	return math2Operation("divide", args );
    }

    
    
    @OpMethod(op = invizio.cip.CIP.MathBinary.class )
   	private Object math2Operation( String operationType , final Object[] args ) {
   		
   		FunctionParameters2 paramsImage = new FunctionParameters2("addImage");
  		paramsImage.addRequired("inputImage1", 	Type.image 	);
  		paramsImage.addRequired("inputImage2", 	Type.image 	);

   		FunctionParameters2 paramsNumber = new FunctionParameters2("addNumber");
  		paramsNumber.addRequired("inputImage", 	Type.image 	);
  		paramsNumber.addRequired("value", 		Type.scalar	);
  		
  		FunctionParameters2 paramsNumber2 = new FunctionParameters2("addNumber2");
  		paramsNumber2.addRequired("value", 		Type.scalar	);
  		paramsNumber2.addRequired("inputImage", Type.image 	);
  		
  		Object[] parametersFinal = new Object[3];
		parametersFinal[0] = operationType;
		String opName = null;
		
		DefaultParameter2 inputImage = null;
  		if ( paramsImage.parseInput( args ) )
  		{
  			cipService.convertToMajorType(paramsImage.get("inputImage1") , paramsImage.get("inputImage2"), operationType );
  			parametersFinal[1] = paramsImage.get("inputImage1").value;
  			parametersFinal[2] = paramsImage.get("inputImage2").value;
  			opName = "Image_Image_MathOperationCIP";
  			inputImage = paramsImage.get("inputImage1");  			
  		}
  		else if (  paramsNumber.parseInput( args )   )
  		{
  			// adapt input data structure (Img for image, net.Imglib2.Type for scalar) type and return type string
  			cipService.convertToMajorType(paramsNumber.get("inputImage") , paramsNumber.get("value"), operationType );
  			parametersFinal[1] = paramsNumber.get("inputImage").value;
  			parametersFinal[2] = paramsNumber.get("value").value;
  			opName = "Image_Number_MathOperationCIP";	
  			inputImage = paramsNumber.get("inputImage");
  		}
  		else if ( paramsNumber2.parseInput( args )  )
  		{
  			// adapt input data structure (Img for image, net.Imglib2.Type for scalar) type and return type string
  			cipService.convertToMajorType(paramsNumber2.get("inputImage") , paramsNumber2.get("value"), operationType );
  			parametersFinal[1] = paramsNumber2.get("value").value;
  			parametersFinal[2] = paramsNumber2.get("inputImage").value;
  			opName = "Number_Image_MathOperationCIP";
  			inputImage = paramsNumber2.get("inputImage");
  		}
  		else
  		{
  			return null;
  		}
  		
  		Object results = ops().run( opName , parametersFinal ); 
  		results = cipService.setMetadata( results, inputImage, operationType+"_" );
  		
  		return results; 
  	}   
       
    
    
    
    
    //min and max op do not exist for images, so they are added in the math package here
    
    public Object min( final Object... args ) {
    	
    	return moreMath2Operation("min", args );
    }

    public Object max( final Object... args ) {
    	
    	return moreMath2Operation("max", args );
    }
    
    public Object pow( final Object... args ) {
    	
    	return moreMath2Operation("pow", args );
    }

    
    @OpMethod(op = invizio.cip.CIP.MathBinary.class )
   	private Object moreMath2Operation( String operationType , final Object[] args ) {
   		
   		FunctionParameters2 paramsImage = new FunctionParameters2("MinImageImage");
  		paramsImage.addRequired("inputImage1", 	Type.image 	);
  		paramsImage.addRequired("inputImage2", 	Type.image 	);

   		FunctionParameters2 paramsNumber = new FunctionParameters2("MinImageNumber");
  		paramsNumber.addRequired("inputImage", 	Type.image 	);
  		paramsNumber.addRequired("value", 		Type.scalar	);
  		
  		FunctionParameters2 paramsNumber2 = new FunctionParameters2("MinNumberImage");
  		paramsNumber2.addRequired("value", 		Type.scalar	);
  		paramsNumber2.addRequired("inputImage", Type.image 	);
  		
  		Object[] parametersFinal = new Object[3];
		parametersFinal[0] = operationType;
		String opName = null;
		String opBaseName = "MoreMathOperationCIP";
		DefaultParameter2 inputImage = null;
  		if ( paramsImage.parseInput( args ) )
  		{
  			cipService.convertToMajorType(paramsImage.get("inputImage1") , paramsImage.get("inputImage2"), operationType );
  			parametersFinal[1] = paramsImage.get("inputImage1").value;
  			parametersFinal[2] = paramsImage.get("inputImage2").value;
  			opName = "Image_Image_"+opBaseName;
  			inputImage = paramsImage.get("inputImage1");
  	  	}
  		else if (  paramsNumber.parseInput( args )   )
  		{
  			// adapt input data structure (Img for image, net.Imglib2.Type for scalar) type and return type string
  			cipService.convertToMajorType(paramsNumber.get("inputImage") , paramsNumber.get("value"), operationType );
  			parametersFinal[1] = paramsNumber.get("inputImage").value;
  			parametersFinal[2] = paramsNumber.get("value").value;
  			opName = "Image_Number_"+opBaseName;	
  			inputImage = paramsNumber.get("inputImage");
  	  	}
  		else if ( paramsNumber2.parseInput( args )  )
  		{
  			// adapt input data structure (Img for image, net.Imglib2.Type for scalar) type and return type string
  			cipService.convertToMajorType(paramsNumber2.get("inputImage") , paramsNumber2.get("value"), operationType );
  			parametersFinal[1] = paramsNumber2.get("value").value;
  			parametersFinal[2] = paramsNumber2.get("inputImage").value;
  			opName = "Number_Image_"+opBaseName;	
  			inputImage = paramsNumber2.get("inputImage");
  	  	}
  		else
  		{
  			return null;
  		}
  		
  		Object results = ops().run( opName , parametersFinal ); 
  		results = cipService.setMetadata( results, inputImage, operationType+"_" );
  		
  		return results; 
  	}
    
    
    
    public interface MathUnary extends Op {
		// Note that the name and aliases are prepended with Namespace.getName
		String NAME = "math unary";
		
	}
    
    
    public Object cos( final Object... args ) {
    	
    	return math1Operation("cos", args );
    }
    
    public Object sin( final Object... args ) {
    	
    	return math1Operation("sin", args );
    }

    public Object tan( final Object... args ) {
    	
    	return math1Operation("tan", args );
    }

    public Object acos( final Object... args ) {
    	
    	return math1Operation("acos", args );
    }
    
    public Object asin( final Object... args ) {
    	
    	return math1Operation("asin", args );
    }

    public Object atan( final Object... args ) {
    	
    	return math1Operation("atan", args );
    }

    public Object log( final Object... args ) {
    	
    	return math1Operation("log", args );
    }
    
    public Object exp( final Object... args ) {
    	
    	return math1Operation("exp", args );
    }

    public Object sqrt( final Object... args ) {
    	
    	return math1Operation("sqrt", args );
    }

    public Object abs( final Object... args ) {
    	
    	return math1Operation("abs", args );
    }

    public Object round( final Object... args ) {
    	
    	return math1Operation("round", args );
    }

    public Object floor( final Object... args ) {
    	
    	return math1Operation("floor", args );
    }

    public Object ceil( final Object... args ) {
    	
    	return math1Operation("ceil", args );
    }

    public Object sign( final Object... args ) {
    	
    	return math1Operation("sign", args );
    }
    
    
    
    
    
    @OpMethod(op = invizio.cip.CIP.MathUnary.class )
   	private Object math1Operation( String operationType , final Object[] args ) {
   		
   		FunctionParameters2 paramsImage = new FunctionParameters2("image function");
  		paramsImage.addRequired("param1", 	Type.image 	);
  		
   		FunctionParameters2 paramsNumber = new FunctionParameters2("number function");
  		paramsNumber.addRequired("param1", 	Type.scalar	);

  		
  		Object[] parametersFinal = new Object[2];
		parametersFinal[0] = operationType;
		String opName = null;
		String opBaseName = "Math1OperationCIP";
		DefaultParameter2 inputImage = null;
  		if ( paramsImage.parseInput( args ) )
  		{
  			cipService.toRaiCIP( paramsImage.get("param1") );
  			parametersFinal[1] = paramsImage.get("param1").value ;
  			opName = "Image_"+opBaseName;
  			inputImage = paramsImage.get("inputImage");
  		}
  		else if (  paramsNumber.parseInput( args )   )
  		{
  			parametersFinal[1] = paramsNumber.get("param1").value;
  			opName = "Number_"+opBaseName;	
  		}
  		else
  		{
  			return null;
  		}
  		
  		Object results = ops().run( opName , parametersFinal );
  		if (inputImage != null ) // else the output is a scalar
  			results = cipService.setMetadata( results, inputImage, operationType+"_" );
  		
  		return  results;
  		
  	}
    
    
    
    int count=0;
    
    @OpMethod(op = invizio.cip.misc.CreateCIP.class)
    public Object create( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params1 = new FunctionParameters2("create1");
		params1.addRequired("inputImage", 	Type.image	);
		params1.addOptional("value", 		Type.scalar , 	0	);
		params1.addOptional("type", 		Type.string , 	null	);
		params1.addOptional("name", 		Type.string , 	"new_"+count	);
		  		
   		FunctionParameters2 params2 = new FunctionParameters2("create2");
		params2.addRequired("extent", 		Type.scalars	);
		params2.addOptional("value", 		Type.scalar , 	0	);
		params2.addOptional("type", 		Type.string , 	"float"	);
		params2.addOptional("name", 		Type.string , 	"new_"+count	);
		 
		count++;
   		String name="";
   		Object[] paramsFinal=null;
   		DefaultParameter2 inputImage = null;
  		if ( params1.parseInput( args ) )
  		{
  			cipService.toRaiCIP( params1.get("inputImage") );
  			
  			if ( params1.get("type").value == null ){
  				// set the type to same type as the image
  				params1.get("type").value = cipService.getImgLib2ScalarType( params1.get("inputImage").value );
  			}

  			Interval interval = (Interval)params1.get("inputImage").value;
  			long[] dimensions = new long[interval.numDimensions()];
  			interval.dimensions(dimensions);
  			params1.get("inputImage").value = dimensions;
  			
  			paramsFinal = params1.getParsedInput();
  			
  			inputImage =  params1.get("inputImage");
  			name = (String) params1.get("name").value;
  		}
  		else if(  params2.parseInput( args ) )
  		{
  			paramsFinal = params2.getParsedInput();
  			name = (String) params2.get("name").value;
  		}
  		else {
  			//TODO: error message
  			return null;
  		}
		results = ops().run( invizio.cip.misc.CreateCIP.class, paramsFinal );
		results = cipService.setMetadata(results , inputImage, "new_");
		((RaiCIP2<?>) results).name = name;
		
		return results; 
  	}
    
    
    
    @OpMethod(op = invizio.cip.misc.SliceCIP.class)
    public Object slice( final Object... args ) {
   		
   		Object results = null;
   		
   		FunctionParameters2 params = new FunctionParameters2("sliceCIP");
		params.addRequired("inputImage", 	Type.image	);
		params.addOptional("dimensions", 	Type.scalars,	null	);
		params.addOptional("position",		Type.scalars, 	null	);
		params.addOptional("method",		Type.string, 	"shallow"	);
		
		if ( params.parseInput( args ) )
		{
			cipService.toRaiCIP( params.get("inputImage") );
			results = ops().run( invizio.cip.misc.SliceCIP.class , params.getParsedInput() );
			
			((RaiCIP2<?>) results).name = "slice_" + ((RaiCIP2<?>) params.get("inputImage").value ).name;
			// metadata handling is done in the SliceCIP class
		}
		else 
		{
			// TODO: send an error message
		}
   		
   		return results;
    }

    
    @OpMethod(op = invizio.cip.misc.DuplicateCIP.class)
    public Object duplicate( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("sliceCIP");
		params.addRequired("inputImage", 	Type.image	);
		params.addOptional("origin", 		Type.scalars,	null	);
		params.addOptional("size",			Type.scalars, 	null	);
		params.addOptional("method",		Type.string, 	"shallow"	);

		if ( params.parseInput( args ) )
		{
			cipService.toRaiCIP( params.get("inputImage") );
			results = ops().run( invizio.cip.misc.DuplicateCIP.class , params.getParsedInput() );
			((RaiCIP2<?>) results).name = "dupl_" + ((RaiCIP2<?>) params.get("inputImage").value ).name;
		}
		else 
		{
			// TODO: send an error message
		}
   		
   		return results;
    }
    

    
    
    @OpMethod(op = invizio.cip.misc.Project2CIP.class)
    public Object project( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("projectCIP");
		params.addRequired("inputImage", 	Type.image	);
		params.addRequired("dimension", 	Type.scalar	);
		params.addOptional("method",		Type.string, 	"max"		);
		params.addOptional("outputType",	Type.string, 	"projection");
		
		if ( params.parseInput( args ) )
		{
			cipService.toRaiCIP( params.get("inputImage") );
			List<Object> resultsTemp = (List<Object>) ops().run( invizio.cip.misc.Project2CIP.class , params.getParsedInput() );
			
			///////////////////////////////////////////////////////////////////////////////
			// check if one of the output is null and discard it from the results list
			results = cipService.discardNullValue(resultsTemp);
			((RaiCIP2<?>) results).name = (String) params.get("method").value + "proj_" + ((RaiCIP2<?>) params.get("inputImage").value ).name;
			
		}
		else 
		{
			// TODO: send an error message
		}
   		
   		return results;
    }

    
    
    /**
     * 
     * @param args an image   
     * @return an array containing the origin of the image
     * 
     */
    public Long[] origin( Object... args )
    {
    	
    	// TODO: cf spacing
    	
    	FunctionParameters2 params = new FunctionParameters2("getOrigin");
		params.addRequired("inputImage", 	Type.image	);
		
		Long[] origin = null;
		if ( params.parseInput( args ) )
		{	
			DefaultParameter2 image = params.get("inputImage");
			cipService.toRaiCIP( image );
			Interval interval = (Interval) image;
			origin = new Long[interval.numDimensions()];
			for(int d=0; d< interval.numDimensions(); d++)
				origin[d] = interval.min(d);
		}
    	return origin;
    }

    
    public Long[] size( Object... args )
    {
    	FunctionParameters2 params = new FunctionParameters2("size");
		params.addRequired("inputImage", 	Type.image	);
		
		Long[] size = null;
		if ( params.parseInput( args ) )
		{	
			DefaultParameter2 image = params.get("inputImage");
			cipService.toRaiCIP( image );
			Interval interval = (Interval) image;
			size = new Long[interval.numDimensions()];
			for(int d=0; d< interval.numDimensions(); d++)
				size[d] = interval.dimension(d);
		}
    	return size;
    }

    
    // functions to collect luts, spacing, axes names
    public List<Double> spacing( Object input ) {
    	
    	// TODO:
    	//		signature 1: if input is an image return the image spacing
    	//		signature 2: if input is a list of double set these as the new image spacing
    	//					 if input is a list of Axes name and a list of spacing adjust only these
    	
    	return cipService.spacing( input );
    }
    
    public List<String> unit( Object input ) {
    	
    	// TODO: cf spacing
    	
    	return cipService.unit( input );
    }

    public List<String> axes( Object input ) {
    	
    	// TODO: cf spacing
    	return cipService.axes( input );
    }
    
    
    
    
    public String show( Object ... args )
    {	
    	
    	FunctionParameters2 paramsImg = new FunctionParameters2("showImage");
    	paramsImg.addRequired("inputImage", 	Type.image				);
    	paramsImg.addOptional("lut", 			Type.strings, 	"grays"	);
    	// params.addOptional("channelDim", 	Type.scalar	, null		);  //if ch dim exist , swap with requested dim, else overwrite the proposed dim 
    	
    	
    	FunctionParameters2 paramsReg = new FunctionParameters2("showRegion");
    	paramsReg.addRequired("imageHandle", 	Type.string				);
    	paramsReg.addRequired("region", 		Type.region 			); // should also handle the case of roi, list<roi>, and list<list<roi>>
    	paramsReg.addOptional("color", 			Type.string,  "lila" 	);
    	paramsReg.addOptional("width", 			Type.scalar,   1.0 		);
    	paramsReg.addOptional("scalars", 		Type.scalars,  null 	);
    	paramsReg.addOptional("reset", 		Type.logic,    new Boolean(false) ); // whether one should reset the overlay or not
    	// also fill for fill color and style for the stroke type
    	
    	
    	String name = null;
    	
    	if ( paramsImg.parseInput( args ) )
		{
    		name  = showCipService.showImage(paramsImg);
    	}
    	else if ( paramsReg.parseInput( args ) )
		{
    		showCipService.showRegion(paramsReg);
		}
    	
    	return name;
    }
	
    
    
    
    
    
    public Object toIJ2(Object ... args ) {
    	
    	Object result = null;
    	
    	FunctionParameters2 paramsImg = new FunctionParameters2("toIJ1_Image");
    	paramsImg.addRequired("image", 	Type.image				);

    	FunctionParameters2 paramsReg = new FunctionParameters2("toIJ1_Region");
    	paramsImg.addRequired("region", 	Type.region				);

    	if ( paramsImg.parseInput( args ) )
		{	
    		Object image = paramsImg.get("image").value;
    		result = new DefaultDataset( this.context() , cipService.toImgPlus( image ) );
		}
    	else if ( paramsReg.parseInput( args ) )
		{
    		// TODO: add conversion from Roi or List<Roi> to Region or List<Region>
    		Object regions = paramsReg.get("region").value; // a region, list of region, roi, list<roi> (2d), List<List<Roi>> (3d)
    		result = Regions.toIterableRegion(regions); // always return a list of iterable regions
		}
    	
    	return result; 
    	
    }
    
    
    
    public Object toIJ1(Object ... args) {
    	
    	Object result = null;
    	
    	FunctionParameters2 paramsImg = new FunctionParameters2("toIJ1_Image");
    	paramsImg.addRequired("image", 	Type.image				);

    	FunctionParameters2 paramsReg = new FunctionParameters2("toIJ1_Region");
    	paramsImg.addRequired("region", 	Type.region				);

    	if ( paramsImg.parseInput( args ) )
		{
    		result = cipService.toImagegPlus( paramsImg.get("image").value );
    	}
    	else if ( paramsReg.parseInput( args ) )
		{
    		result = Regions.toIJ1ROI( paramsReg.get("region").value );
		}
    	
    	return result; 
    	
    }
    
    
    // return an iterable region or a list of iterable regions depending on the input
    // to check: does a thresholded imagePlus converts to a BooleanType RaiCIP2 ?
    public Object region( Object image )
    {
    	return Regions.toIterableRegion( image, cipService );
    }
    
    
    /////////////////////////////////////////////////////////
    // helper to pass a list from jython             //
    /////////////////////////////////////////////////////////
    
    // this way I only need to add list support in the input parsing
//    public static List<Object> list( Object ... args ) {
//		List<Object> list = new ArrayList<Object>();
//		for ( Object obj : args ) {
//			list.add( obj );
//		}
//		return list;
//	}
	
    public static <T> List<T> list( T ... args ) {
		List<T> list = new ArrayList<T>();
		for ( T obj : args ) {
			list.add( obj );
		}
		return list;
	}
    
    public static List<Double> list( Double ... args ) {
		List<Double> list = new ArrayList<Double>();
		for ( Double obj : args ) {
			list.add( obj );
		}
		return list;
	}

    
    // would be awesome if all arrays and scalar would represented by RAIs (i.e. not only images)
	@Deprecated
    public static Img<DoubleType> asimg( double ... ds )
	{
		Img<DoubleType> array = ArrayImgs.doubles(ds.length,1);
		Cursor<DoubleType> c = array.cursor();
		int idx=0;
		while ( c.hasNext() ) {
			c.next().set(ds[idx]);
			idx++;
		}
		return array;
	}
    
	
	
	public void setNumberOfthread( int nThread)
	{
		nThread = Math.max(1 , nThread);
		this.nThread = nThread;
	}
	
	
	
	
	public static void main(final String... args) 
	{
		
		ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		//ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
		ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs32.tif");
		//ij.ui().show(imp);
//		Img<FloatType> img = ImageJFunctions.wrap(imp);
//		float threshold = 100;
//		Float[] pixelSize = new Float[] {0.5f};
		
		CIP cip = new CIP();
		cip.setContext( ij.getContext() );
		cip.setEnvironment( ij.op() );
//		@SuppressWarnings("unchecked")
//		RandomAccessibleInterval<IntType> distMap = (RandomAccessibleInterval<IntType>) cip.distance(img, threshold );
//		
//		ij.ui().show(distMap);
		
		String h1 = cip.show( imp , "red");
		String h2 = cip.show( imp , "green");
		
		System.out.println("h1 -> "+h1);
		System.out.println("h2 -> "+h2);
		System.out.println("done!");
	}
	
	
	
	
	
	
}
