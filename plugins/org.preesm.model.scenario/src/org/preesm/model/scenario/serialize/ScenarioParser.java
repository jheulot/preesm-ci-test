/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2019)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Daniel Madroñal <daniel.madronal@upm.es> (2018)
 * Jonathan Piat <jpiat@laas.fr> (2008 - 2011)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2015)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2016)
 * Pengcheng Mu <pengcheng.mu@insa-rennes.fr> (2008)
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
package org.preesm.model.scenario.serialize;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.preesm.commons.exceptions.PreesmRuntimeException;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.pisdf.AbstractActor;
import org.preesm.model.pisdf.Parameter;
import org.preesm.model.pisdf.PiGraph;
import org.preesm.model.pisdf.serialize.PiParser;
import org.preesm.model.pisdf.util.ActorPath;
import org.preesm.model.scenario.ConstraintGroup;
import org.preesm.model.scenario.MemCopySpeed;
import org.preesm.model.scenario.PreesmScenario;
import org.preesm.model.scenario.Timing;
import org.preesm.model.scenario.TimingManager;
import org.preesm.model.scenario.papi.PapiComponent;
import org.preesm.model.scenario.papi.PapiEvent;
import org.preesm.model.scenario.papi.PapiEventModifier;
import org.preesm.model.scenario.papi.PapiEventSet;
import org.preesm.model.scenario.papi.PapiEventSetType;
import org.preesm.model.scenario.papi.PapifyConfigActor;
import org.preesm.model.scenario.papi.PapifyConfigPE;
import org.preesm.model.scenario.types.DataType;
import org.preesm.model.scenario.types.VertexType;
import org.preesm.model.slam.ComponentInstance;
import org.preesm.model.slam.Design;
import org.preesm.model.slam.component.Component;
import org.preesm.model.slam.serialize.SlamParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * An xml parser retrieving scenario data.
 *
 * @author mpelcat
 */
public class ScenarioParser {

  /** xml tree. */
  private Document dom = null;

  /** scenario being retrieved. */
  private PreesmScenario scenario = null;

  /**
   * Instantiates a new scenario parser.
   */
  public ScenarioParser() {
    this.scenario = new PreesmScenario();
  }

  /**
   * Gets the dom.
   *
   * @return the dom
   */
  public Document getDom() {
    return this.dom;
  }

  /**
   * Retrieves the DOM document.
   *
   * @param file
   *          the file
   * @return the preesm scenario
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws CoreException
   *           the core exception
   */
  public PreesmScenario parseXmlFile(final IFile file) throws FileNotFoundException, CoreException {
    // get the factory
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    try {
      // Using factory get an instance of document builder
      final DocumentBuilder db = dbf.newDocumentBuilder();

      // parse using builder to get DOM representation of the XML file
      this.dom = db.parse(file.getContents());
    } catch (final ParserConfigurationException | SAXException | IOException | CoreException e) {

      PreesmLogger.getLogger().log(Level.WARNING, "Could not parse file: " + e.getMessage(), e);
      return null;
    }

    if (this.dom != null) {
      // get the root elememt
      final Element docElt = this.dom.getDocumentElement();

      Node node = docElt.getFirstChild();

      while (node != null) {

        if (node instanceof Element) {
          final Element elt = (Element) node;
          final String type = elt.getTagName();
          switch (type) {
            case "files":
              parseFileNames(elt);
              break;
            case "constraints":
              parseConstraintGroups(elt);
              break;
            case "timings":
              parseTimings(elt);
              break;
            case "simuParams":
              parseSimuParams(elt);
              break;
            case "parameterValues":
              parseParameterValues(elt);
              break;
            case "papifyConfigs":
              parsePapifyConfigs(elt);
              break;
            case "variables":
              // deprecated
              break;
            case "relativeconstraints":
              // deprecated
              break;
            default:
          }
        }

        node = node.getNextSibling();
      }
    }

    this.scenario.setScenarioURL(file.getFullPath().toString());
    return this.scenario;
  }

