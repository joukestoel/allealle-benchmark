module relational::square::allealle::AlleAlleSquareBenchmark

extend AlleAlleBenchmark;

void runSquareBenchmark() 
  = runBenchmark([4..11], "square", true);

str constructRels(int config) 
  = "Num (nId: id, val: int) = {\<n1,?\>,\<n2,?\>}";
   
str getConstraints(int config) 
  = "∀ n ∈ Num | some n where val \> <config>
    '∃ n1 ∈ Num, n2 ∈ Num ∖ n1 | some (n1 ⨯ n2[nId as n2Id, val as val2]) where val = val2 * val2";
