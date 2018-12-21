package relational.rivercrossing.kodkod;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.ast.Variable;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.instance.Bounds;
import kodkod.instance.TupleFactory;
import kodkod.instance.Universe;

public final class RiverCrossing {
	public final Relation state, startState, ordering, passenger, farmer, wolf, goat, cabbage, far, near, eats;
	
	public RiverCrossing() {
		this.state = Relation.unary("State");
		this.startState = Relation.unary("FirstState");
		this.passenger = Relation.unary("Passenger");
		this.farmer = Relation.unary("Farmer");
		this.wolf = Relation.unary("Wolf");
		this.goat = Relation.unary("Goat");
		this.cabbage = Relation.unary("Cabbage");
		
		this.ordering = Relation.binary("ordering");
		this.far = Relation.binary("far");
		this.near = Relation.binary("near");
		this.eats = Relation.binary("eats");
	}
	
	public Formula onOneShoreAtTheTime() {
		final Variable s = Variable.unary("s");
		return s.join(near).intersection(s.join(far)).no().forAll(s.oneOf(state));
	}
	
	public Formula allPassengersAreThereAllTheTime() {
		final Variable s = Variable.unary("s");
		return s.join(near).union(s.join(far)).eq(passenger).forAll(s.oneOf(state));
	}
	
	public Formula goal() {
		final Variable s = Variable.unary("s");
		return passenger.in(s.join(far)).forSome(s.oneOf(state));
	}
	
	public Formula farmerGoesFromOneShoreToTheOther() {
		final Variable s1 = Variable.unary("s1");
		final Variable s2 = Variable.unary("s2");
		
		return s1.product(s2).in(ordering).implies(farmer.in(s1.join(near)).implies(farmer.in(s2.join(far))).or(farmer.in(s1.join(far)).implies(farmer.in(s2.join(near))))).forAll(s1.oneOf(state).and(s2.oneOf(state)));
	}
	
	public Formula boatOnlyCanHoldOnePassenger() {
		final Variable s1 = Variable.unary("s1");
		final Variable s2 = Variable.unary("s2");
		
		final Variable p1 = Variable.unary("p1");
		final Variable p2 = Variable.unary("p2");
		
		return s1.product(s2).in(ordering).and(p1.eq(p2).not()).implies(
				p1.union(p2).in(s1.join(near).intersection(s2.join(far))).or(p1.union(p2).in(s1.join(far).intersection(s2.join(near)))).not())
				.forAll(s1.oneOf(state).and(s2.oneOf(state).and(p1.oneOf(passenger.difference(farmer)).and(p2.oneOf(passenger.difference(farmer))))));
	}
	
	public Formula onlyCrossWithTheFarmer() {
		Variable s1 = Variable.unary("s1");
		Variable s2 = Variable.unary("s2");
		Variable p = Variable.unary("p");
		
		return s1.product(s2).in(ordering).implies(
				p.in(s1.join(near).intersection(s2.join(far))).implies(farmer.in(s1.join(near).intersection(s2.join(far)))).and(
			    p.in(s1.join(far).intersection(s2.join(near))).implies(farmer.in(s1.join(far).intersection(s2.join(near))))))
				.forAll(s1.oneOf(state).and(s2.oneOf(state)).and(p.oneOf(passenger.difference(farmer))));
	}
	
	public Formula makeSureNoOneGetsEaten() {
		Variable s = Variable.unary("s");
		Variable p1 = Variable.unary("p1");
		Variable p2 = Variable.unary("p2");
		
		return p1.product(p2).in(eats).implies(p1.in(s.join(near)).and(p2.in(s.join(far))).or(
				p1.in(s.join(far)).and(p2.in(s.join(near))).or(p1.union(p2.union(farmer)).in(s.join(near))).or(p1.union(p2.union(farmer)).in(s.join(far)))))
				.forAll(s.oneOf(state).and(p1.oneOf(passenger)).and(p2.oneOf(passenger)));
	}

	
	public Bounds bounds() {
		final List<String> atoms = Arrays.asList("s1","s2","s3","s4","s5","s6","s7","s8","wolf","goat","cabbage","farmer");
		
		final Universe u = new Universe(atoms);
		final TupleFactory f = u.factory();
		
		final Bounds b = new Bounds(u);
		
		b.boundExactly(state, f.range(f.tuple("s1"), f.tuple("s8")));
		b.boundExactly(startState, f.setOf(f.tuple("s1")));
		b.boundExactly(ordering, f.setOf(f.tuple("s1","s2"), f.tuple("s2","s3"), f.tuple("s3","s4"), f.tuple("s4","s5"), f.tuple("s5","s6"), f.tuple("s6","s7"), f.tuple("s7","s8")));
		
		b.boundExactly(passenger, f.setOf(f.tuple("wolf"), f.tuple("goat"), f.tuple("cabbage"), f.tuple("farmer")));
		b.boundExactly(farmer, f.setOf(f.tuple("farmer")));
		b.boundExactly(wolf, f.setOf(f.tuple("wolf")));
		b.boundExactly(goat, f.setOf(f.tuple("goat")));
		b.boundExactly(cabbage, f.setOf(f.tuple("cabbage")));
		
		b.bound(near, f.setOf(f.tuple("s1","farmer"), f.tuple("s1","goat"), f.tuple("s1","wolf"), f.tuple("s1","cabbage")), b.upperBound(state).product(b.upperBound(passenger)));
		b.bound(far, b.upperBound(state).product(b.upperBound(passenger)));
		
		b.boundExactly(eats, f.setOf(f.tuple("goat","cabbage"), f.tuple("wolf","goat")));
		
		return b;
	}
		
	public Formula constraints() {
		return onOneShoreAtTheTime()
				.and(allPassengersAreThereAllTheTime())
				.and(farmerGoesFromOneShoreToTheOther())
				.and(boatOnlyCanHoldOnePassenger())
				.and(onlyCrossWithTheFarmer())
				.and(makeSureNoOneGetsEaten())
				.and(goal());
	}
	
	public static final void main(String[] args) {
		final RiverCrossing problem = new RiverCrossing();
		
		final Solver solver = new Solver();

		solver.options().setLogTranslation(1);
		solver.options().setSymmetryBreaking(0);
		
		final Formula f = problem.constraints();
		final Bounds b = problem.bounds();

		Iterator<Solution> sols = solver.solveAll(f, b);
		int nrOfSols = 0;
		
		while (sols.hasNext()) {
			Solution sol = sols.next();
			if (sol.sat()) {
				nrOfSols += 1;
			}
		}
		
		System.out.println("Nr of solutions: " + nrOfSols);
	}
	
}


