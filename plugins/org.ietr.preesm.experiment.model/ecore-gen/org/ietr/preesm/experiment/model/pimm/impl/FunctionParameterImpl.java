/**
 */
package org.ietr.preesm.experiment.model.pimm.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.ietr.preesm.experiment.model.pimm.Direction;
import org.ietr.preesm.experiment.model.pimm.FunctionParameter;
import org.ietr.preesm.experiment.model.pimm.PiMMPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Function Parameter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.impl.FunctionParameterImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.impl.FunctionParameterImpl#getDirection <em>Direction</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.impl.FunctionParameterImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.impl.FunctionParameterImpl#isIsConfigurationParameter <em>Is Configuration Parameter</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FunctionParameterImpl extends MinimalEObjectImpl.Container implements FunctionParameter {
  /**
   * The default value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected static final String NAME_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected String name = NAME_EDEFAULT;

  /**
   * The default value of the '{@link #getDirection() <em>Direction</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDirection()
   * @generated
   * @ordered
   */
  protected static final Direction DIRECTION_EDEFAULT = Direction.IN;

  /**
   * The cached value of the '{@link #getDirection() <em>Direction</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDirection()
   * @generated
   * @ordered
   */
  protected Direction direction = DIRECTION_EDEFAULT;

  /**
   * The default value of the '{@link #getType() <em>Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getType()
   * @generated
   * @ordered
   */
  protected static final String TYPE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getType()
   * @generated
   * @ordered
   */
  protected String type = TYPE_EDEFAULT;

  /**
   * The default value of the '{@link #isIsConfigurationParameter() <em>Is Configuration Parameter</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isIsConfigurationParameter()
   * @generated
   * @ordered
   */
  protected static final boolean IS_CONFIGURATION_PARAMETER_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isIsConfigurationParameter() <em>Is Configuration Parameter</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isIsConfigurationParameter()
   * @generated
   * @ordered
   */
  protected boolean isConfigurationParameter = IS_CONFIGURATION_PARAMETER_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected FunctionParameterImpl() {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass() {
    return PiMMPackage.Literals.FUNCTION_PARAMETER;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getName() {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setName(String newName) {
    String oldName = name;
    name = newName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PiMMPackage.FUNCTION_PARAMETER__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setDirection(Direction newDirection) {
    Direction oldDirection = direction;
    direction = newDirection == null ? DIRECTION_EDEFAULT : newDirection;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PiMMPackage.FUNCTION_PARAMETER__DIRECTION, oldDirection, direction));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getType() {
    return type;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setType(String newType) {
    String oldType = type;
    type = newType;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PiMMPackage.FUNCTION_PARAMETER__TYPE, oldType, type));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isIsConfigurationParameter() {
    return isConfigurationParameter;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setIsConfigurationParameter(boolean newIsConfigurationParameter) {
    boolean oldIsConfigurationParameter = isConfigurationParameter;
    isConfigurationParameter = newIsConfigurationParameter;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, PiMMPackage.FUNCTION_PARAMETER__IS_CONFIGURATION_PARAMETER, oldIsConfigurationParameter, isConfigurationParameter));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType) {
    switch (featureID) {
      case PiMMPackage.FUNCTION_PARAMETER__NAME:
        return getName();
      case PiMMPackage.FUNCTION_PARAMETER__DIRECTION:
        return getDirection();
      case PiMMPackage.FUNCTION_PARAMETER__TYPE:
        return getType();
      case PiMMPackage.FUNCTION_PARAMETER__IS_CONFIGURATION_PARAMETER:
        return isIsConfigurationParameter();
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
      case PiMMPackage.FUNCTION_PARAMETER__NAME:
        setName((String)newValue);
        return;
      case PiMMPackage.FUNCTION_PARAMETER__DIRECTION:
        setDirection((Direction)newValue);
        return;
      case PiMMPackage.FUNCTION_PARAMETER__TYPE:
        setType((String)newValue);
        return;
      case PiMMPackage.FUNCTION_PARAMETER__IS_CONFIGURATION_PARAMETER:
        setIsConfigurationParameter((Boolean)newValue);
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
      case PiMMPackage.FUNCTION_PARAMETER__NAME:
        setName(NAME_EDEFAULT);
        return;
      case PiMMPackage.FUNCTION_PARAMETER__DIRECTION:
        setDirection(DIRECTION_EDEFAULT);
        return;
      case PiMMPackage.FUNCTION_PARAMETER__TYPE:
        setType(TYPE_EDEFAULT);
        return;
      case PiMMPackage.FUNCTION_PARAMETER__IS_CONFIGURATION_PARAMETER:
        setIsConfigurationParameter(IS_CONFIGURATION_PARAMETER_EDEFAULT);
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
      case PiMMPackage.FUNCTION_PARAMETER__NAME:
        return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
      case PiMMPackage.FUNCTION_PARAMETER__DIRECTION:
        return direction != DIRECTION_EDEFAULT;
      case PiMMPackage.FUNCTION_PARAMETER__TYPE:
        return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
      case PiMMPackage.FUNCTION_PARAMETER__IS_CONFIGURATION_PARAMETER:
        return isConfigurationParameter != IS_CONFIGURATION_PARAMETER_EDEFAULT;
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString() {
    if (eIsProxy()) return super.toString();

    StringBuilder result = new StringBuilder(super.toString());
    result.append(" (name: ");
    result.append(name);
    result.append(", direction: ");
    result.append(direction);
    result.append(", type: ");
    result.append(type);
    result.append(", isConfigurationParameter: ");
    result.append(isConfigurationParameter);
    result.append(')');
    return result.toString();
  }

} //FunctionParameterImpl
