package xyz.angm.lox.ast.statement

import com.oracle.truffle.api.CompilerAsserts
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.ExplodeLoop

class BlockNode(@Children private var stmts: Array<LoxStatementNode>) : LoxStatementNode() {
    @ExplodeLoop
    override fun executeNil(frame: VirtualFrame) {
        CompilerAsserts.compilationConstant<Any>(stmts.size)
        for (stmt in stmts) {
            stmt.executeNil(frame)
        }
    }
}