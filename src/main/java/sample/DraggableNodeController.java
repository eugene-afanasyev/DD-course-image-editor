package sample;

import com.sun.javafx.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class DraggableNodeController {
    @FXML
    private BorderPane nodeInner;

    public BorderPane getNodeInner() {
        return nodeInner;
    }

    public void onNodeMouseDragged(MouseEvent event) {
        Point2D point2D = nodeInner.getParent().localToScene(0,0);
        nodeInner.setTranslateX(event.getSceneX());
        nodeInner.setTranslateY(event.getSceneY() - point2D.getY());
    }
}
