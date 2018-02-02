package org.ietr.preesm.throughput.helpers.test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.preesm.deadlock.IBSDFConsistency;
import org.ietr.preesm.throughput.tools.helpers.GraphStructureHelper;
import org.ietr.preesm.throughput.tools.helpers.Stopwatch;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test of GraphStrucutureHelper class
 * 
 * @author hderoui
 *
 */
public class GraphStrucutureHelperTest_detail {

  @Test
  @Ignore
  public void testTopologicalSorting() {
    // create the DAG to sort
    SDFGraph dag = new SDFGraph();
    dag.setName("dag");
    GraphStructureHelper.addActor(dag, "0", null, 1, 1., null, null);
    GraphStructureHelper.addActor(dag, "1", null, 1, 1., null, null);
    GraphStructureHelper.addActor(dag, "2", null, 1, 1., null, null);
    GraphStructureHelper.addActor(dag, "3", null, 1, 1., null, null);
    GraphStructureHelper.addActor(dag, "4", null, 1, 1., null, null);
    GraphStructureHelper.addActor(dag, "5", null, 1, 1., null, null);

    GraphStructureHelper.addEdge(dag, "5", null, "2", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "5", null, "0", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "4", null, "0", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "4", null, "1", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "2", null, "3", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "3", null, "1", null, 1, 1, 0, null);

    // expected result of the topological sorting
    ArrayList<SDFAbstractVertex> expectedList = new ArrayList<>();
    expectedList.add(0, dag.getVertex("5"));
    expectedList.add(1, dag.getVertex("4"));
    expectedList.add(2, dag.getVertex("2"));
    expectedList.add(3, dag.getVertex("3"));
    expectedList.add(4, dag.getVertex("1"));
    expectedList.add(5, dag.getVertex("0"));

    // topological sorting
    Stopwatch timer = new Stopwatch();
    timer.start();
    ArrayList<SDFAbstractVertex> topologicalSorting = GraphStructureHelper.topologicalSorting(dag);
    timer.stop();

    System.out.println("topological sorting computed in " + timer.toString() + ", the ordered actors: ");

    // check the results
    Assert.assertNotNull(topologicalSorting);
    Assert.assertEquals(dag.vertexSet().size(), topologicalSorting.size());

    for (int i = 0; i < topologicalSorting.size(); i++) {
      System.out.print(topologicalSorting.get(i).getName() + " ");
      Assert.assertEquals(expectedList.get(i).getName(), topologicalSorting.get(i).getName());
    }

    System.out.println("\nPartial topological sorting computed in " + timer.toString() + ", the ordered actors: ");
    ArrayList<SDFAbstractVertex> partialTopologicalSorting = GraphStructureHelper.partialTopologicalSorting(dag.getVertex("5"));
    for (int i = 0; i < partialTopologicalSorting.size(); i++) {
      System.out.print(partialTopologicalSorting.get(i).getName() + " ");
    }

  }

