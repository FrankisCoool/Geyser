package org.geysermc.connector.utils;

import org.geysermc.connector.GeyserConnector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ResourcePack {
    public static final Map<String, ResourcePack> PACKS = new HashMap<>();
    public static final int CHUNK_SIZE = 102400;
    public static boolean LOAD_JAVA_PACK = true;

    private byte[] sha256;
    private File file;
    private ResourcePackManifest manifest;
    private ResourcePackManifest.Version version;

    public static void loadPacks() {
        File directory = new File("packs");
            
        if (!directory.exists()) {
            directory.mkdir();
        }
        
        for (File file : directory.listFiles()) {
            if(file.getName().endsWith(".zip") || file.getName().endsWith(".mcpack")) {
                loadPack(file);
            }
        }

        if (LOAD_JAVA_PACK) {
            FileUtils.downloadClientJar();
            File javaPack = new File("packs/javaPack.zip");
            if (!javaPack.exists()) {
                // Create temporary directory to copy over contents
                File tmpDirectory = new File("packs/tmp");
                tmpDirectory.mkdir();
                try {
                    // Copy over structure of resource pack without images
                    FileUtils.copyFolder(new File(FileUtils.class.getClassLoader().getResource("resource_pack/files").getFile()), tmpDirectory);

                    new File("packs/tmp/textures/entity").mkdirs(); // Can't copy empty folders
                    new File("packs/tmp/font").mkdir();

                    FileUtils.getFileFromClientJar("assets/minecraft/textures/entity/illager/illusioner.png", new File("packs/tmp/textures/entity/illusioner.png"));
                    FileUtils.getFileFromClientJar("assets/minecraft/textures/font/ascii.png", new File("packs/tmp/font/default8.png"));

                    compress(tmpDirectory.toString(), javaPack.toString());
                    Files.delete(tmpDirectory.toPath());
                } catch (IOException e) {
                    GeyserConnector.getInstance().getLogger().error("Could not copy/create Geyser resource pack!");
                    e.printStackTrace();
                    try {
                        Files.delete(tmpDirectory.toPath()); // To prevent errors upon second trial
                    } catch (IOException ignored) {}
                }
                loadPack(javaPack);
            }
        }
    }

    /**
     * Load an individual resource pack
     * @param file
     */
    private static void loadPack(File file) {
        ResourcePack pack = new ResourcePack();

        pack.sha256 = FileUtils.calculateSHA256(file);

        try {
            ZipFile zip = new ZipFile(file);

            zip.stream().forEach((x) -> {
                if (x.getName().contains("manifest.json")) {
                    try {
                        ResourcePackManifest manifest = FileUtils.loadJson(zip.getInputStream(x), ResourcePackManifest.class);

                        pack.file = file;
                        pack.manifest = manifest;
                        pack.version = ResourcePackManifest.Version.fromArray(manifest.getHeader().getVersion());

                        PACKS.put(pack.getManifest().getHeader().getUuid().toString(), pack);
                        GeyserConnector.getInstance().getLogger().debug("Added new resource pack!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            GeyserConnector.getInstance().getLogger().error(file.getName() + " " + "is broken!");
            e.printStackTrace();
        }
    }

    public byte[] getSha256() {
        return sha256;
    }

    public File getFile() {
        return file;
    }

    public ResourcePackManifest getManifest() {
        return manifest;
    }

    public ResourcePackManifest.Version getVersion() {
        return version;
    }

    public static void compress(String dirPath, String outputFile) {
        final Path sourceDir = Paths.get(dirPath);
        try {
            final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(outputFile));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
