/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2011 - 2019) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2019)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Daniel Madroñal <daniel.madronal@upm.es> (2018)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2011 - 2015)
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
package org.preesm.ui.scenario.editor.papify;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.preesm.commons.exceptions.PreesmException;
import org.preesm.commons.files.WorkspaceUtils;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.model.scenario.PapiComponent;
import org.preesm.model.scenario.PapiEvent;
import org.preesm.model.scenario.PapiEventInfo;
import org.preesm.model.scenario.PapiEventSet;
import org.preesm.model.scenario.Scenario;
import org.preesm.model.scenario.serialize.PapiConfigParser;
import org.preesm.model.scenario.util.ScenarioUserFactory;
import org.preesm.model.slam.Design;
import org.preesm.model.slam.component.Component;
import org.preesm.ui.scenario.editor.FileSelectionAdapter;
import org.preesm.ui.scenario.editor.Messages;
import org.preesm.ui.scenario.editor.PreesmAlgorithmTreeLabelProvider;
import org.preesm.ui.scenario.editor.ScenarioPage;

/**
 * Papify editor within the implementation editor.
 *
 * @author dmadronal
 */
public class PapifyPage extends ScenarioPage {

  /** Currently edited scenario. */
  private Scenario scenario = null;

  /** The check state listener. */
  private PapifyCheckStateListener checkStateListener = null;

  /** The table viewer. */

  // DM added this
  CheckboxTreeViewer                         peTreeViewer         = null;
  CheckboxTreeViewer                         actorTreeViewer      = null;
  PapiEventInfo                              papiEvents           = null;
  PapiConfigParser                           papiParser           = new PapiConfigParser();
  PapifyComponentListContentProvider2DMatrix peContentProvider    = null;
  PapifyEventListContentProvider2DMatrix     actorContentProvider = null;

  /**
   * Instantiates a new papify page.
   *
   * @param scenario
   *          the scenario
   * @param editor
   *          the editor
   * @param id
   *          the id
   * @param title
   *          the title
   */
  public PapifyPage(final Scenario scenario, final FormEditor editor, final String id, final String title) {
    super(editor, id, title);
    this.scenario = scenario;
  }

  /**
   * Initializes the display content.
   *
   * @param managedForm
   *          the managed form
   */
  @Override
  protected void createFormContent(final IManagedForm managedForm) {
    final ScrolledForm f = managedForm.getForm();
    f.setText(Messages.getString("Papify.title"));
    final Composite body = f.getBody();
    final GridLayout layout = new GridLayout(2, true);
    layout.verticalSpacing = 10;
    body.setLayout(layout);

    if (this.scenario.isProperlySet()) {

      // Papify file chooser section
      createFileSection(managedForm, Messages.getString("Papify.file"), Messages.getString("Papify.fileDescription"),
          Messages.getString("Papify.fileEdit"), this.scenario.getPapifyConfig().getXmlFileURL(),
          Messages.getString("Papify.fileBrowseTitle"), "xml");

      createPapifyPESection(managedForm, Messages.getString("Papify.titlePESection"),
          Messages.getString("Papify.descriptionPE"));

      createPapifyActorSection(managedForm, Messages.getString("Papify.titleActorSection"),
          Messages.getString("Papify.descriptionActor"));

      if (!this.scenario.getPapifyConfig().getXmlFileURL().equals("")) {
        final String xmlFullPath = getFullXmlPath(this.scenario.getPapifyConfig().getXmlFileURL());
        if (!xmlFullPath.equals("")) {
          parseXmlData(xmlFullPath);
        }
        if ((this.papiEvents != null) && !this.papiEvents.getComponents().isEmpty()) {

          scenario.getPapifyConfig().setPapiData(this.papiEvents);
          updateTables();
        } else {
          this.scenario.getPapifyConfig().clear();
          this.scenario.getPapifyConfig().setPapiData(this.papiEvents);
        }
        managedForm.refresh();
        managedForm.reflow(true);
      }
    } else {
      final FormToolkit toolkit = managedForm.getToolkit();
      final Label lbl = toolkit.createLabel(body,
          "Please properly set Algorithm and Architecture paths on the overview tab, then save, close and "
              + "reopen this file to enable other tabs.");
      lbl.setEnabled(true);
      body.setEnabled(false);
    }

  }

