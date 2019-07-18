/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2014)
 * Matthieu Wipliez [matthieu.wipliez@insa-rennes.fr] (2008)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2008 - 2014)
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
package org.preesm.algorithm.mapper.abc.transaction;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.preesm.algorithm.mapper.abc.order.OrderManager;
import org.preesm.algorithm.mapper.model.MapperDAG;
import org.preesm.algorithm.mapper.model.MapperDAGEdge;
import org.preesm.algorithm.mapper.model.MapperDAGVertex;
import org.preesm.algorithm.mapper.model.special.InvolvementVertex;
import org.preesm.algorithm.mapper.model.special.PrecedenceEdge;
import org.preesm.algorithm.mapper.model.special.PrecedenceEdgeAdder;
import org.preesm.algorithm.mapper.model.special.TransferVertex;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.slam.route.AbstractRouteStep;

/**
 * Transaction executing the addition of an involvement vertex.
 *
 * @author mpelcat
 */
public class AddInvolvementVertexTransaction implements Transaction {

  // Inputs
  /**
   * Determining if the current involvement is executed by the sender or by the receiver of the transfer.
   */
  private final boolean isSender;

  /** Implementation DAG to which the vertex is added. */
  private MapperDAG implementation = null;

  /** Route step corresponding to this involvement. */
  private AbstractRouteStep step = null;

  /** time of this involvement. */
  private long involvementTime = 0;

  /** Original edge and transfer corresponding to this involvement. */
  private MapperDAGEdge edge = null;

  /** manager keeping scheduling orders. */
  private OrderManager orderManager = null;

  // Generated objects
  /** involvement vertex added. */
  private InvolvementVertex iVertex = null;

  /**
   * Instantiates a new adds the involvement vertex transaction.
   *
   * @param isSender
   *          the is sender
   * @param edge
   *          the edge
   * @param implementation
   *          the implementation
   * @param step
   *          the step
   * @param involvementTime
   *          the involvement time
   * @param orderManager
   *          the order manager
   */
  public AddInvolvementVertexTransaction(final boolean isSender, final MapperDAGEdge edge,
      final MapperDAG implementation, final AbstractRouteStep step, final long involvementTime,
      final OrderManager orderManager) {
    super();
    this.isSender = isSender;
    this.edge = edge;
    this.implementation = implementation;
    this.step = step;
    this.orderManager = orderManager;
    this.involvementTime = involvementTime;
  }

  @Override
  public void execute(final List<Object> resultList) {

    final MapperDAGVertex currentSource = (MapperDAGVertex) this.edge.getSource();
    final MapperDAGVertex currentTarget = (MapperDAGVertex) this.edge.getTarget();

    if (this.edge instanceof PrecedenceEdge) {
      PreesmLogger.getLogger().log(Level.INFO, "no involvement vertex corresponding to a schedule edge");
      return;
    }

    final String ivertexID = "__involvement (" + currentSource.getName() + "," + currentTarget.getName() + ")";

    if (this.involvementTime > 0) {
      this.iVertex = new InvolvementVertex(ivertexID, null);
      this.implementation.getTimings().dedicate(this.iVertex);
      this.implementation.getMappings().dedicate(this.iVertex);

      this.implementation.addVertex(this.iVertex);
      this.iVertex.getTiming().setCost(this.involvementTime);

      if (this.isSender) {
        this.iVertex.setEffectiveOperator(this.step.getSender());
        ((TransferVertex) currentTarget).setInvolvementVertex(this.iVertex);
      } else {
        this.iVertex.setEffectiveOperator(this.step.getReceiver());
        ((TransferVertex) currentSource).setInvolvementVertex(this.iVertex);
      }

      if (this.isSender) {
        processSender(currentSource, currentTarget);
      } else {
        processReceiver(currentSource, currentTarget);
      }

      // Scheduling involvement vertex
      new PrecedenceEdgeAdder(this.orderManager, this.implementation).scheduleVertex(this.iVertex);

      if (resultList != null) {
        resultList.add(this.iVertex);
      }
    }
  }

  private void processReceiver(final MapperDAGVertex currentSource, final MapperDAGVertex currentTarget) {
    final MapperDAGEdge newOutEdge = (MapperDAGEdge) this.implementation.addEdge(this.iVertex, currentTarget);
    newOutEdge.setInit(this.edge.getInit().copy());
    newOutEdge.getTiming().setCost(0);

    this.orderManager.insertAfter(currentSource, this.iVertex);
  }

  private void processSender(final MapperDAGVertex currentSource, final MapperDAGVertex currentTarget) {
    final MapperDAGEdge newInEdge = (MapperDAGEdge) this.implementation.addEdge(currentSource, this.iVertex);
    newInEdge.setInit(this.edge.getInit().copy());
    newInEdge.getTiming().setCost(0);

    MapperDAGVertex receiverVertex = currentTarget;
    do {
      final Set<MapperDAGVertex> succs = receiverVertex.getSuccessors(false).keySet();
      if (succs.isEmpty() && (receiverVertex instanceof TransferVertex)) {
        PreesmLogger.getLogger().log(Level.SEVERE, "Transfer has no successor: " + receiverVertex.getName());
      }

      for (final MapperDAGVertex next : receiverVertex.getSuccessors(false).keySet()) {
        if (next != null) {
          receiverVertex = next;
        }
      }
    } while (receiverVertex instanceof TransferVertex);

    final MapperDAGEdge newoutEdge = (MapperDAGEdge) this.implementation.addEdge(this.iVertex, receiverVertex);
    newoutEdge.setInit(this.edge.getInit().copy());
    newoutEdge.getTiming().setCost(0);

    this.orderManager.insertBefore(currentTarget, this.iVertex);
  }

  @Override
  public String toString() {
    return ("AddInvolvement(" + this.iVertex.toString() + ")");
  }

}
