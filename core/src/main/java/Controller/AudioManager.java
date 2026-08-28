package Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private Music currentMusic;
    private String currentTrackPath = "";
    private String pendingTrackPath = null;
    private boolean pendingLooping = true;

    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    private float masterVolume = 1f;
    private float baseVolume = 0.75f;
    private float sfxVolume = 0.9f;

    private boolean fadingOut = false;
    private float fadeTimer = 0f;
    private float fadeOutDuration = 0.45f;

    private final Map<String, Sound> sfxCache = new HashMap<>();

    public AudioManager() {
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;

        if (!musicEnabled) {
            stopMusic();
        } else if (currentMusic != null) {
            currentMusic.setVolume(baseVolume * masterVolume);
            if (!currentMusic.isPlaying()) {
                currentMusic.play();
            }
        }
    }

    public void setMasterVolume(float masterVolume) {
        this.masterVolume = clamp01(masterVolume);

        if (currentMusic != null && musicEnabled) {
            currentMusic.setVolume(baseVolume * this.masterVolume);
        }
    }

    public void setBaseVolume(float baseVolume) {
        this.baseVolume = clamp01(baseVolume);

        if (currentMusic != null && musicEnabled) {
            currentMusic.setVolume(this.baseVolume * masterVolume);
        }
    }

    public void changeMusic(String newTrackPath) {
        changeMusic(newTrackPath, true);
    }

    public void changeMusic(String newTrackPath, boolean looping) {
        if (!musicEnabled || newTrackPath == null || newTrackPath.isEmpty()) {
            stopMusic();
            return;
        }

        if (newTrackPath.equals(currentTrackPath) && currentMusic != null) {
            return;
        }

        pendingLooping = looping;

        if (currentMusic == null) {
            startTrack(newTrackPath, looping);
            return;
        }

        pendingTrackPath = newTrackPath;
        fadingOut = true;
        fadeTimer = 0f;
    }

    public void update(float delta) {
        if (!musicEnabled) {
            return;
        }

        if (!fadingOut) {
            return;
        }

        fadeTimer += delta;

        if (currentMusic != null) {
            float t = clamp01(fadeTimer / fadeOutDuration);
            float volume = (1f - t) * baseVolume * masterVolume;
            currentMusic.setVolume(volume);
        }

        if (fadeTimer >= fadeOutDuration) {
            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic.dispose();
                currentMusic = null;
            }

            fadingOut = false;
            fadeTimer = 0f;

            if (pendingTrackPath != null) {
                startTrack(pendingTrackPath, pendingLooping);
                pendingTrackPath = null;
            }
        }
    }

    public void stopMusic() {
        fadingOut = false;
        fadeTimer = 0f;
        pendingTrackPath = null;

        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }

        currentTrackPath = "";
    }

    public void dispose() {
        stopMusic();
        for (Sound sound : sfxCache.values()) {
            sound.dispose();
        }
        sfxCache.clear();
    }

    public void setSfxEnabled(boolean sfxEnabled) {
        this.sfxEnabled = sfxEnabled;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = clamp01(sfxVolume);
    }

    public void playSfx(String path) {
        playSfx(path, 1f);
    }

    public void playSfx(String path, float volumeScale) {
        if (!sfxEnabled || path == null || path.isEmpty()) {
            return;
        }
        try {
            Sound sound = sfxCache.get(path);
            if (sound == null) {
                sound = Gdx.audio.newSound(Gdx.files.internal(path));
                sfxCache.put(path, sound);
            }
            sound.play(clamp01(sfxVolume * masterVolume * volumeScale));
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Failed to play SFX: " + path, e);
        }
    }

    private void startTrack(String trackPath, boolean looping) {
        if (!musicEnabled || trackPath == null || trackPath.isEmpty()) {
            return;
        }

        String resolvedPath = resolveMusicPath(trackPath);
        if (resolvedPath == null) {
            Gdx.app.error("AudioManager", "Music track not found: " + trackPath);
            return;
        }

        try {
            Music music = Gdx.audio.newMusic(Gdx.files.internal(resolvedPath));
            music.setLooping(looping);
            music.setVolume(baseVolume * masterVolume);
            music.play();

            currentMusic = music;
            currentTrackPath = resolvedPath;
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Failed to play music: " + resolvedPath, e);
            currentMusic = null;
            currentTrackPath = "";
        }
    }

    private String resolveMusicPath(String requestedPath) {
        String normalized = requestedPath.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String directory = slash >= 0 ? normalized.substring(0, slash + 1) : "";
        String fileName = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;

        java.util.ArrayList<String> candidates = new java.util.ArrayList<>();
        addMusicCandidate(candidates, normalized);
        addMusicCandidate(candidates, directory + base + ".mp3");
        addMusicCandidate(candidates, directory + base + ".MP3");
        addMusicCandidate(candidates, directory + base + ".wav");
        addMusicCandidate(candidates, directory + base + ".WAV");

        if (directory.equalsIgnoreCase("music/")) {
            for (String folder : new String[]{"music/", "Music/", "MUSIC/"}) {
                addMusicCandidate(candidates, folder + base + ".mp3");
                addMusicCandidate(candidates, folder + base + ".MP3");
                addMusicCandidate(candidates, folder + base + ".wav");
                addMusicCandidate(candidates, folder + base + ".WAV");
            }
        }

        for (String candidate : candidates) {
            FileHandle handle = Gdx.files.internal(candidate);
            if (handle.exists() && handle.length() > 0) {
                return candidate;
            }
        }

        return null;
    }

    private void addMusicCandidate(java.util.List<String> candidates, String candidate) {
        if (candidate != null && !candidate.isEmpty() && !candidates.contains(candidate)) {
            candidates.add(candidate);
        }
    }

    private float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
