package cop;

/**
 * This file is part of samples, https://github.com/chocoteam/samples
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
/**
 * A class that provides a pattern to declare a model and solve it. <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */

import org.chocosolver.pf4cs.IProblem;
import org.chocosolver.pf4cs.SetUpException;
import org.chocosolver.solver.Model;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import static java.lang.Runtime.getRuntime;

public abstract class AbstractChocoProblem implements IProblem<Model> {

	/**
	 * A seed for random purpose
	 */
	@Option(name = "-seed", usage = "Seed for Shuffle propagation engine.", required = false)
	protected long seed = 29091981;

	/**
	 * Declared problem
	 */
	protected Model model;

	private boolean userInterruption = true;

	@Override
	public void setUp(String... args) throws SetUpException {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java " + this.getClass() + " [options...]");
			parser.printUsage(System.err);
			System.err.println();
			throw new SetUpException("Invalid problem options");
		}
	}

	/**
	 * @return the current model
	 */
	@Override
	public Model getModel() {
		return model;
	}

	/**
	 * Call search configuration
	 */
	@Override
	public void configureSearch() {
	}

	@Override
	public void tearDown() {
	}

	/**
	 * @deprecated
	 * @see #setUp(String...)
	 */
	@Deprecated
	public final boolean readArgs(String... args) {
		try {
			setUp(args);
		} catch (SetUpException e) {
			return false;
		}
		return true;
	}

	private boolean userInterruption() {
		return userInterruption;
	}

	/**
	 * Main method: from argument reading to resolution.
	 * <ul>
	 * <li>read program arguments</li>
	 * <li>build the model</li>
	 * <li>configure the search</li>
	 * <li>launch the resolution</li>
	 * </ul>
	 *
	 * @param args list of arguments to pass to the problem
	 */
	public final void execute(String... args) {
		try {
			this.setUp(args);
		} catch (SetUpException e) {
			return;
		}
		this.buildModel();
		this.configureSearch();

		Thread statOnKill = new Thread() {
			public void run() {
				if (userInterruption()) {
					System.out.println(model.getSolver().getMeasures().toString());
				}
			}
		};

		getRuntime().addShutdownHook(statOnKill);

		this.solve();
		userInterruption = false;
		getRuntime().removeShutdownHook(statOnKill);
	}

}
