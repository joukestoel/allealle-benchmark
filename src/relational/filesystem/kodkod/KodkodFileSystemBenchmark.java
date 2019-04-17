package relational.filesystem.kodkod;

import kodkod.ast.Formula;
import kodkod.instance.Bounds;
import relational.KodkodBenchmark;

public class KodkodFileSystemBenchmark extends KodkodBenchmark {
	private FileSystem fs;
	
	public KodkodFileSystemBenchmark() {
		this.fs = new FileSystem();
	}
	
	@Override
	public int[] getConfig() {
		int begin = 5;
		int end = 11;
		int[] config = new int[end-begin];
		
		for (int i = begin; i < end; i++) {
			config[i-begin] = i;
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

	@Override
	public Bounds getBounds(int config) {
		return fs.bounds(config);
	}

	@Override
	public Formula getFormula(int config) {
		return fs.checkNoDirAliases();
	}

	@Override
	public boolean shouldSolve() {
		return true;
	}
	
	public static void main(String[] args) {
		KodkodFileSystemBenchmark fsb = new KodkodFileSystemBenchmark();
		fsb.run();
	}

	@Override
	protected String getProblemName() {
		return "filesystem";
	}

}
