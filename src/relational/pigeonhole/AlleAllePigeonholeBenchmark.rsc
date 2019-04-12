module relational::pigeonhole::AlleAllePigeonholeBenchmark
extend AlleAlleBenchmark;

void runPigeonholeBenchmark()  
  = runBenchmark([10..11], "pigeonhole", false); 

str constructRels(int config)
  = "Pigeon (pId: id)           =  {<intercalate(",", ["\<p<i>\>" | int i <- [0..config]])>}
    'Hole   (hId: id)           =  {<intercalate(",", ["\<h<i>\>" | int i <- [0..config-1]])>}
    'nest   (pId: id, hId: id) \<= {<intercalate(",", ["\<p<i>,h<j>\>" | int i <- [0..config], int j <- [0..config-1]])>}";
    
str getConstraints(int config) 
  = "nest in Pigeon x Hole
    'forall h:Hole   | lone h |x| nest
    'forall p:Pigeon | one  p |x| nest";
