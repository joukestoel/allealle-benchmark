package relational.rivercrossing.kodkod;

import kodkod.ast.Formula;
import kodkod.instance.Bounds;
import relational.KodkodBenchmark;

public class KodkodRiverCrossingBenchmark extends KodkodBenchmark {
	private RiverCrossing rc;
	
	public KodkodRiverCrossingBenchmark() {
		rc = new RiverCrossing();
	}
	
	@Override
	public int[] getConfig() {
		return new int[] {1};
	}

	@Override
	protected String getProblemName() {
		return "rivercrossing";
	}

	@Override
	public Bounds getBounds(int config) {
		return rc.bounds();
	}

	@Override
	public Formula getFormula(int config) {
		return rc.constraints();
	}

	@Override
	public boolean shouldSolve() {
		return true;
	}

	public static void main(String[] args) {
		KodkodRiverCrossingBenchmark rcb = new KodkodRiverCrossingBenchmark();
		rcb.run();
	}
}
