package xyz.angm.lox.ast.expression

import com.oracle.truffle.api.CallTarget
import com.oracle.truffle.api.CompilerAsserts
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.ExplodeLoop
import xyz.angm.lox.ast.LoxException
import xyz.angm.lox.ast.LoxNode

class CallNode(
    @Child private var function: LoxNode,
    @Children private val arguments: Array<LoxNode>
) : LoxExpressionNode() {
    @ExplodeLoop
    override fun executeGeneric(frame: VirtualFrame): Any? {
        val target = function.executeGeneric(frame)
        CompilerAsserts.compilationConstant<Any>(arguments.size)

        val argumentValues = arrayOfNulls<Any>(arguments.size)
        for (i in arguments.indices) {
            argumentValues[i] = arguments[i].executeGeneric(frame)
        }

        if (target is CallTarget) {
            return target.call(*argumentValues)
        } else {
            throw LoxException.typeError(function, listOf())
        }
    }
}