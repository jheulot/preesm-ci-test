/*
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2014 - 2015)
 * Florian Arrestier [florian.arrestier@insa-rennes.fr] (2018)
 * Julien Hascoet [jhascoet@kalray.eu] (2016)
 * Jonathan Piat [jpiat@laas.fr] (2009 - 2011)
 * Karol Desnos [karol.desnos@insa-rennes.fr] (2012 - 2016)
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
package org.preesm.algorithm.transforms;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.core.runtime.IProgressMonitor;
import org.preesm.algorithm.model.sdf.SDFGraph;
import org.preesm.algorithm.model.sdf.visitors.ToHSDFVisitor;
import org.preesm.commons.doc.annotations.Port;
import org.preesm.commons.doc.annotations.PreesmTask;
import org.preesm.commons.exceptions.PreesmException;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.workflow.elements.Workflow;
import org.preesm.workflow.implement.AbstractTaskImplementation;

/**
 * Class used to transform a SDF graph into a HSDF graph. Actually into a single rate graph (close enough :->)
 *
 * @author jpiat
 * @author mpelcat
 *
 */
@PreesmTask(id = "org.ietr.preesm.plugin.transforms.sdf2hsdf", name = "SDF2HSDF",

    description = "transform a SDF graph into a HSDF graph, that is into a single rate graph",

    inputs = { @Port(name = "SDF", type = SDFGraph.class) },

    outputs = { @Port(name = "SDF", type = SDFGraph.class) })
public class HSDFTransformation extends AbstractTaskImplementation {

  @Override
  public Map<String, Object> execute(final Map<String, Object> inputs, final Map<String, String> parameters,
      final IProgressMonitor monitor, final String nodeName, final Workflow workflow) {

    final Map<String, Object> outputs = new LinkedHashMap<>();
    final SDFGraph algorithm = (SDFGraph) inputs.get("SDF");

    final Logger logger = PreesmLogger.getLogger();
    final StopWatch timer = new StopWatch();
    timer.start();

    try {

      logger.log(Level.INFO, "Transforming application " + algorithm.getName() + " to HSDF");
      algorithm.insertBroadcasts();
      if (algorithm.validateModel()) {

        final ToHSDFVisitor toHsdf = new ToHSDFVisitor();

        final SDFGraph hsdf;
        try {
          algorithm.accept(toHsdf);
          hsdf = toHsdf.getOutput();
          logger.log(Level.INFO, "Minimize special actors");
          JoinForkCleaner.cleanJoinForkPairsFrom(hsdf);
        } catch (final PreesmException e) {
          throw new PreesmRuntimeException(e.getMessage(), e);
        }
        logger.log(Level.INFO, "HSDF transformation complete");

        logger.log(Level.INFO,
            () -> "HSDF with " + hsdf.vertexSet().size() + " vertices and " + hsdf.edgeSet().size() + " edges.");

        outputs.put("SDF", hsdf);
      } else {
        throw new PreesmRuntimeException("Graph not valid, not schedulable");
      }
    } catch (final PreesmException e) {
      throw new PreesmRuntimeException(e.getMessage(), e);
    }

    timer.stop();
    PreesmLogger.getLogger().log(Level.INFO, () -> "HSDF transformation: " + timer.toString() + "s.");

    return outputs;
  }

  @Override
  public Map<String, String> getDefaultParameters() {
    return Collections.emptyMap();
  }

  @Override
  public String monitorMessage() {
    return "HSDF Transformation.";
  }

}
