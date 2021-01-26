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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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

    public void handleImageScroll(ScrollEvent scrollEvent) {
        mainImage.setFitHeight(mainImage.getFitHeight() + scrollEvent.getDeltaY() / 2.0);
    }

    public void initialize() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DraggableNode.fxml"));
        workspaceBox.getChildren().add(loader.load());
        resultNode = loader.getController();
        Label label = new Label("result node");
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 20; -fx-text-fill: white");
        resultNode.getCenterPane().getChildren().add(label);
        resultNode.getCenterPane().setOnMouseDragged(null);
        resultNode.getOutputPane().setMaxWidth(0);

        connections = FXCollections.observableArrayList();
        connections.addListener((ListChangeListener.Change<? extends Connection> change) -> {
            while(change.next()) {
                if (change.wasRemoved()) {
                    processImage();
                    for (Connection connection : change.getRemoved())
                        workspaceBox.getChildren().remove(connection);
                } else if (change.wasAdded()) {
                    processImage();
                }
            }
        });
    }

    public void handleWorkspaceWrapperScroll(ScrollEvent scrollEvent) {}

    public void loadImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");
        File file = fileChooser.showOpenDialog(new Stage());
        if (file.exists()) {
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

//        for (Connection connection : resultNode.getInputConnections()) {
//            if (nodes.get(connection.getOutputNode())) {
//                connection.getOutputNode().processImage(processedImage);
//                traverseNode(connection);
//            }
//        }
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

    public void traverseNode(Connection connection) {
//        nodes.put(connection.getOutputNode(), false);
//        for (Connection con : connection.getOutputNode().getInputConnections()) {
//            if (nodes.get(con.getOutputNode())) {
//                con.outputNode.processImage(processedImage);
//                traverseNode(con);
//            }
//        }
    }

    public void addGrayscaleNode(ActionEvent actionEvent) throws IOException {
        NodeController grayscaleNode = createNodeTemplate("GrayScale");
        grayscaleNode.setProcessFunc((Mat mat) -> {
            Imgproc.cvtColor(originalImage, mat, Imgproc.COLOR_RGB2GRAY);
            System.out.println(1);
        });

        addNodeRemoveButton(grayscaleNode);
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

        return node;
    }

    private void addNodeRemoveButton(NodeController node) {
        Button removeButton = new Button("Remove");
        node.getCenterPane().getChildren().add(removeButton);

        removeButton.setOnMouseClicked((MouseEvent event) -> {
            workspaceBox.getChildren().remove(node.getNodeInner());

//            for (Connection connection : node.getInputConnections()) {
//                workspaceBox.getChildren().remove(connection);
//            }
//            for (Connection connection : node.getOutputConnections()) {
//                workspaceBox.getChildren().remove(connection);
//            }

            node.remove();
            processImage();
        });
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
}
