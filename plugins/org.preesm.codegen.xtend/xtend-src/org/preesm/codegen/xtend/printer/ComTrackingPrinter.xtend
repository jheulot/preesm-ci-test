/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2013 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Karol Desnos [karol.desnos@insa-rennes.fr] (2013)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2013)
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
package org.preesm.codegen.xtend.printer

import org.preesm.codegen.model.CoreBlock
import org.preesm.codegen.model.Delimiter
import org.preesm.codegen.model.Direction
import org.preesm.codegen.model.SharedMemoryCommunication
import org.preesm.codegen.printer.CodegenAbstractPrinter
import org.preesm.codegen.printer.BlankPrinter

/**
 * This {@link ComTrackingPrinter} is a dummy implementation of the
 * {@link DefaultPrinter} where all print methods print nothing
 * except for communications. The goal is to track the order of communication
 * calls for debug purpose.
 * @author mpelcat
 */
class ComTrackingPrinter extends BlankPrinter {

		override printCoreBlockHeader(CoreBlock block) '''
			/**
			 * @file «block.name».txt
			 * @generated by «this.class.simpleName» to check communications
			 */

	'''

	/**
	 * Printing reports for communications.
	 * @see CodegenAbstractPrinter#preProcessing(List,List)
	 */
	override printSharedMemoryCommunication(SharedMemoryCommunication communication) '''
	«/*on each communication start, we track the id and cores*/
	»«IF communication.delimiter == Delimiter::START
	»«IF communication.direction == Direction::SEND»send«
	ELSEIF communication.direction == Direction::RECEIVE»recv«
	ENDIF»(«
	communication.sendStart.coreContainer.name»->«
	communication.receiveStart.coreContainer.name») com «
	communication.id» «communication.data.name»
	«ENDIF»
	'''


}
