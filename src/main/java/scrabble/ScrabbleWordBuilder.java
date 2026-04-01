package scrabble;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Core logic for finding the highest-scoring valid Scrabble word that can be
 * formed from the player's rack plus an optional word already on the board.
 */
public class ScrabbleWordBuilder {

    private static final Pattern ALPHA         = Pattern.compile("[A-Z]+");
    private static final int     MAX_RACK_SIZE = 7;
    private static final int     MIN_WORD_LEN  = 2;
    private static final int     MAX_WORD_LEN  = 15;

    private final LetterData   letterData;
    private final List<String> dictionary; // must be sorted alphabetically (ascending)

    public ScrabbleWordBuilder(LetterData letterData, List<String> dictionary) {
        this.letterData = letterData;
        this.dictionary = dictionary;
    }

    /**
     * Validates the input and finds the best word.
     *
     * @param rack      1-7 uppercase letters from the player's rack (required).
     * @param boardWord Uppercase letters of a word already on the board (may be empty).
     * @return a Result that is either invalid (with an error message) or
     *         valid (with the best word and its score, or null if none found).
     */
    public Result findBestWord(String rack, String boardWord) {
        
        Optional<String> error = validateRackAndBoard(rack, boardWord);
        
        if (error.isPresent()) {
            return Result.invalid(error.get());
        }
        int[] pool = buildPool(rack, boardWord);
        error = validatePool(pool, boardWord);
        if (error.isPresent()) {
            return Result.invalid(error.get());
        }

        // The dictionary is pre-sorted alphabetically, so iterating in order and
        // keeping the first word that achieves a new high score naturally breaks
        // ties in favour of the alphabetically earlier word.
        String bestWord  = null;
        int    bestScore = -1;

        for (String word : dictionary) {
            int len = word.length();
            if (len < MIN_WORD_LEN || len > MAX_WORD_LEN) {
                continue;
            }
            if (!canForm(word, pool)) {
                continue;
            }

            int score = calculateScore(word);
            if (score > bestScore) {
                bestScore = score;
                bestWord  = word;
            }
        }

        return Result.valid(bestWord, bestScore < 0 ? 0 : bestScore);
    }

    /**
     * Validates rack format, board word format, and letter tile limits.
     *
     * @return an error message if the input is invalid, or empty if valid.
     */
    private Optional<String> validateRackAndBoard(String rack, String boardWord) {
        if (rack == null || rack.isEmpty()) {
            return Optional.of("Rack must not be empty.");
        }
        if (!ALPHA.matcher(rack).matches()) {
            return Optional.of("Rack must contain only letters (A-Z).");
        }
        if (rack.length() > MAX_RACK_SIZE) {
            return Optional.of(String.format(
                "The rack contains %d letters, exceeding the maximum allowed of %d", rack.length(), MAX_RACK_SIZE));
        }
        if (!boardWord.isEmpty() && !ALPHA.matcher(boardWord).matches()) {
            return Optional.of("Board word must contain only letters (A-Z).");
        }

        return Optional.empty();
    }

    private Optional<String> validatePool(int[] pool, String boardWord) {
        for (int i = 0; i < 26; i++) {
            if (pool[i] == 0) {
                continue;
            }
            char letter    = (char) ('A' + i);
            int  tileLimit = letterData.getTileCount(letter);
            if (pool[i] > tileLimit) {
                return Optional.of(
                    "Letter '" + letter + "' appears " + pool[i] + " time(s) across the rack" +
                    (boardWord.isEmpty() ? "" : " and board word") +
                    ", but only " + tileLimit + " tile(s) exist in the game.");
            }
        }
        return Optional.empty();
    }

    /** Builds the combined letter-frequency pool from the rack and board word. */
    private int[] buildPool(String rack, String boardWord) {
        int[] pool = new int[26];
        for (char c : rack.toCharArray()) {
            pool[c - 'A']++;
        }
        for (char c : boardWord.toCharArray()) {
            pool[c - 'A']++;
        }
        return pool;
    }

    /** Returns true if word can be spelled using only the letters in pool. */
    private boolean canForm(String word, int[] pool) {
        int[] needed = new int[26];
        for (char c : word.toCharArray()) {
            int idx = c - 'A';
            if (idx < 0 || idx >= 26) {
                return false;
            }
            if (++needed[idx] > pool[idx]) {
                return false;
            }
        }
        return true;
    }

    /** Sums the Scrabble score for every letter in word. */
    private int calculateScore(String word) {
        int score = 0;
        for (char c : word.toCharArray()) {
            score += letterData.getScore(c);
        }
        return score;
    }

    // -------------------------------------------------------------------------
    // Result type
    // -------------------------------------------------------------------------

    public static final class Result {
        private final boolean valid;
        private final String  error;
        private final String  bestWord;
        private final int     score;

        private Result(boolean valid, String error, String bestWord, int score) {
            this.valid    = valid;
            this.error    = error;
            this.bestWord = bestWord;
            this.score    = score;
        }

        public static Result invalid(String error) {
            return new Result(false, error, null, 0);
        }

        public static Result valid(String bestWord, int score) {
            return new Result(true, null, bestWord, score);
        }

        public boolean isValid() {
            return valid;
        }

        public String getError() {
            return error;
        }

        public String getBestWord() {
            return bestWord;
        }

        public int getScore() {
            return score;
        }
    }
}
