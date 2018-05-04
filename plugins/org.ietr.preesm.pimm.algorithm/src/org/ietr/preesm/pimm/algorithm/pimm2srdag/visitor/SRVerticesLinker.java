/**
 *
 */
package org.ietr.preesm.pimm.algorithm.pimm2srdag.visitor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.ietr.dftools.algorithm.model.AbstractEdge;
import org.ietr.dftools.algorithm.model.dag.DAGEdge;
import org.ietr.dftools.algorithm.model.dag.DAGVertex;
import org.ietr.dftools.algorithm.model.dag.edag.DAGBroadcastVertex;
import org.ietr.dftools.algorithm.model.dag.edag.DAGEndVertex;
import org.ietr.dftools.algorithm.model.dag.edag.DAGForkVertex;
import org.ietr.dftools.algorithm.model.dag.edag.DAGInitVertex;
import org.ietr.dftools.algorithm.model.dag.edag.DAGJoinVertex;
import org.ietr.dftools.algorithm.model.dag.types.DAGDefaultEdgePropertyType;
import org.ietr.dftools.algorithm.model.dag.types.DAGDefaultVertexPropertyType;
import org.ietr.dftools.algorithm.model.sdf.SDFEdge;
import org.ietr.dftools.algorithm.model.sdf.types.SDFStringEdgePropertyType;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.experiment.model.pimm.AbstractActor;
import org.ietr.preesm.experiment.model.pimm.AbstractVertex;
import org.ietr.preesm.experiment.model.pimm.DataInputInterface;
import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.DataOutputInterface;
import org.ietr.preesm.experiment.model.pimm.DataOutputPort;
import org.ietr.preesm.experiment.model.pimm.DataPort;
import org.ietr.preesm.experiment.model.pimm.Delay;
import org.ietr.preesm.experiment.model.pimm.DelayActor;
import org.ietr.preesm.experiment.model.pimm.Expression;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.InterfaceActor;
import org.ietr.preesm.mapper.model.MapperDAG;
import org.ietr.preesm.mapper.model.MapperDAGVertex;
import org.ietr.preesm.mapper.model.MapperVertexFactory;
import org.ietr.preesm.pimm.algorithm.helper.PiMMHelperException;

/**
 *
 * Link SR DAG vertices
 *
 * @author farresti
 *
 */
public class SRVerticesLinker {

  /** Property name for property JOIN_VERTEX. */
  public static final String JOIN_VERTEX = "implode_";

  /** Property name for property FORK_VERTEX. */
  public static final String FORK_VERTEX = "explode_";

  // Number of delays
  private final long delays;

  // Source actor
  private final AbstractActor source;
  // Source Port
  private DataOutputPort sourcePort;

  // Sink Actor
  private final AbstractActor sink;
  // Sink Port
  private DataInputPort sinkPort;

  // The fifo
  private final Fifo   fifo;
  private final String fifoName;

  // The fifo data size
  private final int dataSize;

  // The DAG in which we operate
  private final MapperDAG dag;

  // Port modifiers annotations
  private final String sourceModifier;
  private final String targetModifier;

  // Delay init / end id
  String delayInitID;
  String delayEndID;

  /**
   * Retrieve the number of delay tokens contain in a fifo, if any
   *
   * @param fifo
   *          the fifo
   * @return number of delay, 0 if the fifo does not contain any delay
   */
  private static long getNDelays(final Fifo fifo) {
    final Delay delay = fifo.getDelay();
    if (delay == null) {
      return 0;
    }
    // Get the number of delay
    final Expression sizeExpression = fifo.getDelay().getSizeExpression();
    final long nDelays = Long.parseLong(sizeExpression.getExpressionString());
    // Sanity check on delay value
    final DataInputPort targetPort = fifo.getTargetPort();
    final Expression portRateExpression = targetPort.getPortRateExpression();
    final long targetRate = Long.parseLong(portRateExpression.getExpressionString());
    if (nDelays < 0) {
      throw new RuntimeException("Invalid number of delay on fifo[" + fifo.getId() + "]: " + Long.toString(nDelays));
    } else if (nDelays < targetRate) {
      throw new RuntimeException("Insuffisiant number of delay on fifo[" + fifo.getId() + "]: number of delays: " + Long.toString(nDelays) + ", consumption: "
          + Long.toString(targetRate));
    }
    return nDelays;
  }

  /**
   * Get the repetition value for the sink / source actor
   *
   * @param actor
   *          the sink actor
   * @param brv
   *          the basic repetition vector map
   * @return repetition value of the actor, 1 if it is an interface actor
   */
  private static long getRVorDefault(final AbstractActor actor, final Map<AbstractVertex, Long> brv) {
    if (actor instanceof InterfaceActor) {
      return 1;
    }
    return (brv.get(actor));
  }

