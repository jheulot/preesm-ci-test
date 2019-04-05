package org.preesm.model.pisdf.brv;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.pisdf.AbstractVertex;
import org.preesm.model.pisdf.PiGraph;
import org.preesm.model.pisdf.statictools.PiMMHelper;
import org.preesm.model.pisdf.util.PiGraphConsistenceChecker;
import org.preesm.workflow.elements.Workflow;
import org.preesm.workflow.implement.AbstractTaskImplementation;
import org.preesm.workflow.implement.AbstractWorkflowNodeImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This task computes and exports BRV of a PiSDF graph, as a CSV file.
 * 
 * @author ahonorat
 */
public class BRVExporter extends AbstractTaskImplementation {

  /**
   * @see StatsExporterTask
   */
  public static final String DEFAULT_PATH = "/stats/xml/";

  public static final String PARAM_PATH = "path";

  @Override
  public Map<String, Object> execute(Map<String, Object> inputs, Map<String, String> parameters,
      IProgressMonitor monitor, String nodeName, Workflow workflow) {

    final PiGraph graph = (PiGraph) inputs.get(AbstractWorkflowNodeImplementation.KEY_PI_GRAPH);
    PreesmLogger.getLogger().log(Level.INFO, "Computing Repetition Vector for graph [" + graph.getName() + "]");

    PiGraphConsistenceChecker.check(graph);
    // 1. First we resolve all parameters.
    // It must be done first because, when removing persistence, local parameters have to be known at upper level
    PiMMHelper.resolveAllParameters(graph);
    // 2. Compute BRV following the chosen method
    Map<AbstractVertex, Long> brv = PiBRV.compute(graph, BRVMethod.LCM);

    String folderPath = parameters.get("path");

    // Get the root of the workspace
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IWorkspaceRoot root = workspace.getRoot();
    // Get the project
    final String projectName = workflow.getProjectName();
    final IProject project = root.getProject(projectName);

    // Get a complete valid path with all folders existing
    folderPath = project.getLocation() + folderPath;
    final File parent = new File(folderPath);
    parent.mkdirs();

    final String filePath = graph.getName() + "_stats_brv.xml";
    final File file = new File(parent, filePath);

    generateXML(brv, file);

    Map<String, Object> res = new LinkedHashMap<>();
    res.put(AbstractWorkflowNodeImplementation.KEY_PI_GRAPH, graph);

    return res;
  }

  @Override
  public Map<String, String> getDefaultParameters() {
    final Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put(PARAM_PATH, DEFAULT_PATH);
    return parameters;
  }

  @Override
  public String monitorMessage() {
    return "Computes and exports repetition vector as csv.";
  }

  private static void generateXML(final Map<AbstractVertex, Long> brv, final File file) {
    final Map<PiGraph, Long> levelRV = new HashMap<>();
    final Map<AbstractVertex, Long> fullRV = new HashMap<>();

    for (final Entry<AbstractVertex, Long> en : brv.entrySet()) {
      final AbstractVertex av = en.getKey();
      final PiGraph container = av.getContainingPiGraph();
      if (!levelRV.containsKey(container)) {
        levelRV.put(container, PiMMHelper.getHierarchichalRV(container, brv));
      }
      final long actorRV = en.getValue();
      final long actorFullRV = actorRV * levelRV.get(container);
      fullRV.put(av, actorFullRV);
    }

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder;
    try {
      dBuilder = dbFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new PreesmRuntimeException(e);
    }
    Document content = dBuilder.newDocument();

    // Generate the stats to write in an xml file
    generateXMLStats(content, brv, fullRV);

    // Write the file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
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

  private static void generateXMLStats(Document doc, Map<AbstractVertex, Long> brv, Map<AbstractVertex, Long> fullRV) {

    Element root = doc.createElement("brv");
    doc.appendChild(root);

    for (final Entry<AbstractVertex, Long> en : brv.entrySet()) {
      final AbstractVertex av = en.getKey();
      final long actorRV = en.getValue();
      final long actorFullRV = fullRV.get(av);

      String fullName = av.getVertexPath();
      String shortName = av.getName();

      Element nbRepeat = doc.createElement("nbRepeat");
      root.appendChild(nbRepeat);

      nbRepeat.setAttribute("fullName", fullName);
      nbRepeat.setAttribute("shortName", shortName);
      nbRepeat.setAttribute("hierRV", Long.toString(actorRV));
      nbRepeat.setAttribute("fullRV", Long.toString(actorFullRV));

    }

  }

}
