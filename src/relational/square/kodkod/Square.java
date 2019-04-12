package relational.square.kodkod;

import java.util.ArrayList;
import java.util.List;

import kodkod.ast.Formula;
import kodkod.ast.IntConstant;
import kodkod.ast.Relation;
import kodkod.ast.Variable;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.engine.config.Options.IntEncoding;
import kodkod.engine.config.Options.OverflowPolicy;
import kodkod.instance.Bounds;
import kodkod.instance.TupleFactory;
import kodkod.instance.Universe;

public class Square {
	private Relation num;
	private Relation val;
	
	private int numRange;
	
	public Square() {
		num = Relation.unary("Num");
		val = Relation.binary("val");
		
		numRange = 4;
	}
	
	public int getNumRange() {
		return numRange;
	}
	
	public Bounds bounds(int n) {
		this.numRange = pow(2,n-1);
		
		final List<String> atoms = new ArrayList<>();
		atoms.add("n1");
		atoms.add("n2");
		for (int i = -numRange; i < numRange; i++) {
			atoms.add("" + i);
		}
		final Universe u = new Universe(atoms);
		final TupleFactory f = u.factory();
		
		final Bounds b = new Bounds(u);
		
		b.boundExactly(num, f.setOf(f.tuple("n1"), f.tuple("n2")));
		b.bound(val, b.upperBound(num).product(f.range(f.tuple("" + (-numRange)), f.tuple("" + (numRange-1)))));

		for (int i = -numRange; i < numRange; i++) {
			b.boundExactly(i, f.setOf("" + i));
		}
		
		return b;
	}
	
	private int pow(int a, int b) {
	    if (b == 0)        return 1;
	    if (b == 1)        return a;
	    if (b % 2 == 0)    return     pow ( a * a, b/2); //even a=(a^2)^b/2
	    else               return a * pow ( a * a, b/2); //odd  a=a*(a^2)^b/2
	}
	
	public Formula constraints() {
		Variable n = Variable.unary("n");
		Formula f1 = n.join(val).one().forAll(n.oneOf(num));
		
		Variable n1 = Variable.unary("n1");
		Variable n2 = Variable.unary("n2");
		
		Formula f2 = n1.join(val).sum().eq(n2.join(val).sum().multiply(n2.join(val).sum()))
				.forSome(n1.oneOf(num).and(n2.oneOf(num.difference(n1))));

		Formula f3 = n.join(val).sum().gt(IntConstant.constant(2)).forAll(n.oneOf(num));
		
		return f1.and(f2).and(f3);
	}
	
	public static void main(String[] args) {
		Square problem = new Square();
		final Solver solver = new Solver();

		int bitWith = 5;
		
		solver.options().setLogTranslation(1);
		solver.options().setBitwidth(bitWith);
//		solver.options().setSymmetryBreaking(8);
		solver.options().setIntEncoding(IntEncoding.TWOSCOMPLEMENT);
		solver.options().setOverflowPolicy(OverflowPolicy.PREVENT);
		
		final Bounds b = problem.bounds(bitWith);
		final Formula f = problem.constraints();

		Solution sol = solver.solve(f,b);
		
		if (sol.sat()) {
			System.out.println(sol.instance());
		} else {
			System.out.println("Not sat");
		}
		
	}
}
