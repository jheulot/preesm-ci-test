/*
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2014 - 2015)
 * Jonathan Piat [jpiat@laas.fr] (2008 - 2011)
 * Karol Desnos [karol.desnos@insa-rennes.fr] (2016)
 * Matthieu Wipliez [matthieu.wipliez@insa-rennes.fr] (2008)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2008 - 2012)
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
package org.preesm.algorithm.io.xml;

import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.preesm.algorithm.model.sdf.SDFGraph;
import org.preesm.commons.doc.annotations.DocumentedError;
import org.preesm.commons.doc.annotations.Parameter;
import org.preesm.commons.doc.annotations.Port;
import org.preesm.commons.doc.annotations.PreesmTask;
import org.preesm.commons.doc.annotations.Value;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.files.WorkspaceUtils;
import org.preesm.workflow.elements.Workflow;
import org.preesm.workflow.implement.AbstractTaskImplementation;

/**
 * The Class SDFExporter.
 */
@PreesmTask(id = "org.ietr.preesm.plugin.exportXml.sdf4jgml", name = "SDF Exporter", category = "Graph Exporters",

    inputs = { @Port(name = "SDF", type = SDFGraph.class) },

    shortDescription = "Create a new __*.graphml__ file containing the exported SDF graph.",

    description = "The purpose of this task is to create a new __*.graphml__ file containing where the exported "
        + "IBSDF graph will be written. The exported graph can then be visualized and exported using the former "
        + "Preesm graph editor for IBSDF graph, which was replaced with the PiSDF graph editor since version "
        + "2.0.0. This task is generally used to export intermediary graphs generated at different step of a "
        + "workflow execution. For example, to visualize the SDF graph resulting from the flattening of an IBSDF "
        + "graph, or to understand the parallelism that was exposed by the single-rate transformation.",

    parameters = { @Parameter(name = "path",
        description = "Path of the directory within which the exported *.graphml file will be created. If the "
            + "specified directory does not exist, it will be created.",
        values = {
            @Value(name = "path/in/proj",
                effect = "Path within the Preesm project containing the workflow where the ”SDF Exporter” task is "
                    + "instantiated. Even if the workflow of a Preesm project A is executed with a scenario from "
                    + "a different project B, the __*.graphml__ file will be generated within the specified directory"
                    + " of project A.\n" + "\n"
                    + "Exported SDF graphs will be named automatically, usually using the same name as the original "
                    + "SDF graph processed by the workflow. If a graph with this name already exists in the given "
                    + "path, it will be overwritten.\n" + "\n" + "Example: **Algo/generated/singlerate**"),
            @Value(name = "path/in/proj/name.graphml",
                effect = "Path within the Preesm project containing the workflow where the ”SDF Exporter” task "
                    + "is instantiated. Even if the workflow of a Preesm project A is executed with a scenario "
                    + "from a different project B, the __*.graphml__ file will be generated within the specified "
                    + "directory of project A.\n" + "\n"
                    + "Exported SDF graph will be named using the string with the graphml extension at the end of "
                    + "the given path. If a graph with this name already exists in the given path, it will be"
                    + " overwritten.\n" + "\n" + "Example: **Algo/generated/singlerate/myexport.graphml**") }) },

    documentedErrors = { @DocumentedError(message = "Path <given path> is not a valid path for export. <reason>",
        explanation = "The value set for parameter path is not a valid path in the project.") })
@Deprecated
public class SDFExporter extends AbstractTaskImplementation {

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#execute(java.util.Map, java.util.Map,
   * org.eclipse.core.runtime.IProgressMonitor, java.lang.String, org.ietr.dftools.workflow.elements.Workflow)
   */
  @Override
  public Map<String, Object> execute(final Map<String, Object> inputs, final Map<String, String> parameters,
      final IProgressMonitor monitor, final String nodeName, final Workflow workflow) {

    final SDFGraph algorithm = (SDFGraph) inputs.get("SDF");
    final String sXmlPath = WorkspaceUtils.getAbsolutePath(parameters.get("path"), workflow.getProjectName());
    IPath xmlPath = new Path(sXmlPath);
    // Get a complete valid path with all folders existing
    try {
      if (xmlPath.getFileExtension() != null) {
        WorkspaceUtils.createMissingFolders(xmlPath.removeFileExtension().removeLastSegments(1));
      } else {
        WorkspaceUtils.createMissingFolders(xmlPath);
        xmlPath = xmlPath.append(algorithm.getName() + ".graphml");
      }
    } catch (CoreException | IllegalArgumentException e) {
      throw new PreesmRuntimeException("Path " + sXmlPath + " is not a valid path for export.\n" + e.getMessage());
    }

    final SDF2GraphmlExporter exporter = new SDF2GraphmlExporter();
    exporter.export(algorithm, xmlPath);

    WorkspaceUtils.updateWorkspace();

    return new LinkedHashMap<>();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#getDefaultParameters()
   */
  @Override
  public Map<String, String> getDefaultParameters() {
    final Map<String, String> parameters = new LinkedHashMap<>();

    parameters.put("path", "");
    return parameters;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractWorkflowNodeImplementation#monitorMessage()
   */
  @Override
  public String monitorMessage() {
    return "Exporting algorithm graph";
  }

}
