/*
 * Copyright or © or Copr. IETR/INSA - Rennes (2011 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2014)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2011)
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
package org.preesm.ui.scenario.editor;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.preesm.model.scenario.Scenario;

// TODO: Auto-generated Javadoc
/**
 * Tree representing a SDF graph in the constraint page and the code generation page.
 *
 * @author mpelcat
 */
public class SDFTreeSection extends SectionPart {

  /** Current tree viewer initialized here. */
  private CheckboxTreeViewer treeviewer = null;

  /** Current section to which this section part corresponds. */
  private Section section = null;

  /**
   * Creates the tree view.
   *
   * @param scenario
   *          the scenario
   * @param inputSection
   *          the input section
   * @param toolkit
   *          the toolkit
   * @param style
   *          the style
   * @param listener
   *          the listener
   * @param checkStateListener
   *          the check state listener
   */
  public SDFTreeSection(final Scenario scenario, final Section inputSection, final FormToolkit toolkit, final int style,
      final IPropertyListener listener, final ISDFCheckStateListener checkStateListener) {
    super(inputSection);

    this.section = inputSection;

    this.section.setVisible(true);
    final Composite container = toolkit.createComposite(getSection());
    container.setLayout(new GridLayout());

    // Creating a selector for available cores
    checkStateListener.addComboBoxSelector(container, toolkit);

    // Creating the tree view
    this.treeviewer = new CheckboxTreeViewer(toolkit.createTree(container, SWT.CHECK));

    // The content provider fills the tree
    final PreesmAlgorithmTreeContentProvider contentProvider = new PreesmAlgorithmTreeContentProvider(this.treeviewer);
    this.treeviewer.setContentProvider(contentProvider);

    // The check state listener modifies the check status of elements
    checkStateListener.setTreeViewer(this.treeviewer, contentProvider, listener);
    this.treeviewer.setLabelProvider(new PreesmAlgorithmTreeLabelProvider());
    this.treeviewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

    this.treeviewer.addCheckStateListener(checkStateListener);

    final GridData gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 400;
    gd.widthHint = 250;
    this.treeviewer.getTree().setLayoutData(gd);

    this.treeviewer.setUseHashlookup(true);
    this.treeviewer.setInput(scenario);
    toolkit.paintBordersFor(container);
    this.section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
    this.section.setClient(container);

    // Tree is refreshed in case of algorithm modifications
    this.section.addPaintListener(checkStateListener);

  }

}
