package xyz.angm.lox

interface LoxCallable {

    val arity: Int

    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}

class LoxNativeFunction(
    private val name: String,
    override val arity: Int,
    private val call: (interpreter: Interpreter, arguments: List<Any?>) -> Any?
) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>) = call.invoke(interpreter, arguments)
    override fun toString() = "<native func $name>"
}

class LoxFunction(
    private val declaration: Statement.Function,
    private val closure: Environment,
    private val isInitializer: Boolean = false
) : LoxCallable {

    override val arity = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for (i in declaration.params.indices) environment.define(declaration.params[i].lexeme, arguments[i])

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnStatement: Return) {
            return if (isInitializer) closure.getAt(0, "this")
            else returnStatement.value
        }

        return if (isInitializer) closure.getAt(0, "this")
        else null
    }

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }

    override fun toString() = "<func ${declaration.name.lexeme}>"
}

class LoxClass(
    val name: String,
    private val superclass: LoxClass?,
    private val methods: MutableMap<String, LoxFunction>
) : LoxCallable {

    override val arity = findMethod("init")?.arity ?: 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>): LoxInstance {
        val instance = LoxInstance(this)
        findMethod("init")?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    fun findMethod(name: String): LoxFunction? = if (methods.containsKey(name)) methods[name] else superclass?.findMethod(name)

    override fun toString() = name
}

class LoxInstance(private val lClass: LoxClass) {

    private val fields = HashMap<String, Any?>()

    operator fun get(name: Token): Any? {
        return if (fields.containsKey(name.lexeme)) fields[name.lexeme]
        else lClass.findMethod(name.lexeme)?.bind(this)
            ?: throw RuntimeError(name, "Undefined property ${name.lexeme}.")
    }

    operator fun set(name: Token, value: Any?) = fields.put(name.lexeme, value)

    override fun toString() = "${lClass.name} instance"
}