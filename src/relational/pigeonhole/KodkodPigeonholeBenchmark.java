package relational.pigeonhole;

import kodkod.ast.Formula;
import kodkod.instance.Bounds;
import relational.KodkodBenchmark;

public class KodkodPigeonholeBenchmark extends KodkodBenchmark {
	private final Pigeonhole phProblem;
	
	public KodkodPigeonholeBenchmark() {
		super();
		
		phProblem = new Pigeonhole();
	}
	
	@Override
	public int[] getConfig() {
		int startAt = 5;
		int nrOfConfigs = 6;
		
		int[] config = new int[nrOfConfigs];
		for (int i = 0; i < config.length; i++) {
			config[i] = i + startAt;
		}
		
		return config;
	}

	public boolean shouldSolve() {
		return true;
	}

	@Override
	public Bounds getBounds(int config) {
		return phProblem.bounds(config, config-1);
	}

	@Override
	public Formula getFormula(int config) {
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
