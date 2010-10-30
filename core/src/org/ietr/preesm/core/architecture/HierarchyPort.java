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

package org.ietr.preesm.core.architecture;


/**
 * A hierarchical connection joins one interface of a component to one port of
 * its design component
 * 
 * @author mpelcat
 */
public class HierarchyPort {

	/**
	 * A HierarchyPort has only one connection to a given component
	 */
	private String connectedCmpId = null;

	/**
	 * A the connection has a given bus reference
	 */
	private String busRefName = null;

	/**
	 * Hierarchical connection name
	 */
	private String name = null;

	public HierarchyPort(String name, String connectedComponent,
			String busReference) {
		super();
		this.name = name;
		this.connectedCmpId = connectedComponent;
		this.busRefName = busReference;
	}

	@Override
	public HierarchyPort clone() {
		HierarchyPort newHC = new HierarchyPort(this.getName(),
				getConnectedCmpId(), getBusRefName());
		return newHC;
	}

	public String getConnectedCmpId() {
		return connectedCmpId;
	}

	public void setConnectedCmpId(String id) {
		connectedCmpId = id;
	}

	public String getBusRefName() {
		return busRefName;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
