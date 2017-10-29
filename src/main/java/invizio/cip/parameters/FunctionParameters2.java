package invizio.cip.parameters;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import invizio.cip.parameters.DefaultParameter2.Type;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;


/**
 * 
 * @author Benoit Lombardot
 *
 */



public class FunctionParameters2 extends LinkedHashMap< String , DefaultParameter2> {

	private static final long serialVersionUID = 1L;
	
	String functionName = "";
	List<String> parsingFeedback = new ArrayList<String>();
	boolean validInputs = false;
	
	public FunctionParameters2(String functionName)
	{
		this.functionName =functionName;
	}
	
	
	// shortcut to add parameter
	// add required : provide name and class
	public void addRequired( String name, DefaultParameter2.Type type )
	{
		this.add(new DefaultParameter2( name.toLowerCase(), type ));
	}

	
	// add optional : provide name, class, default (default could be null and calculated in the actual function it relies on the other parameter)
	public void addOptional( String name, DefaultParameter2.Type type, Object defaultValue )
	{
		boolean required=false;
		this.add(new DefaultParameter2( name.toLowerCase(), type, required, defaultValue ) );
	}

	@Override
	public DefaultParameter2 get( Object Key )
	{
		String paramName = (String) Key;
		return super.get( paramName.toLowerCase() );
	}
	
	private boolean add( DefaultParameter2 parameter )
	{
		super.put(parameter.name.toLowerCase() , parameter);
		
		parameter.name = parameter.name.trim();
		parameter.name = parameter.name.toLowerCase();
		
		// create one generic aliases if not already used
		String alias1 = parameter.name.substring( 0 , 1 ).toLowerCase();
		String alias2 = parameter.name.substring( 0 , 1 ).toLowerCase() + parameter.name.substring( 1 , 2 ).toLowerCase();
		if( findMatchingParameter( alias1 )== null )
		{
			parameter.aliases.add( alias1 );
		}
		else if( findMatchingParameter( alias1 )== null )
		{
			parameter.aliases.add( alias2 );
		}
		
		return true;
	}
	
	
	
	// needs to be run after parseInput(Objects[] )
	public Object[] getParsedInput()
	{
		Object[] paramArray = new Object[ this.size() ];
		int count=0;
		for( DefaultParameter2 p : this.values() )
		{
			paramArray[count] = p.value ; 
			count++;
		}
		return paramArray;
	}
	
	
	
	public boolean parseInput( Object[] args ) {
		
		boolean validInputs = true;
		
		
		Iterator<DefaultParameter2> pIterator = this.values().iterator();
		int nth = -1;
		for( int i=0; i<args.length ; i++ )
		{
			DefaultParameter2 p = pIterator.next();
			nth=i;
			Object arg = args[nth];
			
			
			if( arg instanceof String )
			{
				// check is arg matches one of the parameter name or there aliases
				if( findMatchingParameter( (String) arg ) != null )
				{
					nth -= 1;
					break; // end of positionnal parameter, start of named parameter
				}
				// else do nothing
			}
			
			
			if( p.type.instanceOf(arg) || arg==null ) 
			{
				p.value = arg;
				p.served = true;
			}
			else 
			{
				validInputs = false;
				
				parsingFeedback.add("Warning: "+ functionName +", "+ nth + "th parameter, expects " + p.type.getName() + " , got " +  args[nth].getClass().getName() );
				break;
			}
		}
		
		////////////////////////////////////////////////////////////////////////////
		// handle the remaining arguments as pairs of String/Object
		////////////////////////////////////////////////////////////////////////////
		
		nth += 1;
		int remainingArgs = args.length-(nth);
		int numPairs = remainingArgs / 2 ;
		for(int i=0 ; i<numPairs ; i++)
		{
			
			// test if the first element of the pair is a string and whether it matches with name or aliased of one of the parameter
			
			DefaultParameter2 p = null;
			
			if (args[nth] instanceof String) 
			{
				String name  = (String) args[nth];
				p = findMatchingParameter( name );
				
				if( p==null ) // the string found no match
				{	
					parsingFeedback.add("Warning: "+ functionName +", no parameter is named \"" + name + "\"" );
					nth += 2;
					continue; // process the next pair of input
				}
			}
			else {
				validInputs = false;
				parsingFeedback.add("Error: "+ functionName +", "+  nth +"th parameter, a string naming a parameter was expected , got " +  args[nth].getClass().getName() );
				break;
			}
			
			
			// test if the 2nd argment of the key/value pair matches with the type of the identified parameter
			
			Object arg = args[nth+1];
			if( p.type.instanceOf(arg) || arg == null ) 
			{
				p.value = arg;
				p.served = true;
			}
			else 
			{
				validInputs = false;
				
				parsingFeedback.add("Error: "+ functionName +", parameter \"" + p.name + "\" expects " + p.type.getName() + " , got " + arg.getClass().getName() );
				break;
			}
			nth += 2;
		}
		
		// check that all required parameters received data (their valid flag should be true)
		if( validInputs == true ) {
			for( DefaultParameter2 p : this.values() ) {
				if( p.required && !p.served ) {
					validInputs = false;
					parsingFeedback.add("Error: "+ functionName +", parameter \"" + p.name + "\" is required but not provided" );
				}
			}
		}
		
		this.validInputs = validInputs;
		return validInputs;
	}
	
	
	
	
	
	
	// Find the first parameter which name or aliases are matching name
	private DefaultParameter2 findMatchingParameter( String name )
	{
		for( DefaultParameter2 p : this.values() )
		{
			if ( isMatching( name , p ) ) {
				return p;
			}
		}
		return null;
	}
	
	
	
