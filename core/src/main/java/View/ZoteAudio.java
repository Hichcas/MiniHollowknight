package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;
import java.util.Random;

public class ZoteAudio {
    private final ArrayList<Sound> grunts = new ArrayList<>();
    private final Random random = new Random();

    private Sound currentSound = null;
    private long currentSoundId = -1;

    public ZoteAudio() {
        String[] files = {
            "sfx/zote/grunt1.WAV",
            "sfx/zote/grunt2.WAV",
            "sfx/zote/grunt3.WAV",
            "sfx/zote/grunt4.WAV"
        };
        for (String path : files) {
            if (Gdx.files.internal(path).exists()) {
                grunts.add(Gdx.audio.newSound(Gdx.files.internal(path)));
            }
        }
    }

    public void Randomization() {
        if (grunts.isEmpty()) {
            return;
        }

        stopCurrent();
        currentSound = grunts.get(random.nextInt(grunts.size()));
        currentSoundId = currentSound.play(0.8f);
    }

    public void stopCurrent() {
        if (currentSound != null && currentSoundId != -1) {
            currentSound.stop(currentSoundId);
        }
        currentSoundId = -1;
        currentSound = null;
    }

    public void dispose() {
        for (Sound s : grunts) {
            s.dispose();
        }
        grunts.clear();
    }
}
