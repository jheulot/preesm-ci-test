/*******************************************************************************
 * Copyright or © or Copr. 2012 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
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

package org.ietr.preesm.mapper.model;

import org.ietr.dftools.algorithm.factories.ModelVertexFactory;
import org.ietr.dftools.algorithm.model.AbstractVertex;
import org.ietr.dftools.algorithm.model.IInterface;
import org.ietr.dftools.algorithm.model.dag.DAGVertex;
import org.w3c.dom.Element;

// TODO: Auto-generated Javadoc
/**
 * Creates vertices of type {@link MapperDAGVertex}.
 *
 * @author mpelcat
 */
public class MapperVertexFactory extends ModelVertexFactory<DAGVertex> {

  /** The instance. */
  private static MapperVertexFactory instance;

  /**
   * Instantiates a new mapper vertex factory.
   */
  private MapperVertexFactory() {
    super();
  }

  /**
   * Gets the single instance of MapperVertexFactory.
   *
   * @return single instance of MapperVertexFactory
   */
  public static MapperVertexFactory getInstance() {
    if (MapperVertexFactory.instance == null) {
      MapperVertexFactory.instance = new MapperVertexFactory();
    }
    return MapperVertexFactory.instance;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.algorithm.factories.ModelVertexFactory#createVertex(java.lang.String)
   */
  @Override
  public DAGVertex createVertex(final String kind) {
    final DAGVertex result = new MapperDAGVertex();
    result.setKind(kind);
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.algorithm.factories.ModelVertexFactory#createVertex(org.w3c.dom.Element)
   */
  @Override
  public DAGVertex createVertex(final Element vertexElt) {
    final String kind = getProperty(vertexElt, AbstractVertex.KIND);
    return this.createVertex(kind);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.algorithm.factories.ModelVertexFactory#createInterface(java.lang.String, int)
   */
  @Override
  public IInterface createInterface(final String name, final int dir) {
    return null;
  }

}
