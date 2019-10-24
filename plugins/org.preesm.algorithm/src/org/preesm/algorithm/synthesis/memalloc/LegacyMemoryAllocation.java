/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2019)
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
package org.preesm.algorithm.synthesis.memalloc;

import bsh.EvalError;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.preesm.algorithm.mapping.model.Mapping;
import org.preesm.algorithm.memalloc.model.Allocation;
import org.preesm.algorithm.memory.allocation.MemoryAllocatorTask;
import org.preesm.algorithm.memory.allocation.tasks.MemoryScriptTask;
import org.preesm.algorithm.schedule.model.Schedule;
import org.preesm.algorithm.synthesis.memalloc.meg.MemExUpdaterEngine;
import org.preesm.algorithm.synthesis.memalloc.meg.PiMemoryExclusionGraph;
import org.preesm.algorithm.synthesis.memalloc.script.PiMemoryScriptEngine;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.pisdf.PiGraph;
import org.preesm.model.scenario.Scenario;
import org.preesm.model.slam.Design;

/**
 *
 *
 * @author anmorvan
 */
public class LegacyMemoryAllocation implements IMemoryAllocation {

  @Override
  public Allocation allocateMemory(final PiGraph piGraph, final Design slamDesign, final Scenario scenario,
      final Schedule schedule, final Mapping mapping) {

    // *************
    // INITIAL MEG BUILD
    // *************
    final PiMemoryExclusionGraph memEx = new PiMemoryExclusionGraph(scenario);
    PreesmLogger.getLogger().log(Level.INFO, () -> "building memex graph");
    memEx.buildGraph(piGraph);
    final int edgeCount = memEx.edgeSet().size();
    final int vertexCount = memEx.vertexSet().size();
    final double density = edgeCount / ((vertexCount * (vertexCount - 1)) / 2.0);
    PreesmLogger.getLogger().log(Level.INFO, () -> "Memory exclusion graph built with " + vertexCount
        + " vertices and density = " + density + " (" + edgeCount + " edges)");

    // *************
    // MEG UPDATE
    // *************
    final MemExUpdaterEngine memExUpdaterEngine = new MemExUpdaterEngine(piGraph, memEx, schedule, mapping, true);
    memExUpdaterEngine.update();

    // *************
    // SCRIPTS
    // *************
    final Map<String, String> param = new LinkedHashMap<>();
    param.put(MemoryScriptTask.PARAM_VERBOSE,
        "? C {" + MemoryScriptTask.VALUE_TRUE + ", " + MemoryScriptTask.VALUE_FALSE + "}");
    param.put(MemoryScriptTask.PARAM_CHECK, "? C {" + MemoryScriptTask.VALUE_CHECK_NONE + ", "
        + MemoryScriptTask.VALUE_CHECK_FAST + ", " + MemoryScriptTask.VALUE_CHECK_THOROUGH + "}");
    param.put(MemoryAllocatorTask.PARAM_ALIGNMENT, MemoryAllocatorTask.VALUE_ALIGNEMENT_DEFAULT);
    param.put(MemoryScriptTask.PARAM_LOG, MemoryScriptTask.VALUE_LOG);

    final String log = param.get(MemoryScriptTask.PARAM_LOG);
    final String checkString = param.get(MemoryScriptTask.PARAM_CHECK);
    final String valueAlignment = param.get(MemoryAllocatorTask.PARAM_ALIGNMENT);

    final PiMemoryScriptEngine engine = new PiMemoryScriptEngine(valueAlignment, log, true);
    try {
      engine.runScripts(piGraph, scenario.getSimulationInfo().getDataTypes(), checkString);
    } catch (final EvalError e) {
      final String message = "An error occurred during memory scripts execution";
      throw new PreesmRuntimeException(message, e);
    }
    engine.updateMemEx(memEx);

    // TODO fix
    return new SimpleMemoryAllocation().allocateMemory(piGraph, slamDesign, scenario, schedule, mapping);
  }
}
