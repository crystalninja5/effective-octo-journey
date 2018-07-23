package com.uber.motif.source

interface ObjectClass : Source {

    val providerMethods: List<ProviderMethod>

    override val type: SourceType
        get() = SourceType.OBJECT_CLASS
}