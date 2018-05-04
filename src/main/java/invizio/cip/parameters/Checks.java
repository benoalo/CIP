package invizio.cip.parameters;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ij.ImagePlus;
import ij.gui.Roi;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;


/**
 * 
 * @author Benoit Lombardot
 *
 */


// 2017-11-2017 : remove Array support as List as systematically used in functions,
//					could be added later if array<->list converter are in place (rk: available from IJ2 )

public class Checks {
	
	public static boolean isLogic( Object value)
	{
		if( value instanceof Boolean )
			return true;
		
		return false;
	}
	
	public static boolean isScalar( Object value)
	{
		if( 	value instanceof Byte		||
				value instanceof Short		||
				value instanceof Integer	||
				value instanceof Long		||
				value instanceof Float		||
				value instanceof Double				
		) {
			return true;
		}
		return false;
	}
	
	
	
	public static boolean isScalars( Object value)
	{
		if( isScalar( value ) )
			return true;
		else {
			if ( isList(value) ){
				List<?> list = (List<?>) value;
				for( Object obj : list ) {
					return isScalar( obj ); // lazy check only on the first item
				}
			}
		}
		return false;
	}
	
	
	public static boolean isOneRegion( Object value)
	{
		if( value instanceof IterableRegion )
			return true;
		else if ( value instanceof Roi)
    		return true;
    	
		
		return false;
	}

	

	public static boolean isRegion2( Object value)
	{
		// IterableRegion or Roi
		if( isOneRegion( value ) )
			return true;
		
		// List<?>
		else if ( isList( value )  &&  ((List<?>)value).size()>0  ) {
    		
			Object item = ((List<?>)value).get(0);
			
			// List<Roi> or List<IterableRegion>
			if( isOneRegion( item ) )
				return true;
			
			// List<List<Roi>>
			else if( item instanceof List ) {
				if( ((List<?>)item).size()>0 && ((List<?>)item).get(0) instanceof Roi) {
					return true;
				}
			}
    	}
    	
		
    	return false;
	}
	

	public static boolean isString( Object value)
	{
		if( value instanceof String )
			return true;
		
		return false;
	}
	
	
	
	public static boolean isStrings( Object value)
	{
		if( isString( value ) )
			return true;
		else {
			if ( isList(value) ){
				List<?> list = (List<?>) value;
				for( Object obj : list ) {
					return isString( obj ); // lazy check only on the first item
				}
			}
			
		}
		return false;
	}
	
	
	public static boolean isScalarOrString( Object value ) {
		
		if (isString(value) || isScalar(value) )
			return true;
		
		return false;
	}
	
	
	public static boolean isScalarsOrStrings( Object value)
	{
		if( isScalarOrString( value ) )
			return true;
		else {
			if ( isList(value) ){
				List<?> list = (List<?>) value;
				for( Object obj : list ) {
					return isScalarOrString( obj ); // lazy check only on the first item
				}
			}
			
		}
		return false;
	}
	
	
	public static boolean isImage( Object value)
	{
		if	( ( value instanceof RandomAccessibleInterval  &&  (!(value instanceof IterableRegion)) ) 	||
				value instanceof ImgPlus					||
				value instanceof Dataset 					||
				value instanceof ImagePlus					
		) {
			return true;
		}
		
		return false;
	}
	
	
	public static boolean isMeasure( Object value )
	{
		if ( value instanceof Map ) {
			Map<?,?> map = (Map<?,?>) value; 
			if (  map.keySet().iterator().next() instanceof String  &&  map.values().iterator().next() instanceof List ) {
				return true;
			}
		}
		
		return false;
	}
	
	
//	public static boolean isArray( Object value)
//	{
//		if ( value == null )
//			return false;
//		
//		if	( value.getClass().isArray() )
//			return true;
//		
//		
//		return false;
//	}
	
	
//	private static boolean isIterable(Object value) {
//		if ( value == null )
//			return false;
//		
//		if	( value instanceof Iterable )
//			return true;
//		
//		return false;
//	}
	
	private static boolean isList(Object value) {
		if ( value == null )
			return false;
		
		if	( value instanceof List )
			return true;
		
		return false;
	}
	
}
	
