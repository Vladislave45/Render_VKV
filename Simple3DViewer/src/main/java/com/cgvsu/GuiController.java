package com.cgvsu;

import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import com.cgvsu.math.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.objwriter.ObjWriter;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    final private float SCALE = 0.1F;
    final private float ROTATION = 10F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);

            // сброс трансформаций
            mesh.resetTransformations();

            System.out.println("Загружена модель и сброшены преобразования.");
            //
        } catch (IOException exception) {
            // обработка ошибок
        }
    }

    @FXML
    private void onSaveOriginalModelMenuItemClick() {
        if (mesh == null) {
            System.out.println("Модель не загружена для сохранения.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Original Model");

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            // сохранение исходной модели
            String originalModelContent = ObjWriter.write(mesh, false);
            Files.writeString(fileName, originalModelContent);
            System.out.println("Исходная модель сохранена в: " + fileName);
        } catch (IOException exception) {
            // Обработка ошибок
        }
    }

    @FXML
    private void onSaveTransformedModelMenuItemClick() {
        if (mesh == null) {
            System.out.println("Модель не загружена для сохранения.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Transformed Model");

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            // сохранение трансформированной модели
            String transformedModelContent = ObjWriter.write(mesh);
            Files.writeString(fileName, transformedModelContent);
            System.out.println("Трансформированная модель сохранена: " + fileName);
        } catch (IOException exception) {
            // Обработка ошибок
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) { // W
        camera.movePositionAndTarget(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) { // S
        camera.movePositionAndTarget(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) { // A
        camera.movePositionAndTarget(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) { // D
        camera.movePositionAndTarget(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraLeftO(ActionEvent actionEvent) { // <-
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRightO(ActionEvent actionEvent) { // ->
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) { // стрелка вверх
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) { // стрелка вниз
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    public void handleModelScaleX(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f scale = mesh.getScale();
            mesh.setScale(new Vector3f(scale.getX() + SCALE, scale.getY(), scale.getZ()));
        }
    }

    @FXML
    public void handleModelScaleY(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f scale = mesh.getScale();
            mesh.setScale(new Vector3f(scale.getX(), scale.getY() + SCALE, scale.getZ()));
        }
    }

    @FXML
    public void handleModelScaleZ(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f scale = mesh.getScale();
            mesh.setScale(new Vector3f(scale.getX(), scale.getY(), scale.getZ() + SCALE));
        }
    }

    @FXML
    public void handleModelRotateX(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f rotation = mesh.getRotation();
            mesh.setRotation(new Vector3f(rotation.getX() + ROTATION, rotation.getY(), rotation.getZ()));
        }
    }

    @FXML
    public void handleModelRotateY(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f rotation = mesh.getRotation();
            mesh.setRotation(new Vector3f(rotation.getX(), rotation.getY() + ROTATION, rotation.getZ()));
        }
    }

    @FXML
    public void handleModelRotateZ(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f rotation = mesh.getRotation();
            mesh.setRotation(new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ() + ROTATION));
        }
    }

    @FXML
    public void handleModelTranslateX(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f translation = mesh.getTranslation();
            mesh.setTranslation(new Vector3f(translation.getX() + TRANSLATION, translation.getY(), translation.getZ()));
        }
    }

    @FXML
    public void handleModelTranslateY(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f translation = mesh.getTranslation();
            mesh.setTranslation(new Vector3f(translation.getX(), translation.getY() + TRANSLATION, translation.getZ()));
        }
    }

    @FXML
    public void handleModelTranslateZ(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f translation = mesh.getTranslation();
            mesh.setTranslation(new Vector3f(translation.getX(), translation.getY(), translation.getZ() + TRANSLATION));
        }
    }

    @FXML
    public void handleModelScaleXNegative(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f scale = mesh.getScale();
            mesh.setScale(new Vector3f(scale.getX() - SCALE, scale.getY(), scale.getZ()));
        }
    }

    @FXML
    public void handleModelScaleYNegative(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f scale = mesh.getScale();
            mesh.setScale(new Vector3f(scale.getX(), scale.getY() - SCALE, scale.getZ()));
        }
    }

    @FXML
    public void handleModelScaleZNegative(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f scale = mesh.getScale();
            mesh.setScale(new Vector3f(scale.getX(), scale.getY(), scale.getZ() - SCALE));
        }
    }

    @FXML
    public void handleModelRotateXNegative(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f rotation = mesh.getRotation();
            mesh.setRotation(new Vector3f(rotation.getX() - ROTATION, rotation.getY(), rotation.getZ()));
        }
    }

    @FXML
    public void handleModelRotateYNegative(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f rotation = mesh.getRotation();
            mesh.setRotation(new Vector3f(rotation.getX(), rotation.getY() - ROTATION, rotation.getZ()));
        }
    }

    @FXML
    public void handleModelRotateZNegative(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f rotation = mesh.getRotation();
            mesh.setRotation(new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ() - ROTATION));
        }
    }

    @FXML
    public void handleModelTranslateXNegative(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f translation = mesh.getTranslation();
            mesh.setTranslation(new Vector3f(translation.getX() - TRANSLATION, translation.getY(), translation.getZ()));
        }
    }

    @FXML
    public void handleModelTranslateYNegative(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f translation = mesh.getTranslation();
            mesh.setTranslation(new Vector3f(translation.getX(), translation.getY() - TRANSLATION, translation.getZ()));
        }
    }

    @FXML
    public void handleModelTranslateZNegative(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f translation = mesh.getTranslation();
            mesh.setTranslation(new Vector3f(translation.getX(), translation.getY(), translation.getZ() - TRANSLATION));
        }
    }
}