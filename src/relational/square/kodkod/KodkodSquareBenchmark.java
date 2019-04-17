package relational.square.kodkod;

import kodkod.ast.Formula;
import kodkod.engine.config.Options;
import kodkod.instance.Bounds;
import relational.KodkodBenchmark;

public class KodkodSquareBenchmark extends KodkodBenchmark {
	private Square problem;
	
	public KodkodSquareBenchmark() {
		problem = new Square();
	}
	
	@Override
	public int[] getConfig() {
		return new int[] {4,5,6,7,8,9,10};
	}

	@Override
	protected String getProblemName() {
		return "square";
	}

	@Override
	public Bounds getBounds(int config) {
		return problem.bounds(config);
	}

	@Override
	public Formula getFormula(int config) {
		return problem.constraints();
	}

	@Override
	public boolean shouldSolve() {
		return true;
	}
	
	@Override
	protected Options getOptions(int config) {
		Options options = super.getOptions(config);
		options.setBitwidth(config);
		
		return options;
	}
	
	public static void main(String[] args) {
		KodkodSquareBenchmark sb = new KodkodSquareBenchmark();
		sb.run();
	}
	
}
