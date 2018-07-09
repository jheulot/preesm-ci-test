/**
 */
package org.ietr.preesm.experiment.model.pimm.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.PiMMPackage;
import org.ietr.preesm.experiment.model.pimm.PortKind;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Input Port</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.impl.DataInputPortImpl#getIncomingFifo <em>Incoming Fifo</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataInputPortImpl extends DataPortImpl implements DataInputPort {
  /**
   * The cached value of the '{@link #getIncomingFifo() <em>Incoming Fifo</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getIncomingFifo()
   * @generated
   * @ordered
   */
  protected Fifo incomingFifo;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected DataInputPortImpl() {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass() {
    return PiMMPackage.Literals.DATA_INPUT_PORT;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Fifo getIncomingFifo() {
    if (incomingFifo != null && incomingFifo.eIsProxy()) {
      InternalEObject oldIncomingFifo = (InternalEObject)incomingFifo;
      incomingFifo = (Fifo)eResolveProxy(oldIncomingFifo);
      if (incomingFifo != oldIncomingFifo) {
        if (eNotificationRequired())
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, PiMMPackage.DATA_INPUT_PORT__INCOMING_FIFO, oldIncomingFifo, incomingFifo));
      }
    }
    return incomingFifo;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Fifo basicGetIncomingFifo() {
    return incomingFifo;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetIncomingFifo(Fifo newIncomingFifo, NotificationChain msgs) {
    Fifo oldIncomingFifo = incomingFifo;
    incomingFifo = newIncomingFifo;
    if (eNotificationRequired()) {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PiMMPackage.DATA_INPUT_PORT__INCOMING_FIFO, oldIncomingFifo, newIncomingFifo);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setIncomingFifo(Fifo newIncomingFifo) {
    if (newIncomingFifo != incomingFifo) {
      NotificationChain msgs = null;
      if (incomingFifo != null)
        msgs = ((InternalEObject)incomingFifo).eInverseRemove(this, PiMMPackage.FIFO__TARGET_PORT, Fifo.class, msgs);
      if (newIncomingFifo != null)
        msgs = ((InternalEObject)newIncomingFifo).eInverseAdd(this, PiMMPackage.FIFO__TARGET_PORT, Fifo.class, msgs);
      msgs = basicSetIncomingFifo(newIncomingFifo, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PiMMPackage.DATA_INPUT_PORT__INCOMING_FIFO, newIncomingFifo, newIncomingFifo));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PortKind getKind() {
    return PortKind.DATA_INPUT;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Fifo getFifo() {
    return this.getIncomingFifo();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
    switch (featureID) {
      case PiMMPackage.DATA_INPUT_PORT__INCOMING_FIFO:
        if (incomingFifo != null)
          msgs = ((InternalEObject)incomingFifo).eInverseRemove(this, PiMMPackage.FIFO__TARGET_PORT, Fifo.class, msgs);
        return basicSetIncomingFifo((Fifo)otherEnd, msgs);
    }
    return super.eInverseAdd(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
    switch (featureID) {
      case PiMMPackage.DATA_INPUT_PORT__INCOMING_FIFO:
        return basicSetIncomingFifo(null, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType) {
    switch (featureID) {
      case PiMMPackage.DATA_INPUT_PORT__INCOMING_FIFO:
        if (resolve) return getIncomingFifo();
        return basicGetIncomingFifo();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue) {
    switch (featureID) {
      case PiMMPackage.DATA_INPUT_PORT__INCOMING_FIFO:
        setIncomingFifo((Fifo)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID) {
    switch (featureID) {
      case PiMMPackage.DATA_INPUT_PORT__INCOMING_FIFO:
        setIncomingFifo((Fifo)null);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID) {
    switch (featureID) {
      case PiMMPackage.DATA_INPUT_PORT__INCOMING_FIFO:
        return incomingFifo != null;
    }
    return super.eIsSet(featureID);
  }

} //DataInputPortImpl
