package com.uber.motif.intellij.graph

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.uber.motif.intellij.index.ScopeIndex
import com.uber.motif.intellij.psi.getScopeClasses
import com.uber.motif.intellij.thread.RetryScheduler
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.atomic.AtomicBoolean

class GraphProcessor(private val project: Project) {

    private val psiManager = PsiManager.getInstance(project)
    private val index = ScopeIndex.getInstance()
    private val retryScheduler = RetryScheduler(project)

    private var scopeClasses: List<PsiClass> = listOf()

    private val isDirty = AtomicBoolean()

    fun start() {
        index.registerListener {
            isDirty.set(true)
            retryScheduler.runWithRetry(object: RetryScheduler.Job {
                override fun run() {
                    // It's possible that multiple refreshes have been queued up by this point. Only do work if necessary.
                    if (isDirty.compareAndSet(true, false)) {
                        refresh()
                    }
                }

                override fun onRetry() {
                    // Job didn't actually complete so reset the dirty flag.
                    isDirty.set(true)
                }
            })
        }
    }

    fun isScopeClass(element: PsiElement): Boolean {
        return element in scopeClasses
    }

    fun scopeClasses(): List<PsiClass> {
        retryScheduler.waitForCompletion()
        return scopeClasses
    }

    private fun refresh() {
        this.scopeClasses = getScopeClasses()
    }

    private fun getScopeClasses(): List<PsiClass> {
        return index.getSnapshot(project).files
                .mapNotNull { psiManager.findFile(it) }
                .flatMap { it.getScopeClasses() }
    }
}