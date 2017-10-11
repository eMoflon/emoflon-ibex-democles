/**
 */
package TGGAttributeConstraint;

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
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see TGGAttributeConstraint.TGGAttributeConstraintFactory
 * @model kind="package"
 * @generated
 */
public class TGGAttributeConstraintPackage extends EPackageImpl {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNAME = "TGGAttributeConstraint";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_URI = "platform:/resource/org.emoflon.ibex.tgg.runtime.democles/model/TGGAttributeConstraint.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_PREFIX = "TGGAttributeConstraint";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final TGGAttributeConstraintPackage eINSTANCE = TGGAttributeConstraint.TGGAttributeConstraintPackage.init();

	/**
	 * The meta object id for the '{@link TGGAttributeConstraint.AttributeConstraint <em>Attribute Constraint</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see TGGAttributeConstraint.AttributeConstraint
	 * @see TGGAttributeConstraint.TGGAttributeConstraintPackage#getAttributeConstraint()
	 * @generated
	 */
	public static final int ATTRIBUTE_CONSTRAINT = 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ATTRIBUTE_CONSTRAINT__PARAMETERS = SpecificationPackage.CONSTRAINT__PARAMETERS;

	/**
	 * The number of structural features of the '<em>Attribute Constraint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int ATTRIBUTE_CONSTRAINT_FEATURE_COUNT = SpecificationPackage.CONSTRAINT_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link TGGAttributeConstraint.EqStr <em>Eq Str</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see TGGAttributeConstraint.EqStr
	 * @see TGGAttributeConstraint.TGGAttributeConstraintPackage#getEqStr()
	 * @generated
	 */
	public static final int EQ_STR = 0;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int EQ_STR__PARAMETERS = ATTRIBUTE_CONSTRAINT__PARAMETERS;

	/**
	 * The number of structural features of the '<em>Eq Str</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int EQ_STR_FEATURE_COUNT = ATTRIBUTE_CONSTRAINT_FEATURE_COUNT + 0;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eqStrEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass attributeConstraintEClass = null;

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
	 * @see TGGAttributeConstraint.TGGAttributeConstraintPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private TGGAttributeConstraintPackage() {
		super(eNS_URI, TGGAttributeConstraintFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link TGGAttributeConstraintPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static TGGAttributeConstraintPackage init() {
		if (isInited) return (TGGAttributeConstraintPackage)EPackage.Registry.INSTANCE.getEPackage(TGGAttributeConstraintPackage.eNS_URI);

		// Obtain or create and register package
		TGGAttributeConstraintPackage theTGGAttributeConstraintPackage = (TGGAttributeConstraintPackage)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof TGGAttributeConstraintPackage ? EPackage.Registry.INSTANCE.get(eNS_URI) : new TGGAttributeConstraintPackage());

		isInited = true;

		// Initialize simple dependencies
		SpecificationPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theTGGAttributeConstraintPackage.createPackageContents();

		// Initialize created meta-data
		theTGGAttributeConstraintPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theTGGAttributeConstraintPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(TGGAttributeConstraintPackage.eNS_URI, theTGGAttributeConstraintPackage);
		return theTGGAttributeConstraintPackage;
	}


	/**
	 * Returns the meta object for class '{@link TGGAttributeConstraint.EqStr <em>Eq Str</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Eq Str</em>'.
	 * @see TGGAttributeConstraint.EqStr
	 * @generated
	 */
	public EClass getEqStr() {
		return eqStrEClass;
	}

	/**
	 * Returns the meta object for class '{@link TGGAttributeConstraint.AttributeConstraint <em>Attribute Constraint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Attribute Constraint</em>'.
	 * @see TGGAttributeConstraint.AttributeConstraint
	 * @generated
	 */
	public EClass getAttributeConstraint() {
		return attributeConstraintEClass;
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public TGGAttributeConstraintFactory getTGGAttributeConstraintFactory() {
		return (TGGAttributeConstraintFactory)getEFactoryInstance();
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
		eqStrEClass = createEClass(EQ_STR);

		attributeConstraintEClass = createEClass(ATTRIBUTE_CONSTRAINT);
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
		eqStrEClass.getESuperTypes().add(this.getAttributeConstraint());
		attributeConstraintEClass.getESuperTypes().add(theSpecificationPackage.getConstraint());

		// Initialize classes and features; add operations and parameters
		initEClass(eqStrEClass, EqStr.class, "EqStr", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(attributeConstraintEClass, AttributeConstraint.class, "AttributeConstraint", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public interface Literals {
		/**
		 * The meta object literal for the '{@link TGGAttributeConstraint.EqStr <em>Eq Str</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see TGGAttributeConstraint.EqStr
		 * @see TGGAttributeConstraint.TGGAttributeConstraintPackage#getEqStr()
		 * @generated
		 */
		public static final EClass EQ_STR = eINSTANCE.getEqStr();

		/**
		 * The meta object literal for the '{@link TGGAttributeConstraint.AttributeConstraint <em>Attribute Constraint</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see TGGAttributeConstraint.AttributeConstraint
		 * @see TGGAttributeConstraint.TGGAttributeConstraintPackage#getAttributeConstraint()
		 * @generated
		 */
		public static final EClass ATTRIBUTE_CONSTRAINT = eINSTANCE.getAttributeConstraint();

	}

} //TGGAttributeConstraintPackage
