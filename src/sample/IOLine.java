package sample;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

public class IOLine {
    private HBox hBox = new HBox();
    private Label lineNum = new Label(), result = new Label();
    private TextField input = new TextField();
    private String lblStyle = "-fx-border-color:#1e1e1e; -fx-text-fill:#1e1e1e; -fx-font-size:14px; -fx-pref-height:30px;",
            tfStyle = "-fx-background-color: #ffe712; -fx-border-color:#1e1e1e; -fx-font-size:14px; -fx-pref-height:30px;";
    private int tfWidth = 400, lineWidth = 524;
    int index;
    Node[] childrenList = {lineNum, input, result};

    IOLine(int index) {
        this.index = index;

        lineNum.setStyle(lblStyle + "-fx-pref-width:40px;");
        lineNum.setText(Integer.toString(index));
        lineNum.setAlignment(Pos.CENTER);

        input.setStyle(tfStyle + "-fx-pref-width:" + tfWidth + "px;");
        input.setPromptText("Enter operation..");
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

        result.setStyle(lblStyle + "-fx-pref-width:" + (lineWidth - tfWidth) +"px;");
        result.setAlignment(Pos.CENTER);
        result.setText("0.0");

        hBox.setStyle("-fx-background-color:#ffe712;");
        for (Node obj : childrenList) {
            hBox.getChildren().add(obj);
        }
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
}
