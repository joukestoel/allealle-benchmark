sig Num {
  value: Int
}

fact {
  all n:Num | n.value > 21
  some n: Num, n': Num-n | 
    n'.value = mul[n.value,n.value]
}

pred show{}

run show for 2 but 10 Int