  /**
   * Constructor for the SR linker
   *
   * @param fifo
   *          the fifo to link
   * @param dag
   *          the dag in which we operate
   * @param scenario
   *          the scenario. Used to retrieve data size of the fifo
   */
  public SRVerticesLinker(final Fifo fifo, final MapperDAG dag, final PreesmScenario scenario) {
    this.fifo = fifo;
    this.delays = SRVerticesLinker.getNDelays(fifo);
    this.source = fifo.getSourcePort().getContainingActor();
    this.sink = fifo.getTargetPort().getContainingActor();
    this.sourcePort = fifo.getSourcePort();
    this.sinkPort = fifo.getTargetPort();
    final String type = fifo.getType();
    this.dataSize = scenario.getSimulationManager().getDataTypeSizeOrDefault(type);
    this.dag = dag;
    this.sourceModifier = getAnnotationFromPort(this.sourcePort);
    this.targetModifier = getAnnotationFromPort(this.sinkPort);
    this.fifoName = this.fifo.getId().replace(".", "_").replace("-", "_");
  }

  /**
   * Convert annotations from to.
   *
   * @param piPort
   *          the pi port
   * @param edge
   *          the edge
   * @param property
   *          the property
   */
  private String getAnnotationFromPort(final DataPort piPort) {
    switch (piPort.getAnnotation()) {
      case READ_ONLY:
        return SDFEdge.MODIFIER_READ_ONLY;
      case WRITE_ONLY:
        return SDFEdge.MODIFIER_WRITE_ONLY;
      case UNUSED:
        return SDFEdge.MODIFIER_UNUSED;
      default:
        return "";
    }
  }

  /**
   * Do the linkage between the dag vertices
   *
   * @param brv
   *          the basic repetition vector map
   *
   * @param pimm2dag
   *          the map between the pimm vertex and the dag vertex
   *
   *
   * @return true if no error, false else
   */
  public Boolean execute(final Map<AbstractVertex, Long> brv, final Map<AbstractVertex, ArrayList<MapperDAGVertex>> pimm2dag) throws PiMMHelperException {
    // These connections are already dealt with
    if ((this.source instanceof DelayActor) || (this.sink instanceof DelayActor)) {
      return true;
    }

    // Initialize delay init / end IDs
    this.delayInitID = "";
    this.delayEndID = "";

    // List of source vertex
    final ArrayList<Pair<DAGVertex, Long>> sourceSet = getSourceSet(brv, pimm2dag);
    // List of sink vertex
    final ArrayList<Pair<DAGVertex, Long>> sinkSet = getSinkSet(brv, pimm2dag);

    // Connect all the source to the sinks
    connectEdges(sourceSet, sinkSet);
    return true;
  }

  /**
   * Connect the sources to the sinks
   *
   * @param sourceSet
   *          set of dag sources
   * @param sinkSet
   *          set of dag sinks
   */
  private void connectEdges(final ArrayList<Pair<DAGVertex, Long>> sourceSet, final ArrayList<Pair<DAGVertex, Long>> sinkSet) {
    while (!sinkSet.isEmpty()) {
      if (connectSources2Sink(sourceSet, sinkSet)) {
        sinkSet.remove(0);
      }
      if (sourceSet.isEmpty() || sinkSet.isEmpty()) {
        break;
      }
      if (connectSinks2Source(sinkSet, sourceSet)) {
        sourceSet.remove(0);
      }
    }
  }

  /**
   * Test if a vertex is a roundbuffer
   *
   * @param vertex
   *          the vertex to test
   * @return true if the vertex is a roundbuffer, false else
   */
  private boolean isRoundBuffer(final DAGVertex vertex) {
    if (!vertex.getKind().equals(DAGBroadcastVertex.DAG_BROADCAST_VERTEX)) {
      return false;
    }
    final String value = (String) vertex.getPropertyBean().getValue(DAGBroadcastVertex.SPECIAL_TYPE);
    return value.equals(DAGBroadcastVertex.SPECIAL_TYPE_ROUNDBUFFER);
  }

