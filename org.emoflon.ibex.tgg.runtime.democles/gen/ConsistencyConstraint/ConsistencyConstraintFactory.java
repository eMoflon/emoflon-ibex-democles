/**
 */
package ConsistencyConstraint;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see ConsistencyConstraint.ConsistencyConstraintPackage
 * @generated
 */
public class ConsistencyConstraintFactory extends EFactoryImpl {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final ConsistencyConstraintFactory eINSTANCE = init();

	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ConsistencyConstraintFactory init() {
		try {
			ConsistencyConstraintFactory theConsistencyConstraintFactory = (ConsistencyConstraintFactory)EPackage.Registry.INSTANCE.getEFactory(ConsistencyConstraintPackage.eNS_URI);
			if (theConsistencyConstraintFactory != null) {
				return theConsistencyConstraintFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ConsistencyConstraintFactory();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ConsistencyConstraintFactory() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case ConsistencyConstraintPackage.CONSISTENCY: return createConsistency();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Consistency createConsistency() {
		Consistency consistency = new Consistency();
		return consistency;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ConsistencyConstraintPackage getConsistencyConstraintPackage() {
		return (ConsistencyConstraintPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ConsistencyConstraintPackage getPackage() {
		return ConsistencyConstraintPackage.eINSTANCE;
	}

} //ConsistencyConstraintFactory
