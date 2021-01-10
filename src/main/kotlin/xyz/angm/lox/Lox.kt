package xyz.angm.lox

import xyz.angm.lox.ast.LoxFunction
import xyz.angm.lox.ast.LoxRootNode
import xyz.angm.lox.ast.expression.*
import xyz.angm.lox.ast.statement.IfNode
import xyz.angm.lox.ast.statement.ReturnNode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


const val MAX_FUNCTION_ARGS = 16


private val interpreter = Interpreter()

private var hadError = false
private var hadRuntimeError = false

fun main(args: Array<String>) {
    // Print fib(40)
    val fnbody = FunctionBodyNode(null)
    val function = LoxFunction(LoxRootNode(fnbody))
    val body = IfNode(
        LessThanNodeGen.create(ReadArgumentNode(0), IntLiteralNode(2)),
        ReturnNode(ReadArgumentNode(0)),
        ReturnNode(
            AddNodeGen.create(
                CallNode(
                    function,
                    arrayOf(SubNodeGen.create(ReadArgumentNode(0), IntLiteralNode(2)))
                ),
                CallNode(
                    function,
                    arrayOf(SubNodeGen.create(ReadArgumentNode(0), IntLiteralNode(1)))
                ),
            )
        )
    )
    fnbody.body = body

    val callnode = CallNode(
        function,
        arrayOf(IntLiteralNode(40))
    )
    val time = System.currentTimeMillis()
    println(GraalVM.exec(callnode))
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