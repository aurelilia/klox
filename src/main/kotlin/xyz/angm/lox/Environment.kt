package xyz.angm.lox

class Environment(val enclosingEnv: Environment? = null) {

    private val values = HashMap<String, Any?>()

    fun define(name: String, value: Any?) = values.set(name, value)

    fun assign(name: Token, value: Any?) {
        when {
            values.containsKey(name.lexeme) -> values[name.lexeme] = value
            enclosingEnv != null -> enclosingEnv.assign(name, value)
            else -> throw RuntimeError(name, "Undefined variable ${name.lexeme}.")
        }
    }

    fun assignAt(distance: Int, name: Token, value: Any?) = ancestor(distance).values.set(name.lexeme, value)

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

    fun getAt(distance: Int, name: String) = ancestor(distance).values[name]

    private fun ancestor(distance: Int): Environment {
        var environment = this
        for (i in 0 until distance) environment = environment.enclosingEnv!!
        return environment
    }

    /** A simple object to signify a variable not yet assigned. */
    object Unassigned
}