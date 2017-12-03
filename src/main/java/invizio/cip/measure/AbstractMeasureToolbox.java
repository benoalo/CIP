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



public abstract class AbstractMeasureToolbox { //<T extends RealType<T>>  {

	// 
	Map<String, AbstractMeasureTool<Object, ? extends RealType<?>> > tools;
	LinkedHashMap<String, Measure > results;
	
	// measurable instance
	Iterable<? extends RealType<?>> iterable;
	Interval interval;
	RaiCIP2<? extends RealType<?>> raiCIP;
	IterableRegion<? extends BooleanType<?>> region;
	
	
	
	public enum MeasurableType {
		ITERABLE,
		INTERVAL,
		RAICIP,
		REGION;	
	}

	
	
	public AbstractMeasureToolbox()
	{
		tools = new HashMap<String, AbstractMeasureTool<Object,? extends RealType<?>> >() ;
	}
	
	
	
	public Measure measure( String toolName )
	{
		Measure result = null;
		toolName = toolName.toLowerCase();
		if( tools.containsKey(toolName) ) {
			
			AbstractMeasureTool<Object,? extends RealType<?>> tool = tools.get(toolName);
			
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
			//if result
			//results.put(toolName, result);
		}
		
		return result;
	}

//	public Map<String,Measure> results()
//	{
//		return results;
//	}

//	public Map<String,Measure> results(String prefix)
//	{
//		Map<String,Measure> results2 = new LinkedHashMap<String,Measure>();
//		for(Entry<String,Measure> e : results.entrySet() )
//			results2.put( prefix + e.getKey() , e.getValue() );
//			
//		return results2;
//	}

	
	public void add( AbstractMeasureToolset<?,? extends RealType<?>> toolset )
	{
		for(AbstractMeasureTool<?,? extends RealType<?>> tool : toolset )
		{
			tools.put( tool.name , (AbstractMeasureTool<Object, ? extends RealType<?>>) tool );
		}
	}
	
	// setMeasurable will be needed for specific implementation

	
	

	
}
