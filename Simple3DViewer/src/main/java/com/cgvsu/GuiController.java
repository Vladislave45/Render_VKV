package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    final private float SCALE = 0.1F;
    final private float ROTATION = 10F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private ListView<String> modelListView; // Список моделей

    private ArrayList<Model> models = new ArrayList<>(); // Список моделей
    private int activeModelIndex = -1; // Индекс активной модели

    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    //////
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isMousePressed = false;
    /////////

    @FXML
    private void initialize() {
        // Привязка размеров холста к размерам панели
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        // Инициализация списка моделей
        modelListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            setActiveModel(newVal.intValue());
        });

        // Инициализация таймлайна для рендеринга
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            // Рендеринг всех моделей
            for (Model model : models) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, model, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        ///// мышь
        canvas.setOnMousePressed(event -> handleMousePressed(event));
        canvas.setOnMouseDragged(event -> handleMouseDragged(event));
        canvas.setOnMouseReleased(event -> handleMouseReleased(event));
        ////////
    }

    /////////мышь
    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
        isMousePressed = true;
    }

    private void handleMouseDragged(MouseEvent event) {
        if (isMousePressed) {
            double deltaX = event.getSceneX() - lastMouseX;
            double deltaY = event.getSceneY() - lastMouseY;

// Обновляем вращение камеры в зависимости от движения мыши
            updateCameraRotation(deltaX, deltaY);

            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        isMousePressed = false;
    }
    private void updateCameraRotation(double deltaX, double deltaY) {
        float sensitivity = 0.1f; // Чувствительность мыши
        float yaw = (float) (-deltaX * sensitivity); // Инвертируем направление вращения по горизонтали
        float pitch = (float) (-deltaY * sensitivity);
        float roll = (float) (deltaY * sensitivity);

// Обновляем углы вращения камеры
        camera.rotateAroundTarget(yaw, pitch, roll);
    }
    ////////////// конец мышь

    // Установка активной модели
    private void setActiveModel(int index) {
        if (index >= 0 && index < models.size()) {
            activeModelIndex = index;
            System.out.println("Активная модель: " + models.get(index).getName());
        } else {
            activeModelIndex = -1;
            System.out.println("Нет активной модели");
        }
    }

    // Добавление модели
    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((anchorPane.getScene().getWindow()));
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model newModel = ObjReader.read(fileContent);
            newModel.setName(file.getName()); // Установка имени модели
            models.add(newModel);
            updateModelList();
            setActiveModel(models.size() - 1); // Установка новой модели как активной
        } catch (IOException exception) {
            System.out.println("Ошибка загрузки модели: " + exception.getMessage());
        }
    }

    // Удаление модели
    @FXML
    private void onRemoveModelButtonClick() {
        if (activeModelIndex != -1) {
            models.remove(activeModelIndex);
            updateModelList();
            setActiveModel(-1); // Сброс активной модели
        } else {
            System.out.println("Нет активной модели для удаления");
        }
    }

    // Обновление списка моделей
    private void updateModelList() {
        modelListView.getItems().clear();
        for (Model model : models) {
            modelListView.getItems().add(model.getName());
        }
    }

    // Сохранение исходной модели
    @FXML
    private void onSaveOriginalModelMenuItemClick() {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            saveModel(activeModel, false);
        } else {
            System.out.println("Нет активной модели для сохранения");
        }
    }

    // Сохранение трансформированной модели
    @FXML
    private void onSaveTransformedModelMenuItemClick() {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            saveModel(activeModel, true);
        } else {
            System.out.println("Нет активной модели для сохранения");
        }
    }

    // Метод для сохранения модели
    private void saveModel(Model model, boolean applyTransformations) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        File file = fileChooser.showSaveDialog((anchorPane.getScene().getWindow()));
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String modelContent = ObjWriter.write(model, applyTransformations);
            Files.writeString(fileName, modelContent);
            System.out.println("Модель сохранена: " + fileName);
        } catch (IOException exception) {
            System.out.println("Ошибка сохранения модели: " + exception.getMessage());
        }
    }

    // Управление камерой
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


    // Трансформации модели
    @FXML
    public void handleModelScaleX(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f scale = activeModel.getScale();
            activeModel.setScale(new Vector3f(scale.getX() + SCALE, scale.getY(), scale.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelScaleXNegative(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f scale = activeModel.getScale();
            activeModel.setScale(new Vector3f(scale.getX() - SCALE, scale.getY(), scale.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelScaleY(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f scale = activeModel.getScale();
            activeModel.setScale(new Vector3f(scale.getX(), scale.getY() + SCALE, scale.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelScaleYNegative(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f scale = activeModel.getScale();
            activeModel.setScale(new Vector3f(scale.getX(), scale.getY() - SCALE, scale.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelScaleZ(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f scale = activeModel.getScale();
            activeModel.setScale(new Vector3f(scale.getX(), scale.getY(), scale.getZ() + SCALE));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelScaleZNegative(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f scale = activeModel.getScale();
            activeModel.setScale(new Vector3f(scale.getX(), scale.getY(), scale.getZ() - SCALE));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelRotateX(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f rotation = activeModel.getRotation();
            activeModel.setRotation(new Vector3f(rotation.getX() + ROTATION, rotation.getY(), rotation.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelRotateXNegative(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f rotation = activeModel.getRotation();
            activeModel.setRotation(new Vector3f(rotation.getX() - ROTATION, rotation.getY(), rotation.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelRotateY(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f rotation = activeModel.getRotation();
            activeModel.setRotation(new Vector3f(rotation.getX(), rotation.getY() + ROTATION, rotation.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelRotateYNegative(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f rotation = activeModel.getRotation();
            activeModel.setRotation(new Vector3f(rotation.getX(), rotation.getY() - ROTATION, rotation.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelRotateZ(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f rotation = activeModel.getRotation();
            activeModel.setRotation(new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ() + ROTATION));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelRotateZNegative(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f rotation = activeModel.getRotation();
            activeModel.setRotation(new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ() - ROTATION));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelTranslateX(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f translation = activeModel.getTranslation();
            activeModel.setTranslation(new Vector3f(translation.getX() + TRANSLATION, translation.getY(), translation.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelTranslateXNegative(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f translation = activeModel.getTranslation();
            activeModel.setTranslation(new Vector3f(translation.getX() - TRANSLATION, translation.getY(), translation.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelTranslateY(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f translation = activeModel.getTranslation();
            activeModel.setTranslation(new Vector3f(translation.getX(), translation.getY() + TRANSLATION, translation.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelTranslateYNegative(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f translation = activeModel.getTranslation();
            activeModel.setTranslation(new Vector3f(translation.getX(), translation.getY() - TRANSLATION, translation.getZ()));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelTranslateZ(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f translation = activeModel.getTranslation();
            activeModel.setTranslation(new Vector3f(translation.getX(), translation.getY(), translation.getZ() + TRANSLATION));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }

    @FXML
    public void handleModelTranslateZNegative(ActionEvent actionEvent) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            Vector3f translation = activeModel.getTranslation();
            activeModel.setTranslation(new Vector3f(translation.getX(), translation.getY(), translation.getZ() - TRANSLATION));
        } else {
            System.out.println("Нет активной модели для трансформации");
        }
    }
}