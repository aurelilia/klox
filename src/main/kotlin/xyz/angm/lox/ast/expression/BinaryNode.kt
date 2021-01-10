package xyz.angm.lox.ast.expression

import com.oracle.truffle.api.CompilerDirectives
import com.oracle.truffle.api.dsl.Fallback
import com.oracle.truffle.api.dsl.NodeChild
import com.oracle.truffle.api.dsl.NodeChildren
import com.oracle.truffle.api.dsl.Specialization
import xyz.angm.lox.ast.LoxException

@NodeChildren(
    NodeChild("left"),
    NodeChild("right")
)
abstract class BinaryNode : LoxExpressionNode() {
    @Fallback
    protected open fun typeError(left: Any?, right: Any?): Any? {
        throw LoxException.typeError(this, listOf(left, right))
    }
}

abstract class AddNode : BinaryNode() {
    @Specialization(rewriteOn = [ArithmeticException::class])
    protected fun withInt(left: Int, right: Int) = Math.addExact(left, right)

    @Specialization(replaces = ["withInt"])
    protected fun withDouble(left: Double, right: Double) = left + right

    @Specialization(guards = ["isString(left, right)"])
    @CompilerDirectives.TruffleBoundary
    protected fun withString(left: Any, right: Any) = left.toString() + right.toString()

    protected fun isString(a: Any?, b: Any?) = a is String || b is String
}

abstract class SubNode : BinaryNode() {
    @Specialization(rewriteOn = [ArithmeticException::class])
    protected fun withInt(left: Int, right: Int) = Math.subtractExact(left, right)

    @Specialization(replaces = ["withInt"])
    protected fun withDouble(left: Double, right: Double) = left - right
}

abstract class MulNode : BinaryNode() {
    @Specialization(rewriteOn = [ArithmeticException::class])
    protected fun withInt(left: Int, right: Int) = Math.multiplyExact(left, right)

    @Specialization(replaces = ["withInt"])
    protected fun withDouble(left: Double, right: Double) = left * right
}

abstract class DivNode : BinaryNode() {
    @Specialization
    protected fun withDouble(left: Double, right: Double) = left / right
}

abstract class EqualNode : BinaryNode() {
    @Specialization
    protected fun withBool(left: Boolean, right: Boolean) = left == right

    @Specialization
    protected fun withInt(left: Int, right: Int) = left == right

    @Specialization(replaces = ["withInt"])
    protected fun withDouble(left: Double, right: Double) = left == right

    @Specialization
    protected fun withString(left: String, right: String) = left == right
}

abstract class LessThanNode : BinaryNode() {
    @Specialization
    protected fun withInt(left: Int, right: Int) = left < right

    @Specialization(replaces = ["withInt"])
    protected fun withDouble(left: Double, right: Double) = left < right
}

abstract class LessEqualThanNode : BinaryNode() {
    @Specialization
    protected fun withInt(left: Int, right: Int) = left <= right

    @Specialization(replaces = ["withInt"])
    protected fun withDouble(left: Double, right: Double) = left <= right
}

abstract class MoreThanNode : BinaryNode() {
    @Specialization
    protected fun withInt(left: Int, right: Int) = left > right

    @Specialization(replaces = ["withInt"])
    protected fun withDouble(left: Double, right: Double) = left > right
}

abstract class MoreThanEqualNode : BinaryNode() {
    @Specialization
    protected fun withInt(left: Int, right: Int) = left >= right

    @Specialization(replaces = ["withInt"])
    protected fun withDouble(left: Double, right: Double) = left >= right
}
