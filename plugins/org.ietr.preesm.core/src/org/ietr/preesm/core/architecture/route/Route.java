/*******************************************************************************
 * Copyright or © or Copr. 2012 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Maxime Pelcat <Maxime.Pelcat@insa-rennes.fr> (2012)
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package org.ietr.preesm.core.architecture.route;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.ietr.dftools.architecture.slam.ComponentInstance;

/**
 * A route contains several Route Steps. It links operators. To operators
 * directly connected have a route with one route step.
 * 
 * @author mpelcat
 */
public class Route extends ArrayList<AbstractRouteStep> {

	/**
	 * ID used to reference the element in a property bean
	 */
	public static final String propertyBeanName = "route";

	public static final int averageTransfer = 1000;

	private static final long serialVersionUID = 1L;

	public Route(AbstractRouteStep step) {
		super();
		this.add(step);
	}

	public Route(Route r1, Route r2) {
		super();
		for (AbstractRouteStep step : r1) {
			this.add(step);
		}
		for (AbstractRouteStep step : r2) {
			this.add(step);
		}
	}

	public Route() {
		super();
	}

	/**
	 * Evaluates the cost of a data transfer with size transferSize along the
	 * route
	 */
	public long evaluateTransferCost(long transferSize) {
		long cost = 0;
		// Iterating the route and incrementing transfer cost
		for (AbstractRouteStep step : this) {
			cost += step.getTransferCost(transferSize);
		}

		return cost;
	}

	/**
	 * Returns true if each operator in the route appears only once
	 */
	public boolean isSingleAppearance() {
		boolean isIt = true;
		Set<ComponentInstance> opSet = new HashSet<ComponentInstance>();
		// Iterating the route and testing number of occurences in sender
		for (AbstractRouteStep step : this) {
			if (opSet.contains(step.getSender())) {
				isIt = false;
			}
			opSet.add(step.getSender());
		}

		// Testing last step receiver
		if (opSet.contains(this.get(this.size() - 1).getReceiver())) {
			isIt = false;
		}
		return isIt;
	}

	@Override
	public String toString() {
		String trace = "";
		for (AbstractRouteStep step : this) {
			trace += step + " ";
		}
		return trace;
	}
}
