module AlleAlleBenchmark

import List;
import Set;
import util::Benchmark;
import IO;
import Map;

import ide::Imploder;   
import translation::AST;
import translation::Environment;
import translation::Translator;
import translation::Relation;
import ModelFinder;
import smtlogic::Core;

import util::MemoCacheClearer;

alias Statistic = tuple[int config, int translationTime, int solvingTime, bool sat, int vars, int clauses];

set[str] alleAlleMods() = {"translation::Environment","translation::Relation"};

Problem getProblem(int config) 
  = implodeProblem("<constructRels(config)>
                   '<getConstraints(config)>");

default str constructRels(int config) { throw "Not yet implemented"; } 
default str getConstraints(int config) { throw "Not yet implemented"; }
default str nrOfAtomsInUniverse(int config) { throw "Not yet implemented"; }

void runBenchmark(list[int] config, str part, str projectName, bool shouldSolve, int warmup = 10, int runsPerConfig = 30) {
  print("Warming up");
  Problem wu = getProblem(config[0]);
  
  for (int i <- [0..warmup]) {
    int startTime = cpuTime();
    translate(i, wu, false);
    int endTime = cpuTime();
    
    print(".(<(endTime - startTime) / 1000000>)");
  }
   
  println("done.");
  
  clearMemoCache(alleAlleMods());

  map[int, list[Statistic]] benchmarkResult = ();
  
  for (int i <- [0..size(config)]) {
    print("Starting \'<projectName>\' run with config \'<config[i]>\'");
    benchmarkResult[i] = [];
        
    Problem p = getProblem(config[i]);
    
    for (int r <- [0..runsPerConfig]) {
      benchmarkResult[i] += translate(config[i], p, shouldSolve);    
      print(".");
    }
    print("done\n");
  } 
    
  println("Completely done, saving to csv");
  saveToCSV(benchmarkResult, part, projectName, config, shouldSolve);
}

private void saveToCSV(map[int, list[Statistic]] benchmarkTimes, str part, str projectName, list[int] config, bool shouldSolve) {
  map[int, Statistic] aggregated = aggregate(benchmarkTimes);

  str csv = "#Config,TranslationTime,SolvingTime,Sat,#Vars,#Clauses <for (int i <- sort(domain(aggregated))) {>
            '<aggregated[i].config>,<aggregated[i].translationTime>,<aggregated[i].solvingTime>,<aggregated[i].sat>,<aggregated[i].vars>,<aggregated[i].clauses><}>";

  loc csvLoc = getCsvLoc(part, projectName, config, shouldSolve);
            
  writeFile(csvLoc, csv);
}

private loc getCsvLoc(str part, str projectName, list[int] config, bool shouldSolve) {
  loc base = |project://allealle-benchmark/results/<part>/<projectName>/|;
  str fileName = "allealle_<shouldSolve ? "with" : "without">_solve_<config[0]>-<config[-1]>.csv";
  
  return base + fileName;
}

private int countPrimVars(Environment env) = size(collectSMTVars(env));

@memo
private int countClauses(TranslationResult r) {
  int nrOfClauses = 0;
  visit(r.form) {
    case Formula _ : nrOfClauses += 1;
  }
  
  return nrOfClauses;
}

private Statistic translate(int config, Problem p, bool shouldSolve) {
  clearMemoCache(alleAlleMods());
  
  int startTime = cpuTime();
  Environment env = createInitialEnvironment(p);
  TranslationResult r = translateProblem(p, env, logIndividualFormula = false);
  int endTime = cpuTime(); 
  
  int translationTime = (endTime - startTime) / 1000000; 
  int primVars = countPrimVars(env);
  int clauses = countClauses(r);
  
  if (shouldSolve) {
    ModelFinderResult mfr = runInSolver(p, r, env, translationTime, 0, log = false);
    
    if (sat(Model currentModel, Model (Domain) nextModel, void () stop) := mfr) {
      stop();
    } 
    
    return <config, translationTime, mfr.solvingTime, (sat(_,_,_) := mfr) || (trivialSat(_) := mfr), primVars, clauses>;        
  } else {
    return <config, translationTime, 0, false, primVars, clauses>;
  } 
}

private list[Statistic] sortByTranslationTime(list[Statistic] stats) 
  = sort(stats, bool (Statistic a, Statistic b) { return a.translationTime < b.translationTime;});

private list[Statistic] sortBySolvingTime(list[Statistic] stats) 
  = sort(stats, bool (Statistic a, Statistic b) { return a.solvingTime < b.solvingTime;});

private map[int, Statistic] aggregate(map[int, list[Statistic]] benchmarkTimes) {
  map[int, Statistic] aggregated = ();
     
  for (int i <- benchmarkTimes) {
    list[Statistic] sortedByTrans = sortByTranslationTime(benchmarkTimes[i]);
    list[Statistic] sortedBySolve = sortBySolvingTime(benchmarkTimes[i]);
    
    // get the mean
    int transMean;
    int solvingMean;
    
    if (size(sortedByTrans)%2 == 0) {
      transMean = (sortedByTrans[size(sortedByTrans)/2].translationTime + sortedByTrans[size(sortedByTrans)/2 + 1].translationTime) / 2;
      solvingMean = (sortedBySolve[size(sortedBySolve)/2].solvingTime + sortedBySolve[size(sortedBySolve)/2 + 1].solvingTime) / 2;
    } else {
      transMean = sortedByTrans[size(sortedByTrans)/2].translationTime;
      solvingMean = sortedBySolve[size(sortedBySolve)/2].solvingTime;
    } 
    Statistic s = getOneFrom(benchmarkTimes[i]);

    aggregated[i] = <s.config, transMean, solvingMean, s.sat, s.vars, s.clauses>;           
  }
  
  return aggregated;
}
