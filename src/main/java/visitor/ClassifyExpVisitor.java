package visitor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;

public class IsSelectExpVisitor extends ExpressionVisitorAdapter {
    private Boolean result;

    @Override
    public void visit(EqualsTo expr) {
        Expression leftExpression = expr.getLeftExpression();
    }

    @Override
    public void visit(GreaterThan expr) {
        super.visit(expr);
    }

    @Override
    public void visit(GreaterThanEquals expr) {
        super.visit(expr);
    }

    @Override
    public void visit(MinorThan expr) {
        super.visit(expr);
    }

    @Override
    public void visit(MinorThanEquals expr) {
        super.visit(expr);
    }

    @Override
    public void visit(NotEqualsTo expr) {
        super.visit(expr);
    }
}