  /**
   * Connect sources to current sink
   *
   * @param sourceSet
   *          set of dag sources
   * @param sink
   *          current sink to connect to
   * @return true if it didn't explode, false else
   */
  private boolean connectSources2Sink(final ArrayList<Pair<DAGVertex, Long>> sourceSet, final ArrayList<Pair<DAGVertex, Long>> sinkSet) {
    final Pair<DAGVertex, Long> sinkPair = sinkSet.get(0);
    DAGVertex sinkVertex = sinkPair.getLeft();
    long cons = sinkPair.getRight();
    long prod = sourceSet.get(0).getRight();
    // Check implode condition
    final boolean implode = (cons > prod);
    // Check if sink is a join
    final boolean isSinkJoinVertex = sinkVertex.getKind().equals(DAGJoinVertex.DAG_JOIN_VERTEX);
    // Check if sink is a roundbuffer
    final boolean isSinkRoundBufferVertex = isRoundBuffer(sinkVertex);
    // Test if we need to add an implode vertex
    if (implode && !isSinkJoinVertex && !isSinkRoundBufferVertex) {
      final String implodeName = SRVerticesLinker.JOIN_VERTEX + sinkVertex.getName() + "_" + this.sinkPort.getName();
      final DAGVertex implodeVertex = createJoinVertex(implodeName, MapperVertexFactory.getInstance());
      final DAGEdge edge = createEdge(implodeVertex, sinkVertex, Long.toString(cons));
      // Add a source port modifier
      edge.setSourcePortModifier(new SDFStringEdgePropertyType(SDFEdge.MODIFIER_WRITE_ONLY));
      sinkVertex = implodeVertex;
    }
    // Array of sources to remove
    final ArrayList<Pair<DAGVertex, Long>> toRemove = new ArrayList<>();
    // Connect the edges
    for (final Pair<DAGVertex, Long> src : sourceSet) {
      prod = src.getRight();
      if ((cons == 0) || (prod > cons)) {
        break;
      }
      toRemove.add(src);
      final DAGVertex sourceVertex = src.getLeft();
      final DAGEdge edge = createEdge(sourceVertex, sinkVertex, Long.toString(prod));

      // If the target is join (new or not) /roundbuffer with new ports
      final boolean isJoinOrRoundBuffer = (sinkVertex != sinkPair.getLeft()) || (implode && isSinkRoundBufferVertex && !isSinkJoinVertex);
      if (isJoinOrRoundBuffer) {
        // update name and source port modifier
        edge.setTargetLabel(edge.getTargetLabel() + "_" + Long.toString(cons));
        edge.setTargetPortModifier(new SDFStringEdgePropertyType(SDFEdge.MODIFIER_READ_ONLY));
      }
      cons = cons - prod;
    }
    // Remove all sources that got connected
    toRemove.forEach(sourceSet::remove);
    // Reset the current top sink
    sinkSet.set(0, Pair.of(sinkVertex, cons));
    // Removing the sink for the set condition
    final boolean shouldRemoveSink = (prod > cons) && (cons != 0);
    return !shouldRemoveSink;
  }

  /**
   * Connect sinks to current source
   *
   * @param sinkSet
   *          set of dag sinks
   * @param source
   *          current source to connect from
   * @return true if it didn't implode, false else
   */
  private boolean connectSinks2Source(final ArrayList<Pair<DAGVertex, Long>> sinkSet, final ArrayList<Pair<DAGVertex, Long>> sourceSet) {
    final Pair<DAGVertex, Long> sourcePair = sourceSet.get(0);
    DAGVertex sourceVertex = sourcePair.getLeft();
    long prod = sourcePair.getRight();
    long cons = sinkSet.get(0).getRight();
    // Check explode condition
    final boolean explode = prod > cons;
    // Check if source is a fork
    final boolean isSourceForkVertex = sourceVertex.getKind().equals(DAGForkVertex.DAG_FORK_VERTEX);
    // Check if source is a roundbuffer
    final boolean isSourceRoundBufferVertex = isRoundBuffer(sourceVertex);
    // Check if source is a broadcast
    final boolean isSourceBroadcastVertex = !isSourceRoundBufferVertex && sourceVertex.getKind().equals(DAGBroadcastVertex.DAG_BROADCAST_VERTEX);
    // Test if we need to add an explode vertex
    if (explode && !isSourceForkVertex && (!isSourceBroadcastVertex || isSourceRoundBufferVertex)) {
      // If we must, we add an explode vertex
      final String explodeName = SRVerticesLinker.FORK_VERTEX + sourceVertex.getName() + "_" + this.sourcePort.getName();
      final DAGVertex explodeVertex = createForkVertex(explodeName, MapperVertexFactory.getInstance());
      final DAGEdge edge = createEdge(sourceVertex, explodeVertex, Long.toString(prod));
      // Add a target port modifier
      edge.setTargetPortModifier(new SDFStringEdgePropertyType(SDFEdge.MODIFIER_READ_ONLY));
      sourceVertex = explodeVertex;
    }
    // Array of sink to remove
    final ArrayList<Pair<DAGVertex, Long>> toRemove = new ArrayList<>();
    // Connect the edges
    for (final Pair<DAGVertex, Long> snk : sinkSet) {
      cons = snk.getRight();
      if ((prod == 0) || (cons > prod)) {
        break;
      }
      toRemove.add(snk);
      final DAGVertex sinkVertex = snk.getLeft();
      final DAGEdge edge = createEdge(sourceVertex, sinkVertex, Long.toString(cons));
      // If the source is a fork (new or not)
      // or a broadcast with a new port
      final boolean isForkOrBroadcast = !((sourceVertex == sourcePair.getLeft()) && (!explode || !(isSourceBroadcastVertex || isSourceForkVertex)));
      if (isForkOrBroadcast) {
        // update name and source port modifier
        edge.setSourceLabel(edge.getSourceLabel() + "_" + Long.toString(prod));
        // Add a source port modifier
        edge.setSourcePortModifier(new SDFStringEdgePropertyType(SDFEdge.MODIFIER_WRITE_ONLY));
      }
      prod = prod - cons;
    }
    // Remove all sinks that got connected
    toRemove.forEach(sinkSet::remove);
    // Reset the current top source
    sourceSet.set(0, Pair.of(sourceVertex, prod));
    // Removing the source for the set condition
    final boolean shouldRemoveSink = (cons > prod) && (prod != 0);
    return !shouldRemoveSink;
  }

