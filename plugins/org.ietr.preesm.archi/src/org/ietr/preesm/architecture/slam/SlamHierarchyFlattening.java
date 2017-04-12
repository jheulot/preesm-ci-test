/*******************************************************************************
 * Copyright or © or Copr. 2012 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
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

package org.ietr.preesm.architecture.slam;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.ietr.dftools.architecture.slam.Design;
import org.ietr.dftools.architecture.slam.process.SlamFlattener;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.elements.Workflow;
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation;
import org.ietr.dftools.workflow.tools.WorkflowLogger;

// TODO: Auto-generated Javadoc
/**
 * Flattening the hierarchy of a given S-LAM architecture.
 *
 * @author mpelcat
 */
public class SlamHierarchyFlattening extends AbstractTaskImplementation {

  /*
   * (non-Javadoc)
   * 
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#execute(java.util.Map, java.util.Map, org.eclipse.core.runtime.IProgressMonitor,
   * java.lang.String, org.ietr.dftools.workflow.elements.Workflow)
   */
  @Override
  public Map<String, Object> execute(final Map<String, Object> inputs, final Map<String, String> parameters, final IProgressMonitor monitor,
      final String nodeName, final Workflow workflow) throws WorkflowException {
    final Map<String, Object> outputs = new HashMap<>();
    final Design design = (Design) inputs.get("architecture");
    final String depthS = parameters.get("depth");

    int depth;
    if (depthS != null) {
      depth = Integer.decode(depthS);
    } else {
      depth = 1;
    }

    final Logger logger = WorkflowLogger.getLogger();
    logger.log(Level.INFO, "flattening " + depth + " level(s) of hierarchy");

    // Copier copier = new Copier();
    // EObject result = copier.copy(design);
    // copier.copyReferences();

    final SlamFlattener flattener = new SlamFlattener();
    flattener.flatten(design, depth);

    final Design resultGraph = design;
    logger.log(Level.INFO, "flattening complete");

    outputs.put("architecture", resultGraph);

    return outputs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#getDefaultParameters()
   */
  @Override
  public Map<String, String> getDefaultParameters() {
    final Map<String, String> parameters = new HashMap<>();

    parameters.put("depth", "1");
    return parameters;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ietr.dftools.workflow.implement.AbstractWorkflowNodeImplementation#monitorMessage()
   */
  @Override
  public String monitorMessage() {
    return "Flattening an S-LAM model hierarchy.";
  }
}
