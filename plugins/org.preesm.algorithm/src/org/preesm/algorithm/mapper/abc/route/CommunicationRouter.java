/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2014 - 2015)
 * Jonathan Piat [jpiat@laas.fr] (2011)
 * Karol Desnos [karol.desnos@insa-rennes.fr] (2017)
 * Matthieu Wipliez [matthieu.wipliez@insa-rennes.fr] (2008)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2008 - 2016)
 *
 * This software is a computer program whose purpose is to help prototyping
 * parallel applications using dataflow formalism.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 */
package org.preesm.algorithm.mapper.abc.route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.preesm.algorithm.mapper.abc.edgescheduling.IEdgeSched;
import org.preesm.algorithm.mapper.abc.order.OrderManager;
import org.preesm.algorithm.mapper.abc.order.Schedule;
import org.preesm.algorithm.mapper.abc.transaction.Transaction;
import org.preesm.algorithm.mapper.abc.transaction.TransactionManager;
import org.preesm.algorithm.mapper.model.MapperDAG;
import org.preesm.algorithm.mapper.model.MapperDAGEdge;
import org.preesm.algorithm.mapper.model.MapperDAGVertex;
import org.preesm.algorithm.mapper.model.special.PrecedenceEdge;
import org.preesm.algorithm.model.dag.DAGEdge;
import org.preesm.algorithm.model.dag.DAGVertex;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.model.scenario.Scenario;
import org.preesm.model.slam.ComponentInstance;
import org.preesm.model.slam.Design;
import org.preesm.model.slam.SlamRoute;
import org.preesm.model.slam.SlamRouteStep;
import org.preesm.model.slam.SlamRouteStepType;
import org.preesm.model.slam.route.RouteCalculator;
import org.preesm.model.slam.route.RouteCostEvaluator;

/**
 * Routes the communications. Based on bridge design pattern. The processing is delegated to implementers
 *
 * @author mpelcat
 */
public class CommunicationRouter {

  /** The Constant transferType. */
  public static final int TRANSFER_TYPE = 0;

  /** The Constant overheadType. */
  public static final int OVERHEAD_TYPE = 1;

  /** The Constant sendReceiveType. */
  public static final int SEND_RECEIVE_TYPE = 2;

  /** The Constant synchroType. */
  public static final int SYNCHRO_TYPE = 3;

  /** The Constant involvementType. */
  public static final int INVOLVEMENT_TYPE = 4;

  /** The calculator. */
  private RouteCalculator calculator = null;

  /**
   * Instantiates a new communication router.
   *
   * @param archi
   *          the archi
   * @param scenario
   *          the scenario
   * @param implementation
   *          the implementation
   * @param edgeScheduler
   *          the edge scheduler
   * @param orderManager
   *          the order manager
   */
  public CommunicationRouter(final Design archi, final Scenario scenario, final MapperDAG implementation,
      final IEdgeSched edgeScheduler, final OrderManager orderManager) {
    this.implementers = new LinkedHashMap<>();
    setManagers(implementation, edgeScheduler, orderManager);

    this.calculator = RouteCalculator.getInstance(archi, scenario.getSimulationInfo().getAverageDataSize());

    // Initializing the available router implementers
    addImplementer(SlamRouteStepType.DMA_TYPE, new DmaComRouterImplementer(this));
    addImplementer(SlamRouteStepType.NODE_TYPE, new MessageComRouterImplementer(this));
    addImplementer(SlamRouteStepType.MEM_TYPE, new SharedRamRouterImplementer(this));
  }

  /**
   * Several ways to simulate a communication depending on which Route is taken into account.
   */
  private final Map<SlamRouteStepType, CommunicationRouterImplementer> implementers;

  /** DAG with communication vertices. */
  private MapperDAG implementation = null;

  /** manager of the generated transfers scheduling. */
  private IEdgeSched edgeScheduler = null;

  /** manager of the vertices order in general. */
  private OrderManager orderManager = null;

  /**
   * Adds the implementer.
   *
   * @param name
   *          the name
   * @param implementer
   *          the implementer
   */
  private void addImplementer(final SlamRouteStepType name, final CommunicationRouterImplementer implementer) {
    this.implementers.put(name, implementer);
  }

