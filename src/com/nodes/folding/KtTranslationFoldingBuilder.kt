package com.nodes.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.PsiTreeUtil.findChildrenOfType
import com.nodes.folding.TranslationFoldingBuilder.Companion.MAX_FOLDED_PLACEHOLDER_TEXT_LENGTH
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement

/**
 * Name: KtTranslationFoldingBuilder<br>
 * User: Yao<br>
 * Date: 2017/11/28<br>
 * Time: 19:04<br>
 */
class KtTranslationFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val expressions = findChildrenOfType(root, KtDotQualifiedExpression::class.java)
        expressions.forEach {
            val validExpr = isValidExpr(it)
            if (validExpr) {
                val node = it.node
                val descriptor = FoldingDescriptor(node, node.textRange, null)
                descriptors.add(descriptor)
            }
        }
        return descriptors.toTypedArray();
    }

    private fun isValidExpr(referenceExpression: KtDotQualifiedExpression?): Boolean {
        if (referenceExpression == null || referenceExpression.children.size != 2) return false
        val nstackSectionReference = referenceExpression.firstChild
        val translationChild = nstackSectionReference.firstChild
        return translationChild.text == "Translation"
    }


    override fun getPlaceholderText(node: ASTNode): String? {
        val lastChild = node.psi.lastChild
        val referenceName = lastChild.text
        val referencedFieldElement = (lastChild as KtElement).mainReference?.resolve()
        var value: String? = null
        if (referencedFieldElement != null) {
            val elements = referencedFieldElement.children
            for (psiElement in elements) {
                if (psiElement is PsiLiteralExpression) {
                    value = psiElement.value as String?
                    if (value?.length!! > MAX_FOLDED_PLACEHOLDER_TEXT_LENGTH) {
                        value = value.substring(0, MAX_FOLDED_PLACEHOLDER_TEXT_LENGTH) + "..."
                    }
                    break
                }
            }
        }
        return if (value != null) referenceName + ": " + value else referenceName + ": ..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }
}
