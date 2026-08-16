package com.gavtechmaster;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Screen;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.SystemTray;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jthemedetecor.OsThemeDetector;

public class App extends Application {
    Map<String, Object> userPreferences = Collections.synchronizedMap(new HashMap<>());
    Deque<Serializable> copiedListSet = new ConcurrentLinkedDeque<>();
    Screen primaryScreen = Screen.getPrimary();
    String fontPath = getClass().getResource("/fonts/geomini.ttf").toExternalForm();
    Image icon = new Image(getClass().getResource("/images/legacyboard.png").toExternalForm());
    Image settingIcon = new Image(getClass().getResource("/images/legear.png").toExternalForm());
    Image fileIcon = new Image(getClass().getResource("/images/legacyfolder.png").toExternalForm());
    Image warningIcon = new Image(getClass().getResource("/images/legacywarning.png").toExternalForm());
    boolean existingUser;
    File appDataDir;
    File configDir;
    File configFile;
    File logFile;
    File openFile;
    File windowStatusFile;
    Gson gson = new Gson();
    OsThemeDetector themeDetector = OsThemeDetector.getDetector();
    TrayIcon legacyTrayIcon;
    boolean clipboardTab = true;
    boolean animationPlaying = false;
    Toolkit systemToolkit = Toolkit.getDefaultToolkit();
    java.awt.Insets systemInsets = systemToolkit.getScreenInsets(
        GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice()
        .getDefaultConfiguration());
    
    enum OS {
        WINDOWS,
        MACOS,
        LINUX
    }

