package xyz.angm.lox.ast.expression

import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.profiles.BranchProfile
import xyz.angm.lox.ast.statement.LoxStatementNode
import xyz.angm.lox.types.LoxReturnException
import xyz.angm.lox.types.Nil

class FunctionBodyNode(@Child var body: LoxStatementNode?) : LoxExpressionNode() {

    private val exceptionTaken = BranchProfile.create()
    private val nullTaken = BranchProfile.create()

    override fun executeGeneric(frame: VirtualFrame): Any? {
        try {
            body!!.executeNil(frame)
        } catch (ret: LoxReturnException) {
            exceptionTaken.enter()
            return ret.value
        }

        nullTaken.enter()
        return Nil
    }
}