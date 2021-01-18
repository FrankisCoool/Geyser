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

import org.geysermc.connector.GeyserConnector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

public class MinecraftJarUtils {
    private static VersionDownload clientJarInfo;
    private static Path temporaryFilePath;

    private static boolean jarIsDownloaded = false;

    /**
     * Download the client jar for retrieving assets
     */
    public static void downloadClientJar() {
        if (jarIsDownloaded) {
            return;
        }
        try {
            // Let the user know we are downloading the JAR
            GeyserConnector.getInstance().getLogger().info(LanguageUtils.getLocaleStringLog("geyser.locale.download.en_us"));
            GeyserConnector.getInstance().getLogger().debug("Download URL: " + clientJarInfo.getUrl());

            // Download the smallest JAR (client or server)
            temporaryFilePath = GeyserConnector.getInstance().getBootstrap().getConfigFolder().resolve("tmp_client.jar");
            WebUtils.downloadFile(clientJarInfo.getUrl(), temporaryFilePath.toString());

            jarIsDownloaded = true;

            // Store the latest jar hash
            FileUtils.writeFile(GeyserConnector.getInstance().getBootstrap().getConfigFolder().resolve("locales/en_us.hash").toString(), clientJarInfo.getSha1().toCharArray());
        } catch (Exception e) {
            throw new AssertionError(LanguageUtils.getLocaleStringLog("geyser.locale.fail.en_us"), e);
        }
    }

    public static void retrieveFile(String jarFile, File destination) {
        try {
            if (!jarIsDownloaded) {
                GeyserConnector.getInstance().getLogger().error("Downloading Minecraft jarfile again; this shouldn't happen!");
                downloadClientJar();
            }

            // Load in the JAR as a zip and extract the file
            ZipFile localeJar = new ZipFile(temporaryFilePath.toString());
            InputStream fileStream = localeJar.getInputStream(localeJar.getEntry(jarFile));
            FileOutputStream outStream = new FileOutputStream(destination);

            // Write the file to the locale dir
            byte[] buf = new byte[fileStream.available()];
            int length;
            while ((length = fileStream.read(buf)) != -1) {
                outStream.write(buf, 0, length);
            }

            // Flush all changes to disk and cleanup
            outStream.flush();
            outStream.close();

            fileStream.close();
            localeJar.close();
        } catch (Exception e) {
            GeyserConnector.getInstance().getLogger().error("Error whilst retrieving file from Minecraft client jar!");
            e.printStackTrace();
        }
    }

    public static void deleteClientJar() {
        if (!jarIsDownloaded) {
            return;
        }
        try {
            // Delete the no longer needed client jar
            Files.delete(temporaryFilePath);
            jarIsDownloaded = false;
        } catch (Exception e) {
            GeyserConnector.getInstance().getLogger().error("Error while trying to remove ");
            e.printStackTrace();
        }
    }

    public static VersionDownload getClientJarInfo() {
        return clientJarInfo;
    }

    public static void setClientJarInfo(VersionDownload clientJarInfo) {
        MinecraftJarUtils.clientJarInfo = clientJarInfo;
    }
}
