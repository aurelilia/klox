package xyz.angm.lox.ast.statement

import com.oracle.truffle.api.frame.VirtualFrame
import xyz.angm.lox.ast.expression.LoxExpressionNode

class StatementExprNode(@Child private var expr: LoxExpressionNode) : LoxStatementNode() {
    override fun executeNil(frame: VirtualFrame) = expr.executeNil(frame)
}