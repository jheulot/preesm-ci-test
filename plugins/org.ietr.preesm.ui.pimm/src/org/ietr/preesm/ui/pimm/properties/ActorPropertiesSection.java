/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2013 - 2018) :
 *
 * Alexandre Honorat <ahonorat@insa-rennes.fr> (2017)
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Julien Heulot <julien.heulot@insa-rennes.fr> (2013)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2015)
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
package org.ietr.preesm.ui.pimm.properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.features.context.impl.LayoutContext;
import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.platform.GFPropertySection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.ietr.preesm.experiment.model.expression.ExpressionEvaluationException;
import org.ietr.preesm.experiment.model.expression.ExpressionEvaluator;
import org.ietr.preesm.experiment.model.pimm.Actor;
import org.ietr.preesm.experiment.model.pimm.CHeaderRefinement;
import org.ietr.preesm.experiment.model.pimm.ExecutableActor;
import org.ietr.preesm.experiment.model.pimm.Expression;
import org.ietr.preesm.experiment.model.pimm.PeriodicElement;
import org.ietr.preesm.experiment.model.pimm.Refinement;
import org.ietr.preesm.experiment.model.pimm.util.PrototypeFormatter;
import org.ietr.preesm.ui.pimm.features.ClearActorMemoryScriptFeature;
import org.ietr.preesm.ui.pimm.features.ClearActorRefinementFeature;
import org.ietr.preesm.ui.pimm.features.OpenMemoryScriptFeature;
import org.ietr.preesm.ui.pimm.features.OpenRefinementFeature;
import org.ietr.preesm.ui.pimm.features.SetActorMemoryScriptFeature;
import org.ietr.preesm.ui.pimm.features.SetActorRefinementFeature;

/**
 * Properties Section used for Actors.
 *
 * @author jheulot
 */
public class ActorPropertiesSection extends GFPropertySection implements ITabbedPropertyConstants {

  /** Items of the {@link ActorPropertiesSection}. */
  private Composite composite;

  /** The lbl name. */
  private CLabel lblName;

  /** The txt name obj. */
  private Text txtNameObj;

  /** The lbl refinement. */
  private CLabel lblRefinement;

  /** The lbl refinement obj. */
  private CLabel lblRefinementObj;

  /** The lbl refinement view. */
  private CLabel lblRefinementView;

  /** The but refinement clear. */
  private Button butRefinementClear;

  /** The but refinement edit. */
  private Button butRefinementBrowse;

  /** The but refinement open. */
  private Button butRefinementOpen;

  /** The lbl memory script. */
  private CLabel lblMemoryScript;

  /** The lbl memory script obj. */
  private CLabel lblMemoryScriptObj;

  /** The but memory script clear. */
  private Button butMemoryScriptClear;

  /** The but memory script edit. */
  private Button butMemoryScriptBrowse;

  /** The but memory script open. */
  private Button butMemoryScriptOpen;

  /** The lbl for the actor period. */
  private CLabel lblPeriod;

  /** The txt for the actor period. */
  private Text txtPeriod;

  /** The lbl value. */
  private CLabel lblPeriodValue;

  /** The lbl value obj. */
  private CLabel lblPeriodValueObj;

  /** The first column width. */
  private final int FIRST_COLUMN_WIDTH = 100;

