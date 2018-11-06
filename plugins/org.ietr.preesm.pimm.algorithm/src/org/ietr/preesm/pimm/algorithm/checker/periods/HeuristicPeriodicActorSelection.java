package org.ietr.preesm.pimm.algorithm.checker.periods;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.experiment.model.pimm.AbstractActor;
import org.ietr.preesm.experiment.model.pimm.Actor;
import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.DataOutputPort;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.PiGraph;

/**
 * This class aims to select periodic actors on which execute the period checkers (nbff and nblf).
 * 
 * @author ahonorat
 */
class HeuristicPeriodicActorSelection {

  static Map<Actor, Long> selectActors(final Map<Actor, Long> periodicActors, final List<Actor> originActors,
      final int rate, final PiGraph graph, final PreesmScenario scenario, boolean reverse) {
    if (rate == 100 || periodicActors.isEmpty()) {
      return periodicActors;
    }
    if (rate == 0) {
      return new HashMap<>();
    }

    Map<AbstractActor, Long> topoRanks = null;
    if (reverse) {
      topoRanks = topologicalASAPrankingT(originActors, graph);
    } else {
      topoRanks = topologicalASAPranking(originActors, graph);
    }
    final Map<Actor, Double> topoRanksPeriodic = new HashMap<>();
    for (Entry<Actor, Long> e : periodicActors.entrySet()) {
      final Actor actor = e.getKey();
      final long rank = topoRanks.get(actor);
      final long period = e.getValue();
      long wcetMin = Long.MAX_VALUE;
      for (String operatorDefinitionID : scenario.getOperatorDefinitionIds()) {
        final long timing = scenario.getTimingManager().getTimingOrDefault(actor.getName(), operatorDefinitionID)
            .getTime();
        if (timing < wcetMin) {
          wcetMin = timing;
        }
      }
      topoRanksPeriodic.put(actor, (period - wcetMin) / (double) rank);
    }
    final StringBuilder sb = new StringBuilder();
    topoRanksPeriodic.entrySet().forEach(a -> sb.append(a.getKey().getName() + "(" + a.getValue() + ") / "));
    WorkflowLogger.getLogger().log(Level.WARNING, "Periodic actor ranks: " + sb.toString());

    return selectFromRate(periodicActors, topoRanksPeriodic, rate);
  }

  private static Map<Actor, Long> selectFromRate(Map<Actor, Long> periodicActors, Map<Actor, Double> topoRanksPeriodic,
      int rate) {
    final int nbPeriodicActors = periodicActors.size();
    final double nActorsToSelect = nbPeriodicActors * (rate / (double) 100.0);
    final int nbActorsToSelect = Math.max((int) Math.ceil(nActorsToSelect), 1);

    Map<Actor,
        Long> selectedActors = periodicActors.entrySet().stream().sorted(Map.Entry.comparingByValue())
            .limit(nbActorsToSelect).collect(
                Collectors.toMap(Map.Entry::getKey, e -> periodicActors.get(e.getKey()), (e1, e2) -> e1, HashMap::new));

    // final Map<Actor, Long> selectedActors = new HashMap<>();
    // for (int i = 0; i < nbActorsToSelect; ++i) {
    // Actor actor = topoRanksPeriodic.firstKey();
    // topoRanksPeriodic.remove(actor);
    // WorkflowLogger.getLogger().log(Level.INFO, "Periodic actor: " + actor.getName());
    // selectedActors.put(actor, periodicActors.get(actor));
    // }
    return selectedActors;
  }

  private static Map<AbstractActor, Long> topologicalASAPranking(final List<Actor> sourceActors, final PiGraph graph) {
    final Map<AbstractActor, Long> topoRanks = new HashMap<>();
    for (Actor actor : sourceActors) {
      topoRanks.put(actor, 1L);
    }

    final Deque<AbstractActor> toVisit = new ArrayDeque<>(sourceActors);
    while (!toVisit.isEmpty()) {
      final AbstractActor actor = toVisit.removeFirst();
      final long rank = topoRanks.get(actor);
      for (DataOutputPort sport : actor.getDataOutputPorts()) {
        final Fifo fifo = sport.getOutgoingFifo();
        final DataInputPort tport = fifo.getTargetPort();
        final AbstractActor dest = tport.getContainingActor();
        if (!topoRanks.containsKey(dest)) {
          topoRanks.put(dest, rank + 1);
          toVisit.addLast(dest);
        }
      }
    }

    return topoRanks;
  }

  private static Map<AbstractActor, Long> topologicalASAPrankingT(final List<Actor> sinkActors, final PiGraph graph) {
    final Map<AbstractActor, Long> topoRanks = new HashMap<>();
    for (Actor actor : sinkActors) {
      topoRanks.put(actor, 1L);
    }

    final Deque<AbstractActor> toVisit = new ArrayDeque<>(sinkActors);
    while (!toVisit.isEmpty()) {
      final AbstractActor actor = toVisit.removeFirst();
      final long rank = topoRanks.get(actor);
      for (DataInputPort tport : actor.getDataInputPorts()) {
        final Fifo fifo = tport.getIncomingFifo();
        final DataOutputPort sport = fifo.getSourcePort();
        final AbstractActor dest = sport.getContainingActor();
        if (!topoRanks.containsKey(dest)) {
          topoRanks.put(dest, rank + 1);
          toVisit.addLast(dest);
        }
      }
    }

    return topoRanks;
  }

}
