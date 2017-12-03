package invizio.cip.measure;


import invizio.cip.parameters.DefaultParameter2;
import net.imglib2.type.numeric.RealType;


/**
 * 
 * @author Benoit Lombardot
 *
 */

public abstract class AbstractMeasureTool<M, T extends RealType<T>>
{
	public String name;
	public AbstractMeasureToolbox.MeasurableType inputType;
	public DefaultParameter2.Type outputType;
	
	public abstract Measure measure( M measurable );
	
}
