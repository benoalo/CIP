package invizio.cip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.util.IterableRandomAccessibleRegion;
import net.imglib2.type.BooleanType;
import net.imglib2.view.Views;

public class RegionCIP<B extends BooleanType<B>> implements IterableRegion<B>, Metadata {

	IterableRegion<B> region;
	MetadataCIP2 metadata;
	public String name="";
	
	public RegionCIP( IterableRegion<B> region )
	{
		metadata = new MetadataCIP2( region.numDimensions() );
		this.region = region;
		name = metadata.name;
	}
	
	
	public RegionCIP( IterableRegion<B> region, List<Double> spacing, List<String> axesName, List<String> axesUnit, List<Long> origin)
	{
		metadata = new MetadataCIP2( spacing , axesName, axesUnit);
		
		// adapt interval origin
		int nDim = region.numDimensions();
		long[] translation = new long[nDim];
		for(int d=0; d<nDim; d++)
			translation[d] = region.min(d) - (long)(double)origin.get(d);
		this.region = IterableRandomAccessibleRegion.create(  Views.offset(region,  translation )  );
		
		
		this.region = region;
		name = metadata.name;
	}
	
	
	public RegionCIP( IterableRegion<B> region, List<Double> spacing, List<String> axesName, List<String> axesUnit)
	{
		metadata = new MetadataCIP2( spacing , axesName, axesUnit);
		this.region = region;
		name = metadata.name;
	}
		
	
	public RegionCIP(IterableRegion<B> region, List<AxisCIP> metadata )
	{
		this.metadata = new MetadataCIP2( metadata );
		this.region = region;
		name = this.metadata.name;
	}

	
	public MetadataCIP2 metadata() {
		return metadata;
	}

	@Override
	public Cursor<Void> cursor() {
		return region.cursor();
	}


	@Override
	public Cursor<Void> localizingCursor() {
		return region.localizingCursor();
	}


	@Override
	public Void firstElement() {
		return region.firstElement();
	}


	@Override
	public Object iterationOrder() {
		return region.iterationOrder();
	}


	@Override
	public double realMin(int d) {
		return region.realMin( d );
	}


	@Override
	public void realMin(double[] min) {
		region.realMin(min);
	}


	@Override
	public void realMin(RealPositionable min) {
		region.realMin(min);
	}


	@Override
	public double realMax(int d) {
		return region.realMax(d);
	}


	@Override
	public void realMax(double[] max) {
		region.realMax(max);
	}


	@Override
	public void realMax(RealPositionable max) {
		region.realMax(max);
	}


	@Override
	public int numDimensions() {
		return region.numDimensions();
	}


	@Override
	public long min(int d) {
		return region.min(d);
	}


	@Override
	public void min(long[] min) {
		region.min(min);
	}


	@Override
	public void min(Positionable min) {
		region.min(min);
	}


	@Override
	public long max(int d) {
		return region.max(d);
	}


	@Override
	public void max(long[] max) {
		region.max(max);
	}


	@Override
	public void max(Positionable max) {
		region.max(max);
	}


	@Override
	public void dimensions(long[] dimensions) {
		region.dimensions( dimensions );
	}


	@Override
	public long dimension(int d) {
		return region.dimension(d);
	}


	@Override
	public RandomAccess<B> randomAccess() {
		return region.randomAccess();
	}


	@Override
	public RandomAccess<B> randomAccess(Interval interval) {
		return region.randomAccess( interval );
	}


	@Override
	public long size() {
		return region.size();
	}


	@Override
	public Iterator<Void> iterator() {
		return region.iterator();
	}


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
		return "Region: " + name;
	}
	
}
