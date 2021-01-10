package xyz.angm.lox.ast.statement

import com.oracle.truffle.api.frame.VirtualFrame
import xyz.angm.lox.ast.LoxNode
import xyz.angm.lox.types.Nil

abstract class LoxStatementNode : LoxNode() {
    abstract override fun executeNil(frame: VirtualFrame)

    override fun executeGeneric(frame: VirtualFrame): Any? {
        executeNil(frame)
        return Nil
    }
}