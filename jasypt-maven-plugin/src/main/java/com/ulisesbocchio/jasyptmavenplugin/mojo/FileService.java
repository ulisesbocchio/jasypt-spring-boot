package com.ulisesbocchio.jasyptmavenplugin.mojo;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * A service for operating on files.
 *
 * @author Rupert Madden-Abbott
 * @version $Id: $Id
 */
public class FileService {
    /**
     * Read a file.
     *
     * @param path the file path
     * @return the contents.
     * @throws org.apache.maven.plugin.MojoExecutionException if any.
     */
    public static String read(final Path path) throws MojoExecutionException {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read file " + path, e);
        }
    }

    /**
     * Write to a file.
     *
     * @param path     the file path
     * @param contents the contents to write
     * @throws org.apache.maven.plugin.MojoExecutionException if any.
     */
    public static void write(final Path path, final String contents) throws MojoExecutionException {
        try {
            Files.write(path, contents.getBytes());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write file " + path, e);
        }
    }

    /**
     * Load a file into properties.
     *
     * @param path       the path
     * @param properties the properties to mutate
     * @throws org.apache.maven.plugin.MojoExecutionException if any.
     */
    public static void load(final Path path, final Properties properties)
            throws MojoExecutionException {
        try {
            properties.load(Files.newInputStream(path));
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to load file " + path, e);
        }
    }
}
