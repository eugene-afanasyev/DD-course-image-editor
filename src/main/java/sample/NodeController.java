package sample;

import com.sun.javafx.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class NodeController {
    @FXML
    private VBox outputPane;
    @FXML
    private VBox centerPane;
    @FXML
    private VBox inputPane;
    @FXML
    private BorderPane nodeInner;

    private ObservableList<Connection> inputConnections;
    private ObservableList<Connection> outputConnections;

    private static NodeController DraggingNode;

    public Consumer<Mat> processFunc;

    public void initialize() {
        inputConnections = FXCollections.observableArrayList();
        outputConnections = FXCollections.observableArrayList();
        processFunc = (Mat a) -> {};
    }

    public ObservableList<Connection> getInputConnections() {
        return inputConnections;
    }

    public ObservableList<Connection> getOutputConnections() {
        return outputConnections;
    }

    public void addInputConnection(Connection connection) {
        inputConnections.add(connection);
    }

    public void addOutputConnection(Connection connection) {
        outputConnections.add(connection);
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

    public void setOutputPane(VBox outputPane) {
        this.outputPane = outputPane;
    }

    public void setCenterPane(VBox centerPane) {
        this.centerPane = centerPane;
    }

    public void setInputPane(VBox inputPane) {
        this.inputPane = inputPane;
    }

    public void setNodeInner(BorderPane nodeInner) {
        this.nodeInner = nodeInner;
    }

    public void onNodeMouseDragged(MouseEvent event) {
        Point2D point2D = nodeInner.getParent().localToScene(0,0);
        nodeInner.setTranslateX(event.getSceneX());
        nodeInner.setTranslateY(event.getSceneY() - point2D.getY());

        for (Connection connection : inputConnections) {
            connection.refreshCurvePos();
        }

        for (Connection connection : outputConnections) {
            connection.refreshCurvePos();
        }
    }

    public void onOutputPaneDragDetected(MouseEvent event) {
        DraggingNode = this;
        outputPane.startFullDrag();
        event.consume();
    }

    public void onInputNodeDragReleased(MouseDragEvent mouseDragEvent) {
        Connection connection = new Connection(this, NodeController.DraggingNode);
        inputConnections.add(connection);
        DraggingNode.outputConnections.add(connection);
    }

    public void setProcessFunc(Consumer<Mat> processFunc) {
        this.processFunc = processFunc;
    }

    public void processImage(Mat mat) {
        processFunc.accept(mat);
    }

    public void remove() {
        for (Connection connection : inputConnections) {
            connection.outputNode.outputConnections.remove(connection);
        }

        for (Connection connection : outputConnections) {
            connection.inputNode.inputConnections.remove(connection);
        }
    }
}
