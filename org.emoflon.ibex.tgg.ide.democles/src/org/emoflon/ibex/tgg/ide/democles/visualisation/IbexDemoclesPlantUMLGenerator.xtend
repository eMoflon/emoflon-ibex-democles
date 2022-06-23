package org.emoflon.ibex.tgg.ide.democles.visualisation

import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EReference
import org.gervarro.democles.specification.emf.Pattern
import org.gervarro.democles.specification.emf.PatternBody
import org.gervarro.democles.specification.emf.PatternInvocationConstraint
import org.gervarro.democles.specification.emf.Variable
import org.gervarro.democles.specification.emf.constraint.emf.emf.EMFVariable
import org.gervarro.democles.specification.emf.constraint.emf.emf.Reference
import org.emoflon.ibex.tgg.editor.ide.visualisation.IbexPlantUMLGenerator

class IbexDemoclesPlantUMLGenerator extends IbexPlantUMLGenerator {

	static def String separator() {
		return "_"
	}

	static def String visualisePatternBody(PatternBody b, String prefix) {
		'''
			«visualiseIsolatedPatternBody(b, prefix)»
			«var j = 0»
			«FOR pi : patternInvocations(b)»
				«var subPrefix = prefix + separator() + j++ + separator()»
				«IF pi.invokedPattern.bodies.size == 1»
					«visualisePatternBody(pi.invokedPattern.bodies.get(0), subPrefix)»
				«ELSE»
					«visualiseSymbolicParameters(pi.invokedPattern, subPrefix)»
				«ENDIF»
				
				«var i = 0»
				«FOR param : pi.parameters»
					«IF pi.positive»
						«identifierFor(param.reference as Variable, b.header, prefix)» #--# «identifierFor(pi.invokedPattern.symbolicParameters.get(i++), pi.invokedPattern, subPrefix)»
					«ELSE»
						namespace «subPrefix»«pi.invokedPattern.name» #DDDDDD {
						«identifierFor(param.reference as Variable, b.header, prefix)» #..# «identifierFor(pi.invokedPattern.symbolicParameters.get(i++), pi.invokedPattern, subPrefix)»
						}
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}

	private static def String visualiseSymbolicParameters(Pattern p, String prefix) {
		'''
			«FOR v : patternVariables(p) SEPARATOR "\n"»
				class «identifierFor(v, p, prefix)»<< (V,#FF7700)>>
			«ENDFOR»
		'''
	}

	private static def patternInvocations(PatternBody body) {
		body.constraints.filter(PatternInvocationConstraint)
	}

	private static def String visualiseIsolatedPatternBody(PatternBody b, String prefix) {
		'''
			«visualiseSymbolicParameters(b.header, prefix)»
			«FOR v : localVariables(b) SEPARATOR "\n"»
				class «identifierFor(v, b.header, prefix)»<< (L,#B0D8F0)>>
			«ENDFOR»
			«FOR ref : referenceConstraints(b)»
				«identifierFor(extractSrc(ref), b.header, prefix)» --> "«extractType(ref.EModelElement)»" «identifierFor(extractTrg(ref), b.header, prefix)»
			«ENDFOR»
		'''
	}

	private def static extractType(EReference ref) {
		if(ref === null || ref.name === null) "???" else ref.name
	}

	private def static localVariables(PatternBody body) {
		body.localVariables.filter[v|!((v as EMFVariable).getEClassifier instanceof EDataType)]
	}

	private static def identifierFor(Variable v, Pattern pattern, String prefix) {
		'''"«prefix»«pattern.name».«v.name»:«extractType((v as EMFVariable).getEClassifier)»"'''
	}

	def static extractType(EClassifier classifier) {
		if(classifier === null || classifier.name === null) "???" else classifier.name
	}

	private static def extractVar(Reference ref, int i) {
		ref.parameters.get(i).reference as Variable
	}

	private static def extractSrc(Reference ref) {
		extractVar(ref, 0)
	}

	private static def extractTrg(Reference ref) {
		extractVar(ref, 1)
	}

	private static def referenceConstraints(PatternBody body) {
		body.constraints.filter(Reference)
	}

	private static def patternVariables(Pattern p) {
		p.symbolicParameters
	}
}
