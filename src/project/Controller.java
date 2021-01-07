package project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public static Controller CONTROLLER;

    public Controller() {
        if (CONTROLLER == null) {
            CONTROLLER = this;
        } else {
            throw new RuntimeException("Cannot open controller");
        }
    }

    @FXML
    public VBox vBox_inputArray;
    @FXML
    public Label lbl_param0, lbl_param1;
    public ArrayList<IOLine> IOLineList = new ArrayList<IOLine>();
    private IOLine selectedLine;
    private Reader reader;
    private final int maxIOLineCount = 10;

    public void onComputeBtnPressed() {
        if(IOLineList != null)
            Reader.READER.read(IOLineList);
    }

    public void onClearBtnPressed() {
        if((IOLine)IOLineList.toArray()[0] == selectedLine) selectedLine.clear();
        deleteLine(selectedLine);
    }
    public void onSettingsBtnPressed() {

    }
    public IOLine addLine(int index) {
        if(IOLineList.size() < maxIOLineCount) {
            IOLine result;
            if (index <= IOLineList.size() - 1) {
                ArrayList<IOLine> tmp = new ArrayList<IOLine>();
                vBox_inputArray.getChildren().removeAll(vBox_inputArray.getChildren());
                result = new IOLine(index);

                for (int i = 0; i < IOLineList.size() + 1; i++) {
                    if (i < index) {
                        tmp.add((IOLine) IOLineList.toArray()[i]);
                        HBox hb = ((IOLine) tmp.toArray()[tmp.size() - 1]).getHBox();
                        vBox_inputArray.getChildren().add(hb);
                    } else if (i == index) {
                        tmp.add(result);
                        HBox hb = ((IOLine) tmp.toArray()[tmp.size() - 1]).getHBox();
                        vBox_inputArray.getChildren().add(hb);
                    } else if (i > index) {
                        tmp.add((IOLine) IOLineList.toArray()[i - 1]);
                        ((IOLine) tmp.toArray()[tmp.size() - 1]).setIndex(i);
                        HBox hb = ((IOLine) tmp.toArray()[tmp.size() - 1]).getHBox();
                        vBox_inputArray.getChildren().add(hb);
                    }
                }
                IOLineList = tmp;
            } else {
                result = new IOLine(index);
                IOLineList.add(result);
                HBox hb = ((IOLine) IOLineList.toArray()[IOLineList.size() - 1]).getHBox();
                vBox_inputArray.getChildren().add(hb);
            }
            setSelected(result);
            IOLineList = reader.updateParams(IOLineList, index, 1);
            return result;
        }
        else return null;
    }
    public void deleteLine(int index) {
        if(index > 0) {
            ArrayList<IOLine> tmp = new ArrayList<IOLine>();
            vBox_inputArray.getChildren().removeAll(vBox_inputArray.getChildren());
            for (int i = 0; i < IOLineList.size(); i++) {
                if (i < index) {
                    tmp.add((IOLine) IOLineList.toArray()[i]);
                    HBox hb = ((IOLine) tmp.toArray()[tmp.size() - 1]).getHBox();
                    vBox_inputArray.getChildren().add(hb);
                } else if (i == index) {
                    continue;
                } else if (i > index) {
                    tmp.add((IOLine) IOLineList.toArray()[i]);
                    ((IOLine) tmp.toArray()[tmp.size() - 1]).setIndex(i - 1);
                    HBox hb = ((IOLine) tmp.toArray()[tmp.size() - 1]).getHBox();
                    vBox_inputArray.getChildren().add(hb);
                }
            }
            IOLineList = tmp;
            System.out.println("deleted line: " + selectedLine.getLineIndex());
            setSelected((IOLine)IOLineList.toArray()[selectedLine.getLineIndex() - 1]);
            IOLineList = reader.updateParams(IOLineList, index, -1);
        }
    }
    public void deleteLine(HBox hBox) {
        if(vBox_inputArray.getChildren().contains(hBox)) {
            int i;
            for (i = 0; i < vBox_inputArray.getChildren().size() - 1; i++) {
                if (vBox_inputArray.getChildren().toArray()[i] == hBox) break;
            }
            deleteLine(i);
        }
    }
    public void deleteLine(IOLine line) {
        if(IOLineList.contains(line)) {
            int i;
            for (i = 0; i < IOLineList.size() - 1; i++) {
                if (IOLineList.toArray()[i] == line) break;
            }
            deleteLine(i);
        }
    }
    public void setSelected(IOLine line) {
        if(selectedLine != line) {
            selectedLine = line;
            line.getTF().requestFocus();
            line.getTF().positionCaret(line.getTF().getText().length());
            System.out.println("selected: " + selectedLine.getLineIndex());
        }
    }
    public boolean setSelected(int index) {
        if(index >= 0 && index < IOLineList.size()) {
            setSelected(IOLineList.get(index));
            return true;
        }
        return false;
    }
    public IOLine getSelected() { return selectedLine; }

    public void init() {
        IOLine line = addLine(0);
        if(line != null) setSelected(line);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reader = new Reader();
    }

}
