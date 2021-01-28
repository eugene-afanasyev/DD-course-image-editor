package sample;

import com.google.gson.*;
import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.CubicCurve;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.features2d.AgastFeatureDetector;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Subdiv2D;

import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


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
            Imgproc.cvtColor(processedImage, mat, Imgproc.COLOR_RGB2GRAY);
            Imgproc.cvtColor(processedImage, mat, Imgproc.COLOR_GRAY2RGB);
        });
        grayscaleNode.type = NodeType.GRAYSCALE;
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

    public void addSepiaNode(ActionEvent actionEvent) {
        NodeController sepiaNode = createNodeTemplate("Sepia");
        sepiaNode.setProcessFunc((Mat src) -> {
            Mat kernel = new Mat(3, 3, CvType.CV_32F);
            double[] tmp = {0.272, 0.534, 0.131, 0.349, 0.686, 0.168, 0.393, 0.769, 0.189};
            kernel.put(0,0, tmp);
            try {
                Core.transform(processedImage, src, kernel);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("OpenCV assertion filed");
                alert.setHeaderText("Advice:");
                alert.setContentText("Using grayscale filter may cause this error");
                alert.showAndWait();
            }
        });
        sepiaNode.type = NodeType.SEPIA;
    }

    public void addBlurringNode(ActionEvent actionEvent) {
        NodeController blurringNode = createNodeTemplate("Blur");

        Slider blurSizeSlider = new Slider(1, 30, 1);
        blurSizeSlider.setShowTickMarks(true);
        blurSizeSlider.setShowTickLabels(true);
        blurSizeSlider.setSnapToTicks(true);
        blurSizeSlider.setStyle("-fx-text-fill: white");
        blurSizeSlider.valueProperty().addListener(((observableValue, number, t1) -> {
            processImage();
        }));

        Label l = new Label("Blur size");
        l.setStyle("-fx-text-fill: white; -fx-font-size: 18");

        blurringNode.getCenterPane().getChildren().addAll(l, blurSizeSlider);

        blurringNode.setProcessFunc((Mat src) -> {
            int sizeValue = (int) blurSizeSlider.getValue();
            sizeValue = sizeValue % 2 == 1 ? sizeValue : sizeValue + 1;
            Imgproc.GaussianBlur(processedImage, src, new Size(sizeValue, sizeValue), 0);
        });
        blurringNode.type = NodeType.BLUR;
    }


    public void saveProject() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save project");
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(NodeController.class, new NodeSerializer())
                    .create();
            String serializedData = "";

            JsonObject result = new JsonObject();
            result.addProperty("imagePath", mainImage.getImage().getUrl());
            result.add("nodes", JsonParser.parseString(gson.toJson(resultNode)).getAsJsonObject());
            serializedData = gson.toJson(result);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(serializedData);

            writer.close();
        }
    }

    public void addTriangularNode(ActionEvent actionEvent) {
        NodeController node = createNodeTemplate("Triangular");
        Slider smoothSlider = new Slider(1, 25, 1);
        smoothSlider.setShowTickLabels(true);
        smoothSlider.setShowTickMarks(true);

        smoothSlider.valueProperty().addListener((c) -> {
            processImage();
        });

        ObservableList<Integer> methods = FXCollections.observableArrayList(0, 1, 2, 10000, 10001, 10002);
        ComboBox<Integer> fDetectorBox = new ComboBox<>(methods);
        fDetectorBox.valueProperty().addListener((c) -> {
            processImage();
        });

        node.getCenterPane().getChildren().addAll(smoothSlider, fDetectorBox);

        node.setProcessFunc((Mat src) -> {
            if (fDetectorBox.getValue() == null)
                return;

            Mat gray = new Mat(src.rows(), src.cols(), src.type());
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
            gray.convertTo(gray, CvType.CV_8UC1);

            int sliderValue = (int) smoothSlider.getValue();
            sliderValue = sliderValue % 2 == 1 ? sliderValue : sliderValue + 1;
            Imgproc.GaussianBlur(gray, gray, new Size(sliderValue, sliderValue), 0);

            MatOfKeyPoint matOfKeyPoint = new MatOfKeyPoint();
            FastFeatureDetector fd = FastFeatureDetector.create(fDetectorBox.getValue());
            fd.detect(gray, matOfKeyPoint);

            for (KeyPoint kp : matOfKeyPoint.toList()) {
                int min = -10, max = 10;
                int yRand = (int) (Math.random() * (max - min) + min);
                int xRand = (int) (Math.random() * (max - min) + min);

                if (kp.pt.x + xRand < gray.width() - 1 && kp.pt.x + xRand > 0)
                    kp.pt.x += xRand;
                if (kp.pt.y + yRand < gray.height() - 1 && kp.pt.y + yRand > 0)
                    kp.pt.y += yRand;
            }

            Subdiv2D subdiv2D = new Subdiv2D(new Rect(0, 0, gray.width(), gray.height()));
            for (KeyPoint kp : matOfKeyPoint.toArray()) {
                subdiv2D.insert(kp.pt);
            }
            subdiv2D.insert(new Point(0, 0));
            subdiv2D.insert(new Point(0, gray.height() - 1));
            subdiv2D.insert(new Point(gray.width() - 1, 0));
            subdiv2D.insert(new Point(gray.width() - 1, gray.height() - 1));


            MatOfFloat6 trianglesMat = new MatOfFloat6();
            subdiv2D.getTriangleList(trianglesMat);

            float[] t = trianglesMat.toArray();
            ArrayList<Triangle> triangles = new ArrayList<>();
            for (int i = 0; i < t.length; i += 6) {
                Point pt1 = new Point(t[i], t[i + 1]);
                Point pt2 = new Point(t[i + 2], t[i + 3]);
                Point pt3 = new Point(t[i + 4], t[i + 5]);
                triangles.add(new Triangle(pt1, pt2, pt3));
            }

            for (Triangle triangle : triangles) {
                for (MatOfPoint matPt : triangle.getPoints()) {
                    Point pt1 = matPt.toArray()[0];
                    Point pt2 = matPt.toArray()[1];
                    Point pt3 = matPt.toArray()[2];

                    Scalar s1 = new Scalar(processedImage.get((int) pt1.y, (int) pt1.x));
                    Scalar s2 = new Scalar(processedImage.get((int) pt2.y, (int) pt2.x));
                    Scalar s3 = new Scalar(processedImage.get((int) pt3.y, (int) pt3.x));

                    double avg1 = (s1.val[0] + s2.val[0] + s3.val[0]) / 3.0;
                    double avg2 = (s1.val[1] + s2.val[1] + s3.val[1]) / 3.0;
                    double avg3 = (s1.val[2] + s2.val[2] + s3.val[2]) / 3.0;

                    triangle.setColor(new Scalar(avg1, avg2, avg3));
                }
                Imgproc.fillPoly(processedImage, triangle.getPoints(), triangle.getColor());
            }
//            Features2d.drawKeypoints(processedImage, matOfKeyPoint, processedImage, new Scalar(255, 255, 255));
        });

    }
}

class Triangle {
    private Point pt1, pt2, pt3;
    private Scalar color;

    public Triangle(Point pt1, Point pt2, Point pt3) {
        this.pt1 = pt1;
        this.pt2 = pt2;
        this.pt3 = pt3;
        color = new Scalar(255,255,255);
    }

    public ArrayList<MatOfPoint> getPoints() {
        MatOfPoint mat = new MatOfPoint();
        mat.fromArray(pt1, pt2, pt3);
        ArrayList<MatOfPoint> points = new ArrayList<>();
        points.add(mat);
        return points;
    }

    public void setColor(Scalar color) {
        this.color = color;
    }

    public Scalar getColor() {
        return color;
    }
}