/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.mapper.abc.edgescheduling;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.mapper.abc.order.OrderManager;
import org.ietr.preesm.mapper.model.MapperDAGVertex;
import org.ietr.preesm.mapper.model.property.VertexTiming;
import org.ietr.preesm.mapper.model.special.TransferVertex;

/**
 * During edge scheduling, one needs to find intervals to fit the transfers.
 * This class deals with intervals in the transfer scheduling
 * 
 * @author mpelcat
 */
public class IntervalFinder {

	/**
	 * Contains the rank list of all the vertices in an implementation
	 */
	private OrderManager orderManager = null;
	private Random random;

	private static class FindType {
		public static final FindType largestFreeInterval = new FindType();
		public static final FindType earliestBigEnoughInterval = new FindType();

		@Override
		public String toString() {
			if (this == largestFreeInterval)
				return "largestFreeInterval";
			if (this == earliestBigEnoughInterval)
				return "largestFreeInterval";
			return "";
		}
	}

	public IntervalFinder(OrderManager orderManager) {
		super();
		this.orderManager = orderManager;
		random = new Random(System.nanoTime());
	}

	/**
	 * Finds the largest free interval in a schedule
	 */
	public Interval findLargestFreeInterval(ComponentInstance component,
			MapperDAGVertex minVertex, MapperDAGVertex maxVertex) {

		return findInterval(component, minVertex, maxVertex,
				FindType.largestFreeInterval, 0);

	}

	public Interval findEarliestNonNullInterval(ComponentInstance component,
			MapperDAGVertex minVertex, MapperDAGVertex maxVertex) {

		return findInterval(component, minVertex, maxVertex,
				FindType.earliestBigEnoughInterval, 0);

	}

	public Interval findEarliestBigEnoughInterval(ComponentInstance component,
			MapperDAGVertex minVertex, MapperDAGVertex maxVertex, long size) {

		return findInterval(component, minVertex, maxVertex,
				FindType.earliestBigEnoughInterval, size);

	}

	/**
	 * Finds the largest free interval in a schedule between a minVertex and a
	 * maxVertex
	 */
	public Interval findInterval(ComponentInstance component,
			MapperDAGVertex minVertex, MapperDAGVertex maxVertex,
			FindType type, long data) {

		List<MapperDAGVertex> schedule = orderManager.getVertexList(component);

		long minIndexVertexEndTime = -1;
		int minIndex = -1;

		if (minVertex != null) {
			minIndex = orderManager.totalIndexOf(minVertex);

			VertexTiming props = minVertex.getTiming();
			if (props.getTLevel() >= 0) {
				minIndexVertexEndTime = props.getTLevel() + props.getCost();
			}
		}

		int maxIndex = Integer.MAX_VALUE;
		if (maxVertex != null) {
			maxIndex = orderManager.totalIndexOf(maxVertex);
		} else {
			maxIndex = orderManager.getTotalOrder().size();
		}

		Interval oldInt = new Interval(0, 0, -1);
		Interval newInt = null;
		Interval freeInterval = new Interval(-1, -1, 0);

		if (schedule != null) {
			for (MapperDAGVertex v : schedule) {
				VertexTiming props = v.getTiming();

				// If we have the current vertex tLevel
				if (props.getTLevel() >= 0) {

					// newInt is the interval corresponding to the execution of
					// the vertex v: a non free interval
					newInt = new Interval(props.getCost(),
							props.getTLevel(), orderManager.totalIndexOf(v));

					// end of the preceding non free interval
					long oldEnd = oldInt.getStartTime() + oldInt.getDuration();
					// latest date between the end of minVertex and the end of
					// oldInt
					long available = Math.max(minIndexVertexEndTime, oldEnd);
					// Computing the size of the free interval
					long freeIntervalSize = newInt.getStartTime() - available;

					if (type == FindType.largestFreeInterval) {
						// Verifying that newInt is in the interval of search
						if (newInt.getTotalOrderIndex() > minIndex
								&& newInt.getTotalOrderIndex() <= maxIndex) {

							if (freeIntervalSize > freeInterval.getDuration()) {
								// The free interval takes the index of its
								// following task v.
								// Inserting a vertex in this interval means
								// inserting it before v.
								freeInterval = new Interval(freeIntervalSize,
										available, newInt.getTotalOrderIndex());
							}
						}
					} else if (type == FindType.earliestBigEnoughInterval) {
						if (newInt.getTotalOrderIndex() > minIndex
								&& newInt.getTotalOrderIndex() <= maxIndex) {

							if (freeIntervalSize >= data) {
								// The free interval takes the index of its
								// following task v.
								// Inserting a vertex in this interval means
								// inserting it before v.
								freeInterval = new Interval(freeIntervalSize,
										available, newInt.getTotalOrderIndex());
								break;
							}
						}
					}
					oldInt = newInt;
				}
			}
		}

		return freeInterval;

	}

