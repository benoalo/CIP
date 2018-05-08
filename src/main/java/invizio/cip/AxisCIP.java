package invizio.cip;

public class AxisCIP {
	
	@Deprecated
	public double origin; // the physical origin of the axis
	
	public double spacing; // the physical spacing between 2 sample
	public String unit;
	public String name;
	
	public AxisCIP(String name)
	{
		this.name = name;
		this.origin = 0;
		this.spacing = 1;
		this.unit = "arbitrary unit";
	}

	public AxisCIP(String name, double origin, double spacing, String unit)
	{
		this.name = name;
		this.origin = origin;
		this.spacing = spacing;
		this.unit = unit;
	}
	
	
}
