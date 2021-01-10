package xyz.angm.lox.ast

import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.RootNode

class LoxRootNode(private var node: LoxNode) : RootNode(null) {
    override fun execute(frame: VirtualFrame) = node.executeGeneric(frame)
}