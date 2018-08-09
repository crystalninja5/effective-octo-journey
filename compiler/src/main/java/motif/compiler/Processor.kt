package motif.compiler

import motif.Scope
import motif.compiler.codegen.Generator
import motif.compiler.errors.CompilerError
import motif.compiler.errors.ErrorHandler
import motif.compiler.ir.SourceSetFactory
import motif.ir.graph.GraphFactory
import motif.ir.graph.errors.GraphValidationErrors
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class Processor : AbstractProcessor() {

    // For testing only.
    var validationErrors: GraphValidationErrors? = null

    private val hasErrors: Boolean
        get() = !(validationErrors?.isEmpty() ?: true)

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Scope::class.java.name)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver() || hasErrors) {
            return true
        }
        process(roundEnv)
        return true
    }

    private fun process(roundEnv: RoundEnvironment) {
        val sourceSet = try {
            SourceSetFactory(processingEnv).create(roundEnv)
        } catch (e: CompilerError) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "\n${e.message}\n", e.element)
            return
        }

        val graph = GraphFactory.create(sourceSet)
        val errors = graph.validationErrors
        this.validationErrors = errors

        if (errors.isEmpty()) {
            Generator(processingEnv, graph).generate()
        } else {
            errors.forEach { error ->
                val errorMessage = ErrorHandler.handle(error)
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "\n${errorMessage.message}\n", errorMessage.element)
            }
        }
    }
}