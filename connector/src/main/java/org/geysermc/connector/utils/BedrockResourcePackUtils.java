/*
 * Copyright (c) 2019-2021 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.connector.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.geysermc.connector.GeyserConnector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BedrockResourcePackUtils {

    private static final String VERSION = "1.0.0";

    public static void create() {
        GeyserConnector.getInstance().getLogger().info("Creating resource pack...");
        //TODO Don't download twice
        MinecraftJarUtils.downloadClientJar();

        File cacheFile = GeyserConnector.getInstance().getBootstrap().getConfigFolder().resolve("cache").toFile();
        if (cacheFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cacheFile.mkdir();
        }

        File finalZipLocation = cacheFile.toPath().resolve("javapack-" + VERSION + ".zip").toFile();
        if (finalZipLocation.exists()) {
            // Pack has already been translated. :)
            ResourcePack pack = ResourcePack.loadPack(finalZipLocation);
            ResourcePack.PACKS.put(pack.getManifest().getHeader().getUuid().toString(), pack);
            return;
        }

        File workingDir = cacheFile.toPath().resolve("javapack").toFile();
        if (!workingDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            workingDir.mkdir();
        }

        //

        try {
            JsonNode filesFromJar;
            try (InputStream stream = FileUtils.getResource("resourcepack/files_to_copy.json")) {
                filesFromJar = GeyserConnector.JSON_MAPPER.readTree(stream);
            }

            for (JsonNode entry : filesFromJar) {
                File path = workingDir.toPath().resolve(entry.get("to").asText()).toFile();
                if (!path.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    path.mkdirs();
                }

                String fileName = entry.get("filename").asText();
                MinecraftJarUtils.retrieveFile(entry.get("from").asText() + "/" + fileName, path.toPath().resolve(fileName).toFile());
            }

            JsonNode filesToCopy;
            try (InputStream stream = FileUtils.getResource("resourcepack/files/files_to_move.json")) {
                filesToCopy = GeyserConnector.JSON_MAPPER.readTree(stream);
            }

            for (JsonNode entry : filesToCopy) {
                File path = workingDir.toPath().resolve(entry.get("destination").asText()).toFile();
                if (!path.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    path.mkdirs();
                }

                String fileName = entry.get("filename").asText();
                Files.copy(FileUtils.getResource("resourcepack/files/" + fileName), path.toPath().resolve(fileName));
            }

            Path workingDirPath = workingDir.toPath();
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(finalZipLocation));
            Files.walkFileTree(workingDirPath, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(workingDirPath.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
            zos.close();

            deleteFolder(workingDir);

            ResourcePack pack = ResourcePack.loadPack(finalZipLocation);
            ResourcePack.PACKS.put(pack.getManifest().getHeader().getUuid().toString(), pack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MinecraftJarUtils.deleteClientJar();
    }

    private static void deleteFolder(File file) {
        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }

}
