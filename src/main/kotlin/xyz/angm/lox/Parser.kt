package xyz.angm.lox

import xyz.angm.lox.TokenType.*
import java.lang.RuntimeException

class Parser(private val tokens: List<Token>) {

    private var current = 0

    fun parse(): Expression? {
        try {
            return expression()
        } catch (error: ParseError) {
            return null
        }
    }

    private fun expression() = ternary()

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
        } else primary()
    }

    private fun primary() = when {
        match(FALSE) -> Expression.Literal(false)
        match(TRUE) -> Expression.Literal(true)
        match(NIL) -> Expression.Literal(null)
        match(NUMBER, STRING) -> Expression.Literal(previous().literal)

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

    private fun syncronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
            }
        }
        advance()
    }

    // Simple helper function for generating left-associative binary operators in series
    private fun generateBinaryParser(nextOperator: () -> Expression, vararg types: TokenType): Expression {
        var expression = nextOperator()
        while (match(*types)) {
            val operator = previous()
            val right = nextOperator()
            expression = Expression.Binary(expression, operator, right)
        }
        return expression
    }

    private class ParseError : RuntimeException()
}