  /**
   * Creates a generic section.
   *
   * @param mform
   *          the mform
   * @param title
   *          the title
   * @param desc
   *          the desc
   * @param numColumns
   *          the num columns
   * @return the section
   */
  public Section createSection(final IManagedForm mform, final String title, final String desc, final int numColumns) {

    final ScrolledForm form = mform.getForm();
    final FormToolkit toolkit = mform.getToolkit();
    final Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TWISTIE
        | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.EXPANDED);
    section.setText(title);
    section.setDescription(desc);
    toolkit.createCompositeSeparator(section);
    return section;
  }

  /**
   * Creates a generic section.
   *
   * @param mform
   *          the mform
   * @param title
   *          the title
   * @param desc
   *          the desc
   * @param numColumns
   *          the num columns
   * @param gridData
   *          the grid data
   * @return the composite
   */
  public Composite createSection(final IManagedForm mform, final String title, final String desc, final int numColumns,
      final GridData gridData) {

    final ScrolledForm form = mform.getForm();
    final FormToolkit toolkit = mform.getToolkit();
    final Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TWISTIE
        | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.EXPANDED);
    section.setText(title);
    section.setDescription(desc);
    toolkit.createCompositeSeparator(section);
    final Composite client = toolkit.createComposite(section);
    final GridLayout layout = new GridLayout();
    layout.marginWidth = layout.marginHeight = 0;
    layout.numColumns = numColumns;
    client.setLayout(layout);
    section.setClient(client);
    section.addExpansionListener(new ExpansionAdapter() {
      @Override
      public void expansionStateChanged(final ExpansionEvent e) {
        form.reflow(false);
      }
    });
    section.setLayoutData(gridData);
    return client;
  }

  /**
   * Creates the section editing PAPIFY PE <--> PAPI_component(s) association.
   *
   * @param managedForm
   *          the managed form
   * @param title
   *          the title
   * @param desc
   *          the desc
   */
  private void createPapifyPESection(final IManagedForm managedForm, final String title, final String desc) {

    // Creates the section
    managedForm.getForm().setLayout(new FillLayout());
    final GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    final Composite container = createSection(managedForm, title, desc, 1, gridData);

    this.checkStateListener = new PapifyCheckStateListener(this.scenario);
    container.addPaintListener(this.checkStateListener);

    final FormToolkit toolkit = managedForm.getToolkit();

    addComponentCheckBoxTreeViewer(container, toolkit);

  }

  /**
   * Creates the section editing PAPIFY Actor <--> EventSet(s) association.
   *
   * @param managedForm
   *          the managed form
   * @param title
   *          the title
   * @param desc
   *          the desc
   */
  private void createPapifyActorSection(final IManagedForm managedForm, final String title, final String desc) {

    // Creates the section
    managedForm.getForm().setLayout(new FillLayout());
    final GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
    gridData.horizontalSpan = 2;
    gridData.grabExcessVerticalSpace = true;
    final Composite container = createSection(managedForm, title, desc, 2, gridData);

    final FormToolkit toolkit = managedForm.getToolkit();
    addEventCheckBoxTreeViewer(managedForm, container, toolkit);

  }

  /**
   * Function of the property listener used to transmit the dirty property.
   *
   * @param source
   *          the source
   * @param propId
   *          the prop id
   */
  @Override
  public void propertyChanged(final Object source, final int propId) {
    if ((source instanceof PapifyCheckStateListener) && (propId == IEditorPart.PROP_DIRTY)) {
      firePropertyChange(IEditorPart.PROP_DIRTY);
    }

  }

  /**
   * Creates a section to edit a file.
   *
   * @param mform
   *          form containing the section
   * @param title
   *          section title
   * @param desc
   *          description of the section
   * @param fileEdit
   *          text to display in text label
   * @param initValue
   *          initial value of Text
   * @param browseTitle
   *          title of file browser
   * @param fileExtension
   *          the file extension
   */
  private void createFileSection(final IManagedForm mform, final String title, final String desc, final String fileEdit,
      final String initValue, final String browseTitle, final String fileExtension) {

    final GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
    final Composite client = createSection(mform, title, desc, 1, gridData);
    final FormToolkit toolkit = mform.getToolkit();

    final GridData gd = new GridData();
    toolkit.createLabel(client, fileEdit);

    final Text text = toolkit.createText(client, initValue, SWT.SINGLE);
    text.setData(title);
    text.addModifyListener(e -> {
      final Text text1 = (Text) e.getSource();

      try {
        importData(text1);
      } catch (final Exception ex) {
        PreesmLogger.getLogger().log(Level.WARNING, "Could not importe Papi data from file '" + text1.getText() + "'",
            ex);
      }

    });

    text.addKeyListener(new KeyListener() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.keyCode == SWT.CR) {
          final Text text = (Text) e.getSource();
          try {
            importData(text);
          } catch (final Exception ex) {
            PreesmLogger.getLogger().log(Level.WARNING,
                "Could not importe Papi data from file '" + text.getText() + "'", ex);
          }
        }

      }

      @Override
      public void keyReleased(final KeyEvent e) {
        // nothing
      }

    });

    gd.widthHint = 400;
    text.setLayoutData(gd);

    final Button button = toolkit.createButton(client, Messages.getString("Overview.browse"), SWT.PUSH);
    final SelectionAdapter adapter = new FileSelectionAdapter(text, browseTitle, fileExtension);
    button.addSelectionListener(adapter);

    toolkit.paintBordersFor(client);
  }

  /**
   * Import data.
   *
   * @param text
   *          the text
   * @throws PreesmException
   *           the invalid model exception
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws CoreException
   *           the core exception
   */
  private void importData(final Text text) {

    final IWorkspace workspace = ResourcesPlugin.getWorkspace();

    WorkspaceUtils.updateWorkspace();

    final Path path = new Path(text.getText());

    if (workspace.getRoot().exists(path)) {
      final IFile file = workspace.getRoot().getFile(path);
      this.papiEvents = this.papiParser.parse(file.getLocation().toString());

      if (!text.getText().equals(this.scenario.getPapifyConfig().getXmlFileURL())
          && (this.papiEvents.getComponents() != null)) {

        PreesmLogger.getLogger().log(Level.INFO, "Loading Papi configuration from '" + text.getText() + "'");

        this.scenario.getPapifyConfig().clear();
        this.scenario.getPapifyConfig().setXmlFileURL(text.getText());
        this.scenario.getPapifyConfig().setPapiData(papiEvents);

        this.peTreeViewer.setInput(this.papiEvents);
        this.peContentProvider.setInput();

        this.checkStateListener.clearEvents();
        updateColumns();
        this.actorTreeViewer.setInput(this.scenario);
        firePropertyChange(IEditorPart.PROP_DIRTY);
      }
    } else {
      PreesmLogger.getLogger().log(Level.WARNING, "Could not locate file under '" + text.getText() + "'");
    }
  }

  /**
   * Get full xml path.
   *
   * @param xmlfile
   *          the xmlfile
   */
  private String getFullXmlPath(final String xmlfile) {

    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    String fullPath = "";

    WorkspaceUtils.updateWorkspace();

    final Path path = new Path(xmlfile);
    final IFile file = workspace.getRoot().getFile(path);

    if (file.exists()) {
      fullPath = file.getLocation().toString();
    }
    return fullPath;
  }

  /**
   * Parse xml data.
   *
   * @param xmlpath
   *          the xmlfile
   */
  private void parseXmlData(final String xmlfile) {

    this.papiEvents = this.papiParser.parse(xmlfile);
    scenario.getPapifyConfig().setPapiData(this.papiEvents);

  }

  /**
   * Update info.
   *
   * @param xmlpath
   *          the xmlfile
   */
  private void updateTables() {
    this.peTreeViewer.setInput(this.papiEvents);
    this.peContentProvider.setInput();
    this.checkStateListener.clearEvents();
    updateColumns();
    this.actorTreeViewer.setInput(this.scenario);
    this.checkStateListener.updateEvents();
  }

  /**
   * Adds a checkBoxTreeViewer to edit component association.
   *
   * @param parent
   *          the parent
   * @param toolkit
   *          the toolkit
   */
  private void addComponentCheckBoxTreeViewer(final Composite parent, final FormToolkit toolkit) {

    // Creating the tree view
    this.peTreeViewer = new CheckboxTreeViewer(toolkit.createTree(parent, SWT.NONE));

    this.peContentProvider = new PapifyComponentListContentProvider2DMatrix(this.scenario);

    peTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

    peTreeViewer.setContentProvider(this.peContentProvider);

    peTreeViewer.setLabelProvider(new LabelProvider());
    peTreeViewer.addCheckStateListener(this.checkStateListener);

    peTreeViewer.setUseHashlookup(true);

    peTreeViewer.getTree().setLinesVisible(true);
    peTreeViewer.getTree().setHeaderVisible(true);

    final TreeViewerColumn peViewerColumn = new TreeViewerColumn(peTreeViewer,
        SWT.CENTER | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL);
    final TreeColumn peColumn = peViewerColumn.getColumn();
    peColumn.setText("Component type \\ PE type");
    peColumn.setWidth(200);
    peViewerColumn.setLabelProvider(new ColumnLabelProvider());

    this.checkStateListener.setPropertyListener(this);
    this.peContentProvider.addCheckStateListener(this.checkStateListener);

    final Design design = this.scenario.getDesign();
    for (final Component columnLabel : design.getOperatorComponents()) {

      final TreeViewerColumn viewerColumn = new TreeViewerColumn(peTreeViewer, SWT.CENTER | SWT.CHECK);
      final TreeColumn column = viewerColumn.getColumn();

      column.setText(columnLabel.getVlnv().getName());
      column.setMoveable(true);
      column.setWidth(150);

      PapifyComponentListContentProvider2DMatrixES editingSupport = new PapifyComponentListContentProvider2DMatrixES(
          peTreeViewer, columnLabel, this.peContentProvider);

      viewerColumn.setLabelProvider(new PapifyComponentListContentProvider2DMatrixCLP(this.scenario, columnLabel));
      viewerColumn.setEditingSupport(editingSupport);
      this.peContentProvider.addEstatusSupport(editingSupport);

    }

    peTreeViewer.setInput(this.papiEvents);
    final GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
    gd.heightHint = 100;

    peTreeViewer.getTree().setLayoutData(gd);

    // Tree is refreshed in case of algorithm modifications
    parent.addPaintListener(checkStateListener);
  }

  /**
   * Adds a checkBoxTreeViewer to edit event association.
   *
   * @param managedForm
   *
   * @param parent
   *          the parent
   * @param toolkit
   *          the toolkit
   */
  private void addEventCheckBoxTreeViewer(final IManagedForm managedForm, final Composite parent,
      final FormToolkit toolkit) {

    // Creating the tree view
    final Tree treeViewerParent = toolkit.createTree(parent, SWT.CHECK);
    this.actorTreeViewer = new CheckboxTreeViewer(treeViewerParent);

    this.actorContentProvider = new PapifyEventListContentProvider2DMatrix(this.scenario);
    this.actorContentProvider.addCheckStateListener(this.checkStateListener);

    this.actorTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

    this.actorTreeViewer.setContentProvider(this.actorContentProvider);

    this.actorTreeViewer.setLabelProvider(new PreesmAlgorithmTreeLabelProvider());

    this.checkStateListener.setPropertyListener(this);
    this.actorTreeViewer.addCheckStateListener(this.checkStateListener);

    this.actorTreeViewer.setUseHashlookup(true);

    treeViewerParent.setLinesVisible(true);
    treeViewerParent.setHeaderVisible(true);

    final TreeViewerColumn actorViewerColumn = new TreeViewerColumn(this.actorTreeViewer, SWT.CENTER | SWT.CHECK);
    final TreeColumn peColumn = actorViewerColumn.getColumn();
    peColumn.setText("Actor name \\ Event name");
    peColumn.setWidth(200);
    actorViewerColumn.setLabelProvider(
        (new PapifyEventListContentProvider2DMatrixCLP(this.scenario, "First_column", this.checkStateListener)));

    this.actorTreeViewer.setInput(this.scenario);

    final GridData gd = new GridData(
        GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    gd.heightHint = 400;
    gd.widthHint = managedForm.getForm().getBody().getClientArea().width;
    treeViewerParent.setLayout(new GridLayout());
    treeViewerParent.setLayoutData(gd);

    // Tree is refreshed in case of algorithm modifications
    parent.addPaintListener(checkStateListener);
  }

  void updateColumns() {

    int counter = 0;
    for (TreeColumn column : this.actorTreeViewer.getTree().getColumns()) {
      if (counter != 0) {
        column.dispose();
      }
      counter++;
    }

    // The timing event
    final PapiEvent timingEvent = ScenarioUserFactory.createTimingEvent();

    final TreeViewerColumn viewerColumnTiming = new TreeViewerColumn(this.actorTreeViewer, SWT.CENTER | SWT.CHECK);
    final TreeColumn columnTiming = viewerColumnTiming.getColumn();

    columnTiming.setText(timingEvent.getName());
    columnTiming.setToolTipText(timingEvent.getDescription());
    columnTiming.setWidth(150);

    PapifyEventListContentProvider2DMatrixES editingSupportTiming = new PapifyEventListContentProvider2DMatrixES(
        this.actorTreeViewer, timingEvent.getName(), this.checkStateListener);

    viewerColumnTiming.setLabelProvider(
        new PapifyEventListContentProvider2DMatrixCLP(this.scenario, timingEvent.getName(), this.checkStateListener));
    viewerColumnTiming.setEditingSupport(editingSupportTiming);
    this.checkStateListener.addEstatusSupport(editingSupportTiming);

    for (PapiComponent oneComponent : this.papiEvents.getComponents().values()) {
      if (!oneComponent.getEventSets().isEmpty()) {
        for (PapiEventSet oneEventSet : oneComponent.getEventSets()) {
          for (PapiEvent oneEvent : oneEventSet.getEvents()) {
            if (oneEvent.getModifiers().isEmpty()) {
              final TreeViewerColumn viewerColumn = new TreeViewerColumn(this.actorTreeViewer, SWT.CENTER | SWT.CHECK);
              final TreeColumn column = viewerColumn.getColumn();

              column.setText(oneEvent.getName());
              column.setToolTipText(oneEvent.getDescription());
              column.setWidth(150);

              PapifyEventListContentProvider2DMatrixES editingSupport = new PapifyEventListContentProvider2DMatrixES(
                  this.actorTreeViewer, oneEvent.getName(), this.checkStateListener);

              viewerColumn.setLabelProvider(new PapifyEventListContentProvider2DMatrixCLP(this.scenario,
                  oneEvent.getName(), this.checkStateListener));
              viewerColumn.setEditingSupport(editingSupport);
              this.checkStateListener.addEstatusSupport(editingSupport);
            }
          }
        }
      }
    }
  }
}
