package invizio.cip.parameters;

import java.util.ArrayList;
import java.util.List;




/**
 * 
 * @author Benoit Lombardot
 *
 */


/*
 * TODO:
 *  [-] improve error message (if the pb is with a positional parameter possible syntax)
 *  [x] add handling of list of number (from jython)
 *  [x] create a util to easily create a list from script
 *  [x] (not needed) automatically convert List of PyInteger or PyFloat to Integer or Float 
 *  [x] explore whether using list rather than array would work smoother as parameter for the op function (see distanceOP to test)
 */
		



public class DefaultParameter2 implements Parameter{
	
	public String name;
	public List<String> aliases = new ArrayList<String>(); 
	public boolean required=true;
	public Object value=null;
	public boolean served;
	
	public Type type;
	
	
	
	public enum Type{
		
		image("Image"),
		scalar("Scalar"),
		numeric("Numeric"),
		string("String"),
		text("Text");
		
		String str;
		Type(String str){
			this.str =str;
		}
		
		public String getName() {
			return str;
		}
		
		public boolean instanceOf( Object obj ) {
			
			switch( this ) {
			case image :
				return Checks.isImage(obj);
				
			case scalar :
				return Checks.isScalar(obj);
				
			case numeric :
				return Checks.isNumeric(obj);
				
			case string :
				return Checks.isString(obj);
				
			default : // text :
				return Checks.isText(obj);
				
			}
		}
		
		
	}
	
	public DefaultParameter2(String name, Type type, boolean required, Object defaultValue)
	{
		this.name = name.toLowerCase();
		this.type = type;
		this.required = required;
		this.value = defaultValue;
		this.served = false;
	}

	
	public DefaultParameter2(String name, Type type)
	{
		this.name = name.toLowerCase();
		this.type = type;
		this.required = true;
		this.served = false;
	}
	
	
	@Override
	public String toString() {
		
		return  ( this.name + "\t, required=" + this.required + "\t, served=" + this.served  + "\t, alias=" + this.aliases.get(0) + "\t, value=" + (this.value==null? "null":this.value.toString()) )  ;
		
	}
	
	/*
	List<ParameterChecker> parameterChecks = new ArrayList<ParameterChecker>();
	
	public void addChecker( ParameterChecker paramCheck )
	{
		parameterChecks.add( paramCheck );
	}
	
	
	
	public boolean check()
	{
		for(ParameterChecker paramChecker : parameterChecks ) {
			if( ! paramChecker.check( this.value ) )
			{
				System.err.println( paramChecker.errorString( value ) );
				return false;
			}
		}
		
		return true;
	}
	*/
	
}
