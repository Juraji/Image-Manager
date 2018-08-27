package nl.juraji.imagemanager.util.io;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Juraji on 23-8-2018.
 * Image Manager
 * Override of {@link java.io.FileOutputStream} implementing {@link AutoCloseable}
 */
public class FileOutputStream extends java.io.FileOutputStream implements AutoCloseable {
    public FileOutputStream(@NotNull String name) throws FileNotFoundException {
        super(name);
    }

    public FileOutputStream(@NotNull File file) throws FileNotFoundException {
        super(file);
    }
}
