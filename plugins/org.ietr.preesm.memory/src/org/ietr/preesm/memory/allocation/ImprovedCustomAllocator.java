/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2012 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2015)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2012 - 2013)
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
package org.ietr.preesm.memory.allocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import org.ietr.preesm.memory.bounds.OstergardSolver;
import org.ietr.preesm.memory.exclusiongraph.MemoryExclusionGraph;
import org.ietr.preesm.memory.exclusiongraph.MemoryExclusionVertex;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

// TODO: Auto-generated Javadoc
/**
 * This implementation of the MemoryAllocator mainly is based on a custom algorithm. <br>
 * The algorithm used in this implementation is based on a coloring approach of a MemoryExclusionGraph derived from the
 * input graph. A description of the algorithm is made in allocate() comments.
 *
 * @author kdesnos
 * @deprecated This algorithm hasn't been updated to fulfill the alignment constraints. (2013/10/23)
 */
@Deprecated
public class ImprovedCustomAllocator extends MemoryAllocator {

  /**
   * Constructor of the allocator taking a Memory Exclusion Graph Node as a Parameter. <br>
   * This constructor was created in order to avoid rebuilding a previously built exclusion graph.
   *
   * @param memExclusionGraph
   *          The exclusion graph whose vertices are to allocate in memory
   */
  public ImprovedCustomAllocator(final MemoryExclusionGraph memExclusionGraph) {
    super(memExclusionGraph);
  }

