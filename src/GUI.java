import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/mainWindow.fxml"))));
        stage.setTitle("Paint");
        stage.setMinHeight(600);
        stage.setMinWidth(800);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}