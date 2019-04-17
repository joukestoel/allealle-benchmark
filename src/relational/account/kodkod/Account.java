package relational.account.kodkod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kodkod.ast.Expression;
import kodkod.ast.Formula;
import kodkod.ast.IntConstant;
import kodkod.ast.IntExpression;
import kodkod.ast.Relation;
import kodkod.ast.Variable;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.engine.config.Options.IntEncoding;
import kodkod.engine.config.Options.OverflowPolicy;
import kodkod.instance.Bounds;
import kodkod.instance.TupleFactory;
import kodkod.instance.Universe;
import relational.util.Math;

public class Account {
	private Relation state, initialState, account, withdraw, deposit;
	private Relation ordering, amount, balance, accountInState, triggeredEvent;
	
	public Account() {
		state = Relation.unary("State");
		initialState = Relation.unary("InitialState");
		account = Relation.unary("Account");
		withdraw = Relation.unary("Withdraw");
		deposit = Relation.unary("Deposit");
		
		ordering = Relation.binary("ordering");
		balance = Relation.binary("balance");
		amount = Relation.binary("amount");
		accountInState = Relation.binary("accountInState");
		triggeredEvent = Relation.binary("triggeredEvent");
	}
	
	public Bounds bounds(int bitWidth) {
		int nums = Math.pow(2, bitWidth-1);
		
		List<String> atoms = new ArrayList<>();
		atoms.addAll(Arrays.asList("s1","s2","s3","s4","s5"));
		atoms.addAll(Arrays.asList("ac1","ac2","ac3","ac4","ac5"));
		atoms.addAll(Arrays.asList("withdraw", "deposit"));
		
		for (int i = -nums; i < nums; i++) {
			atoms.add("" + i);
		}
		
		final Universe uni = new Universe(atoms);
		final TupleFactory tf = uni.factory();
		final Bounds b = new Bounds(uni);

		b.bound(state, tf.setOf(tf.tuple("s1")), tf.range(tf.tuple("s1"), tf.tuple("s5")));
		b.boundExactly(initialState, tf.setOf(tf.tuple("s1")));
		b.bound(account, tf.setOf(tf.tuple("ac1")), tf.range(tf.tuple("ac1"), tf.tuple("ac5")));
		b.boundExactly(withdraw, tf.setOf(tf.tuple("withdraw")));
		b.boundExactly(deposit, tf.setOf(tf.tuple("deposit")));
		
		for (int i = -nums; i < nums; i++) {
			b.boundExactly(i, tf.setOf(tf.tuple("" + i)));
		}
		
		b.bound(ordering, tf.setOf(tf.tuple("s1","s2"), tf.tuple("s2","s3"), tf.tuple("s3","s4"), tf.tuple("s4","s5")));
		
		b.bound(accountInState, tf.setOf(tf.tuple("s1","ac1")), tf.setOf(tf.tuple("s1","ac1"), tf.tuple("s2","ac2"), tf.tuple("s3","ac3"), tf.tuple("s4","ac4"), tf.tuple("s5","ac5")));
		b.bound(triggeredEvent, b.upperBound(state).product(tf.setOf(tf.tuple("withdraw"), tf.tuple("deposit"))));

		b.bound(amount, b.upperBound(state).product(tf.range(tf.tuple("" + (-nums)), tf.tuple("" + (nums-1)))));
		b.bound(balance, b.upperBound(account).product(tf.range(tf.tuple("" + (-nums)), tf.tuple("" + (nums-1)))));
		
//		System.out.println(b);
		
		return b;
	}
	
	public Formula constraints(int bitWidth) {
		return typeConstraints().and(cardinalities()).and(amountConstraints(bitWidth))
				.and(initialState()).and(transitionFunction()).and(goal(bitWidth));
	}
	
	public Formula cardinalities() {
		Variable s = Variable.unary("s");
		Formula f1 = s.join(accountInState).some().forAll(s.oneOf(state));
		
		Formula f2 = state.in(initialState.join(ordering.reflexiveClosure()));
		Formula f3 = account.in(initialState.join(ordering.reflexiveClosure()).join(accountInState));
		
		return f1.and(f2).and(f3);
	}
	
	public Formula amountConstraints(int bitWidth) {
		int max = Math.pow(2, bitWidth-3);
		
		Variable s = Variable.unary("s");
		IntExpression am = s.join(amount).sum();
		return am.gt(IntConstant.constant(0)).and(am.lte(IntConstant.constant(max))).forAll(s.oneOf(state));
	}
	
	public Formula typeConstraints() {
		Formula f1 = amount.function(state, Expression.INTS);
		Formula f2 = balance.function(account, Expression.INTS);
		Formula f3 = ordering.in(state.product(state));
		
		return f1.and(f2).and(f3);
	}
	
	public Formula initialState() {
		return initialState.join(accountInState).join(balance).sum().eq(IntConstant.constant(0));
	}
	
	public Formula transitionFunction() {
		Variable cur = Variable.unary("cur");
		Variable nxt = Variable.unary("nxt");
		
		return cur.product(nxt).in(ordering)
				.implies(deposit(cur,nxt).or(withdraw(cur, nxt)))
				.forAll(cur.oneOf(state).and(nxt.oneOf(state)));
	}
	
	public Formula deposit(Variable cur, Variable nxt) {
		IntExpression curBalance = cur.join(accountInState).join(balance).sum();
		IntExpression nxtBalance = nxt.join(accountInState).join(balance).sum();
		IntExpression curAmount = nxt.join(amount).sum();
		
		return nxtBalance.eq(curBalance.plus(curAmount))
				.and(nxt.join(triggeredEvent).eq(deposit));
	}
	
	public Formula withdraw(Variable cur, Variable nxt) {
		IntExpression curBalance = cur.join(accountInState).join(balance).sum();
		IntExpression nxtBalance = nxt.join(accountInState).join(balance).sum();
		IntExpression curAmount = nxt.join(amount).sum();
	
		return curBalance.minus(curAmount).gte(IntConstant.constant(0))
				.and(nxtBalance.eq(curBalance.minus(curAmount)))
				.and(nxt.join(triggeredEvent).eq(withdraw));
	}
	
	public Formula goal(int bitWidth) {
		int goal = Math.pow(2, bitWidth-1)-bitWidth;

		Variable s = Variable.unary("s");
		
		return s.join(accountInState).join(balance).sum()
				.gte(IntConstant.constant(goal))
				.forSome(s.oneOf(state));
	}
	
	public static void main(String[] args) {
		Account problem = new Account();
		final Solver solver = new Solver();

		int bitWidth = 8;
		
		solver.options().setLogTranslation(1);
		solver.options().setBitwidth(bitWidth);
		solver.options().setIntEncoding(IntEncoding.TWOSCOMPLEMENT);
		solver.options().setOverflowPolicy(OverflowPolicy.PREVENT);
		
		final Bounds b = problem.bounds(bitWidth);
		final Formula f = problem.constraints(bitWidth);

		System.out.println(f);
		
		Solution sol = solver.solve(f,b);
		
		if (sol.sat()) {
			System.out.println(sol.instance());
		} else {
			System.out.println("Not sat");
		}
		
	}
}
