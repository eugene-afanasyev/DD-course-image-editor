package sample;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;



public class Connection extends CubicCurve {
    NodeController inputNode;
    NodeController outputNode;

    public Connection(NodeController inputNode, NodeController outputNode) {
        this.setStroke(Color.BLACK);
        this.setStrokeWidth(2);
        this.setFill(null);

        this.inputNode = inputNode;
        this.outputNode = outputNode;

        refreshCurvePos();

        this.inputNode.addInputConnection(this);
        this.outputNode.addOutputConnection(this);

        Pane node = (Pane) inputNode.getNodeInner().getParent();
        node.getChildren().add(this);
    }

    public void refreshCurvePos() {
        setStartX(inputNode.getInputPane().localToScene(0,0).getX());
        setStartY(inputNode.getNodeInner().localToParent(0,0).getY() + 40);
        setControlX1(inputNode.getInputPane().localToScene(0,0).getX() + 20);
        setControlY1(inputNode.getNodeInner().localToParent(0,0).getY());
        setControlX2(outputNode.getNodeInner().localToParent(0,0).getX() - 20);
        setControlY2(outputNode.getNodeInner().localToParent(0,0).getY());
        setEndX(outputNode.getNodeInner().localToParent(0,0).getX() + 5);
        setEndY(outputNode.getNodeInner().localToParent(0,0).getY() + 40);
    }

    public NodeController getInputNode() {
        return inputNode;
    }

    public NodeController getOutputNode() {
        return outputNode;
    }

    public void setInputNode(NodeController inputNode) {
        this.inputNode = inputNode;
    }

    public void setOutputNode(NodeController outputNode) {
        this.outputNode = outputNode;
    }
}
