package invizio.cip;

import java.util.Map;

public interface FreeDimensionPositionable {

	public void setPosition(String axisName, Long position);
	public void setPosition( Map<String,Long> newfreeDimPosition );
	public Long getPosition(String axisName);
	public Map<String,Long> getPosition();
}
