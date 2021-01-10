package xyz.angm.lox.types

import com.oracle.truffle.api.nodes.ControlFlowException

class LoxReturnException(val value: Any?) : ControlFlowException()