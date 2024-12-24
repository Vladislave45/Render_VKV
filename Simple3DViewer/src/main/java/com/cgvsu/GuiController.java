package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.CameraManager;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.utils.Triangulation;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.vecmath.Point2f;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GuiController {

    private float TRANSLATION = 0.5F;
    private float SCALE = 0.1F;
    private float ROTATION = 10F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private boolean isTriangulationEnabled = false; // Флаг для триангуляции
    private boolean isRasterizationEnabled = false; // Флаг для растеризации

    @FXML
    private ListView<String> modelListView; // Список моделей

    @FXML
    private Button removeVerticesButton; // Кнопка для удаления вершин

    private ArrayList<Model> models = new ArrayList<>(); // Список моделей
    private int activeModelIndex = -1; // Индекс активной модели

    @FXML
    private TextField translationStepField;
    @FXML
    private TextField scaleStepField;
    @FXML
    private TextField rotationStepField;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    // Переменные для выделения вершин
    private List<Integer> selectedVertices = new ArrayList<>();

    //////
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isMousePressed = false;
    /////////

    @FXML
    private void render() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

        // Получаем активную камеру из CameraManager
        Camera activeCamera = cameraManager.getActiveCamera();
        if (activeCamera != null) {
            activeCamera.setAspectRatio((float) (width / height));

            // Передача цветов и параметров трансформации
            Color modelColor = modelColorPicker.getValue();
            Color backgroundColor = backgroundColorPicker.getValue();

            // Рендеринг всех моделей
            for (Model model : models) {
                RenderEngine.render(
                        canvas.getGraphicsContext2D(),
                        activeCamera, // Используем активную камеру
                        model,
                        (int) width,
                        (int) height,
                        selectedVertices,
                        modelColor,
                        backgroundColor,
                        isRasterizationEnabled, // Передаем состояние растеризации
                        useTextureCheckBox.isSelected() ? texture : null, // Передаем текстура, если она включена
                        texture == null ? Color.LIGHTGRAY : null, // Передаем цвет только если текстура не задана
                        drawWireframeCheckBox.isSelected(), // Передаем состояние режима полигональной сетки
                        useLightingCheckBox.isSelected() // Передаем состояние режима освещения
                );
            }
        } else {
            System.out.println("Нет активной камеры для рендеринга");
        }
    }

    @FXML
    private void initialize() {
        // Устанавливаем фокус на Canvas
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        // Инициализация цветов и тем
        modelColorPicker.setValue(Color.BLACK);
        backgroundColorPicker.setValue(Color.WHITE);

        // Инициализация текстовых полей
        scaleXField.setText("1.0");
        scaleYField.setText("1.0");
        scaleZField.setText("1.0");

        rotateXField.setText("0.0");
        rotateYField.setText("0.0");
        rotateZField.setText("0.0");

        translateXField.setText("0.0");
        translateYField.setText("0.0");
        translateZField.setText("0.0");
        modelColorPicker.setValue(Color.BLACK);
        backgroundColorPicker.setValue(Color.WHITE);

        // Привязка размеров холста к размерам панели
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        // Инициализация списка моделей
        modelListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            setActiveModel(newVal.intValue());
        });

        // Инициализация CameraManager
        cameraManager = new CameraManager();
        cameraManager.addCamera(new Camera(
                new Vector3f(0, 0, 100),
                new Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 100
        )); // Добавляем начальную камеру
        updateCameraComboBox(); // Обновляем ComboBox
        updateActiveCameraLabel();

        // Привязка обработчика события для useTextureCheckBox
        useTextureCheckBox.setOnAction(event -> {
            boolean useTexture = useTextureCheckBox.isSelected(); // Получаем состояние чекбокса
            System.out.println("Текстура " + (useTexture ? "включена" : "отключена"));
            timeline.playFromStart(); // Перерисовываем сцену с учетом нового состояния текстуры
        });

        // Инициализация таймлайна для рендеринга
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(33), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

            // Получаем активную камеру из CameraManager
            Camera activeCamera = cameraManager.getActiveCamera();
            if (activeCamera != null) {
                activeCamera.setAspectRatio((float) (width / height));

                // Передача цветов и параметров трансформации
                Color modelColor = modelColorPicker.getValue();
                Color backgroundColor = backgroundColorPicker.getValue();

                // Рендеринг всех моделей
                for (Model model : models) {
                    RenderEngine.render(
                            canvas.getGraphicsContext2D(),
                            activeCamera, // Используем активную камеру
                            model,
                            (int) width,
                            (int) height,
                            selectedVertices,
                            modelColor,
                            backgroundColor,
                            isRasterizationEnabled,
                            texture,
                            texture == null ? Color.LIGHTGRAY : null, // Передаем цвет только если текстура не задана
                            drawWireframeCheckBox.isSelected(), // Передаем состояние режима полигональной сетки
                            useLightingCheckBox.isSelected() // Передаем состояние режима освещения
                    );
                }
            } else {
                System.out.println("Нет активной камеры для рендеринга");
            }
            render(); // Вызов метода render() для обновления сцены
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        // Обработка событий мыши
        canvas.setOnMousePressed(event -> handleMousePressed(event));
        canvas.setOnMouseDragged(event -> handleMouseDragged(event));
        canvas.setOnMouseReleased(event -> handleMouseReleased(event));
        canvas.setOnScroll(event -> handleMouseScrolled(event)); // колесо

        // Обработка событий клавиатуры
        canvas.setOnKeyPressed(event -> handleKeyPressed(event));
        canvas.setOnKeyReleased(event -> handleKeyReleased(event));

        // Установка начальных значений
        drawWireframeCheckBox.setSelected(true);
        useTextureCheckBox.setSelected(false);
        useLightingCheckBox.setSelected(true);

        // Подключение обработчиков событий
        drawWireframeCheckBox.setOnAction(event -> {
            boolean isWireframeEnabled = drawWireframeCheckBox.isSelected();
            System.out.println("Wireframe mode: " + (isWireframeEnabled ? "Enabled" : "Disabled"));
            timeline.playFromStart(); // Перерисовываем сцену
        });

        useTextureCheckBox.setOnAction(this::handleUseTexture);
        useLightingCheckBox.setOnAction(this::handleUseLighting);

        // Привязка обработчика события для useTextureCheckBox
        useTextureCheckBox.setOnAction(event -> render());

        // Привязка обработчика события для drawWireframeCheckBox
        drawWireframeCheckBox.setOnAction(event -> render());

        // Привязка обработчика события для useLightingCheckBox
        useLightingCheckBox.setOnAction(event -> render());

        // Привязка обработчика события для других элементов управления
        modelColorPicker.setOnAction(event -> render());
        backgroundColorPicker.setOnAction(event -> render());
    }

    private void handleDrawWireframe(ActionEvent event) {
        boolean isWireframeEnabled = drawWireframeCheckBox.isSelected();
        System.out.println("Wireframe mode: " + (isWireframeEnabled ? "Enabled" : "Disabled"));
        // Пример: перерисовка сцены с учетом нового режима
        timeline.playFromStart();
    }

    @FXML
    private void handleUseTexture(ActionEvent event) {
        boolean isTextureEnabled = useTextureCheckBox.isSelected();
        System.out.println("Texture mode: " + (isTextureEnabled ? "Enabled" : "Disabled"));
        timeline.playFromStart(); // Перерисовываем сцену с учетом нового состояния текстуры
    }

    private void handleUseLighting(ActionEvent event) {
        boolean isLightingEnabled = useLightingCheckBox.isSelected();
        System.out.println("Lighting mode: " + (isLightingEnabled ? "Enabled" : "Disabled"));
        // Пример: перерисовка сцены с учетом нового режима
        timeline.playFromStart();
    }

    // Метод для обновления ComboBox
    private void updateCameraComboBox() {
        cameraComboBox.getItems().clear(); // Очищаем список
        for (int i = 0; i < cameraManager.getCameras().size(); i++) {
            cameraComboBox.getItems().add("Камера " + i); // Добавляем камеру в список
        }
        cameraComboBox.getSelectionModel().select(cameraManager.getActiveCameraIndex()); // Выбираем активную камеру
    }

    // Обработка отпускания клавиши
    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT) {
            // Если отпущена клавиша SHIFT, сохраняем выделенные вершины
            // (ничего не делаем, так как выделение уже сохранено)
        }
    }

    /////////мышь
    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
        isMousePressed = true;

        // Проверяем, нажата ли клавиша SHIFT
        boolean isShiftPressed = event.isShiftDown();

        // Получаем координаты клика мыши
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Вычисляем modelViewProjectionMatrix
        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate(
                models.get(activeModelIndex).getScale().getX(),
                models.get(activeModelIndex).getScale().getY(),
                models.get(activeModelIndex).getScale().getZ(),
                models.get(activeModelIndex).getRotation().getX(),
                models.get(activeModelIndex).getRotation().getY(),
                models.get(activeModelIndex).getRotation().getZ(),
                models.get(activeModelIndex).getTranslation().getX(),
                models.get(activeModelIndex).getTranslation().getY(),
                models.get(activeModelIndex).getTranslation().getZ()
        );
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f modelViewProjectionMatrix = Matrix4f.multiply(projectionMatrix, Matrix4f.multiply(viewMatrix, modelMatrix));

        // Находим ближайшую вершину к клику мыши
        int closestVertexIndex = findClosestVertex(mouseX, mouseY, modelViewProjectionMatrix);

        if (closestVertexIndex != -1) {
            if (!isShiftPressed) {
                // Если SHIFT не нажата, выбираем только одну вершину
                selectedVertices.clear(); // Очищаем список выбранных вершин
                selectedVertices.add(closestVertexIndex);
            } else {
                // Если SHIFT нажата, добавляем вершину в список выбранных
                if (!selectedVertices.contains(closestVertexIndex)) {
                    selectedVertices.add(closestVertexIndex);
                }
            }
        }
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

        // Обновляем углы вращения камеры
        camera.rotateAroundTarget(yaw, pitch);
    }

    private void handleMouseScrolled(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        Camera activeCamera = cameraManager.getActiveCamera();
        if (activeCamera != null) {
            if (deltaY > 0) {
                activeCamera.movePosition(new Vector3f(0, 0, -TRANSLATION));
            } else {
                activeCamera.movePosition(new Vector3f(0, 0, TRANSLATION));
            }
        } else {
            System.out.println("Нет активной камеры для изменения положения");
        }
    }

    // Метод для поиска ближайшей вершины к клику мыши
    private int findClosestVertex(double mouseX, double mouseY, Matrix4f modelViewProjectionMatrix) {
        if (activeModelIndex == -1) return -1;

        Model activeModel = models.get(activeModelIndex);
        double minDistance = Double.MAX_VALUE;
        int closestVertexIndex = -1;

        for (int i = 0; i < activeModel.vertices.size(); i++) {
            Vector3f vertex = activeModel.vertices.get(i);

            // Преобразуем вершину в экранные координаты
            Vector4f vertexVecmath = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1);
            Point2f screenPoint = GraphicConveyor.vertexToPoint(
                    Matrix4f.multiply(modelViewProjectionMatrix, vertexVecmath).normalizeTo3f(),
                    (int) canvas.getWidth(),
                    (int) canvas.getHeight()
            );

            // Вычисляем расстояние до точки мыши
            double distance = Math.sqrt(Math.pow(screenPoint.x - mouseX, 2) + Math.pow(screenPoint.y - mouseY, 2));

            // Если расстояние меньше минимального, обновляем ближайшую вершину
            if (distance < minDistance) {
                minDistance = distance;
                closestVertexIndex = i;
            }
        }

        return closestVertexIndex;
    }

    // Обработка нажатия клавиш
    private void handleKeyPressed(KeyEvent event) {
        Camera activeCamera = cameraManager.getActiveCamera();
        if (activeCamera == null) {
            System.out.println("Нет активной камеры для управления");
            return;
        }

        switch (event.getCode()) {
            case W -> activeCamera.movePosition(new Vector3f(0, 0, -TRANSLATION));
            case S -> activeCamera.movePosition(new Vector3f(0, 0, TRANSLATION));
            case A -> activeCamera.movePositionAndTarget(new Vector3f(TRANSLATION, 0, 0));
            case D -> activeCamera.movePositionAndTarget(new Vector3f(-TRANSLATION, 0, 0));
            case DELETE -> {
                if (activeModelIndex != -1) {
                    Model activeModel = models.get(activeModelIndex);
                    activeModel.removeVertices(selectedVertices);
                    selectedVertices.clear();
                }
            }
        }
    }

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
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) { // S
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
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
    public void applyScale(ActionEvent event) {
        try {
            SCALE = Float.parseFloat(scaleStepField.getText());
            System.out.println("Scale обновлен: " + SCALE);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введено некорректное значение для Scale.");
        }
    }

    @FXML
    public void applyRotation(ActionEvent event) {
        try {
            ROTATION = Float.parseFloat(rotationStepField.getText());
            System.out.println("Rotation обновлен: " + ROTATION);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введено некорректное значение для Rotation.");
        }
    }

    @FXML
    public void applyTranslation(ActionEvent event) {
        try {
            TRANSLATION = Float.parseFloat(translationStepField.getText());
            System.out.println("Translation обновлен: " + TRANSLATION);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введено некорректное значение для Translation.");
        }
    }
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

    @FXML
    private void handleRemoveVerticesButtonClick(ActionEvent event) {
        if (activeModelIndex != -1) {
            Model activeModel = models.get(activeModelIndex);
            for (int vertexIndex : selectedVertices) {
                activeModel.removeVertexAndUpdatePolygons(vertexIndex);
            }
            selectedVertices.clear();
        }
    }

    @FXML
    private VBox settingsPanel;

    @FXML
    private ColorPicker modelColorPicker;

    @FXML
    private ColorPicker backgroundColorPicker;

    @FXML
    private TextField scaleXField, scaleYField, scaleZField;

    @FXML
    private TextField rotateXField, rotateYField, rotateZField;

    @FXML
    private TextField translateXField, translateYField, translateZField;

    // Метод для переключения видимости панели настроек
    @FXML
    private void toggleSettingsPanel() {
        settingsPanel.setVisible(!settingsPanel.isVisible());
    }

    // Метод для изменения цвета линий модели
    @FXML
    private void handleModelColorChange(ActionEvent event) {
        Color color = modelColorPicker.getValue();
        canvas.getGraphicsContext2D().setStroke(color);
    }

    // Метод для изменения цвета фона
    @FXML
    private void handleBackgroundColorChange(ActionEvent event) {
        Color color = backgroundColorPicker.getValue();
        canvas.getGraphicsContext2D().setFill(color);
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // Метод для переключения на светлую тему
    @FXML
    private void handleLightTheme(ActionEvent event) {
        modelColorPicker.setValue(Color.BLACK);
        backgroundColorPicker.setValue(Color.WHITE);
    }

    // Метод для переключения на тёмную тему
    @FXML
    private void handleDarkTheme(ActionEvent event) {
        modelColorPicker.setValue(Color.LIGHTGRAY);
        backgroundColorPicker.setValue(Color.rgb(64, 64, 64));
    }

    // Метод для применения трансформаций
    @FXML
    private void handleApplyTransformations(ActionEvent event) {
        if (activeModelIndex == -1) return;

        Model activeModel = models.get(activeModelIndex);

        try {
            float scaleX = Float.parseFloat(scaleXField.getText());
            float scaleY = Float.parseFloat(scaleYField.getText());
            float scaleZ = Float.parseFloat(scaleZField.getText());

            float rotateX = Float.parseFloat(rotateXField.getText());
            float rotateY = Float.parseFloat(rotateYField.getText());
            float rotateZ = Float.parseFloat(rotateZField.getText());

            float translateX = Float.parseFloat(translateXField.getText());
            float translateY = Float.parseFloat(translateYField.getText());
            float translateZ = Float.parseFloat(translateZField.getText());

            activeModel.setScale(new Vector3f(scaleX, scaleY, scaleZ));
            activeModel.setRotation(new Vector3f(rotateX, rotateY, rotateZ));
            activeModel.setTranslation(new Vector3f(translateX, translateY, translateZ));

            timeline.playFromStart();
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите корректные числа для трансформации.");
        }
    }


    // Новые методы для управления триангуляцией и растеризацией

    private boolean isTriangulationApplied = false; // Флаг для проверки, была ли уже применена триангуляция
    private Model originalModel; // Переменная для хранения оригинальной модели
    @FXML
    private void handleTriangulate(ActionEvent event) {
        if (!isTriangulationApplied) {
            isTriangulationEnabled = true;
            System.out.println("Триангуляция включена");
            if (activeModelIndex != -1) {
                Model activeModel = models.get(activeModelIndex);
                originalModel = new Model(); // Создаем копию оригинальной модели
                originalModel.vertices = new ArrayList<>(activeModel.vertices);
                originalModel.polygons = new ArrayList<>(activeModel.polygons);
                models.set(activeModelIndex, Triangulation.getTriangulatedModel(activeModel));
                isTriangulationApplied = true;
                timeline.playFromStart();
            }
        } else {
            System.out.println("Триангуляция уже была применена");
        }
    }

    @FXML
    private void handleDisableTriangulate(ActionEvent event) {
        isTriangulationEnabled = false;
        isTriangulationApplied = false;
        System.out.println("Триангуляция отключена");
        if (activeModelIndex != -1 && originalModel != null) {
            models.set(activeModelIndex, originalModel); // Возвращаем оригинальную модель
            timeline.playFromStart(); // Перерисовываем сцену
        }
    }

    @FXML
    private void handleEnableRasterization(ActionEvent event) {
        isRasterizationEnabled = true;
        System.out.println("Растеризация включена");
        timeline.playFromStart(); // Перерисовываем сцену
    }

    @FXML
    private void handleDisableRasterization(ActionEvent event) {
        isRasterizationEnabled = false;
        System.out.println("Растеризация отключена");
        timeline.playFromStart(); // Перерисовываем сцену
    }

    private Model loadOriginalModel() {
// Возвращаем сохранённую оригинальную модель
        return originalModel;
    }

    private Image texture;
    @FXML
    private void onLoadTextureMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setTitle("Загрузить текстуру");

        File file = fileChooser.showOpenDialog((anchorPane.getScene().getWindow()));
        if (file != null) {
            texture = new Image(file.toURI().toString());
            System.out.println("Загруженная текстура: " + file.getName());
            render(); // Перерисовываем сцену с новой текстурой
        }
    }




    @FXML
    private Button addCameraButton;

    @FXML
    private Button removeCameraButton;

    @FXML
    private Button switchCameraButton;

    @FXML
    private Label activeCameraLabel;

    private CameraManager cameraManager;

    @FXML
    private ComboBox<String> cameraComboBox;

    @FXML
    private void handleAddCamera() {
        Camera newCamera = new Camera(
                new Vector3f(0, 0, 100),
                new Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 100
        );
        cameraManager.addCamera(newCamera);
        updateCameraComboBox(); // Обновляем ComboBox
        updateActiveCameraLabel();

        // Убеждаемся, что Canvas в фокусе
        canvas.requestFocus();
    }

    @FXML
    private void handleRemoveCamera() {
        int activeIndex = cameraManager.getActiveCameraIndex();
        if (activeIndex >= 0) {
            cameraManager.removeCamera(activeIndex);
            updateCameraComboBox(); // Обновляем ComboBox
            updateActiveCameraLabel();
        }
    }

    private void updateActiveCameraLabel() {
        Camera activeCamera = cameraManager.getActiveCamera();
        if (activeCamera != null) {
            activeCameraLabel.setText("Активная камера: " + cameraManager.getActiveCameraIndex());
        } else {
            activeCameraLabel.setText("Нет активной камеры");
        }
    }

    @FXML
    private void handleCameraSelection() {
        int selectedIndex = cameraComboBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < cameraManager.getCameras().size()) {
            cameraManager.setActiveCamera(selectedIndex);
            updateActiveCameraLabel();

            canvas.setOnKeyPressed(event -> handleKeyPressed(event));
            canvas.setOnKeyReleased(event -> handleKeyReleased(event));

            canvas.requestFocus();
        }
    }

    @FXML
    private CheckBox drawWireframeCheckBox;

    @FXML
    private CheckBox useTextureCheckBox;

    @FXML
    private CheckBox useLightingCheckBox;
}