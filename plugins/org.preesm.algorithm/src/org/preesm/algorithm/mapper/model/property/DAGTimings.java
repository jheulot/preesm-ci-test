/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2012 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2015)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2012)
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
package org.preesm.algorithm.mapper.model.property;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.preesm.algorithm.mapper.model.MapperDAGVertex;
import org.preesm.commons.CloneableProperty;

/**
 * MapperDAG stores mapping properties shared by several of its vertices that are synchronized.
 *
 * @author mpelcat
 */
public class DAGTimings implements CloneableProperty<DAGTimings> {

  /** The timings. */
  private final Map<String, VertexTiming> timings;

  /**
   * Instantiates a new DAG timings.
   */
  public DAGTimings() {
    this.timings = new LinkedHashMap<>();
  }

  /**
   * Gets the timing.
   *
   * @param vertexId
   *          the vertex id
   * @return the timing
   */
  public VertexTiming getTiming(final String vertexId) {
    return this.timings.get(vertexId);
  }

  /**
   * Dedicates a created VertexMapping object to a single vertex.
   *
   * @param vertex
   *          the vertex
   */
  public void dedicate(final MapperDAGVertex vertex) {
    final VertexTiming newTiming = new VertexTiming();
    put(vertex.getName(), newTiming);
  }

  private void put(final String vertexId, final VertexTiming m) {
    this.timings.put(vertexId, m);
    m.addVertexID(vertexId);
  }

  public void remove(final MapperDAGVertex vertex) {
    this.timings.remove(vertex.getName());
  }

  @Override
  public DAGTimings copy() {
    final DAGTimings newTimings = new DAGTimings();
    for (final Entry<String, VertexTiming> entry : this.timings.entrySet()) {
      final String s = entry.getKey();
      final VertexTiming value = entry.getValue();
      newTimings.put(s, value.copy());
    }
    return newTimings;
  }
}
