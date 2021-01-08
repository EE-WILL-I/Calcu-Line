package project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Reader  {
    public static Reader READER;

    public Map<String, Computer.Operations> signs = new HashMap<String, Computer.Operations>();
    public Map<String, Computer.Functions> funcs = new HashMap<String, Computer.Functions>();

    public Reader() {
        signs.put("+", Computer.Operations.sum);
        signs.put("-", Computer.Operations.sub);
        signs.put("*", Computer.Operations.mult);
        signs.put("/", Computer.Operations.div);
        signs.put("(", Computer.Operations.opInc);
        signs.put(")", Computer.Operations.opDec);
        signs.put("^", Computer.Operations.power);

        funcs.put("sqrt", Computer.Functions.root);
        funcs.put("lg", Computer.Functions.lg);
        funcs.put("ln", Computer.Functions.ln);
        funcs.put("log", Computer.Functions.log);

        XMLReader.READER.subscribe(onConfigChangedHandler);
        readConfigs();

        if (READER == null) {
            READER = this;
        } else {
            throw new RuntimeException("Cannot open reader");
        }
    }
    private Computer computer = new Computer();
    private ArrayList<IOLine> inputLines = new ArrayList<IOLine>();
    private Consumer<Events.EventArgs> onConfigChangedHandler = (eventArgs -> {readConfigs();});
    private int currentIOLIne = 0;
    public String PARAM_CHAR, FUNC_PARAM_CHARS;

    public void read(ArrayList<IOLine> inputLines) {
        this.inputLines = inputLines;
        for(currentIOLIne = 0; currentIOLIne < inputLines.size(); currentIOLIne++)
            if(!((IOLine)inputLines.toArray()[currentIOLIne]).getTF().getText().isEmpty()) {
                String result = Computer.COMPUTER.computeQuerySequence(readLine(this.inputLines.get(currentIOLIne)));
                Controller.CONTROLLER.IOLineList.get(currentIOLIne).setResult(result);
                System.out.println("RESULT: " + result);
            }
    }
    private  QuerySequence readLine(IOLine line) {
        String data = line.getTF().getText();
        return readLine(data);
    }
    private QuerySequence readLine(String data) {
        QuerySequence QS = new QuerySequence();
        StringBuilder buffer = new StringBuilder();

        for(int i = 0; i < data.length(); i++) {
            String c = Character.toString(data.charAt(i));

            if(c.matches("\\d") || c.matches("\\.") || c.matches(",")) {
                c = c.replace(',', '.');
                buffer.append(c);
                continue;
            }
            else {
                if(buffer.length() > 0) QS.addQuery(new QuerySequence.ConstStatement(Double.parseDouble(buffer.toString())));
                buffer.delete(0, buffer.length());
            }
            if(signs.containsKey(c))  {
                QS.addQuery(new QuerySequence.Operator(signs.get(c)));
                continue;
            }
            if(c.matches(PARAM_CHAR)) {
                String c0 = Character.toString(data.charAt(i + 1));
                if(c0.matches("\\d")) {
                    int ind = Integer.parseInt(c0);
                    QuerySequence.ParamStatement q;
                    if(ind >= 0 && ind <= this.inputLines.get(currentIOLIne).getLineIndex()) {
                        q = new QuerySequence.ParamStatement(ind);
                        q.setValue(Double.parseDouble(Controller.CONTROLLER.IOLineList.get(q.refIndex).getResult()));
                    }
                    else {
                        q = new QuerySequence.ParamStatement(-1);
                        q.setValue(0d);
                    }
                    QS.addQuery(q);
                    i++;
                }
            }
            if(c.matches("[a-zA-Z]")) {
                StringBuilder _buffer = new StringBuilder();
                for(int j = i; (j < data.length() && Character.toString(data.charAt(j)).matches("[a-zA-Z]")); j++) {
                    _buffer.append(data.charAt(j));
                    i++;
                }
                i--;
                if(_buffer.length() > 0 && funcs.containsKey(_buffer.toString())) {
                    StringBuilder fParam = new StringBuilder();
                    if(data.charAt(i + 1) == FUNC_PARAM_CHARS.charAt(0)) {
                        i+=2;
                        for (int j = i; j < data.length() && data.charAt(j) != FUNC_PARAM_CHARS.charAt(1); j++) {
                            fParam.append(data.charAt(j));
                            i++;
                        }
                    }
                    if(fParam.length() > 0) QS.addQuery(new QuerySequence.Function(_buffer.toString(), new QuerySequence.ConstStatement(Double.parseDouble(fParam.toString()))));
                    else QS.addQuery(new QuerySequence.Function(_buffer.toString()));
                    continue;
                }
                else QS.addQuery(new QuerySequence.VarStatement(c));
                continue;
            }
        }
        if(buffer.length() > 0) {
            QS.addQuery(new QuerySequence.ConstStatement(Double.parseDouble(buffer.toString())));
            buffer.delete(0, buffer.length());
        }
        return QS;
    }
/*    private String funcReader(String data) {
        String fChar = "@", result;
        for(Computer.Functions key : new ArrayList<>(Computer.COMPUTER.funcs.keySet())) {
            String name = Computer.COMPUTER.funcs.get(key);
            if(data.contains(name)) {
                String tData = data.replaceAll(name, fChar);
                for(int i = 0; i < tData.length(); i++) {
                    String c = Character.toString(tData.charAt(i));
                    if (c.matches(fChar))
                        if (Character.toString(tData.charAt(i + 1)).matches("\\(")) {
                            StringBuilder buffer = new StringBuilder();
                            for (int j = i + 2; (j < tData.length() && !Character.toString(tData.charAt(j)).matches("\\)")); j++) {
                                buffer.append(tData.charAt(j));
                            }
                            result = Computer.COMPUTER.computeQuerySequence(readLine(buffer.toString()));
                            data = data.replaceAll(buffer.toString(), result);
                        }
                }
            }
        }
        System.out.println(data);
        return data;
    }*/
    public ArrayList<IOLine> updateParams(ArrayList<IOLine> inputLines_upt, int index, int bias) {
        for(int ind = 0; ind < inputLines_upt.size(); ind++) {
            if (ind > index) {
                IOLine line = inputLines_upt.get(ind);
                String data = line.getTF().getText();
                for (int i = 0; i < data.length(); i++) {
                    String c = Character.toString(data.charAt(i));
                    if (c.matches(PARAM_CHAR)) {
                        String c0 = Character.toString(data.charAt(i + 1));
                        if (c0.matches("\\d")) {
                            int newInd = Integer.parseInt(c0);
                            if(newInd >= index) {
                                newInd += bias;
                                data = data.replaceAll(PARAM_CHAR + c0, "&" + Integer.toString(newInd));
                            }
                        }
                    }
                }
                data = data.replaceAll("&", PARAM_CHAR);
                inputLines_upt.get(ind).getTF().setText(data);
            }
        }
        return inputLines_upt;
    }
    public void readConfigs() {
        System.out.println("reading params for " + this.getClass().toString());
        PARAM_CHAR = XMLReader.READER.getParameterById("100");
        FUNC_PARAM_CHARS = XMLReader.READER.getParameterById("101");
    }
}
