package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {

        System.out.println("Launching Application");
        launch(args);
    }

    @Override
    public void init() throws Exception {

        System.out.println("Application inits");
        super.init();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("sample.fxml").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Calcu-Line");
        //primaryStage.getIcons().add(new Image("sample/translator_icon.png"));
        primaryStage.setResizable(false);
        Controller controller = loader.getController();
        primaryStage.setOnShown(e -> {
            controller.init();
        });

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {

        System.out.println("Application stops");
        super.stop();
    }
}
