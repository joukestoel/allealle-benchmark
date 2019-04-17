package cop;

public class ChocoStatistic {
	private static final int IN_MS = 1000 * 1000;
	
	private final int variables;
	private final int constraints;
	private final long buildingTime;
	private final long resolutionTime;
	
	public ChocoStatistic(final int variables, final int constraints,
			final long buidlingTime, final long resolutionTime) {
		this.variables = variables;
		this.constraints = constraints;
		this.buildingTime = buidlingTime;
		this.resolutionTime = resolutionTime;
	}
	
	public long getBuildingTime() {
		return buildingTime;
	}
	
	public int getConstraints() {
		return constraints;
	}
	
	public long getResolutionTime() {
		return resolutionTime;
	}
	
	public int getVariables() {
		return variables;
	}
	
	@Override
	public String toString() {
		return "[bt: " + (buildingTime / IN_MS) + " (in ms), rt: " + (resolutionTime / IN_MS) + " (in ms), vars: " + variables + ", constraints: " + constraints + " ]";
	}
}
