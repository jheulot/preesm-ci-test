/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Daniel Madroñal <daniel.madronal@upm.es> (2018)
 * Jonathan Piat <jpiat@laas.fr> (2011)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2013)
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

import java.util.Map;
import java.util.Set;
import org.preesm.model.pisdf.AbstractActor;
import org.preesm.model.pisdf.PiGraph;
import org.preesm.model.scenario.ConstraintGroup;
import org.preesm.model.scenario.ParameterValue;
import org.preesm.model.scenario.ParameterValueManager;
import org.preesm.model.scenario.PreesmScenario;
import org.preesm.model.scenario.RelativeConstraintManager;
import org.preesm.model.scenario.Timing;
import org.preesm.model.scenario.papi.PapiComponent;
import org.preesm.model.scenario.papi.PapiEvent;
import org.preesm.model.scenario.papi.PapiEventModifier;
import org.preesm.model.scenario.papi.PapiEventSet;
import org.preesm.model.scenario.papi.PapifyConfigActor;
import org.preesm.model.scenario.papi.PapifyConfigManager;
import org.preesm.model.scenario.papi.PapifyConfigPE;
import org.preesm.model.scenario.types.DataType;
import org.preesm.model.scenario.types.VertexType;
import org.preesm.model.slam.ComponentInstance;
import org.preesm.model.slam.Design;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

// TODO: Auto-generated Javadoc
/**
 * Writes a scenario as an XML.
 *
 * @author mpelcat
 */
public class ScenarioWriter {

  /** Current document. */
  private Document dom;

  /** Current scenario. */
  private final PreesmScenario scenario;

