package sample;

import java.util.ArrayList;

public class QuerySequence {
    protected enum QueryType{
        Statement,
        Operation
    }
    protected enum StatementType {
        Const,
        Var,
        Param
    }
    public static class Query {
        protected QueryType queryType;
        protected Object value = "null";
        public Object getValue() {
            return value;
        }
    }
    public static class Statement extends Query {
        protected StatementType statementType;
        public Statement() {

        }
        public Statement(Object val) {
            setValue(val);
        }
        public Statement(Query query) {
            setValue(query.getValue());
            statementType = StatementType.Const;
            queryType = QueryType.Statement;
        }
        public boolean setValue(Object val) {
            value = val;
            return true;
        };
    }
    public static class ConstStatement extends Statement {
        ConstStatement(double val) {
            value = val;
            queryType = QueryType.Statement;
            statementType = StatementType.Const;
        }
        public ConstStatement(Query query) {
            setValue(query.getValue());
            statementType = StatementType.Const;
            queryType = QueryType.Statement;
        }
        @Override
        public boolean setValue(Object val) {
            try {
                value = (double) val;
                return true;
            }
            catch (Exception e) { return false; }
        }
    }
    public static class ParamStatement extends Statement {
        public int refIndex = 0;
        ParamStatement(int val) {
            refIndex = val;
            queryType = QueryType.Statement;
            statementType = StatementType.Param;
        }
        @Override
        public boolean setValue(Object val) {
            try {
                if(val.getClass() == Double.class)
                    value = (double) val;
                else if(val.getClass() == String.class)
                    value = (String) val;
                else value = (double)0d;
                return true;
            }
            catch (Exception e) { return false; }
        }
    }
    public static class VarStatement extends Statement {
        VarStatement(String val) {
            value = val;
            queryType = QueryType.Statement;
            statementType = StatementType.Var;
        }
        @Override
        public boolean setValue(Object val) {
            try {
                value = (String) val;
                return true;
            }
            catch (Exception e) { return false; }
        }
    }
    public static class Operation extends Query {

        private Computer.Func type;
        private int priority;
        public boolean isChecked = false;
        Operation(Computer.Func f) {
            setType(f);
            queryType = QueryType.Operation;
            value = type.toString();
        }
        public void setType(Computer.Func f) {
            switch (f) {
                case sum:
                {
                    type = Computer.Func.sum;
                    priority = 1;
                    break;
                }
                case sub:
                {
                    type = Computer.Func.sub;
                    priority = 1;
                    break;
                }
                case mult:
                {
                    type = Computer.Func.mult;
                    priority = 2;
                    break;
                }
                case div:
                {
                    type = Computer.Func.div;
                    priority = 2;
                    break;
                }
                case opInc:
                {
                    type = Computer.Func.opInc;
                    priority = 2;
                    break;
                }
                case opDec:
                {
                    type = Computer.Func.opDec;
                    priority = 2;
                    break;
                }
                default: break;
            }
        }
        public Computer.Func getType() { return type; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        public void addPriority(int priority) { this.priority += priority; }
    }

    private ArrayList<Query> QS = new ArrayList<Query>();
    public boolean setSequence(ArrayList<Query> q) {
        try {
            QS = q;
            return true;
        }
        catch (Exception e) { return false; }
    }
    public boolean addQuery(Query q) {
        try {
            QS.add(q);
            return true;
        }
        catch (NullPointerException e) {
            System.out.println("Null query: " + q.value.toString());
            return false;
        }
    }
    public void removeQuery(Query q) {
        QS.remove(q);
    }
    public void removeQuery(Query[] qs) {
        for(Query q: qs) removeQuery(q);
    }
    public ArrayList<Query> getSequence() { return QS; }
    public static void debug(QuerySequence qs) {
        for (Query q: qs.getSequence()) {
            System.out.println(q.queryType.toString() + ": " + ((q.queryType == QueryType.Statement) ? ((Statement)q).statementType.toString() + " ": "") + q.getValue().toString());
        }
    }
}