  /**
   * Adds a new pair to a set
   *
   * @param set
   *          the set
   * @param vertex
   *          left element of the pair to add
   * @param value
   *          right element of the pair to add
   */
  private void addPair(final ArrayList<Pair<DAGVertex, Long>> set, final DAGVertex vertex, final long value) {
    set.add(Pair.of(vertex, value));
  }

  /**
   * Generate DAG set of sources
   *
   * @param brv
   *          repetition vector values
   * @param pimm2dag
   *          map of PiMM actors and their associated list of dag vertices
   * @return set of dag sources
   * @throws PiMMHelperException
   *           the exception
   */
  private ArrayList<Pair<DAGVertex, Long>> getSourceSet(final Map<AbstractVertex, Long> brv, final Map<AbstractVertex, ArrayList<MapperDAGVertex>> pimm2dag)
      throws PiMMHelperException {
    final ArrayList<Pair<DAGVertex, Long>> sourceSet = new ArrayList<>();

    final MapperVertexFactory vertexFactory = MapperVertexFactory.getInstance();
    final String fifoID = this.fifoName;
    // Port expressions
    final Expression sourceExpression = this.sourcePort.getPortRateExpression();
    final long sourceProduction = Long.parseLong(sourceExpression.getExpressionString());

    // Deals with the delay
    if (this.delays != 0) {
      final Delay delay = this.fifo.getDelay();
      if (delay.hasSetterActor()) {
        final AbstractActor setterActor = this.fifo.getDelay().getSetterActor();
        final ArrayList<MapperDAGVertex> setterActorList = pimm2dag.get(setterActor);
        sourceSet.add(addDelaySetterActor(setterActorList));
      } else {
        // Add an init vertex for the first iteration of the sink actor
        final MapperDAGVertex firstSink = pimm2dag.get(this.sink).get(0);
        this.delayInitID = firstSink.getName() + "_init_" + this.sinkPort.getName();
        final DAGVertex initVertex = createInitVertex(this.delayInitID, vertexFactory);
        addPair(sourceSet, initVertex, this.delays);
      }
    }

    if (this.source instanceof InterfaceActor) {
      // Port expressions
      final Expression sinkExpression = this.sinkPort.getPortRateExpression();
      final long sinkConsumption = Long.parseLong(sinkExpression.getExpressionString());
      // Retrieve corresponding source vertex
      final DAGVertex vertex = pimm2dag.get(this.source).get(0);
      final DAGVertex sourceVertex = getInterfaceSourceVertex(vertex);
      // Repetition values
      final long sinkRV = SRVerticesLinker.getRVorDefault(this.sink, brv);
      if (sourceProduction == (sinkConsumption * sinkRV)) {
        // We don't need to use broadcast
        addPair(sourceSet, sourceVertex, sourceProduction);
      } else {
        final boolean perfectBroadcast = ((sinkConsumption * sinkRV) % sourceProduction) == 0;
        long nBroadcast = (sinkConsumption * sinkRV) / sourceProduction;
        if (!perfectBroadcast) {
          nBroadcast++;
        }
        final DAGVertex broadcastVertex = vertexFactory.createVertex(DAGBroadcastVertex.DAG_BROADCAST_VERTEX);
        broadcastVertex.getPropertyBean().setValue(DAGBroadcastVertex.SPECIAL_TYPE, DAGBroadcastVertex.SPECIAL_TYPE_BROADCAST);
        setVertexDefault(broadcastVertex, "broadcast_" + fifoID);
        this.dag.addVertex(broadcastVertex);
        final DAGEdge edge = createEdge(sourceVertex, broadcastVertex, Long.toString(sourceProduction));
        edge.setTargetLabel("br_" + edge.getTargetLabel());
        // This is bit of a hack here.
        // The problem is that we can not distinguish the number of connection that need to come from the broadcast or a possible INIT.
        // I generate has many broadcast vertex has need but since there are all the same, all connections will be set to the proper unique broadcast vertex.
        for (int i = 0; i < nBroadcast; ++i) {
          addPair(sourceSet, broadcastVertex, sourceProduction);
        }
      }
    } else if (this.source instanceof AbstractActor) {
      // Add the list of the SR-DAG vertex associated with the source
      pimm2dag.get(this.source).forEach(v -> addPair(sourceSet, v, sourceProduction));
    } else {
      throw new PiMMHelperException("Unhandled type of actor: " + this.source.getClass().toString());
    }
    return sourceSet;
  }

