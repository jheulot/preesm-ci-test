/*******************************************************************************
 * Copyright or © or Copr. 2012 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Maxime Pelcat <Maxime.Pelcat@insa-rennes.fr> (2012)
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

package org.ietr.preesm.mapper.abc.transaction;

import java.util.List;
import org.ietr.preesm.mapper.model.MapperDAG;
import org.ietr.preesm.mapper.model.MapperDAGEdge;

// TODO: Auto-generated Javadoc
/**
 * A transaction that removes one edge in an implementation.
 *
 * @author mpelcat
 */
public class RemoveEdgeTransaction extends Transaction {
  // Inputs
  /** Implementation DAG from which the edge is removed. */
  private MapperDAG implementation = null;

  /** edge removed. */
  private MapperDAGEdge edge = null;

  /**
   * Instantiates a new removes the edge transaction.
   *
   * @param edge
   *          the edge
   * @param implementation
   *          the implementation
   */
  public RemoveEdgeTransaction(final MapperDAGEdge edge, final MapperDAG implementation) {
    super();
    this.edge = edge;
    this.implementation = implementation;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ietr.preesm.mapper.abc.transaction.Transaction#execute(java.util.List)
   */
  @Override
  public void execute(final List<Object> resultList) {
    super.execute(resultList);

    this.implementation.removeEdge(this.edge);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ietr.preesm.mapper.abc.transaction.Transaction#toString()
   */
  @Override
  public String toString() {
    return ("RemoveEdge(" + this.edge.toString() + ")");
  }

}
