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

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment)
    }

    override fun toString() = "<func ${declaration.name.lexeme}>"
}

class LoxClass(val name: String, private val methods: MutableMap<String, LoxFunction>) : LoxCallable {

    override val arity = 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>) = LoxInstance(this)

    fun findMethod(name: String) = if (methods.containsKey(name)) methods[name] else null

    override fun toString() = name
}

class LoxInstance(private val lClass: LoxClass) {

    private val fields = HashMap<String, Any?>()

    operator fun get(name: Token): Any? {
        return if (fields.containsKey(name.lexeme)) fields[name.lexeme]
        else lClass.findMethod(name.lexeme).bind(this)
            ?: throw RuntimeError(name, "Undefined property ${name.lexeme}.")
    }

    operator fun set(name: Token, value: Any?) = fields.put(name.lexeme, value)

    override fun toString() = "${lClass.name} instance"
}