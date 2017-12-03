package invizio.cip.measure;



import java.util.LinkedHashMap;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;


/**
 * 
 * @author Benoit Lombardot
 *
 **/

public class IterableRegionMeasureToolbox<T extends RealType<T>, B extends BooleanType<B>> extends AbstractMeasureToolbox {

	
	public IterableRegionMeasureToolbox( OpService op ) { //, Boolean useUnit) {
		
		super();
		 
		this.add(  new IterableMeasureToolset<T>( op ) ); 
		this.add( new IterableRegionMeasureToolset<B>( op ) ); // , useUnit ) );
		
	}
	
//	@SuppressWarnings("unchecked")
//	public void setMeasurable( IterableRegion<B> region ){
//		
//		this.results = new LinkedHashMap<String,Measure>();
//		this.iterable = Regions.sample(region, (RandomAccessibleInterval<T>) region);
//		this.region = region; 
//		
//	}
	
	
	public void setMeasurable( IterableRegion<B> region , RandomAccessibleInterval<T> rai ){
		
		this.results = new LinkedHashMap<String,Measure>();
		this.iterable = Regions.sample( region , rai );
		
		//long[] min = new long[region.numDimensions()];
		//region.min(min);
		//System.out.println("region (x,y) = (" + min[0] +","+ min[1]+")" ) ;
		//long[] min2 = new long[rai.numDimensions()];
		//rai.min(min2);
		//System.out.println("source (x,y) = (" + min2[0] +","+ min2[1]+")" ) ;
		
		this.region = region;
		
	}
	
}

