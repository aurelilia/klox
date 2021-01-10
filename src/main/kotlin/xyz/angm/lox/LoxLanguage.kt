package xyz.angm.lox

import com.oracle.truffle.api.CallTarget
import com.oracle.truffle.api.Truffle
import com.oracle.truffle.api.TruffleFile
import com.oracle.truffle.api.TruffleLanguage
import xyz.angm.lox.ast.LoxFunction
import xyz.angm.lox.ast.LoxRootNode
import xyz.angm.lox.ast.expression.*
import xyz.angm.lox.ast.statement.IfNode
import xyz.angm.lox.ast.statement.ReturnNode
import xyz.angm.lox.runtime.LoxContext

const val LOX = "lox"
const val MIME = "application/x-lox"

@TruffleLanguage.Registration(
    id = LOX,
    name = "Lox",
    defaultMimeType = MIME,
    characterMimeTypes = [MIME],
    contextPolicy = TruffleLanguage.ContextPolicy.SHARED,
    fileTypeDetectors = [LoxFileDetector::class]
)
class LoxLanguage : TruffleLanguage<LoxContext>() {
    override fun createContext(env: Env?) = LoxContext()

    override fun parse(request: ParsingRequest): CallTarget {
        val fnbody = FunctionBodyNode(null)
        val function = LoxFunction(LoxRootNode("fib", fnbody))
        val body = IfNode(
            LessThanNodeGen.create(ReadArgumentNode(0), IntLiteralNode(1)),
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

        val callnode = CallNode(function, arrayOf(IntLiteralNode(30)))
        val root = LoxRootNode("script", callnode)
        return Truffle.getRuntime().createCallTarget(root)
    }
}

class LoxFileDetector : TruffleFile.FileTypeDetector {
    override fun findMimeType(file: TruffleFile?) = if (file?.name?.endsWith(".lox") == true) MIME else null
    override fun findEncoding(file: TruffleFile?) = null
}