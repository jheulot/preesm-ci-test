/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2019) :
 *
 * Alexandre Honorat [alexandre.honorat@insa-rennes.fr] (2019)
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
package org.preesm.algorithm.synthesis.schedule;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.preesm.algorithm.synthesis.schedule.algos.ChocoScheduler;
import org.preesm.algorithm.synthesis.schedule.algos.IScheduler;
import org.preesm.algorithm.synthesis.schedule.algos.PeriodicScheduler;
import org.preesm.commons.doc.annotations.Parameter;
import org.preesm.commons.doc.annotations.Port;
import org.preesm.commons.doc.annotations.PreesmTask;
import org.preesm.commons.doc.annotations.Value;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.pisdf.PiGraph;
import org.preesm.model.scenario.Scenario;
import org.preesm.model.slam.Design;
import org.preesm.workflow.elements.Workflow;
import org.preesm.workflow.implement.AbstractTaskImplementation;
import org.preesm.workflow.implement.AbstractWorkflowNodeImplementation;

/**
 *
 * @author ahonorat
 *
 */
@PreesmTask(id = "pisdf-synthesis.void-periodic-schedule", name = "Periodic scheduling (without output)",
    parameters = { @Parameter(name = "solver", values = { @Value(name = "list"), @Value(name = "choco") }) },
    inputs = { @Port(name = "PiMM", type = PiGraph.class), @Port(name = "architecture", type = Design.class),
        @Port(name = "scenario", type = Scenario.class) })
public class VoidPeriodicScheduleTask extends AbstractTaskImplementation {

  @Override
  public Map<String, Object> execute(Map<String, Object> inputs, Map<String, String> parameters,
      IProgressMonitor monitor, String nodeName, Workflow workflow) {

    final PiGraph algorithm = (PiGraph) inputs.get(AbstractWorkflowNodeImplementation.KEY_PI_GRAPH);
    final Design architecture = (Design) inputs.get(AbstractWorkflowNodeImplementation.KEY_ARCHITECTURE);
    final Scenario scenario = (Scenario) inputs.get(AbstractWorkflowNodeImplementation.KEY_SCENARIO);

    final String solverName = parameters.get("solver").toLowerCase();

    IScheduler scheduler = null;

    if ("list".equalsIgnoreCase(solverName)) {
      scheduler = new PeriodicScheduler();
      PreesmLogger.getLogger().log(Level.INFO, () -> " -- Periodic list scheduling without output ");
    } else if ("choco".equalsIgnoreCase(solverName)) {
      scheduler = new ChocoScheduler();
      PreesmLogger.getLogger().log(Level.INFO, () -> " -- Periodic constraint programming scheduling without output ");
    } else {
      throw new PreesmRuntimeException("Unknown solver.");
    }

    scheduler.scheduleAndMap(algorithm, architecture, scenario);
    // we do not care about result, if it fails, it will throw an exception

    return new HashMap<>();
  }

  @Override
  public Map<String, String> getDefaultParameters() {
    Map<String, String> map = new HashMap<>();
    map.put("solver", "list");
    return map;
  }

  @Override
  public String monitorMessage() {
    return "Only Periodic Scheduling (without output)";
  }

}
