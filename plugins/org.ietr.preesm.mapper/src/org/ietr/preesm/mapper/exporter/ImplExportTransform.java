/*******************************************************************************
 * Copyright or © or Copr. 2012 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2012)
 * Maxime Pelcat <Maxime.Pelcat@insa-rennes.fr> (2012)
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
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
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package org.ietr.preesm.mapper.exporter;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.ietr.dftools.algorithm.model.dag.DirectedAcyclicGraph;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.elements.Workflow;
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.core.Activator;
import org.ietr.preesm.mapper.model.MapperDAG;
import org.ietr.preesm.utils.paths.PathTools;

/**
 * Block in workflow exporting a DAG that contains all information of an
 * implementation
 * 
 * @author mpelcat
 * 
 */
public class ImplExportTransform extends AbstractTaskImplementation {

	@Override
	public Map<String, Object> execute(Map<String, Object> inputs,
			Map<String, String> parameters, IProgressMonitor monitor,
			String nodeName, Workflow workflow) throws WorkflowException {

		DirectedAcyclicGraph dag = (DirectedAcyclicGraph) inputs.get("DAG");

		String sGraphmlPath = PathTools.getAbsolutePath(parameters.get("path"),
				workflow.getProjectName());
		Path graphmlPath = new Path(sGraphmlPath);

		// Exporting the DAG in a GraphML
		if (!graphmlPath.isEmpty()) {
			exportGraphML(dag, graphmlPath);
		}

		Activator.updateWorkspace();

		HashMap<String, Object> outputs = new HashMap<String, Object>();
		outputs.put("xml", sGraphmlPath);
		return outputs ;
	}

	@Override
	public Map<String, String> getDefaultParameters() {
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("path", "");
		return parameters;
	}

	@Override
	public String monitorMessage() {
		return "Exporting implementation.";
	}

	private void exportGraphML(DirectedAcyclicGraph dag, Path path) {

		MapperDAG mapperDag = (MapperDAG) dag;

		ImplementationExporter exporter = new ImplementationExporter();
		MapperDAG clone = mapperDag.clone();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile iGraphMLFile = workspace.getRoot().getFile(path);

		if (iGraphMLFile.getLocation() != null) {
			exporter.export(clone, iGraphMLFile.getLocation().toOSString());
		} else {
			WorkflowLogger.getLogger().log(Level.SEVERE,
					"The output file " + path + " can not be written.");
		}
	}

}
