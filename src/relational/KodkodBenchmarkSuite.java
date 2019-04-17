package relational;

import java.util.ArrayList;
import java.util.List;

import relational.account.kodkod.KodkodAccountBenchmark;
import relational.filesystem.kodkod.KodkodFileSystemBenchmark;
import relational.handshake.kodkod.KodkodHandshakeBenchmark;
import relational.pigeonhole.KodkodPigeonholeBenchmark;
import relational.ringelection.kodkod.KodkodRingElectionBenchmark;
import relational.rivercrossing.kodkod.KodkodRiverCrossingBenchmark;
import relational.square.kodkod.KodkodSquareBenchmark;

public class KodkodBenchmarkSuite {
	private List<KodkodBenchmark> benchmarks;
	
	public KodkodBenchmarkSuite() {
		benchmarks = new ArrayList<>();
		
		benchmarks.add(new KodkodAccountBenchmark());
		benchmarks.add(new KodkodFileSystemBenchmark());
		benchmarks.add(new KodkodHandshakeBenchmark());
		benchmarks.add(new KodkodPigeonholeBenchmark());
		benchmarks.add(new KodkodRingElectionBenchmark());
		benchmarks.add(new KodkodRiverCrossingBenchmark());
		benchmarks.add(new KodkodSquareBenchmark());
	}
	
	public void runAllBenchmarks() {
		for (KodkodBenchmark kbm : benchmarks) {
			System.out.println("");
			System.out.println("-------------------");
			System.out.println("Staring benchmarking: " + kbm.getProblemName());
			kbm.run();
		}
		
		System.out.println("");
		System.out.println("Done with all benchmarks");
	}
	
	public static void main(String[] args) {
		KodkodBenchmarkSuite bms = new KodkodBenchmarkSuite();
		bms.runAllBenchmarks();
	}
}
