package xyz.angm.lox.ast.statement

import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.UnexpectedResultException
import com.oracle.truffle.api.profiles.ConditionProfile
import xyz.angm.lox.ast.LoxException
import xyz.angm.lox.ast.expression.LoxExpressionNode

class IfNode(
    @Child private var condition: LoxExpressionNode,
    @Child private var then: LoxStatementNode,
    @Child private var else_: LoxStatementNode?,
) : LoxStatementNode() {

    private val conditionProfile = ConditionProfile.createCountingProfile()

    override fun executeNil(frame: VirtualFrame) {
        if (conditionProfile.profile(evaluateCondition(frame))) {
            then.executeNil(frame)
        } else {
            else_?.executeNil(frame)
        }
    }

    private fun evaluateCondition(frame: VirtualFrame): Boolean {
        return try {
            condition.executeBoolean(frame)
        } catch (ex: UnexpectedResultException) {
            throw LoxException.typeError(this, listOf(ex.result))
        }
    }
}