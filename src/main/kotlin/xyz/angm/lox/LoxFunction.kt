package xyz.angm.lox

class LoxFunction(private val declaration: Statement.Function) : LoxCallable {

    override val arity = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(interpreter.globals)
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