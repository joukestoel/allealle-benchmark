package relational.account.kodkod;

import kodkod.ast.Formula;
import kodkod.engine.config.Options;
import kodkod.instance.Bounds;
import relational.KodkodBenchmark;

public class KodkodAccountBenchmark extends KodkodBenchmark {
	private Account problem;
	
	public KodkodAccountBenchmark() {
		this.problem = new Account();
	}
	
	@Override
	public int[] getConfig() {
		return new int[] {5,6,7,8,9};
	}

	@Override
	protected String getProblemName() {
		return "account";
	}

	@Override
	public Bounds getBounds(int config) {
		return problem.bounds(config);
	}

	@Override
	public Formula getFormula(int config) {
		return problem.constraints(config);
	}

	@Override
	public boolean shouldSolve() {
		return true;
	}

	@Override
	protected Options getOptions(int config) {
		Options o = super.getOptions(config);
		o.setBitwidth(config);
		return o;
	}
	
	public static void main(String[] args) {
		KodkodAccountBenchmark abm = new KodkodAccountBenchmark();
		abm.run();
	}
}
