/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2009 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2014)
 * Jonathan Piat [jpiat@laas.fr] (2011)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2009 - 2012)
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
package org.preesm.algorithm.mapper.abc;

import org.preesm.algorithm.mapper.model.MapperDAGVertex;
import org.preesm.algorithm.model.dag.DAGVertex;

/**
 * The special vertices are special to the mapper because they have additional mapping rules.
 *
 * @author mpelcat
 */
public class SpecialVertexManager {

  private SpecialVertexManager() {
    // forbid instantiation
  }

  /**
   * Tests if a vertex is of type broadcast.
   *
   * @param vertex
   *          the vertex
   * @return true, if is special
   */
  public static boolean isSpecial(final DAGVertex vertex) {

    final String kind = vertex.getKind();
    if (kind == null) {
      return false;
    }

    return kind.equalsIgnoreCase(MapperDAGVertex.DAG_BROADCAST_VERTEX)
        || kind.equalsIgnoreCase(MapperDAGVertex.DAG_FORK_VERTEX)
        || kind.equalsIgnoreCase(MapperDAGVertex.DAG_JOIN_VERTEX)
        || kind.equalsIgnoreCase(MapperDAGVertex.DAG_INIT_VERTEX)
        || kind.equalsIgnoreCase(MapperDAGVertex.DAG_END_VERTEX);
  }

  /**
   * Tests if a vertex is of type broadcast.
   *
   * @param vertex
   *          the vertex
   * @return true, if is broad cast
   */
  public static boolean isBroadCast(final DAGVertex vertex) {

    final String kind = vertex.getKind();
    if (kind == null) {
      return false;
    }

    return kind.equalsIgnoreCase(MapperDAGVertex.DAG_BROADCAST_VERTEX);
  }

  /**
   * Tests if a vertex is of type fork.
   *
   * @param vertex
   *          the vertex
   * @return true, if is fork
   */
  public static boolean isFork(final DAGVertex vertex) {

    final String kind = vertex.getKind();
    if (kind == null) {
      return false;
    }

    return kind.equalsIgnoreCase(MapperDAGVertex.DAG_FORK_VERTEX);
  }

  /**
   * Tests if a vertex is of type join.
   *
   * @param vertex
   *          the vertex
   * @return true, if is join
   */
  public static boolean isJoin(final DAGVertex vertex) {

    final String kind = vertex.getKind();
    if (kind == null) {
      return false;
    }

    return kind.equalsIgnoreCase(MapperDAGVertex.DAG_JOIN_VERTEX);
  }

  /**
   * Tests if a vertex is of type init.
   *
   * @param vertex
   *          the vertex
   * @return true, if is inits the
   */
  public static boolean isInit(final DAGVertex vertex) {

    final String kind = vertex.getKind();
    if (kind == null) {
      return false;
    }

    return kind.equalsIgnoreCase(MapperDAGVertex.DAG_INIT_VERTEX);
  }

  /**
   * Tests if a vertex is of type init.
   *
   * @param vertex
   *          the vertex
   * @return true, if is end
   */
  public static boolean isEnd(final DAGVertex vertex) {

    final String kind = vertex.getKind();
    if (kind == null) {
      return false;
    }

    return kind.equalsIgnoreCase(MapperDAGVertex.DAG_END_VERTEX);
  }

}
