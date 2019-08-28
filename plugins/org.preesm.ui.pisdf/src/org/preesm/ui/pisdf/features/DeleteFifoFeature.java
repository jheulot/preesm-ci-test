/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2013 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2015)
 * Julien Heulot [julien.heulot@insa-rennes.fr] (2013)
 * Karol Desnos [karol.desnos@insa-rennes.fr] (2013)
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
package org.preesm.ui.pisdf.features;

import java.util.List;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.impl.DeleteContext;
import org.eclipse.graphiti.features.context.impl.MultiDeleteInfo;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
import org.preesm.model.pisdf.Delay;
import org.preesm.model.pisdf.Fifo;

// TODO: Auto-generated Javadoc
/**
 * Delete feature to delete a {@link Fifo} from a graph.
 *
 * @author kdesnos
 *
 */
public class DeleteFifoFeature extends DefaultDeleteFeature {

  /**
   * If the {@link Fifo} has a {@link Delay}, it is associated to 2 {@link Connection}. One of the two
   * {@link Connection} might needs to be removed manually in the {@link #postDelete(IDeleteContext)} method. This
   * {@link Connection} attribute will be used to store this remaining {@link Connection}.
   */
  protected Connection remainingConnection = null;

  /**
   * Default constructor for {@link DeleteFifoFeature}.
   *
   * @param fp
   *          the feature provider
   */
  public DeleteFifoFeature(final IFeatureProvider fp) {
    super(fp);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.graphiti.ui.features.DefaultDeleteFeature#preDelete(org.eclipse.graphiti.features.context.
   * IDeleteContext)
   */
  @Override
  public void preDelete(final IDeleteContext context) {
    // If the Fifo has a delay, first delete it.
    final Connection connection = (Connection) context.getPictogramElement();
    final Fifo fifo = (Fifo) getBusinessObjectForPictogramElement(connection);

    AnchorContainer delayFeature = null;
    Connection targetConnection = null;

    if (fifo.getDelay() != null) {
      // Is the "first half" of the connection (the one before the delay)
      // the one given to the delete context.
      AnchorContainer parent = connection.getStart().getParent();
      Object obj = getBusinessObjectForPictogramElement(parent);
      if (obj instanceof Delay) {
        delayFeature = parent;
      }

      // Is the second half of the connection the one given to the delete
      // context.
      parent = connection.getEnd().getParent();
      obj = getBusinessObjectForPictogramElement(parent);
      if (obj instanceof Delay) {
        delayFeature = parent;
        targetConnection = getTargetConnection(delayFeature, connection);
      }

      final DeleteContext delCtxt = new DeleteContext(delayFeature);
      // To deactivate dialog box
      delCtxt.setMultiDeleteInfo(new MultiDeleteInfo(false, false, 0));
      getFeatureProvider().getDeleteFeature(delCtxt).delete(delCtxt);

      // delay removal already removed our own connection, so we have to remove the opposite one
      if (targetConnection != null) {
        final IRemoveContext rmCtxt = new RemoveContext(targetConnection);
        final IRemoveFeature rmFeature = getFeatureProvider().getRemoveFeature(rmCtxt);
        if (rmFeature.canRemove(rmCtxt)) {
          rmFeature.remove(rmCtxt);
        } else {
          throw new RuntimeException("Could not delete a connection.");
        }
      }
    }
    // Super call
    super.preDelete(context);
  }

  /**
   * Retrieve the target connection when fifo contains a delay
   * 
   * @param delayFeature
   *          Parent delay of the Fifo.
   * @param connec
   *          Half of the fifo, source part
   * @return
   */
  private Connection getTargetConnection(AnchorContainer delayFeature, Connection connec) {
    final Delay delay = (Delay) getBusinessObjectForPictogramElement(delayFeature);
    final ChopboxAnchor cba = (ChopboxAnchor) delayFeature.getAnchors().get(0);
    final List<Connection> outgoingConnections = cba.getOutgoingConnections();
    Connection targetConnection = null;
    for (final Connection connection : outgoingConnections) {
      final Object obj = getBusinessObjectForPictogramElement(connection);
      // With setter delay, there can be multiple FIFOs
      // We have to choose the correct one
      if (obj instanceof Fifo && (((Fifo) obj).getDelay() == delay) && connection != connec) {
        targetConnection = connection;
        break;
      }
    }
    return targetConnection;
  }

}