  /**
   * Retrieves all the parameter values.
   *
   * @param paramValuesElt
   *          the param values elt
   */
  private void parseParameterValues(final Element paramValuesElt) {

    Node node = paramValuesElt.getFirstChild();

    final PiGraph graph = scenario.getAlgorithm();
    if (graph != null) {
      final Set<Parameter> parameters = new LinkedHashSet<>();
      for (final Parameter p : graph.getAllParameters()) {
        if (!p.isConfigurationInterface()) {
          parameters.add(p);
        }
      }

      while (node != null) {
        if (node instanceof Element) {
          final Element elt = (Element) node;
          final String type = elt.getTagName();
          if (type.equals("parameter")) {
            final Parameter justParsed = parseParameterValue(elt, graph);
            parameters.remove(justParsed);
          }
        }

        node = node.getNextSibling();
      }

      // Create a parameter value foreach parameter not yet in the
      // scenario
      for (final Parameter p : parameters) {
        this.scenario.getParameterValueManager().addParameterValue(p);
      }
    }
  }

  /**
   * Retrieve a ParameterValue.
   *
   * @param paramValueElt
   *          the param value elt
   * @param graph
   *          the graph
   * @return the parameter
   */
  private Parameter parseParameterValue(final Element paramValueElt, final PiGraph graph) {
    if (graph == null) {
      throw new IllegalArgumentException();
    }

    Parameter currentParameter = null;

    final String type = paramValueElt.getAttribute("type");
    final String parent = paramValueElt.getAttribute("parent");
    final String name = paramValueElt.getAttribute("name");
    String stringValue = paramValueElt.getAttribute("value");

    currentParameter = graph.lookupParameterGivenGraph(name, parent);
    if (currentParameter == null) {
      PreesmLogger.getLogger().log(Level.WARNING,
          "Parameter with name '" + name + "' cannot be found in PiGraph '" + parent + "'.");
    } else {
      switch (type) {
        case "INDEPENDENT":
        case "STATIC": // Retro-compatibility
          this.scenario.getParameterValueManager().addIndependentParameterValue(currentParameter, stringValue, parent);
          break;
        case "ACTOR_DEPENDENT":
        case "DYNAMIC": // Retro-compatibility
          if ((stringValue.charAt(0) == '[') && (stringValue.charAt(stringValue.length() - 1) == ']')) {
            stringValue = stringValue.substring(1, stringValue.length() - 1);
            final String[] values = stringValue.split(",");

            final Set<Integer> newValues = new LinkedHashSet<>();

            try {
              for (final String val : values) {
                newValues.add(Integer.parseInt(val.trim()));
              }
            } catch (final NumberFormatException e) {
              // TODO: Do smthg?
            }
            this.scenario.getParameterValueManager().addActorDependentParameterValue(currentParameter, newValues,
                parent);
          }
          break;
        case "PARAMETER_DEPENDENT":
        case "DEPENDENT": // Retro-compatibility
          final Set<String> inputParameters = new LinkedHashSet<>();

          for (final Parameter input : currentParameter.getInputParameters()) {
            inputParameters.add(input.getName());
          }
          this.scenario.getParameterValueManager().addParameterDependentParameterValue(currentParameter, stringValue,
              inputParameters, parent);
          break;
        default:
          throw new PreesmRuntimeException("Unknown Parameter type: " + type + " for Parameter: " + name);
      }
    }
    return currentParameter;
  }