	private boolean isMatching( String name, DefaultParameter2 p) {
		
		name = name.toLowerCase();
		
		if( p.name.equals( name ) ){
			return true;
		}
		for(String alias : p.aliases ) {
			if( alias.equals( name ) ){
				return true;
			}
		}
		return false;
	}
	
	
	
	
	@Override
	public String toString() {
		
		String  str = "";
		for( DefaultParameter2 p : this.values() )
		{
			str += p.toString() + "\n";
		}
		return str;
	}
	
	
	
	public void printFeedback()
	{
		if( validInputs==false )
		{
			System.out.println("Input do not match " + functionName );
			for( String str : parsingFeedback )
				System.out.println( str ); 
		}
		else
		{
			System.out.println("Inputs match " + functionName );
		}
	}
	
	
	
	public static void main(final String... args)
	{
		System.out.println("==============================");

		
		//ImagePlus imp = IJ.openImage("F:\\projects\\blobs32.tif");
		ImagePlus imp = IJ.openImage("C:/Users/Ben/workspace/testImages/blobs32.tif");
		Img<FloatType> img = ImageJFunctions.wrap(imp);
		
		FunctionParameters2 paramsHWS = new FunctionParameters2("HWatershed");
		paramsHWS.addRequired("inputImage", 	Type.image );
		paramsHWS.addOptional("threshold", 		Type.scalar , 	null);
		paramsHWS.addOptional("hMin", 			Type.scalar , 	null);
		paramsHWS.addOptional("PeakFlooding", 	Type.scalar , 	100f );
		paramsHWS.addOptional("Method", 		Type.string , 	"gray");
		Object[] objects = new Object[] {"P", new Integer(95), "T", new Float(100)};
		paramsHWS.parseInput( objects );
		paramsHWS.printFeedback();
		System.out.println("");
		
		FunctionParameters2 paramsHWS2 = new FunctionParameters2("HWatershed2");
		paramsHWS2.addRequired("inputImage", 	Type.image );
		paramsHWS2.addOptional("threshold", 	Type.scalar , 	null);
		paramsHWS2.addOptional("hMin", 			Type.scalar , 	null);
		paramsHWS2.addOptional("PeakFlooding", 	Type.scalar , 	100f );
		paramsHWS2.addOptional("Method", 		Type.string , 	"gray");
		Object[] objects2 = new Object[] {img, "P", new Integer(95), "T", new Float(100)};
		paramsHWS2.parseInput( objects2 );
		paramsHWS2.printFeedback();
		
		//Object[] paramArray = paramsHWS2.getParsedInput();
		System.out.println("");
		
		//System.out.println( paramsHWS.toString() );
		
		System.out.println("done!");
	}
	
	
}
