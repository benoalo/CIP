package invizio.cip;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import ij.ImagePlus;
import invizio.cip.parameters.DefaultParameter2;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.ops.OpService;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
 


/**
 * 
 * * @author Benoit Lombardot
 *
 */

@Plugin(type = Service.class)
public class CIPService extends AbstractService implements ImageJService {
 
  @Parameter
  private ScriptService scriptService;
 
  @Parameter
  private OpService op;
  
  @Parameter
  private ConvertService convertService;
  
  @Override
  public void initialize() {
    
	// Register this namespace with the ScriptService so we can drop package prefixes
    // in script parameters, allowing:
    // @CIP
    // instead of
    // @invizio.cip.CIP
    scriptService.addAlias(invizio.cip.CIP.class);
    
	  //System.out.println("hello");
    
    
  }
  
  CIP cip = null;
  
  public CIP cip() {
	  
	  if ( cip == null ) {
		  cip = new CIP();
		  cip.setContext( this.getContext() );
		  cip.setEnvironment( op );
	  }
	  return  cip;
  }
  
  
  
  
	// create a static look of type conversion
	static final Map<String, Integer> pixelTypeOrder= new LinkedHashMap<String,Integer>();
	static final  Map<String, Boolean> isDecimalPixelType = new LinkedHashMap<String,Boolean>();
	static final  Map<String, Boolean> isUnsignedPixelType = new LinkedHashMap<String,Boolean>();
	
	static {
		pixelTypeOrder.put( "BoolType", 1 );
		pixelTypeOrder.put( "BitType", 1 );
		pixelTypeOrder.put( "ByteType", 2 );
		pixelTypeOrder.put( "UnsignedByteType", 3 );
		pixelTypeOrder.put( "ShortType", 4 );
		pixelTypeOrder.put( "UnsignedShortType", 5);
		pixelTypeOrder.put( "IntType", 6 );
		pixelTypeOrder.put( "UnsignedIntType", 7 );
		pixelTypeOrder.put( "FloatType", 8 );
		pixelTypeOrder.put( "LongType", 9 );
		pixelTypeOrder.put( "UnsignedLongType", 10 );
		pixelTypeOrder.put( "DoubleType", 11 );
		
		isDecimalPixelType.put( "BoolType", false );
		isDecimalPixelType.put( "BitType", false);
		isDecimalPixelType.put( "ByteType", false );
		isDecimalPixelType.put( "UnsignedByteType", false );
		isDecimalPixelType.put( "ShortType", false );
		isDecimalPixelType.put( "UnsignedShortType", false);
		isDecimalPixelType.put( "IntType", false );
		isDecimalPixelType.put( "UnsignedIntType", false );
		isDecimalPixelType.put( "FloatType", true );
		isDecimalPixelType.put( "LongType", false );
		isDecimalPixelType.put( "UnsignedLongType", false );
		isDecimalPixelType.put( "DoubleType", true );
		
		isUnsignedPixelType.put( "BoolType", false );
		isUnsignedPixelType.put( "BitType", false);
		isUnsignedPixelType.put( "ByteType", false );
		isUnsignedPixelType.put( "UnsignedByteType", true );
		isUnsignedPixelType.put( "ShortType", false );
		isUnsignedPixelType.put( "UnsignedShortType", true);
		isUnsignedPixelType.put( "IntType", false );
		isUnsignedPixelType.put( "UnsignedIntType", true );
		isUnsignedPixelType.put( "FloatType", false );
		isUnsignedPixelType.put( "LongType", false );
		isUnsignedPixelType.put( "UnsignedLongType", true );
		isUnsignedPixelType.put( "DoubleType", false );
	}	
	
	
	
	
	public void convertToMajorType( DefaultParameter2 parameter1 , DefaultParameter2 parameter2, String operationType )
	{
		toImglib2Object( parameter1 );
		toImglib2Object( parameter2 );
		
		// define the major type to can be casted for the operation output
		String type1 = getImgLib2ScalarType( parameter1.value );
		String type2 = getImgLib2ScalarType( parameter2.value );
		String majorType = findCompatibleType(type1, type2);
		
		if( operationType.equals("divide") )
			if ( majorType!="DoubleType" )
				majorType = "FloatType";
		
		// do the conversion of the input param
		if( !type1.equals(majorType))
			updateImglib2Type( parameter1 , majorType );
		if( !type2.equals(majorType))
			updateImglib2Type( parameter2 , majorType );
	
	}
	
	
	public void toImglib2Object( DefaultParameter2 parameter )
	{
		if(parameter.type == DefaultParameter2.Type.image )
			toImglib2Image( parameter );
		else if(parameter.type == DefaultParameter2.Type.scalar )
			toImglib2Scalar( parameter );
		
		return;
	}
	
