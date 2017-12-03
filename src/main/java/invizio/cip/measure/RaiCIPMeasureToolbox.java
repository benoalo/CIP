package invizio.cip.measure;


import java.util.LinkedHashMap;

import invizio.cip.RaiCIP2;
import net.imagej.ops.OpService;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;


/**
 * 
 * @author Benoit Lombardot
 *
 **/

public class RaiCIPMeasureToolbox<T extends RealType<T>> extends AbstractMeasureToolbox {

	
	public RaiCIPMeasureToolbox( OpService op , Boolean useUnit) {
		
		super();
		 
		this.add(  new IterableMeasureToolset<T>( op ) ); 
		this.add( new RaiCIPMeasureToolset<T>( op , useUnit ) );
		
	}
	
	public void setMeasurable( RaiCIP2<T> raiCIP ){
		
		this.results = new LinkedHashMap<String,Measure>();
		this.iterable = Views.iterable( raiCIP );
		this.raiCIP = raiCIP; 
		
	}
	
}
