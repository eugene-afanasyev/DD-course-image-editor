package sample;

import com.sun.javafx.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import org.opencv.core.Mat;

import java.util.function.Consumer;

public class NodeController {
    @FXML
    private VBox outputPane;
    @FXML
    private VBox centerPane;
    @FXML
    private VBox inputPane;
    @FXML
    private BorderPane nodeInner;

    private ObservableList<NodeController> inputNodes;

    public static NodeController DraggingNode;

    public Consumer<Mat> processFunc;

    public void initialize() {
        inputNodes = FXCollections.observableArrayList();
        processFunc = (Mat a) -> {};
    }

    public VBox getOutputPane() {
        return outputPane;
    }

    public VBox getCenterPane() {
        return centerPane;
    }

    public VBox getInputPane() {
        return inputPane;
    }

    public BorderPane getNodeInner() {
        return nodeInner;
    }

    public ObservableList<NodeController> getInputNodes() {
        return inputNodes;
    }

    public void setInputNodes(ObservableList<NodeController> inputNodes) {
        this.inputNodes = inputNodes;
    }

    public void setCenterPane(VBox centerPane) {
        this.centerPane = centerPane;
    }

    public void onNodeMouseDragged(MouseEvent event) {
        Point2D point2D = nodeInner.getParent().localToScene(0,0);
        nodeInner.setTranslateX(event.getSceneX());
        nodeInner.setTranslateY(event.getSceneY() - point2D.getY());

        for (Connection connection : Controller.connections) {
            connection.refreshCurvePos();
        }
    }

    public void initDraggingCurve(CubicCurve draggingCurve, MouseEvent event) {
        draggingCurve.setFill(null);
        draggingCurve.setStroke(Color.BLACK);
        draggingCurve.setStrokeWidth(2);
        draggingCurve.setStartX(event.getX());
        draggingCurve.setStartY(event.getY());
        draggingCurve.setControlX1(event.getX() + 20);
        draggingCurve.setControlY1(event.getY() + 20);
    }

    public void onLinkPaneDragDetected(MouseEvent event) {
        DraggingNode = this;

        if (event.getSource() == outputPane)
            outputPane.startFullDrag();
        else
            inputPane.startFullDrag();

        event.consume();
    }

    public void onInputNodeDragReleased(MouseDragEvent mouseDragEvent) {
        if (DraggingNode == null)
            return;

        if (mouseDragEvent.getGestureSource() == DraggingNode.getOutputPane() &&
            DraggingNode != this) {
            inputNodes.add(DraggingNode);
            Controller.connections.add(new Connection(this, DraggingNode));
        }
        DraggingNode = null;
    }

    public void onOutputNodeDragReleased(MouseDragEvent mouseDragEvent) {
        if (DraggingNode == null)
            return;

        if (mouseDragEvent.getGestureSource() == DraggingNode.getInputPane() &&
            DraggingNode != this) {
            DraggingNode.inputNodes.add(this);
            Controller.connections.add(new Connection(DraggingNode, this));
        }
        DraggingNode = null;
    }

    public void setProcessFunc(Consumer<Mat> processFunc) {
        this.processFunc = processFunc;
    }

    public void processImage(Mat mat) {
        processFunc.accept(mat);
    }

    public void remove() {
        ObservableList<Connection> matchingCons = FXCollections.observableArrayList();
        for (Connection connection : Controller.connections) {
            if (connection.getInputNode() == this) {
                matchingCons.add(connection);
                inputNodes.remove(connection.getOutputNode());
            } else if (connection.getOutputNode() == this) {
                matchingCons.add(connection);
                connection.getInputNode().getInputNodes().remove(this);
            }
        }

        for (Connection connection : matchingCons) {
            Controller.connections.remove(connection);
        }
    }
}
