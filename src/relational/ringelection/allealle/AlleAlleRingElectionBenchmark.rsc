module relational::ringelection::allealle::AlleAlleRingElectionBenchmark

extend AlleAlleBenchmark;

void runRingElectionBenchmark() 
  = runBenchmark([3..5], "ringelection", true);

str constructRels(int config)
  = "Time (tId: id)                   = {<intercalate(",",["\<t<i>\>" | int i <- [0..config]])>}
    'tord(cur:id, next:id)            = {<intercalate(",",["\<t<i>,t<i+1>\>" | int i <- [0..config-1]])>}
    'tfirst(tId: id)                  = {\<t1\>}
    '
    'Process (pId: id, token:int)     = {<intercalate(",",["\<p<i>,<i>\>" | int i <- [0..config]])>}
    '
    'succ (from:id, to:id)            \<= {<intercalate(",",["\<p<i>,p<j>\>" | int i <- [0..config], int j <- [0..config]])>}
    'tokenSend (pId:id, tokenId:id, tId:id) \<= {<intercalate(",",["\<p<i>,p<j>,t<k>\>" | int i <- [0..config], int j <- [0..config], int k <- [0..config]])>}
    'elected (pId:id, tId:id)         \<= {<intercalate(",",["\<p<i>,t<j>\>" | int i <- [0..config], int j <- [0..config]])>}";

str getConstraints(int config) 
  = "// Successor is a function
    'succ ⊆ Process[pId -\> from] ⨯ Process[pId -\> to]
    '∀ p ∈ Process | one p[pId as from] ⨝ succ
    '
    'elected ⊆ Process[pId] ⨯ Time
    '
    '// fact Ring
    '∀ p ∈ Process[pId as from] | Process[pId] ⊆ (p ⨝ ^\<from,to\>succ)[to -\> pId]
    '
    '// Initialize
    'Process[pId] ⊆ (tokenSend ⨝ tfirst)[pId]
    '∀ i ∈ (tokenSend ⨝ tfirst)[pId,tokenId] | some i where pId = tokenId
    '
    '// Traces
    '∀ to ∈ tord | let t = to[cur -\> tId], t\' = to[next -\> tId] |
    '  ∀ p ∈ Process | 
    '    (let from = (p ⨝ tokenSend)[tokenId,tId], to = ((p[pId as from] ⨝ succ)[to -\> pId] ⨝ tokenSend)[tokenId,tId] |
    '        ∃ token ∈ (from ⨝ t)[tokenId] |  
    '          (from ⨝ t\')[tokenId] = (from ⨝ t)[tokenId] ∖ token ∧ 
    '          (to ⨝ t\')[tokenId] = (to ⨝ t)[tokenId] ∪ (((token ⨝ Process[pId as tokenId]) ⨯ p[token as ownToken]) where token \>= ownToken)[tokenId]  
    '    ) ∨ 
    '    (let pred = (succ ⨝ p[pId as to])[from as pId], from = (pred ⨝ tokenSend)[tokenId,tId], to = ((pred[pId as from] ⨝ succ)[to -\> pId] ⨝ tokenSend)[tokenId,tId] |
    '        ∃ token ∈ (from ⨝ t)[tokenId] |  
    '          (from ⨝ t\')[tokenId] = (from ⨝ t)[tokenId] ∖ token ∧ 
    '          (to ⨝ t\')[tokenId] = (to ⨝ t)[tokenId] ∪ (((token ⨝ Process[pId as tokenId]) ⨯ pred[token as ownToken]) where token \>= ownToken)[tokenId] 
    '    ) ∨ // skip 
    '    (let pr = p ⨝ tokenSend | (pr ⨝ t)[tokenId] = (pr ⨝ t\')[tokenId]) 
    '
    '// Define elected facts
    'no elected ⨝ tfirst
    '
    '∀ to ∈ tord | let t = to[cur -\> tId], t\' = to[next -\> tId] | 
    '  ∀ p ∈ Process[pId] | (p[pId as tokenId] ⊆ (p ⨝ tokenSend ⨝ t\')[tokenId] ∖ (p ⨝ tokenSend ⨝ t)[tokenId]) ⇔ p ⊆ (elected ⨝ t\')[pId]
    ' 
    'some elected";