package nl.juraji.imagemanager.util.io;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Juraji on 22-8-2018.
 * Image Manager
 * Override of {@link java.io.FileInputStream} implementing {@link AutoCloseable}
 */
public class FileInputStream extends java.io.FileInputStream implements AutoCloseable {

    public FileInputStream(@NotNull File file) throws FileNotFoundException {
        super(file);
    }
}
