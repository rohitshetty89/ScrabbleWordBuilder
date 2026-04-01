package scrabble;

import java.io.IOException;
import java.util.List;

/**
 * CLI entry point for the Scrabble Word Builder.
 *
 * Usage:
 *   java -jar scrabble-word-builder-1.0.0.jar --rack <letters> [--word <letters>]
 *
 * Flags:
 *   --rack, -r   Letters on the player's rack (1-7, required)
 *   --word, -w   Word already on the board (optional)
 *   --help, -h   Show usage information
 */
public class Main {

    private static final String USAGE =
        "Usage: java -jar scrabble-word-builder-1.0.0.jar --rack <letters> [--word <letters>]\n" +
        "\n" +
        "  --rack, -r   Letters on the player's rack (1-7 letters, required)\n" +
        "  --word, -w   Word already on the board that may be built upon (optional)\n" +
        "  --help, -h   Show this help message\n" +
        "\n" +
        "Examples:\n" +
        "  java -jar scrabble-word-builder-1.0.0.jar --rack AIDOORW --word WIZ\n" +
        "  java -jar scrabble-word-builder-1.0.0.jar -r AIDOORW\n" +
        "  java -jar scrabble-word-builder-1.0.0.jar AIDOORW WIZ";

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.err.println(USAGE);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error loading resources: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        String rack      = null;
        String boardWord = "";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--rack":
                case "-r":
                    if (++i < args.length) {
                        rack = args[i].toUpperCase().trim();
                    } else {
                        throw new IllegalArgumentException("--rack requires a value.");
                    }
                    break;

                case "--word":
                case "-w":
                    if (++i < args.length) {
                        boardWord = args[i].toUpperCase().trim();
                    } else {
                        throw new IllegalArgumentException("--word requires a value.");
                    }
                    break;

                case "--help":
                case "-h":
                    System.out.println(USAGE);
                    return;

                default:
                    if (args[i].startsWith("-")) {
                        throw new IllegalArgumentException("Unknown option: " + args[i]);
                    }
                    // Positional: first non-flag arg = rack, second = board word
                    if (rack == null) {
                        rack = args[i].toUpperCase().trim();
                    } else if (boardWord.isEmpty()) {
                        boardWord = args[i].toUpperCase().trim();
                    }
                    break;
            }
        }

        if (rack == null) {
            throw new IllegalArgumentException("--rack is required.");
        }

        LetterData   letterData = LetterData.load();
        List<String> dictionary = Dictionary.load();

        ScrabbleWordBuilder builder = new ScrabbleWordBuilder(letterData, dictionary);
        ScrabbleWordBuilder.Result result = builder.findBestWord(rack, boardWord);

        if (!result.isValid()) {
            System.out.println("Invalid input: " + result.getError());
            System.exit(1);
        } else if (result.getBestWord() == null) {
            System.out.println("No valid word found.");
        } else {
            System.out.println(result.getBestWord() + " (" + result.getScore() + " points)");
        }
    }
}
