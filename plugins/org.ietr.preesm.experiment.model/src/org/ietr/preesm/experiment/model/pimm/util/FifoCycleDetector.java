/*******************************************************************************
 * Copyright or © or Copr. %%LOWERDATE%% - %%UPPERDATE%% IETR/INSA:
 *
 * %%AUTHORS%%
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
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
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package org.ietr.preesm.experiment.model.pimm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ietr.preesm.experiment.model.pimm.AbstractActor;
import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.DataOutputPort;
import org.ietr.preesm.experiment.model.pimm.Dependency;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.PiGraph;

public class FifoCycleDetector extends PiMMSwitch<Void> {

	/**
	 * If this boolean is true, the cycle detection will stop at the first cycle
	 * detected.
	 */
	protected boolean fastDetection = false;

	/**
	 * List of the {@link AbstractActor}s that were already visited and are not
	 * involved in cycles.
	 */
	protected HashSet<AbstractActor> visited;

	/**
	 * List the {@link AbstractActor} that are currently being visited. If one
	 * of them is met again, this means that there is a cycle.
	 */
	protected ArrayList<AbstractActor> branch;

	/**
	 * Stores all the {@link AbstractActor} cycles that were detected. Each
	 * element of the {@link ArrayList} is an {@link ArrayList} containing
	 * {@link AbstractActor} forming a cycle. <br>
	 * <br>
	 * <b> Not all cycles are detected by this algorithm ! </b><br>
	 * For example, if two cycles have some links in common, only one of them
	 * will be detected.
	 */
	protected List<List<AbstractActor>> cycles;

	/**
	 * List of {@link Fifo} that will not be considered as part of the
	 * {@link PiGraph} when looking for cycles. This list is usefull when trying
	 * to identify all cycles in a graph.
	 */
	protected Set<Fifo> ignoredFifos;

	/**
	 * Default constructor. Assume fast detection is true. (i.e. the detection
	 * will stop at the first cycle detected)
	 */
	public FifoCycleDetector() {
		this(true);
	}

	/**
	 * 
	 * @param fastDetection
	 */
	public FifoCycleDetector(boolean fastDetection) {
		this.fastDetection = fastDetection;
		visited = new HashSet<AbstractActor>();
		branch = new ArrayList<AbstractActor>();
		cycles = new ArrayList<List<AbstractActor>>();
		ignoredFifos = new HashSet<Fifo>();
	}

	/**
	 * Add the current cycle to the cycle list.
	 * 
	 * @param actor
	 *            the {@link AbstractActor} forming a cycle in the
	 *            {@link Dependency} tree.
	 */
	protected void addCycle(AbstractActor actor) {

		ArrayList<AbstractActor> cycle = new ArrayList<AbstractActor>();

		// Backward scan of the branch list until the actor is found again
		int i = branch.size();
		do {
			i--;
			cycle.add(0, branch.get(i));
		} while (branch.get(i) != actor && i > 0);

		// If i is less than 0, the whole branch was scanned but the actor
		// was not found.
		// This means this branch is not a cycle. (But this should not happen,
		// so throw an error)
		if (i < 0) {
			throw new RuntimeException(
					"No FIFO cycle was found in this branch.");
		}

		// If this code is reached, the cycle was correctly detected.
		// We add it to the cycles list.
		cycles.add(cycle);
	}

	@Override
	public Void casePiGraph(PiGraph graph) {

		// Visit AbstractActor until they are all visited
		ArrayList<AbstractActor> actors = new ArrayList<>(graph.getVertices());
		while (actors.size() != 0) {
			doSwitch(actors.get(0));

			// If fast detection is activated and a cycle was detected, get
			// out of here!
			if (fastDetection && cyclesDetected()) {
				break;
			}

			// Else remove visited AbstractActor and continue
			actors.removeAll(visited);
		}

		return null;
	}

	@Override
	public Void caseAbstractActor(AbstractActor actor) {
		// Visit the AbstractActor and its successors if it was not already done
		if (!visited.contains(actor)) {
			// Check if the AbstractActor is already in the branch (i.e. check
			// if
			// there is a cycle)
			if (branch.contains(actor)) {
				// There is a cycle
				addCycle(actor);
				return null;
			}

			// Add the AbstractActor to the visited branch
			branch.add(actor);

			// Visit all AbstractActor depending on the current one.
			for (DataOutputPort port : actor.getDataOutputPorts()) {
				Fifo outgoingFifo = port.getOutgoingFifo();
				if (outgoingFifo != null && !ignoredFifos.contains(outgoingFifo)) {
					doSwitch(outgoingFifo.getTargetPort());
				}

				// If fast detection is activated and a cycle was detected, get
				// out of here!
				if (fastDetection && cyclesDetected()) {
					break;
				}
			}

			// Remove the AbstractActor from the branch.
			branch.remove(branch.size() - 1);
			// Add the AbstractActor to the visited list
			visited.add(actor);
		}
		return null;
	}

	@Override
	public Void caseDataInputPort(DataInputPort port) {
		// Visit the owner of the data input port only if it is a AbstractActor
		if (port.eContainer() instanceof AbstractActor) {
			doSwitch(port.eContainer());
		}

		return null;
	}

	/**
	 * Reset the visitor to use it again. This method will clean the lists of
	 * already visited {@link AbstractActor} contained in the
	 * {@link FifoCycleDetector}, and the list of detected cycles.
	 */
	public void clear() {
		visited.clear();
		branch.clear();
		cycles.clear();
	}

	/**
	 * Add a {@link Fifo} to the {@link #ignoredFifos} {@link Set}.
	 * 
	 * @param fifo
	 *            the {@link Fifo} to add.
	 */
	public void addIgnoredFifo(Fifo fifo) {
		ignoredFifos.add(fifo);
	}

	/**
	 * Remove a {@link Fifo} from the {@link #ignoredFifos} {@link Set}.
	 * 
	 * @param fifo
	 *            the {@link Fifo} to remove.
	 * @return result from the {@link Set#remove(Object)} operation.
	 */
	public boolean removeIgnoredFifo(Fifo fifo) {
		return ignoredFifos.remove(fifo);
	}

	/**
	 * Clear the {@link Set} of ignored {@link Fifo}.
	 */
	public void clearIgnoredFifos() {
		ignoredFifos.clear();
	}

	public List<List<AbstractActor>> getCycles() {
		return cycles;
	}

	/**
	 * Retrieve the result of the visitor. This method should be called only
	 * after the visitor was executed using
	 * {@link FifoCycleDetector#doSwitch(org.eclipse.emf.ecore.EObject)
	 * doSwitch(object)} method on a {@link AbstractActor} or on a
	 * {@link PiGraph}.
	 * 
	 * @return true if cycles were detected, false else.
	 */
	public boolean cyclesDetected() {
		return cycles.size() > 0;
	}

	public void addIgnoredFifos(Collection<Fifo> fifos) {
		ignoredFifos.addAll(fifos);		
	}

	/**
	 * Considering a {@link List} of {@link AbstractActor} forming a cyclic
	 * data-path (cf. {@link FifoCycleDetector}), this method returns a
	 * {@link List} of all {@link Fifo} involved in this cyclic data-path.
	 * 
	 * @param cycle
	 *            A list of {@link AbstractActor} forming a Cycle.
	 */
	public static List<Fifo> findCycleFeedbackFifos(List<AbstractActor> cycle) {
		// Find the Fifos between each pair of actor of the cycle
		List<List<Fifo>> cyclesFifos = new ArrayList<List<Fifo>>();
		for (int i = 0; i < cycle.size(); i++) {
			AbstractActor srcActor = cycle.get(i);
			AbstractActor dstActor = cycle.get((i + 1) % cycle.size());
	
			List<Fifo> outFifos = new ArrayList<Fifo>();
			srcActor.getDataOutputPorts().forEach(
					port -> {
						if (port.getOutgoingFifo().getTargetPort().eContainer()
								.equals(dstActor))
							outFifos.add(port.getOutgoingFifo());
					});
			cyclesFifos.add(outFifos);
		}
	
		// Find a list of FIFO between a pair of actor with delays on all FIFOs
		List<Fifo> feedbackFifos = null;
		for (List<Fifo> cycleFifos : cyclesFifos) {
			boolean hasDelays = true;
			for (Fifo fifo : cycleFifos) {
				hasDelays &= (fifo.getDelay() != null);
			}
	
			if (hasDelays) {
				// Keep the shortest list of feedback delay
				feedbackFifos = (feedbackFifos == null || feedbackFifos.size() > cycleFifos
						.size()) ? cycleFifos : feedbackFifos;
			}
		}
		if (feedbackFifos != null) {
			return feedbackFifos;
		} else {
			// If no feedback fifo with delays were found. Select a list with a
			// small number of fifos
			cyclesFifos.sort((l1, l2) -> l1.size() - l2.size());
			return cyclesFifos.get(0);
		}
	}
}
