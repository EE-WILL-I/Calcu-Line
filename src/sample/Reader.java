package sample;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Reader implements Initializable {
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

        if (READER == null) {
            READER = this;
        } else {
            throw new RuntimeException("Cannot open reader");
        }
    }
    private Computer computer = new Computer();
    private ArrayList<IOLine> inputLines = new ArrayList<IOLine>();
    private final String PARAMCHAR = "%";

    public void read(ArrayList<IOLine> inputLines) {
        this.inputLines = inputLines;
        for(int i = 0; i < inputLines.size(); i++)
            if(!((IOLine)inputLines.toArray()[i]).getTF().getText().isEmpty())
               Controller.CONTROLLER.IOLineList.get(i).setResult(Computer.COMPUTER.computeQuerySequence(readLine((IOLine)this.inputLines.toArray()[i])));
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
                QS.addQuery(new QuerySequence.Operation(signs.get(c)));
                continue;
            }
            if(c.matches("[a-zA-Z]")) {
                StringBuilder _buffer = new StringBuilder();
                for(int j = i; (j < data.length() && Character.toString(data.charAt(j)).matches("[a-zA-Z]")); j++) {
                    _buffer.append(Character.toString(data.charAt(j)));
                    i++;
                }
                i--;
                if(_buffer.length() > 0 && funcs.containsKey(_buffer.toString())) {
                    QS.addQuery(new QuerySequence.Function(_buffer.toString()));
                    continue;
                }
                else QS.addQuery(new QuerySequence.VarStatement(c));
                continue;
            }
            if(c.matches(PARAMCHAR)) {
                String c0 = Character.toString(data.charAt(i + 1));
                if(c0.matches("\\d")) {
                    QuerySequence.ParamStatement q = new QuerySequence.ParamStatement(Integer.parseInt(c0));
                    ((QuerySequence.ParamStatement) q).setValue(Double.parseDouble(Controller.CONTROLLER.IOLineList.get(((QuerySequence.ParamStatement) q).refIndex).getResult()));
                    QS.addQuery(q);
                    i++;
                }
            }
        }
        if(buffer.length() > 0) {
            QS.addQuery(new QuerySequence.ConstStatement(Double.parseDouble(buffer.toString())));
            buffer.delete(0, buffer.length());
        }
        return QS;
    }
    private String funcReader(String data) {
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
    }
    public ArrayList<IOLine> updateParams(ArrayList<IOLine> inputLines_upt, int index, int bias) {
        for(int ind = 0; ind < inputLines_upt.size(); ind++) {
            if (ind > index) {
                IOLine line = inputLines_upt.get(ind);
                String data = line.getTF().getText();
                for (int i = 0; i < data.length(); i++) {
                    String c = Character.toString(data.charAt(i));
                    if (c.matches(PARAMCHAR)) {
                        String c0 = Character.toString(data.charAt(i + 1));
                        if (c0.matches("\\d")) {
                            int newInd = Integer.parseInt(c0);
                            if(newInd >= index) {
                                newInd += bias;
                                data = data.replaceAll(PARAMCHAR + c0, "&" + Integer.toString(newInd));
                            }
                        }
                    }
                }
                data = data.replaceAll("&", PARAMCHAR);
                inputLines_upt.get(ind).getTF().setText(data);
            }
        }
        return inputLines_upt;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
