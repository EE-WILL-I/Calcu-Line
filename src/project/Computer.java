package project;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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
            opType = ((Operator) op).getType();
            priority = ((Operator) op).getPriority();
            vRight = v2.value;
            queries[0] = v1;
            queries[1] = op;
            queries[2] = v2;
        }

        public static void debug(Operation op) {
            System.out.println(op.vLeft + " " + op.opType.toString() + " " + op.vRight + " (" + op.priority + ")");
        }

        public Query compute() {
//            if (vLeft.getClass() == String.class && vRight.getClass() == Double.class) {
//                vLeft = 0d;
//            }
//            else if (vLeft.getClass() == Double.class && vRight.getClass() == String.class) {
//                vRight = 0d;
//            }
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
            }
            else {
                return new VarStatement(vLeft.toString() + vRight.toString());
            }
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
        funcs.put(Functions.log, "log");

        XMLReader.READER.subscribe(onConfigChangedHandler);
        readConfigs();

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
        lg,
        log,
        ln
    }
    public Map<Operations, String> signs = new HashMap<Operations, String>();
    public Map<Functions, String> funcs = new HashMap<Functions, String>();
    private Consumer<Events.EventArgs> onConfigChangedHandler = (eventArgs -> {readConfigs();});
    private int MAX_ITERATION_COUNT;

    public String computeQuerySequence(QuerySequence querySequence) {
        int curIt = 1;
        while(querySequence.getSequence().size() > 1) {
            System.out.println("Iteration: " + curIt);
            querySequence = computeStep(querySequence);
            curIt++;
            if(curIt > MAX_ITERATION_COUNT) break;
        }

        StringBuilder out = new StringBuilder();
        for(Object q: querySequence.getSequence()) {
            if(q.getClass() == QuerySequence.ConstStatement.class) out.append(((((Query) q).getValue())));
            else if(q.getClass() == QuerySequence.ParamStatement.class) out.append(((((Query) q).getValue())));
            else if(q.getClass() == QuerySequence.VarStatement.class) out.append((String) (((VarStatement) q).getValue()));
            else if(q.getClass() == Operator.class) out.append(signs.get(((Operator) q).getType()));
        }
        return out.toString();
    }
    private QuerySequence computeStep(QuerySequence querySequence) {
        QuerySequence.debug(querySequence);

        querySequence = computeMinuses(querySequence);
        querySequence = executeFunctionQueries(querySequence);
        ArrayList<Operation> operations = getOperationQuery(querySequence);
        Operation mostPriorityOperation = getMostPriorityOperation(operations);

        ConstStatement opRes = new QuerySequence.ConstStatement(mostPriorityOperation.compute());
        querySequence = replace(querySequence, mostPriorityOperation.queries, opRes);
        return querySequence;
    }

    private ArrayList<Operation> getOperationQuery(QuerySequence querySequence) {
        ArrayList<Operation> operationQuery = new ArrayList<Operation>();
        int bias = 0, k = 3;

        for (int i = 0; i <= querySequence.getSequence().size() - k; i++) {
            ArrayList<Query> buffer = new ArrayList<Query>();
            for (int j = i; j < i + k; j++) {
                if (querySequence.getSequence().toArray()[j].getClass() == Operator.class) {
                    Operator op = (Operator) querySequence.getSequence().toArray()[j];
                    if (op.getType() == Operations.opInc && !op.isChecked) {
                        bias += op.getPriority();
                        op.isChecked = true;
                        i += (i < querySequence.getSequence().size() - k) ? 1 : 0;
                    } else if (op.getType() == Operations.opDec && !op.isChecked) {
                        bias -= op.getPriority();
                        op.isChecked = true;
                        i += (i < querySequence.getSequence().size() - k) ? 1 : 0;
                    }
                    else {
                        if (j == i) break;
                        Query q = (Query) querySequence.getSequence().toArray()[j];
                        ((Operator) q).addPriority(bias);
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
            if (query.getClass() == Operator.class) ((Operator) query).isChecked = false;
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
        ArrayList<Query> QS = sequence.getSequence();
        for(int i = 0; i < QS.size(); i++) {
            QuerySequence args = new QuerySequence();
            int j = 0;
            if(QS.get(i).queryType == QueryType.Function) {
                if(QS.get(i + 1).queryType == QueryType.Operator && ((Operator)QS.get(i + 1)).getType() == Operations.opInc) {
                    boolean flag = true;
                    int bias = 1;
                    for(j = i + 2; (j < QS.size() && flag); j++) {
                        if(QS.get(j).queryType == QueryType.Operator && ((Operator) QS.get(j)).getType() == Operations.opInc)
                            bias++;
                        else if(QS.get(j).queryType == QueryType.Operator && ((Operator) QS.get(j)).getType() == Operations.opDec)
                            bias--;
                        if(bias < 1) flag = false;
                        else args.addQuery(QS.get(j));
                    }
                }
                ArrayList<Query> tmp = new ArrayList<Query>(args.getSequence());
                ConstStatement arg;
                try {
                    arg = new ConstStatement(Double.parseDouble(computeQuerySequence(args)));
                } catch (NumberFormatException e) {
                    arg = new ConstStatement(0d);
                    Controller.CONTROLLER.setInfo(XMLReader.READER.getTextById(Controller.CONTROLLER.localization, "015"));
                }
                ((QuerySequence.Function)QS.get(i)).addArg(arg);
                ConstStatement functionResult = ((QuerySequence.Function)QS.get(i)).execute();

                Query[] _args = new Query[tmp.size()];
                for(int k = 0; k < tmp.size(); k++) _args[k] = tmp.get(k);
                try {
                    sequence.removeQuery(QS.get(j - 1));
                    sequence.removeQuery(QS.get(i));
                    sequence.removeQuery(_args);
                } catch (IndexOutOfBoundsException e) {
                    Controller.CONTROLLER.setInfo(XMLReader.READER.getTextById(Controller.CONTROLLER.localization, "017"));
                }
                sequence.addQuery(i, functionResult);
            }
        }
        return sequence;
    }
    private QuerySequence computeMinuses(QuerySequence querySequence) {
        int k = 1;
        for (int i = 0; i <= querySequence.getSequence().size() - k; i++) {
            if (querySequence.getSequence().get(i).getClass() == Operator.class) {
                Operator op = (Operator) querySequence.getSequence().get(i);
                if (op.getType() == Operations.sub && (i - 1 >= 0 && querySequence.getSequence().get(i - 1).getClass() == Operator.class && ((Operator) querySequence.getSequence().get(i - 1)).getType() == Operations.opInc || i == 0)) {
                    if(querySequence.getSequence().get(i + 1).queryType == QueryType.Statement)
                        ((Statement) querySequence.getSequence().get(i + 1)).invertValue();
                    else if(querySequence.getSequence().get(i + 1).queryType == QueryType.Function)
                        ((Function) querySequence.getSequence().get(i + 1)).setNegative(true);
                    querySequence.removeQuery(querySequence.getSequence().get(i));
                }
            }
        }
        return querySequence;
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
                try {
                    if ((QS.get(i - 1).queryType == QueryType.Operator && ((Operator) QS.get(i - 1)).getType() == Operations.opInc) && (QS.get(i + 1).queryType == QueryType.Operator && ((Operator) QS.get(i + 1)).getType() == Operations.opDec)) {
                        QS.remove(i - 1);
                        QS.remove(i);
                    }
                }
                catch (ArrayIndexOutOfBoundsException e) {}
        }
        for (int n = 0; n < QS.size() - 1; n++) {
            if ((QS.get(n).queryType == QueryType.Operator) && (QS.get(n + 1).queryType == QueryType.Operator))
                if ((((Operator) QS.get(n)).getType() == Operations.opInc) && (((Operator) QS.get(n + 1)).getType() == Operations.opDec)) {
                    sequence.removeQuery(((Query) QS.toArray()[n]));
                    sequence.removeQuery(((Query) QS.toArray()[n]));
                } //()[0-9] query check;
        }
        if (QS.get(0).queryType == QueryType.Operator && ((Operator) QS.get(0)).getType() == Operations.opInc ) {
            int j, bias = 1;
            boolean flag = true;
            for(j = 1; (j < QS.size() && flag); j++) {
                if(QS.get(j).queryType == QueryType.Operator && ((Operator) QS.get(j)).getType() == Operations.opInc)
                    bias++;
                boolean check = (QS.get(j).queryType == QueryType.Operator && ((Operator) QS.get(j)).getType() == Operations.opDec);
                if(check) bias--;
                if(bias < 1) flag = false;
            }
            if(j >= QS.size() - 1 && (QS.get(QS.size() - 1).queryType == QueryType.Operator && ((Operator) QS.get(QS.size() - 1)).getType() == Operations.opDec)) {
                sequence.removeQuery(QS.get(0));
                sequence.removeQuery(QS.get(QS.size() - 1));
            }
        } //([0-9]) query check;
        if(QS.size() < 3) {
            for(i = 0; i < QS.size(); i++) if(QS.get(i).queryType == QueryType.Operator) QS.remove(QS.get(i));
        }
        return sequence;
    }
    public void readConfigs() {
        System.out.println("reading params for " + this.getClass().toString());
        MAX_ITERATION_COUNT = Integer.parseInt(XMLReader.READER.getParameterById("300"));
    }
}