package Model;

import com.badlogic.gdx.math.Rectangle;

public class SecretRoom {
    private final String id;
    private final Rectangle bounds;
    private boolean revealed = false;

    public SecretRoom(String id, Rectangle bounds) {
        this.id = id;
        this.bounds = bounds;
    }

    public String getId() {
        return id;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void reveal() {
        this.revealed = true;
    }
}
