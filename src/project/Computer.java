package project;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Computer extends QuerySequence {

    private static class Operation {
        public Object vLeft, vRight;
        public Query[] queries = new Query[3];
        public Operations opType;
        public int priority;

        Operation() {
            vLeft = 0d;
            vRight = 0d;
            opType = Operations.sum;
            priority = 0;
            queries[0] = null;
        }
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
                    case power: {
                        return new QuerySequence.ConstStatement(Math.pow(v1, v2));
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

    public static Computer COMPUTER;

    public Computer() {
        signs.put(Operations.sum,   "+");
        signs.put(Operations.sub,   "-");
        signs.put(Operations.mult,  "*");
        signs.put(Operations.div,   "/");
        signs.put(Operations.opInc, "(");
        signs.put(Operations.opDec, ")");
        signs.put(Operations.power, "^");

        funcs.put(Functions.root, "sqrt");
        funcs.put(Functions.lg, "lg");

        if (COMPUTER == null) {
            COMPUTER = this;
        } else {
            throw new RuntimeException("Cannot open computer");
        }
    }
    public enum Operations {
        sum,
        sub,
        mult,
        div,
        opInc,
        opDec,
        power
    }
    public enum Functions {
        root,
        lg
    }
    public Map<Operations, String> signs = new HashMap<Operations, String>();
    public Map<Functions, String> funcs = new HashMap<Functions, String>();

    public String computeQuerySequence(QuerySequence querySequence) {
       QuerySequence QS = querySequence;
        while(QS.getSequence().size() > 1) {
            QS = computeStep(QS);
        }

        StringBuilder out = new StringBuilder();
        for(Object q: QS.getSequence()) {
            if(q.getClass() == QuerySequence.ConstStatement.class) out.append(((((Query) q).getValue())));
            else if(q.getClass() == QuerySequence.ParamStatement.class) out.append(((((Query) q).getValue())));
            else if(q.getClass() == QuerySequence.VarStatement.class) out.append((String) (((VarStatement) q).getValue()));
            else if(q.getClass() == QuerySequence.Operation.class) out.append(signs.get(((QuerySequence.Operation) q).getType()));
        }
        return out.toString();
    }
    private QuerySequence computeStep(QuerySequence querySequence) {
        QuerySequence.debug(querySequence);


        querySequence = executeFunctionQueries(querySequence);
        ArrayList<Operation> operations = getOperationQuery(querySequence);
        Operation mostPriorityOperation = getMostPriorityOperation(operations);

        replace(querySequence, mostPriorityOperation.queries, new QuerySequence.ConstStatement(mostPriorityOperation.compute()));
        return querySequence;
    }

    private ArrayList<Operation> getOperationQuery(QuerySequence querySequence) {
        ArrayList<Operation> operationQuery = new ArrayList<Operation>();
        int bias = 0, k = 3;

        for (int i = 0; i <= querySequence.getSequence().size() - k; i++) {
            ArrayList<Query> buffer = new ArrayList<Query>();
            for (int j = i; j < i + k; j++) {
                if (querySequence.getSequence().toArray()[j].getClass() == QuerySequence.Operation.class) {
                    QuerySequence.Operation op = (QuerySequence.Operation) querySequence.getSequence().toArray()[j];
                    if (op.getType() == Operations.opInc && !op.isChecked) {
                        bias += op.getPriority();
                        op.isChecked = true;
                        i += (i < querySequence.getSequence().size() - k) ? 1 : 0;
                    } else if (op.getType() == Operations.opDec && !op.isChecked) {
                        bias -= op.getPriority();
                        op.isChecked = true;
                        i += (i < querySequence.getSequence().size() - k) ? 1 : 0;
                    } else {
                        if (j == i) break;
                        Query q = (Query) querySequence.getSequence().toArray()[j];
                        ((QuerySequence.Operation) q).addPriority(bias);
                        buffer.add(q);
                    }
                } else if (querySequence.getSequence().toArray()[j].getClass() == QuerySequence.VarStatement.class) {
                    buffer.add((Query) querySequence.getSequence().toArray()[j]);
                } else {
                    buffer.add((Query) querySequence.getSequence().toArray()[j]);
                }
            }
            if (buffer.size() == 3)
                operationQuery.add(new Operation(buffer.get(0), buffer.get(1), buffer.get(2)));
        }
        for (Query query : querySequence.getSequence())
            if (query.getClass() == QuerySequence.Operation.class) ((QuerySequence.Operation) query).isChecked = false;
        for (Operation operation : operationQuery) Operation.debug(operation);
        return operationQuery;
    }
    private Operation getMostPriorityOperation(@NotNull ArrayList<Operation> operations) {
        int maxPr = 0;
        if (operations.size() > 0) {
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
    private QuerySequence executeFunctionQueries(QuerySequence sequence) {
        QuerySequence args = new QuerySequence();
        for(int i = 0; i < sequence.getSequence().size(); i++) {
            if(sequence.getSequence().get(i).queryType == QueryType.Function) {
                if(sequence.getSequence().get(i + 1).queryType == QueryType.Operation && ((QuerySequence.Operation)sequence.getSequence().get(i + 1)).getType() == Operations.opInc) {
                    for(int j = i + 1; (j + 1 < sequence.getSequence().size() && !(sequence.getSequence().get(j + 1).queryType == QueryType.Operation && ((QuerySequence.Operation)sequence.getSequence().get(j + 1)).getType() == Operations.opDec)); j++) {
                        args.addQuery(sequence.getSequence().get(j));
                    }
                }
                ConstStatement arg = new ConstStatement(Double.parseDouble(computeQuerySequence(args)));
                ((QuerySequence.Function)sequence.getSequence().get(i)).addArg(arg);
                ConstStatement functionResult = ((QuerySequence.Function)sequence.getSequence().get(i)).execute();

                sequence.removeQuery((Query[])args.getSequence().toArray());
                sequence.removeQuery(sequence.getSequence().get(i));
                sequence.addQuery(i, functionResult);
            }
        }
        return sequence;
    }
    private QuerySequence replace(QuerySequence sequence, Query[] queries, Query newQuery) {
        ArrayList<Query> QS = sequence.getSequence();
        int i, s = sequence.getSequence().size();

        if(queries[0] != null) {
            for (i = 0; i < s; i++) {
                if (sequence.getSequence().toArray()[i] == queries[0]) {
                    break;
                }
            }
            for (int j = 0; j < 3; j++)
                if (QS.get(i) == queries[j])
                    QS.remove(i);
                else {
                    i++;
                    j--;
                }
            QS.add(i, newQuery);
        }
        for (int n = 0; n < QS.size() - 1; n++) {
            if ((QS.get(n).queryType == QueryType.Operation) && (QS.get(n + 1).queryType == QueryType.Operation))
                if ((((QuerySequence.Operation) QS.get(n)).getType() == Operations.opInc) && (((QuerySequence.Operation) QS.get(n + 1)).getType() == Operations.opDec)) {
                    sequence.removeQuery(((Query) QS.toArray()[n]));
                    sequence.removeQuery(((Query) QS.toArray()[n]));
                } //()[0-9] query check;
        }
        if (QS.get(0).queryType == QueryType.Operation && ((QuerySequence.Operation) QS.get(0)).getType() == Operations.opInc ) {
            int j, bias = 1;
            boolean flag = true;
            for(j = 1; (j < QS.size() && flag); j++) {
                if(QS.get(j).queryType == QueryType.Operation && ((QuerySequence.Operation) QS.get(j)).getType() == Operations.opInc)
                    bias++;
                boolean check = (QS.get(j).queryType == QueryType.Operation && ((QuerySequence.Operation) QS.get(j)).getType() == Operations.opDec);
                if(check) bias--;
                if(bias < 1) flag = false;
            }
            if(j >= QS.size() - 1) {
                sequence.removeQuery(QS.get(0));
                sequence.removeQuery(QS.get(QS.size() - 1));
            }
        } //([0-9]) query check;
        return sequence;
    }
}