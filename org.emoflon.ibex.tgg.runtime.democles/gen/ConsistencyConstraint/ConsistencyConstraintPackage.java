/**
 */
package ConsistencyConstraint;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.gervarro.democles.specification.emf.SpecificationPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see ConsistencyConstraint.ConsistencyConstraintFactory
 * @model kind="package"
 * @generated
 */
public class ConsistencyConstraintPackage extends EPackageImpl {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNAME = "ConsistencyConstraint";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_URI = "platform:/resource/org.emoflon.ibex.tgg.runtime.democles/model/ConsistencyConstraint.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_PREFIX = "ConsistencyConstraint";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final ConsistencyConstraintPackage eINSTANCE = ConsistencyConstraint.ConsistencyConstraintPackage.init();

	/**
	 * The meta object id for the '{@link ConsistencyConstraint.Consistency <em>Consistency</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ConsistencyConstraint.Consistency
	 * @see ConsistencyConstraint.ConsistencyConstraintPackage#getConsistency()
	 * @generated
	 */
	public static final int CONSISTENCY = 0;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CONSISTENCY__PARAMETERS = SpecificationPackage.CONSTRAINT__PARAMETERS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CONSISTENCY__NAME = SpecificationPackage.CONSTRAINT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Consistency</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CONSISTENCY_FEATURE_COUNT = SpecificationPackage.CONSTRAINT_FEATURE_COUNT + 1;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass consistencyEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see ConsistencyConstraint.ConsistencyConstraintPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ConsistencyConstraintPackage() {
		super(eNS_URI, ConsistencyConstraintFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link ConsistencyConstraintPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ConsistencyConstraintPackage init() {
		if (isInited) return (ConsistencyConstraintPackage)EPackage.Registry.INSTANCE.getEPackage(ConsistencyConstraintPackage.eNS_URI);

		// Obtain or create and register package
		ConsistencyConstraintPackage theConsistencyConstraintPackage = (ConsistencyConstraintPackage)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof ConsistencyConstraintPackage ? EPackage.Registry.INSTANCE.get(eNS_URI) : new ConsistencyConstraintPackage());

		isInited = true;

		// Initialize simple dependencies
		SpecificationPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theConsistencyConstraintPackage.createPackageContents();

		// Initialize created meta-data
		theConsistencyConstraintPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theConsistencyConstraintPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ConsistencyConstraintPackage.eNS_URI, theConsistencyConstraintPackage);
		return theConsistencyConstraintPackage;
	}


	/**
	 * Returns the meta object for class '{@link ConsistencyConstraint.Consistency <em>Consistency</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Consistency</em>'.
	 * @see ConsistencyConstraint.Consistency
	 * @generated
	 */
	public EClass getConsistency() {
		return consistencyEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link ConsistencyConstraint.Consistency#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ConsistencyConstraint.Consistency#getName()
	 * @see #getConsistency()
	 * @generated
	 */
	public EAttribute getConsistency_Name() {
		return (EAttribute)consistencyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public ConsistencyConstraintFactory getConsistencyConstraintFactory() {
		return (ConsistencyConstraintFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		consistencyEClass = createEClass(CONSISTENCY);
		createEAttribute(consistencyEClass, CONSISTENCY__NAME);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		SpecificationPackage theSpecificationPackage = (SpecificationPackage)EPackage.Registry.INSTANCE.getEPackage(SpecificationPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		consistencyEClass.getESuperTypes().add(theSpecificationPackage.getConstraint());

		// Initialize classes and features; add operations and parameters
		initEClass(consistencyEClass, Consistency.class, "Consistency", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getConsistency_Name(), ecorePackage.getEString(), "name", null, 0, 1, Consistency.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public interface Literals {
		/**
		 * The meta object literal for the '{@link ConsistencyConstraint.Consistency <em>Consistency</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ConsistencyConstraint.Consistency
		 * @see ConsistencyConstraint.ConsistencyConstraintPackage#getConsistency()
		 * @generated
		 */
		public static final EClass CONSISTENCY = eINSTANCE.getConsistency();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute CONSISTENCY__NAME = eINSTANCE.getConsistency_Name();

	}

} //ConsistencyConstraintPackage
