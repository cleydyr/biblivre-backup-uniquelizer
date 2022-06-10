package io.github.cleydyr.biblivre.backup.uniquelizer;

import io.github.cleydyr.biblivre.backup.uniquelizer.command.CleanCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class CleanerRunner implements CommandLineRunner, ExitCodeGenerator {

    private final CleanCommand cleaner;

    private final IFactory factory;

    private int exitCode;

    public CleanerRunner(CleanCommand cleaner, IFactory factory) {
        this.cleaner = cleaner;
        this.factory = factory;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(cleaner, factory).execute(args);
    }
}
