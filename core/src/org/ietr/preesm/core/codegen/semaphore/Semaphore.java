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

package org.ietr.preesm.core.codegen.semaphore;

import java.util.List;

import org.ietr.preesm.core.codegen.buffer.Buffer;
import org.ietr.preesm.core.codegen.printer.CodeZoneId;
import org.ietr.preesm.core.codegen.printer.IAbstractPrinter;
import org.ietr.preesm.core.codegen.types.CodeSectionType;

/**
 * Class representing a semaphore in the code. A semaphore protects a buffer
 * from being read while empty or written while full.
 * 
 * @author mpelcat
 */
public class Semaphore {

	/**
	 * Semaphore container.
	 */
	private SemaphoreContainer container;

	/**
	 * The buffers protected by the current semaphore.
	 */
	private List<Buffer> protectedBuffers;

	/**
	 * A semaphore can be the signal of a full buffer aggregate or the signal of
	 * an empty buffer aggregate.
	 */
	private SemaphoreType semaphoreType;

	/**
	 * A semaphore is either created for beginning, loop or end code protection
	 */
	private CodeSectionType codeContainerType;

	public Semaphore(SemaphoreContainer container,
			List<Buffer> protectedBuffers, SemaphoreType semaphoreType, CodeSectionType codeContainerType) {

		this.semaphoreType = semaphoreType;

		this.protectedBuffers = protectedBuffers;

		this.container = container;
		this.codeContainerType = codeContainerType;
	}

	public void accept(IAbstractPrinter printer, Object currentLocation) {
		currentLocation = printer.visit(this, CodeZoneId.body, currentLocation); // Visit self
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Semaphore)
			for(Buffer buff : protectedBuffers){
				if(!((Semaphore) obj).protectedBuffers.contains(buff)){
					return false ;
				}
			}
				if (((Semaphore) obj).semaphoreType == semaphoreType)
					return true;
		return false;
	}

	public List<Buffer> getProtectedBuffers() {
		return protectedBuffers;
	}

	/**
	 * A semaphore is determined by its number.
	 */
	public int getSemaphoreNumber() {
		return container.indexOf(this);
	}

	public SemaphoreType getSemaphoreType() {
		return semaphoreType;
	}

	public CodeSectionType getCodeContainerType() {
		return codeContainerType;
	}

	/**
	 * Displays pseudo-code for test
	 */
	public String toString() {

		String code = "";

		code += "sem[" + getSemaphoreNumber() + ","
				+ semaphoreType.toString() + "]";

		return code;
	}
}
