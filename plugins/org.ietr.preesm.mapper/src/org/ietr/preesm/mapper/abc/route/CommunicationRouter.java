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

package org.ietr.preesm.mapper.abc.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.ietr.dftools.algorithm.model.dag.DAGEdge;
import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.architecture.slam.Design;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.DmaRouteStep;
import org.ietr.preesm.core.architecture.route.MemRouteStep;
import org.ietr.preesm.core.architecture.route.MessageRouteStep;
import org.ietr.preesm.core.architecture.route.Route;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.mapper.abc.edgescheduling.IEdgeSched;
import org.ietr.preesm.mapper.abc.order.OrderManager;
import org.ietr.preesm.mapper.abc.route.calcul.RouteCalculator;
import org.ietr.preesm.mapper.abc.route.impl.DmaComRouterImplementer;
import org.ietr.preesm.mapper.abc.route.impl.MessageComRouterImplementer;
import org.ietr.preesm.mapper.abc.route.impl.SharedRamRouterImplementer;
import org.ietr.preesm.mapper.abc.transaction.Transaction;
import org.ietr.preesm.mapper.abc.transaction.TransactionManager;
import org.ietr.preesm.mapper.model.MapperDAG;
import org.ietr.preesm.mapper.model.MapperDAGEdge;
import org.ietr.preesm.mapper.model.MapperDAGVertex;
import org.ietr.preesm.mapper.model.special.PrecedenceEdge;

/**
 * Routes the communications. Based on bridge design pattern. The processing is
 * delegated to implementers
 * 
 * @author mpelcat
 */
public class CommunicationRouter extends AbstractCommunicationRouter {

	public static final int transferType = 0;
	public static final int overheadType = 1;
	public static final int sendReceiveType = 2;
	public static final int synchroType = 3;
	public static final int involvementType = 4;

	private RouteCalculator calculator = null;

	public CommunicationRouter(Design archi, PreesmScenario scenario,
			MapperDAG implementation, IEdgeSched edgeScheduler,
			OrderManager orderManager) {
		super(implementation, edgeScheduler, orderManager);
		this.calculator = RouteCalculator.getInstance(archi, scenario);

		// Initializing the available router implementers
		this.addImplementer(DmaRouteStep.type,
				new DmaComRouterImplementer(this));
		this.addImplementer(MessageRouteStep.type,
				new MessageComRouterImplementer(this));
		this.addImplementer(MemRouteStep.type, new SharedRamRouterImplementer(
				this));
	}

	/**
	 * adds all the necessary communication vertices with the given type
	 */
	@Override
	public void routeAll(MapperDAG implementation, Integer type) {
		TransactionManager localTransactionManager = new TransactionManager();

		// We iterate the edges and process the ones with different allocations
		Iterator<DAGEdge> iterator = implementation.edgeSet().iterator();

		while (iterator.hasNext()) {
			MapperDAGEdge currentEdge = (MapperDAGEdge) iterator.next();

			if (!(currentEdge instanceof PrecedenceEdge)
					&& currentEdge.getInit().getDataSize() != 0) {
				MapperDAGVertex currentSource = ((MapperDAGVertex) currentEdge
						.getSource());
				MapperDAGVertex currentDest = ((MapperDAGVertex) currentEdge
						.getTarget());

				if (currentSource.hasEffectiveOperator()
						&& currentDest.hasEffectiveOperator()) {
					if (!currentSource.getEffectiveOperator().equals(
							currentDest.getEffectiveOperator())) {
						// Adds several transfers for one edge depending on the
						// route steps
						Route route = calculator.getRoute(currentEdge);
						int routeStepIndex = 0;
						Transaction lastTransaction = null;

						// Adds send and receive vertices and links them
						for (AbstractRouteStep step : route) {
							CommunicationRouterImplementer impl = getImplementer(step
									.getType());
							lastTransaction = impl.addVertices(step,
									currentEdge, localTransactionManager, type,
									routeStepIndex, lastTransaction, null);
							routeStepIndex++;
						}
					}
				}
			}
		}

		localTransactionManager.execute();
	}

	/**
	 * adds all the necessary communication vertices with the given type
	 * affected by the mapping of newVertex
	 */
	@Override
	public void routeNewVertex(MapperDAGVertex newVertex, List<Integer> types) {

		Map<MapperDAGEdge, Route> transferEdges = getRouteMap(newVertex);
		List<Object> createdVertices = new ArrayList<Object>();

		if (!transferEdges.isEmpty()) {
			for (Integer type : types) {
				addVertices(transferEdges, type, createdVertices);
			}
		}
	}

	/**
	 * Creates a map associating to each edge to be routed the corresponding
	 * route
	 */
	public Map<MapperDAGEdge, Route> getRouteMap(MapperDAGVertex newVertex) {
		Map<MapperDAGEdge, Route> transferEdges = new HashMap<MapperDAGEdge, Route>();

		Set<DAGEdge> edges = new HashSet<DAGEdge>();
		if (newVertex.incomingEdges() != null)
			edges.addAll(newVertex.incomingEdges());
		if (newVertex.outgoingEdges() != null)
			edges.addAll(newVertex.outgoingEdges());

		for (DAGEdge edge : edges) {

			if (!(edge instanceof PrecedenceEdge)) {
				MapperDAGVertex currentSource = ((MapperDAGVertex) edge
						.getSource());
				MapperDAGVertex currentDest = ((MapperDAGVertex) edge
						.getTarget());

				if (currentSource.hasEffectiveOperator()
						&& currentDest.hasEffectiveOperator()) {
					if (!currentSource.getEffectiveOperator().equals(
							currentDest.getEffectiveOperator())) {
						MapperDAGEdge mapperEdge = (MapperDAGEdge) edge;
						transferEdges.put(mapperEdge,
								calculator.getRoute(mapperEdge));
					}
				}
			}
		}
		return transferEdges;
	}

	/**
	 * Adds the dynamic vertices to simulate the transfers of the given edges
	 */
	public void addVertices(Map<MapperDAGEdge, Route> transferEdges, int type,
			List<Object> createdVertices) {
		TransactionManager localTransactionManager = new TransactionManager(
				createdVertices);

		for (MapperDAGEdge edge : transferEdges.keySet()) {
			int routeStepIndex = 0;
			Transaction lastTransaction = null;
			for (AbstractRouteStep step : transferEdges.get(edge)) {
				CommunicationRouterImplementer impl = getImplementer(step
						.getType());
				lastTransaction = impl.addVertices(step, edge,
						localTransactionManager, type, routeStepIndex,
						lastTransaction, createdVertices);

				routeStepIndex++;
			}
		}

		localTransactionManager.execute();
	}

	/**
	 * Evaluates the transfer between two operators
	 */
	@Override
	public long evaluateTransferCost(MapperDAGEdge edge) {

		MapperDAGVertex source = ((MapperDAGVertex) edge.getSource());
		MapperDAGVertex dest = ((MapperDAGVertex) edge.getTarget());

		ComponentInstance sourceOp = source.getEffectiveOperator();
		ComponentInstance destOp = dest.getEffectiveOperator();

		long cost = 0;

		// Retrieving the route
		if (sourceOp != null && destOp != null) {
			Route route = calculator.getRoute(sourceOp, destOp);
			cost = route.evaluateTransferCost(edge.getInit().getDataSize());
		} else {
			WorkflowLogger
					.getLogger()
					.log(Level.SEVERE,
							"trying to evaluate a transfer between non mapped operators.");
		}

		return cost;
	}
	
	public Route getRoute(MapperDAGEdge edge){
		return calculator.getRoute(edge);
	}

}
