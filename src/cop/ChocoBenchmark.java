package cop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.measure.Measures;

public abstract class ChocoBenchmark {
	private static final int IN_MS = 1000 * 1000;
	
	private ChocoStatistic[][] benchmarkTimes; 
	
	private final File csv;
	
	private final int[] config;
	
	public ChocoBenchmark() {
		this.csv = new File(getCsvFileLocation());
		this.config = getConfig();
		this.benchmarkTimes = new ChocoStatistic[getConfig().length][getNrOfRunsPerConfig()];
	}
	
	public abstract int[] getConfig();
	
	public abstract AbstractChocoProblem getProblem();
	
	public int getNrOfWarmupRounds() {
		return 10;
	} 
	
	public int getNrOfRunsPerConfig() {
		return 30;
	}
	
	protected String getFileBase() { return "/Users/jouke/workspace/allealle-benchmark/results/cop/"; }
	protected abstract String getProblemName();
	
	protected String getCsvFileLocation() {
		int[] config = getConfig();
		
		return sysSep(getFileBase()) + sysSep(getProblemName()) + "choco_" + config[0] + "-" + config[config.length-1] + ".csv"; 
	}
	
	private String sysSep(String orig) {
		return !orig.endsWith(File.separator) ? orig + File.separator : orig;
	}

	public void run() {				
		System.out.print("Warming up");
		
		for (int i = 0; i < getNrOfWarmupRounds(); i++) {
			AbstractChocoProblem problem = getProblem();
			problem.execute();
			System.out.print(".");
		}
		System.out.println("done");
		
		for (int i = 0; i < config.length; i++) {
			System.out.print("Starting \'" + getProblemName() + "\' run ");

			for (int r = 0; r < getNrOfRunsPerConfig(); r++) {
				System.out.print(".");

				AbstractChocoProblem problem = getProblem();
				problem.execute();
				
				Model model = problem.getModel();
				Measures measures = model.getSolver().getMeasures();
				
				benchmarkTimes[i][r] = new ChocoStatistic(model.getNbVars(), model.getNbCstrs(), 
						inMs(measures.getReadingTimeCountInNanoSeconds()), inMs(measures.getTimeCountInNanoSeconds()));
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
			pw.println("Variables,Constraints,BuildingTime,ResolutionTime");
			
			List<ChocoStatistic> aggregated = aggregate();
			
			for (int i = 0; i < aggregated.size(); i++) {
				pw.printf("%d,%d,%d,%d\n", aggregated.get(i).getVariables(), aggregated.get(i).getConstraints(), aggregated.get(i).getBuildingTime(), aggregated.get(i).getResolutionTime());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<ChocoStatistic> aggregate() {
		List<ChocoStatistic> aggregate = new ArrayList<>(getConfig().length);
		
		for (int i = 0; i < config.length; i++) {
			List<ChocoStatistic> sortedByBuildingTime = sortByBuildingTime(benchmarkTimes[i]);
			List<ChocoStatistic> sortedByResolutionTime = sortByResolutionTime(benchmarkTimes[i]);
			
			long buildingMedian = 0;
			long resolutionMedian = 0;

			int vars = sortedByBuildingTime.get(0).getVariables();
			int constraints = sortedByBuildingTime.get(0).getConstraints();
			
			if (sortedByBuildingTime.size()%2 == 0) {
				buildingMedian = (sortedByBuildingTime.get(sortedByBuildingTime.size()/2).getBuildingTime() + sortedByBuildingTime.get(sortedByBuildingTime.size()/2 + 1).getBuildingTime()) / 2;
				resolutionMedian = (sortedByResolutionTime.get(sortedByResolutionTime.size()/2).getResolutionTime() + sortedByResolutionTime.get(sortedByResolutionTime.size()/2 + 1).getResolutionTime()) / 2;
			} else {
				buildingMedian = sortedByBuildingTime.get(sortedByBuildingTime.size()/2).getBuildingTime();
				resolutionMedian = sortedByResolutionTime.get(sortedByResolutionTime.size()/2).getResolutionTime(); 
			}
						
			ChocoStatistic agg = new ChocoStatistic(vars, constraints, buildingMedian, resolutionMedian);
			
			aggregate.add(agg);
		}
		
		return aggregate;
	}
	
	private long inMs(long inNano) {
		return Math.round((double) inNano / IN_MS); 
	}
	
	protected List<ChocoStatistic> sortByBuildingTime(ChocoStatistic[] unsorted) {
		List<ChocoStatistic> sorted = Arrays.asList(unsorted);
		sorted.sort((a,b) -> (int) (a.getBuildingTime() - b.getBuildingTime()));
		
		return sorted;
	}

	protected List<ChocoStatistic> sortByResolutionTime(ChocoStatistic[] unsorted) {
		List<ChocoStatistic> sorted = Arrays.asList(unsorted);
		sorted.sort((a,b) -> (int) (a.getResolutionTime() - b.getResolutionTime()));
		
		return sorted;
	}
	
	protected static long getCpuTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}
}
