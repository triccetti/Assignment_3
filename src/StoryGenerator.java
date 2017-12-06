import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Stream;

public class StoryGenerator {

    public StoryGenerator() {
        // empty
    }

    public void train(ArrayList<File> files) {
        for(File f : files) {
            //read file into stream, try-with-resources
            try (Stream<String> stream = Files.lines(f.toPath())) {
                stream.forEach(System.out::println);
            }  catch (Exception error) {
                // error
            }
        }
    }

    public String generateStory(int numWords) {

        return "<Insert story here>";
    }

}
