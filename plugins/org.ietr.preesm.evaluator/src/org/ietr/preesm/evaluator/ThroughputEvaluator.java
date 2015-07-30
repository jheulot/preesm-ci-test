package org.ietr.preesm.evaluator;

import org.ietr.dftools.algorithm.model.parameters.InvalidExpressionException;
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFEdge;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.dftools.algorithm.model.sdf.esdf.SDFSourceInterfaceVertex;
import org.ietr.preesm.core.scenario.PreesmScenario;

/**
 * Class used to evaluate the throughput of a SDF or IBSDF graph on its optimal periodic schedule
 * @author blaunay
 *
 */
public abstract class ThroughputEvaluator {
	
	public PreesmScenario scenar;
	
	/**
	 * Launches the evaluation of the throughput
	 * @param inputGraph the graph to evaluate
	 * @return
	 * @throws InvalidExpressionException
	 */
	public abstract double launch(SDFGraph inputGraph) throws InvalidExpressionException;
	
	
	/**
	 * Returns the throughput of the graph given its optimal normalized period,
	 * the throughput is the minimal throughput of an actor of the graph.
	 */
	protected double throughput_computation(double period, SDFGraph sdf) {
		double min_throughput = Double.MAX_VALUE;
		double tmp;
		// use the normalized value Z of every actor
		for (SDFAbstractVertex vertex : sdf.vertexSet()) {
			// Hierarchical vertex, go look for its internal actors
			if (vertex.getGraphDescription() != null
					&& vertex.getGraphDescription() instanceof SDFGraph) {
				tmp = throughput_computation(period, (SDFGraph)vertex.getGraphDescription());
			} else {
				// throughput actor = 1/(K*Z)
				if (vertex.getInterfaces().get(0) instanceof SDFSourceInterfaceVertex){
					tmp = 1.0/(period *(double)(((SDFEdge) vertex.getAssociatedEdge(vertex.getInterfaces().get(0)))
							.getCons().getValue()));
				} else {
					tmp = 1.0/(period *(double)(((SDFEdge) vertex.getAssociatedEdge(vertex.getInterfaces().get(0)))
							.getProd().getValue()));
				}
			}
			// We are looking for the actor with the smallest throughput
			if (tmp < min_throughput)
				min_throughput = tmp;
		}
		return min_throughput;
	}
}
