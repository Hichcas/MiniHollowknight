package Controller;

import Model.Knight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

public class KnightController {
    private final Knight knight;
    private final Preferences prefs;

    public KnightController(Knight knight) {
        this.knight = knight;
        this.prefs = Gdx.app.getPreferences("MyGameSettings");
    }

    private int getJumpKey() {
        return prefs.getInteger("key_JUMP", Input.Keys.SPACE);
    }

    private int getAttackKey() {
        return prefs.getInteger("key_ATTACK", Input.Keys.X);
    }

    private int getLeftKey() {
        return prefs.getInteger("key_LEFT", Input.Keys.A);
    }

    private int getRightKey() {
        return prefs.getInteger("key_RIGHT", Input.Keys.D);
    }

    private int getDashKey() {
        return prefs.getInteger("key_DASH", Input.Keys.R);
    }

    private int getFocusKey() {
        return prefs.getInteger("key_FOCUS", Input.Keys.F);
    }

    private int getUpKey() {
        return prefs.getInteger("key_UP", Input.Keys.W);
    }

    private int getDownKey() {
        return prefs.getInteger("key_DOWN", Input.Keys.S);
    }

    private int getSpellKey() {
        return prefs.getInteger("key_SPELL", Input.Keys.Q);
    }

    public void update(float delta) {
        if (knight.isDead()) {
            return;
        }

        handleMovement();
        handleJump();
        handleDash();
        handleAttack();
        handleFocus();
        handleSpellCast();
    }

    private void handleMovement() {
        if (Gdx.input.isKeyPressed(getUpKey())) {
            knight.lookUp();
        } else if (Gdx.input.isKeyPressed(getDownKey())) {
            knight.lookDown();
        } else {
            knight.clearLook();
        }

        if (Gdx.input.isKeyPressed(getLeftKey())) {
            knight.moveLeft();
        } else if (Gdx.input.isKeyPressed(getRightKey())) {
            knight.moveRight();
        } else {
            knight.stopHorizontalIfGrounded();
        }
    }

    private void handleJump() {
        if (Gdx.input.isKeyJustPressed(getJumpKey())) {
            knight.jump();
        }

        if (!Gdx.input.isKeyPressed(getJumpKey())) {
            knight.cutJumpIfReleased();
        }
    }

    private void handleDash() {
        if (Gdx.input.isKeyJustPressed(getDashKey())) {
            knight.dash();
        }
    }

    private void handleAttack() {

        if (!Gdx.input.isKeyJustPressed(getAttackKey()))
            return;

        if (Gdx.input.isKeyPressed(getUpKey())) {
            knight.attackUp();
        } else if (Gdx.input.isKeyPressed(getDownKey())) {
            knight.attackDown();
        } else {
            knight.attack();
        }
    }

    private void handleFocus() {

        if (Gdx.input.isKeyJustPressed(getFocusKey())) {
            knight.startFocus();
        }

        if (!Gdx.input.isKeyPressed(getFocusKey())) {
            knight.cancelFocus();
        }
    }

    private void handleSpellCast() {
        if (!Gdx.input.isKeyJustPressed(getSpellKey())) {
            return;
        }

        if (Gdx.input.isKeyPressed(getUpKey())) {
            knight.castHowlingWraiths();
        } else {
            knight.castVengefulSpirit();
        }
    }
}
