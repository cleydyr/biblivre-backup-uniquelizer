package io.github.cleydyr.biblivre.backup.uniquelizer.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BackupCleaner {

    private static final String SPACE = " ";

    private static final char TAB = '\t';

    private static String[] UNIQUE_ID_TABLES = {
        "access_cards",
        "access_control",
        "authorities_idx_autocomplete",
        "authorities_indexing_groups",
        "authorities_records",
        "authorities_searches",
        "backups",
        "biblio_idx_autocomplete",
        "biblio_indexing_groups",
        "biblio_records",
        "biblio_searches",
        "digital_media",
        "holding_creation_counter",
        "lending_fines",
        "logins",
        "order",
        "quotations",
        "request",
        "supplier",
        "users_types",
        "vocabulary_idx_autocomplete",
        "vocabulary_indexing_groups",
        "vocabulary_records",
        "vocabulary_searches",
        "z3950_addresses",
        "biblio_holdings",
        "lendings",
        "reservations",
        "users_pkey"
    };

    private static final String COPY_BEGIN = "COPY ";
    private static final String COPY_END = "\\.";
    private static final Logger logger = LoggerFactory.getLogger(BackupCleaner.class);

    @Autowired private ZipOperations zipOperations;

    public void processFile(File backup) throws IOException {
        logger.info("Create temporary directory...");

        Path explodedBackupPath = Files.createTempDirectory("uniquelizer");

        logger.info("Extracting file...");
        zipOperations.extractFile(backup, explodedBackupPath);

        logger.info("Processing duplicates...");
        overwriteWithCleanedUpFiles(explodedBackupPath);

        logger.info("Compressing file again...");
        zipOperations.generateCompressedFile(
                generateFileName(backup.getName()), explodedBackupPath);
    }

    private void overwriteWithCleanedUpFiles(Path explodedBackupPath) throws IOException {
        Stream<Path> dataFiles =
                Files.list(explodedBackupPath).filter(p -> p.toString().endsWith(".data.b5b"));

        dataFiles
                .collect(Collectors.toMap(Function.identity(), this::cleanupLines))
                .forEach(this::overwriteWithLines);
    }

    public String generateFileName(String fileName) {
        return fileName + "-" + System.currentTimeMillis() + "-cleaned.b5bz";
    }

    private void overwriteWithLines(Path path, Collection<String> lines) {
        try {
            Files.write(path, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Collection<String> cleanupLines(Path dataPath) {
        Collection<String> lines = new ArrayList<>();

        try {
            BufferedReader bufferedReader = Files.newBufferedReader(dataPath);

            State state = new State();

            return bufferedReader
                    .lines()
                    .filter(line -> processLine(line, state))
                    .collect(Collectors.toList());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return lines;
    }

    public boolean processLine(String line, State state) {
        if (state.isOnCopy) {
            if (COPY_END.equals(line)) {
                state.isOnCopy = false;

                state.uniqueIds = null;
            } else {
                int lastIndexOfSpace = line.indexOf(TAB);

                String id = line.substring(0, lastIndexOfSpace);

                if (state.uniqueIds.contains(id)) {
                    return false;
                } else {
                    state.uniqueIds.add(id);
                }
            }
        } else {
            if (line.startsWith(COPY_BEGIN) && hasUniqueIDTable(line)) {
                state.isOnCopy = true;

                state.uniqueIds = new HashSet<>();
            }
        }

        state.lineCount++;

        return true;
    }

    public boolean hasUniqueIDTable(String copyLine) {
        String[] parts = copyLine.split(SPACE, 3);

        String tableName = parts[1];

        return Arrays.stream(UNIQUE_ID_TABLES).anyMatch(tableName::equals);
    }

    public class State {
        int lineCount = 1;

        boolean isOnCopy;

        Set<String> uniqueIds;
    }
}
