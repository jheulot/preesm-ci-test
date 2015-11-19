/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot, Clément Guy
 * 
 * [mpelcat,jnezan,kdesnos,jheulot,cguy]@insa-rennes.fr
 * 
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
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
 ******************************************************************************/
package org.ietr.preesm.memory.allocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.memory.allocation.OrderedAllocator.Order;
import org.ietr.preesm.memory.allocation.OrderedAllocator.Policy;
import org.ietr.preesm.memory.exclusiongraph.MemoryExclusionGraph;

public abstract class AbstractMemoryAllocatorTask extends AbstractTaskImplementation {

	static final public String PARAM_VERBOSE = "Verbose";
	static final public String VALUE_TRUE_FALSE_DEFAULT = "? C {True, False}";
	static final public String VALUE_TRUE = "True";
	static final public String VALUE_FALSE = "False";

	static final public String PARAM_ALLOCATORS = "Allocator(s)";
	static final public String VALUE_ALLOCATORS_DEFAULT = "{?,?,...} C {Basic, BestFit, FirstFit, DeGreef}";
	static final public String VALUE_ALLOCATORS_BASIC = "Basic";
	static final public String VALUE_ALLOCATORS_BEST_FIT = "BestFit";
	static final public String VALUE_ALLOCATORS_FIRST_FIT = "FirstFit";
	static final public String VALUE_ALLOCATORS_DE_GREEF = "DeGreef";

	static final public String PARAM_XFIT_ORDER = "Best/First Fit order";
	static final public String VALUE_XFIT_ORDER_DEFAULT = "{?,?,...} C {ApproxStableSet, ExactStableSet, LargestFirst, Shuffle, Scheduling}";
	static final public String VALUE_XFIT_ORDER_APPROX_STABLE_SET = "ApproxStableSet";
	static final public String VALUE_XFIT_ORDER_LARGEST_FIRST = "LargestFirst";
	static final public String VALUE_XFIT_ORDER_SHUFFLE = "Shuffle";
	static final public String VALUE_XFIT_ORDER_EXACT_STABLE_SET = "ExactStableSet";
	static final public String VALUE_XFIT_ORDER_SCHEDULING = "Scheduling";

	static final public String PARAM_NB_SHUFFLE = "Nb of Shuffling Tested";
	static final public String VALUE_NB_SHUFFLE_DEFAULT = "10";

	static final public String PARAM_ALIGNMENT = "Data alignment";
	static final public String VALUE_ALIGNEMENT_NONE = "None";
	static final public String VALUE_ALIGNEMENT_DATA = "Data";
	static final public String VALUE_ALIGNEMENT_FIXED = "Fixed:=";
	static final public String VALUE_ALIGNEMENT_DEFAULT = "? C {None, Data, Fixed:=<nbBytes>}";

	static final public String PARAM_DISTRIBUTION_POLICY = "Distribution";
	static final public String VALUE_DISTRIBUTION_SHARED_ONLY = "SharedOnly";
	static final public String VALUE_DISTRIBUTION_DISTRIBUTED_ONLY = "DistributedOnly";
	static final public String VALUE_DISTRIBUTION_MIXED = "Mixed";
	static final public String VALUE_DISTRIBUTION_DEFAULT = "? C {" + VALUE_DISTRIBUTION_SHARED_ONLY + ", "
			+ VALUE_DISTRIBUTION_MIXED + ", " + VALUE_DISTRIBUTION_DISTRIBUTED_ONLY + "}";

	// Rem: Logger is used to display messages in the console
	protected Logger logger = WorkflowLogger.getLogger();

	// Shared attributes
	protected String valueVerbose;
	protected String valueAllocators;
	protected String valueXFitOrder;
	protected String valueNbShuffle;
	protected String valueDistribution;
	protected boolean verbose;
	protected String valueAlignment;
	protected int alignment;
	protected int nbShuffle;
	protected List<Order> ordering;
	protected List<MemoryAllocator> allocators;