  /**
   * Gets the implementer.
   *
   * @param name
   *          the name
   * @return the implementer
   */
  private CommunicationRouterImplementer getImplementer(final SlamRouteStepType name) {
    return this.implementers.get(name);
  }

  /**
   * Gets the implementation.
   *
   * @return the implementation
   */
  public MapperDAG getImplementation() {
    return this.implementation;
  }

  /**
   * Gets the edge scheduler.
   *
   * @return the edge scheduler
   */
  public IEdgeSched getEdgeScheduler() {
    return this.edgeScheduler;
  }

  /**
   * Gets the order manager.
   *
   * @return the order manager
   */
  public OrderManager getOrderManager() {
    return this.orderManager;
  }

  /**
   * Sets the managers.
   *
   * @param implementation
   *          the implementation
   * @param edgeScheduler
   *          the edge scheduler
   * @param orderManager
   *          the order manager
   */
  public void setManagers(final MapperDAG implementation, final IEdgeSched edgeScheduler,
      final OrderManager orderManager) {
    this.implementation = implementation;
    this.edgeScheduler = edgeScheduler;
    this.orderManager = orderManager;
  }

  /**
   * adds all the necessary communication vertices with the given type.
   *
   * @param type
   *          the type
   */
  public void routeAll(final Integer type) {
    final TransactionManager localTransactionManager = new TransactionManager();

    // Get edges in scheduling order of their producers
    final Schedule totalOrder = this.orderManager.getTotalOrder();
    final List<MapperDAGVertex> list = totalOrder.getList();
    final Iterator<MapperDAGVertex> dagIterator = list.iterator();
    final List<DAGEdge> edgesInPrecedenceOrder = new ArrayList<>();

    while (dagIterator.hasNext()) {
      final DAGVertex vertex = dagIterator.next();
      edgesInPrecedenceOrder.addAll(vertex.outgoingEdges());
    }

    final int dagEdgeCount = this.implementation.edgeSet().size();
    final int outEdgesCount = edgesInPrecedenceOrder.size();
    if (outEdgesCount != dagEdgeCount) {
      // If this happens, this means that not all edges are covered by the previous while loop.
      throw new PreesmRuntimeException("Some DAG edges are not covered. Input DAG has " + dagEdgeCount
          + " edges whereas there are " + outEdgesCount + " edges connected to vertices.");
    }

    // We iterate the edges and process the ones with different allocations
    final Iterator<DAGEdge> iterator = edgesInPrecedenceOrder.iterator();
    while (iterator.hasNext()) {
      final MapperDAGEdge currentEdge = (MapperDAGEdge) iterator.next();

      if (!(currentEdge instanceof PrecedenceEdge) && (currentEdge.getInit().getDataSize() != 0)) {
        final MapperDAGVertex currentSource = ((MapperDAGVertex) currentEdge.getSource());
        final MapperDAGVertex currentDest = ((MapperDAGVertex) currentEdge.getTarget());

        if (currentSource.hasEffectiveOperator() && currentDest.hasEffectiveOperator()
            && !currentSource.getEffectiveOperator().equals(currentDest.getEffectiveOperator())) {
          // Adds several transfers for one edge depending on the
          // route steps
          final SlamRoute route = this.getRoute(currentEdge);
          int routeStepIndex = 0;
          Transaction lastTransaction = null;

          // Adds send and receive vertices and links them
          for (final SlamRouteStep step : route.getRouteSteps()) {
            final CommunicationRouterImplementer impl = getImplementer(step.getType());
            lastTransaction = impl.addVertices(step, currentEdge, localTransactionManager, type, routeStepIndex,
                lastTransaction, null);
            routeStepIndex++;
          }
        }
      }
    }

    localTransactionManager.execute();
  }

  /**
   * adds all the necessary communication vertices with the given type affected by the mapping of newVertex.
   *
   * @param newVertex
   *          the new vertex
   * @param types
   *          the types
   */
  public void routeNewVertex(final MapperDAGVertex newVertex, final List<Integer> types) {

    final Map<MapperDAGEdge, SlamRoute> transferEdges = getRouteMap(newVertex);
    final List<Object> createdVertices = new ArrayList<>();

    if (!transferEdges.isEmpty()) {
      for (final Integer type : types) {
        addVertices(transferEdges, type, createdVertices);
      }
    }
  }

