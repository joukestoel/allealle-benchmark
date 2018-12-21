/**
 * This file is part of samples, https://github.com/chocoteam/samples
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package cop.mario.choco;

import static org.chocosolver.util.tools.ArrayUtils.flatten;

import java.util.HashSet;
import java.util.Set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import cop.AbstractChocoProblem;

/**
 *
 * <h3>Path Constraint Problem</h3>
 *
 * <h2>The Problem</h2> The path problem a simple problem where we are trying to
 * find the best path in a directed graph. A path is a succession of node
 * starting from the source node to the destination node. Each node has to be
 * connected to the previous and the next node by a a directed edge in the
 * graph. The path can't admit any cycle </br>
 *
 * <h2>The Model</h2> This model will specify all the constraints to make a
 * coherent path in the graph. The graph is both represented by a BoolVar matrix
 * and an array of IntVar (successor representation). For instance, if the
 * boolean (i, j) of the matrix is equals to true, that means that the path
 * contains this edge starting from the node of id i to the node of id j. If the
 * edge doesn't exists in the graph the the boolean value is equals to the Choco
 * constants FALSE. Else the edge can be used or not, so its representation is a
 * choco boolean variable which can be instantiated to true : edge is in the
 * path or to false : the edge isn't in the path.
 *
 * <h2>The Example</h2> We are presenting this problem through an little example
 * of Mario's day. </br>
 * Mario is an Italian Plumber and is work is mainly to find gold in the
 * plumbing of all the houses of the neighborhood. Mario is moving in the city
 * using his kart that has a specified amount of fuel. Mario starts his day of
 * work from his personal house and always end to his friend Luigi's house to
 * have the supper. The problem here is to plan the best path for Mario in order
 * to earn the more money with the amount of fuel of his kart !
 * </p>
 * (Version 1.3) We are making the analogy of this problem to the knapsack
 * problem. In fact we want to found a set of edges that form a path where Mario
 * can find the more gold and respects the fuel limit constraint. The analogy is
 * the following :
 * <ul>
 * <li>The weight is the consumption to go through the edge</li>
 * <li>The energy is the gold that we can earn on the house at the end of the
 * edge</li>
 * </ul>
 *
 * @author Amaury Ollagnier, Jean-Guillaume Fages
 * @since 21/05/2013
 */
public class MarioKart extends AbstractChocoProblem {

	// CONSTANTS

	private static int MARIO_HOUSE_ID = 0;// Integer.MAX_VALUE;
	/** The Luigi's house id. Random generation if equals to Integer.MAX_VALUE */
	private static int LUIGI_HOUSE_ID = 1;// Integer.MAX_VALUE;
	/** The amount of fuel of the kart in mini-litres */
	private static int FUEL = 350;

	// INSTANCES VARIABLES

	private PROBLEM currentProblem = PROBLEM.SIMPLE;

	/** The dimension of the graph i.e. the number of nodes in the graph */
	private int n;
	/** The source node id */
	private int s;
	/** The destination node id */
	private int t;
	/**
	 * The matrix of consumptions the keeps the number of mini-litres needed to go
	 * to one house to another
	 */
	private int[][] consumptions;
	/** The amount of gold that Mario can find in each house */
	private int[] gold;

	/**
	 * The boolean matrix represents all the edges in the graph, and if the boolean
	 * (i, j) of the matrix is equals to true, that means that the path contains
	 * this edge starting from the node of id i to the node of id j. If the edge
	 * doesn't exists in the graph the the boolean value is equals to the Choco
	 * constants FALSE. Else the edge can be used or not, so it representation is a
	 * choco boolean variable which can be instantiated to true : edge is in the
	 * path or to false : the edge isn't in the path.
	 */
	private BoolVar[][] edges;

	/**
	 * The next value table. The next variable of a node is the id of the next node
	 * in the path + an offset. If the node isn't used, the next value is equals to
	 * the current node id + the offset
	 */
	private IntVar[] next;

