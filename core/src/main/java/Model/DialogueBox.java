package Model;

import java.util.List;

public class DialogueBox {
    private List<String> lines;
    private int visibleChars = 0;
    private boolean visible = false;
    private boolean lineFinished = false;
    private boolean lineJustChanged = false;
    private int lineIndex = 0;
    private float charTimer = 0f;
    private final float charInterval = 0.03f;

    private void resetLine() {
        charTimer = 0f;
        visibleChars = 0;
        lineFinished = false;
        lineJustChanged = true;
    }

    public void open(List<String> newLines) {
        this.lines = newLines;
        this.lineIndex = 0;
        this.visible = true;
        resetLine();
    }

    public void close() {
        visible = false;
        lines = null;
        lineIndex = 0;
    }

    public boolean consumeLineChangedFlag() {
        boolean value = lineJustChanged;
        lineJustChanged = false;
        return value;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isLastLine() {
        return lines != null && lineIndex == lines.size() - 1;
    }

    public String getVisibleText() {
        if (lines == null || lineIndex >= lines.size()) {
            return "";
        }
        String full = lines.get(lineIndex);
        return full.substring(0, Math.min(visibleChars, full.length()));
    }

    public void advanceOrSkip() {
        if (!visible || lines == null) {
            return;
        }

        String full = lines.get(lineIndex);
        if (!lineFinished) {
            visibleChars = full.length();
            lineFinished = true;
            return;
        }

        lineIndex++;
        if (lineIndex >= lines.size()) {
            close();
        } else {
            resetLine();
        }
    }

    public void update(float delta) {
        if (!visible || lines == null || lineIndex >= lines.size() || lineFinished) {
            return;
        }

        charTimer += delta;
        String full = lines.get(lineIndex);
        while (charTimer >= charInterval && visibleChars < full.length()) {
            charTimer -= charInterval;
            visibleChars++;
        }
        if (visibleChars >= full.length()) {
            lineFinished = true;
        }
    }
}
