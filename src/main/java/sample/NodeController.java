package sample;

import com.sun.javafx.logging.Logger;
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

import java.io.IOException;
import java.util.ArrayList;

public class NodeController {
    @FXML
    private VBox outputPane;
    @FXML
    private VBox centerPane;
    @FXML
    private VBox inputPane;
    @FXML
    private BorderPane nodeInner;

    private ArrayList<Connection> inputConnections;
    private ArrayList<Connection> outputConnections;

    private static NodeController DraggingNode;

    public void initialize() {
        inputConnections = new ArrayList<>();
        outputConnections = new ArrayList<>();
    }

    public ArrayList<Connection> getInputConnections() {
        return inputConnections;
    }

    public ArrayList<Connection> getOutputConnections() {
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
        outputPane.startFullDrag();
        event.consume();
    }

    public void onInputNodeDragReleased(MouseDragEvent mouseDragEvent) {
        System.out.println(mouseDragEvent.getSource());
    }
}
