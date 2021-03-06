/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2015 - 2020) :
 *
 * Alexandre Honorat [alexandre.honorat@insa-rennes.fr] (2019)
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2015)
 * Julien Heulot [julien.heulot@insa-rennes.fr] (2020)
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
package org.preesm.algorithm.mapper.stats.exporter;

import java.awt.Color;
import java.io.File;
import java.util.logging.Level;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.preesm.algorithm.mapper.gantt.GanttComponent;
import org.preesm.algorithm.mapper.gantt.GanttData;
import org.preesm.algorithm.mapper.gantt.GanttTask;
import org.preesm.algorithm.mapper.ui.stats.IStatGenerator;
import org.preesm.commons.exceptions.PreesmException;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.slam.ComponentInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class exports stats from an IAbc (architecture benchmark computer) in XML format.
 *
 * @author cguy
 */
public class XMLStatsExporter {

  /** The Constant TASKCOLOR. */
  private static final String TASKCOLOR = "#c896fa";

  /**
   * Export generated stats from an IAbc to an xml file.
   *
   * @param file
   *          the file
   */
  public static void exportXMLStats(final File file, final IStatGenerator statGen) {

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    DocumentBuilder dBuilder;
    try {
      dBuilder = dbFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new PreesmRuntimeException(e);
    }
    Document content = dBuilder.newDocument();

    // Generate the stats to write in an xml file
    generateXMLStats(content, statGen);

    // Write the file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    try {
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(content);
      StreamResult result = new StreamResult(file);
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(source, result);
    } catch (final Exception e) {
      throw new PreesmRuntimeException("Could not export stats", e);
    }

  }

  /**
   * Generate a String at an XML format from an IAbc.
   *
   * @param abc
   *          the IAbc containing the scheduling of each task
   * @return a String containing the stats at an xml format
   */
  private static void generateXMLStats(Document doc, final IStatGenerator statGen) {
    Element root = doc.createElement("data");
    doc.appendChild(root);

    // Generate scheduling stats (when and on which core a given task is
    // executed)
    generateSchedulingStats(doc, root, statGen.getGanttData());
    // Generate performance stats (loads of the core; work, span and
    // implementation length; number of cores used over total number of
    // cores)
    generatePerformanceStats(doc, root, statGen);

  }

  /**
   * Generate performance stats.
   *
   * @param abc
   *          the abc
   */
  private static void generatePerformanceStats(final Document doc, final Element root, final IStatGenerator statGen) {
    // Starting the performace stats

    Element perfs = doc.createElement("perfs");
    root.appendChild(perfs);
    // Work length
    long work = -1;
    try {
      work = statGen.getDAGWorkLength();
    } catch (final PreesmException e) {
      PreesmLogger.getLogger().log(Level.WARNING, "Could not generate work length perf stats.\n" + e.toString());
    }
    perfs.setAttribute("work", Long.toString(work));
    // Span length
    perfs.setAttribute("span", Long.toString(statGen.getDAGSpanLength()));
    // Implementation length
    perfs.setAttribute("impl_length", Long.toString(statGen.getFinalTime()));
    // Implementation number of cores
    perfs.setAttribute("impl_nbCores", Integer.toString(statGen.getNbMainTypeOperators()));
    // Implementation number of used cores
    perfs.setAttribute("impl_nbUsedCores", Integer.toString(statGen.getNbUsedOperators()));
    for (final ComponentInstance op : statGen.getDesign().getOperatorComponentInstances()) {
      generateCoreLoad(doc, perfs, op, statGen);
    }
    // Ending the performance stats
  }

  /**
   * Generate core load.
   *
   * @param op
   *          the op
   * @param statGen
   *          the stat gen
   */
  private static void generateCoreLoad(final Document doc, final Element root, final ComponentInstance op,
      final IStatGenerator statGen) {
    // Starting core load stat
    Element core = doc.createElement("core");
    root.appendChild(core);
    // Id of the core
    core.setAttribute("id", op.getInstanceName());
    // Load of the core
    core.setAttribute("load", Long.toString(statGen.getLoad(op)));
    // Memory used of the core
    core.setAttribute("used_mem", Long.toString(statGen.getMem(op)));
    // ID for the plotter
    core.setTextContent("Core_" + op.getInstanceName().replace(" ", "_") + ".");
    // Ending core load stat
  }

  /**
   * Generate scheduling stats.
   *
   * @param data
   *          the data
   */
  private static void generateSchedulingStats(final Document doc, final Element root, final GanttData data) {
    // Print the scheduling stats for each core
    for (final GanttComponent component : data.getComponents()) {
      for (final GanttTask task : component.getTasks()) {
        generateTaskStats(doc, root, task, component.getId());
      }
    }
  }

  /**
   * Generate task stats.
   *
   * @param task
   *          the task
   */
  private static void generateTaskStats(final Document doc, final Element root, final GanttTask task,
      final String componentID) {
    // Starting task
    Element event = doc.createElement("event");
    root.appendChild(event);
    // Start time
    event.setAttribute("start", Long.toString(task.getStartTime()));
    // End time
    event.setAttribute("end", Long.toString(task.getStartTime() + task.getDuration()));
    // Task name
    event.setAttribute("title", task.getId());
    // Core
    event.setAttribute("mapping", componentID);
    // Color
    Color c = task.getColor();
    if (c == null) {
      event.setAttribute("color", XMLStatsExporter.TASKCOLOR);
    } else {
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();
      // a bit ugly an unsafe, but it seems to work
      String colHexa = "#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);
      event.setAttribute("color", colHexa);
    }
    // Gantt ID for the task
    event.setTextContent("Step_" + task.getId().replace(" ", "_") + ".");
    // Ending task
  }

}
