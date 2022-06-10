package io.github.cleydyr.biblivre.backup.uniquelizer.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipOperations {

    public void generateCompressedFile(String fileName, Path explodedBackupPath)
            throws IOException, FileNotFoundException {
        try (FileOutputStream cleanedFileOutputStream = new FileOutputStream(fileName);
                ZipOutputStream cleanedFileZipOutputStream =
                        new ZipOutputStream(cleanedFileOutputStream)) {

            Files.list(explodedBackupPath)
                    .forEach(
                            path -> {
                                addToZipFile(
                                        path.toFile(),
                                        path.getFileName().toString(),
                                        cleanedFileZipOutputStream);
                            });
        }
    }

    public void extractFile(File file, Path destination) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
            for (ZipEntry zipEntry = zipInputStream.getNextEntry();
                    zipEntry != null;
                    zipEntry = zipInputStream.getNextEntry()) {

                File newFile = newFile(destination.toFile(), zipEntry);

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }

                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();

                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                        zipInputStream.transferTo(fileOutputStream);
                    }
                }
            }
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();

        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private void addToZipFile(
            File fileToZip, String fileName, ZipOutputStream zipFileOutputStream) {
        if (fileToZip.isHidden()) {
            return;
        }

        try {
            if (fileToZip.isDirectory()) {
                if (fileName.endsWith("/")) {
                    zipFileOutputStream.putNextEntry(new ZipEntry(fileName));
                    zipFileOutputStream.closeEntry();
                } else {
                    zipFileOutputStream.putNextEntry(new ZipEntry(fileName + "/"));
                    zipFileOutputStream.closeEntry();
                }

                File[] children = fileToZip.listFiles();

                for (File childFile : children) {
                    addToZipFile(
                            childFile, fileName + "/" + childFile.getName(), zipFileOutputStream);
                }

                return;
            }

            ZipEntry zipEntry = new ZipEntry(fileName);

            zipFileOutputStream.putNextEntry(zipEntry);

            try (FileInputStream zipFileInputStream = new FileInputStream(fileToZip)) {
                zipFileInputStream.transferTo(zipFileOutputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