  /**
   * Retrieve the source vertex corresponding to current data input interface. <br>
   * The corresponding edge is removed.
   *
   * @param vertex
   *          the vertex
   * @return the corresponding source vertex
   * @throws PiMMHelperException
   *           the exception
   */
  private DAGVertex getInterfaceSourceVertex(final DAGVertex vertex) throws PiMMHelperException {
    final ArrayList<DataPort> dataInputPorts = new ArrayList<>(this.source.getContainingPiGraph().getDataInputPorts());
    final DataInputPort correspondingPort = (DataInputPort) getCorrespondingPort(dataInputPorts, this.source.getName());
    final Set<DAGEdge> incomingEdges = vertex.incomingEdges();
    final String targetLabel = this.source.getName();
    DAGEdge interfaceEdge = null;
    for (final DAGEdge edge : incomingEdges) {
      final DAGEdge actualEdge = (DAGEdge) edge.getAggregate().get(0);
      if (actualEdge.getTargetLabel().equals(targetLabel)) {
        interfaceEdge = edge;
        break;
      }
    }
    final Fifo correspondingFifo = correspondingPort.getFifo();
    if (interfaceEdge == null) {
      final String message = "Edge corresponding to fifo [" + correspondingFifo.getId() + "] not found.";
      throw new PiMMHelperException(message);
    }
    this.sourcePort = getOriginalSource(correspondingPort);
    return interfaceEdge.getSource();
  }

  /**
   * Retrieve the original source port of an interface, even in deep hierarchy.
   *
   * @param sourceInterface
   *          the current source interface
   * @return original source port
   * @throws PiMMHelperException
   *           the PiMMHelperException exception
   */
  private DataOutputPort getOriginalSource(final DataInputPort sourcePort) throws PiMMHelperException {
    final Fifo inFifo = sourcePort.getFifo();
    final DataOutputPort origSource = inFifo.getSourcePort();
    final AbstractActor containingActor = origSource.getContainingActor();
    if (containingActor instanceof DataInputInterface) {
      final ArrayList<DataPort> dataInputPorts = new ArrayList<>(containingActor.getContainingPiGraph().getDataInputPorts());
      final DataInputPort correspondingPort = (DataInputPort) getCorrespondingPort(dataInputPorts, containingActor.getName());
      return getOriginalSource(correspondingPort);
    }
    return origSource;
  }

  /**
   * Deals with the setter actor of a delay. <br>
   * If the RV of the setter actor is greater than 1, then a join actor is created to connect all of its instances. <br>
   *
   * @param setterActorList
   *          list of sr dag instances of the setter actor
   * @return setter actor if its RV = 1, the join actor created else
   */
  private Pair<DAGVertex, Long> addDelaySetterActor(final ArrayList<MapperDAGVertex> setterActorList) {
    final MapperVertexFactory vertexFactory = MapperVertexFactory.getInstance();
    final Delay delay = this.fifo.getDelay();
    final int setterRV = setterActorList.size();
    if (setterRV > 1) {
      // Get the delay setter port
      final DataOutputPort setterPort = delay.getSetterPort();
      // Add a join vertex for the first iteration of the sink actor
      // Connect all instances of the setter actor to the join actor
      final DAGVertex joinVertex = createJoinVertex(SRVerticesLinker.JOIN_VERTEX + this.fifoName, vertexFactory);
      Long currentCons = this.delays;
      for (int i = 0; i < setterRV; ++i) {
        final DAGVertex currentSetterActor = setterActorList.get(i);
        final Expression portRateExpression = setterPort.getPortRateExpression();
        final String rateExpression = portRateExpression.getExpressionString();
        final DAGEdge edge = createEdge(currentSetterActor, joinVertex, rateExpression);
        // update name and target port modifier
        final Long rate = Long.parseLong(rateExpression);
        currentCons = currentCons - rate;
        edge.setTargetLabel(edge.getSourceLabel() + "_" + Long.toString(currentCons));
        edge.setSourceLabel(setterPort.getName());
        edge.setTargetPortModifier(new SDFStringEdgePropertyType(SDFEdge.MODIFIER_READ_ONLY));
      }
      return Pair.of(joinVertex, this.delays);
    } else {
      return Pair.of(setterActorList.get(0), this.delays);
    }
  }