  /**
   * This implementation is based on a custom algorithm. Its major steps are the following :<br>
   * <b>1 -</b> Build G<sub>exclu</sub>the MemoryExclusionGraph from this.graph<br>
   * <b>2 -</b> Get G<sub>inclu</sub> the complementary to G<sub>exclu</sub> exclusion graph<br>
   * <b>3 -</b> Let i := 0<br>
   * <b>4 -</b> Find C<sub>i</sub> the maximum-weight clique in G<sub>inclu</sub>.(Each element <i>elt</i> of
   * C<sub>i</sub> is then a vertex of the clique)<br>
   * <b>5 -</b> Let CWeight := maximum<sub>i</sub>(weight(<i>elt<sub>i</sub></i>))<br>
   * <b>6 -</b> For each element <i>elt</i> of C<sub>i</sub> (in descending order of weights)<br>
   * <b>6.1 -</b> For each neighbor <i>neigh</i> of <i>elt</i> (excluding neighbors from previous elements) in
   * G<sub>exclu</sub> (in descending order of weights)<br>
   * <b>6.1.1 -</b> Let NWeight := <i>neigh</i>.weight + weight(<i>elt</i>)<br>
   * <b>6.1.2 -</b> If (NWeight < CWeight + <i>neigh</i>.weight) Then add <i>neigh</i> to elt and Goto(5)<br>
   * <b>7 -</b> Remove all vertices of C<sub>i</sub> from G<sub>exclu</sub> and G<sub>inclu</sub><br>
   * <b>8 -</b> If G<sub>exclu</sub> is not empty Then i = i+1 and Goto (4)<br>
   * <b>9 -</b> Let cliqueOffset := 0<br>
   * <b>10 -</b> For each C<sub>i</sub><br>
   * <b>10.1 -</b>For each <i>elt</i> of C<sub>i</sub><br>
   * <b>10.1.1 -</b>Let offset <i> o</i> := cliqueOffset<br>
   * <b>10.1.2 -</b>For each vertex <i>v</i> of <i>elt</i><br>
   * <b>10.1.2.1 -</b> Put <i>v</i> in allocation with offset <i>o</i><br>
   * <b>10.1.2.2 -</b> Let o:=o+v.weight<br>
   * <b>10.2 -</b> cliqueOffset := cliqueOffset + maximum<sub>i</sub>(weight(<i>elt<sub>i</sub></i>))<br>
   * <br>
   * G<sub>exclu</sub> := < V,E ><br>
   * V := {vert<sub>1</sub>, vert<sub>2</sub>, ... ,vert<sub>n</sub>}<br>
   * E := { (vert<sub>i</sub>,vert<sub>j</sub>); i!=j}<br>
   * C<sub>i</sub> := { elt<sub>1</sub>, elt<sub>2</sub>, ...}<br>
   * elt := {vert<sub>i</sub>, vert<sub>j</sub>,...}<br>
   * weight(elt) := vert<sub>i</sub>.weight + vert<sub>j</sub>.weight + ...
   */
  @Override
  public void allocate() {
    clear();

    // Logger logger = WorkflowLogger.getLogger();
    // (1)
    final MemoryExclusionGraph exclusionGraph = (MemoryExclusionGraph) this.inputExclusionGraph.clone();

    // (2)
    // logger.log(Level.INFO, "2 - Get Complementary");
    final SimpleGraph<MemoryExclusionVertex, DefaultEdge> inclusionGraph = exclusionGraph.getComplementary();

    // (9)
    int cliqueOffset = 0;

    // (8)
    while (!exclusionGraph.vertexSet().isEmpty()) {
      // (4)
      // TODO Remplacer par solver user define
      OstergardSolver<MemoryExclusionVertex, DefaultEdge> ostSolver;
      ostSolver = new OstergardSolver<>(inclusionGraph);
      // logger.log(Level.INFO, "3 - Stable Set");
      ostSolver.solve();
      final Set<MemoryExclusionVertex> cliqueSet = ostSolver.getHeaviestClique();

      // Allocate Clique elements
      for (final MemoryExclusionVertex node : cliqueSet) {
        // (10) Allocate clique elements
        allocateMemoryObject(node, cliqueOffset);
      }

      // This boolean is used to iterate over the list as long as a vertex
      // is added to an element of the list during an iteration
      boolean loopAgain = !cliqueSet.isEmpty(); // Loop only if clique is
      // not
      // empty (should always
      // be
      // true when reached...)

      // the cliqueWeight will store the weight of the current max element
      // of clique
      int cliqueWeight = Collections.max(cliqueSet).getWeight();
      final int maximumSize = 2 * cliqueWeight;

      ArrayList<MemoryExclusionVertex> nonAllocatedVertex;
      nonAllocatedVertex = new ArrayList<>(exclusionGraph.vertexSet());
      Collections.sort(nonAllocatedVertex, Collections.reverseOrder());

      while (loopAgain) {
        loopAgain = false;

        for (final MemoryExclusionVertex vertex : nonAllocatedVertex) {
          // Get vertex neighbors
          final Set<MemoryExclusionVertex> neighbors = exclusionGraph.getAdjacentVertexOf(vertex);

          // The offset to begin the search
          int offset = cliqueOffset;

          // This boolean indicate that the offset chosen for the
          // vertex is
          // compatible with all the neighbors that are already
          // allocated.
          boolean validOffset = false;

          // Iterate until a valid offset is found (this offset will
          // be the
          // smallest possible)
          while (!validOffset) {
            validOffset = true;
            for (final MemoryExclusionVertex neighbor : neighbors) {
              Integer neighborOffset;
              if ((neighborOffset = this.memExNodeAllocation.get(neighbor)) != null) {

                if ((neighborOffset < (offset + vertex.getWeight()))
                    && ((neighborOffset + neighbor.getWeight()) > offset)) {
                  validOffset = false;
                  offset += neighbor.getWeight();
                  break;
                }
              }
            }
          }
          // Allocate the vertex at the resulting offset if the set is
          // not enlarged by the weight of the node
          if (((offset - cliqueOffset) < cliqueWeight)
              && (((offset - cliqueOffset) + vertex.getWeight()) < maximumSize)) {
            allocateMemoryObject(vertex, offset);
            cliqueSet.add(vertex);
            nonAllocatedVertex.remove(vertex);
            loopAgain = true;
            cliqueWeight = (((offset - cliqueOffset) + vertex.getWeight()) > cliqueWeight)
                ? (offset - cliqueOffset) + vertex.getWeight()
                : cliqueWeight;
            break;
          }
        }
      }
      // ABORTED
      // // Improvement Try to add neighbors of cliqueSet
      // // neighbors are treated in decreasing weight order and added in
      // the best-fit fashion.
      // // However, the CWeight cannot be changed !
      //
      // // Sort the neighbors list in decreasing order of weight
      // Collections.sort(treatedNeighbors,Collections.reverseOrder());
      //
      // // Best-fit alloc aborted
      // for(MemoryExclusionGraphNode currentNeighbor : treatedNeighbors){
      // // retrieve the vertices of Ci that are neighbors of the current
      // neighbor.
      // LinkedHashSet<MemoryExclusionGraphNode> adjacent =
      // exclusionGraph.getAdjacentVertexOf(currentNeighbor);
      // adjacent.retainAll(cliqueSet);
      //
      // // Retrieve all the exclusions. Each node has a starting address
      // (its offset) and occupy memory up to (offset + size)
      // // The current neighbor cannot be allocated between those
      // addresses.
      // ArrayList<Integer> excludeFrom = new
      // ArrayList<Integer>(adjacent.size());
      // ArrayList<Integer> excludeTo = new
      // ArrayList<Integer>(adjacent.size());
      // for(MemoryExclusionGraphNode adjacentNode : adjacent){
      // excludeFrom.add(memExNodeAllocation.get(adjacentNode));
      // excludeTo.add(memExNodeAllocation.get(adjacentNode)+
      // adjacentNode.getWeight());
      // }
      //
      // // merge the two lists
      // Collections.sort(excludeFrom);
      // Collections.sort(excludeTo);
      // Iterator<Integer> iterFrom = excludeFrom.iterator();
      // Iterator<Integer> iterTo = excludeFrom.iterator();
      //
      //
      // int from = 0;
      // int to = 0;
      // int freeFrom = 0;
      // int offset = -1;
      // float occupation = (float)0.0; // Occupation rate of the best
      // fitted space (<=1)
      // int numberExcludingElements = 0;
      //
      // while(iterFrom.hasNext() && iterTo.hasNext()){
      // if(from <= to){
      // // If this is the end of a free space.
      // if(numberExcludingElements == 0 ){
      // // check if neighbor fits in the space left empty
      // if(currentNeighbor.getWeight() <= (freeFrom - from)){
      // // It fits ! But, does it BEST fits ?
      // if(((float)currentNeighbor.getWeight()/(float)(freeFrom - from))
      // > occupation){
      // // It does best fit !(yet)
      // occupation = ((float)currentNeighbor.getWeight()/(float)(freeFrom
      // - from));
      // offset = freeFrom;
      // }
      // }
      // }
      // numberExcludingElements++;
      // from = iterFrom.next();
      // }
      //
      // }
      //
      // }

      // (7)
      // logger.log(Level.INFO, "10 - Remmoving vertex");
      // logger.log(Level.INFO, "Vertex "+ cliqueSet);
      exclusionGraph.removeAllVertices(cliqueSet);
      inclusionGraph.removeAllVertices(cliqueSet);
      cliqueOffset += cliqueWeight;
    }
    // logger.log(Level.INFO, "11 - Over");
  }

