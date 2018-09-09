package nl.juraji.imagemanager.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Juraji on 1-5-2018.
 * pinterestdownloader
 */
public final class FileUtils {

    public static String bytesInHumanReadable(float bytes) {
        final String[] dictionary = {"bytes", "KB", "MB", "GB", "TB"};
        final int stepSize = 1024;

        int index;

        for (index = 0; index < dictionary.length; index++) {
            if (bytes < stepSize) {
                break;
            }

            bytes = bytes / stepSize;
        }

        return String.format("%.1f", bytes) + " " + dictionary[index];
    }

    public static List<File> listFilesAndDirectories(File root, String[] fileExtensionFilter) {
        if (root == null || !root.exists()) {
            throw new IllegalArgumentException("root is null or non-existent");
        }

        Predicate<File> fileExtensionMatcher;
        if (fileExtensionFilter != null) {
            final List<String> extensionFilterList = Arrays.asList(fileExtensionFilter);
            fileExtensionMatcher = file -> {
                final String ext = file.getName().replaceAll(".*\\.([0-9A-Za-z]+)$", "$1");
                return extensionFilterList.contains(ext);
            };
        } else {
            fileExtensionMatcher = file -> true;
        }

        final List<File> files = new ArrayList<>();
        final File[] listFiles = root.listFiles();

        if (listFiles != null) {
            for (File listFile : listFiles) {
                if (listFile.isDirectory() || (!listFile.isDirectory() && fileExtensionMatcher.test(listFile))) {
                    files.add(listFile);
                }
            }
        }

        return files;
    }

    public static void deleteIfExists(File root) throws IOException {
        if (root == null) {
            // Root is null, there is nothing to delete
            return;
        }

        // Delete directory contents
        final List<File> files = listFilesAndDirectories(root, null);
        for (File file : files) {
            if (file.isDirectory()) {
                deleteIfExists(file);
            } else {
                Files.delete(file.toPath());
            }
        }

        Files.delete(root.toPath());
    }
}
