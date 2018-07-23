package com.uber.motif.graph

import com.uber.motif.source.Source

class DependencyConsumer(
        val source: Source,
        val dependency: Dependency) {

    var providers: MutableSet<DependencyProvider> = mutableSetOf()
}