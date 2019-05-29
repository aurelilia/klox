package xyz.angm.lox

interface LoxCallable {

    val arity: Int

    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}

class LoxFunction(
    private val declaration: Statement.Function,
    private val closure: Environment
) : LoxCallable {

    override val arity = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for (i in 0 until declaration.params.size) environment.define(declaration.params[i].lexeme, arguments[i])

        return try {
            interpreter.executeBlock(declaration.body, environment)
            null
        } catch (returnStatement: Return) {
            returnStatement.value
        }
    }

    override fun toString() = "<func ${declaration.name.lexeme}>"
}