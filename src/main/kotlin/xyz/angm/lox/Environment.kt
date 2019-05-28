package xyz.angm.lox

class Environment(val enclosingEnv: Environment? = null) {

    private val values = HashMap<String, Any?>()

    fun define(name: String, value: Any?) = values.set(name, value)

    // Returns the value it was given for easier implementation in [Interpreter]
    fun assign(name: Token, value: Any?): Any? {
        when {
            values.containsKey(name.lexeme) -> values[name.lexeme] = value
            enclosingEnv != null -> enclosingEnv.assign(name, value)
            else -> throw RuntimeError(name, "Undefined variable ${name.lexeme}.")
        }
        return value
    }

    fun get(name: Token): Any? {
        return when {
            values.containsKey(name.lexeme) -> {
                if (values[name.lexeme] == Unassigned) throw RuntimeError(name, "Variable ${name.lexeme} accessed prior to assignment!")
                else values[name.lexeme]
            }
            enclosingEnv != null -> enclosingEnv.get(name)
            else -> throw RuntimeError(name, "Undefined variable ${name.lexeme}.")
        }
    }

    /** A simple object to signify a variable not yet assigned. */
    object Unassigned
}