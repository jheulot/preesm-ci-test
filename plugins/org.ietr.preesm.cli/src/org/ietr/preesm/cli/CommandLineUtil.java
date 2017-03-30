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
package org.ietr.preesm.cli;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.runtime.CoreException;
import org.ietr.dftools.workflow.tools.CLIWorkflowLogger;

/**
 * Define methods to use in specific cases of IApplication executions, in
 * command-line
 * 
 * @author Antoine Lorence
 * 
 */
public class CommandLineUtil {

	/**
	 * If it is enabled, disable auto-building on the current workspace.
	 * 
	 * @return true if auto-building was enabled, false if it is already
	 *         disabled
	 * @throws CoreException
	 */
	public static boolean disableAutoBuild(final IWorkspace wp)
			throws CoreException {
		// IWorkspace.getDescription() returns a copy. We need to extract,
		// modify and set it to the current workspace.
		final IWorkspaceDescription desc = wp.getDescription();
		if (wp.isAutoBuilding()) {
			CLIWorkflowLogger.debugln("Disbale auto-building");
			desc.setAutoBuilding(false);
			wp.setDescription(desc);
			return true;
		}

		return false;
	}

	/**
	 * Enable auto-building on the current workspace
	 * 
	 * @throws CoreException
	 */
	public static void enableAutoBuild(final IWorkspace wp)
			throws CoreException {
		CLIWorkflowLogger.debugln("Re-enable auto-building");
		// IWorkspace.getDescription() returns a copy. We need to extract,
		// modify and set it to the current workspace.
		final IWorkspaceDescription desc = wp.getDescription();
		desc.setAutoBuilding(true);
		wp.setDescription(desc);
	}
}