  /**
   * Generate DAG set of sinks
   *
   * @param brv
   *          repetition vector values
   * @param pimm2dag
   *          map of PiMM actors and their associated list of dag vertices
   * @return set of dag sink
   * @throws PiMMHelperException
   *           the exception
   */
  private ArrayList<Pair<DAGVertex, Long>> getSinkSet(final Map<AbstractVertex, Long> brv, final Map<AbstractVertex, ArrayList<MapperDAGVertex>> pimm2dag)
      throws PiMMHelperException {
    final ArrayList<Pair<DAGVertex, Long>> sinkSet = new ArrayList<>();

    final MapperVertexFactory vertexFactory = MapperVertexFactory.getInstance();

    // Port expressions
    final Expression sinkExpression = this.sinkPort.getPortRateExpression();
    final long sinkConsumption = Long.parseLong(sinkExpression.getExpressionString());
    final Expression sourceExpression = this.sourcePort.getPortRateExpression();
    final long sourceProduction = Long.parseLong(sourceExpression.getExpressionString());

    if (this.sink instanceof InterfaceActor) {
      // Repetition values
      final long sourceRV = SRVerticesLinker.getRVorDefault(this.source, brv);
      // Retrieve corresponding sink vertex
      final DAGVertex vertex = pimm2dag.get(this.sink).get(0);
      final DAGVertex sinkVertex = getInterfaceSinkVertex(vertex);
      if (sinkConsumption == (sourceProduction * sourceRV)) {
        sinkSet.add(Pair.of(sinkVertex, sinkConsumption));
      } else {
        // We need to add a round buffer
        // final long nDroppedTokens = (sourceProduction * sourceRV) - sinkConsumption;
        // long nEnd = (long) Math.ceil((double) nDroppedTokens / sourceProduction);
        // final String fixID = "rb_" + fifoID + "_";
        // for (long i = 0; i < (nEnd - 1); ++i) {
        // final DAGVertex endVertex = createEndVertex(fixID + Long.toString(i), vertexFactory);
        // addPair(sinkSet, endVertex, sourceProduction);
        // }
        // final DAGVertex endVertex = createEndVertex(fixID + Long.toString(nEnd - 1), vertexFactory);
        // addPair(sinkSet, endVertex, nDroppedTokens - (nEnd - 1) * sourceProduction);
        // addPair(sinkSet, sinkVertex, sinkConsumption);
        final DAGVertex roundbufferVertex = createRoundBufferVertex(sinkConsumption, sinkVertex, vertexFactory);
        addPair(sinkSet, roundbufferVertex, sourceProduction * sourceRV);
      }
    } else if (this.sink instanceof AbstractActor) {
      // Add the list of the SR-DAG vertex associated with the sink
      pimm2dag.get(this.sink).forEach(v -> addPair(sinkSet, v, sinkConsumption));

      // This is only true in the case of an interface
      final long sinkRV = SRVerticesLinker.getRVorDefault(this.sink, brv);
      final long leftOver = (sinkConsumption * sinkRV) % sourceProduction;
      final boolean sinkNeedEnd = leftOver != 0;
      if (sinkNeedEnd) {
        // Add an end vertex for the round buffer of the interface
        final DAGVertex endVertex = createEndVertex(sinkSet.get(sinkSet.size() - 1) + "_end_" + this.sinkPort.getName(), vertexFactory);
        addPair(sinkSet, endVertex, sourceProduction - leftOver);
      }
    } else {
      throw new PiMMHelperException("Unhandled type of actor: " + this.sink.getClass().toString());
    }

    // Deals with the delay
    if (this.delays != 0) {
      final Delay delay = this.fifo.getDelay();
      if (delay.hasGetterActor()) {
        final AbstractActor getterActor = this.fifo.getDelay().getGetterActor();
        final ArrayList<MapperDAGVertex> getterActorList = pimm2dag.get(getterActor);
        sinkSet.add(addDelayGetterActor(getterActorList));
      } else {
        // Add an end vertex for the last iteration of the source actor
        final MapperDAGVertex lastSource = pimm2dag.get(this.source).get(pimm2dag.get(this.source).size() - 1);
        this.delayEndID = lastSource.getName() + "_end_" + this.sourcePort.getName();
        final DAGVertex endVertex = createEndVertex(this.delayEndID, vertexFactory);
        setEndReference(endVertex);
        addPair(sinkSet, endVertex, this.delays);
      }
    }
    return sinkSet;
  }

  /**
   * Retrieve the source vertex corresponding to current data input interface. <br>
   * The corresponding edge is removed.
   *
   * @param vertex
   *          the vertex
   * @return the corresponding sink vertex
   * @throws PiMMHelperException
   *           the exception
   */
  private DAGVertex getInterfaceSinkVertex(final DAGVertex vertex) throws PiMMHelperException {
    final ArrayList<DataPort> dataOutputPorts = new ArrayList<>(this.sink.getContainingPiGraph().getDataOutputPorts());
    final DataOutputPort correspondingPort = (DataOutputPort) getCorrespondingPort(dataOutputPorts, this.sink.getName());
    final Set<DAGEdge> outgoingEdges = vertex.outgoingEdges();
    final String sourceLabel = this.sink.getName();
    DAGEdge interfaceEdge = null;
    for (final DAGEdge edge : outgoingEdges) {
      final DAGEdge actualEdge = (DAGEdge) edge.getAggregate().get(0);
      if (actualEdge.getSourceLabel().equals(sourceLabel)) {
        interfaceEdge = edge;
      }
    }
    final Fifo correspondingFifo = correspondingPort.getFifo();
    if (interfaceEdge == null) {
      final String message = "Edge corresponding to fifo [" + correspondingFifo.getId() + "] not found.";
      throw new PiMMHelperException(message);
    }
    // this.sinkPort = correspondingFifo.getTargetPort();
    this.sinkPort = getOriginalSink(correspondingPort);
    return interfaceEdge.getTarget();
  }

