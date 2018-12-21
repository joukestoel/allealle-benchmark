package kodkod.pigeonhole;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kodkod.ast.Formula;
import kodkod.engine.Statistics;
import kodkod.engine.config.Options;
import kodkod.engine.fol2sat.Translation;
import kodkod.engine.fol2sat.Translator;
import kodkod.engine.satlab.SATFactory;
import kodkod.engine.satlab.SATSolver;

import kodkod.pigeonhole.Pigeonhole;

public class KodkodBenchmark {
	private final int startNrOfAtoms;
	private final int endNrOfAtoms;
	private final int nrOfRunsPerConfig;
	private final Pigeonhole model;
	private final Formula formula;

	private Statistics[][] benchmarkTimes; 
	
	private final File csv;
	
	public KodkodBenchmark(int startNrOfAtoms, int endNrOfAtoms, int nrOfRunsPerConfig, String csvFileLocation) {
		this.startNrOfAtoms = startNrOfAtoms;
		this.endNrOfAtoms = endNrOfAtoms;
		this.nrOfRunsPerConfig = nrOfRunsPerConfig;
		this.csv = new File(csvFileLocation);
		
		this.model = new Pigeonhole();
		this.formula = model.declarations().and(model.pigeonPerHole());
		
		this.benchmarkTimes = new Statistics[endNrOfAtoms-startNrOfAtoms][nrOfRunsPerConfig];
	}
	
	public void run() {
		Options options = new Options();
		options.setSymmetryBreaking(0); // Disable symmetry breaking predicates
//		options.setSolver(SATFactory.MiniSat);

		System.out.println("Warming up");
		for (int i = 0; i < 10; i++) {
			Translator.translate(formula, model.bounds(startNrOfAtoms, startNrOfAtoms-1), options);
		}
		System.out.println("Warming up done");
		
		for (int i = 0; i < endNrOfAtoms-startNrOfAtoms; i++) {
			System.out.println("Starting of translating for " + (i+startNrOfAtoms) + " pigeons and " + (i+startNrOfAtoms-1) + " holes");
			for (int r = 0; r < nrOfRunsPerConfig; r++) {
				System.out.print(".");
				long startTransl = getCpuTime();
				Translation.Whole translation = Translator.translate(formula, model.bounds(i+startNrOfAtoms, i+startNrOfAtoms-1), options);
				long endTransl = getCpuTime();
								
				// start solving
				long startSolve = getCpuTime();
				SATSolver cnf = translation.cnf();
				options.reporter().solvingCNF(translation.numPrimaryVariables(), cnf.numberOfVariables(), cnf.numberOfClauses());
				boolean sat = cnf.solve();
				long endSolve = getCpuTime();
				
				System.out.println("Solve time: " + (endSolve - startSolve) / 1000000);
				
				if (sat) {
					System.err.println("Pigeon Hole can not be sat!!");
				}
				
				benchmarkTimes[i][r] = new Statistics(translation, (endTransl - startTransl) / 1000000, (endSolve - startSolve) / 1000000); 
			}
			System.out.println("Done");
		}
		System.out.println("Completely done, saving to csv");
		saveToCSV();
	}
	
	private void saveToCSV() {
		if (!csv.exists()) {
			try {
				csv.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try (PrintWriter pw = new PrintWriter(new FileWriter(csv))) {
			pw.println("#Config,TranslationTime,SolvingTime,#Vars,#Clauses");
			
			List<Statistics> aggregated = aggregate();
			
			for (int i = 0; i < aggregated.size(); i++) {
				pw.printf("%d,%d,%d,%d\n", i+startNrOfAtoms, aggregated.get(i).translationTime(), aggregated.get(i).solvingTime(), aggregated.get(i).primaryVariables(), aggregated.get(i).clauses());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<Statistics> aggregate() {
		List<Statistics> aggregate = new ArrayList<>(endNrOfAtoms-startNrOfAtoms);
		
		for (int i = 0; i < endNrOfAtoms-startNrOfAtoms; i++) {
			List<Statistics> sortedByTransTime = sortByTranslationTime(benchmarkTimes[i]);
			List<Statistics> sortedBySolveTime = sortBySolvingTime(benchmarkTimes[i]);
			
			long translationMean = 0;
			long solvingMean = 0;

			int vars = sortedByTransTime.get(0).primaryVariables();
			int clauses = sortedByTransTime.get(0).clauses();
			
			if (sortedByTransTime.size()%2 == 0) {
				translationMean = (sortedByTransTime.get(sortedByTransTime.size()/2).translationTime() + sortedByTransTime.get(sortedByTransTime.size()/2 + 1).translationTime()) / 2;
				solvingMean = (sortedBySolveTime.get(sortedBySolveTime.size()/2).solvingTime() + sortedBySolveTime.get(sortedBySolveTime.size()/2 + 1).solvingTime()) / 2;
			} else {
				translationMean = sortedByTransTime.get(sortedByTransTime.size()/2).translationTime();
				solvingMean = sortedBySolveTime.get(sortedBySolveTime.size()/2).solvingTime(); 
			}
						
			aggregate.add(new Statistics(vars, vars, clauses, translationMean, solvingMean));
		}
		
		return aggregate;
	}
	
	private List<Statistics> sortBySolvingTime(Statistics[] unsorted) {
		List<Statistics> sorted = Arrays.asList(unsorted);
		sorted.sort((a,b) -> (int) (a.solvingTime() - b.solvingTime()));
		
		return sorted;
	}

	private List<Statistics> sortByTranslationTime(Statistics[] unsorted) {
		List<Statistics> sorted = Arrays.asList(unsorted);
		sorted.sort((a,b) -> (int) (a.translationTime() - b.translationTime()));
		
		return sorted;
	}
	
	private static long getCpuTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}
	
	public static void main(String... args) {
		KodkodBenchmark bm = new KodkodBenchmark(10, 20, 10, "/Users/jouke/workspace/allealle-benchmark/benchmark/results/kodkod-comparison/kodkod-results_with_solve.csv");
		bm.run();
	}
}
