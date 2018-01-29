/**
 */
package org.ietr.preesm.experiment.model.pimm;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Data Output Port</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.ietr.preesm.experiment.model.pimm.DataOutputPort#getOutgoingFifo <em>Outgoing Fifo</em>}</li>
 * </ul>
 *
 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getDataOutputPort()
 * @model
 * @generated
 */
public interface DataOutputPort extends DataPort {
  /**
   * Returns the value of the '<em><b>Outgoing Fifo</b></em>' reference. It is bidirectional and its opposite is
   * '{@link org.ietr.preesm.experiment.model.pimm.Fifo#getSourcePort <em>Source Port</em>}'. <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Outgoing Fifo</em>' reference isn't clear, there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   *
   * @return the value of the '<em>Outgoing Fifo</em>' reference.
   * @see #setOutgoingFifo(Fifo)
   * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getDataOutputPort_OutgoingFifo()
   * @see org.ietr.preesm.experiment.model.pimm.Fifo#getSourcePort
   * @model opposite="sourcePort"
   * @generated
   */
  Fifo getOutgoingFifo();

  /**
   * Sets the value of the '{@link org.ietr.preesm.experiment.model.pimm.DataOutputPort#getOutgoingFifo <em>Outgoing Fifo</em>}' reference. <!-- begin-user-doc
   * --> <!-- end-user-doc -->
   *
   * @param value
   *          the new value of the '<em>Outgoing Fifo</em>' reference.
   * @see #getOutgoingFifo()
   * @generated
   */
  void setOutgoingFifo(Fifo value);

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @model kind="operation" required="true" annotation="http://www.eclipse.org/emf/2002/GenModel body='return PortKind.DATA_OUTPUT;'"
   * @generated
   */
  @Override
  PortKind getKind();

} // DataOutputPort
