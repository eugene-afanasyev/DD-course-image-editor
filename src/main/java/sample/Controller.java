package sample;

import javafx.collections.ListChangeListener;
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

import java.io.File;
import java.io.IOException;

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

        resultNode.getInputConnections().addListener(new ListChangeListener<Connection>() {
            @Override
            public void onChanged(Change<? extends Connection> change) {
                while(change.next()) {
                    if (change.wasAdded() || change.wasRemoved())
                        processImage();
                }
            }
        });
    }

    public void handleWorkspaceWrapperScroll(ScrollEvent scrollEvent) {}

    public void load(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");
        File file = fileChooser.showOpenDialog(new Stage());
        mainImage.setImage(new Image(file.toURI().toString()));
        mainImage.setPreserveRatio(true);
        mainImage.setFitHeight(mainImage.getImage().getHeight());
        mainImage.setFitWidth(mainImage.getImage().getWidth());

        originalImage = Imgcodecs.imread(file.getAbsolutePath());
    }

    public void processImage() {
        if (originalImage == null)
            return;

        processedImage = originalImage.clone();
        for (Connection connection : resultNode.getInputConnections()) {
            connection.outputNode.processImage(processedImage);
            traverseNode(connection);
        }
        try {
            File file = new File("tmp.jpg");

            if (file.exists()) {
                file.delete();
            }
            if (file.createNewFile()) {
                Imgcodecs.imwrite("tmp.jpg", processedImage);
            }

            mainImage.setImage(new Image(file.toURI().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void traverseNode(Connection connection) {
        for (Connection con : connection.getOutputNode().getInputConnections()) {
            con.outputNode.processImage(processedImage);
            traverseNode(con);
        }
    }

    public void addGrayscaleNode(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DraggableNode.fxml"));
        workspaceBox.getChildren().add(loader.load());
        NodeController grayscaleNode = loader.getController();
        Label label = new Label("Grayscale");
        label.setStyle("-fx-font-size: 18; -fx-text-fill: white");
        grayscaleNode.getCenterPane().getChildren().add(label);
        grayscaleNode.setProcessFunc((Mat mat) -> {
            Imgproc.cvtColor(originalImage, mat, Imgproc.COLOR_RGB2GRAY);
            System.out.println(1);
        });

        addNodeRemoveButton(grayscaleNode);
    }

    private void addNodeRemoveButton(NodeController node) {
        Button removeButton = new Button("Remove");
        node.getCenterPane().getChildren().add(removeButton);

        removeButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                workspaceBox.getChildren().remove(node.getNodeInner());

                for (Connection connection : node.getInputConnections()) {
                    workspaceBox.getChildren().remove(connection);
                }
                for (Connection connection : node.getOutputConnections()) {
                    workspaceBox.getChildren().remove(connection);
                }

                node.remove();
                processImage();
            }
        });
    }
}