  @Test
  public void testLongestpath() {
    // create the DAG to sort
    SDFGraph dag = new SDFGraph();
    dag.setName("dag");
    GraphStructureHelper.addActor(dag, "0", null, 1, 3., null, null);
    GraphStructureHelper.addActor(dag, "1", null, 1, 6., null, null);
    GraphStructureHelper.addActor(dag, "2", null, 1, 5., null, null);
    GraphStructureHelper.addActor(dag, "3", null, 1, -1., null, null);
    GraphStructureHelper.addActor(dag, "4", null, 1, -3., null, null);
    GraphStructureHelper.addActor(dag, "5", null, 1, 1., null, null);

    GraphStructureHelper.addEdge(dag, "0", null, "1", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "0", null, "2", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "1", null, "3", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "1", null, "2", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "2", null, "4", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "2", null, "5", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "2", null, "3", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "3", null, "5", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "3", null, "4", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(dag, "4", null, "5", null, 1, 1, 0, null);

    // topological sorting
    Stopwatch timer = new Stopwatch();
    timer.start();
    ArrayList<SDFAbstractVertex> topologicalSorting = GraphStructureHelper.topologicalSorting(dag);
    timer.stop();

    System.out.println("topological sorting computed in " + timer.toString() + ", the ordered actors: ");

    for (int i = 0; i < topologicalSorting.size(); i++) {
      System.out.print(topologicalSorting.get(i).getName() + " ");
    }

    System.out.println("\nPartial topological sorting computed in " + timer.toString() + ", the ordered actors: ");
    ArrayList<SDFAbstractVertex> partialTopologicalSorting = GraphStructureHelper.partialTopologicalSorting(dag.getVertex("0"));
    for (int i = 0; i < partialTopologicalSorting.size(); i++) {
      System.out.print(partialTopologicalSorting.get(i).getName() + " ");
    }

    timer.start();
    Hashtable<String, Double> distance = GraphStructureHelper.getLongestPathToAllTargets(dag.getVertex("0"), null, partialTopologicalSorting);
    timer.stop();

    System.out.println("Longest paths computed in " + timer.toString() + ", the distance: ");

    for (Entry<String, Double> e : distance.entrySet()) {
      System.out.println(e.getKey() + " : " + e.getValue());
    }

    timer.start();
    double lp = GraphStructureHelper.getLongestPath(dag, null, null);
    timer.stop();
    System.out.println("Longest paths in the graph " + lp + " computed in " + timer.toString());

  }

  /**
   * generate an IBSDF graph to test methods
   * 
   * @return IBSDF graph
   */
  public SDFGraph generateIBSDFGraph3levels() {
    // Actors: A B[D[GH]EF] C
    // actor B and D are hierarchical

    // level 0 (toplevel): A B C
    // level 1: D E F
    // level 2: GH

    // create the subgraph GH
    SDFGraph GH = new SDFGraph();
    GH.setName("subgraph");
    GraphStructureHelper.addActor(GH, "G", null, null, 1., null, null);
    GraphStructureHelper.addActor(GH, "H", null, null, 1., null, null);
    GraphStructureHelper.addInputInterface(GH, "f", null, 0., null, null);
    GraphStructureHelper.addOutputInterface(GH, "e", null, 0., null, null);

    GraphStructureHelper.addEdge(GH, "f", null, "G", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(GH, "G", null, "F", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(GH, "H", null, "e", null, 1, 1, 0, null);

    // create the subgraph DEF
    SDFGraph DEF = new SDFGraph();
    DEF.setName("subgraph");
    GraphStructureHelper.addActor(DEF, "D", GH, null, 1., null, null);
    GraphStructureHelper.addActor(DEF, "E", null, null, 1., null, null);
    GraphStructureHelper.addActor(DEF, "F", null, null, 1., null, null);
    GraphStructureHelper.addInputInterface(DEF, "a", null, 0., null, null);
    GraphStructureHelper.addOutputInterface(DEF, "c", null, 0., null, null);

    GraphStructureHelper.addEdge(DEF, "a", null, "E", null, 2, 1, 0, null);
    GraphStructureHelper.addEdge(DEF, "E", null, "F", null, 2, 3, 0, null);
    GraphStructureHelper.addEdge(DEF, "F", null, "D", "f", 1, 2, 0, null);
    GraphStructureHelper.addEdge(DEF, "D", "e", "E", null, 3, 1, 3, null);
    GraphStructureHelper.addEdge(DEF, "F", null, "c", null, 3, 1, 0, null);

    // create the top graph and add the subgraph to the hierarchical actor B
    SDFGraph topgraph = new SDFGraph();
    topgraph.setName("topgraph");
    GraphStructureHelper.addActor(topgraph, "A", null, null, 1., null, null);
    GraphStructureHelper.addActor(topgraph, "B", DEF, null, null, null, null);
    GraphStructureHelper.addActor(topgraph, "C", null, null, 1., null, null);

    GraphStructureHelper.addEdge(topgraph, "A", null, "B", "a", 3, 2, 3, null);
    GraphStructureHelper.addEdge(topgraph, "B", "c", "C", null, 1, 1, 0, null);
    GraphStructureHelper.addEdge(topgraph, "C", null, "A", null, 2, 3, 3, null);

    IBSDFConsistency.computeRV(topgraph);
    return topgraph;
  }

}
