package Controller;

import Model.DialogueBox;
import Model.Enums.ZoteState;
import Model.Knight;
import Model.Zote;
import View.ZoteAudio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ZoteController {
    private final Zote zote;
    private final DialogueBox dialogueBox;
    private final ZoteAudio audio;
    private final Random random = new Random();

    public ZoteController(Zote zote, DialogueBox dialogueBox, ZoteAudio audio) {
        this.zote = zote;
        this.dialogueBox = dialogueBox;
        this.audio = audio;
    }

    public boolean update(float delta, Knight knight) {
        if (zote.getAttackCooldownTimer() > 0f) {
            zote.setAttackCooldownTimer(zote.getAttackCooldownTimer() - delta);
        }
        switch (zote.getState()) {
            case Fall:
                zote.setFallingTimer(zote.getFallingTimer() - delta);
                if (zote.getFallingTimer() <= 0f) {
                    zote.getBody().setLinearVelocity(0f, zote.getBody().getLinearVelocity().y);
                    zote.setState(ZoteState.GetUp);
                    zote.setGetUpTimer(zote.getGetUpDuration());
                }
                return false;
            case GetUp:
                zote.setGetUpTimer(zote.getGetUpTimer() - delta);
                if (zote.getGetUpTimer() <= 0f) {
                    zote.setState(ZoteState.Turn);
                    zote.setTurnTimer(zote.getTurnDuration());
                }
                return false;
            case Turn:
                boolean faceRight = knight.getBody().getPosition().x > zote.getPosition().x;
                zote.setFacingRight(faceRight);
                zote.setTurnTimer(zote.getTurnTimer() - delta);
                if (zote.getTurnTimer() <= 0f) {
                    zote.setState(ZoteState.Roll);
                    zote.setRollTimer(zote.getRollDuration());
                    zote.getBody().setLinearVelocity((faceRight ? 1f : -1f) *
                        zote.getRollSpeed(), zote.getBody().getLinearVelocity().y);
                }
                return false;
            case Roll:
                zote.setRollTimer(zote.getRollTimer() - delta);
                if (zote.getRollTimer() <= 0f) {
                    zote.getBody().setLinearVelocity(0f, zote.getBody().getLinearVelocity().y);
                    zote.setState(ZoteState.Attack);
                    zote.setAngerTimer(zote.getAngerDuration());
                }
                return false;
            case Attack:
                zote.setAngerTimer(zote.getAngerTimer() - delta);
                Vector2 zotePos = zote.getPosition();
                Vector2 knightPos = knight.getBody().getPosition();

                float dir = Math.signum(knightPos.x - zotePos.x);
                if (dir == 0f) {
                    dir = zote.isFacingRight() ? 1f : -1f;
                }
                zote.setFacingRight(dir > 0f);

                zote.getBody().setLinearVelocity(dir * zote.getAttackMoveSpeed(), zote.getBody().getLinearVelocity().y);
                if (zote.getAngerTimer() <= 0f) {
                    zote.getBody().setLinearVelocity(0f, zote.getBody().getLinearVelocity().y);
                    zote.setState(ZoteState.Idle);
                }
                return false;
            case Idle:
                float dist = knight.getBody().getPosition().dst(zote.getPosition());
                zote.setPlayerNearby(dist <= zote.getInteractionRange());

                handleKnightHitsZote(knight);

                if (zote.getState() == ZoteState.Fall) {
                    return false;
                }

                boolean playerOnRight = knight.getBody().getPosition().x > zote.getPosition().x;
                zote.setFacingRight(playerOnRight);

                boolean interactKey = Gdx.input.isKeyJustPressed(Input.Keys.W);

                if (zote.isPlayerNearby() && interactKey) {
                    startDialogue();
                    return true;
                }

                return false;
            case Talk:
                dialogueBox.update(delta);

                if (dialogueBox.consumeLineChangedFlag()) {
                    audio.Randomization();
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    dialogueBox.close();
                    audio.stopCurrent();
                    zote.setState(ZoteState.Idle);
                    return true;
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    dialogueBox.advanceOrSkip();

                    if (!dialogueBox.isVisible()) {
                        audio.stopCurrent();
                        zote.setMainDialogueFinished(true);
                        zote.setState(ZoteState.Idle);
                    }
                }

                return true;
            default:
                return false;
        }

    }

    private void startDialogue() {
        zote.setState(ZoteState.Talk);

        List<String> mainDialogue = isSpanish() ? MAIN_DIALOGUE_ES : MAIN_DIALOGUE_EN;
        List<String> precepts = isSpanish() ? PRECEPTS_ES : PRECEPTS_EN;

        if (!zote.isMainDialogueFinished()) {
            dialogueBox.open(mainDialogue);
            return;
        }

        int idx;
        if (precepts.size() == 1) {
            idx = 0;
        } else {
            do {
                idx = random.nextInt(precepts.size());
            } while (idx == zote.getLastPreceptIndex());
        }

        zote.setLastPreceptIndex(idx);
        dialogueBox.open(Collections.singletonList(precepts.get(idx)));
    }

    private boolean isSpanish() {
        Preferences prefs = Gdx.app.getPreferences("MyGameSettings");
        return "ES".equals(prefs.getString("lang", "EN"));
    }

    public static final List<String> MAIN_DIALOGUE_EN = Arrays.asList(
        "Halt! Before you take one more step, know who stands before you.",
        "I have fought in battles whose names are long forgotten, and walked away from fields where no other army survived.",
        "My nail has broken a thousand times and been remade a thousand more - for a true warrior never yields, even when all seems lost.",
        "Go now, and tell everyone you meet: the great Zote still stands."
    );

    public static final List<String> PRECEPTS_EN = Arrays.asList(
        "Precept the First: Never turn your back on a foe standing above you.",
        "Precept the Fourth: Fear is only your enemy once you surrender to it.",
        "Precept the Seventh: One precise strike is worth more than a thousand hurried ones.",
        "Precept the Ninth: The warrior who learns when not to fight outlasts the one who only learns how.",
        "Precept the Fourteenth: Any wound that does not end you only forges you for the next battle.",
        "Precept the Seventeenth: A name remembered from the battlefield outlives a life spent in silence.",
        "Precept the Twenty-Third: Never turn your back on any foe, however weak - pride is a hero's greatest enemy.",
        "Precept the Thirtieth: He who does not fear defeat is never truly defeated."
    );

    public static final List<String> MAIN_DIALOGUE_ES = Arrays.asList(
        "¡Alto! Antes de dar un paso más, sepas ante quién te encuentras.",
        "He luchado en batallas cuyos nombres ya se han olvidado, y he sobrevivido a campos donde ningún otro ejército quedó en pie.",
        "Mi hoja se ha roto mil veces y se ha forjado mil veces más - porque un verdadero guerrero jamás se rinde, ni cuando todo parece perdido.",
        "Ve ahora, y dile a todo aquel que encuentres: el gran Zote sigue de pie."
    );

    public static final List<String> PRECEPTS_ES = Arrays.asList(
        "Precepto primero: Nunca le des la espalda a un enemigo que está por encima de ti.",
        "Precepto cuarto: El miedo solo es tu enemigo cuando te rindes ante él.",
        "Precepto séptimo: Un golpe certero vale más que mil golpes apresurados.",
        "Precepto noveno: El guerrero que aprende cuándo no luchar dura más que el que solo aprende cómo hacerlo.",
        "Precepto decimocuarto: Toda herida que no te destruye te forja para la siguiente batalla.",
        "Precepto decimoséptimo: Un nombre recordado en el campo de batalla perdura más que una vida vivida en silencio.",
        "Precepto vigésimo tercero: Nunca le des la espalda a ningún enemigo, por débil que sea - el orgullo es el mayor enemigo de un héroe.",
        "Precepto trigésimo: Quien no teme la derrota, jamás es verdaderamente derrotado."
    );

    private void handleKnightHitsZote(Knight knight) {
        if (dialogueBox.isVisible() || zote.getAttackCooldownTimer() > 0f || !knight.isAttacking()) {
            return;
        }
        if (!knight.getAttackBounds().overlaps(zote.getBounds())) {
            return;
        }

        dialogueBox.close();
        audio.stopCurrent();

        float dir = Math.signum(zote.getPosition().x - knight.getBody().getPosition().x);
        if (dir == 0f) {
            dir = zote.isFacingRight() ? -1f : 1f;
        }
        zote.getBody().setLinearVelocity(0f, 0f);
        zote.getBody().applyLinearImpulse(new Vector2(dir * 4.5f, 4f), zote.getBody().getWorldCenter(), true);
        zote.setState(ZoteState.Fall);
        zote.setFallingTimer(zote.getFallDuration());
        zote.setAttackCooldownTimer(zote.getFallDuration() + zote.getGetUpDuration() + zote.getTurnDuration()
            + zote.getRollDuration() + zote.getAngerDuration() + 1f
        );
    }

}