    public OS currentOS() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return OS.WINDOWS;
        } else if (System.getProperty("os.name").toLowerCase().contains("mac") || System.getProperty("os.name").toLowerCase().contains("os x")) {
            return OS.MACOS;
        } else {
            return OS.LINUX;
        }
    }

    // Made method to be able to update variable within lambada
    public void updateScreenDimensions() {
        systemInsets = systemToolkit.getScreenInsets(
            GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration());
        primaryScreen = Screen.getPrimary();
    }

    /* Checks filesystem for the AppData directory for their
     * respective operating system, creates one if it doesn't exist.
     * 
     * Returns true if existing user, false if it isn't
     */
    private boolean initializeAppData() {
        String currentOS = System.getProperty("os.name").toLowerCase();
        String appDataPath;
        String configDirPath;
        if (currentOS.contains("win")) {
            if (System.getenv("LOCALAPPDATA") != null) {
                appDataPath = System.getenv("LOCALAPPDATA") + File.separator + "The Legacy Clipboard";
            } else {
                appDataPath = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local" + File.separator + "The Legacy Clipboard";
            }
            appDataDir = new File(appDataPath);
            configDir = appDataDir;
            if (!appDataDir.exists()) {
                appDataDir.mkdirs();
                logFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardLogs.ser");
                openFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardOpen.dat");
                try {
                    Files.setAttribute(openFile.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
                } catch (IOException e) {}
                windowStatusFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardWindow.dat");
                try {
                    Files.setAttribute(windowStatusFile.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
                } catch (IOException e) {}
                configFile = new File(configDir.getPath() + File.separator + "TheLegacyClipboardConfig.json");
                return false;
            } else {
                logFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardLogs.ser");
                openFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardOpen.dat");
                try {
                    Files.setAttribute(openFile.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
                } catch (IOException e) {}
                windowStatusFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardWindow.dat");
                try {
                    Files.setAttribute(windowStatusFile.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
                } catch (IOException e) {}
                configFile = new File(configDir.getPath() + File.separator + "TheLegacyClipboardConfig.json");
                return true;
            }
        } else if (currentOS.contains("mac") || currentOS.contains("os x")) {
            appDataPath = System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + "The Legacy Clipboard";
            appDataDir = new File(appDataPath);
            configDir = appDataDir;
            if (!appDataDir.exists()) {
                appDataDir.mkdirs();
                logFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardLogs.ser");
                openFile = new File(appDataDir.getPath() + File.separator + ".TheLegacyClipboardOpen.dat");
                windowStatusFile = new File(appDataDir.getPath() + File.separator + ".TheLegacyClipboardWindow.dat");
                configFile = new File(configDir.getPath() + File.separator + "TheLegacyClipboardConfig.json");
                return false;
            } else {
                logFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardLogs.ser");
                openFile = new File(appDataDir.getPath() + File.separator + ".TheLegacyClipboardOpen.dat");
                windowStatusFile = new File(appDataDir.getPath() + File.separator + ".TheLegacyClipboardWindow.dat");
                configFile = new File(configDir.getPath() + File.separator + "TheLegacyClipboardConfig.json");
                return true;
            }
        } else {
            boolean sameDataDir = false; /* Checking if the configuration files 
            and the app data should be in the same directory */

            if (System.getenv("XDG_CONFIG_HOME") != null) {
                configDirPath = System.getenv("XDG_CONFIG_HOME") + File.separator + "The Legacy Clipboard";
            } else if (new File(System.getProperty("user.home") + File.separator + ".config").exists()) { /* If the user doesn't have the 
            XDG_CONFIG_HOME environment variable, it manually checks the directory */
                configDirPath = System.getProperty("user.home") + File.separator + ".config" + File.separator + "The Legacy Clipboard";
            } else if (System.getenv("XDG_DATA_HOME") != null) {
                configDirPath = System.getenv("XDG_DATA_HOME") + File.separator + "The Legacy Clipboard";
                sameDataDir = true;
            } else if (new File(System.getProperty("user.home") + File.separator + ".local" + File.separator + "share").exists()) { /* If the user doesn't have the 
            XDG_DATA_HOME environment variable, it manually checks the directory */
                configDirPath = System.getProperty("user.home") + File.separator + ".local" + File.separator + "share" + File.separator + "The Legacy Clipboard";
                sameDataDir = true;
            } else {
                configDirPath = System.getProperty("user.home") + File.separator + ".The Legacy Clipboard"; // Just places both files in the same directory in home user
                sameDataDir = true;
            }

            if (sameDataDir) {
                appDataPath = configDirPath;
            } else if (System.getenv("XDG_DATA_HOME") != null) {
                appDataPath = System.getenv("XDG_DATA_HOME") + File.separator + "The Legacy Clipboard";
            } else if (new File(System.getProperty("user.home") + File.separator + ".local" + File.separator + "share").exists()) { /*  If the user doesn't have the 
            XDG_CONFIG_HOME environment variable, it manually checks the directory */
                appDataPath = System.getProperty("user.home") + File.separator + ".local" + File.separator + "share" + File.separator + "The Legacy Clipboard";
            } else if (System.getenv("XDG_CONFIG_HOME") != null) { 
                appDataPath = System.getenv("XDG_CONFIG_HOME") + File.separator + "The Legacy Clipboard";
                sameDataDir = true;
            } else if (new File(System.getProperty("user.home") + File.separator + ".config").exists()) { /*  If the user doesn't have the 
            XDG_CONFIG_HOME environment variable, it manually checks the directory */
                appDataPath = System.getProperty("user.home") + File.separator + ".config" + File.separator + "The Legacy Clipboard";
                sameDataDir = true;
            } else {
                appDataPath = System.getProperty("user.home") + File.separator + ".The Legacy Clipboard"; // Just places both files in the same directory in home user
                sameDataDir = true;
            }

            boolean returnValue = true;

            appDataDir = new File(appDataPath);
            configDir = new File(configDirPath);

            if (!appDataDir.exists()) {
                appDataDir.mkdirs();
                logFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardLogs.ser");
                openFile = new File(appDataDir.getPath() + File.separator + ".TheLegacyClipboardOpen.dat");
                windowStatusFile = new File(appDataDir.getPath() + File.separator + ".TheLegacyClipboardWindow.dat");
                returnValue = false;
            } else {
                logFile = new File(appDataDir.getPath() + File.separator + "TheLegacyClipboardLogs.ser");
                openFile = new File(appDataDir.getPath() + File.separator + ".TheLegacyClipboardOpen.dat");
                windowStatusFile = new File(appDataDir.getPath() + File.separator + ".TheLegacyClipboardWindow.dat");
            }

            if (!configDir.exists()) {
                configDir.mkdirs();
                configFile = new File(configDir.getPath() + File.separator + "TheLegacyClipboardConfig.json");
                returnValue = false;
            } else {
                configFile = new File(configDir.getPath() + File.separator + "TheLegacyClipboardConfig.json");
            }

            return returnValue;
        }
    }

    public static Background createPlainBackground(Color backgroundColor) {
        Background background = new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY));
        return background;
    }

    public static Background createPlainBackground(Color backgroundColor, CornerRadii radius) {
        Background background = new Background(new BackgroundFill(backgroundColor, radius, Insets.EMPTY));
        return background;
    }

    public static Background createPlainBackground(Color backgroundColor, CornerRadii radius, Insets insets) {
        Background background = new Background(new BackgroundFill(backgroundColor, radius, insets));
        return background;
    }

    public static boolean imageEquals(Image image1, Image image2) {
        if (image1 == null || image2 == null) {
            return false;
        }
        if (image1 == image2) {
            return true;
        }
        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            return false;
        }
        PixelReader image1Reader = image1.getPixelReader();
        PixelReader image2Reader = image2.getPixelReader();
        for (int x = 0; x < image1.getWidth(); x++) {
            for (int y = 0; y < image1.getHeight(); y++) {
                if (image1Reader.getArgb(x, y) != image2Reader.getArgb(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked") // Left unchecked to allow for multiple data types
    @Override
    public void init() {
        existingUser = initializeAppData();
        try (Scanner configFileScan = new Scanner(configFile)) {
            while (configFileScan.hasNext()) {
                String jsonLine = configFileScan.nextLine().trim();
                if (jsonLine.length() > 0) {
                    userPreferences = gson.fromJson(jsonLine, new TypeToken<Map<String, Object>>(){}.getType());
                    break;
                }
                throw new Exception("Json not properly stored");
            }
        } catch (Exception jsonException) { // Purposely decided to group all exceptions
            userPreferences.put("clipboard_enabled", true);
            userPreferences.put("copy_text", true);
            userPreferences.put("copy_images", true);
            userPreferences.put("copy_files", true);
            userPreferences.put("notifications_on", false);
            userPreferences.put("max_items", 15);
            try (FileWriter configFileWriter = new FileWriter(configFile)) {
                String jsonString = gson.toJson(userPreferences);
                configFileWriter.write(jsonString);
            } catch (IOException ioError) {
                initializeAppData();
            }
        }

        try (ObjectInputStream logReader = new ObjectInputStream(new FileInputStream(logFile))) {
            copiedListSet = (Deque<Serializable>) logReader.readObject();
        } catch (Exception anyException) {
            initializeAppData();
        }

        try {
            openFile.createNewFile();
        } catch (IOException ioError) {}
    }

    private Group getRectBackground(Stage mainStage) {
        VBox vboxDecorations = new VBox();
        vboxDecorations.setAlignment(Pos.TOP_LEFT);
        vboxDecorations.setSpacing(mainStage.getHeight() / 30);
        for (int i = 0; i < 30; i++) {
            Rectangle rectBox = new Rectangle(
                mainStage.getWidth() / 3, 
                mainStage.getHeight() / 40, 
                (themeDetector.isDark()) ? Color.rgb(66, 66, 66) : Color.rgb(227, 227, 227)
            );
            TranslateTransition rectTransition = new TranslateTransition(Duration.millis(800), rectBox);
            if (i % 2 == 0) {
                rectTransition.setFromX(0);
                rectTransition.setToX(mainStage.getWidth() - rectBox.getWidth());
            } else {
                rectTransition.setFromX(mainStage.getWidth() - rectBox.getWidth());
                rectTransition.setToX(0);
            }
            rectTransition.setAutoReverse(true);
            rectTransition.setCycleCount(Animation.INDEFINITE);
            vboxDecorations.getChildren().add(rectBox);
            rectTransition.play();
        }

        HBox hboxDecorations = new HBox();
        hboxDecorations.setAlignment(Pos.TOP_LEFT);
        hboxDecorations.setSpacing(mainStage.getHeight() / 30);
        for (int i = 0; i < 30; i++) {
            Rectangle rectBox = new Rectangle(
                mainStage.getHeight() / 40, 
                mainStage.getWidth() / 3, 
                (themeDetector.isDark()) ? Color.rgb(66, 66, 66) : Color.rgb(227, 227, 227)
            );
            TranslateTransition rectTransition = new TranslateTransition(Duration.millis(800), rectBox);
            if (i % 2 == 0) {
                rectTransition.setFromY(0);
                rectTransition.setToY(mainStage.getHeight() - rectBox.getHeight());
            } else {
                rectTransition.setFromY(mainStage.getHeight() - rectBox.getHeight());
                rectTransition.setToY(0);
            }
            rectTransition.setAutoReverse(true);
            rectTransition.setCycleCount(Animation.INDEFINITE);
            hboxDecorations.getChildren().add(rectBox);
            rectTransition.play();
        }
        Group rectGroup = new Group(vboxDecorations, hboxDecorations);
        return rectGroup;
    }

    // Configuring the scene for the startup scene
    private Scene configureStartupScene(Stage mainStage) {
        // Sorry in advanced for the unorganized node configurations
        Double[] gridPercentages = {12.5, 12.5, 50.0, 25.0};
        Group rectBackground = getRectBackground(mainStage);
        VBox vboxDecorations = (VBox) rectBackground.getChildren().get(0);
        HBox hboxDecorations = (HBox) rectBackground.getChildren().get(1);
        StackPane mainPane = new StackPane();
        ImageView startupIcon = new ImageView(icon);
        startupIcon.setPreserveRatio(true);
        startupIcon.setFitWidth(mainStage.getWidth()/2);
        startupIcon.setFitHeight(mainStage.getHeight()/2);
        GridPane startupGPane = new GridPane();
        Label startupLabel = new Label("Welcome to the Legacy Clipboard!");
        Label startupDesc = new Label("advanced clipboard support for legacy systems");
        Button startupButton = new Button("Start");
        startupButton.setDisable(true);
        startupButton.setFont(Font.font(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 20) / 2).getFamily(), FontPosture.ITALIC, (double) ((double) mainStage.getHeight() / 10) / 2));
        startupButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
        startupDesc.setAlignment(Pos.CENTER);
        startupLabel.setAlignment(Pos.CENTER);
        GridPane.setHalignment(startupLabel, HPos.CENTER);
        GridPane.setHalignment(startupDesc, HPos.CENTER);
        GridPane.setHalignment(startupIcon, HPos.CENTER);
        GridPane.setHalignment(startupButton, HPos.CENTER);
        GridPane.setValignment(startupIcon, VPos.CENTER);
        GridPane.setValignment(startupButton, VPos.CENTER);
        Platform.runLater(() -> {
            startupGPane.getColumnConstraints().add(new ColumnConstraints(mainStage.getScene().getWidth()));
        });
        for (Double gridPercent: gridPercentages) {
            RowConstraints rowConstraint = new RowConstraints();
            rowConstraint.setPercentHeight(gridPercent);
            startupGPane.getRowConstraints().add(rowConstraint);
        }
        startupLabel.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 10) / 2));
        startupDesc.setFont(Font.font(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 20) / 2).getFamily(), FontPosture.ITALIC, (double) ((double) mainStage.getHeight() / 20) / 2));
        startupGPane.add(startupLabel, 0, 0);
        startupGPane.add(startupDesc, 0, 1);
        startupGPane.add(startupIcon, 0, 2);
        startupGPane.add(startupButton, 0, 3);
        Scene startupScene = new Scene(mainPane);

        // Fades in the nodes
        FadeTransition startupLabelFade = new FadeTransition(Duration.millis(300), startupLabel);
        FadeTransition startupDescFade = new FadeTransition(Duration.millis(300), startupDesc);
        FadeTransition startupIconFade = new FadeTransition(Duration.millis(300), startupIcon);
        FadeTransition startupButtonFade = new FadeTransition(Duration.millis(300), startupButton);
        ScaleTransition setupButtonScale = new ScaleTransition(Duration.millis(800), startupButton);
        setupButtonScale.setFromX(1);
        setupButtonScale.setToX(1.1);
        setupButtonScale.setFromY(1);
        setupButtonScale.setToY(1.1);
        setupButtonScale.setAutoReverse(true);
        setupButtonScale.setCycleCount(Animation.INDEFINITE);
        startupLabelFade.setFromValue(0);
        startupLabelFade.setToValue(1.0);
        startupDescFade.setFromValue(0);
        startupDescFade.setToValue(1.0);
        startupIconFade.setFromValue(0);
        startupIconFade.setToValue(1.0);
        startupButtonFade.setFromValue(0);
        startupButtonFade.setToValue(1.0);
        startupButtonFade.setOnFinished(finished -> {
            startupButton.setDisable(false);
            setupButtonScale.play();
        });

        SequentialTransition startupMenuTranstition = new SequentialTransition(
            startupLabelFade,
            startupDescFade,
            startupIconFade,
            startupButtonFade
        );

        startupButton.setOnMouseEntered(hovered -> {
            if (!startupButton.isDisabled()) {
                startupButton.setBackground(createPlainBackground(Color.rgb(80, 127, 205), new CornerRadii(50)));
                startupScene.setCursor(Cursor.HAND);
            }
        });

        startupButton.setOnMouseExited(notHovered -> {
            if (!startupButton.isDisabled()) {
                startupButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
                startupScene.setCursor(Cursor.DEFAULT);
            }
        });

        // Determines theme based on system
        if (themeDetector.isDark()) {
            mainPane.setBackground(createPlainBackground(Color.rgb(55, 55, 55)));
            startupLabel.setTextFill(Color.WHITE);
            startupDesc.setTextFill(Color.WHITE);
            startupButton.setTextFill(Color.WHITE);
        } else {
            mainPane.setBackground(createPlainBackground(Color.WHITE));
            startupLabel.setTextFill(Color.BLACK);
            startupDesc.setTextFill(Color.BLACK);
            startupButton.setTextFill(Color.BLACK);
        }
        themeDetector.registerListener((isDark) -> {
            Platform.runLater(() -> {
                if (isDark) {
                    mainPane.setBackground(createPlainBackground(Color.rgb(55, 55, 55)));
                    startupLabel.setTextFill(Color.WHITE);
                    startupDesc.setTextFill(Color.WHITE);
                    startupButton.setTextFill(Color.WHITE);
                    startupButton.setBackground(createPlainBackground(Color.PURPLE, new CornerRadii(50)));
                    for (Node rect: vboxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(66, 66, 66));
                    }

                    for (Node rect: hboxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(66, 66, 66));
                    }
                } else {
                    mainPane.setBackground(createPlainBackground(Color.WHITE));
                    startupLabel.setTextFill(Color.BLACK);
                    startupDesc.setTextFill(Color.BLACK);
                    startupButton.setTextFill(Color.BLACK);
                    startupButton.setBackground(createPlainBackground(Color.CYAN, new CornerRadii(50)));
                    for (Node rect: vboxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(227, 227, 227));
                    }

                    for (Node rect: hboxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(227, 227, 227));
                    }
                }
            });
        });

        startupButton.setOnAction(pressed -> {
            mainStage.setScene(configureInfoScene(mainStage));
        });

        mainPane.getChildren().addAll(rectBackground, startupGPane);
        StackPane.setAlignment(startupGPane, Pos.TOP_LEFT);
        StackPane.setAlignment(rectBackground, Pos.TOP_LEFT);
        startupMenuTranstition.play();
        return startupScene;
    }

    private Scene configureInfoScene(Stage mainStage) {
        Double[] gridPercentages = {12.5, 37.5, 25.0, 25.0};
        Group rectBackground = getRectBackground(mainStage);
        VBox vboxDecorations = (VBox) rectBackground.getChildren().get(0);
        HBox hboxDecorations = (HBox) rectBackground.getChildren().get(1);
        StackPane mainPane = new StackPane();
        GridPane infoGrid = new GridPane();
        VBox infoList = new VBox();
        VBox infoOpenSource = new VBox();
        infoList.setSpacing((double)37.5/3);
        infoOpenSource.setAlignment(Pos.CENTER);
        Scene infoScene = new Scene(mainPane);
        Button continueButton = new Button("Continue");
        Label infoTrack = new Label("\u2022 Track and manage what you copy on your clipboard.");
        Label infoFlush = new Label("\u2022 Remember text, images, and files on clipboard.");
        Label infoCustom = new Label("\u2022 Customize your clipboard experience.");
        Label infoTitle = new Label("With this software, you can:");
        Label infoGithub = new Label("This project is open source on Github:");
        Hyperlink linkToGithub = new Hyperlink("Link to the Github Page");
        linkToGithub.setTextFill(Color.rgb(130, 177, 255));
        linkToGithub.setUnderline(true);
        infoTitle.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 10) / 2));
        infoGithub.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 12) / 2));
        linkToGithub.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 12) / 2));
        infoList.maxWidthProperty().bind(mainStage.widthProperty());
        infoTrack.setFont(Font.font(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 20) / 2).getFamily(), FontPosture.REGULAR, (double) mainStage.getHeight() / 30));
        infoFlush.setFont(Font.font(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 20) / 2).getFamily(), FontPosture.REGULAR, (double) mainStage.getHeight() / 30));
        infoCustom.setFont(Font.font(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 20) / 2).getFamily(), FontPosture.REGULAR, (double) mainStage.getHeight() / 30));
        continueButton.setFont(Font.font(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 20) / 2).getFamily(), FontPosture.ITALIC, (double) ((double) mainStage.getHeight() / 10) / 2));
        continueButton.setDisable(true);
        continueButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
        infoList.getChildren().addAll(infoTrack, infoFlush, infoCustom);
        infoList.setAlignment(Pos.CENTER);
        GridPane.setHalignment(infoTitle, HPos.CENTER);
        GridPane.setHalignment(continueButton, HPos.CENTER);
        Platform.runLater(() -> {
            infoGrid.getColumnConstraints().add(new ColumnConstraints(mainStage.getWidth()));
        });
        for (Double gridPercent: gridPercentages) {
            RowConstraints rowConstraint = new RowConstraints();
            rowConstraint.setPercentHeight(gridPercent);
            infoGrid.getRowConstraints().add(rowConstraint);
        }
        infoOpenSource.getChildren().addAll(infoGithub, linkToGithub);
        infoGrid.add(infoTitle, 0, 0);
        infoGrid.add(infoList, 0, 1);
        infoGrid.add(infoOpenSource, 0, 2);
        infoGrid.add(continueButton, 0, 3);
        if (themeDetector.isDark()) {
            mainPane.setBackground(createPlainBackground(Color.rgb(55, 55, 55)));
            infoTrack.setTextFill(Color.WHITE);
            infoFlush.setTextFill(Color.WHITE);
            infoCustom.setTextFill(Color.WHITE);
            infoTitle.setTextFill(Color.WHITE);
            infoGithub.setTextFill(Color.WHITE);
            continueButton.setTextFill(Color.WHITE);
        } else {
            mainPane.setBackground(createPlainBackground(Color.WHITE));
            infoTrack.setTextFill(Color.BLACK);
            infoFlush.setTextFill(Color.BLACK);
            infoCustom.setTextFill(Color.BLACK);
            infoTitle.setTextFill(Color.BLACK);
            infoGithub.setTextFill(Color.BLACK);
            continueButton.setTextFill(Color.BLACK);
        }
        themeDetector.registerListener(isDark -> {
            if (isDark) {
                mainPane.setBackground(createPlainBackground(Color.rgb(55, 55, 55)));
                infoTrack.setTextFill(Color.WHITE);
                infoFlush.setTextFill(Color.WHITE);
                infoCustom.setTextFill(Color.WHITE);
                infoTitle.setTextFill(Color.WHITE);
                infoGithub.setTextFill(Color.WHITE);
                continueButton.setTextFill(Color.WHITE);
                continueButton.setBackground(createPlainBackground(Color.PURPLE, new CornerRadii(50)));
                for (Node rect: vboxDecorations.getChildren()) {
                    Rectangle rectInstance = (Rectangle) rect;
                    rectInstance.setFill(Color.rgb(66, 66, 66));
                }
                for (Node rect: hboxDecorations.getChildren()) {
                    Rectangle rectInstance = (Rectangle) rect;
                    rectInstance.setFill(Color.rgb(66, 66, 66));
                }
            } else {
                mainPane.setBackground(createPlainBackground(Color.WHITE));
                infoTrack.setTextFill(Color.BLACK);
                infoFlush.setTextFill(Color.BLACK);
                infoCustom.setTextFill(Color.BLACK);
                infoTitle.setTextFill(Color.BLACK);
                infoGithub.setTextFill(Color.BLACK);
                continueButton.setTextFill(Color.BLACK);
                continueButton.setBackground(createPlainBackground(Color.CYAN, new CornerRadii(50)));
                for (Node rect: vboxDecorations.getChildren()) {
                    Rectangle rectInstance = (Rectangle) rect;
                    rectInstance.setFill(Color.rgb(227, 227, 227));
                }

                for (Node rect: hboxDecorations.getChildren()) {
                    Rectangle rectInstance = (Rectangle) rect;
                    rectInstance.setFill(Color.rgb(227, 227, 227));
                }
            }
        });
        linkToGithub.setOnAction(linked -> {
            getHostServices().showDocument("https://github.com");
        });
        continueButton.setOnMouseEntered(hovered -> {
            if (!continueButton.isDisabled()) {
                continueButton.setBackground(createPlainBackground(Color.rgb(80, 127, 205), new CornerRadii(50)));
                continueButton.setCursor(Cursor.HAND);
            }
        });
        continueButton.setOnMouseExited(notHovered -> {
            if (!continueButton.isDisabled()) {
                continueButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
                continueButton.setCursor(Cursor.DEFAULT);
            }
        });
        FadeTransition titleTransition = new FadeTransition(Duration.millis(300), infoTitle);
        titleTransition.setFromValue(0);
        titleTransition.setToValue(1.0);
        FadeTransition trackTransition = new FadeTransition(Duration.millis(800), infoList.getChildren().get(0));
        trackTransition.setFromValue(0);
        trackTransition.setToValue(1.0);
        FadeTransition flushTransition = new FadeTransition(Duration.millis(800), infoList.getChildren().get(1));
        flushTransition.setFromValue(0);
        flushTransition.setToValue(1.0);
        FadeTransition CLITransition = new FadeTransition(Duration.millis(800), infoList.getChildren().get(2));
        CLITransition.setFromValue(0);
        CLITransition.setToValue(1.0);
        FadeTransition githubTransition = new FadeTransition(Duration.millis(300), infoOpenSource.getChildren().get(0));
        githubTransition.setFromValue(0);
        githubTransition.setToValue(1.0);
        FadeTransition githubLinkTransition = new FadeTransition(Duration.millis(300), infoOpenSource.getChildren().get(1));
        githubLinkTransition.setFromValue(0);
        githubLinkTransition.setToValue(1.0);
        FadeTransition continueTransition = new FadeTransition(Duration.millis(300), continueButton);
        ScaleTransition continueScaleTransition = new ScaleTransition(Duration.millis(800), continueButton);
        continueScaleTransition.setFromX(1);
        continueScaleTransition.setFromY(1);
        continueScaleTransition.setToX(1.1);
        continueScaleTransition.setToY(1.1);
        continueScaleTransition.setAutoReverse(true);
        continueScaleTransition.setCycleCount(Animation.INDEFINITE);
        continueTransition.setFromValue(0);
        continueTransition.setToValue(1.0);
        continueTransition.setOnFinished(finished -> {
            continueButton.setDisable(false);
            continueScaleTransition.play();
        });
        SequentialTransition infoTransition = new SequentialTransition(
            titleTransition,
            trackTransition,
            flushTransition,
            CLITransition,
            githubTransition,
            githubLinkTransition,
            continueTransition
        );
        continueButton.setOnAction(pressed -> {
            mainStage.setScene(startMainApp(mainStage));
        });
        infoTransition.play();
        mainPane.getChildren().addAll(rectBackground, infoGrid);
        StackPane.setAlignment(rectBackground, Pos.TOP_LEFT);
        return infoScene;
    }


    private Scene startMainApp(Stage mainStage) {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        ClipboardContent addToSystemClipboard = new ClipboardContent();
        VBox copiedVBox = new VBox();
        Function<String, Button> copiedItemBlueprint = copiedItem -> {
            Button textButton = new Button(copiedItem);
            textButton.setWrapText(true);
            textButton.setFont(Font.font(Font.loadFont(fontPath, 15).getFamily(), FontWeight.BOLD, 15));
            textButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
            textButton.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
            textButton.setPadding(new Insets(20));
            textButton.setOnMouseEntered(entered -> {
                if (clipboardTab) {
                    textButton.setCursor(Cursor.HAND);
                    textButton.setBackground(createPlainBackground(Color.rgb(130, 177, 255), new CornerRadii(50)));
                }
            });
            textButton.setOnMouseExited(entered -> {
                textButton.setCursor(Cursor.DEFAULT);
                textButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
            });
            textButton.setOnAction(pressed -> {
                if (systemClipboard.getString() != null && clipboardTab) {
                    if (!systemClipboard.getString().equals(copiedItem)) {
                        addToSystemClipboard.putString(copiedItem);
                        systemClipboard.setContent(addToSystemClipboard);
                    }
                } else if (systemClipboard.getString() == null && clipboardTab) {
                    if (!systemClipboard.getString().equals(copiedItem)) {
                        addToSystemClipboard.putString(copiedItem);
                        systemClipboard.setContent(addToSystemClipboard);
                    }
                }
            });
            Platform.runLater(() -> {
                textButton.setMaxWidth(mainStage.getScene().getWidth() / 1.5);
                textButton.setPrefWidth(mainStage.getScene().getWidth() / 1.5);
                textButton.setPrefHeight(mainStage.getScene().getHeight() / 3);
            });
            return textButton;
        };
        Function<Image, Button> copiedImageBlueprint = copiedImage -> {
            ImageView renderedCopiedImage = new ImageView(copiedImage);
            renderedCopiedImage.setPreserveRatio(true);
            renderedCopiedImage.setScaleX(0.9);
            renderedCopiedImage.setScaleY(0.9);
            Button imageButton = new Button();
            imageButton.setGraphic(renderedCopiedImage);
            imageButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
            imageButton.setPadding(new Insets(10));
            imageButton.setOnMouseEntered(hovered -> {
                if (clipboardTab) {
                    imageButton.setBackground(createPlainBackground(Color.rgb(130, 177, 255), new CornerRadii(50)));
                }
            });
            imageButton.setOnMouseExited(notHovered -> {
                imageButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
            });
            imageButton.setOnAction(pressed -> {
                if (systemClipboard.getImage() != null && clipboardTab) {
                    if (!systemClipboard.getImage().equals(copiedImage)) {
                        addToSystemClipboard.putImage(copiedImage);
                        systemClipboard.setContent(addToSystemClipboard);
                    }
                } else if (systemClipboard.getImage() == null && clipboardTab) {
                    if (!systemClipboard.getImage().equals(copiedImage)) {
                        addToSystemClipboard.putImage(copiedImage);
                        systemClipboard.setContent(addToSystemClipboard);
                    }
                }
            });
            Platform.runLater(() -> {
                renderedCopiedImage.setFitWidth(mainStage.getScene().getWidth() / 1.5);
                renderedCopiedImage.setFitHeight(mainStage.getScene().getHeight() - mainStage.getScene().getHeight() / 3);
                imageButton.setMaxWidth(mainStage.getScene().getWidth() / 1.5);
                imageButton.setPrefWidth(mainStage.getScene().getWidth() / 1.5);
                imageButton.setMaxHeight(mainStage.getScene().getHeight() - mainStage.getScene().getHeight() / 3);
                imageButton.setPrefHeight(mainStage.getScene().getHeight() - mainStage.getScene().getHeight() / 3);
            });
            return imageButton;
        };
        Function<List<File>, Button> copiedFilesBlueprint = copiedFiles -> {
            ImageView fileImage = new ImageView(fileIcon);
            fileImage.setPreserveRatio(true);
            Label fileAmount = new Label(String.format("Copied File(s)/Dir(s): %s\nClick Button to Copy and Paste to Folder.", copiedFiles.size()));
            fileAmount.setFont(Font.font(Font.loadFont(fontPath, 13).getFamily(), FontWeight.BOLD, 13));
            fileAmount.setWrapText(true);
            fileAmount.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
            themeDetector.registerListener(isDark -> {
                fileAmount.setTextFill(isDark ? Color.WHITE : Color.BLACK);
            });
            VBox fileInfo = new VBox(fileImage, fileAmount);
            fileInfo.setAlignment(Pos.CENTER);
            Button textButton = new Button();
            textButton.setGraphic(fileInfo);
            textButton.setWrapText(true);
            textButton.setFont(Font.font(Font.loadFont(fontPath, 13).getFamily(), FontWeight.BOLD, 13));
            textButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
            textButton.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
            textButton.setPadding(new Insets(10));
            textButton.setAlignment(Pos.CENTER);
            Platform.runLater(() -> {
                fileImage.setFitWidth((mainStage.getScene().getWidth() / 2) * 0.9);
                fileImage.setFitHeight((mainStage.getScene().getWidth() / 6) * 0.9);
                fileInfo.setMaxSize((mainStage.getScene().getWidth() / 2) * 0.9, (mainStage.getScene().getWidth() / 3) * 0.9);
                fileInfo.setPrefSize((mainStage.getScene().getWidth() / 2) * 0.9, (mainStage.getScene().getWidth() / 3) * 0.9);
                textButton.setMaxWidth(mainStage.getScene().getWidth() / 2);
                textButton.setPrefWidth(mainStage.getScene().getWidth() / 2);
                textButton.setPrefHeight(mainStage.getScene().getHeight() / 3);
            });
            textButton.setOnMouseEntered(entered -> {
                if (clipboardTab) {
                    textButton.setCursor(Cursor.HAND);
                    textButton.setBackground(createPlainBackground(Color.rgb(130, 177, 255), new CornerRadii(50)));
                }
            });
            textButton.setOnMouseExited(entered -> {
                textButton.setCursor(Cursor.DEFAULT);
                textButton.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
            });
            textButton.setOnAction(pressed -> {
                if (systemClipboard.getFiles() != null && clipboardTab) {
                    if (!systemClipboard.getFiles().equals(copiedFiles)) {
                        Iterator<File> copiedFileIterator = copiedFiles.iterator();
                        while (copiedFileIterator.hasNext()) {
                            File file = copiedFileIterator.next();
                            if (!file.exists()) {
                                copiedFiles.remove(copiedFiles.indexOf(file));
                                fileAmount.setText(String.format("Copied File(s)/Dir(s): %s\nClick Button to Copy and Paste to Folder.", copiedFiles.size()));
                            }
                        }
                        if (copiedFiles.size() > 0) {
                            addToSystemClipboard.putFiles(copiedFiles);
                            systemClipboard.setContent(addToSystemClipboard);
                        } else {
                            if (copiedVBox.getChildren().contains(textButton)) {
                                copiedVBox.getChildren().remove(copiedVBox.getChildren().indexOf(textButton));
                            }
                        }
                    }
                } else if (systemClipboard.getFiles() == null && clipboardTab) {
                    if (!systemClipboard.getFiles().equals(copiedFiles)) {
                        Iterator<File> copiedFileIterator = copiedFiles.iterator();
                        while (copiedFileIterator.hasNext()) {
                            File file = copiedFileIterator.next();
                            if (!file.exists()) {
                                copiedFiles.remove(copiedFiles.indexOf(file));
                                fileAmount.setText(String.format("Copied File(s)/Dir(s): %s\nClick Button to Copy and Paste to Folder.", copiedFiles.size()));
                            }
                        }
                        if (copiedFiles.size() > 0) {
                            addToSystemClipboard.putFiles(copiedFiles);
                            systemClipboard.setContent(addToSystemClipboard);
                        } else {
                            if (copiedVBox.getChildren().contains(textButton)) {
                                copiedVBox.getChildren().remove(copiedVBox.getChildren().indexOf(textButton));
                            }
                        }
                    }
                }
            });
            KeyFrame checkFiles = new KeyFrame(Duration.millis(100), forEachFile -> {
                Iterator<File> copiedFileIterator = copiedFiles.iterator();
                while (copiedFileIterator.hasNext()) {
                    File file = copiedFileIterator.next();
                    if (!file.exists()) {
                        copiedFiles.remove(copiedFiles.indexOf(file));
                        fileAmount.setText(String.format("Copied File(s)/Dir(s): %s\nClick Button to Copy and Paste to Folder.", copiedFiles.size()));
                    }
                }
                if (copiedFiles.size() <= 0) {
                    if (copiedVBox.getChildren().contains(textButton)) {
                        copiedVBox.getChildren().remove(copiedVBox.getChildren().indexOf(textButton));
                    }
                }
            });
            Timeline checkFilesinList = new Timeline(checkFiles);
            checkFilesinList.setCycleCount(Animation.INDEFINITE);
            checkFilesinList.play();
            return textButton;
        };
        Group mainRectBackground = getRectBackground(mainStage);
        VBox mainVBoxDecorations = (VBox) mainRectBackground.getChildren().get(0);
        HBox mainHBoxDecorations = (HBox) mainRectBackground.getChildren().get(1);
        Group settingRectBackground = getRectBackground(mainStage);
        VBox settingVBoxDecorations = (VBox) settingRectBackground.getChildren().get(0);
        HBox settingHBoxDecorations = (HBox) settingRectBackground.getChildren().get(1);
        ImageView settingImage = new ImageView(settingIcon);
        settingImage.setPreserveRatio(true);
        ImageView clipboardImage = new ImageView(icon);
        clipboardImage.setPreserveRatio(true);
        Button clipboardButton = new Button();
        clipboardButton.setBackground(createPlainBackground(Color.TRANSPARENT));
        clipboardButton.setGraphic(clipboardImage);
        Button settingButton = new Button();
        settingButton.setBackground(createPlainBackground(Color.TRANSPARENT));
        settingButton.setGraphic(settingImage);
        StackPane leftPane = new StackPane(clipboardButton);
        leftPane.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(clipboardButton, Pos.CENTER);
        StackPane rightPane = new StackPane(settingButton);
        rightPane.setAlignment(Pos.TOP_RIGHT);
        StackPane.setAlignment(settingButton, Pos.CENTER);
        Rectangle rectButton = new Rectangle(mainStage.getWidth() / 2, mainStage.getHeight() / 10);
        rectButton.setFill(themeDetector.isDark() ? Color.PURPLE : Color.CYAN);
        TranslateTransition rectTransition = new TranslateTransition(Duration.millis(300), rectButton);
        HBox buttonBar = new HBox(
            leftPane,
            rightPane
        );
        buttonBar.setAlignment(Pos.TOP_CENTER);
        ScrollPane clipboardScroll = new ScrollPane(
            copiedVBox
        );
        copiedVBox.setBackground(createPlainBackground(Color.TRANSPARENT));
        copiedVBox.setAlignment(Pos.TOP_LEFT);
        clipboardScroll.setBackground(createPlainBackground(Color.TRANSPARENT));
        clipboardScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        clipboardScroll.setPannable(false);
        Label emptyVBox = new Label("Nothing Copied Yet!");
        emptyVBox.setFont(Font.loadFont(fontPath, 15));
        emptyVBox.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
        StackPane clipboardScreen = new StackPane(
            mainRectBackground,
            clipboardScroll
        );
        StackPane.setAlignment(emptyVBox, Pos.CENTER);
        StackPane.setAlignment(clipboardScroll, Pos.TOP_LEFT);
        clipboardScreen.setBackground(createPlainBackground(themeDetector.isDark() ? Color.rgb(55, 55, 55) : Color.WHITE));
        GridPane settingGrid = new GridPane();
        settingGrid.setMaxHeight(mainStage.getHeight());
        settingGrid.setAlignment(Pos.TOP_LEFT);
        settingGrid.setGridLinesVisible(false);
        Platform.runLater(() -> {
            settingGrid.getColumnConstraints().addAll(new ColumnConstraints(mainStage.getWidth() / 2), new ColumnConstraints(mainStage.getWidth() / 2));
        });
        for (int r = 0; r < 7; r++) {
            RowConstraints rowConstraint = new RowConstraints();
            rowConstraint.setPercentHeight(12.8571428571);
            settingGrid.getRowConstraints().add(rowConstraint);
        }
        Label setClipboardLabel = new Label("Clipboard Listening:");
        setClipboardLabel.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 15) / 2));
        setClipboardLabel.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
        GridPane.setHalignment(setClipboardLabel, HPos.CENTER);
        CheckBox clipboardBox = new CheckBox();
        clipboardBox.setSelected((boolean) userPreferences.get("clipboard_enabled"));
        clipboardBox.setOnMouseEntered(hovered -> {
            if (!clipboardTab) {
                clipboardBox.setCursor(Cursor.HAND);
            }
        });
        clipboardBox.setOnMouseExited(notHovered -> {
            clipboardBox.setCursor(Cursor.DEFAULT);
        });
        KeyFrame setClipboardBG = new KeyFrame(Duration.millis(100), clipboardBG -> {
            Platform.runLater(() -> {
                Region clipboardBoxBG = (Region) clipboardBox.lookup(".box");
                clipboardBoxBG.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(3)));
                clipboardBoxBG.setScaleX(2);
                clipboardBoxBG.setScaleY(2);
                Region clipboardBoxMark = (Region) clipboardBox.lookup(".mark");
                clipboardBoxMark.setBackground(clipboardBox.isSelected() ? (themeDetector.isDark() ? createPlainBackground(Color.WHITE) : createPlainBackground(Color.BLACK)) : createPlainBackground(Color.TRANSPARENT));
            });
        });
        Timeline startClipboardSet = new Timeline(setClipboardBG);
        startClipboardSet.setCycleCount(Animation.INDEFINITE);
        GridPane.setHalignment(clipboardBox, HPos.CENTER);
        Label setCopyTextLabel = new Label("Copy Text:");
        setCopyTextLabel.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 15) / 2));
        setCopyTextLabel.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
        CheckBox copyTextBox = new CheckBox();
        copyTextBox.setSelected((boolean) userPreferences.get("copy_text"));
        copyTextBox.setOnMouseEntered(hovered -> {
            if (!clipboardTab) {
                copyTextBox.setCursor(Cursor.HAND);
            }
        });
        copyTextBox.setOnMouseExited(notHovered -> {
            copyTextBox.setCursor(Cursor.DEFAULT);
        });
        KeyFrame setCopyTextBG = new KeyFrame(Duration.millis(100), copyText -> {
            Platform.runLater(() -> {
                Region copyTextBG = (Region) copyTextBox.lookup(".box");
                copyTextBG.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(3)));
                copyTextBG.setScaleX(2);
                copyTextBG.setScaleY(2);
                Region copyTextMark = (Region) copyTextBox.lookup(".mark");
                copyTextMark.setBackground(copyTextBox.isSelected() ? (themeDetector.isDark() ? createPlainBackground(Color.WHITE) : createPlainBackground(Color.BLACK)) : createPlainBackground(Color.TRANSPARENT));
            });
        });
        Timeline startCopyTextSet = new Timeline(setCopyTextBG);
        startCopyTextSet.setCycleCount(Animation.INDEFINITE);
        GridPane.setHalignment(copyTextBox, HPos.CENTER);
        GridPane.setHalignment(setCopyTextLabel, HPos.CENTER);
        Label setCopyImagesLabel = new Label("Copy Images:");
        setCopyImagesLabel.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 15) / 2));
        setCopyImagesLabel.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
        CheckBox copyImagesBox = new CheckBox();
        copyImagesBox.setSelected((boolean) userPreferences.get("copy_images"));
        copyImagesBox.setOnMouseEntered(hovered -> {
            if (!clipboardTab) {
                copyImagesBox.setCursor(Cursor.HAND);
            }
        });
        copyImagesBox.setOnMouseExited(notHovered -> {
            copyImagesBox.setCursor(Cursor.DEFAULT);
        });
        KeyFrame setCopyImagesBG = new KeyFrame(Duration.millis(100), copyImages -> {
            Platform.runLater(() -> {
                Region copyImagesBG = (Region) copyImagesBox.lookup(".box");
                copyImagesBG.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(3)));
                copyImagesBG.setScaleX(2);
                copyImagesBG.setScaleY(2);
                Region copyImagesMark = (Region) copyImagesBox.lookup(".mark");
                copyImagesMark.setBackground(copyImagesBox.isSelected() ? (themeDetector.isDark() ? createPlainBackground(Color.WHITE) : createPlainBackground(Color.BLACK)) : createPlainBackground(Color.TRANSPARENT));
            });
        });
        Timeline startCopyImagesSet = new Timeline(setCopyImagesBG);
        startCopyImagesSet.setCycleCount(Animation.INDEFINITE);
        GridPane.setHalignment(copyImagesBox, HPos.CENTER);
        GridPane.setHalignment(setCopyImagesLabel, HPos.CENTER);
        Label setCopyFilesLabel = new Label("Copy Files:");
        setCopyFilesLabel.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 15) / 2));
        setCopyFilesLabel.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
        CheckBox copyFilesBox = new CheckBox();
        copyFilesBox.setSelected((boolean) userPreferences.get("copy_files"));
        copyFilesBox.setOnMouseEntered(hovered -> {
            if (!clipboardTab) {
                copyFilesBox.setCursor(Cursor.HAND);
            }
        });
        copyFilesBox.setOnMouseExited(notHovered -> {
            copyFilesBox.setCursor(Cursor.DEFAULT);
        });
        KeyFrame setCopyFilesBG = new KeyFrame(Duration.millis(100), copyFiles -> {
            Platform.runLater(() -> {
                Region copyFilesBG = (Region) copyFilesBox.lookup(".box");
                copyFilesBG.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(3)));
                copyFilesBG.setScaleX(2);
                copyFilesBG.setScaleY(2);
                Region copyFilesMark = (Region) copyFilesBox.lookup(".mark");
                copyFilesMark.setBackground(copyFilesBox.isSelected() ? (themeDetector.isDark() ? createPlainBackground(Color.WHITE) : createPlainBackground(Color.BLACK)) : createPlainBackground(Color.TRANSPARENT));
            });
        });
        Timeline startCopyFilesSet = new Timeline(setCopyFilesBG);
        startCopyFilesSet.setCycleCount(Animation.INDEFINITE);
        GridPane.setHalignment(copyFilesBox, HPos.CENTER);
        GridPane.setHalignment(setCopyFilesLabel, HPos.CENTER);
        Label setNotificationsLabel = new Label("Notifications:");
        setNotificationsLabel.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 15) / 2));
        setNotificationsLabel.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
        GridPane.setHalignment(setNotificationsLabel, HPos.CENTER);
        CheckBox notificationsBox = new CheckBox();
        notificationsBox.setSelected((boolean) userPreferences.get("notifications_on"));
        notificationsBox.setOnMouseEntered(hovered -> {
            if (!clipboardTab) {
                notificationsBox.setCursor(Cursor.HAND);
            }
        });
        notificationsBox.setOnMouseExited(notHovered -> {
            notificationsBox.setCursor(Cursor.DEFAULT);
        });
        notificationsBox.setOnAction(pressed -> {
            // Prompts user to manually toggle the notification permission
            try {
                if (notificationsBox.isSelected()) {
                    int osVersion = Integer.parseInt(System.getProperty("os.version").split("\\.")[0]);
                    if (osVersion >= 13) {
                        ProcessBuilder modernOpenSystem = new ProcessBuilder("open", "x-apple.systempreferences:com.apple.Notifications-Settings.extension");
                        modernOpenSystem.start();
                    } else if (osVersion == 12) {
                        ProcessBuilder montereyOpenSystem = new ProcessBuilder("open", "x-apple.systempreferences:com.apple.preference.notifications?Notifications");
                        montereyOpenSystem.start();
                    } else {
                        ProcessBuilder legacyOperatingSystem = new ProcessBuilder("open", "x-apple.systempreferences:com.apple.preference.notifications");
                        legacyOperatingSystem.start();               
                    }
                }
            } catch (IOException ioError) {}
        });
        KeyFrame setNotificationsBG = new KeyFrame(Duration.millis(100), copyNotifications -> {
            Platform.runLater(() -> {
                Region copyNotificationsBG = (Region) notificationsBox.lookup(".box");
                copyNotificationsBG.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(3)));
                copyNotificationsBG.setScaleX(2);
                copyNotificationsBG.setScaleY(2);
                Region copyNotificationsMark = (Region) notificationsBox.lookup(".mark");
                copyNotificationsMark.setBackground(notificationsBox.isSelected() ? (themeDetector.isDark() ? createPlainBackground(Color.WHITE) : createPlainBackground(Color.BLACK)) : createPlainBackground(Color.TRANSPARENT));
            });
        });
        Timeline startNotificationsSet = new Timeline(setNotificationsBG);
        startNotificationsSet.setCycleCount(Animation.INDEFINITE);
        GridPane.setHalignment(notificationsBox, HPos.CENTER);
        Label setMaxItemsLabel = new Label("Max Items:");
        setMaxItemsLabel.setFont(Font.loadFont(fontPath, (double) ((double) mainStage.getHeight() / 15) / 2));
        setMaxItemsLabel.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
        GridPane.setHalignment(setMaxItemsLabel, HPos.CENTER);
        TextField maxItemsField = new TextField(Integer.toString(((Number) userPreferences.get("max_items")).intValue()));
        maxItemsField.setMaxWidth(mainStage.getWidth() / 4);
        maxItemsField.setFont(Font.loadFont(fontPath, 15));
        maxItemsField.setAlignment(Pos.CENTER);
        maxItemsField.setTextFormatter(new TextFormatter<>(input -> {
            try {
                @SuppressWarnings("unused") // Made textInt variable unused to test if the text is int
                int textInt = Integer.parseInt(input.getControlNewText());
                if (input.getControlNewText().trim().length() <= 3) {
                    if (!input.getControlNewText().trim().startsWith("0")) {
                        return input;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (NumberFormatException notInt) {
                if (input.getControlNewText().equals("")) {
                    return input;
                } else {
                    return null;
                }
            }
        }));
        Button clearClipboard = new Button("Clear Clipboard");
        clearClipboard.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
        clearClipboard.setFont(Font.loadFont(fontPath, 15));
        clearClipboard.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
        clearClipboard.setOnMouseEntered(hovered -> {
            if (!clipboardTab) {
                clearClipboard.setBackground(createPlainBackground(Color.rgb(80, 127, 205), new CornerRadii(50)));
                clearClipboard.setCursor(Cursor.HAND);
            }
        });
        clearClipboard.setOnMouseExited(notHovered -> {
            clearClipboard.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
            clearClipboard.setCursor(Cursor.DEFAULT);
        });
        GridPane.setHalignment(clearClipboard, HPos.CENTER);
        Button resetToDefaults = new Button("Reset Settings");
        resetToDefaults.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
        resetToDefaults.setFont(Font.loadFont(fontPath, 15));
        resetToDefaults.setTextFill(themeDetector.isDark() ? Color.WHITE : Color.BLACK);
        resetToDefaults.setOnMouseEntered(hovered -> {
            if (!clipboardTab) {
                resetToDefaults.setBackground(createPlainBackground(Color.rgb(80, 127, 205), new CornerRadii(50)));
                resetToDefaults.setCursor(Cursor.HAND);
            }
        });
        resetToDefaults.setOnMouseExited(notHovered -> {
            resetToDefaults.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN, new CornerRadii(50)));
            resetToDefaults.setCursor(Cursor.DEFAULT);
        });
        resetToDefaults.setOnAction(pressed -> {
            clipboardBox.setSelected(true);
            copyTextBox.setSelected(true);
            copyImagesBox.setSelected(true);
            copyFilesBox.setSelected(true);
            notificationsBox.setSelected(false);
            maxItemsField.setText("15");
        });
        GridPane.setHalignment(resetToDefaults, HPos.CENTER);
        KeyFrame updatePreferences = new KeyFrame(Duration.millis(100), preferences -> {
            Platform.runLater(() -> {
                userPreferences.put("clipboard_enabled", clipboardBox.isSelected());
                userPreferences.put("copy_text", copyTextBox.isSelected());
                userPreferences.put("copy_images", copyImagesBox.isSelected());
                userPreferences.put("copy_files", copyFilesBox.isSelected());
                userPreferences.put("notifications_on", notificationsBox.isSelected());
                if (maxItemsField.getText().length() > 0 && clipboardTab) {
                    userPreferences.put("max_items", Integer.parseInt(maxItemsField.getText()));
                }
                String configJson = gson.toJson(userPreferences);
                try (FileWriter configWriter = new FileWriter(configFile)) {
                    configWriter.write(configJson);
                } catch (IOException fileError) {
                    initializeAppData();
                }
            });
        });
        Timeline trackUserPreferences = new Timeline(updatePreferences);
        trackUserPreferences.setCycleCount(Animation.INDEFINITE);
        trackUserPreferences.play();
        GridPane.setHalignment(maxItemsField, HPos.CENTER);
        settingGrid.add(setClipboardLabel, 0, 0);
        settingGrid.add(clipboardBox, 1, 0);
        settingGrid.add(setCopyTextLabel, 0, 1);
        settingGrid.add(copyTextBox, 1, 1);
        settingGrid.add(setCopyImagesLabel, 0, 2);
        settingGrid.add(copyImagesBox, 1, 2);
        settingGrid.add(setCopyFilesLabel, 0, 3);
        settingGrid.add(copyFilesBox, 1, 3);
        settingGrid.add(setNotificationsLabel, 0, 4);
        settingGrid.add(notificationsBox, 1, 4);
        settingGrid.add(setMaxItemsLabel, 0, 5);
        settingGrid.add(maxItemsField, 1, 5);
        settingGrid.add(clearClipboard, 0, 6);
        settingGrid.add(resetToDefaults, 1, 6);
        Platform.runLater(() -> {
            startClipboardSet.play();
            startCopyTextSet.play();
            startCopyImagesSet.play();
            startCopyFilesSet.play();
            startNotificationsSet.play();
        });
        StackPane settingScreen = new StackPane(
            settingRectBackground,
            settingGrid
        );
        settingScreen.setAlignment(Pos.TOP_LEFT);
        settingScreen.setBackground(createPlainBackground(themeDetector.isDark() ? Color.rgb(55, 55, 55) : Color.WHITE));
        Button exitButton = new Button("x");
        exitButton.setBackground(createPlainBackground(Color.RED));
        exitButton.setTextFill(Color.WHITE);
        exitButton.setFont(Font.font(exitButton.getFont().getFamily(), 15));
        exitButton.setOnMouseEntered(hovered -> {
            exitButton.setBackground(createPlainBackground(Color.rgb(255, 50, 50)));
            exitButton.setCursor(Cursor.HAND);
        });
        exitButton.setOnMouseExited(hovered -> {
            exitButton.setBackground(createPlainBackground(Color.RED));
            exitButton.setCursor(Cursor.DEFAULT);
        });
        exitButton.setOnAction(pressed -> {
            mainStage.hide();
        });
        StackPane sceneModeBar = new StackPane(
            settingScreen,
            clipboardScreen,
            rectButton, 
            buttonBar,
            emptyVBox,
            exitButton
        );
        StackPane.setAlignment(exitButton, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(clipboardScreen, Pos.TOP_LEFT);
        StackPane.setAlignment(settingScreen, Pos.TOP_LEFT);
        StackPane.setAlignment(rectButton, Pos.TOP_LEFT);
        StackPane.setAlignment(buttonBar, Pos.TOP_CENTER);
        Consumer<Boolean> isClipboardTab = mainTab -> {
            Platform.runLater(() -> {
                if (mainTab) {
                    settingScreen.setVisible(false);
                    settingScreen.setManaged(false);
                    clipboardScreen.setVisible(true);
                    clipboardScreen.setManaged(true);
                } else {
                    settingScreen.setVisible(true);
                    settingScreen.setManaged(true);
                    clipboardScreen.setVisible(false);
                    clipboardScreen.setManaged(false);
                }
            });
        };
        Scene mainAppScene = new Scene(sceneModeBar);
        Set<String> stringList = ConcurrentHashMap.newKeySet();
        Set<Image> imageList = ConcurrentHashMap.newKeySet();
        Set<List<File>> filesList = ConcurrentHashMap.newKeySet();
        clearClipboard.setOnAction(pressed -> {
            ImageView warningImage = new ImageView(warningIcon);
            warningImage.setFitWidth(48);
            warningImage.setFitHeight(48);
            Alert emptyFieldAlert = new Alert(AlertType.CONFIRMATION);
            emptyFieldAlert.setTitle("Clear Confirmation");
            emptyFieldAlert.setHeaderText("Are You Sure?");
            emptyFieldAlert.setContentText("Legacy Warning Says:\n\"This action cannot be reversed.\"");
            emptyFieldAlert.getDialogPane().setGraphic(warningImage);
            Stage alertStage = (Stage) emptyFieldAlert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(warningIcon);
            Optional<ButtonType> confirmationResult = emptyFieldAlert.showAndWait();
            if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                addToSystemClipboard.putString(null);
                addToSystemClipboard.putImage(null);
                addToSystemClipboard.putFiles(null);
                addToSystemClipboard.putHtml(null);
                addToSystemClipboard.putRtf(null);
                addToSystemClipboard.putUrl(null);
                systemClipboard.setContent(addToSystemClipboard);
                copiedVBox.getChildren().clear();
                copiedListSet.clear();
                stringList.clear();
                imageList.clear();
                filesList.clear();
            }
        });
        Iterator<Serializable> copiedIterator = copiedListSet.descendingIterator();
        while (copiedIterator.hasNext()) {
            Serializable copiedObject = copiedIterator.next();
            if (copiedVBox.getChildren().size() < ((Number) userPreferences.get("max_items")).intValue()) {
                if (copiedObject instanceof String) {
                    String copiedListString = (String) copiedObject;
                    Button copiedListText = copiedItemBlueprint.apply(copiedListString);
                    copiedVBox.getChildren().add(0, copiedListText);
                    VBox.setMargin(copiedListText, new Insets(0, 0, 0, (mainStage.getWidth() - (mainStage.getWidth() / 1.5)) / 2));
                    stringList.add(copiedListString);
                } else if (copiedObject instanceof byte[]) {
                    byte[] copiedByteImage = (byte[]) copiedObject;
                    try (ByteArrayInputStream bytesToImage = new ByteArrayInputStream(copiedByteImage)) {
                        BufferedImage loadedImage = ImageIO.read(bytesToImage);
                        Image copiedListImage = SwingFXUtils.toFXImage(loadedImage, null);
                        Button imageListCopied = copiedImageBlueprint.apply(copiedListImage);
                        copiedVBox.getChildren().add(0, imageListCopied);
                        VBox.setMargin(imageListCopied, new Insets(0, 0, 0, (mainStage.getWidth() - (mainStage.getWidth() / 1.5)) / 2));
                        imageList.add(copiedListImage);
                    } catch (IOException ioError) {}
                } else {
                    @SuppressWarnings("unchecked")
                    List<File> copiedListFiles = (List<File>) copiedObject;
                    Button filesListCopied = copiedFilesBlueprint.apply(copiedListFiles);
                    copiedVBox.getChildren().add(0, filesListCopied);
                    VBox.setMargin(filesListCopied, new Insets(0, 0, 0, (mainStage.getWidth() - (mainStage.getWidth() / 2)) / 2));
                    filesList.add(copiedListFiles);
                }
            } else {
                break;
            }
        }
        KeyFrame getStringContents = new KeyFrame(Duration.millis(100), copiedStr -> {
            Platform.runLater(() -> {
                if (systemClipboard.hasString() && (boolean) userPreferences.get("copy_text") && (boolean) userPreferences.get("clipboard_enabled")) {
                    if (stringList.isEmpty() || !stringList.contains(systemClipboard.getString())) {
                        Button copiedText = copiedItemBlueprint.apply(systemClipboard.getString());
                        copiedVBox.getChildren().add(0, copiedText);
                        VBox.setMargin(copiedText, new Insets(0, 0, 0, (mainStage.getWidth() - (mainStage.getWidth() / 1.5)) / 2));
                        stringList.add(systemClipboard.getString());
                        copiedListSet.addFirst(systemClipboard.getString());
                        if ((boolean) userPreferences.get("notifications_on")) {
                            legacyTrayIcon.displayMessage(
                            "The Legacy Clipboard",
                            "Text has been copied!",
                            MessageType.INFO
                            );
                        }
                    }
                } else if (!(boolean) userPreferences.get("copy_text") && systemClipboard.hasString() || !(boolean) userPreferences.get("clipboard_enabled") && systemClipboard.hasString()) {
                    stringList.add(systemClipboard.getString());
                }
            });
        });
        KeyFrame getImageClipboard = new KeyFrame(Duration.millis(100), copiedImage -> {
            Platform.runLater(() -> {
                if (systemClipboard.hasImage() && (boolean) userPreferences.get("copy_images") && (boolean) userPreferences.get("clipboard_enabled")) {
                    if (!imageList.isEmpty()) {
                        boolean alreadyCopied = false;
                        for (Image image: imageList) {
                            if (imageEquals(image, systemClipboard.getImage())) {
                                alreadyCopied = true;
                                break;
                            }
                        }
                        if (!alreadyCopied) {
                            Button copiedClipboardImage = copiedImageBlueprint.apply(systemClipboard.getImage());
                            copiedVBox.getChildren().add(0, copiedClipboardImage);
                            VBox.setMargin(copiedClipboardImage, new Insets(0, 0, 0, (mainStage.getWidth() - (mainStage.getWidth() / 1.5)) / 2));
                            imageList.add(systemClipboard.getImage());
                            try (ByteArrayOutputStream imageToBytes = new ByteArrayOutputStream()) {
                                BufferedImage bufferedByteImage = SwingFXUtils.fromFXImage(systemClipboard.getImage(), null);
                                ImageIO.write(bufferedByteImage, "png", imageToBytes);
                                byte[] imageBytes = imageToBytes.toByteArray();
                                copiedListSet.addFirst(imageBytes);
                            } catch (IOException ioError) {}
                            if ((boolean) userPreferences.get("notifications_on")) {
                                legacyTrayIcon.displayMessage(
                                "The Legacy Clipboard",
                                "An image has been copied!",
                                    MessageType.INFO
                                );
                            }
                        }
                    } else {
                        Button copiedClipboardImage = copiedImageBlueprint.apply(systemClipboard.getImage());
                        copiedVBox.getChildren().add(0, copiedClipboardImage);
                        VBox.setMargin(copiedClipboardImage, new Insets(0, 0, 0, (mainStage.getWidth() - (mainStage.getWidth() / 1.5)) / 2));
                        imageList.add(systemClipboard.getImage());
                        try (ByteArrayOutputStream imageToBytes = new ByteArrayOutputStream()) {
                            BufferedImage bufferedByteImage = SwingFXUtils.fromFXImage(systemClipboard.getImage(), null);
                            ImageIO.write(bufferedByteImage, "png", imageToBytes);
                            byte[] imageBytes = imageToBytes.toByteArray();
                            copiedListSet.addFirst(imageBytes);
                        } catch (IOException ioError) {}
                        if ((boolean) userPreferences.get("notifications_on")) {
                            legacyTrayIcon.displayMessage(
                        "The Legacy Clipboard",
                        "An image has been copied!",
                            MessageType.INFO
                            );
                        }
                    }
                } else if (!(boolean) userPreferences.get("copy_images") && systemClipboard.hasImage() || !(boolean) userPreferences.get("clipboard_enabled") && systemClipboard.hasImage()) {
                    imageList.add(systemClipboard.getImage());
                }
            });
        });
        KeyFrame getFilesClipboard = new KeyFrame(Duration.millis(100), copiedFiles -> {
            Platform.runLater(() -> {
                if (systemClipboard.hasFiles() && (boolean) userPreferences.get("copy_files") && (boolean) userPreferences.get("clipboard_enabled")) {
                    if (filesList.isEmpty() || !filesList.contains(systemClipboard.getFiles())) {
                        Button copiedClipboardFiles = copiedFilesBlueprint.apply(systemClipboard.getFiles());
                        copiedVBox.getChildren().add(0, copiedClipboardFiles);
                        VBox.setMargin(copiedClipboardFiles, new Insets(0, 0, 0, (mainStage.getWidth() - (mainStage.getWidth() / 2)) / 2));
                        filesList.add(systemClipboard.getFiles());
                        copiedListSet.addFirst(new ArrayList<File>(systemClipboard.getFiles()));
                        if ((boolean) userPreferences.get("notifications_on")) {
                            legacyTrayIcon.displayMessage(
                            "Legacy Clipboard",
                                String.format("%d File(s)/Dir(s) have been copied!", systemClipboard.getFiles().size()),
                                MessageType.INFO
                            );
                        }
                    }
                } else if (!(boolean) userPreferences.get("copy_files") && systemClipboard.hasFiles() || !(boolean) userPreferences.get("clipboard_enabled") && systemClipboard.hasFiles()) {
                    filesList.add(systemClipboard.getFiles());
                }
            });
        });
        KeyFrame clearVBoxClutter = new KeyFrame(Duration.millis(100), copiedClipboardVBox -> {
            Platform.runLater(() -> {
                if (copiedVBox.getChildren().isEmpty() && clipboardTab) {
                    emptyVBox.setVisible(true);
                } else {
                    emptyVBox.setVisible(false);
                }
                while (copiedVBox.getChildren().size() > ((Number) userPreferences.get("max_items")).intValue() && clipboardTab) {
                    copiedVBox.getChildren().remove(copiedVBox.getChildren().size() - 1);
                }

                while (copiedListSet.size() > ((Number) userPreferences.get("max_items")).intValue() && clipboardTab) {
                    copiedListSet.removeLast();
                }

                try (ObjectOutputStream copiedStream = new ObjectOutputStream(new FileOutputStream(logFile))) {
                    copiedStream.writeObject(copiedListSet);
                } catch (IOException logError) {
                    initializeAppData();
                }
            });
        });
        Timeline manageVBoxSize = new Timeline(clearVBoxClutter);
        manageVBoxSize.setCycleCount(Animation.INDEFINITE);
        manageVBoxSize.play();
        Timeline checkClipboardContents = new Timeline(getStringContents);
        Timeline checkClipboardImages = new Timeline(getImageClipboard);
        Timeline checkClipboardFiles = new Timeline(getFilesClipboard);
        checkClipboardContents.setCycleCount(Animation.INDEFINITE);
        checkClipboardContents.play();
        checkClipboardImages.setCycleCount(Animation.INDEFINITE);
        checkClipboardImages.play();
        checkClipboardFiles.setCycleCount(Animation.INDEFINITE);
        checkClipboardFiles.play();
        themeDetector.registerListener(isDark -> {
            Platform.runLater(() -> {
                if (isDark) {
                    setClipboardLabel.setTextFill(Color.WHITE);
                    setCopyTextLabel.setTextFill(Color.WHITE);
                    setCopyImagesLabel.setTextFill(Color.WHITE);
                    setCopyFilesLabel.setTextFill(Color.WHITE);
                    setNotificationsLabel.setTextFill(Color.WHITE);
                    setMaxItemsLabel.setTextFill(Color.WHITE);
                    clearClipboard.setBackground(createPlainBackground(Color.PURPLE, new CornerRadii(50)));
                    clearClipboard.setTextFill(Color.WHITE);
                    resetToDefaults.setBackground(createPlainBackground(Color.PURPLE, new CornerRadii(50)));
                    resetToDefaults.setTextFill(Color.WHITE);
                    emptyVBox.setTextFill(Color.WHITE);
                    clipboardScreen.setBackground(createPlainBackground(Color.rgb(55, 55, 55)));
                    settingScreen.setBackground(createPlainBackground(Color.rgb(55, 55, 55)));
                    rectButton.setFill(Color.PURPLE);
                    ScrollBar clipboardScrollBar = (ScrollBar) clipboardScroll.lookup(".scroll-bar:vertical");
                    clipboardScrollBar.setBackground(createPlainBackground(Color.PURPLE));
                    for (Node copiedNode: copiedVBox.getChildren()) {
                        Button copiedButton = (Button) copiedNode;
                        copiedButton.setTextFill(Color.WHITE);
                        copiedButton.setBackground(createPlainBackground(Color.PURPLE, new CornerRadii(50)));
                    }
                    for (Node rect: mainVBoxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(66, 66, 66));
                    }
                    for (Node rect: mainHBoxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(66, 66, 66));
                    }
                    for (Node rect: settingVBoxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(66, 66, 66));
                    }
                    for (Node rect: settingHBoxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(66, 66, 66));
                    }
                } else {
                    setClipboardLabel.setTextFill(Color.BLACK);
                    setCopyTextLabel.setTextFill(Color.BLACK);
                    setCopyImagesLabel.setTextFill(Color.BLACK);
                    setCopyFilesLabel.setTextFill(Color.BLACK);
                    setNotificationsLabel.setTextFill(Color.BLACK);
                    setMaxItemsLabel.setTextFill(Color.BLACK);
                    clearClipboard.setBackground(createPlainBackground(Color.CYAN, new CornerRadii(50)));
                    clearClipboard.setTextFill(Color.BLACK);
                    resetToDefaults.setBackground(createPlainBackground(Color.CYAN, new CornerRadii(50)));
                    resetToDefaults.setTextFill(Color.BLACK);
                    emptyVBox.setTextFill(Color.BLACK);
                    clipboardScreen.setBackground(createPlainBackground(Color.WHITE));
                    settingScreen.setBackground(createPlainBackground(Color.WHITE));
                    rectButton.setFill(Color.CYAN);
                    ScrollBar clipboardScrollBar = (ScrollBar) clipboardScroll.lookup(".scroll-bar:vertical");
                    clipboardScrollBar.setBackground(createPlainBackground(Color.CYAN));
                    for (Node copiedNode: copiedVBox.getChildren()) {
                        Button copiedButton = (Button) copiedNode;
                        copiedButton.setTextFill(Color.BLACK);
                        copiedButton.setBackground(createPlainBackground(Color.CYAN, new CornerRadii(50)));
                    }
                    for (Node rect: mainVBoxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(227, 227, 227));
                    }
                    for (Node rect: mainHBoxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(227, 227, 227));
                    }
                    for (Node rect: settingVBoxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(227, 227, 227));
                    }
                    for (Node rect: settingHBoxDecorations.getChildren()) {
                        Rectangle rectInstance = (Rectangle) rect;
                        rectInstance.setFill(Color.rgb(227, 227, 227));
                    }
                }
            });
        });
        Platform.runLater(() -> {
            clipboardScroll.applyCss();
            clipboardScroll.layout();
            StackPane clipboardBackground = (StackPane) clipboardScroll.lookup(".viewport");
            clipboardBackground.setStyle("-fx-background-color: transparent;");
            ScrollBar clipboardScrollBar = (ScrollBar) clipboardScroll.lookup(".scroll-bar:vertical");
            clipboardScrollBar.setBackground(createPlainBackground(themeDetector.isDark() ? Color.PURPLE : Color.CYAN));
            clipboardScrollBar.setMaxHeight(mainStage.getScene().getHeight() - mainStage.getScene().getHeight() / 10);
            clipboardScrollBar.setPrefHeight(mainStage.getScene().getHeight() - mainStage.getScene().getHeight() / 10);
        });
        clipboardButton.setOnAction(pressed -> {
            if (!clipboardTab && !animationPlaying && maxItemsField.getText().trim().length() > 0) {
                animationPlaying = true;
                try {
                    rectTransition.setFromX(mainStage.getScene().getWidth() / 2);
                } catch (NullPointerException nullError) {
                    rectTransition.setFromX(mainStage.getWidth() / 2);
                }
                rectTransition.setToX(0);
                rectTransition.setOnFinished(finished -> {
                    clipboardTab = true;
                    animationPlaying = false;
                    isClipboardTab.accept(clipboardTab);
                });
                rectTransition.play();
            } else if (maxItemsField.getText().trim().length() <= 0) {
                ImageView warningImage = new ImageView(warningIcon);
                warningImage.setFitWidth(48);
                warningImage.setFitHeight(48);
                Alert emptyFieldAlert = new Alert(AlertType.WARNING);
                emptyFieldAlert.setTitle("Configuration Error");
                emptyFieldAlert.setHeaderText("Please set the \"Max Items\" value.");
                emptyFieldAlert.setContentText("Legacy Warning Says:\n\"Settings are supposed to be \'set\''.\"");
                emptyFieldAlert.getDialogPane().setGraphic(warningImage);
                Stage alertStage = (Stage) emptyFieldAlert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(warningIcon);
                emptyFieldAlert.showAndWait();
            }
        });
        clipboardButton.setOnMouseEntered(hovered -> {
            if (!animationPlaying && !clipboardTab) {
                clipboardButton.setCursor(Cursor.HAND);
            }
        });
        clipboardButton.setOnMouseExited(notHovered -> {
            clipboardButton.setCursor(Cursor.DEFAULT);
        });
        settingButton.setOnAction(pressed -> {
            if (clipboardTab && !animationPlaying) {
                animationPlaying = true;
                rectTransition.setFromX(0);
                try {
                    rectTransition.setToX(mainStage.getScene().getWidth() / 2);
                } catch (NullPointerException nullError) {
                    rectTransition.setToX(mainStage.getWidth() / 2);
                }
                rectTransition.setOnFinished(finished -> {
                    clipboardTab = false;
                    animationPlaying = false;
                    isClipboardTab.accept(clipboardTab);
                });
                rectTransition.play();
            }
        });
        settingButton.setOnMouseEntered(hovered -> {
            if (!animationPlaying && clipboardTab) {
                settingButton.setCursor(Cursor.HAND);
            }
        });
        settingButton.setOnMouseExited(notHovered -> {
            settingButton.setCursor(Cursor.DEFAULT);
        });
        isClipboardTab.accept(clipboardTab);
        Platform.runLater(() -> {
            settingImage.setFitWidth(mainStage.getScene().getWidth() / 2);
            settingImage.setFitHeight(mainStage.getScene().getHeight() / 12);
            clipboardImage.setFitWidth(mainStage.getScene().getWidth() / 2);
            clipboardImage.setFitHeight(mainStage.getScene().getHeight() / 12);
            clipboardButton.setMaxWidth(mainStage.getScene().getWidth() / 2);
            clipboardButton.setMaxHeight(mainStage.getScene().getHeight() / 12);
            settingButton.setMaxWidth(mainStage.getScene().getWidth() / 2);
            settingButton.setMaxHeight(mainStage.getScene().getHeight() / 12);
            leftPane.setPrefSize(mainStage.getScene().getWidth(), mainStage.getScene().getHeight() / 12);
            leftPane.setMaxSize(mainStage.getScene().getWidth(), mainStage.getScene().getHeight() / 12);
            rightPane.setPrefSize(mainStage.getScene().getWidth(), mainStage.getScene().getHeight() / 12);
            rightPane.setMaxSize(mainStage.getScene().getWidth(), mainStage.getScene().getHeight() / 12);
            buttonBar.setMaxWidth(mainStage.getScene().getWidth());
            buttonBar.setPrefWidth(mainStage.getScene().getWidth());
            buttonBar.setMaxHeight(mainStage.getScene().getHeight() / 10);
            buttonBar.setPrefHeight(mainStage.getScene().getHeight() / 10);
            copiedVBox.setSpacing(mainStage.getScene().getWidth() / 20);
            clipboardScroll.setMaxWidth(mainStage.getScene().getWidth());
            clipboardScroll.setPrefHeight(mainStage.getScene().getHeight() - mainStage.getScene().getHeight() / 10);
            clipboardScroll.setMaxHeight(mainStage.getScene().getHeight() - mainStage.getScene().getHeight() / 10);
            clipboardScroll.setTranslateY(mainStage.getScene().getHeight() / 10);
            settingGrid.setPadding(new Insets(mainStage.getScene().getHeight() / 10, 0, 0, 0));
            rectButton.setWidth(mainStage.getScene().getWidth() / 2);
            rectButton.setHeight(mainStage.getScene().getHeight() / 10);
            exitButton.setPrefWidth(mainStage.getScene().getWidth() / 10);
            exitButton.setPrefHeight(mainStage.getScene().getHeight() / 10);
        });
        return mainAppScene;
    }

    @Override
    public void start(Stage mainStage) {
        // Checking if there's already an instance of the app running
        try (RandomAccessFile openPIDFile = new RandomAccessFile(openFile, "rw")) {
            try {
                long currentPID = openPIDFile.readLong();
                if (ProcessHandle.of(currentPID).isPresent() && ProcessHandle.of(currentPID).get().isAlive() && ProcessHandle.current().info().command().get().toLowerCase().contains("legacy clipboard")) {
                    /* Sending signal to an already active instance of the app to open the window.
                     * Because if someone is trying to open up the exe when it is already open, they likely do not 
                     * see the app already running.
                    */
                    try (RandomAccessFile windowOutputStream = new RandomAccessFile(windowStatusFile, "rw")) {
                        windowOutputStream.setLength(0);
                        windowOutputStream.seek(0);
                        windowOutputStream.writeByte(1);
                        windowOutputStream.getChannel().force(true);
                    } finally {
                        System.exit(0);
                    }
                } else {
                    openPIDFile.seek(0);
                    openPIDFile.writeLong(ProcessHandle.current().pid());
                }
            } catch (EOFException blankError) {
                openPIDFile.seek(0);
                openPIDFile.writeLong(ProcessHandle.current().pid());
            }
        } catch (IOException ioError) {}
        // Continuously checks signal to see when to open the window
        KeyFrame checkWindow = new KeyFrame(Duration.millis(100), checkStatus -> {
            updateScreenDimensions();
            if (systemInsets.top > 0) {
                mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                mainStage.setY(systemInsets.top);
            } else if (systemInsets.bottom > 0) {
                mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight());
            } else if (systemInsets.left > 0) {
                mainStage.setX(systemInsets.left);
                mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight());
            } else {
                mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight());
            }
            try (RandomAccessFile windowStatusStream = new RandomAccessFile(windowStatusFile, "rw")) {
                windowStatusStream.seek(0);
                int windowStatusFileResult = windowStatusStream.read();
                if (windowStatusFileResult == 1) {
                    windowStatusStream.seek(0);
                    windowStatusStream.setLength(0);
                    mainStage.show();
                    windowStatusStream.writeByte(2);
                    windowStatusStream.getChannel().force(true);
                }
            } catch (IOException ioError) {
                initializeAppData();
            }
        });
        Timeline checkWindowStatus = new Timeline(checkWindow);
        checkWindowStatus.setCycleCount(Animation.INDEFINITE);
        checkWindowStatus.play();
        Platform.setImplicitExit(false);
        mainStage.setOnCloseRequest(close -> {
            mainStage.hide();
        });
        mainStage.setTitle("The Legacy Clipboard");
        mainStage.setResizable(false);
        mainStage.setWidth(primaryScreen.getVisualBounds().getWidth() / 3);
        mainStage.setHeight(primaryScreen.getVisualBounds().getHeight() - (primaryScreen.getVisualBounds().getHeight() / 3));
        mainStage.initStyle(StageStyle.UNDECORATED);
        if (existingUser) {
            mainStage.setScene(startMainApp(mainStage));
        } else {
            mainStage.setScene(configureStartupScene(mainStage));
        }
        mainStage.getIcons().add(icon);
        mainStage.show();
        mainStage.setAlwaysOnTop(true);
        if (Taskbar.isTaskbarSupported() && currentOS() == OS.MACOS) {
            Taskbar systemTaskBar = Taskbar.getTaskbar();
            systemTaskBar.setIconImage(systemToolkit.getImage(getClass().getResource("/images/legacyboard.png")));
        }
        if (SystemTray.isSupported()) {
            try {
                SystemTray systemTray = SystemTray.getSystemTray();
                java.awt.Image systemLegacyImage = ImageIO.read(getClass().getResource("/images/legacyboard.png"));
                legacyTrayIcon = new TrayIcon(systemLegacyImage, "The Legacy Clipboard");
                legacyTrayIcon.setImageAutoSize(true);
                if (systemInsets.top > 0) {
                    mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                    mainStage.setY(systemInsets.top);
                } else if (systemInsets.bottom > 0) {
                    mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                    mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight());
                } else if (systemInsets.left > 0) {
                    mainStage.setX(systemInsets.left);
                    mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight());
                } else {
                    mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                    mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight());
                }
                legacyTrayIcon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) { 
                            Platform.runLater(() -> {
                                if (systemInsets.top > 0) {
                                    if (e.getX() > primaryScreen.getBounds().getWidth() / 2) {
                                        mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                                        mainStage.setY(systemInsets.top);
                                    } else {
                                        mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                                        mainStage.setY(systemInsets.top); 
                                    }
                                } else if (systemInsets.bottom > 0) {
                                    if (e.getX() > primaryScreen.getBounds().getWidth() / 2) {
                                        mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                                        mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight());
                                    } else {
                                        mainStage.setX(systemInsets.left);
                                        mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight()); 
                                    }
                                } else if (systemInsets.left > 0) {
                                    if (e.getY() > primaryScreen.getBounds().getHeight() / 2) {
                                        mainStage.setX(systemInsets.left);
                                        mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight());
                                    } else {
                                        mainStage.setX(systemInsets.left);
                                        mainStage.setY(systemInsets.top);
                                    }
                                } else {
                                    if (e.getY() > primaryScreen.getBounds().getHeight() / 2) {
                                        mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                                        mainStage.setY(primaryScreen.getBounds().getHeight() - systemInsets.bottom - mainStage.getHeight());
                                    } else {
                                        mainStage.setX(primaryScreen.getBounds().getWidth() - systemInsets.right - mainStage.getWidth());
                                        mainStage.setY(systemInsets.top);
                                    }
                                } 
                                if (mainStage.isShowing()) {
                                    mainStage.hide();
                                } else {
                                    mainStage.show();
                                }
                            });
                        }
                    }
                });
                systemTray.add(legacyTrayIcon);
            } catch (AWTException awtError) {
                mainStage.initStyle(StageStyle.DECORATED);
            } catch (IOException ioError) {}
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}