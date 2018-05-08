package invizio.cip;

import java.util.ArrayList;
import java.util.List;


import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;
import net.imglib2.view.Views;

public class RaiCIP2<T> implements RandomAccessibleInterval<T> , Metadata {

	RandomAccessibleInterval<T> rai;
	MetadataCIP2 metadata;
	public String name;
	
	public RaiCIP2( RandomAccessibleInterval<T> rai)
	{
		metadata = new MetadataCIP2( rai.numDimensions() );
		this.rai = rai;		
		this.name = metadata.name;
	}
	
	
	public RaiCIP2( RandomAccessibleInterval<T> rai, List<Double> spacing, List<String> axesName, List<String> axesUnit, List<Long> origin)
	{
		metadata = new MetadataCIP2( spacing , axesName, axesUnit);
		
		// adapt interval origin
		int nDim = rai.numDimensions();
		long[] interval_translation = new long[nDim];
		for(int d=0; d<nDim; d++)
			interval_translation[d] = rai.min(d) - (long)(double)origin.get(d);
		this.rai = Views.offset(rai,  interval_translation );
		
		this.name = metadata.name;
	}
	
	public RaiCIP2( RandomAccessibleInterval<T> rai, List<Double> spacing, List<String> axesName, List<String> axesUnit)
	{
		metadata = new MetadataCIP2( spacing , axesName, axesUnit);
		this.rai = rai;
		this.name = metadata.name;
	}
		
	
	public RaiCIP2( RandomAccessibleInterval<T> rai, List<AxisCIP> metadata )
	{
		this.metadata = new MetadataCIP2( metadata );
		this.rai = rai;
		this.name = this.metadata.name;
	}
	
	
	public MetadataCIP2 metadata() {
		return metadata;
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
	
	
	// Metadata methods
	
	@Override
	public int axesIndex(String name) {
		return metadata.axesIndex(name);
	}


	@Override
	public List<String> axes() {
		return metadata.axes();
	}


	@Override
	public String axes(int d) {
		// TODO Auto-generated method stub
		return metadata.axes(d);
	}


	@Override
	public List<Double> spacing() {
		return metadata.spacing();
	}


	@Override
	public double spacing(int d) {
		return metadata.spacing(d);
	}


	@Override
	public double spacing(String axisName) {
		return metadata.spacing(axisName);
	}

	
	
	public List<Long> min() {
		List<Long> origin = new ArrayList<Long>();
		for( int d=0; d<this.numDimensions(); d++){
			origin.add( this.min(d));
		}
		return origin;
	}


	public long min(String axisName) {
		int d = metadata.axesDim.get(axisName);
		return this.min(d);
	}

	@Override
	public List<String> unit() {
		return metadata.unit();
	}


	@Override
	public String unit(int d) {
		return metadata.unit(d);
	}

	@Override
	public String unit(String axisName) {
		return metadata.unit(axisName);
	}
	
	@Override
	public String toString() {
		return "Image: " + name;
	}
	
	public void setMin(int d, long newOrigin)
	{
		long oldOrigin = rai.min(d);
		long[] offset = new long[numDimensions()];
		offset[d] = oldOrigin-newOrigin;
		rai = Views.offset( rai , offset );
	}


}