	/** Integer Variable which represents the overall size of the path founded */
	private IntVar size;

	/**
	 * All the gold that Mario has found on the path : the objective variable of the
	 * problem
	 */
	private IntVar goldFound;

	/** The consumption of the Mario's Kart in the path */
	private IntVar fuelConsumed;

	// METHODS

	@Override
	public void buildModel() {
		model = new Model();
		data();
		variables();
		constraints();
		strengthenFiltering();
	}

	@Override
	public void configureSearch() {
		/* Listeners */
		model.getSolver();
	}

	@Override
	public void solve() {
		model.setObjective(true, goldFound);
		while (model.getSolver().solve()) {
			prettyOut();
		}
		
		printInputData();
	}

	private void prettyOut() {
		/* log out the solution of the problem founded */
		System.out.println(
				size.getValue() + " houses visited");
		System.out.println(fuelConsumed.getValue() + " fuel burned");
		System.out.println("! " + goldFound.getValue() + " gold coins earned !");
		
		printRoute();
	}

	private void printRoute() {
		int currentHouse = MARIO_HOUSE_ID;
		int nextHouse = next[MARIO_HOUSE_ID].getValue();
		
		Set<Integer> visited = new HashSet<>();
		while (!visited.contains(currentHouse)) {
			visited.add(currentHouse);
			System.out.printf("%d -> %d\n", currentHouse+1, nextHouse+1);
			
			currentHouse = nextHouse;
			nextHouse = next[currentHouse].getValue();
		}
	}
	
	private void printInputData() {
		System.out.println("nbHouses = " + currentProblem.nrOfHouses + ";");
		System.out.println("MarioHouse = " + MARIO_HOUSE_ID + ";");
		System.out.println("LuigiHouse = " + LUIGI_HOUSE_ID + ";");
		System.out.println("fuelMax = " + FUEL + ";");
		System.out.println("goldTotalAmount = " + currentProblem.getTotalAmountOfGold() + ";");
		String conso = "conso = [";
		for (int i = 0; i < currentProblem.nrOfHouses; i++) {
			String s = "|";
			for (int j = 0; j < currentProblem.nrOfHouses - 1; j++) {
				s += this.consumptions[i][j] + ",";
			}
			conso += s + this.consumptions[i][currentProblem.nrOfHouses - 1];
		}
		conso += "|];";
		System.out.println(conso);
		String goldInHouse = "goldInHouse = [";
		for (int i = 0; i < currentProblem.nrOfHouses - 1; i++) {
			goldInHouse += this.gold[i] + ",";
		}
		goldInHouse += this.gold[currentProblem.nrOfHouses - 1] + "];";
		System.out.println(goldInHouse);
	}

	/** Creation of the problem instance */
	private void data() {
		/* Data of the town */
		consumptions = currentProblem.fuelConsumption;
		gold = currentProblem.goldPerHouse;

		/* The basics variables of the graph */
		this.n = currentProblem.nrOfHouses;
		this.s = MARIO_HOUSE_ID;
		this.t = LUIGI_HOUSE_ID;
	}

	/** Creation of CP variables */
	private void variables() {
		/* Choco variables */
		fuelConsumed = model.intVar("Fuel Consumption", 0, FUEL, true);
		goldFound = model.intVar("Gold Found", 0, currentProblem.getTotalAmountOfGold(), true);
		/* Initialisation of the boolean matrix */
		edges = model.boolVarMatrix("edges", n, n);
		/* Initialisation of all the next value for each house */
		next = model.intVarArray("next", n, 0, n - 1, false);
		/* Initialisation of the size variable */
		size = model.intVar("size", 2, n, true);
	}

