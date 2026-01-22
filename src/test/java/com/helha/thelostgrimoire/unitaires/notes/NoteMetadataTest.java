package com.helha.thelostgrimoire.unitaires.notes;

import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tests Unitaires - Logique Métier (Metadata)")
class NoteMetadataTest {

    @Test
    @DisplayName("NULL Content -> 0")
    void testNullContent() {
        DbNotes note = new DbNotes();
        note.content = null;
        assertEquals(0, note.getWordCount());
        assertEquals(0, note.getCharacterCount());
        assertEquals(0, note.getLineCount());
    }

    @Test
    @DisplayName("Empty Content -> 0")
    void testEmptyContent() {
        DbNotes note = new DbNotes();
        note.content = "";
        assertEquals(0, note.getWordCount());
        assertEquals(0, note.getCharacterCount());
    }

    @ParameterizedTest(name = "Content='{0}' expecting {1} words")
    @CsvSource({
            "'Hello', 1",
            "'Hello World', 2",
            "'One Two Three', 3",
            "'   Spaces   ', 1",
            "'Word1  Word2', 2",
            "'Word1\tWord2', 2",
            "'Line1\nLine2', 2"
    })
    void testWordCounts(String content, int expectedWords) {
        DbNotes note = new DbNotes();
        note.content = content;
        assertEquals(expectedWords, note.getWordCount());
    }

    @ParameterizedTest(name = "Content='{0}' expecting {1} lines")
    @CsvSource({
            "'Hello', 1",
            "'Hello\nWorld', 2",
            "'One\nTwo\nThree', 3",
            "'', 0"
    })
    void testLineCounts(String content, int expectedLines) {
        DbNotes note = new DbNotes();
        note.content = content;
        if(content.isEmpty()) {
            assertEquals(0, note.getLineCount());
        } else {
            assertEquals(expectedLines, note.getLineCount());
        }
    }

    @Test
    @DisplayName("Emojis & UTF-8")
    void testSpecialChars() {
        DbNotes note = new DbNotes();
        note.content = "🎃";

        // CORRECTION : En Java (UTF-16), cet emoji prend 2 caractères (surrogate pair)
        assertEquals(2, note.getCharacterCount());

        // Optionnel : Vérif taille octets (UTF-8 = 4 octets)
         assertEquals(4, note.getByteSize());
    }
}