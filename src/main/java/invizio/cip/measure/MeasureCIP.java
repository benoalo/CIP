package invizio.cip.measure;


import java.util.List;
import java.util.Map;

import org.scijava.plugin.Parameter;
import org.scijava.service.AbstractService;

import invizio.cip.CIPService;
import invizio.cip.RaiCIP2;
import net.imagej.ImageJService;
import net.imagej.ops.OpService;
import net.imglib2.type.numeric.RealType;



/**
 * 
 * @author Benoit Lombardot
 *
 **/



public class MeasureCIP  extends AbstractService implements ImageJService {
	
	@Parameter
	private OpService op;
	
	@Parameter
	private CIPService cipService;
	
	
	public <T extends RealType<T>> Object imageMeasures(RaiCIP2<T> raiCIP , List<String> measureNames, Boolean  useUnit, String prefix)
	{

		// parse input parameters
		if ( useUnit == null)
			useUnit=true;
		if (prefix == null)
			prefix = "";
		
		// instantiate a measure Toolbox
		RaiCIPMeasureToolbox<T> measureToolbox = new RaiCIPMeasureToolbox<T>( op , useUnit );
		measureToolbox.setMeasurable( raiCIP );
		for( String measureName : measureNames) {
			measureToolbox.measure( measureName );
		}
		Map<String,Measure> results = measureToolbox.results( prefix );
		
		return results;
	}


	
	
}
