package xyz.angm.lox

import xyz.angm.lox.TokenType.*
import java.util.Arrays.asList

class Parser(private val tokens: List<Token>) {

    private var current = 0

    fun parse(): List<Statement> {
        val statements = ArrayList<Statement>()
        while (!isAtEnd()) statements.add(declaration() ?: continue)
        return statements
    }

    private fun declaration(): Statement? {
        return try {
            when {
                match(CLASS) -> classDeclaration()
                match(FUN) -> function("function")
                match(VAR) -> varDeclaration()
                else -> statement()
            }
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    private fun classDeclaration(): Statement.Class {
        val name = consume(IDENTIFIER, "Expected class name.")
        val superclass = if (match(LESS)) Expression.Variable(consume(IDENTIFIER, "Expected name of superclass.")) else null

        consume(LEFT_BRACE, "Expected '{' before class body.")

        val methods = ArrayList<Statement.Function>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) methods.add(function("method"))
        consume(RIGHT_BRACE, "Expected '}' after class body.")

        return Statement.Class(name, superclass, methods)
    }

    private fun function(kind: String): Statement.Function {
        val name = consume(IDENTIFIER, "Expected $kind name.")

        consume(LEFT_PAREN, "Expected '(' after $kind name.")
        val parameters = ArrayList<Token>()
        if (!check(RIGHT_PAREN)) {
            do parameters.add(consume(IDENTIFIER, "Expected parameter name."))
            while (match(COMMA))
            if (parameters.size > MAX_FUNCTION_ARGS) error(peek(), "Cannot have more than $MAX_FUNCTION_ARGS arguments.")
        }
        consume(RIGHT_PAREN, "Expected ')' after parameters.")

        consume(LEFT_BRACE, "Expected '{' before $kind body.")
        val body = block()
        return Statement.Function(name, parameters, body)
    }

    private fun varDeclaration(): Statement {
        val name = consume(IDENTIFIER, "Expected variable name.")
        var initializer: Expression? = null
        if (match(EQUAL)) initializer = expression()

        consume(SEMICOLON, "Expected ';' after variable declaration.")
        return Statement.Var(name, initializer)
    }

    private fun statement() =
        when {
            match(LEFT_BRACE) -> Statement.Block(block())
            match(IF) -> ifStatement()
            match(WHILE) -> whileStatement()
            match(FOR) -> forStatement()
            match(RETURN) -> returnStatement()
            else -> expressionStatement()
        }

    private fun block(): List<Statement> {
        val statements = ArrayList<Statement>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) statements.add(declaration()!!)

        consume(RIGHT_BRACE, "Expected '}' after block.")
        return statements
    }

    private fun ifStatement(): Statement {
        consume(LEFT_PAREN, "Expected '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expected ')' after if condition.")
        val thenBranch = statement()
        return Statement.If(condition, thenBranch, elseBranch = if (match(ELSE)) statement() else null)
    }

    private fun whileStatement(): Statement {
        consume(LEFT_PAREN, "Expected '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expected ')' after while condition.")
        val body = statement()
        return Statement.While(condition, body)
    }

    private fun forStatement(): Statement {
        consume(LEFT_PAREN, "Expected '(' after 'for'.")
        val initializer = when {
            match(SEMICOLON) -> null
            match(VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        val condition = if (!check(SEMICOLON)) expression() else Expression.Literal(true)
        consume(SEMICOLON, "Expect ';' after for condition.")
        val increment = if (!check(RIGHT_PAREN)) expression() else null
        consume(RIGHT_PAREN, "Expected ')' after for increment.")
        var body = statement()

        if (increment != null) {
            body = Statement.Block(asList<Statement>(
                body, Statement.Expression(increment)
            ))
        }
        body = Statement.While(condition, body)

        return if (initializer != null) Statement.Block(asList<Statement>(initializer, body))
        else body
    }

    private fun returnStatement(): Statement {
        val keyword = previous()
        val value = if (!check(SEMICOLON)) expression() else null
        consume(SEMICOLON, "Expect ';' after return value.")
        return Statement.Return(keyword, value)
    }

    private fun expressionStatement(): Statement {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Statement.Expression(value)
    }

    private fun expression() = assignment()

    private fun assignment(): Expression {
        val expression = logicOr()
        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            when (expression) {
                is Expression.Variable -> return Expression.Assign(expression.name, value)
                is Expression.Get -> return Expression.Set(expression.obj, expression.name, value)
                else -> error(equals, "Invalid assignment target.")
            }
        }
        return expression
    }

    private fun logicOr() = generateBinaryParser(::logicAnd, OR, logical = true)

    private fun logicAnd() = generateBinaryParser(::ternary, AND, logical = true)

    private fun ternary(): Expression {
        var expression = equality()
        while (match(QUESTION)) {
            val isTrue = equality()
            advance()
            val isFalse = equality()
            expression = Expression.Ternary(expression, isTrue, isFalse)
        }
        return expression
    }

    private fun equality() = generateBinaryParser(::comparison, BANG_EQUAL, EQUAL_EQUAL)

    private fun comparison() = generateBinaryParser(::addition, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)

    private fun addition() = generateBinaryParser(::multiplication, MINUS, PLUS)

    private fun multiplication() = generateBinaryParser(::unary, SLASH, STAR)

    private fun unary(): Expression {
        return if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            Expression.Unary(operator, right)
        } else call()
    }

    private fun call(): Expression {
        var expression = primary()
        while (true) {
            if (match(LEFT_PAREN)) expression = finishCall(expression)
            else if (match(DOT)) expression = Expression.Get(expression, consume(IDENTIFIER, "Expected property name after '.'."))
            else break
        }
        return expression
    }

    private fun finishCall(callee: Expression): Expression {
        val arguments = ArrayList<Expression>()
        if (!check(RIGHT_PAREN)) {
            do arguments.add(expression())
            while (match(COMMA))
            if (arguments.size > MAX_FUNCTION_ARGS) error(peek(), "Cannot have more than $MAX_FUNCTION_ARGS arguments.")
        }

        val paren = consume(RIGHT_PAREN, "Expected ')' after arguments.")
        return Expression.Call(callee, paren, arguments)
    }

    private fun primary() =
        when {
            match(FALSE) -> Expression.Literal(false)
            match(TRUE) -> Expression.Literal(true)
            match(NIL) -> Expression.Literal(null)
            match(NUMBER, STRING) -> Expression.Literal(previous().literal)
            match(IDENTIFIER) -> Expression.Variable(previous())
            match(THIS) -> Expression.This(previous())

            match(SUPER) -> {
                val keyword = previous()
                consume(DOT, "Expected '.' after 'super'.")
                val method = consume(IDENTIFIER, "Expected superclass method name.")
                Expression.Super(keyword, method)
            }

            match(LEFT_PAREN) -> {
                val expression = expression()
                consume(RIGHT_PAREN, "Expected ')' after expression.")
                Expression.Grouping(expression)
            }

            else -> throw error(peek(), "Expected primary expression.")
        }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun check(type: TokenType) = if (isAtEnd()) false else peek().type == type

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]

    private fun isAtEnd() = peek().type == EOF

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, RETURN -> return
            }
        }
        advance()
    }

    // Simple helper function for generating left-associative binary operators in series
    private fun generateBinaryParser(nextOperator: () -> Expression, vararg types: TokenType, logical: Boolean = false): Expression {
        var expression = nextOperator()
        while (match(*types)) {
            val operator = previous()
            val right = nextOperator()
            expression = if (logical) Expression.Logical(expression, operator, right) else Expression.Binary(expression, operator, right)
        }
        return expression
    }

    private class ParseError : RuntimeException()
}