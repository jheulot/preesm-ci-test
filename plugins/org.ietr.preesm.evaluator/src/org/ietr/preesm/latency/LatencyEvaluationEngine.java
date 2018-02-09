/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2018) :
 *
 * Hamza Deroui <hamza.deroui@insa-rennes.fr> (2018)
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
package org.ietr.preesm.latency;

import java.util.ArrayList;
import java.util.Hashtable;
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFEdge;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.throughput.tools.helpers.GraphStructureHelper;
import org.ietr.preesm.throughput.tools.helpers.Stopwatch;
import org.ietr.preesm.throughput.tools.transformers.SDFTransformer;

/**
 * @author hderoui
 *
 */
public class LatencyEvaluationEngine {

  // list of replacement graphs
  private Hashtable<String, SDFGraph> replacementSubgraphlList;
  private PreesmScenario              _scenario;
  public Stopwatch                    timer;

  /**
   * computes the maximum latency of the IBSDF graph which is equivalent to a single core execution
   * 
   * @return maxLatency
   */
  public double getMinLatencySingleCore(SDFGraph graph, PreesmScenario scenario) {
    timer = new Stopwatch();
    timer.start();

    // sum l(a)*rv_global(a) -- not the local RV
    double minLatencySingleCore = 0;

    // loop actors of the graph
    for (SDFAbstractVertex actor : graph.vertexSet()) {
      double actorLatency = 0;

      // define the latency of the actor
      if (actor.getGraphDescription() != null) {
        // case of hierarchical actor : compute its subgraph latency
        actorLatency = getSubgraphMinLatencySinlgeCore(actor, scenario);
      } else {
        // case of regular actor : get its latency from the scenario
        if (scenario != null) {
          actorLatency = scenario.getTimingManager().getTimingOrDefault(actor.getId(), "x86").getTime();
        } else {
          actorLatency = (double) actor.getPropertyBean().getValue("duration");
        }
      }

      // multiply the actor latency by its repetition factor
      minLatencySingleCore += actorLatency * actor.getNbRepeatAsInteger();
    }

    timer.stop();
    System.out.println("Minimum Latency of the graph = " + minLatencySingleCore + " computed in " + timer.toString());

    return minLatencySingleCore;
  }

  /**
   * computes the maximum latency of a subgraph
   * 
   * @return subgraph latency
   */
  public double getSubgraphMinLatencySinlgeCore(SDFAbstractVertex hierarchicalActor, PreesmScenario scenario) {
    // sum l(a)*rv_global(a) -- not the local RV
    double subgraphLatency = 0;

    // get the subgraph
    SDFGraph subgraph = (SDFGraph) hierarchicalActor.getGraphDescription();

    // loop actors of the subgraph
    for (SDFAbstractVertex actor : subgraph.vertexSet()) {
      double actorLatency = 0;

      // define the latency of the actor
      if (actor.getGraphDescription() != null) {
        // case of hierarchical actor : compute its subgraph latency
        actorLatency = getSubgraphMinLatencySinlgeCore(actor, scenario);
      } else {
        // case of regular actor : get its latency from the scenario
        if (scenario != null) {
          actorLatency = scenario.getTimingManager().getTimingOrDefault(actor.getId(), "x86").getTime();
        } else {
          actorLatency = (double) actor.getPropertyBean().getValue("duration");
        }
      }

      // multiply the actor latency by its repetition factor
      subgraphLatency += actorLatency * actor.getNbRepeatAsInteger();
    }

    return subgraphLatency;
  }

