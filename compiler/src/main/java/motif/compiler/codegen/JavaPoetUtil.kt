package motif.compiler.codegen

import com.squareup.javapoet.*
import motif.compiler.javax.Executable
import motif.compiler.javax.ExecutableParam
import motif.compiler.javax.JavaxUtil
import motif.internal.Meta
import motif.ir.graph.Scope
import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.base.Dependency
import motif.ir.source.base.Type
import motif.ir.source.child.ChildDeclaration
import motif.ir.source.dependencies.AnnotatedDependency
import motif.ir.source.dependencies.Dependencies
import motif.ir.source.objects.FactoryMethod
import motif.ir.source.objects.ObjectsClass
import motif.ir.source.objects.SpreadMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

interface JavaPoetUtil : JavaxUtil {

    val Type.mirror: TypeMirror
        get() = userData as TypeMirror

    val ObjectsClass.isInterface: Boolean
        get() = type.asElement().kind == ElementKind.INTERFACE

    fun ObjectsClass.abstractFactoryMethods(): List<Executable> {
        return factoryMethods
                .filter { it.isAbstract }
                .map { it.executable }
    }

    val SpreadMethod.executable: Executable
        get() = userData as Executable

    val FactoryMethod.executable: Executable
        get() = userData as Executable

    fun Executable.overriding(): MethodSpec.Builder {
        return MethodSpec.overriding(element, owner, env.typeUtils)
    }

    fun Executable.overrideUnsupported(): MethodSpec {
        return overriding()
                .addStatement("throw new \$T()", UnsupportedOperationException::class.java)
                .build()
    }

    val AccessMethod.executable: Executable
        get() = userData as Executable

    fun Executable.overrideWithFinalParams(): MethodSpec.Builder {
        val builder = MethodSpec.methodBuilder(name)
                .addAnnotation(Override::class.java)
                .returns(returnType.typeName)

        parameters
                .map {
                    it.specBuilder()
                            .addModifiers(Modifier.FINAL)
                            .build()
                }
                .forEach { builder.addParameter(it) }

        return builder
    }

    fun ExecutableParam.specBuilder(): ParameterSpec.Builder {
        return ParameterSpec.builder(
                type.typeName,
                element.simpleName.toString())
    }

    val ObjectsClass.type: DeclaredType
        get() = userData as DeclaredType

    val ChildDeclaration.executable: Executable
        get() = method.userData as Executable

    val Scope.type: DeclaredType
        get() = scopeClass.userData as DeclaredType

    val motif.ir.source.base.Annotation.mirror: AnnotationMirror
        get() = userData as AnnotationMirror

    val Dependency.declaredType: DeclaredType
        get() = userData as DeclaredType

    val Dependency.typeName: TypeName
        get() = ClassName.get(declaredType)

    fun TypeSpec.write(packageName: String): TypeSpec {
        JavaFile.builder(packageName, this).build().writeTo(env.filer)
        return this
    }

    fun Dependencies.abstractMethodSpecs(
            additionalDependencies: List<Dependency> = listOf()): Map<Dependency, MethodSpec> {
        return methodSpecBuilders(additionalDependencies).mapValues {
            it.value.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build()
        }
    }

    fun Dependencies.methodSpecBuilders(
            additionalDependencies: List<Dependency> = listOf()): Map<Dependency, MethodSpec.Builder> {
        return nameScope {
            list.map { it.dependency }
                    .plus(additionalDependencies)
                    .associateBy({ it }) {
                        it.methodSpecBuilder()
                    }
        }
    }

    fun Dependencies.abstractMethodSpecsMeta(): Map<Dependency, MethodSpec> {
        return nameScope {
            list.associateBy({ it.dependency }) {
                val metaSpec = it.metaSpec()
                it.dependency.methodSpecBuilder()
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(metaSpec)
                        .build()
            }
        }
    }

    fun AnnotatedDependency.metaSpec(): AnnotationSpec {
        val consumingClassNames = consumingScopes.map { it.mirror.typeName }.toTypedArray()
        val consumingList = consumingClassNames.map { "\$T.class" }.joinToString(", ")
        return AnnotationSpec.builder(Meta::class.java)
                .addMember("transitive", "$transitive")
                .addMember("consumingScopes", "{$consumingList}", *consumingClassNames)
                .build()
    }

    fun <T> nameScope(block: NameScope.() -> T): T {
        return NameScope(env).block()
    }

    class NameScope(override val env: ProcessingEnvironment) : JavaPoetUtil {

        private val names = UniqueNameSet()

        fun Dependency.methodSpecBuilder(): MethodSpec.Builder {
            return MethodSpec.methodBuilder(name())
                    .returns(typeName)
                    .apply { qualifier?.let { addAnnotation(it.spec()) } }
        }

        fun Dependency.parameterSpec(): ParameterSpec {
            return ParameterSpec.builder(typeName, names.unique(name()))
                    .apply { qualifier?.let { addAnnotation(it.spec()) } }
                    .build()
        }

        fun motif.ir.source.base.Annotation.spec(): AnnotationSpec {
            return AnnotationSpec.get(mirror)
        }

        fun Dependency.name(): String {
            val preferred = declaredType.asElement().simpleName.toString().decapitalize()
            return names.unique(preferred)
        }

        fun claim(name: String) {
            names.claim(name)
        }
    }
}