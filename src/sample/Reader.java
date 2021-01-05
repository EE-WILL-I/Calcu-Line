package sample;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Reader implements Initializable {
    public static Reader READER;

    public Map<String, Computer.Func> signs = new HashMap<String, Computer.Func>();

    public Reader() {
        signs.put("+", Computer.Func.sum);
        signs.put("-", Computer.Func.sub);
        signs.put("*", Computer.Func.mult);
        signs.put("/", Computer.Func.div);
        signs.put("(", Computer.Func.opInc);
        signs.put(")", Computer.Func.opDec);

        if (READER == null) {
            READER = this;
        } else {
            throw new RuntimeException("Cannot open reader");
        }
    }
    private Computer computer = new Computer();
    private ArrayList<IOLine> inputLines = new ArrayList<IOLine>();

    public void read(ArrayList<IOLine> inputLines) {
        this.inputLines = inputLines;
        for(int i = 0; i < inputLines.size(); i++)
            if(!((IOLine)inputLines.toArray()[i]).getTF().getText().isEmpty())
               Controller.CONTROLLER.IOLineList.get(i).setResult(Computer.COMPUTER.readQuerySequence(readLine((IOLine)this.inputLines.toArray()[i])));
    }
    private QuerySequence readLine(IOLine line) {
        QuerySequence QS = new QuerySequence();
        String data = line.getTF().getText();
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
                if(QS.addQuery(new QuerySequence.VarStatement(c))) {}
                else QS.addQuery((new QuerySequence.VarStatement("_")));
                continue;
            }
            if(c.matches("(\\$)")) {
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
    public ArrayList<IOLine> updateParams(ArrayList<IOLine> inputLines_upt, int index, int bias) {
        for(int ind = 0; ind < inputLines_upt.size(); ind++) {
            if (ind > index) {
                IOLine line = inputLines_upt.get(ind);
                String data = line.getTF().getText();
                for (int i = 0; i < data.length(); i++) {
                    String c = Character.toString(data.charAt(i));
                    if (c.matches("(\\$)")) {
                        String c0 = Character.toString(data.charAt(i + 1));
                        if (c0.matches("\\d")) {
                            int newInd = Integer.parseInt(c0);
                            if(newInd >= index) {
                                newInd += bias;
                                String s = "\\$";
                                data = data.replaceAll(s + c0, s + Integer.toString(newInd));
                            }
                        }
                    }
                }
                inputLines_upt.get(ind).getTF().setText(data);
                System.out.println("invoked line " + ind);
            }
        }
        return inputLines_upt;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
