package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads animation frames from both a normal filesystem and resources packed in a JAR.
 * Classpath directories cannot always be enumerated reliably through FileHandle.list()
 * once they are inside a JAR, so this loader explicitly enumerates JAR entries.
 */
public final class AnimationLoader {

    private AnimationLoader() {
    }

    public static boolean hasFrames(String folderName) {
        return !findFramePaths(folderName).isEmpty();
    }

    public static Animation<TextureRegion> load(
        String folderName,
        float frameDuration,
        Animation.PlayMode playMode,
        List<Texture> loadedTextures
    ) {
        List<String> framePaths = findFramePaths(folderName);

        TextureRegion[] frames = new TextureRegion[framePaths.size()];
        for (int i = 0; i < framePaths.size(); i++) {
            FileHandle file = Gdx.files.internal(framePaths.get(i));
            Texture texture = new Texture(file);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            loadedTextures.add(texture);
            frames[i] = new TextureRegion(texture);
        }

        if (frames.length == 0) {
            Gdx.app.error("AnimationLoader", "No animation frames found: " + folderName);
            return createFallback(frameDuration, playMode, loadedTextures);
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(playMode);
        return animation;
    }

    private static List<String> findFramePaths(String folderName) {
        String normalized = folderName.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        List<String> result = findFromCodeSource(normalized);
        if (!result.isEmpty()) {
            return result;
        }

        // Development/classpath fallback.
        try {
            FileHandle folder = Gdx.files.internal(normalized);
            if (folder.exists() && folder.isDirectory()) {
                FileHandle[] files = folder.list((dir, name) -> isImage(name));
                if (files != null) {
                    for (FileHandle file : files) {
                        result.add(normalized + "/" + file.name());
                    }
                    result.sort(String.CASE_INSENSITIVE_ORDER);
                }
            }
        } catch (Exception ignored) {
            // JAR classpath may not support directory listing; code-source scan above is preferred.
        }

        return result;
    }

    private static List<String> findFromCodeSource(String folderName) {
        List<String> result = new ArrayList<>();

        try {
            URL location = AnimationLoader.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();

            if (location == null) {
                return result;
            }

            if ("file".equalsIgnoreCase(location.getProtocol())) {
                File locationFile = new File(location.toURI());

                if (locationFile.isDirectory()) {
                    Path folder = locationFile.toPath().resolve(folderName);
                    if (Files.isDirectory(folder)) {
                        try (var stream = Files.list(folder)) {
                            stream.filter(Files::isRegularFile)
                                .filter(path -> isImage(path.getFileName().toString()))
                                .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                                .forEach(path -> result.add(folderName + "/" + path.getFileName()));
                        }
                    }
                } else if (locationFile.isFile() && locationFile.getName().toLowerCase().endsWith(".jar")) {
                    readJar(locationFile, folderName, result);
                }
            } else if ("jar".equalsIgnoreCase(location.getProtocol())) {
                JarURLConnection connection = (JarURLConnection) location.openConnection();
                try (JarFile jar = connection.getJarFile()) {
                    readJar(jar, folderName, result);
                }
            }
        } catch (Exception ignored) {
            // Fall back to Gdx FileHandle enumeration.
        }

        return result;
    }

    private static void readJar(File jarFile, String folderName, List<String> result) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            readJar(jar, folderName, result);
        }
    }

    private static void readJar(JarFile jar, String folderName, List<String> result) {
        String prefix = folderName + "/";
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }

            String name = entry.getName();
            if (!name.startsWith(prefix)) {
                continue;
            }

            String remainder = name.substring(prefix.length());
            if (!remainder.contains("/")) {
                String fileName = name.substring(name.lastIndexOf('/') + 1);
                if (isImage(fileName)) {
                    result.add(name);
                }
            }
        }

        result.sort(String.CASE_INSENSITIVE_ORDER);
    }

    private static boolean isImage(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    private static Animation<TextureRegion> createFallback(
        float frameDuration,
        Animation.PlayMode playMode,
        List<Texture> loadedTextures
    ) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        loadedTextures.add(texture);

        Animation<TextureRegion> animation = new Animation<>(
            frameDuration,
            new TextureRegion(texture)
        );
        animation.setPlayMode(playMode);
        return animation;
    }
}