  /**
   * Instantiates a new scenario writer.
   *
   * @param scenario
   *          the scenario
   */
  public ScenarioWriter(final PreesmScenario scenario) {
    super();

    this.scenario = scenario;

    try {
      DOMImplementation impl;
      impl = DOMImplementationRegistry.newInstance().getDOMImplementation("Core 3.0 XML 3.0 LS");
      this.dom = impl.createDocument("", "scenario", null);
    } catch (final Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Generate scenario DOM.
   *
   * @return the document
   */
  public Document generateScenarioDOM() {

    final Element root = this.dom.getDocumentElement();

    addFiles(root);
    addConstraints(root);
    addRelativeConstraints(root);
    addTimings(root);
    addSimuParams(root);
    addParameterValues(root);
    addPapifyConfigs(root);

    return this.dom;
  }

  /**
   * Adds the parameter values.
   *
   * @param parent
   *          the parent
   */
  private void addParameterValues(final Element parent) {
    final Element valuesElt = this.dom.createElement("parameterValues");
    parent.appendChild(valuesElt);

    final ParameterValueManager manager = this.scenario.getParameterValueManager();

    for (final ParameterValue value : manager.getParameterValues()) {
      addParameterValue(valuesElt, value);
    }
  }

  /**
   * Adds the parameter value.
   *
   * @param parent
   *          the parent
   * @param value
   *          the value
   */
  private void addParameterValue(final Element parent, final ParameterValue value) {
    // Serialize only if the kept value(s) is different from the "default value" found in the PiGraph:
    // - if the parameter is actor dependent, there is no default value
    // - otherwise, compare the kept value to the parameter expression
    boolean needToBeSerialized = false;
    String valueToPrint = "";
    switch (value.getType()) {
      case INDEPENDENT:
        valueToPrint = "" + value.getValue();
        if (!value.getParameter().getExpression().getExpressionAsString().equals(valueToPrint)) {
          needToBeSerialized = true;
        }
        break;
      case ACTOR_DEPENDENT:
        valueToPrint = value.getValues().toString();
        needToBeSerialized = true;
        break;
      case PARAMETER_DEPENDENT:
        valueToPrint = value.getExpression();
        if (!value.getParameter().getExpression().getExpressionAsString().equals(valueToPrint)) {
          needToBeSerialized = true;
        }
        break;
      default:
    }
    if (needToBeSerialized) {
      final Element valueElt = this.dom.createElement("parameter");
      parent.appendChild(valueElt);

      valueElt.setAttribute("name", value.getName());
      valueElt.setAttribute("parent", value.getParentVertex());
      valueElt.setAttribute("type", value.getType().toString());

      valueElt.setAttribute("value", valueToPrint);
    }
  }

  /**
   * Adds the papify configurations.
   *
   * @param parent
   *          the parent
   */
  private void addPapifyConfigs(final Element parent) {
    final Element papifyConfigs = this.dom.createElement("papifyConfigs");
    parent.appendChild(papifyConfigs);

    papifyConfigs.setAttribute("xmlUrl", this.scenario.getPapifyConfigManager().getXmlFileURL());

    final PapifyConfigManager manager = this.scenario.getPapifyConfigManager();

    for (final PapifyConfigActor config : manager.getPapifyConfigGroupsActors()) {
      addPapifyConfigActor(papifyConfigs, config);
    }
    for (final PapifyConfigPE config : manager.getPapifyConfigGroupsPEs()) {
      addPapifyConfigPE(papifyConfigs, config);
    }
  }

  /**
   * Adds the papify config Actor.
   *
   * @param parent
   *          the parent
   * @param value
   *          the value
   */
  private void addPapifyConfigActor(final Element parent, final PapifyConfigActor config) {

    if (!config.getActorId().equals("") && (config.getPAPIEvents() != null) && !config.getPAPIEvents().isEmpty()) {
      final Element papifyConfigElt = this.dom.createElement("papifyConfigActor");
      parent.appendChild(papifyConfigElt);

      final Element actorPath = this.dom.createElement("actorPath");
      papifyConfigElt.appendChild(actorPath);
      actorPath.setAttribute("actorPath", config.getActorPath());
      final Element actorId = this.dom.createElement("actorId");
      papifyConfigElt.appendChild(actorId);
      actorId.setAttribute("actorId", config.getActorId());
      final Map<String, Set<PapiEvent>> eventSets = config.getPAPIEvents();
      final Set<String> keys = eventSets.keySet();
      for (final String key : keys) {
        final Set<PapiEvent> eventSet = eventSets.get(key);
        if (!eventSet.isEmpty()) {
          final Element component = this.dom.createElement("component");
          actorId.appendChild(component);
          component.setAttribute("component", key);
          for (final PapiEvent event : eventSet) {
            final Element singleEvent = this.dom.createElement("event");
            component.appendChild(singleEvent);
            addPapifyEvent(singleEvent, event);
          }
        }
      }
    }
  }

  /**
   * Adds the papify config PE.
   *
   * @param parent
   *          the parent
   * @param value
   *          the value
   */
  private void addPapifyConfigPE(final Element parent, final PapifyConfigPE config) {

    if (!config.getpeType().equals("") && (config.getPAPIComponents() != null)
        && !config.getPAPIComponents().isEmpty()) {
      final Element papifyConfigElt = this.dom.createElement("papifyConfigPE");
      parent.appendChild(papifyConfigElt);

      final Element peType = this.dom.createElement("peType");
      papifyConfigElt.appendChild(peType);
      peType.setAttribute("peType", config.getpeType());
      final Set<PapiComponent> components = config.getPAPIComponents();

      for (final PapiComponent component : components) {
        final Element singleComponent = this.dom.createElement("PAPIComponent");
        peType.appendChild(singleComponent);
        addPapifyComponent(singleComponent, component);
      }
    }
  }

  /**
   * Adds the papify component.
   *
   * @param component
   *          the parent component
   * @param papiComponent
   *          the papiComponent itself
   */
  private void addPapifyComponent(final Element component, final PapiComponent papiComponent) {

    component.setAttribute("componentId", papiComponent.getId());
    component.setAttribute("componentType", papiComponent.getType().toString());
    component.setAttribute("componentIndex", Integer.toString(papiComponent.getIndex()));

    for (final PapiEventSet eventSet : papiComponent.getEventSets()) {
      final Element singleEventSet = this.dom.createElement("eventSet");
      component.appendChild(singleEventSet);
      singleEventSet.setAttribute("type", eventSet.getType().toString());
      for (final PapiEvent event : eventSet.getEvents()) {
        final Element singleEvent = this.dom.createElement("event");
        singleEventSet.appendChild(singleEvent);
        addPapifyEvent(singleEvent, event);
      }
    }
  }

  /**
   * Adds the papify event.
   *
   * @param event
   *          the parent event
   * @param papiEvent
   *          the papiEvent itself
   */
  private void addPapifyEvent(final Element event, final PapiEvent papiEvent) {

    final Element eventId = this.dom.createElement("eventId");
    event.appendChild(eventId);
    eventId.setAttribute("eventId", Integer.toString(papiEvent.getIndex()));
    final Element eventName = this.dom.createElement("eventName");
    event.appendChild(eventName);
    eventName.setAttribute("eventName", papiEvent.getName());
    final Element eventDescription = this.dom.createElement("eventDescription");
    event.appendChild(eventDescription);
    eventDescription.setAttribute("eventDescription", papiEvent.getDescription());

    for (final PapiEventModifier eventModifier : papiEvent.getModifiers()) {
      final Element singleEventModifier = this.dom.createElement("eventModifier");
      event.appendChild(singleEventModifier);
      singleEventModifier.setAttribute("name", eventModifier.getName());
      singleEventModifier.setAttribute("description", eventModifier.getDescription());
    }
  }

  /**
   * Adds the simu params.
   *
   * @param parent
   *          the parent
   */
  private void addSimuParams(final Element parent) {

    final Element params = this.dom.createElement("simuParams");
    parent.appendChild(params);

    final Element core = this.dom.createElement("mainCore");
    params.appendChild(core);
    core.setTextContent(this.scenario.getSimulationManager().getMainOperator().getInstanceName());

    final Element medium = this.dom.createElement("mainComNode");
    params.appendChild(medium);
    medium.setTextContent(this.scenario.getSimulationManager().getMainComNode().getInstanceName());

    final Element dataSize = this.dom.createElement("averageDataSize");
    params.appendChild(dataSize);
    dataSize.setTextContent(String.valueOf(this.scenario.getSimulationManager().getAverageDataSize()));

    final Element dataTypes = this.dom.createElement("dataTypes");
    params.appendChild(dataTypes);

    for (final DataType dataType : this.scenario.getSimulationManager().getDataTypes().values()) {
      addDataType(dataTypes, dataType);
    }

    final Element sVOperators = this.dom.createElement("specialVertexOperators");
    params.appendChild(sVOperators);

    for (final ComponentInstance opId : this.scenario.getSimulationManager().getSpecialVertexOperators()) {
      addSpecialVertexOperator(sVOperators, opId);
    }

    final Element nbExec = this.dom.createElement("numberOfTopExecutions");
    params.appendChild(nbExec);
    nbExec.setTextContent(String.valueOf(this.scenario.getSimulationManager().getNumberOfTopExecutions()));
  }

  /**
   * Adds the data type.
   *
   * @param parent
   *          the parent
   * @param dataType
   *          the data type
   */
  private void addDataType(final Element parent, final DataType dataType) {

    final Element dataTypeElt = this.dom.createElement("dataType");
    parent.appendChild(dataTypeElt);
    dataTypeElt.setAttribute("name", dataType.getTypeName());
    dataTypeElt.setAttribute("size", Long.toString(dataType.getSize()));
  }

  /**
   * Adds the special vertex operator.
   *
   * @param parent
   *          the parent
   * @param opId
   *          the op id
   */
  private void addSpecialVertexOperator(final Element parent, final ComponentInstance opId) {

    final Element dataTypeElt = this.dom.createElement("specialVertexOperator");
    parent.appendChild(dataTypeElt);
    dataTypeElt.setAttribute("path", opId.getInstanceName());
  }

  /**
   * Adds the files.
   *
   * @param parent
   *          the parent
   */
  private void addFiles(final Element parent) {

    final Element files = this.dom.createElement("files");
    parent.appendChild(files);

    final PiGraph algorithm = this.scenario.getAlgorithm();
    if (algorithm != null) {
      final Element algo = this.dom.createElement("algorithm");
      files.appendChild(algo);
      algo.setAttribute("url", algorithm.getUrl());
    }

    final Design design = this.scenario.getDesign();
    if (design != null) {
      final Element archi = this.dom.createElement("architecture");
      files.appendChild(archi);
      archi.setAttribute("url", design.getUrl());
    }

    final Element codeGenDir = this.dom.createElement("codegenDirectory");
    files.appendChild(codeGenDir);
    codeGenDir.setAttribute("url", this.scenario.getCodegenDirectory());

  }

  /**
   * Adds the constraints.
   *
   * @param parent
   *          the parent
   */
  private void addConstraints(final Element parent) {

    final Element constraints = this.dom.createElement("constraints");
    parent.appendChild(constraints);

    constraints.setAttribute("excelUrl", this.scenario.getConstraintGroupManager().getExcelFileURL());

    for (final ConstraintGroup cst : this.scenario.getConstraintGroupManager().getConstraintGroups()) {
      addConstraint(constraints, cst);
    }
  }

  /**
   * Adds the constraint.
   *
   * @param parent
   *          the parent
   * @param cst
   *          the cst
   */
  private void addConstraint(final Element parent, final ConstraintGroup cst) {

    final Element constraintGroupElt = this.dom.createElement("constraintGroup");
    parent.appendChild(constraintGroupElt);

    final String opId = cst.getOperatorId();
    final Element opdefelt = this.dom.createElement("operator");
    constraintGroupElt.appendChild(opdefelt);
    opdefelt.setAttribute("name", opId);

    for (final AbstractActor vtxId : cst.getVertexPaths()) {
      final Element vtxelt = this.dom.createElement(VertexType.TYPE_TASK);
      constraintGroupElt.appendChild(vtxelt);
      vtxelt.setAttribute("name", vtxId.getVertexPath());
    }
  }

  /**
   * Adds the relative constraints.
   *
   * @param parent
   *          the parent
   */
  private void addRelativeConstraints(final Element parent) {

    final RelativeConstraintManager manager = this.scenario.getRelativeconstraintManager();
    final Element timings = this.dom.createElement("relativeconstraints");
    parent.appendChild(timings);

    timings.setAttribute("excelUrl", manager.getExcelFileURL());

    for (final String id : manager.getExplicitConstraintIds()) {
      addRelativeConstraint(timings, id, manager.getConstraintOrDefault(id));
    }
  }

  /**
   * Adds the relative constraint.
   *
   * @param parent
   *          the parent
   * @param id
   *          the id
   * @param group
   *          the group
   */
  private void addRelativeConstraint(final Element parent, final String id, final int group) {

    final Element timingelt = this.dom.createElement("relativeconstraint");
    parent.appendChild(timingelt);
    timingelt.setAttribute("vertexname", id);
    timingelt.setAttribute("group", Integer.toString(group));
  }

  /**
   * Adds the timings.
   *
   * @param parent
   *          the parent
   */
  private void addTimings(final Element parent) {

    final Element timings = this.dom.createElement("timings");
    parent.appendChild(timings);

    timings.setAttribute("excelUrl", this.scenario.getTimingManager().getExcelFileURL());

    for (final Timing timing : this.scenario.getTimingManager().getTimings()) {
      addTiming(timings, timing);
    }

    for (final String opDef : this.scenario.getTimingManager().getMemcpySpeeds().keySet()) {
      addMemcpySpeed(timings, opDef, this.scenario.getTimingManager().getMemcpySetupTime(opDef),
          this.scenario.getTimingManager().getMemcpyTimePerUnit(opDef));
    }
  }

  /**
   * Adds the timing.
   *
   * @param parent
   *          the parent
   * @param timing
   *          the timing
   */
  private void addTiming(final Element parent, final Timing timing) {

    final Element timingelt = this.dom.createElement("timing");
    parent.appendChild(timingelt);
    timingelt.setAttribute("vertexname", timing.getVertexId());
    timingelt.setAttribute("opname", timing.getOperatorDefinitionId());
    String timeString;
    if (timing.isEvaluated()) {
      timeString = Long.toString(timing.getTime());
    } else {
      timeString = timing.getStringValue();
    }
    timingelt.setAttribute("time", timeString);
  }

  /**
   * Adds the memcpy speed.
   *
   * @param parent
   *          the parent
   * @param opDef
   *          the op def
   * @param memcpySetupTime
   *          the memcpy setup time
   * @param memcpyTimePerUnit
   *          the memcpy time per unit
   */
  private void addMemcpySpeed(final Element parent, final String opDef, final long memcpySetupTime,
      final float memcpyTimePerUnit) {

    final Element timingelt = this.dom.createElement("memcpyspeed");
    parent.appendChild(timingelt);
    timingelt.setAttribute("opname", opDef);
    timingelt.setAttribute("setuptime", Long.toString(memcpySetupTime));
    timingelt.setAttribute("timeperunit", Float.toString(memcpyTimePerUnit));
  }
}
