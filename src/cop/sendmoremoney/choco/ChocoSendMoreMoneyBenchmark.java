package cop.sendmoremoney.choco;

import cop.AbstractChocoProblem;
import cop.ChocoBenchmark;

public class ChocoSendMoreMoneyBenchmark extends ChocoBenchmark {

	@Override
	public int[] getConfig() {
		return new int[] {1};
	}

	@Override
	public AbstractChocoProblem getProblem() {
		return new ChocoSendMoreMoney();
	}

	@Override
	protected String getProblemName() {
		return "sendmoremoney";
	}
	
	public static void main(String[] args) {
		ChocoSendMoreMoneyBenchmark smmbm = new ChocoSendMoreMoneyBenchmark();
		smmbm.run();
	}
	
}
