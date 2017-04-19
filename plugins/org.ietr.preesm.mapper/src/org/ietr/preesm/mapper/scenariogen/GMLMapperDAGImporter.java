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
package org.ietr.preesm.mapper.scenariogen;

import org.ietr.dftools.algorithm.factories.SDFEdgeFactory;
import org.ietr.dftools.algorithm.factories.SDFVertexFactory;
import org.ietr.dftools.algorithm.importer.GMLImporter;
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFEdge;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * Importer for DAG graphs produced by the mapper.
 *
 * @author mpelcat
 */
public class GMLMapperDAGImporter extends GMLImporter<SDFGraph, SDFAbstractVertex, SDFEdge> {

  /**
   * COnstructs a new importer for SDF graphs.
   */
  public GMLMapperDAGImporter() {
    super(new SDFEdgeFactory());
  }

  /**
   * Parses an Edge in the DOM document.
   *
   * @param edgeElt
   *          The DOM Element
   * @param parentGraph
   *          The parent Graph of this Edge
   */
  @Override
  public void parseEdge(final Element edgeElt, final SDFGraph parentGraph) {
  }

  /**
   * Parses a Graph in the DOM document.
   *
   * @param graphElt
   *          The graph Element in the DOM document
   * @return The parsed graph
   */
  @Override
  public SDFGraph parseGraph(final Element graphElt) {
    final SDFGraph graph = new SDFGraph((SDFEdgeFactory) this.edgeFactory);
    final NodeList childList = graphElt.getChildNodes();
    parseParameters(graph, graphElt);
    parseVariables(graph, graphElt);
    for (int i = 0; i < childList.getLength(); i++) {
      if (childList.item(i).getNodeName().equals("node")) {
        final Element vertexElt = (Element) childList.item(i);
        parseNode(vertexElt, graph);
      }
    }
    for (int i = 0; i < childList.getLength(); i++) {
      if (childList.item(i).getNodeName().equals("edge")) {
        final Element edgeElt = (Element) childList.item(i);
        parseEdge(edgeElt, graph);
      }
    }
    parseKeys(graphElt, graph);
    return graph;
  }

  /**
   * Parses a Vertex from the DOM document.
   *
   * @param vertexElt
   *          The node Element in the DOM document
   * @param parentGraph
   *          the parent graph
   * @return The parsed node
   */
  @Override
  public SDFAbstractVertex parseNode(final Element vertexElt, final SDFGraph parentGraph) {

    SDFAbstractVertex vertex;
    /*
     * HashMap<String, String> attributes = new HashMap<String, String>(); for (int i = 0; i <
     * vertexElt.getAttributes().getLength(); i++) {
     * attributes.put(vertexElt.getAttributes().item(i).getNodeName(),
     * vertexElt.getAttributes().item(i).getNodeValue()); }
     *
     * attributes.put("kind", SDFVertex.VERTEX);
     */
    vertex = SDFVertexFactory.getInstance().createVertex(vertexElt);

    vertex.setId(vertexElt.getAttribute("id"));

    for (int i = 0; i < vertexElt.getChildNodes().getLength(); i++) {
      final Node n = vertexElt.getChildNodes().item(i);
      if (n.getNodeName().equals("data")) {
        vertex.getPropertyBean().setValue(n.getAttributes().getNamedItem("key").getTextContent(),
            n.getTextContent());
      }
    }

    parseKeys(vertexElt, vertex);
    this.vertexFromId.put(vertex.getId(), vertex);
    parseArguments(vertex, vertexElt);
    return vertex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ietr.dftools.algorithm.importer.GMLImporter#parsePort(org.w3c.dom.Element,
   * org.ietr.dftools.algorithm.model.AbstractGraph)
   */
  @Override
  public SDFAbstractVertex parsePort(final Element portElt, final SDFGraph parentGraph) {
    return null;
  }

}