  /**
   * This method is used to retrieve the sum of weight of a set of vertices.
   *
   * @param set
   *          the set of vertices to treat
   * @return the sum of the vertices weight
   */
  protected int weight(final Set<MemoryExclusionVertex> set) {
    int result = 0;

    for (final MemoryExclusionVertex vertex : set) {
      result += vertex.getWeight().intValue();
    }
    return result;
  }

  /**
   * This method is used to order a list of elements wher each element is a set of vertices. The resulting list is
   * ordered in decreasing weight order.
   *
   * @param elementList
   *          the list to order.
   */
  protected void orderElementList(final ArrayList<Set<MemoryExclusionVertex>> elementList) {
    // Define a comparator of list elements. The weight of an element is
    // used for comparison
    final Comparator<Set<MemoryExclusionVertex>> comparator = (o1, o2) -> (weight(o2) - weight(o1));
    Collections.sort(elementList, comparator);
  }

  /**
   * This method return the maximum weight of an element of the List. Each element of the list is a set of vertices.
   *
   * @param elementList
   *          the list of set of vertices
   * @param isOrdered
   *          true if the list has been ordered before
   * @return the largest weight of an element
   */
  protected int maxElementWeight(final ArrayList<Set<MemoryExclusionVertex>> elementList, final boolean isOrdered) {
    int result = 0;

    // If the list has been ordered before, just return the weight of the
    // first element
    if (isOrdered && !elementList.isEmpty()) {
      return weight(elementList.get(0));
    }
    // Else, search the list
    for (final Set<MemoryExclusionVertex> element : elementList) {
      final int temp = weight(element);
      if (temp > result) {
        result = temp;
      }
    }
    return result;
  }
}
