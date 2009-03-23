/**
 * 
 */
package org.ietr.preesm.core.architecture.route;

import java.util.List;

import org.ietr.preesm.core.architecture.simplemodel.AbstractNode;
import org.ietr.preesm.core.architecture.simplemodel.Operator;

/**
 * Represents a single step in a route between two operators separated by
 * contention nodes and parallel nodes
 * 
 * @author mpelcat
 */
public class NodeRouteStep extends AbstractRouteStep {

	/**
	 * Communication nodes separating the sender and the receiver
	 */
	List<AbstractNode> nodes;
	
	public NodeRouteStep(Operator sender, Operator receiver) {
		super(sender, receiver);
		// TODO Auto-generated constructor stub
	}
	
	public void addNode(AbstractNode node){
		nodes.add(node);
	}

}
