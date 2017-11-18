package invizio.cip;

public class AxisCIP {
	
	public double origin;
	public double spacing;
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
