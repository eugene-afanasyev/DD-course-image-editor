package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Controller {
    @FXML
    public VBox layout;
    @FXML
    public ScrollPane imageWrapper;
    @FXML
    public ScrollPane workspaceWrapper;
    @FXML
    public ToolBar toolbar;
    @FXML
    public Button loadButton;

    public void handleImageWrapperScrol(ScrollEvent scrollEvent) {
    }

    public void handleWorkspaceWrapperScrol(ScrollEvent scrollEvent) {
    }

    public void load(MouseEvent event) {
    }
}
