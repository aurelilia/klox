package xyz.angm.lox

import xyz.angm.lox.TokenType.*
import java.util.Scanner

class Return(val value: Any?) : RuntimeException(null, null, false, false)

class RuntimeError(val token: Token, message: String) : java.lang.RuntimeException(message)

class Interpreter : Expression.Visitor<Any?>, Statement.Visitor<Unit> {

    private val globals = Environment()
    private var environment = globals
    private val locals = HashMap<Expression, Int>()

    init {
        globals.define("clock", LoxNativeFunction("clock", 0) { _, _ -> System.currentTimeMillis().toDouble() })
        globals.define("print", LoxNativeFunction("print", 1) { _, arguments -> print(stringify(arguments[0])) })
        globals.define("printLine", LoxNativeFunction("printLine", 1) { _, arguments -> println(stringify(arguments[0])) })

        val scanner = Scanner(System.`in`)
        globals.define("readLine", LoxNativeFunction("readLine", 0) { _, _ -> scanner.nextLine() })
    }

    fun interpret(statements: List<Statement>) {
        try {
            statements.forEach(::execute)
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    private fun execute(statement: Statement) = statement.accept(this)

    fun executeBlock(statements: List<Statement>, environment: Environment) {
        val prevEnv = this.environment
        try {
            this.environment = environment
            statements.forEach(::execute)
        } finally {
            this.environment = prevEnv
        }
    }

    fun resolve(expression: Expression, depth: Int) = locals.put(expression, depth)

    // Statement Visitors

    override fun visitBlockStatement(statement: Statement.Block) = executeBlock(statement.statements, Environment(environment))

    override fun visitClassStatement(statement: Statement.Class) {
        val superclass = if (statement.superclass != null) evaluate(statement.superclass) else null
        if (superclass != null && superclass !is LoxClass) throw RuntimeError(statement.superclass!!.name, "Superclass must be a class.")

        environment.define(statement.name.lexeme, null)

        if (statement.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods = HashMap<String, LoxFunction>()
        statement.methods.forEach { methods[it.name.lexeme] = LoxFunction(it, environment, it.name.lexeme == "init") }

        val lClass = LoxClass(statement.name.lexeme, superclass as? LoxClass, methods)

        if (superclass != null) environment = environment.enclosingEnv!!

        environment.assign(statement.name, lClass)
    }

    override fun visitExpressionStatement(statement: Statement.Expression) {
        evaluate(statement.expression)
    }

    override fun visitFunctionStatement(statement: Statement.Function) {
        val function = LoxFunction(statement, environment)
        environment.define(statement.name.lexeme, function)
    }

    override fun visitReturnStatement(statement: Statement.Return) = throw Return(if (statement.value != null) evaluate(statement.value) else null)

    override fun visitIfStatement(statement: Statement.If) {
        if (isTruthy(evaluate(statement.condition))) execute(statement.thenBranch)
        else execute(statement.elseBranch ?: return)
    }

    override fun visitVarStatement(statement: Statement.Var) {
        var initValue: Any? = Environment.Unassigned
        if (statement.initializer != null) initValue = evaluate(statement.initializer)
        environment.define(statement.name.lexeme, initValue)
    }

    override fun visitWhileStatement(statement: Statement.While) {
        while (isTruthy(evaluate(statement.condition))) execute(statement.body)
    }

    // Expression Visitors

    override fun visitLiteralExpression(expression: Expression.Literal) = expression.value

    override fun visitVariableExpression(expression: Expression.Variable) = lookUpVariable(expression.name, expression)

    override fun visitAssignExpression(expression: Expression.Assign): Any? {
        val value = evaluate(expression.value)
        val distance = locals[expression]

        if (distance != null) environment.assignAt(distance, expression.name, value)
        else globals.assign(expression.name, value)

        return value
    }

    override fun visitGroupingExpression(expression: Expression.Grouping) = evaluate(expression.expression)

    override fun visitCallExpression(expression: Expression.Call): Any? {
        val callee = evaluate(expression.callee)
        val arguments = ArrayList<Any?>()
        expression.arguments.forEach { argument -> arguments.add(evaluate(argument)) }

        when {
            callee !is LoxCallable -> throw RuntimeError(expression.paren, "Only functions and classes are allowed to be called!")
            arguments.size != callee.arity -> throw RuntimeError(expression.paren, "Expected ${callee.arity} arguments but got ${arguments.size}.")

            else -> return callee.call(this, arguments)
        }
    }

    override fun visitGetExpression(expression: Expression.Get): Any? {
        val obj = evaluate(expression.obj)
        if (obj is LoxInstance) return obj[expression.name]
        throw RuntimeError(expression.name, "Only instances have properties.")
    }

    override fun visitSetExpression(expression: Expression.Set): Any? {
        val obj = evaluate(expression.obj) as? LoxInstance ?: throw RuntimeError(expression.name, "Only instances have properties.")
        val value = evaluate(expression.value)

        obj[expression.name] = value
        return value
    }

    override fun visitSuperExpression(expression: Expression.Super): Any? {
        val distance = locals[expression]!!
        val superclass = environment.getAt(distance, "super") as LoxClass
        val obj = environment.getAt(distance - 1, "this") as LoxInstance

        return superclass.findMethod(expression.method.lexeme)?.bind(obj)
            ?: throw RuntimeError(expression.method, "Undefined property '${expression.method.lexeme}'.")
    }

    override fun visitThisExpression(expression: Expression.This) = lookUpVariable(expression.keyword, expression)

    override fun visitLogicalExpression(expression: Expression.Logical): Any? {
        val left = evaluate(expression.left)
        return if ((expression.operator.type == OR && isTruthy(left)) || !isTruthy(left)) return left
        else evaluate(expression.right)
    }

    override fun visitUnaryExpression(expression: Expression.Unary): Any? {
        val right = evaluate(expression.right)
        return when (expression.operator.type) {
            MINUS -> {
                checkNumberOperands(expression.operator, right)
                -(right as Double)
            }
            BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitBinaryExpression(expression: Expression.Binary): Any? {
        val left = evaluate(expression.left)
        val right = evaluate(expression.right)

        return when (expression.operator.type) {
            BANG_EQUAL -> !isEqual(left, right)
            EQUAL_EQUAL -> isEqual(left, right)
            PLUS -> {
                if (left is Double && right is Double) left + right
                else if (left is String || right is String) stringify(left) + stringify(right)
                else throw RuntimeError(expression.operator, "Operands must be two numbers or strings.")
            }
            else -> {
                if (left is Double && right is Double) binaryExpressionWithNumbers(expression.operator, left, right)
                else throw RuntimeException("Encountered illegal state while evaluating binary expression!")
            }
        }
    }

    override fun visitTernaryExpression(expression: Expression.Ternary): Any? {
        val condition = evaluate(expression.condition)
        return evaluate(if (isTruthy(condition)) expression.isTrue else expression.isFalse)
    }

    private fun binaryExpressionWithNumbers(operator: Token, left: Double, right: Double): Any? {
        checkNumberOperands(operator, left, right)
        return when (operator.type) {
            GREATER -> left > right
            GREATER_EQUAL -> left >= right
            LESS -> left <= right
            LESS_EQUAL -> left <= right
            MINUS -> left - right
            STAR -> left * right
            SLASH -> {
                if (right == 0.0) throw RuntimeError(operator, "Division by zero is not allowed!")
                else left / right
            }
            else -> throw RuntimeException("Encountered illegal state while evaluating binary expression!")
        }
    }


    // Everything else, mostly helpers

    private fun evaluate(expression: Expression) = expression.accept(this)

    private fun checkNumberOperands(operator: Token, operandLeft: Any?, operandRight: Any? = operandLeft) {
        if (!(operandLeft is Double && operandRight is Double))
            throw RuntimeError(operator, "Operand[s] must be a number.")
    }

    private fun lookUpVariable(name: Token, expression: Expression): Any? {
        val distance = locals[expression]
        return if (distance != null) environment.getAt(distance, name.lexeme)
        else globals.get(name)
    }

    private fun isTruthy(obj: Any?) = if (obj is Boolean) obj else (obj != null)

    private fun isEqual(a: Any?, b: Any?) = (a == null && b == null) || (a?.equals(b) ?: false)

    private fun stringify(obj: Any?) =
        when (obj) {
            null -> "nil"
            is Double -> {
                var text = obj.toString()
                if (text.endsWith(".0")) text = text.substring(0, text.length - 2)
                text
            }
            else -> obj.toString()
        }
}