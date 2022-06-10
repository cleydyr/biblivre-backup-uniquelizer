package io.github.cleydyr.biblivre.backup.uniquelizer.command;

import io.github.cleydyr.biblivre.backup.uniquelizer.processor.BackupCleaner;
import java.io.File;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "cleaner",
        mixinStandardHelpOptions = true,
        description = "Removes duplicate rows from a Biblivre backup and generates a valid new one")
class Cleaner implements Callable<Integer> {

    @Parameters(index = "0", description = "The backup file to clean")
    private File file;

    @Override
    public Integer call() throws Exception {
        BackupCleaner backupCleaner = new BackupCleaner();

        System.out.println("Cleaning file " + file);
        backupCleaner.processFile(file);

        return 0;
    }

    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        int exitCode = new CommandLine(new Cleaner()).execute(args);

        System.exit(exitCode);
    }
}
