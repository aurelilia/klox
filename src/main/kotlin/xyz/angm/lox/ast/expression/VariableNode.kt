package xyz.angm.lox.ast.expression

import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.profiles.BranchProfile

class ReadArgumentNode(private val index: Int) : LoxExpressionNode() {

    // Profile when the function was called with less args then declared
    private val outOfBoundsTaken = BranchProfile.create()

    override fun executeGeneric(frame: VirtualFrame): Any? {
        val args = frame.arguments
        return if (index < args.size) {
            args[index]
        } else {
            outOfBoundsTaken.enter()
            null
        }
    }
}