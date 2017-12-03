package invizio.cip.measure;


import java.util.ArrayList;

import net.imglib2.type.numeric.RealType;



public abstract class AbstractMeasureToolset<M, T extends RealType<T>> extends ArrayList<AbstractMeasureTool<M,T>>{
	
	private static final long serialVersionUID = 1L;
	
	
	public AbstractMeasureTool<M,T> get( String toolName )
	{
		for( AbstractMeasureTool<M,T> tool : this )
			if( tool.name.equals(toolName) )
				return tool;
		
		return null;
	}
	
	
}
