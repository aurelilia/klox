package xyz.angm.lox.ast.statement

import com.oracle.truffle.api.frame.VirtualFrame
import xyz.angm.lox.ast.expression.LoxExpressionNode
import xyz.angm.lox.types.LoxReturnException

class ReturnNode(@Child private var value: LoxExpressionNode?) : LoxStatementNode() {
    override fun executeNil(frame: VirtualFrame) {
        throw LoxReturnException(value?.executeGeneric(frame))
    }
}