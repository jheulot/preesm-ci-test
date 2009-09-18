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

package org.ietr.preesm.plugin.architransfo.transforms;

import java.util.logging.Level;

import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.writer.DesignWriter;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.core.task.IExporter;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.sdf4j.model.AbstractGraph;
import org.sdf4j.model.dag.DirectedAcyclicGraph;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Exporter for IP-XACT multicore architectures
 * 
 * @author mpelcat
 * 
 */
public class ArchitectureExporter implements IExporter {

	@Override
	public boolean isDAGExporter() {
		return false;
	}

	@Override
	public boolean isSDFExporter() {
		return false;
	}

	@Override
	public boolean isArchiExporter() {
		return true;
	}

	@Override
	public void transform(MultiCoreArchitecture archi, TextParameters params) {
		
		String pathKey = "path";
		if(params.hasVariable(pathKey)){
		DesignWriter writer = new DesignWriter(archi);
		writer.generateArchitectureDOM();
		writer.writeDom(params.getVariable(pathKey));
		}
		else{
			PreesmLogger.getLogger().log(Level.SEVERE,"Architecture exporter has no file path.");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void transform(AbstractGraph algorithm, TextParameters params) {
		
	}

	@Override
	public void transform(DirectedAcyclicGraph dag, SDFGraph sdf,
			MultiCoreArchitecture archi, IScenario scenario,
			TextParameters params) {
		
	}


}