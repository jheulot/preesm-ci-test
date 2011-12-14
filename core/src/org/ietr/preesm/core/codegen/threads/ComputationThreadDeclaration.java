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

package org.ietr.preesm.core.codegen.threads;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import net.sf.dftools.algorithm.model.sdf.SDFAbstractVertex;
import net.sf.dftools.algorithm.model.sdf.SDFEdge;

import org.ietr.preesm.core.codegen.ImplementationPropertyNames;
import org.ietr.preesm.core.codegen.SchedulingOrderComparator;
import org.ietr.preesm.core.codegen.buffer.AbstractBufferContainer;
import org.ietr.preesm.core.codegen.buffer.allocators.VirtualHeapAllocator;
import org.ietr.preesm.core.codegen.model.VertexType;

/**
 * Declaration of a communication thread for code generation. A computation
 * thread calls the functions corresponding to the dag tasks.
 * 
 * @author mpelcat
 */
public class ComputationThreadDeclaration extends ThreadDeclaration {

	VirtualHeapAllocator heap;

	public ComputationThreadDeclaration(AbstractBufferContainer parentContainer) {
		super("computationThread", parentContainer);
	}

	public void setVirtualHeap(VirtualHeapAllocator heap) {
		this.heap = heap;
	}

	/**
	 * Gets the communication vertices preceding or following the vertex vertex.
	 * If preceding = true, returns the communication vertices preceding the
	 * vertex, otherwise, returns the communication vertices following the
	 * vertex. The communication vertices are returned in scheduling order
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<SDFAbstractVertex> getComVertices(
			SDFAbstractVertex vertex, boolean preceding) {
		SDFAbstractVertex currentVertex = null;

		ConcurrentSkipListSet<SDFAbstractVertex> schedule = new ConcurrentSkipListSet<SDFAbstractVertex>(
				new SchedulingOrderComparator());

		Iterator<SDFEdge> iterator = null;

		if (preceding) {
			iterator = vertex.getBase().incomingEdgesOf(vertex).iterator();
		} else {
			iterator = vertex.getBase().outgoingEdgesOf(vertex).iterator();
		}

		while (iterator.hasNext()) {
			SDFEdge edge = iterator.next();

			if (preceding) {
				currentVertex = edge.getSource();
			} else {
				currentVertex = edge.getTarget();
			}

			// retrieving the type of the vertex
			VertexType vertexType = (VertexType) currentVertex
					.getPropertyBean().getValue(
							ImplementationPropertyNames.Vertex_vertexType);

			if (vertexType != null
					&& (vertexType.equals(VertexType.send) || vertexType
							.equals(VertexType.receive))
					&& !schedule.contains(currentVertex)) {
				schedule.add(currentVertex);
			}
		}

		return schedule;
	}
}
