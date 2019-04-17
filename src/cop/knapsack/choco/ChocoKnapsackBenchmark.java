package cop.knapsack.choco;

import cop.AbstractChocoProblem;
import cop.ChocoBenchmark;

public class ChocoKnapsackBenchmark extends ChocoBenchmark {

	@Override
	public int[] getConfig() {
		return new int[] {1};
	}

	@Override
	public AbstractChocoProblem getProblem() {
		return new Knapsack();
	}

	@Override
	protected String getProblemName() {
		return "knapsack";
	}
	
	public static void main(String[] args) {
		ChocoKnapsackBenchmark kbm = new ChocoKnapsackBenchmark();
		kbm.run();
	}

}
