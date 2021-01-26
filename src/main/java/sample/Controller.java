package sample;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    public Pane workspaceBox;

    private static NodeController resultNode;

    private Mat originalImage;
    private Mat processedImage;

    public static ObservableList<Connection> connections;

    private final String tmpImagePath = "tmp.jpg";

    public CubicCurve draggingCurve;

    public void handleImageScroll(ScrollEvent scrollEvent) {
        mainImage.setFitHeight(mainImage.getFitHeight() + scrollEvent.getDeltaY() / 2.0);
    }

    public void initialize() throws IOException {
        resultNode = createNodeTemplate("Result Node");
        resultNode.getCenterPane().getChildren().removeIf((Node n) -> (n instanceof Button));
        resultNode.getCenterPane().setOnMouseDragged(null);
        resultNode.getOutputPane().setMaxWidth(0);

        connections = FXCollections.observableArrayList();
        connections.addListener((ListChangeListener.Change<? extends Connection> change) -> {
            while(change.next()) {
                if (change.wasRemoved()) {
                    for (Connection connection : change.getRemoved()) {
                        workspaceBox.getChildren().remove(connection);
                        connection.getInputNode().getInputNodes().remove(connection.getOutputNode());
                    }
                    processImage();
                } else if (change.wasAdded()) {
                    processImage();
                }
            }
        });
    }

    public void loadImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null && file.exists()) {
            mainImage.setImage(new Image(file.toURI().toString()));
            mainImage.setPreserveRatio(true);
            mainImage.setFitHeight(mainImage.getImage().getHeight());
            mainImage.setFitWidth(mainImage.getImage().getWidth());

            originalImage = Imgcodecs.imread(file.getAbsolutePath());
            processedImage = originalImage.clone();
        }
    }

    public void processImage() {
        if (originalImage == null)
            return;
        processedImage = originalImage.clone();

        traverseNode(resultNode);

        try {
            File file = new File(tmpImagePath);

            if (file.exists()) {
                file.delete();
            }
            if (file.createNewFile()) {
                Imgcodecs.imwrite(tmpImagePath, processedImage);
            }

            mainImage.setImage(new Image(file.toURI().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void traverseNode(NodeController node) {
        node.processImage(processedImage);
        for (NodeController nodeController : node.getInputNodes()) {
            traverseNode(nodeController);
        }
    }

    public void addGrayscaleNode(ActionEvent actionEvent) throws IOException {
        NodeController grayscaleNode = createNodeTemplate("GrayScale");
        grayscaleNode.setProcessFunc((Mat mat) -> {
            Imgproc.cvtColor(originalImage, mat, Imgproc.COLOR_RGB2GRAY);
        });
    }

    private NodeController createNodeTemplate(String title) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DraggableNode.fxml"));
        try {
            workspaceBox.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        NodeController node = loader.getController();
        Label label = new Label(title);
        label.setStyle("-fx-font-size: 18; -fx-text-fill: white");
        node.getCenterPane().getChildren().add(label);

        Button removeButton = new Button("Remove");
        node.getCenterPane().getChildren().add(removeButton);

        removeButton.setOnMouseClicked((MouseEvent event) -> {
            workspaceBox.getChildren().remove(node.getNodeInner());
            node.remove();
            processImage();
        });

        return node;
    }

    public void saveImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save image");
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                Image img = mainImage.getImage();
                ImageIO.write(SwingFXUtils.fromFXImage(img,
                        null), "png", file);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void onMouseDragged(MouseEvent event) {
        if (NodeController.DraggingNode == null)
            return;

        if (draggingCurve == null) {
            draggingCurve = new CubicCurve();
            resultNode.initDraggingCurve(draggingCurve, event);
            workspaceBox.getChildren().add(draggingCurve);
        }
        draggingCurve.setControlX2(event.getX() - 20);
        draggingCurve.setControlY2(event.getY() + 20);
        draggingCurve.setEndX(event.getX() - 1);
        draggingCurve.setEndY(event.getY() - 1);
        event.consume();
    }

    public void onWorkspaceMouseDragReleased(MouseDragEvent mouseDragEvent) {
        workspaceBox.getChildren().remove(draggingCurve);
        draggingCurve = null;
        NodeController.DraggingNode = null;
    }

    public void onWorkspaceDragDetected(MouseEvent event) {
        workspaceWrapper.startFullDrag();
    }
}
