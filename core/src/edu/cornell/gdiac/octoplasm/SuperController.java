package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.Screen;

public abstract class SuperController implements Screen {
    /**
     * On screen transitions, this method draws the screen without updating anything.
     *
     * @param delta Number of seconds since last update
     */
    public abstract void transitionDraw(float delta);

    public abstract void showTransition();
}
