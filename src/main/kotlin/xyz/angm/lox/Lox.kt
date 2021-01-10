package xyz.angm.lox

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


const val MAX_FUNCTION_ARGS = 16


private val interpreter = Interpreter()

private var hadError = false
private var hadRuntimeError = false

fun main(args: Array<String>) {
    val source = Source.newBuilder(LOX, File(args[0])).build()
    val context = Context.newBuilder(LOX).`in`(System.`in`).out(System.out).build();
    println("== running on " + context.engine)

    val time = System.currentTimeMillis()
    context.eval(source)
    println(System.currentTimeMillis() - time)
}

fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while (true) {
        print("> ")
        run(reader.readLine())
        hadError = false
    }
}

fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes, Charset.defaultCharset()))
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val statements = parser.parse()

    if (hadError) return

    val resolver = Resolver(interpreter)
    resolver.resolve(statements)

    if (hadError) return

    interpreter.interpret(statements)
}

object Lox {

    fun error(line: Int, message: String, where: String = "") {
        println("[PR][Line $line] Error$where: $message")
        hadError = true
    }

    fun error(token: Token, message: String) {
        if (token.type === TokenType.EOF) error(token.line, message, " at end")
        else error(token.line, message, " at '" + token.lexeme + "'")
    }

    fun runtimeError(error: RuntimeError) {
        println("[RT][Line ${error.token.line}] ${error.message}")
        hadRuntimeError = true
    }
}