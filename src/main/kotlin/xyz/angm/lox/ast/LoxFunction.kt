package xyz.angm.lox.ast

import com.oracle.truffle.api.RootCallTarget
import com.oracle.truffle.api.Truffle
import com.oracle.truffle.api.frame.VirtualFrame

class LoxFunction(@Child private var body: LoxRootNode) : LoxNode() {
    private val target: RootCallTarget = Truffle.getRuntime().createCallTarget(body)
    override fun executeGeneric(frame: VirtualFrame) = target
}