/*
 * Copyright or © or Copr. IETR/INSA - Rennes (2018 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2018 - 2019)
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
package org.preesm.algorithm.model.sdf.esdf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.preesm.algorithm.model.AbstractEdge;
import org.preesm.algorithm.model.sdf.SDFEdge;
import org.preesm.algorithm.model.sdf.SDFInterfaceVertex;
import org.preesm.model.pisdf.AbstractVertex;

/**
 * Special vertex that supports broadcast.
 *
 * @author jpiat
 */
public class SDFBroadcastVertex extends SDFAbstractSpecialVertex {

  /** Kind of node. */
  public static final String BROADCAST = "Broadcast";

  /** String to access the property edges order. */
  private static final String EDGES_ORDER = "edges_order";

  /**
   * Creates a new SDFInterfaceVertex with the default direction (SINK).
   */
  public SDFBroadcastVertex(AbstractVertex origVertex) {
    super(origVertex);
    setKind(SDFBroadcastVertex.BROADCAST);
    setNbRepeat(1L);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex#clone()
   */
  @Override
  public SDFBroadcastVertex copy() {
    final SDFBroadcastVertex copy = new SDFBroadcastVertex(this.origVertex);
    copy.setName(getName());
    copy.setNbRepeat(getNbRepeat());

    // Copy the ports
    for (final SDFInterfaceVertex sink : getSinks()) {
      if ((copy.getGraphDescription() != null) && (copy.getGraphDescription().getVertex(sink.getName()) != null)) {
        copy.addSink((SDFSinkInterfaceVertex) getGraphDescription().getVertex(sink.getName()));
      } else {
        copy.addSink((SDFSinkInterfaceVertex) sink.copy());
      }
    }
    for (final SDFInterfaceVertex source : getSources()) {
      if ((copy.getGraphDescription() != null) && (copy.getGraphDescription().getVertex(source.getName()) != null)) {
        copy.addSource((SDFSourceInterfaceVertex) getGraphDescription().getVertex(source.getName()));
      } else {
        copy.addSource((SDFSourceInterfaceVertex) source.copy());
      }
    }

    return copy;
  }

  /**
   * Adds the connection.
   *
   * @param newEdge
   *          the new edge
   */
  private void addConnection(final SDFEdge newEdge) {
    getConnections().put((long) getConnections().size(), newEdge);
  }

  /**
   * Removes the connection.
   *
   * @param newEdge
   *          the new edge
   */
  private void removeConnection(final SDFEdge newEdge) {
    final Long index = getEdgeIndex(newEdge);
    getConnections().remove(index);

    // update the indexes of remaining connections.
    for (long i = index; i < getConnections().size(); i++) {
      final SDFEdge edge = getConnections().remove(i + 1);
      getConnections().put(i, edge);
    }
  }

  /**
   * Remove the given {@link SDFEdge} from its current index and insert it just before the {@link SDFEdge} currently at
   * the given index (or at the end of the list if index == connections.size).
   *
   * @param edge
   *          the {@link SDFEdge} to move
   * @param index
   *          the new index for the {@link SDFEdge}
   * @return <code>true</code> if the edge was found and moved at an existing index, <code>false</code> otherwise.
   */
  @Override
  public boolean setEdgeIndex(final SDFEdge edge, long index) {
    final Map<Long, SDFEdge> connections = getConnections();
    if ((index < connections.size()) && connections.containsValue(edge)) {
      final long oldIndex = getEdgeIndex(edge);
      removeConnection(edge);
      index = (oldIndex < index) ? index - 1 : index;
      // update the indexes of subsequent edges.
      for (long i = (connections.size() - 1); i >= index; i--) {
        connections.put(i + 1, connections.remove(i));
      }
      // put the edge in it new place
      connections.put(index, edge);
      return true;
    }

    // Special case, put the edge at the end
    if ((index == connections.size()) && connections.containsValue(edge)) {
      removeConnection(edge);
      addConnection(edge);
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.algorithm.model.AbstractVertex#connectionAdded(org.ietr.dftools.algorithm.model.AbstractEdge)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void connectionAdded(final AbstractEdge e) {
    addConnection((SDFEdge) e);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.ietr.dftools.algorithm.model.AbstractVertex#connectionRemoved(org.ietr.dftools.algorithm.model.AbstractEdge)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void connectionRemoved(final AbstractEdge e) {
    removeConnection((SDFEdge) e);
  }

  /**
   * Gives the edge connection index.
   *
   * @param edge
   *          The edge to get the connection index
   * @return The connection index of the edge
   */
  public Long getEdgeIndex(final SDFEdge edge) {
    for (final Long idx : getConnections().keySet()) {
      if (getConnections().get(idx).equals(edge)) {
        return idx;
      }
    }
    return null;
  }

  /**
   * Gets the outgoing connections.
   *
   * @return The outgoing connections of this for in an ordered list
   */
  public List<SDFEdge> getOutgoingConnections() {
    final List<SDFEdge> edges = new ArrayList<>(getConnections().size());
    for (long i = 0; i < getConnections().size(); i++) {
      if (getConnections().get(i) != null) {
        edges.add(getConnections().get(i));
      }
    }
    return edges;
  }

  /**
   * Gets the connections.
   *
   * @return the connections
   */
  protected Map<Long, SDFEdge> getConnections() {
    Map<Long, SDFEdge> connections = getPropertyBean().getValue(SDFBroadcastVertex.EDGES_ORDER);
    if (connections == null) {
      connections = new LinkedHashMap<>();
      getPropertyBean().setValue(SDFBroadcastVertex.EDGES_ORDER, connections);
    }
    return connections;
  }

}
