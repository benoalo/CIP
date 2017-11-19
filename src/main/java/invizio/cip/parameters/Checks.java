package invizio.cip.parameters;


import ij.ImagePlus;
import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;


/**
 * 
 * @author Benoit Lombardot
 *
 */

public class Checks {

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
	
	
	
	public static boolean isNumeric( Object value)
	{
		if( isScalar( value ) )
			return true;
		else {
			if( isArray(value) ){
				return isScalar( ((Object[]) value)[0] );  // won't work if value is a primitive array
			}
			if ( isIterable(value) ){
				Iterable<?> iterable = (Iterable<?>) value;
				for( Object obj : iterable ) {
					return isScalar( obj ); // in principle would have to be checked
				}
			}
		}
		return false;
	}
	
	
	public static boolean isOneRegion( Object value)
	{
		if( value instanceof IterableRegion )
			return true;
		
		return false;
	}

	
	public static boolean isRegion( Object value)
	{
		if( isOneRegion( value ) )
			return true;
		else {
			if( isArray(value) ){
				return isOneRegion( ((Object[]) value)[0] );  // won't work if value is a primitive array
			}
			if ( isIterable(value) ){
				Iterable<?> iterable = (Iterable<?>) value;
				for( Object obj : iterable ) {
					return isOneRegion( obj ); // in principle all items would have to be checked
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
	
	
	
	public static boolean isText( Object value)
	{
		if( isString( value ) )
			return true;
		else {
			if( isArray(value) ){
				return isString( ((Object[]) value)[0] );  // won't work if value is a primitive array
			}
		}
		return false;
	}
	
	
	
	public static boolean isImage( Object value)
	{
		if	( 	value instanceof RandomAccessibleInterval 	||
				value instanceof Dataset 					||
				value instanceof ImagePlus					
		) {
			return true;
		}
		
		return false;
	}
	
	
	
	
	
	public static boolean isArray( Object value)
	{
		if ( value == null )
			return false;
		
		if	( value.getClass().isArray() )
			return true;
		
		
		return false;
	}
	
	
	private static boolean isIterable(Object value) {
		if ( value == null )
			return false;
		
		if	( value instanceof Iterable )
			return true;
		
		return false;
	}
	
}
	
