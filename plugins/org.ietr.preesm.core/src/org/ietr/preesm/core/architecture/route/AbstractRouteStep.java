/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.core.architecture.route;

import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a single step in a route between two operators
 * 
 * @author mpelcat
 */
public abstract class AbstractRouteStep {

	/**
	 * The sender of the route step. A route step is always directed.
	 */
	private ComponentInstance sender;

	/**
	 * The receiver of the route step
	 */
	private ComponentInstance receiver;

	public AbstractRouteStep(ComponentInstance sender,
			ComponentInstance receiver) {
		super();
		this.sender = sender;
		this.receiver = receiver;
	}

	public ComponentInstance getReceiver() {
		return receiver;
	}

	public ComponentInstance getSender() {
		return sender;
	}

	public void setReceiver(ComponentInstance receiver) {
		this.receiver = receiver;
	}

	public void setSender(ComponentInstance sender) {
		this.sender = sender;
	}

	/**
	 * The route step type determines how the communication will be simulated.
	 */
	public abstract String getType();

	/**
	 * The id is given to code generation. It selects the communication
	 * functions to use
	 */
	public abstract String getId();

	/**
	 * The name of the step node is retrieved
	 */
	public abstract String getName();

	/**
	 * Evaluates the cost of a data transfer with size transferSize. This cost
	 * can include overheads, involvements...
	 */
	public abstract long getTransferCost(long transfersSize);

	/**
	 * Returns the longest time a contention node needs to transfer the data
	 */
	public abstract long getWorstTransferTime(long transfersSize);

	@Override
	protected abstract Object clone() throws CloneNotSupportedException;

	/**
	 * Appends the route step informations to a dom3 xml file
	 */
	public void appendRouteStep(Document dom, Element comFct) {

		Element routeStep = dom.createElement("routeStep");
		comFct.appendChild(routeStep);

		Element sender = dom.createElement("sender");
		sender.setAttribute("name", this.getSender().getInstanceName());
		sender.setAttribute("def", this.getSender().getComponent().getVlnv()
				.getName());
		routeStep.appendChild(sender);

		Element receiver = dom.createElement("receiver");
		receiver.setAttribute("name", this.getReceiver().getInstanceName());
		receiver.setAttribute("def", this.getReceiver().getComponent()
				.getVlnv().getName());
		routeStep.appendChild(receiver);

		if (this.getType() == DmaRouteStep.type) {
			routeStep.setAttribute("type", "dma");
			DmaRouteStep dStep = (DmaRouteStep) this;
			routeStep
					.setAttribute("dmaDef", dStep.getDma().getVlnv().getName());

			for (ComponentInstance node : dStep.getNodes()) {
				Element eNode = dom.createElement("node");
				eNode.setAttribute("name", node.getInstanceName());
				eNode.setAttribute("def", node.getComponent().getVlnv()
						.getName());
				routeStep.appendChild(eNode);
			}
		} else if (this.getType() == MessageRouteStep.type) {
			routeStep.setAttribute("type", "msg");
			MessageRouteStep nStep = (MessageRouteStep) this;

			for (ComponentInstance node : nStep.getNodes()) {
				Element eNode = dom.createElement("node");
				eNode.setAttribute("name", node.getInstanceName());
				eNode.setAttribute("def", node.getComponent().getVlnv()
						.getName());
				routeStep.appendChild(eNode);
			}
		} else if (this.getType() == MemRouteStep.type) {
			routeStep.setAttribute("type", "ram");
			MemRouteStep rStep = (MemRouteStep) this;
			routeStep
					.setAttribute("ramDef", rStep.getMem().getVlnv().getName());
		}
	}
}
