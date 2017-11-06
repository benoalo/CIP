package invizio.cip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;

public class RAI_CIP<T> implements RandomAccessibleInterval<T> {

	RandomAccessibleInterval<T> rai;
	double[] spacing;
	List<String> axes;
	Map<String, Integer> axesDim;
	
	//String[] defaultAxesName = new String[] {"D0","D1","D2","D3","D4","D5","D6","D7","D8","D9"};
	
	RAI_CIP( RandomAccessibleInterval<T> rai)
	{
		this.rai = rai;
		
		int nDim = rai.numDimensions();
		this.spacing = new double[nDim];
		this.axes = new ArrayList<String>();
		axesDim = new HashMap<String,Integer>();
		
		for(int d=0; d<nDim; d++ )
		{
			String axisName = "D"+d;
			spacing[d] = 1;
			axes.add(axisName);
			axesDim.put(axisName, d);
		}
			
	}
	
	
	RAI_CIP( RandomAccessibleInterval<T> rai, double[] spacing, List<String> axes )
	{
		this.rai = rai;
		this.spacing = spacing;
		this.axes = axes;
		
		axesDim = new HashMap<String,Integer>();
		int count = 0;
		for(String name : axes ) {
			axesDim.put(name, count);
			count++;
		}
			
	}
	
	public int axesDim(String name) {
		return axesDim.get(name);
	}
	
	public String axes(int d) {
		return axes.get(d);
	}

	public double spacing(int d) {
		return spacing[d];
	}

	public double spacing(String axisName) {
		return spacing[axesDim.get(axisName)];
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
