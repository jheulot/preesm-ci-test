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

package org.ietr.preesm.mapper.ui.bestcost;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.ietr.preesm.mapper.ui.BestCostPlotter;

/**
 * Editor displaying the best cost found in time
 * 
 * @author mpelcat
 */
public class BestCostEditor extends EditorPart {

	private BestCostPlotter plotter = null;

	public BestCostEditor() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {

		try {
			setSite(site);
			setInput(input);
			setPartName(input.getName());

			if (input instanceof BestCostEditorInput) {
				BestCostEditorInput implinput = (BestCostEditorInput) input;
				this.plotter = implinput.getPlotter();
			}

		} catch (Exception e) {
			// Editor might not exist anymore if switching databases. So
			// just close it.
			this.getEditorSite().getPage().closeEditor(this, false);
			throw new PartInitException("File " + input.getName()
					+ " does not exist.");
		}

	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {

		if (plotter != null) {

			plotter.display(parent);
		}

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public static void createEditor(BestCostPlotter plotter) {
		IEditorInput input = new BestCostEditorInput(plotter);

		PlatformUI.getWorkbench().getDisplay()
				.asyncExec(new BestCostEditorRunnable(input));

	}
}