  /**
   * Retrieve the original source port of an interface, even in deep hierarchy.
   *
   * @param sourceInterface
   *          the current source interface
   * @return original source port
   * @throws PiMMHelperException
   *           the PiMMHelperException exception
   */
  private DataInputPort getOriginalSink(final DataOutputPort sinkPort) throws PiMMHelperException {
    final Fifo inFifo = sinkPort.getFifo();
    final DataInputPort origSink = inFifo.getTargetPort();
    final AbstractActor containingActor = origSink.getContainingActor();
    if (containingActor instanceof DataOutputInterface) {
      final ArrayList<DataPort> dataOutputPorts = new ArrayList<>(containingActor.getContainingPiGraph().getDataOutputPorts());
      final DataOutputPort correspondingPort = (DataOutputPort) getCorrespondingPort(dataOutputPorts, containingActor.getName());
      return getOriginalSink(correspondingPort);
    }
    return origSink;
  }

  /**
   * Retrieve the port matching portName in a portList.<br>
   * If the port is not found, it throws an exception
   *
   * @param portList
   *          the list of port in which to look for
   * @param portName
   *          the name of the search port
   * @return the corresponding port
   * @throws PiMMHelperException
   *           the PiMMHelperException exception
   */
  private DataPort getCorrespondingPort(final ArrayList<DataPort> portList, final String portName) throws PiMMHelperException {
    DataPort correspondingPort = null;
    for (final DataPort port : portList) {
      if (port.getName().equals(portName)) {
        correspondingPort = port;
        break;
      }
    }
    if (correspondingPort == null) {
      final String message = "Data output port corresponding to interface [" + portName + "] not found.";
      throw new PiMMHelperException(message);
    }
    return correspondingPort;
  }

  /**
   * Deals with the getter actor of a delay. <br>
   * If the RV of the getter actor is greater than 1, then a fork actor is created to connect all of its instances. <br>
   *
   * @param getterActorList
   *          list of sr dag instances of the getter actor
   * @return getter actor if its RV = 1, the fork actor created else
   */
  private Pair<DAGVertex, Long> addDelayGetterActor(final ArrayList<MapperDAGVertex> getterActorList) {
    final MapperVertexFactory vertexFactory = MapperVertexFactory.getInstance();
    final Delay delay = this.fifo.getDelay();
    final int getterRV = getterActorList.size();
    if (getterRV > 1) {
      // Add a fork vertex for the last iteration of the source actor
      // Connect all instances of the getter actor to the fork actor
      final DAGVertex forkVertex = createForkVertex(SRVerticesLinker.FORK_VERTEX + this.fifoName, vertexFactory);
      Long currentProd = this.delays;
      for (int i = 0; i < getterRV; ++i) {
        final DAGVertex currentGetterActor = getterActorList.get(i);
        final Expression portRateExpression = delay.getGetterPort().getPortRateExpression();
        final String rateExpression = portRateExpression.getExpressionString();
        final DAGEdge edge = createEdge(forkVertex, currentGetterActor, rateExpression);
        // update name and source port modifier
        final Long rate = Long.parseLong(rateExpression);
        currentProd = currentProd - rate;
        edge.setSourceLabel(edge.getTargetLabel() + "_" + Long.toString(currentProd));
        edge.setTargetLabel(delay.getGetterPort().getName());
        edge.setSourcePortModifier(new SDFStringEdgePropertyType(SDFEdge.MODIFIER_WRITE_ONLY));
      }
      return Pair.of(forkVertex, this.delays);
    } else {
      return Pair.of(getterActorList.get(0), this.delays);
    }
  }

  /**
   * Creates a MapperDAGEdge and sets initial properties. <br>
   * The created edge is added to the MapperDAG.
   *
   *
   * @param source
   *          source vertex of the edge
   * @param target
   *          target vertex of the edge
   * @param rateExpression
   *          rate expression of the edge
   * @return the created edge
   */
  private DAGEdge createEdge(final DAGVertex source, final DAGVertex target, final String rateExpression) {
    // final DAGEdge edge;
    // final int weight = this.dataSize * Integer.parseInt(rateExpression);
    // if (this.dag.containsEdge(source, target)) {
    // edge = this.dag.getEdge(source, target);
    // edge.setWeight(new DAGDefaultEdgePropertyType(weight + edge.getWeight().intValue()));
    // } else {
    // edge = this.dag.addEdge(source, target);
    // edge.setWeight(new DAGDefaultEdgePropertyType(weight));
    // }

    final int weight = Integer.parseInt(rateExpression);
    final DAGEdge edge = this.dag.addEdge(source, target);

    final DAGEdge newEdge = new DAGEdge();
    if (!this.sourceModifier.isEmpty()) {
      newEdge.setSourcePortModifier(new SDFStringEdgePropertyType(this.sourceModifier));
    }
    if (!this.targetModifier.isEmpty()) {
      newEdge.setTargetPortModifier(new SDFStringEdgePropertyType(this.targetModifier));
    }
    newEdge.setPropertyValue(SDFEdge.DATA_TYPE, this.fifo.getType());
    newEdge.setPropertyValue(SDFEdge.DATA_SIZE, this.dataSize);
    newEdge.setWeight(new DAGDefaultEdgePropertyType(weight));
    newEdge.setSourceLabel(this.sourcePort.getName());
    newEdge.setTargetLabel(this.sinkPort.getName());
    newEdge.setPropertyValue(AbstractEdge.BASE, this.dag);
    newEdge.setContainingEdge(edge);

    edge.getAggregate().add(newEdge);
    edge.setWeight(new DAGDefaultEdgePropertyType(weight * this.dataSize));

    return edge;
  }

