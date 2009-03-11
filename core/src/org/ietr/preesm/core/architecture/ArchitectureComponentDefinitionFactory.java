/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Peng Cheng Mu, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,pmu,jnezan,mraulet]@insa-rennes.fr

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

import org.ietr.preesm.core.architecture.advancedmodel.BusDefinition;
import org.ietr.preesm.core.architecture.advancedmodel.CommunicationNodeDefinition;
import org.ietr.preesm.core.architecture.advancedmodel.CommunicatorDefinition;
import org.ietr.preesm.core.architecture.advancedmodel.FifoDefinition;
import org.ietr.preesm.core.architecture.advancedmodel.IpCoprocessorDefinition;
import org.ietr.preesm.core.architecture.advancedmodel.MemoryDefinition;
import org.ietr.preesm.core.architecture.advancedmodel.ProcessorDefinition;
import org.ietr.preesm.core.architecture.simplemodel.ContentionNode;
import org.ietr.preesm.core.architecture.simplemodel.ContentionNodeDefinition;
import org.ietr.preesm.core.architecture.simplemodel.Dma;
import org.ietr.preesm.core.architecture.simplemodel.DmaDefinition;
import org.ietr.preesm.core.architecture.simplemodel.MediumDefinition;
import org.ietr.preesm.core.architecture.simplemodel.OperatorDefinition;
import org.ietr.preesm.core.architecture.simplemodel.ParallelNode;
import org.ietr.preesm.core.architecture.simplemodel.ParallelNodeDefinition;


/**
 * Factory able to create an architecture component of any type
 * 
 * @author mpelcat
 */
public class ArchitectureComponentDefinitionFactory {
	
	public static ArchitectureComponentDefinition createElement(ArchitectureComponentType type,String name){

		ArchitectureComponentDefinition result = null;
		
		if(type != null){
			//Simple model
			if(type == ArchitectureComponentType.medium){
				result = new MediumDefinition(name);
			}
			else if(type == ArchitectureComponentType.operator){
				result = new OperatorDefinition(name);
			}
			else if(type == ArchitectureComponentType.contentionNode){
				result = new ContentionNodeDefinition(name);
			}
			else if(type == ArchitectureComponentType.parallelNode){
				result = new ParallelNodeDefinition(name);
			}
			else if(type == ArchitectureComponentType.dma){
				result = new DmaDefinition(name);
			}
			// Advanced model
			else if(type == ArchitectureComponentType.processor){
				result = new ProcessorDefinition(name);
			}
			else if(type == ArchitectureComponentType.ipCoprocessor){
				result = new IpCoprocessorDefinition(name);
			}
			else if(type == ArchitectureComponentType.communicationNode){
				result = new CommunicationNodeDefinition(name);
			}
			else if(type == ArchitectureComponentType.communicator){
				result = new CommunicatorDefinition(name);
			}
			else if(type == ArchitectureComponentType.memory){
				result = new MemoryDefinition(name);
			}
			else if(type == ArchitectureComponentType.bus){
				result = new BusDefinition(name);
			}
			else if(type == ArchitectureComponentType.fifo){
				result = new FifoDefinition(name);
			}
		}
		
		return result;
	}
}
