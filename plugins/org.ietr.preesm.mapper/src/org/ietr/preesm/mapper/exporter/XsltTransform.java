/*******************************************************************************
 * Copyright or © or Copr. %%LOWERDATE%% - %%UPPERDATE%% IETR/INSA:
 *
 * %%AUTHORS%%
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

import javax.xml.transform.TransformerConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.elements.Workflow;
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.utils.paths.PathTools;
import org.ietr.preesm.utils.xml.XsltTransformer;

/**
 * This class provides methods to transform an XML file or a DOM element to a
 * string in a workflow
 * 
 * @author Matthieu Wipliez
 * @author mpelcat
 * 
 */
public class XsltTransform extends AbstractTaskImplementation {

	@Override
	public Map<String, Object> execute(Map<String, Object> inputs,
			Map<String, String> parameters, IProgressMonitor monitor,
			String nodeName, Workflow workflow) throws WorkflowException {
		
		String sInputPath = PathTools.getAbsolutePath(parameters.get("inputFile"),
				workflow.getProjectName());		
		if(parameters.get("inputFile").equals("")){
			sInputPath =  PathTools.getAbsolutePath((String)inputs.get("xml"),
					workflow.getProjectName());
		}		
		Path inputPath = new Path(sInputPath);
		
		String sOutputPath = PathTools.getAbsolutePath(parameters.get("outputFile"),
				workflow.getProjectName());		
		Path outputPath = new Path(sOutputPath);
		
		String sxslPath = PathTools.getAbsolutePath(parameters.get("xslFile"),
				workflow.getProjectName());		
		Path xslPath = new Path(sxslPath);
		
		if (!inputPath.isEmpty() && !outputPath.isEmpty() && !xslPath.isEmpty()) {
			try {
				XsltTransformer xsltTransfo = new XsltTransformer();
				if (xsltTransfo.setXSLFile(xslPath.toOSString())) {
					WorkflowLogger.getLogger().log(Level.INFO,
							"Generating file: " + outputPath.toOSString());
					xsltTransfo.transformFileToFile(inputPath.toOSString(),
							outputPath.toOSString());
				}

				// xsltTransfo.
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			}
		}
		HashMap<String, Object> outputs = new HashMap<String, Object>();
		outputs.put("xml", parameters.get("outputFile"));

		return outputs;
	}

	@Override
	public Map<String, String> getDefaultParameters() {
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("inputFile", "");
		parameters.put("outputFile", "");
		parameters.put("xslFile", "");
		return parameters;
	}

	@Override
	public String monitorMessage() {
		return "XSL Transformation.";
	}
}
