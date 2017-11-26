package invizio.cip.measure;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import invizio.cip.RaiCIP2;
import net.imglib2.Interval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;


/**
 * 
 * @author Benoit Lombardot
 *
 */



public abstract class AbstractMeasureToolbox<T extends RealType<T>>  {

	// 
	Map<String, AbstractMeasureTool<Object,T>> tools;
	LinkedHashMap<String, Measure > results;
	
	// measurable instance
	Iterable<T> iterable;
	Interval interval;
	RaiCIP2<T> raiCIP;
	IterableRegion<? extends BooleanType<?>> region;
	
	
	
	public enum MeasurableType {
		ITERABLE,
		INTERVAL,
		RAICIP,
		REGION;	
	}

	
	
	public AbstractMeasureToolbox()
	{
		tools = new HashMap<String, AbstractMeasureTool<Object,T> >() ;
	}
	
	
	
	public void measure( String toolName )
	{
		Measure result = null;
		toolName = toolName.toLowerCase();
		if( tools.containsKey(toolName) ) {
			
			AbstractMeasureTool<Object,T> tool = tools.get(toolName);
			
			switch( tool.inputType )
			{
			case ITERABLE:
				result =  tool.measure( iterable );
				break;
				
			case INTERVAL:
				result = tool.measure( interval );
				break;
				
			case RAICIP :
				result = tool.measure( raiCIP );
				break;
				
			case REGION :
				result = tool.measure( region );
				break;
				
			default :
				result = null;
				break;
			}
			results.put(toolName, result);
		}
			
	}

	public Map<String,Measure> results()
	{
		return results;
	}

	public Map<String,Measure> results(String prefix)
	{
		Map<String,Measure> results2 = new LinkedHashMap<String,Measure>();
		for(Entry<String,Measure> e : results.entrySet() )
			results2.put( prefix + e.getKey() , e.getValue() );
			
		return results2;
	}

	
	public void add( AbstractMeasureToolset<?,T> toolset )
	{
		for(AbstractMeasureTool<?,T> tool : toolset )
		{
			tools.put( tool.name , (AbstractMeasureTool<Object, T>) tool );
		}
	}
	
	// setMeasurable will be needed for specific implementation

	
	

	
}
