package scrabble;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads and provides per-letter score and tile-count data from letter_data.csv.
 * The CSV must have the header "letter,score,tiles" followed by one row per letter.
 */
public class LetterData {

    private static final String FILE_NAME = "letter_data.csv";
    private static final String FILE_PATH = "data/" + FILE_NAME;

    private final int[] scores     = new int[26];
    private final int[] tileCounts = new int[26];

    private LetterData() {}

    /**
     * Loads letter data from data/letter_data.csv relative to the working directory.
     */
    public static LetterData load() throws IOException {
        
        Path p = Paths.get(FILE_PATH);
        if (!Files.exists(p)) {
            throw new IOException(FILE_PATH + " not found on classpath or in the current directory.");
        }
        InputStream is = Files.newInputStream(p);
        
        return parse(is);
    }

    private static LetterData parse(InputStream is) throws IOException {
        LetterData data = new LetterData();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            // Fix 4: verify the header line is actually present
            String header = br.readLine();
            if (header == null) {
                throw new IOException(FILE_PATH + " is empty; expected a header row followed by letter data.");
            }

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }

                String letterStr = parts[0].trim().toUpperCase();

                // Fix 3: skip rows whose first column is not a single A-Z letter
                if (letterStr.length() != 1 || letterStr.charAt(0) < 'A' || letterStr.charAt(0) > 'Z') {
                    continue;
                }

                int idx = letterStr.charAt(0) - 'A';
                data.scores[idx]     = Integer.parseInt(parts[1].trim());
                data.tileCounts[idx] = Integer.parseInt(parts[2].trim());
            }
        }
        return data;
    }

    /** Returns the Scrabble score for the given letter (A–Z, case-insensitive). */
    public int getScore(char c) {
        return scores[Character.toUpperCase(c) - 'A'];
    }

    /** Returns the number of tiles available in the game for the given letter. */
    public int getTileCount(char c) {
        return tileCounts[Character.toUpperCase(c) - 'A'];
    }
}
