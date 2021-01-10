package xyz.angm.lox

import com.oracle.truffle.api.Truffle
import xyz.angm.lox.ast.LoxNode
import xyz.angm.lox.ast.LoxRootNode

object GraalVM {
    fun exec(node: LoxNode): Any? {
        val root = LoxRootNode(node)
        val target = Truffle.getRuntime().createCallTarget(root)
        return target.call()
    }
}