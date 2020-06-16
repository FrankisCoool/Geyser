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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import org.geysermc.connector.GeyserConnector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipFile;

public class LocaleUtils {

    public static final Map<String, Map<String, String>> LOCALE_MAPPINGS = new HashMap<>();

    private static final String DEFAULT_LOCALE = (GeyserConnector.getInstance().getConfig().getDefaultLocale() != null ? GeyserConnector.getInstance().getConfig().getDefaultLocale() : "en_us");

    static {
        // Create the locales folder
        File localesFolder = new File("locales/");
        localesFolder.mkdir();

        // Download the latest asset list and cache it
        downloadAndLoadLocale(DEFAULT_LOCALE);
    }

    /**
     * Downloads a locale from Mojang if its not already loaded
     *
     * @param locale Locale to download and load
     */
    public static void downloadAndLoadLocale(String locale) {
        locale = locale.toLowerCase();

        // Check the locale isn't already loaded
        if (!FileUtils.ASSET_MAP.containsKey("minecraft/lang/" + locale + ".json") && !locale.equals("en_us")) {
            GeyserConnector.getInstance().getLogger().warning("Invalid locale requested to download and load: " + locale);
            return;
        }

        GeyserConnector.getInstance().getLogger().debug("Downloading and loading locale: " + locale);

        downloadLocale(locale);
        loadLocale(locale);
    }

    /**
     * Downloads the specified locale if its not already downloaded
     *
     * @param locale Locale to download
     */
    private static void downloadLocale(String locale) {
        File localeFile = new File("locales/" + locale + ".json");

        // Check if we have already downloaded the locale file
        if (localeFile.exists()) {
            GeyserConnector.getInstance().getLogger().debug("Locale already downloaded: " + locale);
            return;
        }

        // Create the en_us locale
        if (locale.equals("en_us")) {
            FileUtils.getFileFromClientJar("assets/minecraft/lang/en_us.json", localeFile);
            return;
        }

        // Get the hash and download the locale
        String hash = FileUtils.ASSET_MAP.get("minecraft/lang/" + locale + ".json").getHash();
        WebUtils.downloadFile("http://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash, "locales/" + locale + ".json");
    }

    /**
     * Loads a locale already downloaded, if the file doesn't exist it just logs a warning
     *
     * @param locale Locale to load
     */
    private static void loadLocale(String locale) {
        File localeFile = new File("locales/" + locale + ".json");

        // Load the locale
        if (localeFile.exists()) {
            // Read the localefile
            InputStream localeStream;
            try {
                localeStream = new FileInputStream(localeFile);
            } catch (FileNotFoundException e) {
                throw new AssertionError("Unable to load locale: " + locale + " (" + e.getMessage() + ")");
            }

            // Parse the file as json
            JsonNode localeObj;
            try {
                localeObj = GeyserConnector.JSON_MAPPER.readTree(localeStream);
            } catch (Exception e) {
                throw new AssertionError("Unable to load Java edition lang map for " + locale, e);
            }

            // Parse all the locale fields
            Iterator<Map.Entry<String, JsonNode>> localeIterator = localeObj.fields();
            Map<String, String> langMap = new HashMap<>();
            while (localeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = localeIterator.next();
                langMap.put(entry.getKey(), entry.getValue().asText());
            }

            // Insert the locale into the mappings
            LOCALE_MAPPINGS.put(locale.toLowerCase(), langMap);
        } else {
            GeyserConnector.getInstance().getLogger().warning("Missing locale file: " + locale);
        }
    }

    /**
     * Translate the given language string into the given locale, or falls back to the default locale
     *
     * @param messageText Language string to translate
     * @param locale Locale to translate to
     * @return Translated string or the original message if it was not found in the given locale
     */
    public static String getLocaleString(String messageText, String locale) {
        Map<String, String> localeStrings = LocaleUtils.LOCALE_MAPPINGS.get(locale.toLowerCase());
        if (localeStrings == null)
            localeStrings = LocaleUtils.LOCALE_MAPPINGS.get(DEFAULT_LOCALE);

        return localeStrings.getOrDefault(messageText, messageText);
    }

    public static void init() {
        // no-op
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
class VersionManifest {
    @JsonProperty("latest")
    private LatestVersion latestVersion;

    @JsonProperty("versions")
    private List<Version> versions;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
class LatestVersion {
    @JsonProperty("release")
    private String release;

    @JsonProperty("snapshot")
    private String snapshot;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
class Version {
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("url")
    private String url;

    @JsonProperty("time")
    private String time;

    @JsonProperty("releaseTime")
    private String releaseTime;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
class VersionInfo {
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("time")
    private String time;

    @JsonProperty("releaseTime")
    private String releaseTime;

    @JsonProperty("assetIndex")
    private AssetIndex assetIndex;

    @JsonProperty("downloads")
    private Map<String, VersionDownload> downloads;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
class VersionDownload {
    @JsonProperty("sha1")
    private String sha1;

    @JsonProperty("size")
    private int size;

    @JsonProperty("url")
    private String url;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
class AssetIndex {
    @JsonProperty("id")
    private String id;

    @JsonProperty("sha1")
    private String sha1;

    @JsonProperty("size")
    private int size;

    @JsonProperty("totalSize")
    private int totalSize;

    @JsonProperty("url")
    private String url;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
class Asset {
    @JsonProperty("hash")
    private String hash;

    @JsonProperty("size")
    private int size;
}
