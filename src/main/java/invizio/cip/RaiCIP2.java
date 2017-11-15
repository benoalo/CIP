package invizio.cip;

import java.util.List;


import ij.process.LUT;
import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;

public class RaiCIP2<T> extends MetadataCIP2 implements RandomAccessibleInterval<T> {

	RandomAccessibleInterval<T> rai;
	
	
	public RaiCIP2( RandomAccessibleInterval<T> rai)
	{
		super( rai.numDimensions() );
		this.rai = rai;		
	}
	
	
	public RaiCIP2( RandomAccessibleInterval<T> rai, List<Double> spacing, List<String> axesName, List<String> axesUnit)
	{
		super( spacing , axesName, axesUnit);
		this.rai = rai;
	}
		
	
	public RaiCIP2( RandomAccessibleInterval<T> rai, List<AxisCIP> metadata )
	{
		super( metadata );
		this.rai = rai;
	}
	
	
	@Override
	public RandomAccess<T> randomAccess() {
		return rai.randomAccess();
	}

	@Override
	public RandomAccess<T> randomAccess(Interval interval) {
		return rai.randomAccess(interval);
	}

	@Override
	public int numDimensions() {
		return rai.numDimensions();
	}

	@Override
	public long min(int d) {
		return rai.min(d);
	}

	@Override
	public void min(long[] min) {
		rai.min(min);
	}

	@Override
	public void min(Positionable min) {
		rai.min( min );
	}

	@Override
	public long max(int d) {
		return rai.max(d);
	}

	@Override
	public void max(long[] max) {
		rai.max(max);
	}

	@Override
	public void max(Positionable max) {
		rai.max(max);	
	}

	@Override
	public double realMin(int d) {
		return rai.realMin(d);
	}

	@Override
	public void realMin(double[] min) {
		rai.realMin(min);
	}

	@Override
	public void realMin(RealPositionable min) {
		rai.realMin(min);
	}

	@Override
	public double realMax(int d) {
		return 	rai.realMax(d);
	}

	@Override
	public void realMax(double[] max) {
		rai.realMax(max);
	}

	@Override
	public void realMax(RealPositionable max) {
		rai.realMax(max);
	}

	@Override
	public void dimensions(long[] dimensions) {
		rai.dimensions( dimensions );
	}

	@Override
	public long dimension(int d) {
		return rai.dimension( d );
	}

}