	/**
	 * This method retrieves the value of task parameters from the workflow and
	 * stores them in local protected attributes. Some parameter {@link String}
	 * are also interpreted by this method (eg. {@link #verbose},
	 * {@link #allocators}).
	 * 
	 * @param parameters
	 *            the parameter {@link Map} given to the
	 *            {@link #execute(Map, Map, org.eclipse.core.runtime.IProgressMonitor, String, org.ietr.dftools.workflow.elements.Workflow)
	 *            execute()} method.
	 */
	protected void init(Map<String, String> parameters) {
		// Retrieve parameters from workflow
		valueVerbose = parameters.get(PARAM_VERBOSE);
		valueAllocators = parameters.get(PARAM_ALLOCATORS);
		valueXFitOrder = parameters.get(PARAM_XFIT_ORDER);
		valueNbShuffle = parameters.get(PARAM_NB_SHUFFLE);
		valueDistribution = parameters.get(PARAM_DISTRIBUTION_POLICY);

		verbose = valueVerbose.equals(VALUE_TRUE);

		// Correct default distribution policy
		if (valueDistribution.equals(VALUE_DISTRIBUTION_DEFAULT)) {
			valueDistribution = VALUE_DISTRIBUTION_SHARED_ONLY;
		}

		// Retrieve the alignment param
		valueAlignment = parameters.get(PARAM_ALIGNMENT);

		switch (valueAlignment.substring(0, Math.min(valueAlignment.length(), 7))) {
		case VALUE_ALIGNEMENT_NONE:
			alignment = -1;
			break;
		case VALUE_ALIGNEMENT_DATA:
			alignment = 0;
			break;
		case VALUE_ALIGNEMENT_FIXED:
			String fixedValue = valueAlignment.substring(7);
			alignment = Integer.parseInt(fixedValue);
			break;
		default:
			alignment = -1;
		}
		if (verbose) {
			logger.log(Level.INFO, "Allocation with alignment:=" + alignment + ".");
		}

		// Retrieve the ordering policies to test
		nbShuffle = 0;
		ordering = new ArrayList<Order>();
		if (valueXFitOrder.contains(VALUE_XFIT_ORDER_SHUFFLE)) {
			nbShuffle = Integer.decode(valueNbShuffle);
			ordering.add(Order.SHUFFLE);
		}
		if (valueXFitOrder.contains(VALUE_XFIT_ORDER_LARGEST_FIRST)) {
			ordering.add(Order.LARGEST_FIRST);
		}
		if (valueXFitOrder.contains(VALUE_XFIT_ORDER_APPROX_STABLE_SET)) {
			ordering.add(Order.STABLE_SET);
		}
		if (valueXFitOrder.contains(VALUE_XFIT_ORDER_EXACT_STABLE_SET)) {
			ordering.add(Order.EXACT_STABLE_SET);
		}
		if (valueXFitOrder.contains(VALUE_XFIT_ORDER_SCHEDULING)) {
			ordering.add(Order.SCHEDULING);
		}

	}

