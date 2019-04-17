package cop.mario.choco;

import cop.AbstractChocoProblem;
import cop.ChocoBenchmark;

public class ChocoMarioKartBenchmark extends ChocoBenchmark {
	@Override
	public int[] getConfig() {
		return new int[] {1};
	}

	@Override
	public AbstractChocoProblem getProblem() {
		return new MarioKart();
	}

	@Override
	protected String getProblemName() {
		return "mariokart";
	}

	public static void main(String[] args) {
		ChocoMarioKartBenchmark mkbm = new ChocoMarioKartBenchmark();
		mkbm.run();
	}
	
}
