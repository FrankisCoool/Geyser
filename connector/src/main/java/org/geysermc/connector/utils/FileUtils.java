/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.geysermc.connector.GeyserConnector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipFile;

public class FileUtils {

    public static final Map<String, Asset> ASSET_MAP = new HashMap<>();

    private static String clientJarURL = "";

    static {
        generateAssetCache();
    }

    /**
     * Load the given YAML file into the given class
     *
     * @param src File to load
     * @param valueType Class to load file into
     * @return The data as the given class
     * @throws IOException if the config could not be loaded
     */
    public static <T> T loadConfig(File src, Class<T> valueType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        return objectMapper.readValue(src, valueType);
    }

    public static <T> T loadYaml(InputStream src, Class<T> valueType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory()).enable(JsonParser.Feature.IGNORE_UNDEFINED).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper.readValue(src, valueType);
    }

    public static <T> T loadJson(InputStream src, Class<T> valueType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()).enable(JsonParser.Feature.IGNORE_UNDEFINED).enable(JsonParser.Feature.ALLOW_COMMENTS).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper.readValue(src, valueType);
    }

    /**
     * Open the specified file or copy if from resources
     *
     * @param name File and resource name
     * @param fallback Formatting callback
     * @return File handle of the specified file
     * @throws IOException if the file failed to copy from resource
     */
    public static File fileOrCopiedFromResource(String name, Function<String, String> fallback) throws IOException {
        return fileOrCopiedFromResource(new File(name), name, fallback);
    }

    /**
     * Open the specified file or copy if from resources
     *
     * @param file File to open
     * @param name Name of the resource get if needed
     * @param format Formatting callback
     * @return File handle of the specified file
     * @throws IOException if the file failed to copy from resource
     */
    public static File fileOrCopiedFromResource(File file, String name, Function<String, String> format) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            InputStream input = GeyserConnector.class.getResourceAsStream("/" + name); // resources need leading "/" prefix

            byte[] bytes = new byte[input.available()];

            input.read(bytes);

            for(char c : format.apply(new String(bytes)).toCharArray()) {
                fos.write(c);
            }

            fos.flush();
            input.close();
            fos.close();
        }

        return file;
    }

    /**
     * Writes the given data to the specified file on disk
     *
     * @param file File to write to
     * @param data Data to write to the file
     * @throws IOException if the file failed to write
     */
    public static void writeFile(File file, char[] data) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file);

        for (char c : data) {
            fos.write(c);
        }

        fos.flush();
        fos.close();
    }

    /**
     * Writes the given data to the specified file on disk
     *
     * @param name File path to write to
     * @param data Data to write to the file
     * @throws IOException if the file failed to write
     */
    public static void writeFile(String name, char[] data) throws IOException {
        writeFile(new File(name), data);
    }

    /**
     * Get an InputStream for the given resource path, throws AssertionError if resource is not found
     *
     * @param resource Resource to get
     * @return InputStream of the given resource
     */
    public static InputStream getResource(String resource) {
        InputStream stream = FileUtils.class.getClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            throw new AssertionError("Unable to find resource: " + resource);
        }
        return stream;
    }

    /**
     * Calculate the SHA256 hash of the resource pack file
     * @param file File to calculate the hash for
     * @return A byte[] representation of the hash
     */
    public static byte[] calculateSHA256(File file) {
        byte[] sha256;

        try {
            sha256 = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            throw new RuntimeException("Could not calculate pack hash", e);
        }

        return sha256;
    }

    public static void copyFolder(File source, File destination)
    {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String files[] = source.list();

            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);
                    copyFolder(srcFile, destFile);
                }
            }
        }
        else {
            InputStream in;
            OutputStream out;

            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(destination);

                byte[] buffer = new byte[1024];

                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to copy folder!", e);
            }
        }
    }

    /**
     * Fetch the latest versions asset cache from Mojang so we can grab the locale files later
     */
    private static void generateAssetCache() {
        try {
            // Get the version manifest from Mojang
            VersionManifest versionManifest = GeyserConnector.JSON_MAPPER.readValue(WebUtils.getBody("https://launchermeta.mojang.com/mc/game/version_manifest.json"), VersionManifest.class);

            // Get the url for the latest version of the games manifest
            String latestInfoURL = "";
            for (Version version : versionManifest.getVersions()) {
                if (version.getId().equals(versionManifest.getLatestVersion().getRelease())) {
                    latestInfoURL = version.getUrl();
                    break;
                }
            }

            // Make sure we definitely got a version
            if (latestInfoURL.isEmpty()) {
                throw new Exception("Unable to get latest Minecraft version");
            }

            // Get the individual version manifest
            VersionInfo versionInfo = GeyserConnector.JSON_MAPPER.readValue(WebUtils.getBody(latestInfoURL), VersionInfo.class);

            // Get the smallest jar for use when downloading the en_us locale, will be either the server or client
            for (VersionDownload download : versionInfo.getDownloads().values()) {
                if (download.getUrl().endsWith("client.jar")) {
                    clientJarURL = download.getUrl();
                }
            }

            // Get the assets list
            JsonNode assets = GeyserConnector.JSON_MAPPER.readTree(WebUtils.getBody(versionInfo.getAssetIndex().getUrl())).get("objects");

            // Put each asset into an array for use later
            Iterator<Map.Entry<String, JsonNode>> assetIterator = assets.fields();
            while (assetIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = assetIterator.next();
                Asset asset = GeyserConnector.JSON_MAPPER.treeToValue(entry.getValue(), Asset.class);
                ASSET_MAP.put(entry.getKey(), asset);
            }

            //downloadClientJar();

        } catch (Exception e) {
            GeyserConnector.getInstance().getLogger().info("Failed to load asset cache: " + (!e.getMessage().isEmpty() ? e.getMessage() : e.getStackTrace()));
        }
    }

    /**
     * Download then en_us locale by downloading the server jar and extracting it from there.
     */
    public static void downloadClientJar() {
        if (new File("tmp_client.jar").exists()) return;
        try {
            // Let the user know we are downloading the JAR
            GeyserConnector.getInstance().getLogger().info("Downloading Minecraft JAR to extract en_us locale, please wait... (this may take some time depending on the speed of your internet connection)");
            GeyserConnector.getInstance().getLogger().debug("Download URL: " + clientJarURL);

            // Download the client jar to get language files and assets
            WebUtils.downloadFile(clientJarURL, "tmp_client.jar");

//            if (!ResourcePack.LOAD_JAVA_PACK) {
//                // Delete the nolonger needed client/server jar
//                Files.delete(Paths.get("tmp_client.jar"));
//            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Unable to download and extract client jar!", e);
        }
    }

    /**
     * Retrieves a file from a downloaded client jar
     * @param path
     * @param output
     */
    public static void getFileFromClientJar(String path, File output) {
        try {
            if (!output.exists()) {
                output.createNewFile();
            }
            // Load in the JAR as a zip and extract the file
            ZipFile clientJar = new ZipFile("tmp_client.jar");

            InputStream inputStream = clientJar.getInputStream(clientJar.getEntry(path));
            FileOutputStream outputStream = new FileOutputStream(output);

            // Write the file to the locale dir
            int data = inputStream.read();
            while (data != -1) {
                outputStream.write(data);
                data = inputStream.read();
            }

            // Flush all changes to disk and cleanup
            outputStream.flush();
            outputStream.close();

            inputStream.close();

            clientJar.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("Unable to download and extract file from jar!", e);
        }
    }
}
