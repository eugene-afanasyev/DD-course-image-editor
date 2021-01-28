package sample;

import com.google.gson.*;
import com.sun.javafx.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.stage.FileChooser;
import org.opencv.core.Mat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Consumer;

enum NodeType {
    RESULT,
    GRAYSCALE,
    SEPIA,
    BLUR
}

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

    public NodeType type;

    public void initialize() {
        inputNodes = FXCollections.observableArrayList();
        processFunc = (Mat a) -> {};
        type = NodeType.RESULT;
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
            if (!inputNodes.contains(DraggingNode) && !DraggingNode.getInputNodes().contains(this)) {
                inputNodes.add(DraggingNode);
                Controller.connections.add(new Connection(this, DraggingNode));
            }
        }

        DraggingNode = null;
    }

    public void onOutputNodeDragReleased(MouseDragEvent mouseDragEvent) {
        if (DraggingNode == null)
            return;

        if (mouseDragEvent.getGestureSource() == DraggingNode.getInputPane() &&
            DraggingNode != this) {
            if (!DraggingNode.inputNodes.contains(this) && !inputNodes.contains(DraggingNode)) {
                DraggingNode.inputNodes.add(this);
                Controller.connections.add(new Connection(DraggingNode, this));
            }
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

class NodeSerializer implements JsonSerializer<NodeController> {
    @Override
    public JsonElement serialize(NodeController src, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("type", String.valueOf(src.type));
        result.addProperty("x", src.getNodeInner().getTranslateX());
        result.addProperty("y", src.getNodeInner().getTranslateY());

        if (src.type == NodeType.BLUR) {
            for (Node node : src.getCenterPane().getChildren()) {
                if (node instanceof Slider) {
                    var tmp = (Slider)node;
                    result.addProperty("blurSize", tmp.getValue());
                }
            }
        }

        JsonArray inputNodes = new JsonArray();
        for (NodeController inputNode : src.getInputNodes()) {
            inputNodes.add(context.serialize(inputNode));
        }
        result.add("inputNodes", inputNodes);

        return result;
    }
}

class NodeDeserializer implements JsonDeserializer<NodeController> {
    @Override
    public NodeController deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DraggableNode.fxml"));
        NodeController node = loader.getController();

        JsonObject jsonObject = new JsonObject();
        NodeType nodeType = NodeType.valueOf(jsonObject.get("type").getAsString());
        node.type = nodeType;
        node.getNodeInner().setTranslateX(jsonObject.get("x").getAsInt());
        node.getNodeInner().setTranslateY(jsonObject.get("y").getAsInt());

        ObservableList<NodeController> inputNodes = FXCollections.observableArrayList();
        JsonArray inputNodesJson = new JsonArray();
        inputNodesJson = jsonObject.getAsJsonArray("inputNodes");
        for (JsonElement inputNodeJson : inputNodesJson) {
            node.getInputNodes().add(context.deserialize(inputNodeJson, NodeController.class));
        }

        return node;
    }
}
