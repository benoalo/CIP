package invizio.cip;

import java.util.List;

public interface Metadata {
	
	public int axesIndex(String name);
	
	public List<String> axes();
	public String axes(int d);
	
	public List<Double> spacing();
	public double spacing(int d);
	public double spacing(String axisName);
	
	public List<String> unit();
	public String unit(int d);
	
}
