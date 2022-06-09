package io.github.cleydyr.biblivrebackupuniquelizer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cleydyr.biblivre.backup.uniquelizer.processor.BackupCleaner;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class BackupCleanerTest {

    @Test
    void testUniqueFileNameGenerated() throws InterruptedException {
        String fileName = "foobar";

        BackupCleaner backupCleaner = new BackupCleaner();

        String fileNameA = backupCleaner.generateFileName(fileName);

        Thread.sleep(500);

        String fileNameB = backupCleaner.generateFileName(fileName);

        assertNotEquals(fileNameA, fileNameB);
    }

    @Test
    void testGeneratedFilePrefix() throws InterruptedException {
        String fileName = "foobar";

        BackupCleaner backupCleaner = new BackupCleaner();

        String fileNameA = backupCleaner.generateFileName(fileName);

        assertTrue(fileNameA.contains(fileName));
    }

    @Test
    void testUniqueIdTableName() {
        BackupCleaner backupCleaner = new BackupCleaner();

        assertTrue(
                backupCleaner.hasUniqueIDTable(
                        "COPY vocabulary_searches (id, parameters, created, created_by) FROM stdin;"));

        assertFalse(
                backupCleaner.hasUniqueIDTable(
                        "COPY vocabulary_form_datafields (datafield, collapsed, repeatable, indicator_1, indicator_2, material_type, created, created_by, modified, modified_by, sort_order) FROM stdin;"));
    }

    @Test
    void testRemoveRepeatedIdLine() {
        BackupCleaner backupCleaner = new BackupCleaner();

        String lines =
                "COPY biblio_searches (id, parameters, created, created_by) FROM stdin; \n"
                        + "6946 \n"
                        + "6947	\n"
                        + "6948	\n"
                        + "6949	\n"
                        + "6950	\n"
                        + "6946	shouldberemoved \n"
                        + "\\.";

        BackupCleaner.State state = backupCleaner.new State();

        Stream<String> filteredLines =
                lines.lines().filter(line -> backupCleaner.processLine(lines, state));

        assertFalse(filteredLines.anyMatch(line -> line.contains("shouldberemoved")));

        String lines2 =
                "COPY vocabulary_form_datafields (id, parameters, created, created_by) FROM stdin; \n"
                        + "6946 \n"
                        + "6947	\n"
                        + "6948	\n"
                        + "6946	shouldnotberemoved \n"
                        + "6949	\n"
                        + "6950	\n"
                        + "\\.";

        Stream<String> filteredLines2 =
                lines2.lines()
                        .filter(
                                line ->
                                        backupCleaner.processLine(
                                                lines, backupCleaner.new State()));

        assertTrue(filteredLines2.anyMatch(line -> line.contains("shouldnotberemoved")));
    }
}
