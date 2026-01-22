package com.helha.thelostgrimoire.unitaires.notes;

import com.helha.thelostgrimoire.infrastructure.notes.DbNotes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("🧮 Tests Unitaires - Calcul des Métadonnées (DbNotes)")
class NoteMetadataTest {

    @Test
    @DisplayName("Doit calculer correctement pour une phrase simple")
    void shouldCalculateForSimpleSentence() {
        // GIVEN
        DbNotes note = new DbNotes();
        note.content = "Bonjour le monde";

        // WHEN & THEN
        assertEquals(3, note.getWordCount(), "Devrait compter 3 mots");
        assertEquals(1, note.getLineCount(), "Devrait compter 1 ligne");
        assertEquals(16, note.getCharacterCount(), "Devrait compter 16 caractères");
        assertEquals(16, note.getByteSize(), "Devrait compter 16 octets (ASCII)");
    }

    @Test
    @DisplayName("Doit calculer correctement pour plusieurs lignes")
    void shouldCalculateForMultiLine() {
        // GIVEN
        DbNotes note = new DbNotes();
        note.content = "Ligne 1\nLigne 2";

        // WHEN & THEN
        // "Ligne 1" (7) + "\n" (1) + "Ligne 2" (7) = 15 chars
        assertEquals(4, note.getWordCount(), "Devrait compter 4 mots");
        assertEquals(2, note.getLineCount(), "Devrait compter 2 lignes");
        assertEquals(15, note.getCharacterCount(), "Devrait compter 15 caractères");
    }

    @Test
    @DisplayName("Doit renvoyer 0 pour un contenu vide")
    void shouldReturnZeroForEmptyContent() {
        // GIVEN
        DbNotes note = new DbNotes();
        note.content = "";

        // WHEN & THEN
        assertEquals(0, note.getWordCount());
        assertEquals(0, note.getLineCount()); // Ou 1 selon ton implémentation, mais 0 est logique pour du vide
        assertEquals(0, note.getCharacterCount());
        assertEquals(0, note.getByteSize());
    }

    @Test
    @DisplayName("Doit gérer les espaces multiples (trim)")
    void shouldHandleMultipleSpaces() {
        // GIVEN
        DbNotes note = new DbNotes();
        note.content = "   Mot1    Mot2   ";

        // WHEN & THEN
        assertEquals(2, note.getWordCount(), "Ne doit pas compter les espaces comme des mots");
        assertEquals(18, note.getCharacterCount(), "Compte tous les caractères, y compris espaces");
    }

    @Test
    @DisplayName("Doit gérer les caractères spéciaux (UTF-8)")
    void shouldHandleSpecialCharacters() {
        // GIVEN
        // Le symbole '€' compte pour 1 caractère, mais souvent 3 octets en UTF-8
        DbNotes note = new DbNotes();
        note.content = "€";

        // WHEN & THEN
        assertEquals(1, note.getCharacterCount(), "€ est 1 seul caractère");

        // Note: En Java, getBytes() par défaut dépend de l'OS, mais généralement on veut UTF-8.
        // Si ton implémentation utilise content.getBytes(StandardCharsets.UTF_8).length :
        assertEquals(3, note.getByteSize(), "€ pèse 3 octets en UTF-8");
    }

    @Test
    @DisplayName("Doit être robuste si le contenu est NULL")
    void shouldHandleNullContent() {
        // GIVEN
        DbNotes note = new DbNotes();
        note.content = null;

        // WHEN & THEN
        // Idéalement, tes getters doivent avoir un "if (content == null) return 0;"
        assertEquals(0, note.getWordCount());
        assertEquals(0, note.getByteSize());
    }
}