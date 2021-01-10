package xyz.angm.lox.ast.expression

import com.oracle.truffle.api.frame.VirtualFrame

class IntLiteralNode(private val value: Int) : LoxExpressionNode() {
    override fun executeDouble(frame: VirtualFrame) = value.toDouble()
    override fun executeInt(frame: VirtualFrame) = value
    override fun executeGeneric(frame: VirtualFrame) = value
}

class DoubleLiteralNode(private val value: Double) : LoxExpressionNode() {
    override fun executeDouble(frame: VirtualFrame) = value
    override fun executeInt(frame: VirtualFrame) = value.toInt()
    override fun executeGeneric(frame: VirtualFrame) = value
}