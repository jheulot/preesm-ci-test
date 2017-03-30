/*******************************************************************************
 * Copyright or © or Copr. %%LOWERDATE%% - %%UPPERDATE%% IETR/INSA:
 *
 * %%AUTHORS%%
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

package org.ietr.preesm.mapper.abc.edgescheduling;

import org.ietr.preesm.mapper.abc.order.OrderManager;

/**
 * Methods common to every edge schedulers
 * 
 * @author mpelcat
 */
public abstract class AbstractEdgeSched implements IEdgeSched {

	/**
	 * Contains the rank list of all the vertices in an implementation
	 */
	protected OrderManager orderManager = null;

	public AbstractEdgeSched(OrderManager orderManager) {
		super();
		this.orderManager = orderManager;
	}

	/**
	 * Gets the edge scheduler from an edge scheduler type
	 */
	public static IEdgeSched getInstance(EdgeSchedType edgeSchedType,
			OrderManager orderManager) {

		AbstractEdgeSched edgeSched = null;

		if (edgeSchedType == EdgeSchedType.Simple) {
			edgeSched = new SimpleEdgeSched(orderManager);
		} else if (edgeSchedType == EdgeSchedType.Switcher) {
			edgeSched = new SwitcherEdgeSched(orderManager);
		} else if (edgeSchedType == EdgeSchedType.Advanced) {
			edgeSched = new AdvancedEdgeSched(orderManager);
		} else {
			// Default scheduler
			edgeSched = new SimpleEdgeSched(orderManager);
		}

		return edgeSched;
	}

	@Override
	public OrderManager getOrderManager() {
		return orderManager;
	}
}
