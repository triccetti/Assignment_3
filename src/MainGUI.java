import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.jnlp.FileSaveService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;


public class MainGUI extends Application {

    /**
     * The scenes height.
     */
    private static final int HEIGHT = 800;

    /**
     * The scenes width.
     */
    private static final int WIDTH = 600;

    private final int ROW_HEIGHT = 24;

    /**
     * The main stage for the gui.
     */
    private Stage stage;

    private final ObservableList data = FXCollections.observableArrayList();

    private ListView listView;

    private ArrayList<File> filesToTrain;

    private StoryGenerator storyGen;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        filesToTrain = new ArrayList<>();
        storyGen = new StoryGenerator();
        stage = primaryStage;
        stage.setTitle("Story Generator!");
        stage.setScene(new Scene(setup(), WIDTH, HEIGHT));
        stage.show();
    }

    /**
     * Sets up the GUI for the Story generator.
     *
     * @return the gridPane
     */
    private Pane setup() {
        FlowPane startPane = new FlowPane();
        startPane.setPadding(new Insets(ROW_HEIGHT));
        startPane.setOrientation(Orientation.VERTICAL);
        startPane.setAlignment(Pos.TOP_CENTER);
        listView = new ListView(data);

        Button addTrainingText = new Button("Upload Text");
        listView.setPrefHeight(1);

        Button trainText = new Button("Train");

        addTrainingText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                List<Text> t = getTrainingFile();
                trainText.setDisable(false);
                if (t != null) {
                    for(Text text : t) {
                        data.add(text);
                    }
                    listView.setPrefHeight(data.size() * ROW_HEIGHT);
                }
            }
        });


        TextArea wordsInStory = new TextArea("100");
        wordsInStory.setPrefWidth(ROW_HEIGHT * 2);
        wordsInStory.setPrefHeight(ROW_HEIGHT - 20);

        // force the field to be numeric only
        wordsInStory.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    wordsInStory.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        Text message = new Text();

        Task<Void> longRunningTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                long startTime = System.currentTimeMillis();
                storyGen.loadFiles(filesToTrain);
                storyGen.train();
                long endTime = System.currentTimeMillis();

                long duration = (endTime - startTime) / 1000L;
                message.setText(message.getText() + " It took " + duration + " seconds to train the Story Generator.");

                Platform.runLater(() -> trainText.setText("Train Complete"));
                return null;
            }
        };


        TextArea storyResults = new TextArea();
        storyResults.setEditable(false);
        storyResults.setWrapText(true);
        storyResults.setPrefHeight(ROW_HEIGHT * 15);
        ScrollPane sp = new ScrollPane();
        sp.setContent(storyResults);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        trainText.setOnAction(e -> {
            if (!storyGen.isTraining()) {
                trainText.setText("Training...");
                Calendar rightNow = Calendar.getInstance();
                String now = rightNow.get(Calendar.HOUR_OF_DAY) + ":" + rightNow.get(Calendar.MINUTE);
                message.setText("Started training at " + now);
                trainText.setDisable(true);
                new Thread(longRunningTask).start();

            }
        });


        Button generateStory = new Button("Generate a Story");
        generateStory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!storyGen.isTrainingComplete()) {
                    storyResults.setText("Training is not complete.");
                } else {
                    String story = storyGen.generateStory(Integer.parseInt(wordsInStory.getText()));
                    storyResults.setText(story);
                }
            }
        });
        generateStory.setAlignment(Pos.CENTER);


        Button saveStory = new Button("Save Story");
        saveStory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String story = storyResults.getText();

                File workingDirectory = new File(System.getProperty("user.dir"));
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(workingDirectory);
                fileChooser.setTitle("Save your story.");
                File fileToSave = fileChooser.showSaveDialog(stage);
                if(fileToSave != null) {
                    try {
                        Files.write(fileToSave.toPath(), story.getBytes());
                    } catch (Exception err) {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Your story could not be saved please try again.");
                        alert.showAndWait();
                    }
                }

            }
        });
        saveStory.setAlignment(Pos.CENTER);

        GridPane trainButtons = new GridPane();
        trainButtons.setPadding(new Insets(ROW_HEIGHT));
        trainButtons.setAlignment(Pos.CENTER);
        trainButtons.setHgap(10);

        trainButtons.add(addTrainingText, 0, 0);
        trainButtons.add(trainText, 1, 0);

        GridPane genButtons = new GridPane();
        genButtons.setPadding(new Insets(ROW_HEIGHT));
        genButtons.setHgap(10);
        genButtons.add(new Label("Number of words in the story: "), 0, 0);
        genButtons.add(wordsInStory, 1, 0);
        genButtons.add(generateStory, 2, 0);
        startPane.getChildren().addAll(listView, new Text("Click file to delete."), trainButtons, message,
                genButtons, storyResults, saveStory);


        return startPane;
    }


    public  List<Text> getTrainingFile() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(workingDirectory);
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {
            List<Text> texts = new ArrayList<>();
            for (File selectedFile : selectedFiles) {
                filesToTrain.add(selectedFile);

                Text fileToTrainWith = new Text(selectedFile.getName());

                fileToTrainWith.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        // If the file is clicked, remove it
                        data.remove(fileToTrainWith);
                        filesToTrain.remove(selectedFile);
                        listView.setPrefHeight(data.size() * ROW_HEIGHT);

                    }
                });

                texts.add(fileToTrainWith);

            }
            return texts;

        }
        return null;
    }
}