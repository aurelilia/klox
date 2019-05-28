package xyz.angm.lox

import xyz.angm.lox.TokenType.*

private val numberRange = '0'.rangeTo('9')
private val alphaRangeSmall = 'a'.rangeTo('z')
private val alphaRangeBig = 'A'.rangeTo('Z')

class Scanner(private val source: String) {

    private val sourceChars = source.toCharArray()
    private val tokens = ArrayList<Token>()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '?' -> addToken(QUESTION)
            ':' -> addToken(COLON)

            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)

            ' ', '\r', '\t' -> Unit // Don't do anything
            '\n' -> line++
            '"' -> string()
            '/' -> handleSlash()
            else -> handleOtherToken(c)
        }
    }

    private fun handleSlash() {
        when {
            match('/') -> while (peek() != '\n' && !isAtEnd()) advance()
            match('*') -> blockComment()
            else -> addToken(SLASH)
        }
    }

    private fun handleOtherToken(c: Char) {
        when {
            isDigit(c) -> number()
            isAlpha(c) -> identifier()
            else -> Lox.error(line, "Unexpected character.")
        }
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
            return
        }

        advance() // Closing '"'

        val value = source.substring(start + 1, current - 1) // Trim surrounding '"'
        addToken(STRING, value)
    }

    private fun number() {
        while (isDigit(peek())) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance() // Consume the '.'
            while (isDigit(peek())) advance()
        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        addToken(Keywords[text] ?: IDENTIFIER)
    }

    private fun blockComment() {
        var nestLevel = 1
        while (nestLevel > 0 && !isAtEnd()) {
            if (peek() == '\n') line++
            else if (peek() == '/' && peekNext() == '*') nestLevel++
            else if (peek() == '*' && peekNext() == '/') nestLevel--
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated block comment.")
            return
        } else advance() // '/' of the closing '*/'
    }

    private fun addToken(tokenType: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(tokenType, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (sourceChars[current] != expected) return false

        current++
        return true
    }

    private fun advance(): Char {
        current++
        return sourceChars[current - 1]
    }

    private fun peek() = if (isAtEnd()) '\n' else sourceChars[current]

    private fun peekNext() = if (current + 1 >= source.length) '\n' else sourceChars[current + 1]

    private fun isAtEnd() = (current >= source.length)

    private fun isDigit(c: Char) = (c in numberRange)

    private fun isAlpha(c: Char) = (c in alphaRangeSmall) || (c in alphaRangeBig || c == '_')

    private fun isAlphaNumeric(c: Char) = (isDigit(c) || isAlpha(c))

    companion object Keywords : HashMap<String, TokenType>() {

        init {
            put("and", AND)
            put("class", CLASS)
            put("else", ELSE)
            put("false", FALSE)
            put("for", FOR)
            put("fun", FUN)
            put("if", IF)
            put("nil", NIL)
            put("or", OR)
            put("print", PRINT)
            put("return", RETURN)
            put("super", SUPER)
            put("this", THIS)
            put("true", TRUE)
            put("var", VAR)
            put("while", WHILE)
        }
    }
}
