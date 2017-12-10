import com.sun.org.apache.xpath.internal.operations.Equals;

import java.io.File;
import java.util.*;

public class StoryGenerator {

    private LinkedList<String> words;

    private HashMap<String, ArrayList<String>> allTriagrams;

    private boolean filesLoaded;
    private boolean trained;
    private boolean training;

    public StoryGenerator() {
        filesLoaded = false;
        trained = false;
        training = false;
    }

    public void loadFiles(ArrayList<File> files) {
        words = new LinkedList<>();
        boolean loaded = true;
        for (File f : files) {
            // Read in the file with words in order.
            Scanner sc;
            try {
                sc = new Scanner(f);
                while (sc.hasNext()) {
                    String line = sc.next();
                    words.add(line.toLowerCase());
                }
            } catch (Exception e) {
                loaded = false;
                System.err.println("The file " + f.getName() + " could not be read.");
            }
        }
        filesLoaded = loaded;
    }

    public void train() {
        if (filesLoaded) {
            allTriagrams = new HashMap<>();
            training = true;
            for (int i = 0; i < words.size() - 2; i++) {

                // triagrams.add(new Triagram(words.get(i), words.get(i + 1), words.get(i + 2)));
                String bigram = words.get(i) + " " + words.get(i + 1);
                String w3 = words.get(i + 2);
                if (allTriagrams.containsKey(bigram)) {
                    // adds the third word to the list of words
                    allTriagrams.get(bigram).add(w3);
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(w3);
                    allTriagrams.put(bigram, list);
                }
            }
            trained = true;
            training = false;
            filesLoaded = false;
        }
    }

    public String generateStory(int numWords) {
        StringBuilder story = new StringBuilder();
        if (trained) {
            int startWordIndex = (int) (Math.random() * words.size() - 1);
            String w1 = words.get(startWordIndex);
            String w2 = words.get(startWordIndex + 1);

            for (int i = 0; i < numWords; i++) {
                story.append(w1);
                story.append(" ");
                String value = w1 + " " + w2;
                ArrayList<String> list = allTriagrams.get(value);
                w1 = w2;
                w2 = list.get((int) (Math.random() * list.size()));
            }
        } else {
            story.append("StoryGenerator is not trained. Please train on some text.");
        }
        return story.toString().trim();
    }

    public boolean isTraining() {
        return training;
    }

    public boolean isTrainingComplete() {
        return trained;
    }

}