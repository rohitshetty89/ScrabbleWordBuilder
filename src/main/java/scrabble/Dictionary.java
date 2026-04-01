package scrabble;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads the word list used for finding valid Scrabble words.
 * Words are upper-cased, deduplicated, and sorted alphabetically on load.
 */
public class Dictionary {

    private static final String FILE_NAME = "dictionary.txt";
    private static final String FILE_PATH = "data/" + FILE_NAME;

    private Dictionary() {}

    /**
     * Loads the dictionary from data/dictionary.txt relative to the working directory.
     */
    public static List<String> load() throws IOException {
        
        Path p = Paths.get(FILE_PATH);
        if (!Files.exists(p)) {
                throw new IOException(FILE_PATH + " not found on classpath or in the current directory.");
        }
        InputStream is = Files.newInputStream(p);
        
        return read(is);
    }

    /** Package-private to allow direct use in tests without touching the filesystem. */
    static List<String> read(InputStream is) throws IOException {
        Set<String> wordSet = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                String word = line.trim().toUpperCase();
                if (!word.isEmpty()) {
                    wordSet.add(word);
                }
            }
        }
        List<String> words = new ArrayList<>(wordSet);
        Collections.sort(words);
        return words;
    }
}
