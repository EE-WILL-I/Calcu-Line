package project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.scene.control.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.Consumer;

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
    public VBox vBox_inputArray, vBox_parametersArray;
    @FXML
    public Label lbl_info000, lbl_info001, lbl_info002, lbl_param00, lbl_param01, lbl_param02, lbl_param00_val, lbl_param01_val, lbl_param02_val;
    @FXML
    public Button btn_settings, btn_compute, btn_clear, btn_default, btn_revert, btn_apply;
    @FXML
    public AnchorPane ap_main, ap_settings;
    @FXML
    public TextField tf_param00_input, tf_param01_input;
    @FXML
    public ChoiceBox<String> cb_language;

    public ArrayList<IOLine> IOLineList = new ArrayList<IOLine>();
    public String localization = "russian";
    public ObservableList<String> languages = FXCollections.observableArrayList("english", "russian");
    private Consumer<Events.EventArgs> onConfigChangedHandler = (eventArgs -> { localization = XMLReader.READER.getParameterById("000"); setLocalization(localization);});
    private IOLine selectedLine;
    private Reader reader;
    private XMLReader xmlReader;
    private final int maxIOLineCount = 10;

    public void onComputeBtnPressed() {
        if(IOLineList != null)
            Reader.READER.read(IOLineList);
    }

    public void onClearBtnPressed() {
        deleteAllLines();
        addLine(0);
    }
    public void onSettingsBtnPressed() {
        showSettings();
    }
    public void onApplyBtnPressed() {
        saveSettings();
        ap_main.setDisable(false);
        ap_main.setVisible(true);
        ap_settings.setDisable(true);
        ap_settings.setVisible(false);
    }
    public void onRevertBtnPressed() {
        ap_settings.setDisable(true);
        ap_settings.setVisible(false);
        ap_main.setDisable(false);
        ap_main.setVisible(true);
    }
    public void onDefaultBtnPressed() {
        xmlReader.resetConfig();
        xmlReader.invoke();
        ap_settings.setDisable(true);
        ap_settings.setVisible(false);
        ap_main.setDisable(false);
        ap_main.setVisible(true);
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
    public void deleteAllLines() {
        vBox_inputArray.getChildren().removeAll(vBox_inputArray.getChildren());
        IOLineList.removeAll(IOLineList);
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
        cb_language.setItems(languages);
        ap_main.setDisable(false);
        ap_main.setVisible(true);
        ap_settings.setDisable(true);
        ap_settings.setVisible(false);
    }
    public void setInfo(String info) {
        lbl_info001.setText(info);
    }
    private void showSettings() {
        ap_main.setDisable(true);
        ap_main.setVisible(false);
        cb_language.setValue(localization);
        lbl_param00_val.setText(xmlReader.getTextById(localization,"006") + xmlReader.getParameterById("100"));
        lbl_param01_val.setText(xmlReader.getTextById(localization,"006") + xmlReader.getParameterById("101"));
        lbl_param02_val.setText(xmlReader.getTextById(localization,"006") + xmlReader.getParameterById("000"));
        tf_param00_input.clear();
        tf_param01_input.clear();
        tf_param00_input.setPromptText(xmlReader.getTextById(localization, "014"));
        tf_param01_input.setPromptText(xmlReader.getTextById(localization, "014"));
        ap_settings.setDisable(false);
        ap_settings.setVisible(true);
    }
    private void saveSettings() {
        if(tf_param00_input.getText().length() == 1) xmlReader.writeConfig("100", tf_param00_input.getText());
        if(tf_param01_input.getText().length() == 2) xmlReader.writeConfig("101", tf_param01_input.getText());
        if(cb_language.getValue() != null) xmlReader.writeConfig("000", cb_language.getValue());
        xmlReader.invoke();
        ap_settings.setDisable(true);
        ap_settings.setVisible(false);
        ap_main.setDisable(false);
        ap_main.setVisible(true);
    }
    private void setLocalization(String lcl) {
        Node[] textContainingNodes = new Node[]{lbl_info000, lbl_info001, btn_settings, btn_clear, btn_compute,lbl_info002,lbl_param00_val,lbl_param00,lbl_param01,lbl_param02,btn_default,btn_revert,btn_apply};
        for(int i = 0; i < textContainingNodes.length; i++) {
            String id = "";
            for(int k = 3 - String.valueOf(i).length(); k > 0; k--) id += "0";
            id += String.valueOf(i);
            if(textContainingNodes[i].getClass() == Label.class) {
                ((Label) textContainingNodes[i]).setText(xmlReader.getTextById(lcl, id));
                if(id.equals("001")) ((Label) textContainingNodes[i]).setText(xmlReader.getTextById(lcl, id).replace("#", xmlReader.getParameterById("100")));
            }
            else if(textContainingNodes[i].getClass() == Button.class) {
                ((Button) textContainingNodes[i]).setText(xmlReader.getTextById(lcl, id));
            }
        }
        System.out.println("localization is set to '"+ lcl + "'");
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xmlReader = new XMLReader();
        xmlReader.readConfig();
        xmlReader.readTextData();
        xmlReader.subscribe(onConfigChangedHandler);
        reader = new Reader();
        localization = xmlReader.getParameterById("000");
        setLocalization(localization);
    }
}
