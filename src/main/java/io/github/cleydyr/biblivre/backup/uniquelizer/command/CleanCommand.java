package io.github.cleydyr.biblivre.backup.uniquelizer.command;

import java.io.File;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.cleydyr.biblivre.backup.uniquelizer.processor.BackupCleaner;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(
        name = "clean",
        mixinStandardHelpOptions = true,
        description = "Removes duplicate rows from a Biblivre backup and generates a valid new one")
public class CleanCommand implements Callable<Integer> {
	private static final Logger logger = LoggerFactory.getLogger(CleanCommand.class);

    @Parameters(index = "0", description = "The backup file to clean")
    private File file;

    @Autowired
    private BackupCleaner backupCleaner;

    @Override
    public Integer call() throws Exception {
    	logger.info("Cleaning file " + file);

        backupCleaner.processFile(file);

        return 0;
    }

    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        int exitCode = new CommandLine(new CleanCommand()).execute(args);

        System.exit(exitCode);
    }
}
