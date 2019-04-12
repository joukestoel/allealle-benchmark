package relational.pigeonhole;

import kodkod.ast.Formula;
import kodkod.instance.Bounds;
import relational.KodkodBenchmark;
import kodkod.examples.alloy.Pigeonhole;

public class KodkodPigeonholeBenchmark extends KodkodBenchmark {
	private final Pigeonhole phProblem;
	
	public KodkodPigeonholeBenchmark() {
		super();
		
		phProblem = new Pigeonhole();
	}
	
	@Override
	public int[] getConfig() {
		int startAt = 10;
		int nrOfConfigs = 90;
		
		int[] config = new int[nrOfConfigs];
		for (int i = 0; i < config.length; i++) {
			config[i] = i + startAt;
		}
		
		return config;
	}

	@Override
	public int getNrOfWarmupRounds() {
		return 10;
	}

	@Override
	public int getNrOfRunsPerConfig() {
		return 30;
	}
	
	public boolean shouldSolve() {
		return false;
	}

	@Override
	public Bounds getBounds(int config) {
		return phProblem.bounds(config, config-1);
	}

	@Override
	public Formula getFormula() {
		return phProblem.declarations().and(phProblem.pigeonPerHole());
	}
	
	public static void main(String[] args) {
		KodkodPigeonholeBenchmark phb = new KodkodPigeonholeBenchmark();
		phb.run();
	}

	@Override
	protected String getProblemName() {
		return "pigeonhole";
	}
}
