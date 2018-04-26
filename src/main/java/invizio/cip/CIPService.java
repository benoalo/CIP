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
import ij.measure.Calibration;

import invizio.cip.parameters.Checks;
import invizio.cip.parameters.DefaultParameter2;

import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.DefaultAxisType;
import net.imagej.ops.OpService;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.util.Util;
 


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
	
	
	public void adaptTypeForUnaryMath( DefaultParameter2 parameter, String optype )
	{
		toImglib2Object( parameter );
		String type = getImgLib2ScalarType( parameter.value );
		
		if(optype.equals("log") || optype.equals("exp") || optype.equals("sqrt") || 
				optype.equals("sin") || optype.equals("cos") || optype.equals("tan") || 
				optype.equals("asin") || optype.equals("acos") || optype.equals("atan") ) 
		{
			String newType = null;
			if( type.equals("DoubleType") )
			{
				newType = "DoubleType";
			}
			else if( type.equals("LongType") || type.equals("UnsignedLongType") )
			{
				newType = "DoubleType";
			}
			else {
				newType = "FloatType";
			}
			if( !type.equals(newType))
				updateImglib2Type( parameter , newType );
		}
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
		{
			//toImglib2Image( parameter );
			toRaiCIP( parameter );
		}
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
	private <T extends RealType<T>, U extends RealType<U>> void updateImgType( DefaultParameter2 parameter , String type)
	{
		RaiCIP2<T> input0 = (RaiCIP2<T>) parameter.value;
		
		IterableInterval<T> input = null;
		//if( parameter.value instanceof RandomAccessibleInterval )
		//{
		input = Views.iterable(  input0  ) ;
		//}
		//else if( parameter.value instanceof IterableInterval )
		//{
		//	input = (IterableInterval<T>) parameter.value;
		//}
		//else
		//{
		//	System.err.println("CIP: " + parameter.value.getClass().getSimpleName() + " is not convertible, use Img, IterableInterval or RandomAccessibleInterval.");
		//}
		
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
		
		RandomAccessibleInterval<U> rai = (RandomAccessibleInterval<U>) parameter.value;
		parameter.value = new RaiCIP2<U>( rai, input0.spacing(), input0.axes(), input0.unit());
		
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
  
	
	public void toRaiCIP(DefaultParameter2 parameter)
	{
		parameter.value = toRaiCIP( parameter.value ); 
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <T extends RealType<T> > RaiCIP2<T> toRaiCIP(Object input)
	{
		
		String name = "";
		RandomAccessibleInterval<T> rai = null;
		if (	input instanceof RaiCIP2)
		{ 
			return (RaiCIP2<T>)input;
		}
		else if (	input instanceof RandomAccessibleInterval )
		{ 
			rai = (RandomAccessibleInterval<T>) input;
		}
		else if (	input instanceof Dataset )
		{
			Dataset dataset = (Dataset) input;
			rai = (Img<T>) convertService.convert( dataset , Img.class );
			name = dataset.getName();
		}
		else if (	input instanceof ImgPlus )
		{
			ImgPlus<T> imgPlus = (ImgPlus<T>) input;
			rai = (Img<T>) convertService.convert( imgPlus , Img.class );
			name = imgPlus.getName();
		}
		else if (	input instanceof ImagePlus )
		{
			ImagePlus imp = (ImagePlus) input;
			Dataset dataset = (Dataset) convertService.convert( imp , Dataset.class );
			rai = (Img<T>) dataset.getImgPlus().getImg();
			name = imp.getTitle();
		}
		else
		{
			//System.err.println("Unknown image type:" + input.getClass().getName() );
			return null;
		}
		
		RaiCIP2<T> output = new RaiCIP2<T>( rai , spacing(input) , axes(input), unit(input));
		output.name = name;
		
		return output;
	}

	
	@SuppressWarnings("unchecked")
	public <T extends RealType<T> > Object toRaiCIP( Object result, DefaultParameter2 parameter)
	{
		RaiCIP2<T> outputCIP = null;
		
		if (	result instanceof RandomAccessibleInterval )
		{
			RaiCIP2<?> input = (RaiCIP2<?>) parameter.value;
			RandomAccessibleInterval<T> output = (RandomAccessibleInterval<T>) result;
			outputCIP = new RaiCIP2<T>( output , input.spacing() , input.axes(), input.unit() );  
		}
		
		return outputCIP;
	}
	
	
	public <T extends RealType<T> > List<RaiCIP2<T>> toRaiCIPs(Object inputs)
	{
		List<RaiCIP2<T>> output_list = new ArrayList<RaiCIP2<T>>(); 
		@SuppressWarnings("unchecked")
		List<Object> input_list = (List<Object>) inputs;
		for(Object image : input_list )
			output_list.add( toRaiCIP(image) );
		
		return output_list;
	}
	
	
	
	public List<Double> spacing( Object input ) {
		
		List<Double> spacing = null;
		
		if (	input instanceof RaiCIP2)
		{ 
			spacing = ((RaiCIP2<?>) input).spacing();
		}
		else if (	input instanceof RandomAccessibleInterval )
		{ 
			RandomAccessibleInterval<?> rai = (RandomAccessibleInterval<?>) input;
			int nDim = rai.numDimensions();
			spacing = new ArrayList<Double>();
			for(int d=0; d<nDim; d++) {
				spacing.add( 1d );
			}
		}
		else if (	input instanceof Dataset )
		{
			Dataset dataset = (Dataset) input;
			int nDim = dataset.numDimensions();
			spacing = new ArrayList<Double>();
			for(int d=0; d<nDim; d++){
				spacing.add( dataset.axis(d).calibratedValue(1) - dataset.axis(d).calibratedValue(0) ); 
			}
		}
		else if (	input instanceof ImgPlus )
		{
			ImgPlus<?> imgPlus = (ImgPlus<?>) input;
			int nDim = imgPlus.numDimensions();
			spacing = new ArrayList<Double>();
			for(int d=0; d<nDim; d++){
				spacing.add( imgPlus.axis(d).calibratedValue(1) - imgPlus.axis(d).calibratedValue(0) ); 
			}
		}
		else if (	input instanceof ImagePlus )
		{
			ImagePlus imp = (ImagePlus) input;
			int[] dims = imp.getDimensions();
			
			spacing = new ArrayList<Double>();

			double[] impSpacing = new double[5];
			impSpacing[0] = imp.getCalibration().pixelWidth;
			impSpacing[1] = imp.getCalibration().pixelHeight;
			impSpacing[2] = 1;
			impSpacing[3] = imp.getCalibration().pixelDepth;
			impSpacing[4] = imp.getCalibration().frameInterval;
			
			for( int d=0; d<5; d++ ) {
				if( dims[d] > 1 ) {
					spacing.add( impSpacing[d] );
				}
			}
		}
		else {
			System.err.println("Unknown image type:" + input.getClass().getName() );
		}
		
		return spacing;
	}
	
	
	
	public List<String> unit( Object input ) {
		List<String> axesUnit = new ArrayList<String>();
		
		if (	input instanceof RaiCIP2)
		{ 
			axesUnit =  ((RaiCIP2<?>) input).unit();
		}
		else if (	input instanceof RandomAccessibleInterval )
		{ 
			RandomAccessibleInterval<?> rai = (RandomAccessibleInterval<?>) input;
			int nDim = rai.numDimensions();
			for(int d=0; d<nDim; d++) {
				axesUnit.add( "arbitrary" );
			}
		}
		else if (	input instanceof Dataset )
		{
			Dataset dataset = (Dataset) input;
			int nDim = dataset.numDimensions();
			for(int d=0; d<nDim; d++){
				axesUnit.add( dataset.axis(d).unit() );
			}
		}
		else if (	input instanceof ImgPlus )
		{
			ImgPlus<?> imgPlus = (ImgPlus<?>) input;
			int nDim = imgPlus.numDimensions();
			for(int d=0; d<nDim; d++){
				axesUnit.add( imgPlus.axis(d).unit() );
			}
		}
		else if (	input instanceof ImagePlus )
		{
			ImagePlus imp = (ImagePlus) input;

			int[] dims = imp.getDimensions();

			List<String> impUnit = new ArrayList<String>();
			Calibration cal = imp.getCalibration();
			impUnit.add( cal.getXUnit().equals("pixel")? "" : cal.getXUnit() );
			impUnit.add( cal.getYUnit().equals("pixel")? "" : cal.getYUnit() );
			impUnit.add( "" ); // channel
			impUnit.add( cal.getZUnit().equals("pixel")? "" : cal.getZUnit() );
			impUnit.add( cal.getTimeUnit() );
			
			for( int d=0; d<5; d++ )
				if( dims[d] > 1 )
					axesUnit.add( impUnit.get(d) );
		}
		else
		{
			System.err.println("Unknown image type:" + input.getClass().getName() );
		}
		
		return axesUnit;
	}
	
	
	public List<String> axes( Object input )
	{
		List<String> axesName = new ArrayList<String>();
		
		if (	input instanceof RaiCIP2)
		{ 
			axesName =  ((RaiCIP2<?>) input).axes();
		}
		else if (	input instanceof RandomAccessibleInterval )
		{ 
			RandomAccessibleInterval<?> rai = (RandomAccessibleInterval<?>) input;
			int nDim = rai.numDimensions();
			String[] defaultNames = new String[] {"X", "Y", "Z"};
			for(int d=0; d<nDim; d++) {
				if (d < 3)
					axesName.add( defaultNames[d] );
				else
					axesName.add( "D"+d );
				;
			}
		}
		else if (	input instanceof Dataset )
		{
			Dataset dataset = (Dataset) input;
			int nDim = dataset.numDimensions();
			for(int d=0; d<nDim; d++){
				axesName.add( dataset.axis(d).type().getLabel() );
			}
		}
		else if (	input instanceof ImgPlus )
		{
			ImgPlus<?> imgPlus = (ImgPlus<?>) input;
			int nDim = imgPlus.numDimensions();
			for(int d=0; d<nDim; d++){
				axesName.add( imgPlus.axis(d).type().getLabel() );
			}
		}
		else if (	input instanceof ImagePlus )
		{
			ImagePlus imp = (ImagePlus) input;

			int[] dims = imp.getDimensions();

			String[] impAxes = new String[] {"X","Y","C","Z","T"}; 
			
			for( int d=0; d<5; d++ )
				if( dims[d] > 1 )
					axesName.add( impAxes[d] );
		}
		else
		{
			System.err.println("Unknown image type:" + input.getClass().getName() );
		}
		
		return axesName;
	}

	/*
	public List<LUT> lut(Object input)
	{
		List<LUT> luts = new ArrayList<LUT>();
			
		if (	input instanceof RaiCIP)
		{ 
			luts = ((RaiCIP<?>) input).lut();
		}
		else if (	input instanceof RandomAccessibleInterval )
		{ 
			luts.add( null ); 
		}
		else if (	input instanceof Dataset )
		{
			Dataset dataset = (Dataset) input;
			for( int ch=0; ch<dataset.getChannels(); ch++ ) {
				luts.add( null );
			}
		}
		else if (	input instanceof ImagePlus )
		{
			ImagePlus imp = (ImagePlus) input;
			int nCh = imp.getNChannels();
			for( int ch=0; ch<nCh; ch++) {
				luts.add( imp.getLuts()[ch] );
			}
		}
		else
		{
			System.err.println("Unknown image type:" + input.getClass().getName() );
		}
		
		return luts;
	}
	*/
	
	
	public Object setMetadata( Object outputImage )
	{	
		return setMetadata(outputImage, null, "" );
	}
	
	
	public Object setMetadata( Object outputImage, DefaultParameter2 inputImage, String str)
	{
		if( outputImage == null ) {
			return null;
		}
		else if ( outputImage instanceof RandomAccessibleInterval )
		{
			if( inputImage==null )
				outputImage = toRaiCIP( outputImage );
			else {
				outputImage = toRaiCIP( outputImage, inputImage ); // assumes InputImage.value is RaiCIP2
				if( outputImage instanceof RaiCIP2)
					((RaiCIP2<?>) outputImage).name = str + ((RaiCIP2<?>)inputImage.value).name ;
			}
			
		}
		return outputImage;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public Object discardNullValue( List<Object> resultsTemp )
	{
		Object results = new ArrayList<Object>();
		int count = 0;
		for(Object obj : resultsTemp ) {
			if ( obj != null ) {
				count++;
				( (List<Object>) results ).add( obj );
			}
		}
		if( count==1 )
			results = ((List<Object>)results).get(0) ;
		if ( count == 0 )
			results = null;
		
		return results;
	}

	
	@SuppressWarnings("unchecked")
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> toImgPlus(Object image )
	{
		ImgPlus<T> imgPlus = null;
		
		if (	image instanceof RaiCIP2)
		{ 
			imgPlus = toImgPlus( (RaiCIP2<T>) image );
		}
		else if (	image instanceof RandomAccessibleInterval )
		{ 
			RandomAccessibleInterval<T> rai = (RandomAccessibleInterval<T>) image;
			Img<T> img = ImgView.wrap(  rai, Util.getArrayOrCellImgFactory( rai , rai.randomAccess().get() ) );
			imgPlus = new ImgPlus<T>( img );
		}
		else if (	image instanceof Dataset )
		{
			Dataset dataset = (Dataset) image;
			imgPlus = (ImgPlus<T>) dataset.getImgPlus();
		}
		else if (	image instanceof ImgPlus )
		{
			imgPlus = (ImgPlus<T>) image;
		}
		else if (	image instanceof ImagePlus )
		{
			ImagePlus imp = (ImagePlus) image;
			Dataset dataset = (Dataset) convertService.convert( imp , Dataset.class );
			imgPlus = (ImgPlus<T>) dataset.getImgPlus();
		}
		else
		{
			System.err.println("Unknown image type:" + image.getClass().getName() );
		}

		return imgPlus;
	}
	
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> toImgPlus(RaiCIP2<T> raiCIP )
	{		
		Img<T> img = ImgView.wrap( raiCIP , Util.getArrayOrCellImgFactory( raiCIP , raiCIP.randomAccess().get() ) );
		
		int nDim = raiCIP.numDimensions();
		AxisType[] axesType = new AxisType[nDim];
		double[] spacing = new double[nDim];
		for(int d=0; d<nDim; d++)
		{
			spacing[d] = raiCIP.spacing(d);
			int nSpacDim = 0;
			if( raiCIP.axes(d).toUpperCase().equals("X") ) {
				axesType[d] = Axes.X;
				nSpacDim++;
			}
			else if( raiCIP.axes(d).toUpperCase().equals("Y") ) {
				axesType[d] = Axes.Y;
				nSpacDim++;
			}
			else if( raiCIP.axes(d).toUpperCase().equals("Z") ) {
				axesType[d] = Axes.Z;
				nSpacDim++;
			}
			else if( raiCIP.axes(d).toUpperCase().equals("T") ) {
				axesType[d] = Axes.TIME;
			}
			else if( raiCIP.axes(d).toUpperCase().equals("C") ) {
				axesType[d] = Axes.CHANNEL;
			}
			else {
				if ( nSpacDim < 3 ) {
					axesType[d] = new DefaultAxisType( raiCIP.axes(d) , true );
					nSpacDim++;
				}
				else
					axesType[d] = new DefaultAxisType( raiCIP.axes(d) , false );
				
			}	
		}
		
		String[] unit = raiCIP.unit().toArray(new String[1]);
		String name = new String(raiCIP.name);
		
		ImgPlus<T> imgPlus = new ImgPlus<T>(img , name, axesType , spacing, unit );
		
		return imgPlus;
	}
	
	
	public <T extends RealType<T> & NativeType<T>> ImagePlus toImagegPlus( Object image )
	{
		
		// convert everything to ImgPlus
		ImgPlus<T> imgPlus = toImgPlus( image );
		
		// wrap in dataset 
		Dataset dataset = new DefaultDataset( this.context(), imgPlus ); 
		
		// convert to IJ1 ImagePlus
		ImagePlus imagePlus = (ImagePlus) convertService.convert( dataset , ImagePlus.class );
		
		return imagePlus;
	}


	public Double scalar( Object value)
	{
		//Double d = (double) value;
		Double d = null;
		
		if( 	value instanceof Byte		)
			d = (double)(byte) value;
		
		else if( 	value instanceof Short		)
			d = (double)(short) value;
		
		else if( 	value instanceof Integer	)
			d = (double)(int) value;
		
		else if( 	value instanceof Float		)
			d = (double)(float) value;
		
		else if( 	value instanceof Double		)
			d = (double) value;		
		return d;
	}	
	
	
	// valid only if value was check with Checks.isScalars(value) , this is usually done
	//	in the parseinput function of FunctionParameters2
	@SuppressWarnings("unchecked")
	public List<Double> scalars( Object value )
	{
		List<Double> list2 = new ArrayList<Double>();
		
		if ( Checks.isScalar(value) ) {
			list2.add( (double) value );
		}
		else if ( value instanceof List ) {
			List<?> list = (List<?>) value;
			int n = list.size();
			Object item = null;
			if( n>0 )
				item = list.get(0);
			if( item!=null ) {
				
				if( 	value instanceof Byte		) {
					for( int i=0; i<n ; i++) {
						list2.add( (double) (byte) list.get(i) );
					}
				}
				
				else if( 	value instanceof Short	) {
					for( int i=0; i<n ; i++) {
						list2.add( (double) (short) list.get(i) );
					}
				}
				
				else if( 	value instanceof Integer	) {
					for( int i=0; i<n ; i++) {
						list2.add( (double) (int) list.get(i) );
					}
				}
				
				else if( 	value instanceof Float	) {
					for( int i=0; i<n ; i++) {
						list2.add( (double) (float) list.get(i) );
					}
				}
				
				else if( 	value instanceof Double	) {
					list2 = (List<Double>) list;
				}

			}
		}
		else {
			return null;
		}
		return list2;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<String> strings(Object value )
	{
		List<String> strings = null;
		
		if( value instanceof String ) {
			strings = new ArrayList<String>();
			strings.add( (String) value );
		}
		else if( 	value instanceof List  		&&
					((List<?>) value).size()>0	&&
					((List<?>) value).get(0) instanceof String ) {
			strings = (List<String>) value; 
		}
		
		return strings;
	}
	
}