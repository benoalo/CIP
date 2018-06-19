package invizio.cip.measure;



import invizio.cip.parameters.DefaultParameter2;
import net.imagej.ops.OpService;
import net.imglib2.type.numeric.RealType;





public class IterableMeasureToolset<T extends RealType<T>> extends AbstractMeasureToolset<Iterable<T>,T> {

	private static final long serialVersionUID = 1L;
	
	OpService op;
	
	public IterableMeasureToolset( OpService op)
	{
		this.op = op;
		
		add( new MinMeasureTool<Iterable<T>,T>() 		);
		add( new MaxMeasureTool<Iterable<T>,T>() 		);
		add( new MeanMeasureTool<Iterable<T>,T>() 	);
		add( new StddevMeasureTool<Iterable<T>,T>() 	);
		add( new MedianMeasureTool<Iterable<T>,T>() 	);
	}

	
	public class MinMeasureTool<N extends Iterable<U>, U extends RealType<U>> extends AbstractMeasureTool<N,U> {
		
		MinMeasureTool( ){	
			name = "min";
			outputType = DefaultParameter2.Type.scalar;
			inputType = AbstractMeasureToolbox.MeasurableType.ITERABLE;
		}
		
		
		@Override
		public Measure measure(N measurable) {
			
			Double value = op.stats().min( measurable ).getRealDouble();
			return new Measure( name , outputType, value);
		}
	}
	
	
	public class MaxMeasureTool<N extends Iterable<U>, U extends RealType<U>> extends AbstractMeasureTool<N,U> {
		
		
		MaxMeasureTool(){	
			name = "max";
			outputType = DefaultParameter2.Type.scalar;
			inputType = AbstractMeasureToolbox.MeasurableType.ITERABLE;
		}
		
		@Override
		public Measure measure(N measurable) {
			Double value = op.stats().max( measurable ).getRealDouble();
			return new Measure( name , outputType, value);
		}		
	}

	
	public class MeanMeasureTool<N extends Iterable<U>, U extends RealType<U>> extends AbstractMeasureTool<N,U> {

		MeanMeasureTool() {	
			name = "mean";
			outputType = DefaultParameter2.Type.scalar;
			inputType = AbstractMeasureToolbox.MeasurableType.ITERABLE;
		}
		
		@Override
		public Measure measure(N measurable) {
			Double value = op.stats().mean( measurable ).getRealDouble();
			return new Measure( name , outputType, value);
		}		
	}

	
	
	public class StddevMeasureTool<N extends Iterable<U>, U extends RealType<U>> extends AbstractMeasureTool<N,U> {

		StddevMeasureTool() {	
			name = "stddev";
			outputType = DefaultParameter2.Type.scalar;
			inputType = AbstractMeasureToolbox.MeasurableType.ITERABLE;
		}
		
		@Override
		public Measure measure(N measurable) {
			Double value = op.stats().stdDev( measurable ).getRealDouble();
			return new Measure( name , outputType, value);
		}		
	}

	
	
	public class MedianMeasureTool<N extends Iterable<U>, U extends RealType<U>> extends AbstractMeasureTool<N,U> {

		MedianMeasureTool() {	
			name = "median";
			outputType = DefaultParameter2.Type.scalar;
			inputType = AbstractMeasureToolbox.MeasurableType.ITERABLE;
		}
		
		@Override
		public Measure measure(N measurable) {
			Double value = op.stats().median( measurable ).getRealDouble();
			return new Measure( name , outputType, value);
		}		
	}





		
}
