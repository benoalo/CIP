package invizio.cip;

import java.util.ArrayList;
import java.util.List;

import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import invizio.cip.parameters.DefaultParameter2;
import invizio.cip.parameters.Format;
import invizio.cip.parameters.FunctionParameters2;
import invizio.cip.parameters.DefaultParameter2.Type;
import net.imagej.ImageJ;
import net.imagej.ops.AbstractNamespace;
import net.imagej.ops.Namespace;
import net.imagej.ops.Op;
import net.imagej.ops.OpMethod;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
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
			results = ops().run(invizio.cip.segment.HWatershedCIP.class, paramsHWS.getParsedInput() );
		}
		else if ( paramsSeededWS.parseInput( args ) )
		{
			results = ops().run(invizio.cip.segment.SeededWatershedCIP.class, paramsSeededWS.getParsedInput() );
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
	
	public interface DISTANCE extends Op {
		// Note that the name and aliases are prepended with Namespace.getName
		String NAME = "distance";
		String ALIASES = "dist";
	}

	// distance method
	@OpMethod(op = invizio.cip.CIP.DISTANCE.class)
	public Object distance(final Object... args) {
		
		
		Object results = null;
		
		FunctionParameters2 paramsDist = new FunctionParameters2("Distance");
		paramsDist.addRequired("inputImage", 	Type.image 	);
		paramsDist.addOptional("threshold", 	Type.scalar , 		null	);
		paramsDist.addOptional("pixelSize", 	Type.numeric , 		1f		);
		
		if ( paramsDist.parseInput( args ) )
		{
			results = ops().run(invizio.cip.filter.DistanceCIP.class, paramsDist.getParsedInput() );
		}
		return results; 
	}

	
	
	
	
	
	
	
	
	
	
	
	/********************************************************************************
	 * 	Maxima detection interface													*
	 ********************************************************************************/
	
	public interface MAXIMA extends Op {
		// Note that the name and aliases are prepended with Namespace.getName
		String NAME = "maxima";
		String ALIASES = "max";
	}
	
	// distance method
	@OpMethod(op = invizio.cip.CIP.MAXIMA.class)
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
		params.addOptional("PixelSize", 	Type.numeric, 	null	);
		
		if ( params.parseInput( args ) )
		{
			results = ops().run( invizio.cip.CIP.MAXIMA.class, params.getParsedInput() );
		}
		return results; 
	}
	
	
    public void test( Object... args ){
    
    	for(Object object : args)
            System.out.println( object.toString() );
    
    }
	

    
    
    
    
    
    
    
    
    
    /********************************************************************************
	 * 	image labeling 													*
	 ********************************************************************************/
 	
    @OpMethod(op = invizio.cip.segment.LabelCIP.class)
 	public Object label( final Object... args ) {
 		
 		Object results = null;
 	
 		FunctionParameters2 params = new FunctionParameters2("Maxima");
		params.addRequired("inputImage", 	Type.image 	);
		params.addOptional("threshold", 	Type.scalar , 	null	); // not needed if the image is of boolean type
		
		if ( params.parseInput( args ) )
		{
			results = ops().run( invizio.cip.segment.LabelCIP.class, params.getParsedInput() );
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
			results = ops().run( invizio.cip.segment.ThresholdManualCIP.class, params1.getParsedInput() );
		}
		else if ( params2.parseInput( args ) )
		{
			List<Object> resultsTemp = (List<Object>) ops().run( invizio.cip.segment.ThresholdAutoCIP.class, params2.getParsedInput() );
			
			
			///////////////////////////////////////////////////////////////////////////////
			// check if one of the output is null and discard it from the results list
			
			results = new ArrayList<Object>();
			int count = 0;
			for(Object obj : resultsTemp ) {
				if ( obj != null ) {
					count++;
					((ArrayList<Object>)results).add( obj );
				}
			}
			if( count==1 )
				results = ((ArrayList<Object>)results).get(0) ;
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
   		params.addRequired("radius", 		Type.numeric ); 
   		params.addOptional("boundary", 		Type.string  , 	null	); 
   		params.addOptional("pixelSize", 	Type.numeric , 	null	); 
   		
   		if ( params.parseInput( args ) )
   		{
   			results = ops().run( invizio.cip.filter.GaussCIP.class, params.getParsedInput() );
   		}
   		return results; 
   	}

    
    
   @OpMethod(op = invizio.cip.filter.MedianCIP.class)
   public Object median( final Object... args ) {
  		
  		Object results = null;
  	
  		FunctionParameters2 params = new FunctionParameters2("median");
 		params.addRequired("inputImage", 	Type.image 	);
 		params.addRequired("radius", 		Type.numeric );
 		params.addOptional("shape", 		Type.string  , 	null	); 
 		params.addOptional("boundary", 		Type.string  , 	null	); 
 		params.addOptional("pixelSize", 	Type.numeric , 	null	); 
 		
 		
 		if ( params.parseInput( args ) )
 		{
 			results = ops().run( invizio.cip.filter.MedianCIP.class, params.getParsedInput() );
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
 			results = ops().run( invizio.cip.filter.InvertCIP.class, params.getParsedInput() );
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
  		params.addRequired("radius", 		Type.numeric );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.numeric , 	null	); 
  		params.addOptional("nthread", 		Type.numeric ,	null	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			results = ops().run( invizio.cip.filter.DilationCIP.class, params.getParsedInput() );
  		}
  		return results; 
  	}
     
 
    
    @OpMethod(op = invizio.cip.filter.ErosionCIP.class)
   	public Object erode( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("erosion");
  		params.addRequired("inputImage", 	Type.image 	);
  		params.addRequired("radius", 		Type.numeric );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.numeric , 	null	); 
  		params.addOptional("nthread", 		Type.numeric ,	nThread	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			results = ops().run( invizio.cip.filter.ErosionCIP.class, params.getParsedInput() );
  		}
  		return results; 
  	}
 
       
    @OpMethod(op = invizio.cip.filter.OpeningCIP.class)
   	public Object opening( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("opening");
  		params.addRequired("inputImage", 	Type.image 	);
  		params.addRequired("radius", 		Type.numeric );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.numeric , 	null	); 
  		params.addOptional("nthread", 		Type.numeric ,	nThread	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			results = ops().run( invizio.cip.filter.OpeningCIP.class, params.getParsedInput() );
  		}
  		return results; 
  	}
       
       
      
       
    @OpMethod(op = invizio.cip.filter.OpeningCIP.class)
   	public Object closing( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("closing");
  		params.addRequired("inputImage", 	Type.image 	);
  		params.addRequired("radius", 		Type.numeric );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.numeric , 	null	); 
  		params.addOptional("nthread", 		Type.numeric ,	nThread	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			results = ops().run( invizio.cip.filter.ClosingCIP.class, params.getParsedInput() );
  		}
  		return results; 
  	}
       
       
       
    @OpMethod(op = invizio.cip.filter.TophatCIP.class)
   	public Object tophat( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params = new FunctionParameters2("tophat");
  		params.addRequired("inputImage", 	Type.image 	);
  		params.addRequired("radius", 		Type.numeric );
  		params.addOptional("shape", 		Type.string  , 	null	); 
  		params.addOptional("boundary", 		Type.string  , 	null	); 
  		params.addOptional("output", 		Type.string  ,	null	);
  		params.addOptional("pixelSize", 	Type.numeric , 	null	); 
  		params.addOptional("nthread", 		Type.numeric ,	nThread	);
  		
  		
  		if ( params.parseInput( args ) )
  		{
  			results = ops().run( invizio.cip.filter.TophatCIP.class, params.getParsedInput() );
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
		
  		if ( paramsImage.parseInput( args ) )
  		{
  			cipService.convertToMajorType(paramsImage.get("inputImage1") , paramsImage.get("inputImage2"), operationType );
  			parametersFinal[1] = paramsImage.get("inputImage1").value;
  			parametersFinal[2] = paramsImage.get("inputImage2").value;
  			opName = "Image_Image_MathOperationCIP";
  		}
  		else if (  paramsNumber.parseInput( args )   )
  		{
  			// adapt input data structure (Img for image, net.Imglib2.Type for scalar) type and return type string
  			cipService.convertToMajorType(paramsNumber.get("inputImage") , paramsNumber.get("value"), operationType );
  			parametersFinal[1] = paramsNumber.get("inputImage").value;
  			parametersFinal[2] = paramsNumber.get("value").value;
  			opName = "Image_Number_MathOperationCIP";	
  		}
  		else if ( paramsNumber2.parseInput( args )  )
  		{
  			// adapt input data structure (Img for image, net.Imglib2.Type for scalar) type and return type string
  			cipService.convertToMajorType(paramsNumber2.get("inputImage") , paramsNumber2.get("value"), operationType );
  			parametersFinal[1] = paramsNumber2.get("value").value;
  			parametersFinal[2] = paramsNumber2.get("inputImage").value;
  			opName = "Number_Image_MathOperationCIP";	
  		}
  		else
  		{
  			return null;
  		}
  		
  		return ops().run( opName , parametersFinal ); 
  		
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
  		if ( paramsImage.parseInput( args ) )
  		{
  			cipService.convertToMajorType(paramsImage.get("inputImage1") , paramsImage.get("inputImage2"), operationType );
  			parametersFinal[1] = paramsImage.get("inputImage1").value;
  			parametersFinal[2] = paramsImage.get("inputImage2").value;
  			opName = "Image_Image_"+opBaseName;
  		}
  		else if (  paramsNumber.parseInput( args )   )
  		{
  			// adapt input data structure (Img for image, net.Imglib2.Type for scalar) type and return type string
  			cipService.convertToMajorType(paramsNumber.get("inputImage") , paramsNumber.get("value"), operationType );
  			parametersFinal[1] = paramsNumber.get("inputImage").value;
  			parametersFinal[2] = paramsNumber.get("value").value;
  			opName = "Image_Number_"+opBaseName;	
  		}
  		else if ( paramsNumber2.parseInput( args )  )
  		{
  			// adapt input data structure (Img for image, net.Imglib2.Type for scalar) type and return type string
  			cipService.convertToMajorType(paramsNumber2.get("inputImage") , paramsNumber2.get("value"), operationType );
  			parametersFinal[1] = paramsNumber2.get("value").value;
  			parametersFinal[2] = paramsNumber2.get("inputImage").value;
  			opName = "Number_Image_"+opBaseName;	
  		}
  		else
  		{
  			return null;
  		}
  		
  		return ops().run( opName , parametersFinal ); 
  		
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
  		if ( paramsImage.parseInput( args ) )
  		{
  			parametersFinal[1] = paramsImage.get("param1").value;
  			opName = "Image_"+opBaseName;
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
  		
  		return ops().run( opName , parametersFinal ); 
  		
  	}
    
    
    
    
    
    @OpMethod(op = invizio.cip.misc.CreateCIP.class)
    public Object create( final Object... args ) {
   		
   		Object results = null;
   	
   		FunctionParameters2 params1 = new FunctionParameters2("create1");
		params1.addRequired("inputImage", 	Type.image	);
		params1.addOptional("value", 		Type.scalar , 	0	);
		params1.addOptional("type", 		Type.string , 	null	);
		  		
   		FunctionParameters2 params2 = new FunctionParameters2("create2");
		params2.addRequired("extent", 		Type.numeric	);
		params2.addOptional("value", 		Type.scalar , 	0	);
		params2.addOptional("type", 		Type.string , 	"float"	);
		  		
   		
   		Object[] paramsFinal=null;
  		if ( params1.parseInput( args ) )
  		{
  			cipService.toImglib2Image( params1.get("inputImage") );
  			
  			if ( params1.get("type").value == null ){
  				// set the type to same type as the image
  				params1.get("type").value = cipService.getImgLib2ScalarType( params1.get("inputImage").value );
  			}

  			Interval interval = (Interval)params1.get("inputImage").value;
  			long[] dimensions = new long[interval.numDimensions()];
  			interval.dimensions(dimensions);
  			params1.get("inputImage").value = dimensions;
  			
  			paramsFinal = params1.getParsedInput();
  		}
  		else if(  params2.parseInput( args ) )
  		{
  			paramsFinal = params2.getParsedInput();
  		}
  		else {
  			//TODO: error message
  			return null;
  		}
		results = ops().run( invizio.cip.misc.CreateCIP.class, paramsFinal );

  		return results; 
  	}
    
    
    
    @OpMethod(op = invizio.cip.misc.ProjectCIP.class)
    public Object slice( final Object... args ) {
   		
   		Object results = null;
   		
   		FunctionParameters2 params = new FunctionParameters2("sliceCIP");
		params.addRequired("inputImage", 	Type.image	);
		params.addOptional("dimensions", 	Type.numeric,	null	);
		params.addOptional("position",		Type.numeric, 	null	);
		params.addOptional("method",		Type.numeric, 	"shallow"	);
		
		if ( params.parseInput( args ) )
		{
			results = ops().run( invizio.cip.misc.ProjectCIP.class , params.getParsedInput() );
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
		params.addOptional("origin", 		Type.numeric,	null	);
		params.addOptional("size",			Type.numeric, 	null	);
		params.addOptional("method",		Type.string, 	"shallow"	);

		if ( params.parseInput( args ) )
		{
			results = ops().run( invizio.cip.misc.DuplicateCIP.class , params.getParsedInput() );
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
			List<Object> resultsTemp = (List<Object>) ops().run( invizio.cip.misc.Project2CIP.class , params.getParsedInput() );
			
			///////////////////////////////////////////////////////////////////////////////
			// check if one of the output is null and discard it from the results list
			
			results = new ArrayList<Object>();
			int count = 0;
			for(Object obj : resultsTemp ) {
				if ( obj != null ) {
					count++;
					((ArrayList<Object>)results).add( obj );
				}
			}
			if( count==1 )
				results = ((ArrayList<Object>)results).get(0) ;
			
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
    	FunctionParameters2 params = new FunctionParameters2("getOrigin");
		params.addRequired("inputImage", 	Type.image	);
		
		Long[] origin = null;
		if ( params.parseInput( args ) )
		{	
			DefaultParameter2 image = params.get("InputImage");
			cipService.toImglib2Image( image );
			Interval interval = (Interval) image;
			origin = new Long[interval.numDimensions()];
			for(int d=0; d< interval.numDimensions(); d++)
				origin[d] = interval.min(d);
		}
    	return origin;
    }

    
    public Long[] size( Object... args )
    {
    	FunctionParameters2 params = new FunctionParameters2("getSize");
		params.addRequired("inputImage", 	Type.image	);
		
		Long[] size = null;
		if ( params.parseInput( args ) )
		{	
			DefaultParameter2 image = params.get("InputImage");
			cipService.toImglib2Image( image );
			Interval interval = (Interval) image;
			size = new Long[interval.numDimensions()];
			for(int d=0; d< interval.numDimensions(); d++)
				size[d] = interval.dimension(d);
		}
    	return size;
    }

    
 

    
    
    
    /////////////////////////////////////////////////////////
    // helper to pass a list from jython             //
    /////////////////////////////////////////////////////////
    
    // this way I only need to add list support in the input parsing
    public static List<Double> aslist( double ... ds ) {
		List<Double> list = new ArrayList<Double>();
		for ( double value : ds ) {
			list.add( value );
		}
		return list;
	}
	
    
    // would be awesome if all arrays and scalar would represented by RAIs (i.e. and not only images)
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
		
		//ij.ui().showUI();
		
		//ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
		//ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs32.tif");
		//ij.ui().show(imp);
		/*
		
		Img<FloatType> img = ImageJFunctions.wrap(imp);
		float threshold = 100;
		Float[] pixelSize = new Float[] {0.5f};
		
		CIP cip = new CIP();
		cip.setContext( ij.getContext() );
		cip.setEnvironment( ij.op() );
		@SuppressWarnings("unchecked")
		RandomAccessibleInterval<IntType> distMap = (RandomAccessibleInterval<IntType>) cip.distance(img, threshold );
		
		ij.ui().show(distMap);
		*/
		System.out.println("done!");
	}
	
	
	
	
	
	
}