  /**
   * Creates a map associating to each edge to be routed the corresponding route.
   *
   * @param newVertex
   *          the new vertex
   * @return the route map
   */
  private Map<MapperDAGEdge, SlamRoute> getRouteMap(final MapperDAGVertex newVertex) {
    final Map<MapperDAGEdge, SlamRoute> transferEdges = new LinkedHashMap<>();

    final Set<DAGEdge> edges = new LinkedHashSet<>();
    if (newVertex.incomingEdges() != null) {
      edges.addAll(newVertex.incomingEdges());
    }
    if (newVertex.outgoingEdges() != null) {
      edges.addAll(newVertex.outgoingEdges());
    }

    for (final DAGEdge edge : edges) {

      if (!(edge instanceof PrecedenceEdge)) {
        final MapperDAGVertex currentSource = ((MapperDAGVertex) edge.getSource());
        final MapperDAGVertex currentDest = ((MapperDAGVertex) edge.getTarget());

        if (currentSource.hasEffectiveOperator() && currentDest.hasEffectiveOperator()
            && !currentSource.getEffectiveOperator().equals(currentDest.getEffectiveOperator())) {
          final MapperDAGEdge mapperEdge = (MapperDAGEdge) edge;
          transferEdges.put(mapperEdge, this.getRoute(mapperEdge));
        }
      }
    }
    return transferEdges;
  }

  /**
   * Adds the dynamic vertices to simulate the transfers of the given edges.
   *
   * @param transferEdges
   *          the transfer edges
   * @param type
   *          the type
   * @param createdVertices
   *          the created vertices
   */
  private void addVertices(final Map<MapperDAGEdge, SlamRoute> transferEdges, final int type,
      final List<Object> createdVertices) {
    final TransactionManager localTransactionManager = new TransactionManager(createdVertices);

    for (final Entry<MapperDAGEdge, SlamRoute> route : transferEdges.entrySet()) {
      final MapperDAGEdge edge = route.getKey();
      int routeStepIndex = 0;
      Transaction lastTransaction = null;
      for (final SlamRouteStep step : transferEdges.get(edge).getRouteSteps()) {
        final CommunicationRouterImplementer impl = getImplementer(step.getType());
        lastTransaction = impl.addVertices(step, edge, localTransactionManager, type, routeStepIndex, lastTransaction,
            createdVertices);

        routeStepIndex++;
      }
    }

    localTransactionManager.execute();
  }

  /**
   * Evaluates the transfer between two operators.
   *
   * @param edge
   *          the edge
   * @return the long
   */
  public long evaluateTransferCost(final MapperDAGEdge edge) {

    final MapperDAGVertex source = ((MapperDAGVertex) edge.getSource());
    final MapperDAGVertex dest = ((MapperDAGVertex) edge.getTarget());

    final ComponentInstance sourceOp = source.getEffectiveOperator();
    final ComponentInstance destOp = dest.getEffectiveOperator();

    final long dataSize = edge.getInit().getDataSize();

    long cost = 0;

    // Retrieving the route
    if ((sourceOp != null) && (destOp != null)) {
      final SlamRoute route = this.calculator.getRoute(sourceOp, destOp);
      cost = RouteCostEvaluator.evaluateTransferCost(route, dataSize);
    } else {
      final String msg = "trying to evaluate a transfer between non mapped operators.";
      throw new PreesmRuntimeException(msg);
    }

    return cost;
  }

  /**
   *
   */
  public SlamRoute getRoute(final MapperDAGEdge edge) {
    final MapperDAGVertex source = (MapperDAGVertex) edge.getSource();
    final MapperDAGVertex target = (MapperDAGVertex) edge.getTarget();
    final ComponentInstance sourceOp = source.getEffectiveOperator();
    final ComponentInstance targetOp = target.getEffectiveOperator();
    return calculator.getRoute(sourceOp, targetOp);
  }

}