	/**
	 * Based on allocators specified in the task parameters, and stored in the
	 * {@link #allocators} attribute, this method instantiate the
	 * {@link MemoryAllocator} that are to be executed on the given
	 * {@link MemoryExclusionGraph MEG}.
	 * 
	 * @param memEx
	 *            the {@link MemoryExclusionGraph MEG} to allocate.
	 */
	protected void createAllocators(MemoryExclusionGraph memEx) {
		// Create all allocators
		allocators = new ArrayList<MemoryAllocator>();
		if (valueAllocators.contains(VALUE_ALLOCATORS_BASIC)) {
			MemoryAllocator alloc = new BasicAllocator(memEx);
			alloc.setAlignment(alignment);
			allocators.add(alloc);
		}
		if (valueAllocators.contains(VALUE_ALLOCATORS_FIRST_FIT)) {
			for (Order o : ordering) {
				OrderedAllocator alloc = new FirstFitAllocator(memEx);
				alloc.setNbShuffle(nbShuffle);
				alloc.setOrder(o);
				alloc.setAlignment(alignment);
				allocators.add(alloc);
			}
		}
		if (valueAllocators.contains(VALUE_ALLOCATORS_BEST_FIT)) {
			for (Order o : ordering) {
				OrderedAllocator alloc = new BestFitAllocator(memEx);
				alloc.setNbShuffle(nbShuffle);
				alloc.setOrder(o);
				alloc.setAlignment(alignment);
				allocators.add(alloc);
			}
		}
		if (valueAllocators.contains(VALUE_ALLOCATORS_DE_GREEF)) {
			MemoryAllocator alloc = new DeGreefAllocator(memEx);
			alloc.setAlignment(alignment);
			allocators.add(alloc);
		}
	}

	protected void allocateWith(MemoryAllocator allocator) throws WorkflowException {
		long tStart, tFinish;
		String sAllocator = allocator.getClass().getSimpleName();
		if (allocator instanceof OrderedAllocator) {
			sAllocator += "(" + ((OrderedAllocator) allocator).getOrder();
			if (((OrderedAllocator) allocator).getOrder() == Order.SHUFFLE) {
				sAllocator += ":" + ((OrderedAllocator) allocator).getNbShuffle();
			}
			sAllocator += ")";
		}

		if (verbose) {
			logger.log(Level.INFO, "Starting allocation with " + sAllocator);
		}

		tStart = System.currentTimeMillis();
		allocator.allocate();
		tFinish = System.currentTimeMillis();

		// Check the correct allocation
		try {
			if (!allocator.checkAllocation().isEmpty()) {
				throw new WorkflowException("The obtained allocation was not valid because mutually"
						+ " exclusive memory objects have overlapping address ranges."
						+ " The allocator is not working.\n" + allocator.checkAllocation());
			}
		} catch (RuntimeException e) {
			throw new WorkflowException(e.getMessage());
		}

		if (!allocator.checkAlignment().isEmpty()) {
			throw new WorkflowException("The obtained allocation was not valid because there were"
					+ " unaligned memory objects. The allocator is not working.\n" + allocator.checkAlignment());
		}

		String log = sAllocator + " allocates " + allocator.getMemorySize() + "mem. units in " + (tFinish - tStart)
				+ " ms.";

		if (allocator instanceof OrderedAllocator && ((OrderedAllocator) allocator).getOrder() == Order.SHUFFLE) {
			((OrderedAllocator) allocator).setPolicy(Policy.worst);
			log += " worst: " + allocator.getMemorySize();
			
			((OrderedAllocator) allocator).setPolicy(Policy.mediane);
			log += "(med: " + allocator.getMemorySize();
			
			((OrderedAllocator) allocator).setPolicy(Policy.average);
			log += " avg: " + allocator.getMemorySize() + ")";
			
			((OrderedAllocator) allocator).setPolicy(Policy.best);
		}

		logger.log(Level.INFO, log);
	}

	@Override
	public Map<String, String> getDefaultParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(PARAM_VERBOSE, VALUE_TRUE_FALSE_DEFAULT);
		parameters.put(PARAM_ALLOCATORS, VALUE_ALLOCATORS_DEFAULT);
		parameters.put(PARAM_XFIT_ORDER, VALUE_XFIT_ORDER_DEFAULT);
		parameters.put(PARAM_NB_SHUFFLE, VALUE_NB_SHUFFLE_DEFAULT);
		parameters.put(PARAM_ALIGNMENT, VALUE_ALIGNEMENT_DEFAULT);
		parameters.put(PARAM_DISTRIBUTION_POLICY, VALUE_DISTRIBUTION_DEFAULT);
		return parameters;
	}
}
