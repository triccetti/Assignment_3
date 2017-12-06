import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Stream;


public class MainGUI extends Application {

    /**
     * The scenes height.
     */
    private static final int HEIGHT = 600;

    /**
     * The scenes width.
     */
    private static final int WIDTH = 800;

    private final int ROW_HEIGHT = 24;

    /**
     * The main stage for the gui.
     */
    private Stage stage;

    private final ObservableList data = FXCollections.observableArrayList();

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
        ListView listView = new ListView(data);

        Button addTrainingText = new Button("Upload Text");
        listView.setPrefHeight(1);

        addTrainingText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Text t = getTrainingFile();
                if (t != null) {
                    data.add(t);
                    listView.prefHeightProperty().bind(Bindings.size(data).multiply(ROW_HEIGHT));
                }
            }
        });

        Button trainText = new Button("Train");
        trainText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                trainText.setText("Training...");
                storyGen.train(filesToTrain);
                trainText.setText("Train");
            }
        });

        TextArea wordsInStory = new TextArea("1000");
        wordsInStory.setPrefWidth(ROW_HEIGHT * 2);
        wordsInStory.setPrefHeight(ROW_HEIGHT / 2);

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


        TextArea storyResults = new TextArea();
        storyResults.setEditable(false);
        storyResults.setWrapText(true);
        ScrollPane sp = new ScrollPane();
        sp.setContent(storyResults);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Button generateStory = new Button("Generate a Story");
        generateStory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String story = storyGen.generateStory(Integer.parseInt(wordsInStory.getText()));
                storyResults.setText(story);
            }
        });
        generateStory.setAlignment(Pos.CENTER);


        Button saveStory = new Button("Save Story");
        saveStory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String story = storyResults.getText();

            }
        });
        saveStory.setAlignment(Pos.CENTER);

        GridPane trainButtons = new GridPane();
        trainButtons.setPadding(new Insets(ROW_HEIGHT));
        trainButtons.setAlignment(Pos.CENTER);
        trainButtons.setHgap(10);

        trainButtons.add(addTrainingText, 0, 0);
        trainButtons.add(trainText, 1,0);

        GridPane genButtons = new GridPane();
        genButtons.setPadding(new Insets(ROW_HEIGHT));
        genButtons.setHgap(10);
        genButtons.add(new Label("Number of words in the story: "), 0, 0);
        genButtons.add(wordsInStory, 1, 0);
        genButtons.add(generateStory, 2, 0);
        startPane.getChildren().addAll(listView, new Text("Click file to delete."), trainButtons, genButtons,
                storyResults, saveStory);


        return startPane;
    }

    public Text getTrainingFile() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(workingDirectory);
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            filesToTrain.add(selectedFile);

            Text fileToTrainWith = new Text(selectedFile.getName());
            fileToTrainWith.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    // If the file is clicked, remove it
                    data.remove(fileToTrainWith);
                    filesToTrain.remove(selectedFile);
                }
            });

            return fileToTrainWith;
        }
        return null;
    }
}