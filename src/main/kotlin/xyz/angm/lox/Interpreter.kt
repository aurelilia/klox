package xyz.angm.lox

import xyz.angm.lox.TokenType.*

class Interpreter : Expression.Visitor<Any?>, Statement.Visitor<Unit> {

    private var environment = Environment()

    fun interpret(statements: List<Statement>) {
        try {
            statements.forEach(::execute)
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    private fun execute(statement: Statement) = statement.accept(this)

    private fun executeBlock(statements: List<Statement>, environment: Environment) {
        val prevEnv = this.environment
        try {
            this.environment = environment
            statements.forEach(::execute)
        } finally {
            this.environment = prevEnv
        }
    }

    // Statement Visitors

    override fun visitBlockStatement(statement: Statement.Block) = executeBlock(statement.statements, Environment(environment))

    override fun visitExpressionStatement(statement: Statement.Expression) {
        evaluate(statement.expression)
    }

    override fun visitIfStatement(statement: Statement.If) {
        if (isTruthy(evaluate(statement.condition))) execute(statement.thenBranch)
        else execute(statement.elseBranch ?: return)
    }

    override fun visitPrintStatement(statement: Statement.Print) = println(stringify(evaluate(statement.expression)))

    override fun visitVarStatement(statement: Statement.Var) {
        var initValue: Any? = Environment.Unassigned
        if (statement.initializer != null) initValue = evaluate(statement.initializer)
        environment.define(statement.name.lexeme, initValue)
    }

    // Expression Visitors

    override fun visitLiteralExpression(expression: Expression.Literal) = expression.value

    override fun visitVariableExpression(expression: Expression.Variable) = environment.get(expression.name)

    override fun visitAssignExpression(expression: Expression.Assign) = environment.assign(expression.name, evaluate(expression.value))

    override fun visitGroupingExpression(expression: Expression.Grouping) = evaluate(expression.expression)

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