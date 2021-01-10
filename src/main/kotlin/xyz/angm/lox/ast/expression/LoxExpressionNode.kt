package xyz.angm.lox.ast.expression

import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.UnexpectedResultException
import xyz.angm.lox.ast.LoxNode
import xyz.angm.lox.types.LoxTypeSystem
import kotlin.jvm.Throws

abstract class LoxExpressionNode : LoxNode() {
    @Throws(UnexpectedResultException::class)
    open fun executeBoolean(frame: VirtualFrame) = LoxTypeSystem.expectBoolean(executeGeneric(frame))

    @Throws(UnexpectedResultException::class)
    open fun executeInt(frame: VirtualFrame) = LoxTypeSystem.expectInteger(executeGeneric(frame))

    @Throws(UnexpectedResultException::class)
    open fun executeDouble(frame: VirtualFrame) = LoxTypeSystem.expectDouble(executeGeneric(frame))
}