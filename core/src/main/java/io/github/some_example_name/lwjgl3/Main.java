package io.github.some_example_name.lwjgl3;

import View.Screen.MainMenuScreen;
import com.badlogic.gdx.Game;

public class Main extends Game {
    @Override
    public void create() {
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }
}
