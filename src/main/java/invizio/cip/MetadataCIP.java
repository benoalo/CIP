package invizio.cip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import ij.process.LUT;


public class MetadataCIP {

	public int channelDim = -1;
	
	double[] spacing;
	List<String> axes;
	Map<String, Integer> axesDim;
	List<LUT> luts;
	List<String> axesUnit;
	int nChannel = 1;
	String name;
	
	public MetadataCIP(int nDim)
	{
		this.spacing = new double[nDim];
		this.axes = new ArrayList<String>();
		axesDim = new HashMap<String,Integer>();
		axesUnit = new ArrayList<String>();
		luts = null;
		
		for(int d=0; d<nDim; d++ )
		{
			String axisName = "D"+d;
			spacing[d] = 1;
			axes.add(axisName);
			axesDim.put(axisName, d);
			axesUnit.add(null);
		}	
	}

	
	
	
	public MetadataCIP( double[] spacing, List<String> axesName, List<String> axesUnit, List<LUT> luts)
	{
		this.spacing = spacing;
		this.axes = axesName;
		this.axesUnit = axesUnit;
		this.luts = luts;
		
		axesDim = new HashMap<String,Integer>();
		int count = 0;
		for(String name : axes ) {
			axesDim.put(name, count);
			count++;
		}
		
		for( int d=0 ; d<axesName.size() ; d++ ) {
			String axisName = axesName.get(d);
			if( axisName.toLowerCase().equals("c") || axisName.toLowerCase().equals("ch") || axisName.toLowerCase().equals("channel") ) {
				channelDim = d;
			}
		}
		
		if( luts != null ) {
			nChannel = luts.size();
		}
		
	}

	
	
	
	public MetadataCIP( MetadataCIP metadata)
	{
		this.spacing = metadata.spacing();
		this.axes = metadata.axes();
		this.axesUnit = metadata.unit();
		this.luts = metadata.lut();
		this.channelDim = metadata.channelDim;
		
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
	
	
	public List<String> axes(){
		return copy( axes );
	}
	
	
	public String axes(int d) {
		return new String( axes.get(d) );
	}

	
	public double[] spacing() {
		double[] spacing2 = Arrays.copyOf( spacing, spacing.length );
		return spacing2;
	}
	
	
	public double spacing(int d) {
		return spacing[d];
	}

	public double spacing(String axisName) {
		return spacing[axesDim.get(axisName)];
	}
	

	public String unit(int d){
		return new String( axesUnit.get(d) );
	}

	public List<String> unit(){
		return copy( axesUnit );
	}
	

	public LUT lut(int d){
		return luts.get(d); // TODO: clone before return
	}

	public List<LUT> lut(){
		return luts; // todo: clone
	}

	public void lut(LUT lut) {
		if ( luts == null ) {
			luts = new ArrayList<LUT>();
			luts.add(lut);
		}
		else {
			for(int ch=0; ch<luts.size(); ch++)
				luts.set( ch, lut);
		}
	}
	
	
	
	public void dropDimensions( Integer[] dimensions ) {
		
		Integer[] dims = dimensions.clone();
		Arrays.sort(dims);
		for(int i=dims.length-1 ; i>=0 ; i-- )
			dropDimension( dims[i] );
	}
		
	
	public void dropDimension( int dim ) {
		
		axes.remove( dim );
		axesUnit.remove( dim );

		double[] spacingTmp = spacing;
		
		spacing  = new double[spacingTmp.length-1];
		int count = 0 ;
		for( int d=0; d<spacingTmp.length; d++)
		{
			if(d!=dim){
				spacing[count] = spacingTmp[d];
				if ( channelDim == d )
					channelDim = count;
				count++;
			}
		}
		if ( channelDim==dim )
			channelDim=-1;
		
		
	}
	
	
	
	public void dropLutsOutOfRange( int start, int stop ) {
		
		List<LUT> lutsTmp = luts;
		luts = new ArrayList<LUT>();
		for( int pos=start ; pos<stop; pos++ )
			luts.add( lutsTmp.get(pos) );
	}
	
	
	private static List<String> copy( List<String> in){
		
		if ( in==null )
			return null;
		
		List<String> out = new ArrayList<String>();
		for(String str: in )
			out.add( str==null ? null : new String( str ) );
		return out;
	}

	
}