  /**
   * computes the minimum latency of the IBSDF graph which is equivalent to a multi-core execution with unlimited number of available cores
   * 
   * @return minLatency
   */
  public double getMinLatencyMultiCore(SDFGraph graph, PreesmScenario scenario, Boolean retiming) {

    /*
     * Algorithm
     * 
     * 
     * Step 1: Construct the replacement subgraph of the top graph hierarchical actors
     * 
     * Step 2: convert the top graph to a DAG
     * 
     * Step 3: replace the hierarchical actors by their replacement subgraph
     * 
     * Step 4: compute the longest path of the top graph
     * 
     */

    this._scenario = scenario;

    // re-time the IBSDF graph
    if (retiming) {
      GraphStructureHelper.retime(graph);
      System.out.println("Computing the minimum Latency of the graph using the decomposition technique after a retinming phase ...");
    } else {
      System.out.println("Computing the minimum Latency of the graph using the decomposition technique ...");
    }

    timer = new Stopwatch();
    timer.start();

    // Step 1: Construct the replacement subgraph of the top graph hierarchical actors
    System.out.println("Step 1: Construct the replacement subgraph of toprgraph hierarchical actors");
    this.replacementSubgraphlList = new Hashtable<String, SDFGraph>();
    for (SDFAbstractVertex actor : graph.vertexSet()) {
      if (actor.getGraphDescription() != null) {
        process(actor, (SDFGraph) actor.getGraphDescription());
      }
    }

    // Step 2: convert the top graph to a DAG
    System.out.println("Step 2: convert the top graph to a DAG");
    SDFGraph topgraph_dag = SDFTransformer.convertToDAG(graph);

    // Step 3: replace the hierarchical actors by their replacement subgraph
    System.out.println("Step 3: replace the hierarchical actors by their replacement subgraph");
    ArrayList<SDFAbstractVertex> actorToReplace = new ArrayList<SDFAbstractVertex>();
    for (SDFAbstractVertex actor : topgraph_dag.vertexSet()) {
      if (((SDFAbstractVertex) actor.getPropertyBean().getValue("baseActor")).getGraphDescription() != null) {
        actorToReplace.add(actor);
      }
    }
    for (SDFAbstractVertex actor : actorToReplace) {
      SDFAbstractVertex baseActor = (SDFAbstractVertex) actor.getPropertyBean().getValue("baseActor");
      GraphStructureHelper.replaceHierarchicalActor(topgraph_dag, actor, replacementSubgraphlList.get(baseActor.getName()));
    }

    // Step 4: compute the longest path of the top graph
    System.out.println("Step 4: compute the longest path of the top graph");
    double minLatency = GraphStructureHelper.getLongestPath(topgraph_dag, scenario, null);

    timer.stop();
    System.out.println("Minimum Latency of the graph = " + minLatency + " computed in " + timer.toString());

    return minLatency;
  }

  private void process(SDFAbstractVertex h, SDFGraph subgraph) {
    // Step1: process the hierarchical actors of the subgraph
    for (SDFAbstractVertex actor : subgraph.vertexSet()) {
      if (actor.getGraphDescription() != null) {
        process(actor, (SDFGraph) actor.getGraphDescription());
      }
    }

    // Step 2: convert the subgraph to a DAG
    SDFGraph subgraph_dag = SDFTransformer.convertToDAG(subgraph);

    // Step 3: replace the hierarchical actors by their replacement subgraph
    ArrayList<SDFAbstractVertex> actorToReplace = new ArrayList<SDFAbstractVertex>();
    ArrayList<String> subgraphExecutionModelToRemove = new ArrayList<String>();
    for (SDFAbstractVertex actor : subgraph_dag.vertexSet()) {
      SDFAbstractVertex baseActor = (SDFAbstractVertex) actor.getPropertyBean().getValue("baseActor");
      if (baseActor.getGraphDescription() != null) {
        actorToReplace.add(actor);
        // add the parent actor to the list of subgraph execution model to remove
        if (!subgraphExecutionModelToRemove.contains(baseActor.getName())) {
          subgraphExecutionModelToRemove.add(baseActor.getName());
        }
      }
    }
    for (SDFAbstractVertex actor : actorToReplace) {
      SDFAbstractVertex baseActor = (SDFAbstractVertex) actor.getPropertyBean().getValue("baseActor");
      GraphStructureHelper.replaceHierarchicalActor(subgraph_dag, actor, replacementSubgraphlList.get(baseActor.getName()));
    }

    // delete all replacement graphs that are no longer needed
    for (String actor : subgraphExecutionModelToRemove) {
      replacementSubgraphlList.remove(actor);
    }

    // Step 4: compute the longest path of the subgraph
    SDFGraph replGraph = constructReplacementGraph(h, subgraph_dag);

    // save the replacement graph
    replacementSubgraphlList.put(h.getName(), replGraph);
  }

