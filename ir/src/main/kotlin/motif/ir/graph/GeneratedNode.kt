package motif.ir.graph

import motif.ir.source.base.Dependency
import motif.ir.source.dependencies.Dependencies
import motif.ir.source.dependencies.GeneratedDependencies

class GeneratedNode(private val generatedDependencies: GeneratedDependencies) : Node {

    override val childDependencies: Dependencies = Dependencies(listOf())

    override val dependencies: Dependencies by lazy {
        Dependencies(generatedDependencies.annotatedDependencies)
    }

    override val missingDependencies: Dependencies? = null

    override val dependencyCycle: List<Dependency>? = null

    override val children: List<Node> = listOf()
}