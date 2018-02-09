/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2017) :
 *
 * Hamza Deroui <hamza.deroui@insa-rennes.fr> (2017)
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
package org.ietr.preesm.throughput.tools.transformers;

import java.util.Hashtable;
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.dftools.algorithm.model.sdf.esdf.SDFSinkInterfaceVertex;
import org.ietr.dftools.algorithm.model.sdf.esdf.SDFSourceInterfaceVertex;
import org.ietr.preesm.throughput.tools.helpers.GraphStructureHelper;
import org.ietr.preesm.throughput.tools.helpers.Stopwatch;

/**
 * @author hderoui
 *
 *         IBSDF Transformer class : contains the method to transform an IBSDF graph into a flat srSDF graph
 */
public abstract class IBSDFTransformer {

  /**
   * Converts an IBSDF graph to a flat srSDF graph
   * 
   * @param IBSDF
   *          graph
   * @return flat srSDF graph
   */
  public static SDFGraph convertToSrSDF(SDFGraph IBSDF, boolean withExecRulres) {
    Stopwatch timer = new Stopwatch();
    timer.start();

    // Step 1: Convert all the SDF subgraphs to a srSDF subgraphs
    Hashtable<String, SDFGraph> srSDFsubgraphList = convertAllSubgraphs(IBSDF, withExecRulres);

    // Step 2: Convert the top SDF graph to a srSDF graph
    SDFGraph flatSrSDF = SDFTransformer.convertToSrSDF(IBSDF);

    // Step 3: Replace each instance of a hierarchical actor by its srSDF subgraph version
    Hashtable<String, SDFAbstractVertex> actorsToReplcae = GraphStructureHelper.getHierarchicalActors(flatSrSDF);
    while (!actorsToReplcae.isEmpty()) {
      // replace the hierarchical actor
      SDFAbstractVertex h = actorsToReplcae.elements().nextElement();
      SDFGraph srSubgraph = srSDFsubgraphList.get(((SDFAbstractVertex) h.getPropertyBean().getValue("baseActor")).getName());
      GraphStructureHelper.replaceHierarchicalActor(flatSrSDF, h, srSubgraph);

      // add the hierarchical actors of the subgraph to the list of actors to replace
      for (SDFAbstractVertex a : srSubgraph.vertexSet()) {
        if (a.getGraphDescription() != null) {
          String actorInstanceNewName = h.getName() + "_" + a.getName();
          actorsToReplcae.put(actorInstanceNewName, flatSrSDF.getVertex(actorInstanceNewName));
        }
      }

      // remove the hierarchical actor from the list of actors to replace
      actorsToReplcae.remove(h.getName());
    }

    timer.stop();
    System.out.println("IBSDF graph converted to SrSDF graph in " + timer.toString());
    return flatSrSDF;
  }

  /**
   * converts all the SDF subgraphs of the hierarchy into srSDF subgraphs
   * 
   * @param IBSDF
   *          graph
   * @param withExecRulres
   *          boolean, add execution rules if true
   * @return the list of srSDF subgraphs
   */
  private static Hashtable<String, SDFGraph> convertAllSubgraphs(SDFGraph IBSDF, boolean withExecRulres) {
    // the list of srSDF subgraphs
    Hashtable<String, SDFGraph> srSDFsubgraphsList = new Hashtable<String, SDFGraph>();

    // get all the hierarchical actors of the IBSDF graph
    Hashtable<String, SDFAbstractVertex> actorsToConvert = GraphStructureHelper.getAllHierarchicalActors(IBSDF);

    // convert the hierarchical actors SDF subgraphs to a srSDF subgraph
    for (SDFAbstractVertex h : actorsToConvert.values()) {
      // System.out.println("====> converting the subgraph of actor " + h.getId());
      SDFGraph srSDFsubgraph = SDFTransformer.convertToSrSDF((SDFGraph) h.getGraphDescription());
      // add the execution rules to the srSDF subgraph
      if (withExecRulres) {
        addExecRules(srSDFsubgraph);
      }
      srSDFsubgraphsList.put(h.getName(), srSDFsubgraph);
    }

    return srSDFsubgraphsList;
  }

  /**
   * Adds the execution rules to a srSDF subgraph
   * 
   * @param srSDF
   *          graph
   */
  private static void addExecRules(SDFGraph srSDF) {
    // step 1: add the two special actors Start and End actors
    GraphStructureHelper.addActor(srSDF, "start", null, 1, 0., null, null);
    GraphStructureHelper.addActor(srSDF, "end", null, 1, 0., null, null);

    // add the edge back from End actor to Start actor (no need of the edge from start to end)
    GraphStructureHelper.addEdge(srSDF, "end", "to_start", "start", "from_end", 1, 1, 1, null);

    // step 2: add the connection between actors/interfaces and start/end actors
    for (SDFAbstractVertex actor : srSDF.vertexSet()) {
      if (actor.getName() != "start" && actor.getName() != "end") {
        // add the connection between the interface and start/end actor
        SDFAbstractVertex baseActor = (SDFAbstractVertex) actor.getPropertyBean().getValue("baseActor");
        if (baseActor instanceof SDFSourceInterfaceVertex) {
          // input interface
          GraphStructureHelper.addEdge(srSDF, actor.getName(), "to_start", "start", "from_" + actor.getName(), 1, 1, 0, null);
        } else if (baseActor instanceof SDFSinkInterfaceVertex) {
          // output interface
          GraphStructureHelper.addEdge(srSDF, "end", "to_" + actor.getName(), actor.getName(), "from_end", 1, 1, 0, null);
        } else {
          // add the connection between the actor and start/end actor
          GraphStructureHelper.addEdge(srSDF, "start", "to_" + actor.getName(), actor.getName(), "from_start", 1, 1, 0, null);
          GraphStructureHelper.addEdge(srSDF, actor.getName(), "to_end", "end", "from_" + actor.getName(), 1, 1, 0, null);
        }
      }
    }
  }

}