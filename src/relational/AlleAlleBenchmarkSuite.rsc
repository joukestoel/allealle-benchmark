module relational::AlleAlleBenchmarkSuite

import relational::account::allealle::AlleAlleAccountBenchmark;
import relational::filesystem::allealle::AlleAlleFileSystemBenchmark;
import relational::handshake::allealle::AlleAlleHandshakeBenchmark;
import relational::pigeonhole::AlleAllePigeonholeBenchmark;
import relational::ringelection::allealle::AlleAlleRingElectionBenchmark;
import relational::rivercrossing::allealle::AlleAlleRiverCrossingBenchmark;
import relational::square::allealle::AlleAlleSquareBenchmark;

void runAllBenchmarks() {
  runAccountBenchmark();
  runFileSystemBenchmark();
  runHandshakeBenchmark();
  runPigeonholeBenchmark();
  runRingElectionBenchmark();
  runRiverCrossingBenchmark();
  runSquareBenchmark();
}
