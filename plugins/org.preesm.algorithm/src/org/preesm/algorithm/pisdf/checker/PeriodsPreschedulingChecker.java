/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2018 - 2019) :
 *
 * Alexandre Honorat <ahonorat@insa-rennes.fr> (2018)
 * Alexandre Honorat <alexandre.honorat@insa-rennes.fr> (2018 - 2019)
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
package org.preesm.algorithm.pisdf.checker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.preesm.commons.doc.annotations.Parameter;
import org.preesm.commons.doc.annotations.Port;
import org.preesm.commons.doc.annotations.PreesmTask;
import org.preesm.commons.doc.annotations.Value;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.pisdf.AbstractActor;
import org.preesm.model.pisdf.Actor;
import org.preesm.model.pisdf.ExecutableActor;
import org.preesm.model.pisdf.PeriodicElement;
import org.preesm.model.pisdf.PiGraph;
import org.preesm.model.scenario.PreesmScenario;
import org.preesm.model.slam.Design;
import org.preesm.workflow.elements.Workflow;
import org.preesm.workflow.implement.AbstractTaskImplementation;
import org.preesm.workflow.implement.AbstractWorkflowNodeImplementation;

/**
 * This class computes necessary conditions for the schedulability of graphs with periods.
 *
 * @author ahonorat
 *
 */
@PreesmTask(id = "org.ietr.preesm.pimm.algorithm.checker.periods.PeriodsPreschedulingChecker",
    name = "Periods Prescheduling Checker",

    inputs = { @Port(name = "PiMM", type = PiGraph.class), @Port(name = "scenario", type = PreesmScenario.class),
        @Port(name = "architecture", type = Design.class) },

    outputs = { @Port(name = "PiMM", type = PiGraph.class) },

    parameters = { @Parameter(name = "Selection rate (%)", values = { @Value(name = "100", effect = "") }) }

)

public class PeriodsPreschedulingChecker extends AbstractTaskImplementation {

  /**
   * Identify the parameter to get the rate of periodic actors to analyze.
   */
  public static final String SELECTION_RATE = "Selection rate (%)";

  /**
   * By default all actors are analyzed.
   */
  public static final String DEFAULT_SELECTION_RATE = "100";

  private static final String GENERIC_RATE_ERROR = "Periodic actors selection rate "
      + "must be an integer between 1 and 100 (%), instead of: ";

  @Override
  public Map<String, Object> execute(final Map<String, Object> inputs, final Map<String, String> parameters,
      final IProgressMonitor monitor, final String nodeName, final Workflow workflow) {

    inputs.get(AbstractWorkflowNodeImplementation.KEY_ARCHITECTURE);
    final PreesmScenario scenario = (PreesmScenario) inputs.get(AbstractWorkflowNodeImplementation.KEY_SCENARIO);
    final PiGraph graph = (PiGraph) inputs.get(AbstractWorkflowNodeImplementation.KEY_PI_GRAPH);

    if (!graph.getChildrenGraphs().isEmpty()) {
      throw new PreesmRuntimeException("This task must be called with a flatten PiMM graph, abandon.");
    }

    final String rateStr = parameters.get(PeriodsPreschedulingChecker.SELECTION_RATE);
    int rate = 100;
    try {
      rate = Integer.parseInt(rateStr);
      if ((rate < 0) || (rate > 100)) {
        throw new PreesmRuntimeException(PeriodsPreschedulingChecker.GENERIC_RATE_ERROR + rate + ".");
      }
    } catch (final NumberFormatException e) {
      throw new PreesmRuntimeException(PeriodsPreschedulingChecker.GENERIC_RATE_ERROR + rateStr + ".", e);
    }

    final Map<Actor, Long> periodicActors = new HashMap<>();
    for (final AbstractActor absActor : graph.getActors()) {
      if ((absActor instanceof Actor) && (absActor instanceof PeriodicElement)) {
        final Actor actor = (Actor) absActor;
        if (!actor.isHierarchical() && !actor.isConfigurationActor()) {
          final long period = actor.getPeriod().evaluate();
          if (period > 0) {
            periodicActors.put(actor, period);
          }
        }
      }
    }

    // 0. find all cycles and retrieve actors placed after delays.
    HeuristicLoopBreakingDelays heurFifoBreaks = new HeuristicLoopBreakingDelays();
    heurFifoBreaks.performAnalysis(graph);

    // 1. find all actor w/o incoming edges and all others w/o outgoing edge
    final Set<AbstractActor> sourceActors = new HashSet<>(heurFifoBreaks.additionalSourceActors);
    final Set<AbstractActor> sinkActors = new HashSet<>(heurFifoBreaks.additionalSinkActors);
    for (final AbstractActor absActor : graph.getActors()) {
      if (absActor instanceof ExecutableActor) {
        if (absActor.getDataOutputPorts().isEmpty()) {
          sinkActors.add(absActor);
        }
        if (absActor.getDataInputPorts().isEmpty()) {
          sourceActors.add(absActor);
        }
      }
    }

    StringBuilder sources = new StringBuilder();
    sourceActors.stream().forEach(a -> sources.append(a.getName() + " / "));
    PreesmLogger.getLogger().log(Level.FINE, "Sources: " + sources.toString());
    StringBuilder sinks = new StringBuilder();
    sinkActors.stream().forEach(a -> sinks.append(a.getName() + " / "));
    PreesmLogger.getLogger().log(Level.FINE, "Sinks: " + sinks.toString());

    // 2. perform heuristic to select periodic nodes
    final StringBuilder sbNBFF = new StringBuilder();
    final Map<Actor, Long> actorsNBFF = HeuristicPeriodicActorSelection.selectActors(periodicActors, sourceActors,
        heurFifoBreaks.actorsNbVisitsTopoRank, rate, graph, scenario, false);
    actorsNBFF.keySet().forEach(a -> sbNBFF.append(a.getName() + " / "));
    PreesmLogger.getLogger().log(Level.INFO, "Periodic actor for NBFF: " + sbNBFF.toString());

    final StringBuilder sbNBLF = new StringBuilder();
    final Map<Actor, Long> actorsNBLF = HeuristicPeriodicActorSelection.selectActors(periodicActors, sinkActors,
        heurFifoBreaks.actorsNbVisitsTopoRankT, rate, graph, scenario, true);
    actorsNBLF.keySet().forEach(a -> sbNBLF.append(a.getName() + " / "));
    PreesmLogger.getLogger().log(Level.INFO, "Periodic actor for NBLF: " + sbNBLF.toString());

    // 3. for each selected periodic node for nblf:
    // _a compute subgraph
    // _b compute nblf
    // 4. for each selected periodic node for nbff:
    // _a compute subgraph
    // _b compute nbff

    final Map<String, Object> output = new LinkedHashMap<>();
    output.put(AbstractWorkflowNodeImplementation.KEY_PI_GRAPH, graph);
    return output;
  }

  @Override
  public Map<String, String> getDefaultParameters() {
    final Map<String, String> parameters = new HashMap<>();
    parameters.put(PeriodsPreschedulingChecker.SELECTION_RATE, PeriodsPreschedulingChecker.DEFAULT_SELECTION_RATE);
    return parameters;
  }

  @Override
  public String monitorMessage() {
    return "Checking if periods constraints prevent to have a schedulable application.";
  }

}
