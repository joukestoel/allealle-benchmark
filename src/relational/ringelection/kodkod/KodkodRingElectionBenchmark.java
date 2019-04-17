package relational.ringelection.kodkod;

import kodkod.ast.Formula;
import kodkod.instance.Bounds;
import relational.KodkodBenchmark;

public class KodkodRingElectionBenchmark extends KodkodBenchmark {
	private RingElection re;
	
	public KodkodRingElectionBenchmark() {
		this.re = new RingElection();
	}
	
	@Override
	public int[] getConfig() {
		return new int[] {3,4,5};
	}

	@Override
	protected String getProblemName() {
		return "ringelection";
	}
	
	@Override
	public Bounds getBounds(int config) {
		return re.bounds(config, config+1);
	}

	@Override
	public Formula getFormula(int config) {
		return re.invariants().and(re.someElected());
	}

	@Override
	public boolean shouldSolve() {
		return true;
	}

	public static void main(String[] args) {
		KodkodRingElectionBenchmark reb = new KodkodRingElectionBenchmark();
		reb.run();
	}
}