	public void toImglib2Image(DefaultParameter2 parameter)
	{
		Object input = parameter.value;
		
		if (	input instanceof RandomAccessibleInterval )
		{ 
			// do nothing
		}
		else if (	input instanceof Dataset )
		{
			Dataset dataset = (Dataset) input;
			parameter.value = convertService.convert( dataset , Img.class );			
		}
		else if (	input instanceof ImagePlus )
		{
			ImagePlus imp = (ImagePlus) input;
			parameter.value = convertService.convert( imp , Img.class );
		}
		else {
			System.err.println("Unknown image type:" + input.getClass().getName() );
		}
		
		return;
	}
	
	
	
	
	public void toImglib2Scalar(DefaultParameter2 parameter)
	{
		
		Object value = parameter.value;
		Type<?> adaptedValue = null;
		if( value instanceof Boolean	)
		{
			adaptedValue = new ByteType();
			((BitType)adaptedValue).set( (Boolean) value );
		}
		else if( value instanceof Byte	)
		{
			adaptedValue = new ByteType();
			((ByteType)adaptedValue).set( (Byte) value );
		}
		else if( value instanceof Short	)
		{
			adaptedValue = new ShortType();
			((ShortType)adaptedValue).set( (Short) value );
		}
		if( value instanceof Integer	)
		{
			adaptedValue = new IntType();
			((IntType)adaptedValue).set( (Integer) value );
		}
		else if( value instanceof Float )
		{
			Float valF = (Float) value;
			if(  ( valF - Math.floor( valF.doubleValue() ) ) == 0  )
			{
				adaptedValue = new IntType();
				((IntType)adaptedValue).set( new Integer( valF.intValue() ) );
			}
			else {
				adaptedValue = new FloatType();
				((FloatType)adaptedValue).set( (Float) value );
			}
		}
		else if( value instanceof Double )
		{
			Double valD = (Double) value;
			if(  ( valD - Math.floor( valD.doubleValue() ) ) == 0  )
			{
				adaptedValue = new IntType();
				((IntType)adaptedValue).set( new Integer( valD.intValue() ));
			}
			else {
				adaptedValue = new FloatType();
				((FloatType)adaptedValue).set( (Float) valD.floatValue() );
			}
		}
		else if( value instanceof Long	)
		{
			adaptedValue = new LongType();
			((LongType)adaptedValue).set( (Long) value );
		}
		
		parameter.value = adaptedValue;
		
	}
	
	
	public String getImgLib2ScalarType( Object object )
	{
		String scalarType = null;
		if ( object instanceof Type ) {
			scalarType = object.getClass().getSimpleName();
		}
		else if( object instanceof RandomAccessibleInterval) {
			scalarType = ((RandomAccessibleInterval<?>)object).randomAccess().get().getClass().getSimpleName();
		}
		else if( object instanceof IterableInterval) {
			scalarType = ((IterableInterval<?>)object).firstElement().getClass().getSimpleName();
		}
		else {
			System.out.println("CIP: " + object.toString() + " does not have an Imglib2 scalarType (Type, Img, IterableInterval, RandomAccessibleInterval expected)");
		}
		
		return scalarType;
	}
	
	
	
