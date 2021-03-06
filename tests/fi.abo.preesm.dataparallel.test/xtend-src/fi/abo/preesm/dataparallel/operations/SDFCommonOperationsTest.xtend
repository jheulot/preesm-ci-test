/**
 * Copyright or © or Copr. Åbo Akademi University (2017 - 2019),
 * IETR/INSA - Rennes (2017 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Sudeep Kanur [skanur@abo.fi] (2017 - 2018)
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
package fi.abo.preesm.dataparallel.operations

import fi.abo.preesm.dataparallel.NodeChainGraph
import fi.abo.preesm.dataparallel.SDF2DAG
import fi.abo.preesm.dataparallel.SrSDFToSDF
import fi.abo.preesm.dataparallel.fifo.FifoActor
import fi.abo.preesm.dataparallel.fifo.FifoActorBeanKey
import fi.abo.preesm.dataparallel.fifo.FifoActorGraph
import fi.abo.preesm.dataparallel.pojo.RetimingInfo
import fi.abo.preesm.dataparallel.test.util.Util
import java.util.Collection
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.graph.AsSubgraph
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.preesm.algorithm.model.sdf.SDFAbstractVertex
import org.preesm.algorithm.model.sdf.SDFEdge
import org.preesm.algorithm.model.sdf.SDFGraph
import org.preesm.algorithm.model.sdf.visitors.ToHSDFVisitor

/**
 * Property based test for {@link RearrangeOperations} on {@link SDFGraph}s
 *
 * @author Sudeep Kanur
 */
@RunWith(Parameterized)
class SDFCommonOperationsTest {
	protected val SDFGraph sdf

	protected val Boolean isAcyclic

	protected val Boolean isInstanceIndependent

	/**
	 * Generates the following parameters from {@link Util#provideAllGraphsContext}:
	 * <ol>
	 * 	<li> {@link SDFGraph} instance
	 * 	<li> <code>true</code> if the SDFG is acyclic, <code>false</code> otherwise
	 *  <li> <code>true</code> if the SDFG is instance independent, <code>false</code> otherwise
	 * </ol>
	 */
	new(SDFGraph sdf
	   , Boolean isAcyclic
	   , Boolean isInstanceIndependent
	) {
		this.sdf = sdf
		this.isAcyclic = isAcyclic
		this.isInstanceIndependent = isInstanceIndependent
	}

