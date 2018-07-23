package com.uber.motif.graph

import com.uber.motif.source.ScopeClass
import com.uber.motif.util.MultiMap

class Node(val scopeClass: ScopeClass) {

    val consumers: MutableSet<DependencyConsumer> = mutableSetOf()
    val providers: MutableSet<DependencyProvider> = mutableSetOf()
    val external: MultiMap<Dependency, DependencyConsumer> = MultiMap()

    val parents: MutableSet<Node> = mutableSetOf()
    val children: MutableSet<Node> = mutableSetOf()
}