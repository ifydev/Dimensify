/*
 *
 * This file is part of Dimensify, licensed under the MIT License (MIT).
 * Copyright (c) Innectic
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.ifydev.dimensifyspigot.world;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.Set;

public class WorldFiles {

    private static final Set<String> EXCLUDED = Set.of("uid.dat", "session.lock", "playerdata", "players", "stats", "advancements");
    private final Path container;

    public WorldFiles(Path container) {
        try {
            this.container = container.toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("Could not open world container", e);
        }
    }

    public static void validateName(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_-]{1,50}"))
            throw new IllegalArgumentException("Names must contain 1-50 letters, numbers, underscores or hyphens.");
        if (name.matches("(?i)(con|prn|aux|nul|com[0-9]|lpt[0-9])"))
            throw new IllegalArgumentException("That name is reserved by the operating system.");
    }

    public Path resolve(String name) {
        validateName(name);
        Path target = container.resolve(name).normalize();
        if (!target.getParent().equals(container) || Files.isSymbolicLink(target))
            throw new IllegalArgumentException("World folder must be inside the world container.");
        try {
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS) && !target.toRealPath().equals(target))
                throw new IllegalArgumentException("World folder must not be a link.");
        } catch (IOException e) {
            throw new IllegalStateException("Could not inspect world folder", e);
        }
        return target;
    }

    public boolean exists(String name) {
        return Files.exists(resolve(name), LinkOption.NOFOLLOW_LINKS);
    }

    public void copy(String source, String destination) throws IOException {
        Path from = resolve(source);
        Path to = resolve(destination);
        if (!Files.isRegularFile(from.resolve("level.dat"))) throw new IOException("Source has no level.dat.");
        Files.createDirectory(to);
        Files.walkFileTree(from, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) throws IOException {
                if (directory.equals(from)) return FileVisitResult.CONTINUE;
                if (EXCLUDED.contains(directory.getFileName().toString())) return FileVisitResult.SKIP_SUBTREE;
                if (!directory.toRealPath().startsWith(from)) throw new IOException("Linked world directories are not supported.");
                Files.createDirectory(to.resolve(from.relativize(directory)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                if (Files.isSymbolicLink(file)) throw new IOException("Linked world files are not supported.");
                if (!EXCLUDED.contains(file.getFileName().toString())) Files.copy(file, to.resolve(from.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void move(String source, Path destination) throws IOException {
        Path from = resolve(source);
        Files.createDirectories(destination.toAbsolutePath().getParent());
        Files.move(from, destination);
    }
}
