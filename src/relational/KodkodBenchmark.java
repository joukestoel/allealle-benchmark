package relational;

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
import kodkod.engine.config.Options.OverflowPolicy;
import kodkod.engine.fol2sat.Translation;
import kodkod.engine.fol2sat.Translator;
import kodkod.engine.satlab.SATFactory;
import kodkod.engine.satlab.SATSolver;
import kodkod.instance.Bounds;

public abstract class KodkodBenchmark {
	private Statistics[][] benchmarkTimes; 
	
	private final File csv;
	
	private final int[] config;
	
	public KodkodBenchmark() {
		this.csv = new File(getCsvFileLocation());
		this.config = getConfig();
		this.benchmarkTimes = new Statistics[getConfig().length][getNrOfRunsPerConfig()];
	}
	
	public abstract int[] getConfig();
	public int getNrOfWarmupRounds() {
		return 10;
	}
	public int getNrOfRunsPerConfig() {
		return 30;
	}
	
	protected String getFileBase() { return "/Users/jouke/workspace/allealle-benchmark/results/relational/"; }
	protected abstract String getProblemName();
	
	protected String getCsvFileLocation() {
		int[] config = getConfig();
		String solvePart = shouldSolve() ? "with" : "without";
		
		return sysSep(getFileBase()) + sysSep(getProblemName()) + "kodkod_" + solvePart + "_solve_" + config[0] + "-" + config[config.length-1] + ".csv"; 
	}
	
	private String sysSep(String orig) {
		return !orig.endsWith(File.separator) ? orig + File.separator : orig;
	}
	
	public abstract Bounds getBounds(int config);
	public abstract Formula getFormula();
	
	public abstract boolean shouldSolve();
	
	protected Options getOptions(int config) {
		Options options = new Options();
		options.setSymmetryBreaking(0); // Disable symmetry breaking predicates
		options.setSolver(SATFactory.DefaultSAT4J);
		options.setOverflowPolicy(OverflowPolicy.PREVENT);
		
		return options;
	}
	
	public void run() {
		Formula formula = getFormula();
				
		Options options = getOptions(1);
		
		System.out.print("Warming up");
		for (int i = 0; i < getNrOfWarmupRounds(); i++) {
			long startTime = System.currentTimeMillis();
			Translator.translate(formula, getBounds(config[0]), options);
			System.out.print(".(" + (System.currentTimeMillis() - startTime) + ")");
		}
		System.out.println("done");
		
		for (int i = 0; i < config.length; i++) {
			System.out.print("Starting \'" + getProblemName() + "\' run with config \'" + config[i] + "\'");
			
			for (int r = 0; r < getNrOfRunsPerConfig(); r++) {
				System.out.print(".");
				
				options = getOptions(config[i]);
				
				long startTransl = getCpuTime();
				Translation.Whole translation = Translator.translate(formula, getBounds(config[i]), options);
				long endTransl = getCpuTime();
				
				Statistics stat;
				if (shouldSolve()) {
					// start solving
					SATSolver cnf = translation.cnf();
					options.reporter().solvingCNF(translation.numPrimaryVariables(), cnf.numberOfVariables(), cnf.numberOfClauses());

					long startSolve = getCpuTime();
					boolean sat = cnf.solve();
					long endSolve = getCpuTime();
					
					stat = new Statistics(translation, (endTransl - startTransl) / 1000000, (endSolve - startSolve) / 1000000);
					stat.setSat(sat);
				} else {
					stat = new Statistics(translation, (endTransl - startTransl) / 1000000, 0);
				}
				
				benchmarkTimes[i][r] = stat;
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
			pw.println("#Config,TranslationTime,SolvingTime,Sat,#Vars,#Clauses");
			
			List<Statistics> aggregated = aggregate();
			
			for (int i = 0; i < aggregated.size(); i++) {
				pw.printf("%d,%d,%d,%b,%d,%d\n", config[i], aggregated.get(i).translationTime(), aggregated.get(i).solvingTime(), aggregated.get(i).getSat(), aggregated.get(i).primaryVariables(), aggregated.get(i).clauses());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<Statistics> aggregate() {
		List<Statistics> aggregate = new ArrayList<>(getConfig().length);
		
		for (int i = 0; i < config.length; i++) {
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
						
			Statistics agg = new Statistics(vars, vars, clauses, translationMean, solvingMean);
			agg.setSat(sortedByTransTime.get(0).getSat());
			
			aggregate.add(agg);
		}
		
		return aggregate;
	}
	
	protected List<Statistics> sortBySolvingTime(Statistics[] unsorted) {
		List<Statistics> sorted = Arrays.asList(unsorted);
		sorted.sort((a,b) -> (int) (a.solvingTime() - b.solvingTime()));
		
		return sorted;
	}

	protected List<Statistics> sortByTranslationTime(Statistics[] unsorted) {
		List<Statistics> sorted = Arrays.asList(unsorted);
		sorted.sort((a,b) -> (int) (a.translationTime() - b.translationTime()));
		
		return sorted;
	}
	
	protected static long getCpuTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}
}
