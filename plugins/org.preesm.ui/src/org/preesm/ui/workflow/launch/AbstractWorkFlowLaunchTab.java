/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2018 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2018 - 2019)
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
/**
 *
 */
package org.preesm.ui.workflow.launch;

import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.preesm.commons.messages.PreesmMessages;
import org.preesm.ui.utils.FileUtils;

/**
 * Containing common funtionalities of launch tabs.
 *
 * @author mpelcat
 */
public abstract class AbstractWorkFlowLaunchTab extends AbstractLaunchConfigurationTab {

  /** current Composite. */
  private Composite currentComposite;

  /** file attribute name to save the entered file. */
  private String fileAttributeName = null;

  /** The file path. */
  private Text filePath = null;

  @Override
  public void createControl(final Composite parent) {

    this.currentComposite = new Composite(parent, SWT.NONE);
    setControl(this.currentComposite);

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    this.currentComposite.setLayout(gridLayout);

  }

  /**
   * Displays a file text window with a browser button.
   *
   * @param title
   *          A line of text displayed before the file chooser
   * @param attributeName
   *          The name of the attribute in which the property should be saved
   */
  public void drawFileChooser(final String title, final String attributeName, final String fileType) {

    final Label label2 = new Label(this.currentComposite, SWT.NONE);
    label2.setText(title);
    this.fileAttributeName = attributeName;

    new Label(this.currentComposite, SWT.NONE);

    final Button buttonBrowse = new Button(this.currentComposite, SWT.PUSH);
    buttonBrowse.setText("Browse...");
    buttonBrowse.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        final Set<String> scenarioExtensions = new LinkedHashSet<>();
        scenarioExtensions.add(fileType);
        final String message = PreesmMessages
            .getString("Workflow.browse" + Character.toUpperCase(fileType.charAt(0)) + fileType.substring(1) + "Title");
        IPath browseFiles = FileUtils.browseFiles(message, scenarioExtensions);
        if (browseFiles != null) {
          filePath.setText(browseFiles.toString());
        }
      }
    });

    this.filePath = new Text(this.currentComposite, SWT.BORDER);

    final GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
    layoutData.widthHint = 200;
    this.filePath.setLayoutData(layoutData);
    this.filePath.addModifyListener(e -> {
      setDirty(true);
      updateLaunchConfigurationDialog();
    });

  }

  /**
   * Gets the current composite.
   *
   * @return the current composite
   */
  protected Composite getCurrentComposite() {
    return this.currentComposite;
  }

  @Override
  public void initializeFrom(final ILaunchConfiguration configuration) {
    try {
      this.filePath.setText(configuration.getAttribute(this.fileAttributeName, ""));
    } catch (final CoreException e) {
      this.filePath.setText("");
    }

    setDirty(false);
  }

  @Override
  public boolean isValid(final ILaunchConfiguration launchConfig) {
    return true;
  }

  @Override
  public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
    // Saving the file path chosen in a tab attribute
    if ((this.filePath != null) && (this.fileAttributeName != null)) {
      configuration.setAttribute(this.fileAttributeName, this.filePath.getText());
    }
    setDirty(false);
  }

  @Override
  public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {

    configuration.setAttribute(this.fileAttributeName, "");
    setDirty(false);
  }

}
