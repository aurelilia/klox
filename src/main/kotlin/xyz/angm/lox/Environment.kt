package xyz.angm.lox

class Environment {

    private val values = HashMap<String, Any?>()

    fun define(name: String, value: Any?) = values.set(name, value)

    // Returns the value it was given for easier implementation in [Interpreter]
    fun assign(name: Token, value: Any?): Any? {
        if (values.containsKey(name.lexeme)) values[name.lexeme] = value
        else throw RuntimeError(name, "Undefined variable ${name.lexeme}.")
        return value
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) return values[name.lexeme]
        else throw RuntimeError(name, "Undefined variable ${name.lexeme}.")
    }
}