  /**
   * Creates a new join actor with a unique ID. <br>
   * The created vertex is automatically added to the DAG.
   *
   * @param fixID
   *          fix part of the join actor ID
   * @param vertexFactory
   *          DAGVertex factory
   * @return the Join DAGVertex
   */
  private DAGVertex createJoinVertex(final String fixID, final MapperVertexFactory vertexFactory) {
    final DAGVertex joinVertex = vertexFactory.createVertex(DAGJoinVertex.DAG_JOIN_VERTEX);
    // final String id = fixID + "_" + Integer.toString(this.joinIDCounter++);
    setVertexDefault(joinVertex, fixID);
    this.dag.addVertex(joinVertex);
    return joinVertex;
  }

  /**
   * Creates a new fork actor with a unique ID. <br>
   * The created vertex is automatically added to the DAG.
   *
   * @param fixID
   *          fix part of the fork actor ID
   * @param vertexFactory
   *          DAGVertex factory
   * @return the Fork DAGVertex
   */
  private DAGVertex createForkVertex(final String fixID, final MapperVertexFactory vertexFactory) {
    final DAGVertex forkVertex = vertexFactory.createVertex(DAGForkVertex.DAG_FORK_VERTEX);
    // final String id = fixID + "_" + Integer.toString(this.forkIDCounter++);
    setVertexDefault(forkVertex, fixID);
    this.dag.addVertex(forkVertex);
    return forkVertex;
  }

  /**
   * Creates a new init actor. <br>
   * The created vertex is automatically added to the DAG.
   *
   * @param fixID
   *          id of the vertex
   * @param vertexFactory
   *          DAGVertex factory
   * @return the init DAGVertex
   */
  private DAGVertex createInitVertex(final String fixID, final MapperVertexFactory vertexFactory) {
    final DAGVertex initVertex = vertexFactory.createVertex(DAGInitVertex.DAG_INIT_VERTEX);
    setVertexDefault(initVertex, fixID);
    initVertex.getPropertyBean().setValue(DAGInitVertex.INIT_SIZE, (int) this.delays);
    this.dag.addVertex(initVertex);
    return initVertex;
  }

  /**
   * Creates a new end actor. <br>
   * The created vertex is automatically added to the DAG.
   *
   * @param fixID
   *          id of the vertex
   * @param vertexFactory
   *          DAGVertex factory
   * @return the end DAGVertex
   */
  private DAGVertex createEndVertex(final String fixID, final MapperVertexFactory vertexFactory) {
    final DAGVertex endVertex = vertexFactory.createVertex(DAGEndVertex.DAG_END_VERTEX);
    setVertexDefault(endVertex, fixID);
    this.dag.addVertex(endVertex);
    return endVertex;
  }

  private void setEndReference(final DAGVertex endVertex) {
    // Test to see if there is an init actor
    final DAGVertex initVertex = this.dag.getVertex(this.delayInitID);
    if (initVertex != null) {
      initVertex.getPropertyBean().setValue(DAGInitVertex.END_REFERENCE, endVertex.getName());
      endVertex.getPropertyBean().setValue(DAGInitVertex.END_REFERENCE, initVertex.getName());
    }
  }

  /**
   * Creates a new roundBuffer actor. <br>
   * The created vertex is automatically added to the DAG.
   *
   * @param fixID
   *          id of the vertex
   * @param vertexFactory
   *          DAGVertex factory
   * @return the roundBuffer DAGVertex
   */
  private DAGVertex createRoundBufferVertex(final Long sinkConsumption, final DAGVertex sinkVertex, final MapperVertexFactory vertexFactory) {
    final DAGVertex roundbufferVertex = vertexFactory.createVertex(DAGBroadcastVertex.DAG_BROADCAST_VERTEX);
    roundbufferVertex.getPropertyBean().setValue(DAGBroadcastVertex.SPECIAL_TYPE, DAGBroadcastVertex.SPECIAL_TYPE_ROUNDBUFFER);
    setVertexDefault(roundbufferVertex, "rb_" + sinkVertex.getName());
    this.dag.addVertex(roundbufferVertex);
    final DAGEdge edge = createEdge(roundbufferVertex, sinkVertex, sinkConsumption.toString());
    edge.setSourcePortModifier(new SDFStringEdgePropertyType(SDFEdge.MODIFIER_WRITE_ONLY));
    return roundbufferVertex;
  }

  /**
   * Set default properties of a DAGVertex. <br>
   * id = name = info <br>
   * nbRepeat = 1
   *
   * @param vertex
   *          the vertex to init
   * @param id
   *          the id to set
   */
  private void setVertexDefault(final DAGVertex vertex, final String id) {
    vertex.setId(id);
    vertex.setName(id);
    vertex.setInfo(id);
    vertex.setNbRepeat(new DAGDefaultVertexPropertyType(1));
  }
}
