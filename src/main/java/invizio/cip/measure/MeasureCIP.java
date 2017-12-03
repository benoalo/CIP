package invizio.cip.measure;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import invizio.cip.CIPService;
import invizio.cip.RaiCIP2;
import invizio.cip.parameters.DefaultParameter2.Type;
import net.imagej.ImageJService;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;



/**
 * 
 * @author Benoit Lombardot
 *
 **/


@Plugin(type = Service.class)
public class MeasureCIP  extends AbstractService implements ImageJService {
	
	@Parameter
	private OpService op;
	
	@Parameter
	private CIPService cipService;
	
	
	public <T extends RealType<T>> Map<String,List<Object>> imageMeasures(RaiCIP2<T> raiCIP , List<String> measureNames, Boolean  useUnit, String prefix)
	{

		// parse input parameters
		if ( useUnit == null)
			useUnit=true;
		if (prefix == null)
			prefix = "";
		
		
		LinkedHashMap<String,List<Object>> measures = new LinkedHashMap<String, List<Object>>();
		
		// instantiate a measure Toolbox
		RaiCIPMeasureToolbox<T> measureToolbox = new RaiCIPMeasureToolbox<T>( op , useUnit );
		measureToolbox.setMeasurable( raiCIP );
		for( String measureName : measureNames) {
			Measure measure = measureToolbox.measure( measureName );
			initMeasures(prefix, measureName, measure, measures);
			addToMeasures(prefix, measureName, measure, measures);
		}
		
		
		return measures;
	}


	
	@SuppressWarnings("unchecked")
	public <B extends BooleanType<B> , T extends RealType<T>> Map<String,List<Object>>
					regionMeasures( List<IterableRegion<B>> regions, List<String> measureNames, RandomAccessibleInterval<T> source, String prefix)
	{
		boolean needInit = true;
		boolean updateSource = false;
		// parse input parameters
		if (prefix == null)
			prefix = "";
		if( source == null) 
			updateSource=true;
		
		LinkedHashMap<String,List<Object>> measures = new LinkedHashMap<String, List<Object>>();
		
		// instantiate a measure Toolbox
		IterableRegionMeasureToolbox<T,B> measureToolbox = new IterableRegionMeasureToolbox<T,B>( op );
		for( IterableRegion<B> region : regions )
		{
			//long[] min = new long[region.numDimensions()];
			//region.min(min);
			//System.out.println("(x,y) = (" + min[0] +","+ min[1]+")" ) ;
			
			if( updateSource ) 
				source = (RandomAccessibleInterval<T>) region;
			
			measureToolbox.setMeasurable( region, source );
			
			if ( needInit )
			{
				for( String measureName : measureNames)
				{
					final Measure measure = measureToolbox.measure( measureName );
					initMeasures(prefix, measureName, measure, measures);
					addToMeasures(prefix, measureName, measure, measures);
				}
				needInit = false;
			}
			else
			{
				for( String measureName : measureNames)
				{
					final Measure measure = measureToolbox.measure( measureName );
					addToMeasures(prefix, measureName, measure, measures);
				}
			}
		}	
		
		return measures;
	}

	
	
	public void initMeasures(String prefix, String measureName, Measure measure, LinkedHashMap<String,List<Object>> measures )
	{
		if(  measure==null  ||  measure.type==Type.scalar  ||  measure.type==Type.string  )
		{
			measures.put(prefix + measureName, new ArrayList<Object>() );
		}
		else {
			@SuppressWarnings("unchecked")
			final Map<String,Object> values = (Map<String,Object>) measure.value;
			for( String key : values.keySet() )
				measures.put( prefix + measureName +"_"+key, new ArrayList<Object>() );
		}
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	public void addToMeasures(String prefix, String measureName, Measure measure, LinkedHashMap<String,List<Object>> measures )
	{
		
		if( measure == null ) 
		{
			measures.get( prefix+measureName ).add( "Not defined :-\\" );
		}
		else
		{
			switch( measure.type )
			{
				case string:
					measures.get( prefix+measureName ).add( measure.value );
					break;
				
				case scalar:
					measures.get( prefix+measureName ).add( measure.value );
					break;
				
				case strings :
					final Map<String,Object> values = (Map<String,Object>) measure.value;
					for(Entry<String,Object> e : values.entrySet() )
						measures.get( prefix+measureName+"_"+e.getKey() ).add( e.getValue() );
					break;
				
				case scalars:
					final Map<String,Object> values2 = (Map<String,Object>) measure.value;
					for(Entry<String,Object> e : values2.entrySet() )
						measures.get( prefix+measureName+"_"+e.getKey() ).add( e.getValue() );
					break;
				
				default:
					break;
			}
		}
	}
	
	
	
	
}



