package invizio.cip.measure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import invizio.cip.RaiCIP2;
import invizio.cip.parameters.DefaultParameter2;
import net.imagej.ops.OpService;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;




public class RaiCIPMeasureToolset<T extends RealType<T>> extends AbstractMeasureToolset<RaiCIP2<T>,T>{

	private static final long serialVersionUID = 1L;
	
	OpService op;
	private boolean useImageUnit;

	
	public RaiCIPMeasureToolset( OpService op , boolean useImageUnit )
	{
		this.useImageUnit = useImageUnit;
		this.op = op;
		
		add( new PositionMeasureTool<RaiCIP2<T>,T>() );
		add( new BoundarySizeMeasureTool<RaiCIP2<T>,T>( this ) ); // the toolbox is passed so the measure can reuse
		add( new SizeMeasureTool<RaiCIP2<T>,T>() );
	}
		
	
	
	public class PositionMeasureTool<N extends RaiCIP2<U>, U extends RealType<U>> extends AbstractMeasureTool<N,U> {

		PositionMeasureTool( ){	
			name = "position";
			inputType = AbstractMeasureToolbox.MeasurableType.RAICIP;
			outputType = DefaultParameter2.Type.scalars;
		}
		
		@Override
		public Measure measure(N measurable) {
			
			int nDim = measurable.numDimensions();
			List<String> axes = measurable.axes();
			Map<String,Double> result = new HashMap<String,Double>();
			for( int d=0; d<nDim; d++ ) {
				double pos = (double)( measurable.min(d) + measurable.max(d) ) / 2 ;
				result.put( axes.get(d) ,pos );
			}
			return new Measure( name , outputType, result);			
		}
	}
	
	
	
	public class BoundarySizeMeasureTool<N extends RaiCIP2<U>, U extends RealType<U>> extends AbstractMeasureTool<N,U> {

		RaiCIPMeasureToolset<U> toolbox;
		
		BoundarySizeMeasureTool( RaiCIPMeasureToolset<U> toolbox ){	
			name = "position";
			inputType = AbstractMeasureToolbox.MeasurableType.RAICIP;
			outputType = DefaultParameter2.Type.scalar;
			this.toolbox = toolbox;
		}
		
		@Override
		public Measure measure(N measurable) {
			
			final int nDim = measurable.numDimensions();
			final List<Double> spacing = measurable.spacing();
			
			Double size = (Double) toolbox.get("size").measure(measurable).value;
			
			double value = 0;
			if( useImageUnit ) {
				for( int d=0; d<nDim; d++)
					value += 2*size / ( measurable.dimension(d) * spacing.get(d) );				
			}
			else {
				for( int d=0; d<nDim; d++)
					value += 2*size/measurable.dimension(d);
			}
			return new Measure( name , outputType, value);
		}
	}

	
	
	public class SizeMeasureTool<N extends RaiCIP2<U>, U extends RealType<U>> extends AbstractMeasureTool<N,U> {

		SizeMeasureTool( ){	
			name = "size";
			inputType = AbstractMeasureToolbox.MeasurableType.RAICIP;
			outputType = DefaultParameter2.Type.scalar;
		}
		
		@Override
		public Measure measure(N measurable) {
			
			final int nDim = measurable.numDimensions();
			final List<Double> spacing = measurable.spacing();
			
			Double value = 1d;
			for( int d=0; d<nDim; d++)
				value *= measurable.dimension(d);
				
			if( useImageUnit ) {
				for( int d=0; d<nDim; d++)
					value *= spacing.get(d) ;				
			}
			return new Measure( name , outputType, value);			
		}
	}

	
	
	
}