  /**
   * Parses the simulation parameters.
   *
   * @param filesElt
   *          the files elt
   */
  private void parseSimuParams(final Element filesElt) {

    Node node = filesElt.getFirstChild();

    while (node != null) {

      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        final String content = elt.getTextContent();
        switch (type) {
          case "mainCore":
            final ComponentInstance mainCore = scenario.getDesign().getComponentInstance(content);
            this.scenario.getSimulationManager().setMainOperator(mainCore);
            break;
          case "mainComNode":
            final ComponentInstance mainComNode = scenario.getDesign().getComponentInstance(content);
            this.scenario.getSimulationManager().setMainComNode(mainComNode);
            break;
          case "averageDataSize":
            this.scenario.getSimulationManager().setAverageDataSize(Long.valueOf(content));
            break;
          case "dataTypes":
            parseDataTypes(elt);
            break;
          case "specialVertexOperators":
            parseSpecialVertexOperators(elt);
            break;
          case "numberOfTopExecutions":
            this.scenario.getSimulationManager().setNumberOfTopExecutions(Integer.parseInt(content));
            break;
          default:
        }
      }

      node = node.getNextSibling();
    }
  }

  /**
   * Retrieves the data types.
   *
   * @param dataTypeElt
   *          the data type elt
   */
  private void parseDataTypes(final Element dataTypeElt) {

    Node node = dataTypeElt.getFirstChild();

    while (node != null) {

      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        if (type.equals("dataType")) {
          final String name = elt.getAttribute("name");
          final String size = elt.getAttribute("size");

          if (!name.isEmpty() && !size.isEmpty()) {
            final DataType dataType = new DataType(name, Integer.parseInt(size));
            this.scenario.getSimulationManager().putDataType(dataType);
          }
        }
      }

      node = node.getNextSibling();
    }
  }

  /**
   * Retrieves the operators able to execute fork/join/broadcast.
   *
   * @param spvElt
   *          the spv elt
   */
  private void parseSpecialVertexOperators(final Element spvElt) {

    Node node = spvElt.getFirstChild();

    while (node != null) {

      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        if (type.equals("specialVertexOperator")) {
          final String path = elt.getAttribute("path");

          if (path != null) {
            final ComponentInstance componentInstance = this.scenario.getDesign().getComponentInstance(path);
            this.scenario.getSimulationManager().addSpecialVertexOperator(componentInstance);
          }
        }
      }

      node = node.getNextSibling();
    }

    /*
     * It is not possible to remove all operators from special vertex executors: if no operator is selected, all of them
     * are!!
     */
    if (this.scenario.getSimulationManager().getSpecialVertexOperators().isEmpty()
        && (this.scenario.getOperatorIds() != null)) {
      for (final ComponentInstance opId : this.scenario.getOperators()) {
        this.scenario.getSimulationManager().addSpecialVertexOperator(opId);
      }
    }
  }

  /**
   * Parses the archi and algo files and retrieves the file contents.
   *
   * @param filesElt
   *          the files elt
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws CoreException
   *           the core exception
   */
  private void parseFileNames(final Element filesElt) {

    Node node = filesElt.getFirstChild();

    while (node != null) {

      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        final String url = elt.getAttribute("url");
        if (url.length() > 0) {
          if (type.equals("algorithm")) {
            try {
              this.scenario.setAlgorithm(PiParser.getPiGraphWithReconnection(url));
            } catch (final Exception e) {
              PreesmLogger.getLogger().log(Level.WARNING, "Could not parse the algorithm: " + e.getMessage(), e);
            }
          } else if (type.equals("architecture")) {
            try {
              initializeArchitectureInformation(url);
            } catch (final Exception e) {
              throw new PreesmRuntimeException("Could not parse the architecture: " + e.getMessage(), e);
            }
          } else if (type.equals("codegenDirectory")) {
            this.scenario.setCodegenDirectory(url);
          }
        }
      }

      node = node.getNextSibling();
    }
  }

  /**
   * Depending on the architecture model, parses the model and populates the scenario.
   *
   * @param url
   *          the url
   */
  private Design initializeArchitectureInformation(final String url) {
    final Design design = SlamParser.parseSlamDesign(url);
    this.scenario.setArchitecture(design);
    return design;
  }

  /**
   * Retrieves all the constraint groups.
   *
   * @param cstGroupsElt
   *          the cst groups elt
   */
  private void parseConstraintGroups(final Element cstGroupsElt) {

    final String excelFileUrl = cstGroupsElt.getAttribute("excelUrl");
    this.scenario.getConstraintGroupManager().setExcelFileURL(excelFileUrl);

    Node node = cstGroupsElt.getFirstChild();

    while (node != null) {

      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        if (type.equals("constraintGroup")) {
          final ConstraintGroup cg = getConstraintGroup(elt);
          this.scenario.getConstraintGroupManager().addConstraintGroup(cg);
        }
      }

      node = node.getNextSibling();
    }
  }

  /**
   * Retrieves a constraint group.
   *
   * @param cstGroupElt
   *          the cst group elt
   * @return the constraint group
   */
  private ConstraintGroup getConstraintGroup(final Element cstGroupElt) {
    ComponentInstance opId = null;
    final Set<AbstractActor> paths = new LinkedHashSet<>();

    if (scenario.getAlgorithm() != null) {
      Node node = cstGroupElt.getFirstChild();
      while (node != null) {
        if (node instanceof Element) {
          final Element elt = (Element) node;
          final String type = elt.getTagName();
          final String name = elt.getAttribute("name");
          if (type.equals(VertexType.TYPE_TASK)) {
            final AbstractActor actorFromPath = getActorFromPath(name);
            if (actorFromPath != null) {
              paths.add(actorFromPath);
            }
          } else if (type.equals("operator")) {
            if (this.scenario.getDesign().containsComponentInstance(name)) {
              opId = this.scenario.getDesign().getComponentInstance(name);
            }
          }
        }
        node = node.getNextSibling();
      }
    }

    final ConstraintGroup cg = new ConstraintGroup(opId);
    cg.addVertexPaths(paths);
    return cg;
  }

  /**
   * Retrieves all the papifyConfig groups.
   *
   * @param cstGroupsElt
   *          the cst groups elt
   */
  private void parsePapifyConfigs(final Element papifyConfigsElt) {

    final String xmlFileURL = papifyConfigsElt.getAttribute("xmlUrl");
    this.scenario.getPapifyConfigManager().setExcelFileURL(xmlFileURL);

    Node node = papifyConfigsElt.getFirstChild();

    while (node != null) {

      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        if (type.equals("papifyConfigActor")) {
          final PapifyConfigActor pg = getPapifyConfigActor(elt);
          this.scenario.getPapifyConfigManager().addPapifyConfigActorGroup(pg);
        } else if (type.equals("papifyConfigPE")) {
          final PapifyConfigPE pg = getPapifyConfigPE(elt);
          this.scenario.getPapifyConfigManager().addPapifyConfigPEGroup(pg);
        }
      }

      node = node.getNextSibling();
    }
  }

  /**
   * Retrieves a papifyConfigActor.
   *
   * @param papifyConfigElt
   *          the papifyConfig group elt
   * @return the papifyConfig
   */
  private PapifyConfigActor getPapifyConfigActor(final Element papifyConfigElt) {

    Node node = papifyConfigElt.getFirstChild();
    PapifyConfigActor pc = new PapifyConfigActor();

    while (node != null) {
      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        String actorPath = "";
        String actorId = "";
        if (type.equals("actorPath")) {
          actorPath = elt.getAttribute("actorPath");
          pc.addActorPath(actorPath);
        } else if (type.equals("actorId")) {
          actorId = elt.getAttribute("actorId");
          pc.addActorId(actorId);
          Node nodeEvents = node.getFirstChild();
          while (nodeEvents != null) {

            if (nodeEvents instanceof Element) {
              final Element eltEvents = (Element) nodeEvents;
              final String typeEvents = eltEvents.getTagName();
              if (typeEvents.equals("component")) {
                final String component = eltEvents.getAttribute("component");
                final Set<PapiEvent> eventSet = new LinkedHashSet<>();
                final List<PapiEvent> eventList = getPapifyEvents(eltEvents);
                eventSet.addAll(eventList);
                pc.addPAPIEventSet(component, eventSet);
              }
            }
            nodeEvents = nodeEvents.getNextSibling();
          }
        }

      }
      node = node.getNextSibling();
    }
    return pc;
  }

  /**
   * Retrieves a papifyConfigPE.
   *
   * @param papifyConfigElt
   *          the papifyConfig group elt
   * @return the papifyConfig
   */
  private PapifyConfigPE getPapifyConfigPE(final Element papifyConfigElt) {

    PapifyConfigPE pc = null;
    Node node = papifyConfigElt.getFirstChild();

    while (node != null) {
      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String peType = elt.getAttribute("peType");
        pc = new PapifyConfigPE(peType);
        final Set<PapiComponent> components = getPAPIComponents(elt);
        pc.addPAPIComponents(components);
      }
      node = node.getNextSibling();
    }
    return pc;
  }

  /**
   * Retrieves all the PAPI components.
   *
   * @param componentsElt
   *          the PAPI components elt
   */
  private Set<PapiComponent> getPAPIComponents(final Element componentsElt) {

    Set<PapiComponent> components = new LinkedHashSet<>();
    Node node = componentsElt.getFirstChild();

    while (node != null) {

      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        if (type.equals("PAPIComponent")) {
          final PapiComponent cg = getPapifyComponent(elt);
          components.add(cg);
        }
      }

      node = node.getNextSibling();
    }
    return components;
  }

  /**
   * Retrieves the component info.
   *
   * @param node
   *          the papi component elt
   * @return the papi component
   */
  private PapiComponent getPapifyComponent(final Element papifyComponentElt) {

    PapiComponent component = null;
    final String componentId = papifyComponentElt.getAttribute("componentId");
    final String componentIndex = papifyComponentElt.getAttribute("componentIndex");
    final String componentType = papifyComponentElt.getAttribute("componentType");

    component = new PapiComponent(componentId, componentIndex, componentType);

    final List<PapiEventSet> eventSetList = new ArrayList<>();

    Node node = papifyComponentElt.getFirstChild();
    while (node != null) {
      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getAttribute("type");
        final PapiEventSet eventSet = new PapiEventSet();

        final List<PapiEvent> eventList = getPapifyEvents(elt);
        eventSet.setType(Optional.ofNullable(type).map(PapiEventSetType::valueOf).orElse(null));
        eventSet.setEvents(eventList);
        eventSetList.add(eventSet);
      }
      node = node.getNextSibling();
    }
    component.setEventSets(eventSetList);
    return component;
  }

  /**
   * Retrieves the events info.
   *
   * @param node
   *          the event set elt
   * @return the papi list of events
   */
  private List<PapiEvent> getPapifyEvents(final Element papifyEventsElt) {

    final List<PapiEvent> eventList = new ArrayList<>();
    Node node = papifyEventsElt.getFirstChild();
    while (node != null) {
      if (node instanceof Element) {
        final Element elt = (Element) node;
        final PapiEvent event = getPapifyEvent(elt);
        eventList.add(event);
      }
      node = node.getNextSibling();
    }
    return eventList;
  }

  /**
   * Retrieves the event info.
   *
   * @param node
   *          the event elt
   * @return the papi event
   */
  private PapiEvent getPapifyEvent(final Element papifyEventElt) {

    final PapiEvent event = new PapiEvent();
    Node node = papifyEventElt.getFirstChild();
    final List<PapiEventModifier> eventModiferList = new ArrayList<>();
    while (node != null) {
      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        if (type.equals("eventDescription")) {
          final String eventDescription = elt.getAttribute("eventDescription");
          event.setDesciption(eventDescription);
        } else if (type.equals("eventId")) {
          final String eventId = elt.getAttribute("eventId");
          event.setIndex(Integer.valueOf(eventId));
        } else if (type.equals("eventName")) {
          final String eventName = elt.getAttribute("eventName");
          event.setName(eventName);
        } else if (type.equals("eventModifier")) {
          final PapiEventModifier eventModifer = new PapiEventModifier();
          final String description = elt.getAttribute("description");
          eventModifer.setDescription(description);
          final String name = elt.getAttribute("name");
          eventModifer.setName(name);
          eventModiferList.add(eventModifer);
        }
      }
      node = node.getNextSibling();
    }
    event.setModifiers(eventModiferList);
    return event;
  }

  /**
   * Retrieves the timings.
   *
   * @param timingsElt
   *          the timings elt
   */
  private void parseTimings(final Element timingsElt) {

    final String timingFileUrl = timingsElt.getAttribute("excelUrl");
    this.scenario.getTimingManager().setExcelFileURL(timingFileUrl);

    Node node = timingsElt.getFirstChild();

    while (node != null) {

      if (node instanceof Element) {
        final Element elt = (Element) node;
        final String type = elt.getTagName();
        if (type.equals("timing")) {
          final Timing timing = getTiming(elt);
          if (timing != null) {
            this.scenario.getTimingManager().addTiming(timing);
          }
        } else if (type.equals("memcpyspeed")) {
          retrieveMemcpySpeed(this.scenario.getTimingManager(), elt);
        }
      }

      node = node.getNextSibling();
    }
  }

  /**
   * Retrieves one timing.
   *
   * @param timingElt
   *          the timing elt
   * @return the timing
   */
  private Timing getTiming(final Element timingElt) {

    Timing timing = null;

    if (scenario.getAlgorithm() != null) {

      final String type = timingElt.getTagName();
      if (type.equals("timing")) {
        final String vertexpath = timingElt.getAttribute("vertexname");
        final String opdefname = timingElt.getAttribute("opname");
        long time;
        final String stringValue = timingElt.getAttribute("time");
        boolean isEvaluated = false;
        try {
          time = Long.parseLong(stringValue);
          isEvaluated = true;
        } catch (final NumberFormatException e) {
          time = -1;
        }

        final boolean contains = this.scenario.getDesign().containsComponent(opdefname);
        final AbstractActor lookup = ActorPath.lookup(this.scenario.getAlgorithm(), vertexpath);
        if ((lookup != null) && contains) {
          final Component component = this.scenario.getDesign().getComponent(opdefname);
          if (isEvaluated) {
            timing = new Timing(component, lookup, time);
          } else {
            timing = new Timing(component, lookup, stringValue);
          }
        }
      }
    }

    return timing;
  }

  /**
   * Returns an actor Object (either SDFAbstractVertex from SDFGraph or AbstractActor from PiGraph) from the path in its
   * container graph.
   *
   * @param path
   *          the path to the actor, where its segment is the name of an actor and separators are "/"
   * @return the wanted actor, if existing, null otherwise
   */
  private AbstractActor getActorFromPath(final String path) {
    AbstractActor result = null;
    if (scenario.getAlgorithm() != null) {
      result = ActorPath.lookup(scenario.getAlgorithm(), path);
    }
    return result;
  }

  /**
   * Retrieves one memcopy speed composed of integer setup time and timeperunit.
   *
   * @param timingManager
   *          the timing manager
   * @param timingElt
   *          the timing elt
   */
  private void retrieveMemcpySpeed(final TimingManager timingManager, final Element timingElt) {

    if (scenario.getAlgorithm() != null) {
      final String type = timingElt.getTagName();
      if (type.equals("memcpyspeed")) {
        final String opdefname = timingElt.getAttribute("opname");
        final String sSetupTime = timingElt.getAttribute("setuptime");
        final String sTimePerUnit = timingElt.getAttribute("timeperunit");
        long setupTime;
        double timePerUnit;

        try {
          setupTime = Long.parseLong(sSetupTime);
          timePerUnit = Double.parseDouble(sTimePerUnit);
        } catch (final NumberFormatException e) {
          setupTime = -1;
          timePerUnit = -1;
        }

        final boolean contains = this.scenario.getDesign().containsComponent(opdefname);
        if (contains && (setupTime >= 0) && (timePerUnit >= 0)) {
          final Component component = this.scenario.getDesign().getComponent(opdefname);
          final MemCopySpeed speed = new MemCopySpeed(component, setupTime, timePerUnit);
          timingManager.putMemcpySpeed(speed);
        }
      }

    }
  }
}
