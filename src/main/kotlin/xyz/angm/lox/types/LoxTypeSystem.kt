package xyz.angm.lox.types

import com.oracle.truffle.api.dsl.ImplicitCast
import com.oracle.truffle.api.dsl.TypeCast
import com.oracle.truffle.api.dsl.TypeCheck
import com.oracle.truffle.api.dsl.TypeSystem
import com.oracle.truffle.api.nodes.UnexpectedResultException

@Suppress("UNUSED_PARAMETER")
@TypeSystem
abstract class LoxTypeSystem {
    companion object {
        @TypeCheck(Nil::class)
        @JvmStatic
        fun isNil(value: Any?) = value == Nil

        @TypeCast(Nil::class)
        fun asNil(value: Any?): Nil = Nil

        @ImplicitCast
        @JvmStatic
        fun castDoubleToNil(value: Double) = Nil

        @ImplicitCast
        @JvmStatic
        fun castIntToNil(value: Int) = Nil

        @ImplicitCast
        @JvmStatic
        fun castIntToDouble(value: Int) = value.toDouble()

        @JvmStatic
        fun expectBoolean(value: Any?): Boolean {
            if (value is Boolean) {
                return value
            }
            throw UnexpectedResultException(value)
        }

        @JvmStatic
        fun expectInteger(value: Any?): Int {
            if (value is Int) {
                return value
            }
            throw UnexpectedResultException(value)
        }

        @JvmStatic
        fun expectDouble(value: Any?): Double {
            if (value is Double) {
                return value
            }
            throw UnexpectedResultException(value)
        }
    }
}