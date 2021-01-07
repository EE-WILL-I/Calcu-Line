package project;

import java.util.ArrayList;

public class QuerySequence {
    protected enum QueryType{
        Statement,
        Operator,
        Function
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
        public void invertValue() {
            if(value.getClass() == Double.class) setValue((double)value * -1d);
            if(value.getClass() == String.class) setValue("-" + (String)value);
        }
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

        @Override
        public void invertValue() {
            setValue((double)value * -1d);
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
    public static class Operator extends Query {

        private Computer.Operations type;
        private int priority;
        public boolean isChecked = false;
        Operator(Computer.Operations f) {
            setType(f);
            queryType = QueryType.Operator;
            value = type.toString();
        }
        public void setType(Computer.Operations f) {
            switch (f) {
                case sum:
                {
                    type = Computer.Operations.sum;
                    priority = 1;
                    break;
                }
                case sub:
                {
                    type = Computer.Operations.sub;
                    priority = 1;
                    break;
                }
                case mult:
                {
                    type = Computer.Operations.mult;
                    priority = 2;
                    break;
                }
                case div:
                {
                    type = Computer.Operations.div;
                    priority = 2;
                    break;
                }
                case opInc:
                {
                    type = Computer.Operations.opInc;
                    priority = 3;
                    break;
                }
                case opDec:
                {
                    type = Computer.Operations.opDec;
                    priority = 3;
                    break;
                }
                case power: {
                    type = Computer.Operations.power;
                    priority = 3;
                    break;
                }
                default: break;
            }
        }
        public Computer.Operations getType() { return type; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        public void addPriority(int priority) { this.priority += priority; }
    }
    public static class Function extends Query {
        private Computer.Functions fType;
        private boolean isNegative = false;
        ArrayList<Statement> args = new ArrayList<Statement>(), parameters = new ArrayList<Statement>();
        Function(Statement arg, Computer.Functions fType) {
            this.args.add(arg);
            this.fType = fType;
            queryType = QueryType.Function;
        }
        Function(String val) {
            setValue(val);
            queryType = QueryType.Function;
        }
        Function(String val, Statement param) {
            setValue(val);
            parameters.add(param);
            queryType = QueryType.Function;
        }
        public ConstStatement execute() {
            double result = 0d;
            if (args.get(0).getClass() == ConstStatement.class)
                switch (fType) {
                    case root: {
                        result = Math.sqrt((Double) args.get(0).getValue());
                        break;
                    }
                    case lg: {
                        result = Math.log10((Double) args.get(0).getValue());
                        break;
                    }
                    case ln: {
                        result = Math.log((Double) args.get(0).getValue());
                        break;
                    }
                    case log: {
                        result = Math.log((Double) args.get(0).getValue()) / Math.log((Double)parameters.get(0).getValue());
                    }
                    default:
                        break;
                }
            return new ConstStatement(result * ((isNegative) ? -1 : 1));
        }
        public void setArgs(ArrayList<Statement> args) {
            this.args = args;
        }
        public  void addArg(Statement s) {
            args.add(s);
        }
        public void addParam(Statement param) {
            parameters.add(param);
        }
        public boolean setValue(Object val) {
            if(val.getClass() == String.class) {
                setFType(Reader.READER.funcs.get((String)val));
                value = (String)val;
                return true;
            }
            if(val.getClass() == double.class) {
                value = new ConstStatement((Double)val);
                return true;
            }
            return false;
        }
        public void setNegative(boolean val) { isNegative = val; }
        public void setFType(Computer.Functions fType) {
            this.fType = fType;
        }
    }

    private ArrayList<Query> QS = new ArrayList<Query>();
    QuerySequence(ArrayList<Query> arg) {
        setSequence(arg);
    }
    QuerySequence() {}
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
    public boolean addQuery(int pos, Query q) {
        try {
            QS.set(pos, q);
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
