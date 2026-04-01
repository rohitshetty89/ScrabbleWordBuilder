package scrabble;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScrabbleWordBuilderTest {

    private ScrabbleWordBuilder builder;

    // Minimal word list covering all test scenarios, pre-sorted alphabetically.
    // Using a small fixed list keeps tests fast and independent of dictionary.txt.
    private static final List<String> DICT = List.of(
        "AA", "DOOR", "DRAW", "RADIO", "WARD", "WIZARD", "WOO", "WOOD", "WORD", "ZOO"
    );

    // Standard Scrabble letter data loaded from the bundled classpath resource.
    private static LetterData letterData;

    @BeforeAll
    static void loadLetterData() throws Exception {
        letterData = LetterData.load();
    }

    @BeforeEach
    void setUp() {
        builder = new ScrabbleWordBuilder(letterData, DICT);
    }

    // --- spec examples ---

    @Test
    void example1_rackAndBoardWord_returnsWizard() {
        var result = builder.findBestWord("AIDOORW", "WIZ");
        assertTrue(result.isValid());
        assertEquals("WIZARD", result.getBestWord());
        assertEquals(19, result.getScore());
    }

    @Test
    void example2_rackOnly_returnsDrawAlphabetically() {
        // DRAW, WARD, WOOD, WORD all score 8; DRAW is alphabetically first
        var result = builder.findBestWord("AIDOORW", "");
        assertTrue(result.isValid());
        assertEquals("DRAW", result.getBestWord());
        assertEquals(8, result.getScore());
    }

    @Test
    void example3_zTileExceeded_returnsInvalid() {
        var result = builder.findBestWord("AIDOORZ", "QUIZ");
        assertFalse(result.isValid());
        String expected = "Letter 'Z' appears 2 time(s) across the rack and board word,"
            + " but only 1 tile(s) exist in the game.";
        assertEquals(expected, result.getError());
    }

    @Test
    void example4_rackTooLong_returnsInvalid() {
        var result = builder.findBestWord("AIDOORWZ", "");
        assertFalse(result.isValid());
        assertEquals("The rack contains 8 letters, exceeding the maximum allowed of 7", result.getError());
    }

    // --- validation ---

    @Test
    void emptyRack_returnsInvalid() {
        assertFalse(builder.findBestWord("", "").isValid());
    }

    @Test
    void nullRack_returnsInvalid() {
        assertFalse(builder.findBestWord(null, "").isValid());
    }

    @Test
    void rackWithNonLetters_returnsInvalid() {
        var result = builder.findBestWord("A1B", "");
        assertFalse(result.isValid());
        assertEquals("Rack must contain only letters (A-Z).", result.getError());
    }

    @Test
    void boardWordWithNonLetters_returnsInvalid() {
        var result = builder.findBestWord("ABC", "A1B");
        assertFalse(result.isValid());
        assertEquals("Board word must contain only letters (A-Z).", result.getError());
    }

    // --- search ---

    @Test
    void noValidWordFound_returnsNullBestWord() {
        // BXJK: valid tile counts but no word in DICT is formable from these letters
        var result = builder.findBestWord("BXJK", "");
        assertTrue(result.isValid());
        assertNull(result.getBestWord());
    }

    @Test
    void tieBrokenAlphabetically() {
        // DRAW and WARD both score 8 from this rack; DRAW comes first alphabetically
        var result = builder.findBestWord("DRAWARD", "");
        assertTrue(result.isValid());
        assertEquals("DRAW", result.getBestWord());
    }

    @Test
    void boardWordLettersExpandAvailablePool() {
        // Rack alone cannot form WIZARD (missing Z); the board word WIZ supplies it
        assertNotEquals("WIZARD", builder.findBestWord("AIDOORW", "").getBestWord());
        var withBoard = builder.findBestWord("AIDOORW", "WIZ");
        assertEquals("WIZARD", withBoard.getBestWord());
    }

    @Test
    void singleLetterRack_noWordFormable() {
        var result = builder.findBestWord("A", "");
        assertTrue(result.isValid());
        assertNull(result.getBestWord());
    }

    @Test
    void lowercaseRack_returnsInvalid() {
        // ScrabbleWordBuilder requires uppercase; Main.java handles normalization for CLI users
        assertFalse(builder.findBestWord("aidoorw", "").isValid());
    }

    @Test
    void lowercaseBoardWord_returnsInvalid() {
        assertFalse(builder.findBestWord("AIDOORW", "wiz").isValid());
    }

    @Test
    void wordLongerThanPool_notReturned() {
        // Pool has 4 letters total (DRAW); WIZARD (6 letters) cannot be formed
        var result = builder.findBestWord("DRA", "W");
        assertTrue(result.isValid());
        assertNotEquals("WIZARD", result.getBestWord());
    }

    // --- Dictionary loading ---

    @Test
    void dictionaryRead_deduplicatesAndSorts() throws Exception {
        String raw = "word\nWORD\ndraw\nDRAW\nward\n";
        var words = Dictionary.read(new ByteArrayInputStream(raw.getBytes()));
        assertEquals(List.of("DRAW", "WARD", "WORD"), words);
    }
}