	public void displayCurrentSchedule(TransferVertex vertex,
			MapperDAGVertex source) {

		ComponentInstance component = vertex
				.getEffectiveComponent();
		List<MapperDAGVertex> schedule = orderManager.getVertexList(component);

		VertexTiming sourceProps = source.getTiming();
		long availability = sourceProps.getTLevel() + sourceProps.getCost();
		if (sourceProps.getTLevel() < 0)
			availability = -1;

		String trace = "schedule of " + vertex.getName() + " available at "
				+ availability + ": ";

		if (schedule != null) {
			for (MapperDAGVertex v : schedule) {
				VertexTiming props = v.getTiming();
				if (props.getTLevel() >= 0)
					trace += "<" + props.getTLevel() + ","
							+ (props.getTLevel() + props.getCost()) + ">";
			}
		}

		WorkflowLogger.getLogger().log(Level.INFO, trace);
	}

	public OrderManager getOrderManager() {
		return orderManager;
	}

	/**
	 * Returns the best index to schedule vertex in total order
	 */
	public int getBestIndex(MapperDAGVertex vertex, long minimalHoleSize) {
		int index = -1;
		int latePred = getLatestPredecessorIndex(vertex);
		int earlySuc = getEarliestsuccessorIndex(vertex);

		ComponentInstance op = vertex
				.getEffectiveOperator();
		MapperDAGVertex source = (latePred == -1) ? null : orderManager
				.get(latePred);
		MapperDAGVertex target = (earlySuc == -1) ? null : orderManager
				.get(earlySuc);

		// Finds the largest free hole after the latest predecessor
		if (op != null) {
			Interval largestInterval = findLargestFreeInterval(op, source,
					target);

			// If it is big enough, use it
			if (largestInterval.getDuration() > minimalHoleSize) {
				index = largestInterval.getTotalOrderIndex();
			} else if (latePred != -1) {
				// Otherwise, place the vertex randomly
				int sourceIndex = latePred + 1;
				int targetIndex = earlySuc;
				if (targetIndex == -1) {
					targetIndex = orderManager.getTotalOrder().size();
				}

				if (targetIndex - sourceIndex > 0) {
					int randomVal = random.nextInt(targetIndex - sourceIndex);
					index = sourceIndex + randomVal;
				}
			}

		}

		return index;
	}

	/**
	 * Returns the best index to schedule vertex in total order
	 */
	public int getIndexOfFirstBigEnoughHole(MapperDAGVertex vertex, long size) {
		int index = -1;
		int latePred = getLatestPredecessorIndex(vertex);
		int earlySuc = getEarliestsuccessorIndex(vertex);

		ComponentInstance op = vertex
				.getEffectiveOperator();
		MapperDAGVertex source = (latePred == -1) ? null : orderManager
				.get(latePred);
		MapperDAGVertex target = (earlySuc == -1) ? null : orderManager
				.get(earlySuc);

		// Finds the largest free hole after the latest predecessor
		if (op != null) {
			Interval largestInterval = findEarliestBigEnoughInterval(op,
					source, target, size);

			// If it is big enough, use it
			if (largestInterval.getDuration() >= 0) {
				index = largestInterval.getTotalOrderIndex();
			} else {
				index = -1;
			}

		}

		return index;
	}

	/**
	 * Returns the earliest index after the last predecessor
	 */
	public int getEarliestIndex(MapperDAGVertex vertex) {
		int latePred = getLatestPredecessorIndex(vertex);

		if (latePred != -1) {
			latePred++;
		}
		return latePred;
	}

	/**
	 * Returns the highest index of vertex predecessors
	 */
	private int getLatestPredecessorIndex(MapperDAGVertex testVertex) {
		int index = -1;

		for (MapperDAGVertex v : testVertex.getPredecessors(true).keySet()) {
			index = Math.max(index, orderManager.totalIndexOf(v));
		}

		return index;
	}

	/**
	 * Returns the lowest index of vertex successors
	 */
	private int getEarliestsuccessorIndex(MapperDAGVertex testVertex) {
		int index = Integer.MAX_VALUE;

		for (MapperDAGVertex v : testVertex.getSuccessors(true).keySet()) {
			index = Math.min(index, orderManager.totalIndexOf(v));
		}

		if (index == Integer.MAX_VALUE)
			index = -1;

		return index;
	}
}
