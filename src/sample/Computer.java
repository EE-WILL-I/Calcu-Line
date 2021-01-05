package sample;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Computer extends QuerySequence {
    public static Computer COMPUTER;

    public Computer() {
        signs.put(Func.sum,   "+");
        signs.put(Func.sub,   "-");
        signs.put(Func.mult,  "*");
        signs.put(Func.div,   "/");
        signs.put(Func.opInc, "(");
        signs.put(Func.opDec, ")");
        if (COMPUTER == null) {
            COMPUTER = this;
        } else {
            throw new RuntimeException("Cannot open computer");
        }
    }
    public enum Func{
        sum,
        sub,
        mult,
        div,
        opInc,
        opDec
    }
    public Map<Func, String> signs = new HashMap<Func, String>();

    private static class Operation {
        public Object vLeft, vRight;
        public Query[] queries = new Query[3];
        public Func opType;
        public int priority;

        Operation() {}
        Operation(Query v1, Query op, Query v2) {
            vLeft = v1.value;
            opType = ((QuerySequence.Operation) op).getType();
            priority = ((QuerySequence.Operation) op).getPriority();
            vRight = v2.value;
            queries[0] = v1;
            queries[1] = op;
            queries[2] = v2;
        }

        public static void debug(Operation op) {
            System.out.println(op.vLeft + " " + op.opType.toString() + " " + op.vRight + " (" + op.priority + ")");
        }

        public Query compute() {
            if (vLeft.getClass() == Double.class && vRight.getClass() == Double.class) {
                double v1, v2;
                v1 = (double) vLeft;
                v2 = (double) vRight;
                switch (opType) {
                    case sum: {
                        return new QuerySequence.ConstStatement(v1 + v2);
                    }
                    case sub: {
                        return new QuerySequence.ConstStatement(v1 - v2);
                    }
                    case mult: {
                        return new QuerySequence.ConstStatement(v1 * v2);
                    }
                    case div: {
                        return new QuerySequence.ConstStatement(v1 / v2);
                    }
                    default:
                        return new Query();
                }
            } else if (vLeft.getClass() == String.class && vRight.getClass() == String.class) {
                /*String v1, v2;
                v1 = (String) vLeft;
                v2 = (String) vRight;
                switch (opType) {
                    case sum: {
                        return new QuerySequence.VarStatement(v1 + "+" + v2);
                    }
                    case sub: {
                        return new QuerySequence.VarStatement(v1 + "-" + v2);
                    }
                    case mult: {
                        return new QuerySequence.VarStatement(v1 + "*" + v2);
                    }
                    case div: {
                        return new QuerySequence.VarStatement(v1 + "/" + v2);
                    }
                    default:
                        return new Query();
                }*/
                return new Query();
            }
            return new Query();
        }
    }

    public String readQuerySequence(QuerySequence querySequence) {
       QuerySequence QS = querySequence;
        while(QS.getSequence().size() > 1) {
            QS = computeStep(QS);
        }

        String out = "";
        for(Object q: QS.getSequence()) {
            if(q.getClass() == QuerySequence.ConstStatement.class) out += Double.toString((Double)(((Query)q).getValue()));
            else if(q.getClass() == QuerySequence.ParamStatement.class) out += Double.toString((Double)(((Query)q).getValue()));
            else if(q.getClass() == QuerySequence.VarStatement.class) out += (String)(((VarStatement) q).getValue());
            else if(q.getClass() == QuerySequence.Operation.class) out += signs.get(((QuerySequence.Operation)q).getType());
        }
        return out;
    }
    private QuerySequence computeStep(QuerySequence querySequence) {
        QuerySequence.debug(querySequence);

        ArrayList<Operation> operations = getOperationQuery(querySequence);
        Operation mostPriorityOperation = getMostPriorityOperation(operations);

        replace(querySequence, mostPriorityOperation.queries, new QuerySequence.ConstStatement(mostPriorityOperation.compute()));
        return querySequence;
    }

    private ArrayList<Operation> getOperationQuery(QuerySequence querySequence) {
        ArrayList<Operation> operationQuery = new ArrayList<Operation>();
        int bias = 0, k = 3;

        for(int i = 0; i <= querySequence.getSequence().size() - k; i++) {
            ArrayList<Query> buffer = new ArrayList<Query>();
            for(int j = i; j < i + k; j++) {
                if (querySequence.getSequence().toArray()[j].getClass() == QuerySequence.Operation.class) {
                    QuerySequence.Operation op = (QuerySequence.Operation) querySequence.getSequence().toArray()[j];
                    if (op.getType() == Func.opInc && !op.isChecked) {
                        bias += op.getPriority();
                        op.isChecked = true;
                        i++;
                    } else if (op.getType() == Func.opDec) {
                        bias -= op.getPriority();
                        op.isChecked = true;
                        i++;
                    }
                    else {
                        if(j == i) break;
                        Query q = (Query) querySequence.getSequence().toArray()[j];
                        ((QuerySequence.Operation) q).addPriority(bias);
                        buffer.add(q);
                    }
                }
                else if(querySequence.getSequence().toArray()[j].getClass() == QuerySequence.VarStatement.class) {
                    buffer.add((Query) querySequence.getSequence().toArray()[j]);
                }
                else {
                    buffer.add((Query) querySequence.getSequence().toArray()[j]);
                }
            }
            if(buffer.size() == 3)
                operationQuery.add(new Operation(buffer.get(0), buffer.get(1), buffer.get(2)));
        }
        for(Query query: querySequence.getSequence()) if(query.getClass() == QuerySequence.Operation.class) ((QuerySequence.Operation)query).isChecked = false;
        for(Operation operation: operationQuery) Operation.debug(operation);
        return operationQuery;
    }
        private Operation getMostPriorityOperation(@NotNull ArrayList<Operation> operations) {
            int maxPr = 0;
            if(operations.size() > 0) {
                Operation maxPrOp = operations.get(0);
                for (Operation op : operations)
                    if (op.priority > maxPr) {
                        maxPr = op.priority;
                        maxPrOp = op;
                    }
                System.out.println("Most priority: ");
                Operation.debug(maxPrOp);
                return maxPrOp;
            }
            return new Operation();
        }
        private QuerySequence replace(QuerySequence sequence, Query[] queries, Query newQuery) {
        int i, s = sequence.getSequence().size();
            for (i = 0; i < s; i++) {
                if (sequence.getSequence().toArray()[i] == queries[0]) {
                    break;
                }
            }
            for (int j = 0; j < 3; j++)
                if(sequence.getSequence().get(i) == queries[j])
                    sequence.getSequence().remove(i);
                else {
                    i++;
                    j--;
                }
            sequence.getSequence().add(i, newQuery);
            for(int n = 0; n < sequence.getSequence().size() - 1; n++) {
                if ((((Query) sequence.getSequence().toArray()[n]).queryType == QueryType.Operation) & (((Query) sequence.getSequence().toArray()[n + 1]).queryType == QueryType.Operation))
                    if ((((QuerySequence.Operation) ((Query) sequence.getSequence().toArray()[n])).getType() == Func.opInc) & (((QuerySequence.Operation) ((Query) sequence.getSequence().toArray()[n + 1])).getType() == Func.opDec)) {
                        sequence.removeQuery(((Query) sequence.getSequence().toArray()[n]));
                        sequence.removeQuery(((Query) sequence.getSequence().toArray()[n]));
                    } //()[0-9] query check;
                if (sequence.getSequence().get(0).queryType == QueryType.Operation && sequence.getSequence().get(1).queryType == QueryType.Statement && sequence.getSequence().get(2).queryType == QueryType.Operation) {
                    Query[] q = {sequence.getSequence().get(0), sequence.getSequence().get(1), sequence.getSequence().get(2)};
                    replace(sequence, q, sequence.getSequence().get(1));
                } //([0-9]) query check;
            }
            return sequence;
        }
}