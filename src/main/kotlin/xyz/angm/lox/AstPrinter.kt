package xyz.angm.lox

class AstPrinter : Expression.Visitor<String> {

    fun print(expression: Expression) = expression.accept(this)

    override fun visitBinaryExpression(expression: Expression.Binary) =
        parenthesize(expression.operator.lexeme, expression.left, expression.right)

    override fun visitGroupingExpression(expression: Expression.Grouping) =
        parenthesize("group", expression.expression)

    override fun visitLiteralExpression(expression: Expression.Literal) =
        if (expression.value == null) "nil" else expression.value.toString()

    override fun visitUnaryExpression(expression: Expression.Unary) =
        parenthesize(expression.operator.lexeme, expression.right)

    override fun visitTernaryExpression(expression: Expression.Ternary) =
        "${expression.condition.accept(this)} ? ${expression.isTrue.accept(this)} : ${expression.isFalse.accept(this)}"

    private fun parenthesize(name: String, vararg exprs: Expression): String {
        val builder = StringBuilder()

        builder.append("(").append(name)
        exprs.forEach { expr ->
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")

        return builder.toString()
    }
}