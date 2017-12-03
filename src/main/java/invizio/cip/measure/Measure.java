package invizio.cip.measure;

import invizio.cip.parameters.DefaultParameter2;


/**
 * 
 * @author Benoit Lombardot
 *
 **/


public class Measure {
	
	final public String name;
	final public DefaultParameter2.Type type; // string(s) or scalar(s)
	final public Object value;	// expected to be a a string, a double, map<string,string>, or map<string,double>
	 
	
	//	type	| value
	//	string	| String
	//	Strings	| Map<String,String>
	//	scalar	| Double
	//	scalars	| Map<String,Double>	for instance position would be {'x':1, 'y':2, 'z':10 }
	
	public Measure(String name, DefaultParameter2.Type type, Object value)
	{
		this.name = name;
		this.type = type;
		this.value = value;
	}
}
