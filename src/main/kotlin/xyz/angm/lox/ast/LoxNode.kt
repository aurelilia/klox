package xyz.angm.lox.ast

import com.oracle.truffle.api.dsl.TypeSystemReference
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.Node
import xyz.angm.lox.types.LoxTypeSystem

@TypeSystemReference(LoxTypeSystem::class)
abstract class LoxNode : Node() {
    open fun executeNil(frame: VirtualFrame) {
        executeGeneric(frame)
    }

    abstract fun executeGeneric(frame: VirtualFrame): Any?
}