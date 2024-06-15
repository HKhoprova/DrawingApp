import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class mainWindowController {

    @FXML
    private Button brushButton;

    @FXML
    private Button bucketButton;

    @FXML
    private Canvas canvas;

    @FXML
    private Button circleButton;

    @FXML
    private Button circleFilledButton;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Button createButton;

    @FXML
    private Rectangle currentColorRect;

    @FXML
    private Button eraserButton;

    @FXML
    private Button eyedropperButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button lineButton;

    @FXML
    private Button openButton;

    @FXML
    public static Button pencilButton;

    @FXML
    private Button redoButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button squareButton;

    @FXML
    private Button squareFilledButton;

    @FXML
    private Circle toolSizeCircle;

    @FXML
    private Spinner<Integer> toolSizeSpinner;

    @FXML
    private Button undoButton;

    private GraphicsContext g;

    private String currentTool = "pensil";
    private List<WritableImage> drawings = new ArrayList<>();
    private int currentDrawing = 0;

    private double startX;
    private double startY;
    private double endX;
    private double endY;

    Line currentLine;
    Rectangle currentRect;
    Circle currentCircle;

    @FXML
    public void initialize() {
        g = canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);
        
        toolSizeCircle.radiusProperty().bind(toolSizeSpinner.valueProperty());
        colorPicker.setValue(Color.BLACK);
        currentColorRect.fillProperty().bind(colorPicker.valueProperty());
        clearCanvas();

        canvas.setOnMouseClicked(e -> {
            double size = Double.valueOf(toolSizeSpinner.getValue())*2;
            double x = e.getX() - size/2;
            double y = e.getY() - size/2;

            if(currentTool.equals("bucket")) {
                    x = e.getX();
                    y = e.getY();
                    WritableImage image = canvasToWritableImage(canvas);
                    PixelReader pixelReader = image.getPixelReader();
                    Color targetColor = pixelReader.getColor((int) x, (int) y);
                    Color replacementColor = colorPicker.getValue();
                    floodFill(image, (int) x, (int) y, targetColor, replacementColor);
                    g.drawImage(image, 0, 0);
                    WritableImage currentImage = canvasToWritableImage(canvas);
                    drawings.add(currentImage);
                    currentDrawing += 1;
            } else if(currentTool.equals("eyedropper")) {
                    Robot robot = new Robot();
                    Point mousePos = MouseInfo.getPointerInfo().getLocation();
                    Color color = robot.getPixelColor(mousePos.getX(), mousePos.getY());
                    colorPicker.setValue(color);
            }
        });

        canvas.setOnMousePressed(e -> {
            if(currentTool.matches("line|square|squareFilled|circle|circleFilled")) {
                startX = e.getX();
                startY = e.getY();
            }
        });

        canvas.setOnMouseDragged(e -> {
            double size = Double.valueOf(toolSizeSpinner.getValue())*2;
            double x = e.getX() - size/2;
            double y = e.getY() - size/2;

            switch (currentTool) {
                case "pensil":
                    g.setFill(colorPicker.getValue());
                    g.fillRect(x, y, size, size);
                    break;
                case "brush":
                    g.setFill(colorPicker.getValue());
                    g.fillOval(x, y, size, size);
                    break;
                case "eraser":
                    g.setFill(Color.WHITE);
                    g.fillRect(x, y, size, size);
                    break;
                case "eyedropper":
                    Robot robot = new Robot();
                    Point mousePos = MouseInfo.getPointerInfo().getLocation();
                    Color color = robot.getPixelColor(mousePos.getX(), mousePos.getY());
                    colorPicker.setValue(color);
                    break;
                case "line":
                    endX = e.getX();
                    endY = e.getY();
                    g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    g.drawImage(drawings.get(currentDrawing), 0, 0);
                    g.setStroke(colorPicker.getValue());
                    g.setLineWidth(toolSizeSpinner.getValue()*1.5);
                    g.strokeLine(startX, startY, endX, endY);
                    break;
                case "square":
                    endX = e.getX();
                    endY = e.getY();
                    g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    g.drawImage(drawings.get(currentDrawing), 0, 0);
                    g.setStroke(colorPicker.getValue());
                    g.setLineWidth(toolSizeSpinner.getValue()*1.5);
                    if(startX <= endX && startY <= endY) {
                        g.strokeRect(startX, startY, endX - startX, endY - startY);
                    } else if(startX >= endX && startY <= endY) {
                        g.strokeRect(endX, startY, startX - endX, endY - startY);
                    } else if(startX <= endX && startY >= endY) {
                        g.strokeRect(startX, endY, endX - startX, startY - endY);
                    } else {
                        g.strokeRect(endX, endY, startX - endX, startY - endY);
                    }
                    break;
                case "squareFilled":
                    endX = e.getX();
                    endY = e.getY();
                    g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    g.drawImage(drawings.get(currentDrawing), 0, 0);
                    g.setFill(colorPicker.getValue());
                    if(startX <= endX && startY <= endY) {
                        g.fillRect(startX, startY, endX - startX, endY - startY);
                    } else if(startX >= endX && startY <= endY) {
                        g.fillRect(endX, startY, startX - endX, endY - startY);
                    } else if(startX <= endX && startY >= endY) {
                        g.fillRect(startX, endY, endX - startX, startY - endY);
                    } else {
                        g.fillRect(endX, endY, startX - endX, startY - endY);
                    }
                    break;
                case "circle":
                    endX = e.getX();
                    endY = e.getY();
                    g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    g.drawImage(drawings.get(currentDrawing), 0, 0);
                    g.setStroke(colorPicker.getValue());
                    g.setLineWidth(toolSizeSpinner.getValue()*1.5);
                    if(startX <= endX && startY <= endY) {
                        g.strokeOval(startX, startY, endX - startX, endY - startY);
                    } else if(startX >= endX && startY <= endY) {
                        g.strokeOval(endX, startY, startX - endX, endY - startY);
                    } else if(startX <= endX && startY >= endY) {
                        g.strokeOval(startX, endY, endX - startX, startY - endY);
                    } else {
                        g.strokeOval(endX, endY, startX - endX, startY - endY);
                    }
                    break;
                case "circleFilled":
                    endX = e.getX();
                    endY = e.getY();
                    g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    g.drawImage(drawings.get(currentDrawing), 0, 0);
                    g.setFill(colorPicker.getValue());
                    if(startX <= endX && startY <= endY) {
                        g.fillOval(startX, startY, endX - startX, endY - startY);
                    } else if(startX >= endX && startY <= endY) {
                        g.fillOval(endX, startY, startX - endX, endY - startY);
                    } else if(startX <= endX && startY >= endY) {
                        g.fillOval(startX, endY, endX - startX, startY - endY);
                    } else {
                        g.fillOval(endX, endY, startX - endX, startY - endY);
                    }
                    break;
                default:
                    break;
            }
        });

        canvas.setOnMouseReleased(e -> {
            if(currentTool.matches("pensil|brush|eraser|line|square|squareFilled|circle|circleFilled")) {
                while(currentDrawing != drawings.size()-1) {
                    drawings.remove(drawings.size()-1);
                }
                
                WritableImage currentImage = canvasToWritableImage(canvas);
                drawings.add(currentImage);

                switch (currentTool) {
                    case "line":
                        endX = e.getX();
                        endY = e.getY();
                        g.strokeLine(startX, startY, endX, endY);
                        break;
                    case "square":
                        endX = e.getX();
                        endY = e.getY();
                        if(startX <= endX && startY <= endY) {
                            g.strokeRect(startX, startY, endX - startX, endY - startY);
                        } else if(startX >= endX && startY <= endY) {
                            g.strokeRect(endX, startY, startX - endX, endY - startY);
                        } else if(startX <= endX && startY >= endY) {
                            g.strokeRect(startX, endY, endX - startX, startY - endY);
                        } else {
                            g.strokeRect(endX, endY, startX - endX, startY - endY);
                        }
                        break;
                    case "squareFilled":
                        endX = e.getX();
                        endY = e.getY();
                        if(startX <= endX && startY <= endY) {
                            g.fillRect(startX, startY, endX - startX, endY - startY);
                        } else if(startX >= endX && startY <= endY) {
                            g.fillRect(endX, startY, startX - endX, endY - startY);
                        } else if(startX <= endX && startY >= endY) {
                            g.fillRect(startX, endY, endX - startX, startY - endY);
                        } else {
                            g.fillRect(endX, endY, startX - endX, startY - endY);
                        }
                        break;
                    case "circle":
                        endX = e.getX();
                        endY = e.getY();
                        if(startX <= endX && startY <= endY) {
                            g.strokeOval(startX, startY, endX - startX, endY - startY);
                        } else if(startX >= endX && startY <= endY) {
                            g.strokeOval(endX, startY, startX - endX, endY - startY);
                        } else if(startX <= endX && startY >= endY) {
                            g.strokeOval(startX, endY, endX - startX, startY - endY);
                        } else {
                            g.strokeOval(endX, endY, startX - endX, startY - endY);
                        }
                        break;
                    case "circleFilled":
                        endX = e.getX();
                        endY = e.getY();
                        if(startX <= endX && startY <= endY) {
                            g.fillOval(startX, startY, endX - startX, endY - startY);
                        } else if(startX >= endX && startY <= endY) {
                            g.fillOval(endX, startY, startX - endX, endY - startY);
                        } else if(startX <= endX && startY >= endY) {
                            g.fillOval(startX, endY, endX - startX, startY - endY);
                        } else {
                            g.fillOval(endX, endY, startX - endX, startY - endY);
                        }
                        break;
                    default:
                        break;
                }

                if(drawings.size() > 10)
                    drawings.remove(0);
                
                currentDrawing = drawings.size()-1;
            }
        });
    }

    private WritableImage canvasToWritableImage(Canvas canvas) {
        SnapshotParameters params = new SnapshotParameters();
        double s = 600.0 / canvas.getBoundsInLocal().getWidth();
        params.setTransform(new Scale(s, s));
        return canvas.snapshot(params, null);
    }

    public void clearCanvas() {
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawings.clear();
        drawings.add(canvasToWritableImage(canvas));
        currentDrawing = 0;
    }

    private void floodFill(WritableImage image, int x, int y, Color targetColor, Color replacementColor) {
        if (targetColor.equals(replacementColor)) {
            return;
        }

        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = image.getPixelWriter();

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));

        while (!queue.isEmpty()) {
            Point p = queue.remove();
            int px = p.x;
            int py = p.y;

            if (px < 0 || px >= image.getWidth() || py < 0 || py >= image.getHeight()) {
                continue;
            }

            if (pixelReader.getColor(px, py).equals(targetColor)) {
                pixelWriter.setColor(px, py, replacementColor);

                queue.add(new Point(px + 1, py));
                queue.add(new Point(px - 1, py));
                queue.add(new Point(px, py + 1));
                queue.add(new Point(px, py - 1));
            }
        }
    }

    @FXML
    void brushTool(ActionEvent event) {
        currentTool = "brush";
    }

    @FXML
    void createFile(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/newFileWindow.fxml"));
            Parent root = fxmlLoader.load();

            newFileWindowController newFileController = fxmlLoader.getController();
            newFileController.setMainWindowController(this);

            Stage newStage = new Stage();
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.setTitle("Creating new file!");
            newStage.setScene(new Scene(root));
            newStage.setResizable(false); 
            newStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void drawCircleFilledTool(ActionEvent event) {
        currentTool = "circleFilled";
    }

    @FXML
    void drawCircleTool(ActionEvent event) {
        currentTool = "circle";
    }

    @FXML
    void drawLineTool(ActionEvent event) {
        currentTool = "line";
    }

    @FXML
    void drawSquareFilledTool(ActionEvent event) {
        currentTool = "squareFilled";
    }

    @FXML
    void drawSquareTool(ActionEvent event) {
        currentTool = "square";
    }

    @FXML
    void eraserTool(ActionEvent event) {
        currentTool = "eraser";
    }

    @FXML
    void fillAreaTool(ActionEvent event) {
        currentTool = "bucket";
    }

    @FXML
    void help(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/messageWindow.fxml"));
            Parent root = fxmlLoader.load();

            Stage newStage = new Stage();
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.setTitle("About the program");
            newStage.setScene(new Scene(root));
            newStage.setResizable(false);
            newStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");

        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.bmp");
        fileChooser.getExtensionFilters().addAll(extFilterPNG, extFilterJPG, extFilterBMP);

        Stage newStage = new Stage();
        File file = fileChooser.showOpenDialog(newStage);

        if (file != null) {
            
                Image img = new Image(file.toURI().toString());
                g.setFill(Color.WHITE);
                g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                g.drawImage(img, 0, 0);
            
        }
    }

    @FXML
    void pencilTool(ActionEvent event) {
        currentTool = "pensil";
    }

    @FXML
    void pickColorTool(ActionEvent event) {
        currentTool = "eyedropper";
    }

    @FXML
    void redo(ActionEvent event) {
        if(currentDrawing < drawings.size()-1) {
            currentDrawing += 1;
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(drawings.get(currentDrawing), 0, 0);
        }
    }

    @FXML
    void saveFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");

        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.bmp");
        fileChooser.getExtensionFilters().addAll(extFilterPNG, extFilterJPG, extFilterBMP);

        Stage newStage = new Stage();
        File file = fileChooser.showSaveDialog(newStage);

        if (file != null) {
            try {
                Image snapshot = canvas.snapshot(null, null);
                BufferedImage awtImage = new BufferedImage((int)snapshot.getWidth(), (int)snapshot.getHeight(), BufferedImage.TYPE_INT_RGB);
                String extension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0).substring(2);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, awtImage), extension, file);
            } catch (IOException ex) {
                System.out.println("Error saving image: " + ex.getMessage());
            }
        }
    }

    @FXML
    void undo(ActionEvent event) {
        if(currentDrawing > 0) {
            currentDrawing -= 1;
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(drawings.get(currentDrawing), 0, 0);
        }
    }

}
