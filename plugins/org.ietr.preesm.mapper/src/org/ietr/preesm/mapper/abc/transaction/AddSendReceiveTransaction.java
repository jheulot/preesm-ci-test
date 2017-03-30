/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.mapper.abc.transaction;

import java.util.List;
import java.util.logging.Level;

import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.mapper.abc.order.OrderManager;
import org.ietr.preesm.mapper.model.MapperDAG;
import org.ietr.preesm.mapper.model.MapperDAGEdge;
import org.ietr.preesm.mapper.model.MapperDAGVertex;
import org.ietr.preesm.mapper.model.special.PrecedenceEdge;
import org.ietr.preesm.mapper.model.special.ReceiveVertex;
import org.ietr.preesm.mapper.model.special.SendVertex;
import org.ietr.preesm.mapper.model.special.TransferVertex;

/**
 * A transaction that adds a send and a receive vertex in an implementation.
 * 
 * @author mpelcat
 */
public class AddSendReceiveTransaction extends Transaction {
	// Inputs
	/**
	 * If not null, the transfer vertices need to be chained with formerly added
	 * ones
	 */
	private Transaction precedingTransaction = null;
	/**
	 * Implementation DAG to which the vertex is added
	 */
	private MapperDAG implementation = null;

	/**
	 * Route step corresponding to this overhead
	 */
	private AbstractRouteStep step = null;

	/**
	 * Original edge corresponding to this overhead
	 */
	private MapperDAGEdge edge = null;

	/**
	 * manager keeping scheduling orders
	 */
	private OrderManager orderManager = null;

	/**
	 * Cost of the transfer to give to the transfer vertex
	 */
	private long transferCost = 0;

	/**
	 * Index of the route step within its route
	 */
	private int routeIndex = 0;

	// Generated objects
	/**
	 * overhead vertex added
	 */
	private TransferVertex sendVertex = null;
	private TransferVertex receiveVertex = null;

	/**
	 * edges added
	 */
	private MapperDAGEdge newEdge1 = null;
	private MapperDAGEdge newEdge2 = null;
	private MapperDAGEdge newEdge3 = null;

	public AddSendReceiveTransaction(MapperDAGEdge edge,
			MapperDAG implementation, OrderManager orderManager,
			int routeIndex, AbstractRouteStep step, long transferCost) {
		super();
		this.precedingTransaction = null;
		this.edge = edge;
		this.implementation = implementation;
		this.orderManager = orderManager;
		this.routeIndex = routeIndex;
		this.step = step;
		this.transferCost = transferCost;
	}

	public AddSendReceiveTransaction(Transaction precedingTransaction,
			MapperDAGEdge edge, MapperDAG implementation,
			OrderManager orderManager, int routeIndex, AbstractRouteStep step,
			long transferCost) {
		super();
		this.precedingTransaction = precedingTransaction;
		this.edge = edge;
		this.implementation = implementation;
		this.orderManager = orderManager;
		this.routeIndex = routeIndex;
		this.step = step;
		this.transferCost = transferCost;
	}

	@Override
	public void execute(List<Object> resultList) {
		super.execute(resultList);

		MapperDAGVertex currentSource = null;
		MapperDAGVertex currentTarget = (MapperDAGVertex) edge.getTarget();
		if (precedingTransaction != null
				&& precedingTransaction instanceof AddSendReceiveTransaction) {
			currentSource = ((AddSendReceiveTransaction) precedingTransaction).receiveVertex;

			((MapperDAG) currentSource.getBase()).removeAllEdges(currentSource,
					currentTarget);
		} else {
			currentSource = (MapperDAGVertex) edge.getSource();
		}

		// Careful!!! Those names are used in code generation
		String nameRadix = ((MapperDAGVertex) edge.getSource()).getName()
				+ currentTarget.getName() + "_" + routeIndex;

		String sendVertexID = "s_" + nameRadix;

		String receiveVertexID = "r_" + nameRadix;

		if (edge instanceof PrecedenceEdge) {
			WorkflowLogger.getLogger().log(Level.INFO,
					"no transfer vertex corresponding to a schedule edge");
			return;
		}

		ComponentInstance senderOperator = step.getSender();
		ComponentInstance receiverOperator = step.getReceiver();

		sendVertex = new SendVertex(sendVertexID, implementation,
				(MapperDAGVertex) edge.getSource(),
				(MapperDAGVertex) edge.getTarget(), 0, 0);
		implementation.getTimings().dedicate(sendVertex);
		implementation.getMappings().dedicate(sendVertex);
		sendVertex.setRouteStep(step);
		implementation.addVertex(sendVertex);
		sendVertex.getTiming().setCost(transferCost);
		sendVertex.setEffectiveOperator(senderOperator);
		orderManager.insertAfter(currentSource, sendVertex);

		receiveVertex = new ReceiveVertex(receiveVertexID, implementation,
				(MapperDAGVertex) edge.getSource(),
				(MapperDAGVertex) edge.getTarget(), 0, 0);
		implementation.getTimings().dedicate(receiveVertex);
		implementation.getMappings().dedicate(receiveVertex);
		receiveVertex.setRouteStep(step);
		implementation.addVertex(receiveVertex);
		receiveVertex.getTiming().setCost(transferCost);
		receiveVertex.setEffectiveOperator(receiverOperator);
		orderManager.insertAfter(sendVertex, receiveVertex);

		newEdge1 = (MapperDAGEdge) implementation.addEdge(currentSource,
				sendVertex);
		newEdge2 = (MapperDAGEdge) implementation.addEdge(sendVertex,
				receiveVertex);
		newEdge3 = (MapperDAGEdge) implementation.addEdge(receiveVertex,
				currentTarget);

		newEdge1.setInit(edge.getInit().clone());
		newEdge2.setInit(edge.getInit().clone());
		newEdge3.setInit(edge.getInit().clone());

		newEdge1.getTiming().setCost(0);
		newEdge2.getTiming().setCost(0);
		newEdge3.getTiming().setCost(0);

		newEdge1.setAggregate(edge.getAggregate());
		newEdge2.setAggregate(edge.getAggregate());
		newEdge3.setAggregate(edge.getAggregate());

		// TODO: Consider the need for transfer vertex rescheduling
		/*
		 * if (false) { // Remove original edges
		 * implementation.removeAllEdges(currentSource, currentTarget); }
		 * 
		 * if (false) { // Scheduling transfer vertex PrecedenceEdgeAdder adder
		 * = new PrecedenceEdgeAdder(orderManager, implementation);
		 * adder.scheduleVertex(sendVertex);
		 * adder.scheduleVertex(receiveVertex); }
		 */

		if (resultList != null) {
			resultList.add(sendVertex);
			resultList.add(receiveVertex);
		}
	}

	@Override
	public String toString() {
		return ("AddSendReceive");
	}

}
