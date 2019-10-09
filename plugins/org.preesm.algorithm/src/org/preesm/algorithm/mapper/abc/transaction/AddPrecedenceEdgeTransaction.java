/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2008 - 2012)
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
import org.preesm.algorithm.mapper.model.MapperDAG;
import org.preesm.algorithm.mapper.model.MapperDAGVertex;
import org.preesm.algorithm.mapper.model.special.PrecedenceEdge;

/**
 * Transaction executing the addition of a {@link PrecedenceEdge}.
 *
 * @author mpelcat
 */
public class AddPrecedenceEdgeTransaction implements Transaction {

  // Inputs

  /** Implementation DAG to which the edge is added. */
  private MapperDAG implementation = null;

  /** Source of the added edge. */
  private MapperDAGVertex source = null;

  /** Destination of the added edge. */
  private MapperDAGVertex destination = null;

  // Generated objects
  /** edges added. */
  private PrecedenceEdge precedenceEdge = null;

  /**
   * Instantiates a new adds the precedence edge transaction.
   *
   * @param implementation
   *          the implementation
   * @param source
   *          the source
   * @param destination
   *          the destination
   */
  public AddPrecedenceEdgeTransaction(final MapperDAG implementation, final MapperDAGVertex source,
      final MapperDAGVertex destination) {
    super();
    this.destination = destination;
    this.implementation = implementation;
    this.source = source;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.preesm.mapper.abc.transaction.Transaction#execute(java.util.List)
   */
  @Override
  public void execute(final List<MapperDAGVertex> resultList) {
    this.precedenceEdge = new PrecedenceEdge(this.source);
    this.precedenceEdge.getTiming().setCost(0);
    this.implementation.addEdge(this.source, this.destination, this.precedenceEdge);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.preesm.mapper.abc.transaction.Transaction#toString()
   */
  @Override
  public String toString() {
    return ("AddPrecedence(" + this.precedenceEdge.toString() + ")");
  }

}
