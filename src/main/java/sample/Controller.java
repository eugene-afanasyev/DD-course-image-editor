package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
    @FXML
    public ImageView mainImage;
    @FXML
    public StackPane workspaceBox;

    public ArrayList<DraggableNodeController> nodes;

    public void handleImageScroll(ScrollEvent scrollEvent) {
        mainImage.setFitHeight(mainImage.getFitHeight() + scrollEvent.getDeltaY() / 2.0);

    }

    public void initialize() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DraggableNode.fxml"));
        workspaceBox.getChildren().add(loader.load());
        DraggableNodeController resultNode = loader.getController();
        Label label = new Label("result node");
        label.setStyle("-fx-font-size: 20; -fx-text-fill: white");
        resultNode.getCenterPane().getChildren().add(label);
    }

    public void handleWorkspaceWrapperScroll(ScrollEvent scrollEvent) {
    }

    public void load(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");
        File file = fileChooser.showOpenDialog(new Stage());
        mainImage.setImage(new Image(file.toURI().toString()));
        mainImage.setPreserveRatio(true);
        mainImage.setFitHeight(mainImage.getImage().getHeight());
        mainImage.setFitWidth(mainImage.getImage().getWidth());
    }

    public void zoom(ZoomEvent zoomEvent) {
    }
}
