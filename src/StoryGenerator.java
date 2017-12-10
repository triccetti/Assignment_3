import java.io.File;
import java.util.*;

/**
 * The StoryGenerator uses a Trigram language model to
 * generate stories. You need to load files for the StoryGenerator
 * to read and then train the StoryGenerator. After training is complete
 * you can generate a story with the number of words you want.
 *
 * TCSS 435 - AI
 * Programming assignment 3
 *
 * @Author Taylor Riccetti
 */
public class StoryGenerator {

    /**
     * Console user input for the StoryGenerator.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        StoryGenerator storyGen = new StoryGenerator();
        Scanner input = new Scanner(System.in);
        System.out.println("============= WELCOME TO THE STORY GENERATOR ==============");
        System.out.print("Please enter files you would like to train the generator on (Seperate them by ','): ");
        String[] listOfFiles = input.nextLine().split(",");
        ArrayList<File> files = new ArrayList<>();
        for (String fileName : listOfFiles) {
            files.add(new File(fileName));
        }
        System.out.println("We are traing the files. Please be aware this may take a few minuets.");
        storyGen.loadFiles(files);
        storyGen.train();
        System.out.print("Training has completed. To generate a story we need the number of word you would like to have: ");
        int count = input.nextInt();
        System.out.println("You have generated a " + count + " word story!");
        System.out.println("=============================================================================");
        System.out.println(storyGen.generateStory(count));
    }

    /**
     * The array list of words in order.
     */
    private ArrayList<String> words;

    /**
     * The list of trigrams.
     */
    private HashMap<String, ArrayList<String>> allTrigrams;

    private boolean filesLoaded;
    private boolean trained;
    private boolean training;

    /**
     * A Story generator object that handles generating stories.
     */
    public StoryGenerator() {
        filesLoaded = false;
        trained = false;
        training = false;
    }

    /**
     * Takes in a list of files and creates an in-order list of words.
     *
     * @param files the files to read in.
     */
    public void loadFiles(ArrayList<File> files) {
        words = new ArrayList<>();
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

    /**
     * Trains the program on the list of words and creates a trigram
     * map of bigrams and the 3rd word that follows them.
     */
    public void train() {
        if (filesLoaded) {
            allTrigrams = new HashMap<>();
            training = true;
            for (int i = 0; i < words.size() - 2; i++) {
                String bigram = words.get(i) + " " + words.get(i + 1);
                String w3 = words.get(i + 2);
                if (allTrigrams.containsKey(bigram)) {
                    allTrigrams.get(bigram).add(w3);
                } else {
                    // Adds words to an arraylist to account for the frequency of the word.
                    ArrayList<String> list = new ArrayList<>();
                    list.add(w3);
                    allTrigrams.put(bigram, list);
                }
            }
            trained = true;
            training = false;
            filesLoaded = false;
        }
    }

    /**
     * Takes in the number of words you want in your story
     * and generates a story based on the text you trained on.
     *
     * @param numWords the number of words in your story.
     * @return a story.
     */
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
                ArrayList<String> list = allTrigrams.get(value);
                w1 = w2;
                w2 = list.get((int) (Math.random() * list.size()));
            }
        } else {
            story.append("StoryGenerator is not trained. Please train some text files.");
        }
        return story.toString().trim();
    }

    /**
     * Returns true if the StoryGenerator is currently
     * training or not.
     *
     * @return if the StoryGenerator is training.
     */
    public boolean isTraining() {
        return training;
    }

    /**
     * Returns true if the StoryGenerator is trained or not.
     *
     * @return if the story generator is trained.
     */
    public boolean isTrainingComplete() {
        return trained;
    }

}