package project;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class IOLine {
    private HBox hBox = new HBox();
    private Label lineNum = new Label(), result = new Label();
    private TextField input = new TextField();
//    private String lblStyle = "-fx-border-color:#1e1e1e; -fx-text-fill:#1e1e1e; -fx-font-size:14px; -fx-pref-height:30px;",
//////            tfStyle = "-fx-background-color: #ffe712; -fx-border-color:#1e1e1e; -fx-font-size:14px; -fx-pref-height:30px;";
//////    private int tfWidth = 400, lineWidth = 524;
    private String[] parameters = new String[4];
    private int index;
    private Node[] childrenList = {lineNum, input, result};
    private Consumer<Events.EventArgs> onConfigChangedHandler = (eventArgs -> {readConfigs();});

    IOLine(int index) {
        XMLReader.READER.subscribe(onConfigChangedHandler);
        readConfigs();
        this.index = index;

        lineNum.setText(Integer.toString(index));
        lineNum.setAlignment(Pos.CENTER);

        input.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e) -> {
            switch (e.getCode()) {
                case ENTER: { Controller.CONTROLLER.addLine(this.index + 1); break; }
                case BACK_SPACE: { if(input.getText().isEmpty()) Controller.CONTROLLER.deleteLine(this); break; }
                case DOWN: { Controller.CONTROLLER.setSelected(Controller.CONTROLLER.getSelected().getLineIndex() + 1); break; }
                case UP: { Controller.CONTROLLER.setSelected(Controller.CONTROLLER.getSelected().getLineIndex() - 1); break; }
                default: break;
            }
        });
        input.setOnMouseClicked(event ->  {
            Controller.CONTROLLER.setSelected(this);
        });

        result.setAlignment(Pos.CENTER);
        result.setText("0.0");

        for (Node obj : childrenList) {
            hBox.getChildren().add(obj);
        }
    }
    protected void finalize() {
        XMLReader.READER.unsubscribe(onConfigChangedHandler);
    }
    void setParameters() {
        lineNum.setStyle(parameters[0]);
        input.setStyle(parameters[1]);
        input.setPromptText(XMLReader.READER.getTextById(Controller.CONTROLLER.localization, "005"));
        result.setStyle(parameters[2]);
        hBox.setStyle(parameters[3]);
    }
    void setIndex(int index) {
        this.index = index;
        lineNum.setText(Integer.toString(index));
    }
    void setResult(String out) {
        result.setText(out);
    }
    String getResult() { return result.getText(); }

    HBox getHBox() {
        return hBox;
    }
    int getLineIndex() {
        return index;
    }
    TextField getTF(){
        return input;
    }
    void clear() {
        input.clear();
        result.setText("0");
    }
    public void readConfigs() {
            for(int i = 0; i < parameters.length; i++)
                parameters[i] = XMLReader.READER.getParameterById("20" + i);
            setParameters();
    }
}
