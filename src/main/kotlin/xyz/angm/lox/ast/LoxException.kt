package xyz.angm.lox.ast

import com.oracle.truffle.api.exception.AbstractTruffleException
import com.oracle.truffle.api.interop.InteropLibrary
import com.oracle.truffle.api.library.ExportLibrary

@ExportLibrary(InteropLibrary::class)
class LoxException(message: String, location: LoxNode) : AbstractTruffleException(message, location) {

    companion object {
        // TODO: This error message is not particularly helpful
        fun typeError(on: LoxNode, values: List<Any?>) = LoxException("[RT] Type Error! at $on, with $values", on)
    }
}