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

package org.ietr.preesm.mapper.abc.impl;

import java.util.HashSet;
import java.util.Set;

import org.ietr.dftools.algorithm.model.dag.DAGEdge;
import org.ietr.dftools.algorithm.model.dag.DAGVertex;
import org.ietr.preesm.mapper.abc.order.OrderManager;
import org.ietr.preesm.mapper.abc.transaction.RemoveVertexTransaction;
import org.ietr.preesm.mapper.abc.transaction.TransactionManager;
import org.ietr.preesm.mapper.model.MapperDAG;
import org.ietr.preesm.mapper.model.MapperDAGVertex;
import org.ietr.preesm.mapper.model.special.InvolvementVertex;
import org.ietr.preesm.mapper.model.special.OverheadVertex;
import org.ietr.preesm.mapper.model.special.PrecedenceEdgeAdder;
import org.ietr.preesm.mapper.model.special.TransferVertex;

/**
 * Class cleaning an implementation i.e. removing added transfers and edges.
 * 
 * @author mpelcat
 */
public class ImplementationCleaner {

	private OrderManager orderManager;
	private MapperDAG implementation;
	private TransactionManager transactionManager;

	public ImplementationCleaner(OrderManager orderManager,
			MapperDAG implementation) {
		super();
		this.orderManager = orderManager;
		this.implementation = implementation;
		this.transactionManager = new TransactionManager();
	}

	/**
	 * Removes all transfers from routes coming from or going to vertex
	 */
	public void removeAllTransfers(MapperDAGVertex vertex) {

		for (DAGVertex v : getAllTransfers(vertex)) {
			if (v instanceof TransferVertex) {
				transactionManager.add(new RemoveVertexTransaction(
						(MapperDAGVertex) v, implementation, orderManager));

			}
		}

		transactionManager.execute();
		transactionManager.clear();
	}

	/**
	 * Removes all overheads from routes coming from or going to vertex
	 */
	public void removeAllOverheads(MapperDAGVertex vertex) {
		transactionManager.clear();

		for (DAGVertex v : getAllTransfers(vertex)) {
			if (v instanceof TransferVertex) {
				MapperDAGVertex o = ((TransferVertex) v).getPrecedingOverhead();
				if (o != null && o instanceof OverheadVertex) {
					transactionManager.add(new RemoveVertexTransaction(o,
							implementation, orderManager));
				}
			}
		}

		transactionManager.execute();
		transactionManager.clear();
	}

	/**
	 * Removes all overheads from routes coming from or going to vertex
	 */
	public void removeAllInvolvements(MapperDAGVertex vertex) {
		transactionManager.clear();

		for (DAGVertex v : getAllTransfers(vertex)) {
			if (v instanceof TransferVertex) {
				MapperDAGVertex o = ((TransferVertex) v).getInvolvementVertex();
				if (o != null && o instanceof InvolvementVertex) {
					transactionManager.add(new RemoveVertexTransaction(o,
							implementation, orderManager));
				}
			}
		}

		transactionManager.execute();
		transactionManager.clear();
	}

	/**
	 * Removes the precedence edges scheduling a vertex and schedules its
	 * successor after its predecessor.
	 */
	public void unscheduleVertex(MapperDAGVertex vertex) {

		MapperDAGVertex prev = orderManager.getPrevious(vertex);
		MapperDAGVertex next = orderManager.getNext(vertex);
		PrecedenceEdgeAdder adder = new PrecedenceEdgeAdder(orderManager,
				implementation);

		if (prev != null) {
			adder.removePrecedenceEdge(prev, vertex);
		}

		if (next != null) {
			adder.removePrecedenceEdge(vertex, next);
		}

		// System.out.println("tutu");
		Set<DAGEdge> edges = implementation.getAllEdges(prev, next);

		if ((prev != null && next != null)
				&& (edges == null || edges.isEmpty())) {
			// TODO: Remove, only for debug
			if (prev.getEffectiveOperator().getInstanceName() != next
					.getEffectiveOperator().getInstanceName()) {
				System.out.println("wrong!!");
			}
			adder.addPrecedenceEdge(prev, next);
		}

	}

	/**
	 * Gets all transfers from routes coming from or going to vertex. Do not
	 * execute if overheads are present
	 */
	public static Set<DAGVertex> getAllTransfers(MapperDAGVertex vertex) {

		Set<DAGVertex> transfers = new HashSet<DAGVertex>();

		transfers.addAll(getPrecedingTransfers(vertex));
		transfers.addAll(getFollowingTransfers(vertex));

		return transfers;
	}

	/**
	 * Gets all transfers preceding vertex. Recursive function
	 */
	public static Set<DAGVertex> getPrecedingTransfers(MapperDAGVertex vertex) {

		Set<DAGVertex> transfers = new HashSet<DAGVertex>();

		for (MapperDAGVertex v : vertex.getPredecessors(true).keySet()) {
			if (v instanceof TransferVertex) {
				transfers.add(v);
				transfers.addAll(getPrecedingTransfers(v));
			}
		}

		return transfers;
	}

	/**
	 * Gets all transfers following vertex. Recursive function
	 */
	public static Set<DAGVertex> getFollowingTransfers(MapperDAGVertex vertex) {

		Set<DAGVertex> transfers = new HashSet<DAGVertex>();

		for (MapperDAGVertex v : vertex.getSuccessors(true).keySet()) {
			if (v instanceof TransferVertex) {
				transfers.add(v);
				transfers.addAll(getFollowingTransfers(v));
			}
		}

		return transfers;
	}
}