	public void updateImglib2Type( DefaultParameter2 parameter , String type )
	{
		if(parameter.type == DefaultParameter2.Type.image )
			updateImgType( parameter , type );
		else if(parameter.type == DefaultParameter2.Type.scalar )
			updateScalarType( parameter , type );
		
		return;
	}
	
	
	@SuppressWarnings("unchecked")
	public <T extends RealType<T>> void updateImgType( DefaultParameter2 parameter , String type)
	{
		IterableInterval<T> input = null;
		if( parameter.value instanceof RandomAccessibleInterval )
		{
			input = Views.iterable(  (RandomAccessibleInterval<T>) parameter.value  ) ;
		}
		else if( parameter.value instanceof IterableInterval )
		{
			input = (IterableInterval<T>) parameter.value;
		}
		else
		{
			System.err.println("CIP: " + parameter.value.getClass().getSimpleName() + " is not convertible, Img, IterableInterval or RandomAccessibleInterval.");
		}
		
		switch( type ) {
		case "BitType":
			parameter.value = op.convert().bit( input );
			break;
			
		case "BoolType":
			parameter.value = op.convert().bit( input );
			break;			
			
		case "ByteType":
			parameter.value = op.convert().int8( input );
			break;			
			
		case "UnsignedByteType":
			parameter.value = op.convert().uint8( input );
			break;			
			
		case "ShortType":
			parameter.value = op.convert().int16( input );
			break;			
			
		case "UnsignedShortType":
			parameter.value = op.convert().uint16( input );
			break;			
			
		case "IntType":
			parameter.value = op.convert().int32( input );
			break;			
			
		case "UnsignedIntType":
			parameter.value = op.convert().uint32( input );
			break;			
			
		case "FloatType":
			parameter.value = op.convert().float32( input );
			break;			
			
		case "LongType":
			parameter.value = op.convert().int64( input );
			break;			
			
		case "UnsignedLongType":
			parameter.value = op.convert().uint64(input );
			break;			
			
		case "DoubleType":
			parameter.value = op.convert().float64( input );
			break;			
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <T extends RealType<T>> void updateScalarType( DefaultParameter2 parameter , String type)
	{
		T input = null;
		if( parameter.value instanceof RealType )
		{
			input = (T) parameter.value  ;
		}
		else
		{
			System.err.println("CIP: " + parameter.value.getClass().getSimpleName() + " is not convertible to RealType.");
		}
		
		
		switch( type ) {
		case "BitType":
			parameter.value = op.convert().bit( input );
			break;
			
		case "BoolType":
			parameter.value = op.convert().bit( input );
			break;			
			
		case "ByteType":
			parameter.value = op.convert().int8( input );
			break;			
			
		case "UnsignedByteType":
			parameter.value = op.convert().uint8( input );
			break;			
			
		case "ShortType":
			parameter.value = op.convert().int16( input );
			break;			
			
		case "UnsignedShortType":
			parameter.value = op.convert().uint16( input );
			break;			
			
		case "IntType":
			parameter.value = op.convert().int32( input );
			break;			
			
		case "UnsignedIntType":
			parameter.value = op.convert().uint32( input );
			break;			
			
		case "FloatType":
			parameter.value = op.convert().float32( input );
			break;			
			
		case "LongType":
			parameter.value = op.convert().int64( input );
			break;			
			
		case "UnsignedLongType":
			parameter.value = op.convert().uint64(input );
			break;			
			
		case "DoubleType":
			parameter.value = op.convert().float64( input );
			break;			
		}
		
		
	}
	
		
	
	public static String findCompatibleType( String type1 , String type2 )
	{
	
		// if the minor type is decimal and the major not convert to the next decimal type above the major type
		// if the minor type is signed and major not convert to the next signed type above the major type
		// else convert to the major type
		
		//////////////////////////////////////////////////////////////////////////////////////
		// Find minor and major PixelType of the two images
		//////////////////////////////////////////////////////////////////////////////////////
		
		int order1 = pixelTypeOrder.get(type1);
		int order2 = pixelTypeOrder.get(type2);
		
		if( order1 == order2 )
			return type1;
		
		
		String majorType = null;
		String minorType = null;
		if( order1>order2 )
		{
			majorType = type1;
			minorType = type2;
		}
		else
		{
			majorType = type2;
			minorType = type1;
		}
		
		// update the major type if needed
		if(		 isUnsignedPixelType.get( majorType )  &&  !isUnsignedPixelType.get( minorType )  )
		{
			// update major to the next signed type
			Iterator<Entry<String,Boolean>> iterator = isUnsignedPixelType.entrySet().iterator();
			while( iterator.hasNext() ) {
				if( iterator.next().getKey().equals(majorType) ) {
					break;
				}
			}
			while( iterator.hasNext() )
			{	
				String key = iterator.next().getKey();
				if(   ! isUnsignedPixelType.get( key )   )
				{
					majorType = key ;
				}
			}
		}
		else if( !isDecimalPixelType.get( majorType )  &&  isDecimalPixelType.get( minorType )  )
		{
			// update major to the next decimal type
			majorType = "DoubleType";
		}
		
		
		return majorType;
	}
  
  
  
	
	
	public <T extends RealType<T> > void toRAI_CIP(DefaultParameter2 parameter)
	{
		Object input = parameter.value;
		
		if (	input instanceof RAI_CIP)
		{ 
			// do nothing;
		}
		
		else if (	input instanceof RandomAccessibleInterval )
		{ 
			parameter.value = new RAI_CIP<T>( (RandomAccessibleInterval<T>) input );
		}
		
		else if (	input instanceof Dataset )
		{
			Dataset dataset = (Dataset) input;
			
			int nDim = dataset.numDimensions();
			double[] spacing = new double[nDim];
			List<String> axes = new ArrayList<String>();
			for(int d=0; d<nDim; d++){
				axes.add( dataset.axis(d).type().getLabel() );
				spacing[d] = dataset.axis(d).calibratedValue(1) - dataset.axis(d).calibratedValue(0); 
			}
			
			Img<T> img = (Img<T>) convertService.convert( dataset , Img.class );
			parameter.value = new RAI_CIP<T>( img , spacing , axes );
			
		}
		
		else if (	input instanceof ImagePlus )
		{
			ImagePlus imp = (ImagePlus) input;
			Img<T> img = (Img<T>) convertService.convert( imp , Img.class );
			
			int[] dims = imp.getDimensions();
			int currDim=-1;
			String[] impAxes = new String[] {"X","Y","C","Z","T"}; 
			double[] impSpacing = new double[5];
			impSpacing[0] = imp.getCalibration().pixelWidth;
			impSpacing[1] = imp.getCalibration().pixelHeight;
			impSpacing[2] = 1;
			impSpacing[3] = imp.getCalibration().pixelDepth;
			impSpacing[4] = imp.getCalibration().frameInterval;
			
			double[] spacing = new double[img.numDimensions()];
			List<String> axes = new ArrayList<String>();

			for( int d=0; d<5; d++ ) {
				int val = dims[d];
				if( val > 1 ) {
					currDim++;
					axes.add( impAxes[d]);
					spacing[currDim] = impSpacing[d];
				}
			}
			parameter.value = new RAI_CIP<T>( img , spacing , axes );
		
		}
		
		else {
			System.err.println("Unknown image type:" + input.getClass().getName() );
		}
		
		return;
	}
	
	
	
	public <T extends RealType<T> > Object toRAI_CIP( Object result, DefaultParameter2 parameter)
	{
		RAI_CIP<T> outputCIP = null;
		
		if (	result instanceof RandomAccessibleInterval )
		{
			RAI_CIP<?> input = (RAI_CIP<?>) parameter.value;
			RandomAccessibleInterval<T> output = (RandomAccessibleInterval<T>) result;
			outputCIP = new RAI_CIP<T>( output , input.spacing() , input.axes() );  
		}
		
		return outputCIP;
	}
	
}