	/** Post all the constraints of the problem */
	private void constraints() {
		/*
		 * The scalar constraint to compute global consumption of the kart to perform
		 * the path
		 */
		model.scalar(flatten(edges), flatten(consumptions), "=", fuelConsumed).post();

		/*
		 * The scalar constraint to compute the amount of gold founded by Mario in the
		 * path. With our model if a node isn't used then his next value is equals to
		 * his id. Then the boolean edges[i][i] is equals to true
		 */
		BoolVar[] used = new BoolVar[n];
		for (int i = 0; i < used.length; i++)
			used[i] = edges[i][i].not();
		model.scalar(used, gold, "=", goldFound).post();

		/*
		 * The subCircuit constraint. This forces all the next value to form a circuit
		 * which the overall size is equals to the size variable. This constraint check
		 * if the path contains any sub circles.
		 */
		model.subCircuit(next, 0, size).post();

		/*
		 * The path has to end on the t node. This constraint doesn't create a path, but
		 * a circle or a circuit. So we force the edge (t,s) then all the other node of
		 * the circuit will form a starting from s and ending at t
		 */
		model.arithm(next[t], "=", s).post();

		/*
		 * The boolean channeling constraint. Enforce the relation between the next
		 * values and the edges values in the graph boolean variable matrix
		 */
		for (int i = 0; i < n; i++) {
			model.boolsIntChanneling(edges[i], next[i], 0).post();
		}
	}

	/** Adds more constraints to get a stronger filtering */
	private void strengthenFiltering() {
		/*
		 * FUEL RELATED FILTERING: identifies the min/max fuel consumption involved by
		 * visiting each house
		 */
		IntVar[] fuelHouse = new IntVar[currentProblem.nrOfHouses];
		for (int i = 0; i < currentProblem.nrOfHouses; i++) {
			fuelHouse[i] = model.intVar("fuelHouse", 0, FUEL, false);
			model.element(fuelHouse[i], consumptions[i], next[i], 0).post();
		}
		model.sum(fuelHouse, "=", fuelConsumed).post();

		/*
		 * GOLD RELATED FILTERING This problem can be seen has a knapsack problem where
		 * are trying to found the set of edges that contains the more golds and
		 * respects the fuel limit constraint. The analogy is the following : the weight
		 * is the consumption to go through the edge and the energy is the gold that we
		 * can earn
		 */
//		int[][] goldMatrix = new int[n][n];
//		for (int i = 0; i < goldMatrix.length; i++) {
//			for (int j = 0; j < goldMatrix.length; j++) {
//				goldMatrix[i][j] = (i == j) ? 0 : gold[i];
//			}
//		}
//		
//		model.knapsack(flatten(edges), model.intVar(FUEL), goldFound, flatten(consumptions), flatten(goldMatrix)).post();
	}

	// LAUNCHER

	/**
	 * The main to execute
	 *
	 * @param args arguments
	 */
	public static void main(String[] args) {
		new MarioKart().execute(args);
	}

	private enum PROBLEM {
		SIMPLE( 6, 
				new int[] { /* mario */0, /* luigi */0, /* h3 */40, /* h4 */67, /* h5 */89, /* h6 */50 },
				new int[][] { 
						/* mario */{ 0, 221, 274, 808, 13, 677 }, 
						/* luigi */{ 0, 0, 702, 83, 813, 679 },
						/* h3 */{ 274, 702, 0, 127, 110, 72 },
						/* h4 */{ 808, 83, 127, 0, 717, 80 },
						/* h5 */{ 13, 813, 110, 717, 0, 951 },
						/* h6 */{ 677, 679, 72, 80, 951, 0 } });

		private final int nrOfHouses;
		private final int[] goldPerHouse;
		private final int[][] fuelConsumption;

		private PROBLEM(int nrOfHouses, int[] goldPerHouse, int[][] fuelConsumption) {
			this.nrOfHouses = nrOfHouses;
			this.goldPerHouse = goldPerHouse;
			this.fuelConsumption = fuelConsumption;
		}

		int getTotalAmountOfGold() {
			int total = 0;
			for (int gold : goldPerHouse) {
				total += gold;
			}

			return total;
		}
	}

}