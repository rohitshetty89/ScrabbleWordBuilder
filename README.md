# Scrabble Word Builder

A CLI tool that finds the highest-scoring valid Scrabble word formable from a player's rack and an optional word already on the board.

---

## Requirements

- Java 11 or later (`java`, `javac`, `jar` on your `PATH`)

---

## Installing Java

### macOS

**Option A — Homebrew (recommended):**
```bash
# Install Homebrew if you don't have it
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 17 (LTS)
brew install openjdk@17

# Add Java to your PATH (add this line to ~/.zshrc or ~/.bash_profile)
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"

# Reload your shell
source ~/.zshrc

```

### Windows

1. Download the installer from [Adoptium](https://adoptium.net/) (select Java 17 LTS, Windows x64 `.msi`).
2. Run the installer and check **"Set JAVA_HOME variable"** and **"Add to PATH"** when prompted.


### Verify your installation

After installing, confirm `java`, `javac`, and `jar` are all on your `PATH`:

```bash
java -version    # should print "openjdk version 17..." or similar (11+ is fine)
javac -version
jar --version
```

---

## Setup & Installation

```bash
cd ScrabbleWordBuilder
chmod +x build.sh
./build.sh
```

This compiles the sources, bundles the resources, and produces:

```
target/scrabble-word-builder-1.0.0.jar
```

## Usage

```
java -jar target/scrabble-word-builder-1.0.0.jar --rack <letters> [--word <letters>]
```

| Flag | Short | Description |
|------|-------|-------------|
| `--rack` | `-r` | Letters on the player's rack (**required**, 1–7 letters A–Z) |
| `--word` | `-w` | Word already on the board the player may build upon (optional) |
| `--help` | `-h` | Print usage information |

Named flags and positional arguments are both accepted:

```bash
# Named flags
java -jar target/scrabble-word-builder-1.0.0.jar --rack AIDOORW --word WIZ

# Short flags
java -jar target/scrabble-word-builder-1.0.0.jar -r AIDOORW -w WIZ

# Positional (rack first, then optional board word)
java -jar target/scrabble-word-builder-1.0.0.jar AIDOORW WIZ
```

Input is **case-insensitive** (`aidoorw` is treated the same as `AIDOORW`).

---

## Examples

### Example 1 — Rack + board word
```
$ java -jar target/scrabble-word-builder-1.0.0.jar --rack AIDOORW --word WIZ
WIZARD (19 points)
```
WIZARD (W=4, I=1, Z=10, A=1, R=1, D=2) = 19 points.

### Example 2 — Rack only
```
$ java -jar target/scrabble-word-builder-1.0.0.jar --rack AIDOORW
DRAW (8 points)
```
Several words score 8 (DRAW, WARD, WOOD, WORD); DRAW is returned as the alphabetically earliest.

### Example 3 — Invalid: letter tile limit exceeded
```
$ java -jar target/scrabble-word-builder-1.0.0.jar --rack AIDOORZ --word QUIZ
Invalid input: Letter 'Z' appears 2 time(s) across the rack and board word, but only 1 tile(s) exist in the game.
```

### Example 4 — Invalid: rack too long
```
$ java -jar target/scrabble-word-builder-1.0.0.jar --rack AIDOORWZ
Invalid input: The rack contains 8 letters, exceeding the maximum allowed of 7
```

---

## Running Tests

Tests require Maven. If Maven is not installed, install it first:

```bash
# macOS (Homebrew)
brew install maven

# Verify
mvn -version
```

Then run the test suite:

```bash
mvn test
```

Maven will compile the sources, run all JUnit 5 tests in `src/test/java/`, and print a summary. Expected output:

```
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The tests cover:
- All four specification examples
- Each validation error path (empty rack, non-letter characters, rack too long, tile limit exceeded)
- Alphabetical tie-breaking
- Board word expanding the available letter pool
- No-word-found result
- `Dictionary` loading, deduplication, and filtering

---

## Asset Files

### `data/dictionary.txt`

- **Source:** [ENABLE (Enhanced North American Benchmark LExicon)](https://raw.githubusercontent.com/dolph/dictionary/master/enable1.txt) — a free, public-domain English word list widely used in Scrabble-like games and spell-checkers.
- **Contents:** 172,823 valid English words, one per line, in lowercase.
- **How it was obtained:** Downloaded directly from the GitHub mirror of the ENABLE1 word list:
  ```
  curl -s "https://raw.githubusercontent.com/dolph/dictionary/master/enable1.txt" \
    -o ScrabbleWordBuilder/Data/dictionary.txt
  ```
- **At runtime:** Words are trimmed and uppercased when loaded, so the lowercase source file works without modification.
- **Why ENABLE:** It is a well-established, openly licensed word list that closely mirrors the official North American Scrabble tournament word list (TWL), making it appropriate for Scrabble-style scoring applications.

### `data/letter_data.csv`

- **Source:** Standard English Scrabble tile distribution, as defined by the official Hasbro/Mattel Scrabble rules.
- **Contents:** For each letter A–Z, the JSON records:
  - `score` — the point value of one tile (e.g. A=1, Q=10, Z=10)
  - `tileCount` — the number of tiles of that letter in a standard 100-tile English Scrabble set (excluding the 2 blank tiles, which are not supported)
- **How it was constructed:** Manually authored based on the publicly documented standard Scrabble tile set:


---

## Project Structure

```
scrabble/
├── build.sh                               # Build script (no Maven required)
├── pom.xml                                # Maven build descriptor
├── README.md
├── data/
│   ├── dictionary.txt                     # Word list
│   └── letter_data.csv                    # Letter scores and tile counts
├── src/
│   ├── main/
│   │   ├── java/scrabble/
│   │   │   ├── Main.java                  # CLI entry point; argument parsing
│   │   │   ├── Dictionary.java            # Loads dictionary.txt
│   │   │   ├── LetterData.java            # Loads letter_data.csv
│   │   │   └── ScrabbleWordBuilder.java   # Validation and word-finding logic
│   │   └── resources/
│   └── test/java/scrabble/
│       └── ScrabbleWordBuilderTest.java   # JUnit 5 unit tests
└── target/
    └── scrabble-word-builder-1.0.0.jar   # Executable fat JAR (after build)
```

---
## Exit Codes

- `0`: Success (word found or no word found message printed)
- `1`: Invalid input or fatal error


## Assumptions & Design Decisions

### Letter availability
The **combined** count of each letter across `rack + word` must not exceed the number of tiles for that letter in the standard Scrabble set (see `letter_data.csv`). This mirrors the physical constraint that there are a finite number of each tile in the game.

### Building upon a board word
The board word contributes its letters to the available pool of letters. The resulting word can be any word in the dictionary that fits within that combined pool — no positional constraints are enforced (bonus squares, crossing direction, etc. are ignored as specified).

### Blank tiles
Blank tiles are not modelled. Only the 26 standard letters are considered.

### Tie-breaking
When multiple words share the highest score, the one that comes first **alphabetically** (A–Z) is returned. This is implemented by sorting the dictionary alphabetically before the search and keeping only strictly higher-scoring words, so the first alphabetical word at any given score is naturally retained.

### Word length
Only words between 2 and 15 characters (inclusive) are considered, matching the specified rules.

### Case handling
All input is normalised to uppercase before processing. The dictionary is loaded as uppercase; words containing characters other than A–Z are silently ignored.

### No runtime dependencies
The implementation uses only the Java standard library. Maven is needed only for running tests; the application itself can be built and run with plain `javac`/`jar`.

### Letter data format
CSV was chosen for `letter_data.csv` because it requires no external parsing library and is trivially human-readable and editable.
