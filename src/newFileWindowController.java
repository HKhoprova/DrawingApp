import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class newFileWindowController {

    private mainWindowController mainController;

    @FXML
    private Button cancelButton;

    @FXML
    private Button createButton;

    public void setMainWindowController(mainWindowController mainController) {
        this.mainController = mainController;
    }
    
    @FXML
    void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void createNewFile(ActionEvent event) {
        mainController.clearCanvas();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

}
