module cop::knapsack::allealle::AlleAlleKnapsackBenchmark

extend AlleAlleBenchmark;

void runKnapsackBenchmark() = runBenchmark([1], "cop", "knapsack", true);

str constructRels(int config) 
  = "sack(maxWeight:int, totalWeight:int, totalPrice:int)      = {\<550, ?, ?\>}
    'package(pId:id, price:int, weight:int, maxItems:int)      = {\<a,100,0,42\>,\<b,49,25,22\>,\<c,54,0,42\>,\<d,12,41,14\>,\<e,78,94,6\>,\<f,30,75,8\>,\<g,65,40,14\>,\<h,31,59,10\>,\<i,90,95,6\>,\<j,50,99,6\>}
    'content(pId:id, quantity: int, tPrice: int, tWeight:int) \<= {\<a,?,?,?\>,\<b,?,?,?\>,\<c,?,?,?\>,\<d,?,?,?\>,\<e,?,?,?\>,\<f,?,?,?\>,\<g,?,?,?\>,\<h,?,?,?\>,\<i,?,?,?\>,\<j,?,?,?\>}";
  
str getConstraints(int config)
  = "∀ c ∈ content | some (c ⨝ package) where (quantity \>=0 && quantity \<= maxItems && 
    '                                      tPrice  = quantity * price && 
    '                                      tWeight = quantity * weight)
    '
    'some (sack ⨯ (content ⨝ package)[sum(tPrice) as calPrice, sum(tWeight) as calWeight]) 
    '  where totalWeight = calWeight && totalPrice=calPrice && totalWeight \<= maxWeight
    '
    'objectives: maximize sack[totalPrice]";