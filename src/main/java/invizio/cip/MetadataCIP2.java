package invizio.cip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import ij.process.LUT;


public class MetadataCIP2 extends ArrayList<AxisCIP>{

	
	
	int nDim;
	Map<String, Integer> axesDim;
	String name="";
	String[] defaultNames = new String[] {"X", "Y", "Z"};
	
	public MetadataCIP2(int nDim)
	{
		this.nDim = nDim;
		axesDim = new HashMap<String,Integer>();
		for(int d=0; d<nDim; d++ )
		{
			String axisName;
			if (d < 3)
				axisName = defaultNames[d];
			else
				axisName = "D"+d;
			
			add( new AxisCIP( axisName ) ); 
			axesDim.put(axisName , d );
		}	
	}


	public MetadataCIP2(List<AxisCIP> axes)
	{
		this.nDim = axes.size();
		
		axesDim = new HashMap<String,Integer>();
		int d=0;
		for(AxisCIP axis : axes)
		{
			add(  new AxisCIP( axis.name, axis.origin, axis.spacing, axis.unit) );
			axesDim.put( axis.name , d );
			d++;
		}
	}
	
	
	public MetadataCIP2( List<Double> spacing, List<String> axesName, List<String> axesUnit)
	{
		nDim = axesName.size();
		
		axesDim = new HashMap<String,Integer>();
		for(int d=0 ; d<nDim ; d++)
		{
			axesDim.put(axesName.get(d), d );
			add( new AxisCIP( axesName.get(d) , 0 , spacing.get(d), axesUnit.get(d) ) );
		}
		
		
	}

	
	
	
	
	
	
	
	
	public int axesIndex(String name) {
		return axesDim.get(name);
	}
	
	
	public List<String> axes(){
		
		List<String> axesName = new ArrayList<String>();
		for( AxisCIP axis : this ) {
			axesName.add(axis.name);
		}
		
		return axesName;
	}
	
	
	public String axes(int d) {
		return new String( get(d).name );
	}

	
	public List<Double> spacing() {
		List<Double> spacing = new ArrayList<Double>();
		for( AxisCIP axis : this ) {
			spacing.add(axis.spacing);
		}
		return spacing;
	}
	
	
	public double spacing(int d) {
		return get(d).spacing;
	}

	public double spacing(String axisName) {
		int d = axesDim.get(axisName);
		return spacing(d);
	}
	

	public String unit(int d){
		return new String( get(d).unit );
	}

	public List<String> unit(){
		List<String> units = new ArrayList<String>();
		for( AxisCIP axis : this ) {
			units.add(axis.unit);
		}
		return units;
	}
	
	
	
	public void dropDimensions( Integer[] dimensions ) {
		
		Integer[] dims = Arrays.copyOf(dimensions, dimensions.length);
		Arrays.sort( dims );
		for(int i=dims.length-1 ; i>=0 ; i-- )
			dropDimension( dims[i] );
		
	}
		
	
	public void dropDimension( int dim ) {
		
		this.remove( dim );		
	
	}
	
	
	
}