	/**
	 * Generates the following parameters from {@link Util#provideAllGraphsContext}:
	 * <ol>
	 * 	<li> {@link SDFGraph} instance
	 * 	<li> <code>true</code> if the SDFG is acyclic, <code>false</code> otherwise
	 *  <li> <code>true</code> if the SDFG is instance independent, <code>false</code> otherwise
	 * </ol>
	 */
	@Parameterized.Parameters
	static def Collection<Object[]> instancesToTest() {
		/*
		 * Contains following parameters
		 * 1. SDFGraph
		 * 2. Is SDFGraph acyclic?
		 * 3. Is SDFGraph instance independent?
		 */
		val parameters = newArrayList

		Util.provideAllGraphsContext.forEach[sdfContext |
			parameters.add(#[sdfContext.graph, sdfContext.isAcyclic, sdfContext.isInstanceIndependent])
		]

		return parameters
	}

	/**
	 * Verify output of {@link AcyclicLikeSubgraphDetector} against manually defined parameter
	 * <p>
	 * <b>Warning!</b> Not a generic test. Test depends on manually defined parameters
	 * <p>
	 * <i>Strong test</i>
	 */
	@Test
	def void sdfIsAcyclic() {
		if(isAcyclic) {
			val acyclicLikeVisitor = new AcyclicLikeSubgraphDetector
			sdf.accept(acyclicLikeVisitor)

			Assert.assertTrue(acyclicLikeVisitor.isAcyclicLike)
		}
	}

	/**
	 * Correctness of {@link RearrangeOperations}. Following tests are carried out:
	 * <ol>
	 * 	<li> The graph that is instance independent and non-acyclic like becomes acyclic like
	 * 	<li> Transient graphs are schedulable
	 * 	<li> The transient graphs have no delays
	 * 	<li> Transient graphs are always acyclic
	 * 	<li> The transient graphs have only {@link FifoActor} in their output
	 * 	<li> FifoActors of {@link FifoActorGraph} does not have duplicate FifoActors in
	 * 	<li> SrSDF edges with delays. Meaning, SrSDF edges with delays have one unique FifoActor
	 * present either in the SrSDF graph edge or in a transient graph.
	 * 	<li> All movable instances are seen in the transient graph
	 * 	<li> All delays are positive
	 * <p>
	 * <i>Strong test</i>
	 */
	@Test
	def void retimeTest() {
		val acyclicLikeVisitor = new AcyclicLikeSubgraphDetector
		sdf.accept(acyclicLikeVisitor)
		if(!acyclicLikeVisitor.isAcyclicLike && isInstanceIndependent) {

			val info = new RetimingInfo(newArrayList)
			val srsdfVisitor = new ToHSDFVisitor
			sdf.accept(srsdfVisitor)
			val srsdf = srsdfVisitor.output

			// For later checks of re-timing transformation
			val transform = new SrSDFToSDF(sdf, srsdf)

			// For checking if all movable instances are seen in the graph
			val allMovableInstances = newArrayList

			acyclicLikeVisitor.SDFSubgraphs.forEach[sdfSubgraph |
				// Get strongly connected components
				val strongCompDetector = new KosarajuStrongConnectivityInspector(sdfSubgraph)

				// Collect strongly connected component that has loops in it
				// Needed because stronglyConnectedSubgraphs also yield subgraphs with no loops
				strongCompDetector.getStronglyConnectedComponents.forEach[ subgraph |
					val cycleDetector = new CycleDetector(subgraph as
						AsSubgraph<SDFAbstractVertex, SDFEdge>
					)
					if(cycleDetector.detectCycles) {
						// ASSUMPTION: Strongly connected component of a directed graph contains atleast
						// one loop. Perform the tests now. As only instance independent graphs are
						// added, no check is made

						// We need not only strongly connected components, but also vertices that
						// connect to the rest of the graph. This is because, calculation of root
						// and exit vertices also depends if there are enough delay tokens at the
						// interface edges.

						val relevantVertices = newLinkedHashSet
						val relevantEdges = newLinkedHashSet
							sdfSubgraph.vertexSet.forEach[vertex |
							if(subgraph.vertexSet.contains(vertex)) {
								sdfSubgraph.incomingEdgesOf(vertex).forEach[edge |
									if(!subgraph.vertexSet.contains(edge.source)) {
										relevantVertices.add(edge.source)
										relevantEdges.add(edge)
									}
								]

								sdfSubgraph.outgoingEdgesOf(vertex).forEach[edge |
									if(!subgraph.vertexSet.contains(edge.target)) {
										relevantVertices.add(edge.target)
										relevantEdges.add(edge)
									}
								]
							}
						]
						relevantVertices.addAll(subgraph.vertexSet)
						relevantEdges.addAll(subgraph.edgeSet)

						val subgraphInterfaceVertices = new AsSubgraph(sdfSubgraph, relevantVertices, relevantEdges)

						val subgraphDAGGen = new SDF2DAG(subgraphInterfaceVertices)

						val sc = new KosarajuStrongConnectivityInspector(sdfSubgraph)
						val sourceActors = sc.stronglyConnectedComponents.filter[sg |
							val cd = new CycleDetector(sg as
								AsSubgraph<SDFAbstractVertex, SDFEdge>
							)
							!cd.detectCycles
						].map[sg |
							sg.vertexSet
						].flatten
						.toList

						val moveInstanceVisitor = new MovableInstances(sourceActors)
						subgraphDAGGen.accept(moveInstanceVisitor)
						allMovableInstances.addAll(moveInstanceVisitor.movableInstances)

						val retimingVisitor = new RearrangeOperations(srsdf, info, sourceActors)
						subgraphDAGGen.accept(retimingVisitor)
					}
				]
			]

			// 1. Check re-timing creates acyclic-like graphs
			val retimedSDF = transform.getRetimedSDF(srsdf)
			val retimedAcyclicLikeVisitor = new AcyclicLikeSubgraphDetector
			retimedSDF.accept(retimedAcyclicLikeVisitor)
			Assert.assertTrue(retimedAcyclicLikeVisitor.isAcyclicLike)


			Assert.assertTrue(!info.initializationGraphs.empty)

			info.initializationGraphs.forEach[graph |
				// 2. Transient graphs are schedulable
				Assert.assertTrue(graph.schedulable)

				// 3. Check transient graph has no delays
				val edgesWithDelays = newArrayList
				graph.edgeSet.forEach[edge |
					if(edge.delay.longValue > 0) {
						edgesWithDelays.add(edge)
					}
				]
				Assert.assertTrue(edgesWithDelays.empty)

				// 4. Transient graphs are always acyclic
				val cycleDetector = new CycleDetector(graph)
				Assert.assertTrue(!cycleDetector.detectCycles)

				// 5. The transient graphs have only {@link FifoActor} in their output
				graph.vertexSet.filter[vertex |
					vertex.sinks.empty
				].forEach[sinkVertex |
					Assert.assertTrue(sinkVertex instanceof FifoActor)
				]
			]

			// 6. SrSDF edges with delays have one unique {@link FifoActor}
			// present either in the SrSDF graph edge or in a transient graph.
			val fifoActorsFromSrSDF = newArrayList
			val fifoActorsFromTransientGraph = newArrayList

			srsdf.edgeSet.forEach[edge |
				val fifoActor = edge.propertyBean.getValue(FifoActorBeanKey.key)
				if(fifoActor !== null) {
					fifoActorsFromSrSDF.add(fifoActor)
				}
			]

			info.initializationGraphs.forEach[graph |
				graph.vertexSet.forEach[vertex |
					if(vertex instanceof FifoActor) {
						// Assure that transient graphs have no duplicate FifoActors
						Assert.assertTrue(!fifoActorsFromTransientGraph.contains(vertex))
						fifoActorsFromTransientGraph.add(vertex)
					}
				]
			]

			fifoActorsFromTransientGraph.forEach[fifoActor |
				Assert.assertTrue(!fifoActorsFromSrSDF.contains(fifoActor))
			]

			// 7. All movable instances are seen in the transient graph
			val allUserAddedInstances = newArrayList // Actors added in SDF
			val nodeChainGraph = new NodeChainGraph(srsdf)
			val movableNodeNames = nodeChainGraph.nodechains.keySet.map[it.name].toList
			val allSignificantMovableInstances = allMovableInstances.filter[vertex |
				movableNodeNames.contains(vertex.name)
			]
			info.initializationGraphs.forEach[graph |
				allUserAddedInstances.addAll(graph.vertexSet.filter[vertex |
					nodeChainGraph.nodechains.keySet.contains(vertex)
				])
			]

			allSignificantMovableInstances.forEach[moveInstance |
				val srsdfMoveInstance = srsdf.getVertex(moveInstance.name)
				Assert.assertTrue(allUserAddedInstances.contains(srsdfMoveInstance))
			]

			allUserAddedInstances.forEach[addedInstance |
				Assert.assertEquals(1, allSignificantMovableInstances.filter[vertex |
					vertex.name == addedInstance.name].size)
			]

			// 8. Check all delays are positive
			srsdf.edgeSet.forEach[edge |
				if(edge.delay.longValue < 0) {
					println(edge)
				}
			]

			Assert.assertTrue(srsdf.edgeSet.forall[edge |
				edge.delay.longValue >= 0
			])
		}
	}
}
