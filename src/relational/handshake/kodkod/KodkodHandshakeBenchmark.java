package relational.handshake.kodkod;

import kodkod.ast.Formula;
import kodkod.engine.config.Options;
import kodkod.engine.config.Options.OverflowPolicy;
import kodkod.instance.Bounds;
import relational.KodkodBenchmark;

public class KodkodHandshakeBenchmark extends KodkodBenchmark {
	private final Handshake hs;
	
	public KodkodHandshakeBenchmark() {
		super();
		
		hs = new Handshake();
	}
	
	@Override
	public int[] getConfig() {
		return new int[] {10,11,12,13,14,15,16,17};
	}
	
	@Override
	public Bounds getBounds(int config) {
		return hs.bounds(config);
	}

	@Override
	public Formula getFormula(int config) {
		return hs.runPuzzle();
	}

	@Override
	public boolean shouldSolve() {
		return true;
	}
	
	@Override
	protected Options getOptions(int config) {
		Options o = super.getOptions(config);
		o.setBitwidth(32 - Integer.numberOfLeadingZeros(config));
		o.setOverflowPolicy(OverflowPolicy.NONE);
		
		return o;
	}

	public static void main(String[] args) {
		KodkodHandshakeBenchmark khb = new KodkodHandshakeBenchmark();
		khb.run();
	}

	@Override
	protected String getProblemName() {
		return "handshake";
	}
}
