/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2009 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2009 - 2012)
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
package org.ietr.preesm.mapper.abc.route.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.MessageRouteStep;
import org.ietr.preesm.mapper.abc.edgescheduling.IEdgeSched;
import org.ietr.preesm.mapper.abc.edgescheduling.SimpleEdgeSched;
import org.ietr.preesm.mapper.abc.route.AbstractCommunicationRouter;
import org.ietr.preesm.mapper.abc.route.CommunicationRouter;
import org.ietr.preesm.mapper.abc.route.CommunicationRouterImplementer;
import org.ietr.preesm.mapper.abc.transaction.AddInvolvementVertexTransaction;
import org.ietr.preesm.mapper.abc.transaction.AddSendReceiveTransaction;
import org.ietr.preesm.mapper.abc.transaction.AddTransferVertexTransaction;
import org.ietr.preesm.mapper.abc.transaction.Transaction;
import org.ietr.preesm.mapper.abc.transaction.TransactionManager;
import org.ietr.preesm.mapper.model.MapperDAGEdge;
import org.ietr.preesm.mapper.model.MapperDAGVertex;
import org.ietr.preesm.mapper.model.special.TransferVertex;

// TODO: Auto-generated Javadoc
/**
 * Class responsible to generate the suited vertices while simulating a message communication.
 *
 * @author mpelcat
 */
public class MessageComRouterImplementer extends CommunicationRouterImplementer {

  /**
   * Instantiates a new message com router implementer.
   *
   * @param user
   *          the user
   */
  public MessageComRouterImplementer(final AbstractCommunicationRouter user) {
    super(user);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.preesm.mapper.abc.route.CommunicationRouterImplementer#removeVertices(org.ietr.preesm.
   * mapper.model.MapperDAGEdge, org.ietr.preesm.mapper.abc.transaction.TransactionManager)
   */
  @Override
  public void removeVertices(final MapperDAGEdge edge, final TransactionManager transactions) {

  }

  /**
   * Careful!!! Only simple edge scheduler allowed for synchronized edges.
   *
   * @return the edge scheduler
   */
  @Override
  public IEdgeSched getEdgeScheduler() {
    return new SimpleEdgeSched(getOrderManager());
  }

  /**
   * Adds the simulation vertices for a given edge and a given route step.
   *
   * @param routeStep
   *          the route step
   * @param edge
   *          the edge
   * @param transactions
   *          the transactions
   * @param type
   *          the type
   * @param routeStepIndex
   *          the route step index
   * @param lastTransaction
   *          the last transaction
   * @param alreadyCreatedVertices
   *          the already created vertices
   * @return the transaction
   */
  @Override
  public Transaction addVertices(final AbstractRouteStep routeStep, final MapperDAGEdge edge,
      final TransactionManager transactions, final int type, final int routeStepIndex,
      final Transaction lastTransaction, final List<Object> alreadyCreatedVertices) {

    if (routeStep instanceof MessageRouteStep) {
      // Adding the transfers
      final MessageRouteStep messageStep = ((MessageRouteStep) routeStep);
      // All the transfers along the path have the same time: the time
      // to transfer the data on the slowest contention node
      final long transferTime = messageStep.getWorstTransferTime(edge.getInit().getDataSize());

      // Adding the transfers of a message route step
      if (type == CommunicationRouter.transferType) {
        final List<ComponentInstance> nodes = messageStep.getContentionNodes();
        AddTransferVertexTransaction transaction = null;

        for (final ComponentInstance node : nodes) {
          final int nodeIndex = nodes.indexOf(node);
          transaction = new AddTransferVertexTransaction("transfer", lastTransaction, getEdgeScheduler(), edge,
              getImplementation(), getOrderManager(), routeStepIndex, nodeIndex, routeStep, transferTime, node, true);
          transactions.add(transaction);
        }

        return transaction;
      } else if (type == CommunicationRouter.involvementType) {
        // Adding the involvement
        MapperDAGEdge incomingEdge = null;
        // TransferVertex correspondingTransfer = null;

        for (final Object o : alreadyCreatedVertices) {
          if (o instanceof TransferVertex) {
            final TransferVertex v = (TransferVertex) o;
            if (v.getSource().equals(edge.getSource()) && v.getTarget().equals(edge.getTarget())
                && (v.getRouteStep() == routeStep) && (v.getNodeIndex() == 0)) {
              // Finding the edge where to add an involvement
              incomingEdge = (MapperDAGEdge) v.incomingEdges().toArray()[0];
              // correspondingTransfer = v;
            }

          }
        }

        if (incomingEdge != null) {
          transactions.add(new AddInvolvementVertexTransaction(true, incomingEdge, getImplementation(), routeStep,
              transferTime, getOrderManager()));
        } else {
          WorkflowLogger.getLogger().log(Level.FINE,
              "The transfer following vertex" + edge.getSource() + "was not found. We could not add overhead.");
        }

      } else if (type == CommunicationRouter.synchroType) {

        // Synchronizing the previously created transfers
        final List<MapperDAGVertex> toSynchronize = new ArrayList<>();

        for (final Object o : alreadyCreatedVertices) {
          if (o instanceof TransferVertex) {
            final TransferVertex v = (TransferVertex) o;
            if (v.getSource().equals(edge.getSource()) && v.getTarget().equals(edge.getTarget())
                && (v.getRouteStep() == routeStep)) {
              toSynchronize.add(v);

              if (v.getInvolvementVertex() != null) {
                toSynchronize.add(v.getInvolvementVertex());
              }
            }

          }
        }

        // Synchronizing the vertices in order manager (they
        // have consecutive total order and be scheduled
        // simultaneously).
        /*
         * if (toSynchronize.size() > 1) { ImplementationCleaner cleaner = new ImplementationCleaner( getOrderManager(),
         * getImplementation()); PrecedenceEdgeAdder adder = new PrecedenceEdgeAdder( getOrderManager(),
         * getImplementation()); MapperDAGVertex last = null; last = null;
         *
         * for (MapperDAGVertex v : toSynchronize) { cleaner.unscheduleVertex(v); last =
         * getOrderManager().synchronize(last, v); adder.scheduleVertex(v); }
         *
         * }
         */
      } else if (type == CommunicationRouter.sendReceiveType) {

        final Transaction transaction = new AddSendReceiveTransaction(lastTransaction, edge, getImplementation(),
            getOrderManager(), routeStepIndex, routeStep, TransferVertex.SEND_RECEIVE_COST);

        transactions.add(transaction);
        return transaction;
      }
    }
    return null;
  }

}