  /**
   * construct the replacement graph of the hierarchical actor
   * 
   * @param h
   *          hierarchical actor
   * @param subgraph_dag
   *          DAG version of the subgraph in which all the sub-hierarchical actor was replaced by its replacement graph
   * @return replacement graph of the hierarchical actor
   */
  private SDFGraph constructReplacementGraph(SDFAbstractVertex h, SDFGraph subgraph_dag) {
    // version simple

    // construct the replacement graph of the hierarchical actor
    SDFGraph replGraph = new SDFGraph();

    // Step 1: define the list of inputs and outputs
    ArrayList<SDFAbstractVertex> inputActors = new ArrayList<>();
    ArrayList<SDFAbstractVertex> outputActors = new ArrayList<>();

    // loop actors
    for (SDFAbstractVertex actor : subgraph_dag.vertexSet()) {
      // check if the actor has no inputs
      if (actor.getSources().isEmpty()) {
        inputActors.add(actor);

        // create the associated actor in the replacement graph
        GraphStructureHelper.addActor(replGraph, actor.getName(), null, actor.getNbRepeatAsInteger(), 0., null,
            (SDFAbstractVertex) actor.getPropertyBean().getValue("baseActor"));
      }

      // check if the actor has no outputs
      if (actor.getSinks().isEmpty()) {
        outputActors.add(actor);

        // get actor duration
        double duration;
        if (_scenario != null) {
          duration = _scenario.getTimingManager().getTimingOrDefault(actor.getId(), "x86").getTime();
        } else {
          duration = (Double) actor.getPropertyBean().getValue("duration");
        }

        // create the associated actor in the replacement graph
        GraphStructureHelper.addActor(replGraph, actor.getName(), null, actor.getNbRepeatAsInteger(), duration, null,
            (SDFAbstractVertex) actor.getPropertyBean().getValue("baseActor"));
      }
    }

    // Step 2: sort actors
    ArrayList<SDFAbstractVertex> topoSortList = GraphStructureHelper.topologicalSorting(subgraph_dag);

    // table of distances
    Hashtable<String, Double> distance;

    // Step 3: for each input actor compute the longest path to the output actors
    for (SDFAbstractVertex actor : inputActors) {
      distance = GraphStructureHelper.getLongestPathToAllTargets(actor, _scenario, topoSortList);
      // for each output actor (if connected to the current input actor), add an actor with a duration equal
      // to the distance from the input actor and the output actor
      for (SDFAbstractVertex output : outputActors) {
        Double output_distance = distance.get(output.getName());
        if (output_distance != Double.NEGATIVE_INFINITY) {
          // // add a new actor representing the distance between the current input actor and the current output actor
          // SDFAbstractVertex distanceActor = GraphStructureHelper.addActor(replGraph, actor.getName() + "_" + output.getName(), null, 1,
          // distance.get(output.getName()),
          // null,
          // null);
          // // add the duration of the new actor to the scenario
          // // _scenario.getTimingManager().setTiming(distanceActor.getId(), "x86", distance.get(output.getName()).longValue());
          //
          // // ad the input and output edge
          // GraphStructureHelper.addEdge(replGraph, actor.getName(), null, distanceActor.getName(), null, 1, 1, 0, null);
          // GraphStructureHelper.addEdge(replGraph, distanceActor.getName(), null, output.getName(), null, 1, 1, 0, null);

          // add edge
          SDFEdge e = GraphStructureHelper.addEdge(replGraph, actor.getName(), null, output.getName(), null, 1, 1, 0, null);
          e.setPropertyValue("weight_LP", output_distance);
        }
      }

    }

    // Step 4: simplify the replacement graph

    return replGraph;
  }

}