  /*
   * (non-Javadoc)
   *
   * @see
   * org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite,
   * org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
   */
  @Override
  public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage) {

    super.createControls(parent, tabbedPropertySheetPage);

    final TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
    this.composite = factory.createFlatFormComposite(parent);

    FormData data;

    /**** NAME ****/
    this.txtNameObj = factory.createText(this.composite, "");
    data = new FormData();
    data.left = new FormAttachment(0, this.FIRST_COLUMN_WIDTH);
    data.right = new FormAttachment(50, 0);
    this.txtNameObj.setLayoutData(data);
    this.txtNameObj.setEnabled(true);

    this.lblName = factory.createCLabel(this.composite, "Name:");
    data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(this.txtNameObj, -ITabbedPropertyConstants.HSPACE);
    this.lblName.setLayoutData(data);

    /*** Name box listener ***/
    this.txtNameObj.addModifyListener(e -> {
      final PictogramElement pe = getSelectedPictogramElement();
      if (pe != null) {
        final EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
        if (bo == null) {
          return;
        }

        if (bo instanceof ExecutableActor) {
          final ExecutableActor actor = (ExecutableActor) bo;
          if (ActorPropertiesSection.this.txtNameObj.getText().compareTo(actor.getName()) != 0) {
            setNewName(actor, ActorPropertiesSection.this.txtNameObj.getText());
            getDiagramTypeProvider().getFeatureProvider().updateIfPossible(new UpdateContext(pe));
            getDiagramTypeProvider().getFeatureProvider().layoutIfPossible(new LayoutContext(pe));
          }
        } // end Actor
      }
    });

    /**
     * Refinement
     */
    createRefinementControl(factory, this.composite);

    /**
     * Memory script
     */
    createMemoryScriptControl(factory, this.composite);

    /**** Period ****/
    this.txtPeriod = factory.createText(this.composite, "0");
    data = new FormData();
    data.left = new FormAttachment(0, this.FIRST_COLUMN_WIDTH);
    data.right = new FormAttachment(50, 0);
    data.top = new FormAttachment(lblMemoryScript);
    this.txtPeriod.setLayoutData(data);
    this.txtPeriod.setEnabled(true);
    this.txtPeriod.setToolTipText("Enter an expression if periodic, or 0 if aperiodic.");

    this.lblPeriod = factory.createCLabel(this.composite, "Period expression:");
    data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(this.txtPeriod, -ITabbedPropertyConstants.HSPACE);
    data.top = new FormAttachment(lblMemoryScript);
    this.lblPeriod.setLayoutData(data);

    /*** Period box listener ***/
    this.txtPeriod.addModifyListener(e -> {
      final PictogramElement pe = getSelectedPictogramElement();
      if (pe != null) {
        final EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
        if (bo == null) {
          return;
        }

        if (bo instanceof PeriodicElement) {
          final PeriodicElement periodEl = (PeriodicElement) bo;
          Expression periodicExp = periodEl.getExpression();
          String strPeriod = ActorPropertiesSection.this.txtPeriod.getText();
          if (strPeriod.compareTo(periodicExp.getExpressionString()) != 0) {
            setNewPeriod(periodicExp, strPeriod);
          }
        } // end PeriodicElement
      }
    });

    this.lblPeriodValue = factory.createCLabel(this.composite, "0");
    data = new FormData();
    data.left = new FormAttachment(0, this.FIRST_COLUMN_WIDTH);
    data.right = new FormAttachment(50, 0);
    data.top = new FormAttachment(lblPeriod);
    this.lblPeriodValue.setLayoutData(data);

    this.lblPeriodValueObj = factory.createCLabel(this.composite, "Period value:");
    data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(this.lblPeriodValue, -ITabbedPropertyConstants.HSPACE);
    data.top = new FormAttachment(lblPeriod);
    this.lblPeriodValueObj.setLayoutData(data);

  }

  /**
   * Create the part responsible for editing the refinement of the actor.
   *
   * @param factory
   *          the factory
   * @param composite
   *          the composite
   */
  protected void createRefinementControl(final TabbedPropertySheetWidgetFactory factory, final Composite composite) {

    /*** Clear Button ***/
    this.butRefinementClear = factory.createButton(composite, "Clear", SWT.PUSH);
    FormData data = new FormData();
    data.left = new FormAttachment(100, -100);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(this.txtNameObj);
    this.butRefinementClear.setLayoutData(data);
    this.butRefinementClear.setEnabled(true);

    /*** Edit Button ***/
    this.butRefinementBrowse = factory.createButton(composite, "Browse", SWT.PUSH);
    data = new FormData();
    data.left = new FormAttachment(100, -205);
    data.right = new FormAttachment(100, -105);
    data.top = new FormAttachment(this.txtNameObj);
    this.butRefinementBrowse.setLayoutData(data);
    this.butRefinementBrowse.setEnabled(true);

    /*** Open Button ***/
    this.butRefinementOpen = factory.createButton(composite, "Open", SWT.PUSH);
    data = new FormData();
    data.left = new FormAttachment(100, -310);
    data.right = new FormAttachment(100, -210);
    data.top = new FormAttachment(this.txtNameObj);
    this.butRefinementOpen.setLayoutData(data);
    this.butRefinementOpen.setEnabled(true);

    /**** Refinement ****/
    this.lblRefinementObj = factory.createCLabel(composite, "");
    data = new FormData();
    data.left = new FormAttachment(0, this.FIRST_COLUMN_WIDTH);
    data.right = new FormAttachment(this.butRefinementBrowse, 0);
    data.top = new FormAttachment(this.txtNameObj);
    this.lblRefinementObj.setLayoutData(data);
    this.lblRefinementObj.setEnabled(true);

    this.lblRefinement = factory.createCLabel(composite, "Refinement:");
    data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(this.lblRefinementObj, -ITabbedPropertyConstants.HSPACE);
    data.top = new FormAttachment(this.txtNameObj);
    this.lblRefinement.setLayoutData(data);

    /**** Refinement view ****/
    this.lblRefinementView = factory.createCLabel(composite, "loop: \n init:");
    data = new FormData();
    data.left = new FormAttachment(0, this.FIRST_COLUMN_WIDTH);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(this.lblRefinement);
    data.height = 30;
    this.lblRefinementView.setLayoutData(data);
    this.lblRefinementView.setEnabled(true);

    /*** Clear Button Listener ***/
    this.butRefinementClear.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        final PictogramElement[] pes = new PictogramElement[1];
        pes[0] = getSelectedPictogramElement();

        final CustomContext context = new CustomContext(pes);
        final ICustomFeature[] clearRefinementFeature = getDiagramTypeProvider().getFeatureProvider()
            .getCustomFeatures(context);

        for (final ICustomFeature feature : clearRefinementFeature) {
          if (feature instanceof ClearActorRefinementFeature) {
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(feature, context);
            final LayoutContext contextLayout = new LayoutContext(getSelectedPictogramElement());
            final ILayoutFeature layoutFeature = getDiagramTypeProvider().getFeatureProvider()
                .getLayoutFeature(contextLayout);
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(layoutFeature, contextLayout);
          }
        }

        refresh();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
      }

    });

    /*** Edit Button Listener ***/
    this.butRefinementBrowse.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        final PictogramElement[] pes = new PictogramElement[1];
        pes[0] = getSelectedPictogramElement();

        final CustomContext context = new CustomContext(pes);
        final ICustomFeature[] setRefinementFeature = getDiagramTypeProvider().getFeatureProvider()
            .getCustomFeatures(context);

        for (final ICustomFeature feature : setRefinementFeature) {
          if (feature instanceof SetActorRefinementFeature) {
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(feature, context);
            final LayoutContext contextLayout = new LayoutContext(getSelectedPictogramElement());
            final ILayoutFeature layoutFeature = getDiagramTypeProvider().getFeatureProvider()
                .getLayoutFeature(contextLayout);
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(layoutFeature, contextLayout);
          }
        }

        refresh();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
      }

    });

    /*** Open Button Listener ***/
    this.butRefinementOpen.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        final PictogramElement[] pes = new PictogramElement[1];
        pes[0] = getSelectedPictogramElement();

        final CustomContext context = new CustomContext(pes);
        final ICustomFeature[] openRefinementFeature = getDiagramTypeProvider().getFeatureProvider()
            .getCustomFeatures(context);

        for (final ICustomFeature feature : openRefinementFeature) {
          if (feature instanceof OpenRefinementFeature) {
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(feature, context);
            final LayoutContext contextLayout = new LayoutContext(getSelectedPictogramElement());
            final ILayoutFeature layoutFeature = getDiagramTypeProvider().getFeatureProvider()
                .getLayoutFeature(contextLayout);
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(layoutFeature, contextLayout);
          }
        }

        refresh();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
      }

    });

  }

  /**
   * Creates the memory script control.
   *
   * @param factory
   *          the factory
   * @param composite
   *          the composite
   */
  protected void createMemoryScriptControl(final TabbedPropertySheetWidgetFactory factory, final Composite composite) {
    /*** Clear Button ***/
    this.butMemoryScriptClear = factory.createButton(composite, "Clear", SWT.PUSH);
    FormData data = new FormData();
    data.left = new FormAttachment(100, -100);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(this.lblRefinementView);
    this.butMemoryScriptClear.setLayoutData(data);
    this.butMemoryScriptClear.setEnabled(true);

    /*** Edit Button ***/
    this.butMemoryScriptBrowse = factory.createButton(composite, "Browse", SWT.PUSH);
    data = new FormData();
    data.left = new FormAttachment(100, -205);
    data.right = new FormAttachment(100, -105);
    data.top = new FormAttachment(this.lblRefinementView);
    this.butMemoryScriptBrowse.setLayoutData(data);
    this.butMemoryScriptBrowse.setEnabled(true);

    /*** Open Button ***/
    this.butMemoryScriptOpen = factory.createButton(composite, "Open", SWT.PUSH);
    data = new FormData();
    data.left = new FormAttachment(100, -310);
    data.right = new FormAttachment(100, -210);
    data.top = new FormAttachment(this.lblRefinementView);
    this.butMemoryScriptOpen.setLayoutData(data);
    this.butMemoryScriptOpen.setEnabled(true);

    /**** Memory Script ****/
    this.lblMemoryScriptObj = factory.createCLabel(composite, "");
    data = new FormData();
    data.left = new FormAttachment(0, this.FIRST_COLUMN_WIDTH);
    data.right = new FormAttachment(this.butMemoryScriptBrowse, 0);
    data.top = new FormAttachment(this.lblRefinementView);
    this.lblMemoryScriptObj.setLayoutData(data);
    this.lblMemoryScriptObj.setEnabled(true);

    this.lblMemoryScript = factory.createCLabel(composite, "Memory script:");
    data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(this.lblMemoryScriptObj, -ITabbedPropertyConstants.HSPACE);
    data.top = new FormAttachment(this.lblRefinementView);
    this.lblMemoryScript.setLayoutData(data);

    /*** Clear Button Listener ***/
    this.butMemoryScriptClear.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        final PictogramElement[] pes = new PictogramElement[1];
        pes[0] = getSelectedPictogramElement();

        final CustomContext context = new CustomContext(pes);
        final ICustomFeature[] clearMemoryScriptFeature = getDiagramTypeProvider().getFeatureProvider()
            .getCustomFeatures(context);

        for (final ICustomFeature feature : clearMemoryScriptFeature) {
          if (feature instanceof ClearActorMemoryScriptFeature) {
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(feature, context);
            final LayoutContext contextLayout = new LayoutContext(getSelectedPictogramElement());
            final ILayoutFeature layoutFeature = getDiagramTypeProvider().getFeatureProvider()
                .getLayoutFeature(contextLayout);
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(layoutFeature, contextLayout);
          }
        }

        refresh();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
      }

    });

    /*** Edit Button Listener ***/
    this.butMemoryScriptBrowse.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        final PictogramElement[] pes = new PictogramElement[1];
        pes[0] = getSelectedPictogramElement();

        final CustomContext context = new CustomContext(pes);
        final ICustomFeature[] setMemoryScriptFeature = getDiagramTypeProvider().getFeatureProvider()
            .getCustomFeatures(context);

        for (final ICustomFeature feature : setMemoryScriptFeature) {
          if (feature instanceof SetActorMemoryScriptFeature) {
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(feature, context);
            final LayoutContext contextLayout = new LayoutContext(getSelectedPictogramElement());
            final ILayoutFeature layoutFeature = getDiagramTypeProvider().getFeatureProvider()
                .getLayoutFeature(contextLayout);
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(layoutFeature, contextLayout);
          }
        }

        refresh();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
      }

    });

    /*** Open Button Listener ***/
    this.butMemoryScriptOpen.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        final PictogramElement[] pes = new PictogramElement[1];
        pes[0] = getSelectedPictogramElement();

        final CustomContext context = new CustomContext(pes);
        final ICustomFeature[] openMemoryScriptFeature = getDiagramTypeProvider().getFeatureProvider()
            .getCustomFeatures(context);

        for (final ICustomFeature feature : openMemoryScriptFeature) {
          if (feature instanceof OpenMemoryScriptFeature) {
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(feature, context);
            final LayoutContext contextLayout = new LayoutContext(getSelectedPictogramElement());
            final ILayoutFeature layoutFeature = getDiagramTypeProvider().getFeatureProvider()
                .getLayoutFeature(contextLayout);
            getDiagramTypeProvider().getDiagramBehavior().executeFeature(layoutFeature, contextLayout);
          }
        }

        refresh();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
      }

    });

  }

  /**
   * Safely set a new name to the {@link Actor}.
   *
   * @param actor
   *          {@link Actor} to set
   * @param value
   *          String value
   */
  private void setNewName(final ExecutableActor actor, final String value) {
    final TransactionalEditingDomain editingDomain = getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();
    editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

      @Override
      protected void doExecute() {
        actor.setName(value);
      }
    });
  }

  /**
   * Safely set a new name to the {@link Actor}.
   *
   * @param periodicEl
   *          {@link Actor} to set
   * @param value
   *          String value
   */
  private void setNewPeriod(final Expression periodicExp, final String value) {
    final TransactionalEditingDomain editingDomain = getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();
    editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

      @Override
      protected void doExecute() {
        periodicExp.setExpressionString(value);
      }
    });
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
   */
  @Override
  public void refresh() {
    final PictogramElement pe = getSelectedPictogramElement();

    if (pe != null) {
      final Object bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
      if (bo == null) {
        return;
      }

      if (bo instanceof ExecutableActor) {
        final ExecutableActor exexcutableActor = (ExecutableActor) bo;
        this.txtNameObj.setEnabled(false);
        if ((exexcutableActor.getName() == null) && (!this.txtNameObj.getText().isEmpty())) {
          this.txtNameObj.setText("");
        } else if (this.txtNameObj.getText().compareTo(exexcutableActor.getName()) != 0) {
          this.txtNameObj.setText(exexcutableActor.getName());
        }
        this.txtNameObj.setEnabled(true);

        if (bo instanceof PeriodicElement) {
          final Point selelection = this.txtPeriod.getSelection();
          this.txtPeriod.setEnabled(false);

          final PeriodicElement periodEl = (PeriodicElement) bo;
          Expression periodicExp = periodEl.getExpression();
          String newStrExp = ActorPropertiesSection.this.txtPeriod.getText();
          if (newStrExp.compareTo(periodicExp.getExpressionString()) != 0) {
            if (newStrExp.isEmpty()) {
              newStrExp = "0";
            }
            setNewPeriod(periodicExp, newStrExp);
          }
          final boolean expressionHasFocus = this.txtPeriod.isFocusControl();
          try {
            // try out evaluating the expression
            final long evaluate = ExpressionEvaluator.evaluate(periodicExp);
            // if evaluation went well, just write the result
            if (evaluate < 0) {
              throw new IllegalArgumentException(
                  "The period must be positive if any, or 0 if aperiodic. Negative numbers not allowed.");
            }
            this.lblPeriodValueObj.setText(Long.toString(evaluate));
            this.txtPeriod.setBackground(new Color(null, 255, 255, 255));
          } catch (final ExpressionEvaluationException | IllegalArgumentException e) {
            // otherwise print error message and put red background
            this.lblPeriodValueObj.setText("Error : " + e.getMessage());
            this.txtPeriod.setBackground(new Color(null, 240, 150, 150));
          }

          if (expressionHasFocus) {
            this.txtPeriod.setFocus();
            this.txtPeriod.setSelection(selelection);
          }

          this.txtPeriod.setEnabled(true);
        } // end PeriodicElement

        if (bo instanceof Actor) {
          final Actor actor = (Actor) bo;
          final Refinement refinement = actor.getRefinement();
          if ((refinement == null) || (refinement.getFilePath() == null)) {
            this.lblRefinementObj.setText("(none)");
            this.lblRefinementView.setText("(none)");
            this.butRefinementClear.setEnabled(false);
            this.butRefinementBrowse.setEnabled(true);
            this.butRefinementOpen.setEnabled(false);
          } else {
            final IPath path = refinement.getFilePath();
            final String text = path.lastSegment();
            this.lblRefinementObj.setText(text);

            String view = "";

            if (refinement instanceof CHeaderRefinement) {
              String tooltip = "";
              // Max length
              int maxLength = (int) ((this.composite.getBounds().width - this.FIRST_COLUMN_WIDTH) * 0.17);
              maxLength = Math.max(maxLength, 40);
              if (((CHeaderRefinement) refinement).getLoopPrototype() != null) {
                final String loop = "loop: "
                    + PrototypeFormatter.format(((CHeaderRefinement) refinement).getLoopPrototype());
                view += (loop.length() <= maxLength) ? loop : loop.substring(0, maxLength) + "...";
                tooltip = loop;
              }
              if (((CHeaderRefinement) refinement).getInitPrototype() != null) {
                final String init = "\ninit: "
                    + PrototypeFormatter.format(((CHeaderRefinement) refinement).getInitPrototype());
                view += (init.length() <= maxLength) ? init : init.substring(0, maxLength) + "...";
                ;
                tooltip += init;
              }
              this.lblRefinementView.setToolTipText(tooltip);
            }
            this.lblRefinementView.setText(view);
            this.butRefinementClear.setEnabled(true);
            this.butRefinementBrowse.setEnabled(true);
            this.butRefinementOpen.setEnabled(true);
          }

          if (actor.getMemoryScriptPath() == null) {
            this.lblMemoryScriptObj.setText("(none)");
            this.butMemoryScriptClear.setEnabled(false);
            this.butMemoryScriptBrowse.setEnabled(true);
            this.butMemoryScriptOpen.setEnabled(false);
          } else {
            final IPath path = actor.getMemoryScriptPath();
            final String text = path.lastSegment();

            this.lblMemoryScriptObj.setText(text);
            this.butMemoryScriptClear.setEnabled(true);
            this.butMemoryScriptBrowse.setEnabled(true);
            this.butMemoryScriptOpen.setEnabled(true);
          }
          this.lblRefinement.setVisible(true);
          this.lblRefinementObj.setVisible(true);
          this.lblRefinementView.setVisible(true);
          this.butRefinementClear.setVisible(true);
          this.butRefinementBrowse.setVisible(true);
          this.butRefinementOpen.setVisible(true);
          this.lblMemoryScript.setVisible(true);
          this.lblMemoryScriptObj.setVisible(true);
          this.butMemoryScriptClear.setVisible(true);
          this.butMemoryScriptBrowse.setVisible(true);
          this.butMemoryScriptOpen.setVisible(true);
        } else {
          this.lblRefinement.setVisible(false);
          this.lblRefinementObj.setVisible(false);
          this.lblRefinementView.setVisible(false);
          this.butRefinementClear.setVisible(false);
          this.butRefinementBrowse.setVisible(false);
          this.butRefinementOpen.setVisible(false);
          this.lblMemoryScript.setVisible(false);
          this.lblMemoryScriptObj.setVisible(false);
          this.butMemoryScriptClear.setVisible(false);
          this.butMemoryScriptBrowse.setVisible(false);
          this.butMemoryScriptOpen.setVisible(false);
        }

      } // end ExecutableActor

    }
  }
}
