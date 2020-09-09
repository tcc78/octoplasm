package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.octoplasm.util.AudioController;
import edu.cornell.gdiac.octoplasm.util.ScreenListener;

public class TransitionController implements Screen {

    private enum TransitionState {
        FADE_IN,
        HOLD,
        FADE_OUT,
        DONE
    }

    private static final String BLACK_BACKGROUND = "backgrounds/black.png";
    /**  */
    private static final String TRANSITION = "sounds/gameplay/transition.wav";


    private static final float SCREEN_FADE_SECONDS = 0.3f;
    private static final float SCREEN_BLACK_SECONDS = 0.3f;


    public SuperController screen1;
    public SuperController screen2;
    public boolean rightToLeft;

    private float counter = 0;
    private boolean active = false;
    private TransitionState currentState = TransitionState.DONE;
    private Texture blackBackground;
    private GameCanvas canvas;
    private ScreenListener listener;
    private Vector2 cache = new Vector2();

    public TransitionController() {
        screen1 = null;
        screen2 = null;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Preloads the assets for this controller.
     *
     * @param manager Reference to global asset manager.
     */
    public void preLoadContent(AssetManager manager) {
        manager.load(BLACK_BACKGROUND, Texture.class);
    }

    /**
     * Loads the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(AssetManager manager) {
        blackBackground = manager.get(BLACK_BACKGROUND, Texture.class);
    }

    /**
     * Unloads the assets for this game.
     *
     * This method erases the static variables.  It also deletes the associated textures
     * from the asset manager. If no assets are loaded, this method does nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void unloadContent(AssetManager manager) {
        if (manager.isLoaded(BLACK_BACKGROUND)) {
            manager.unload(BLACK_BACKGROUND);
        }
    }

    //=========================================================================
    //#region Screen Interface Methods
    @Override
    public void show() {
        active = true;
        counter = 0;
        currentState = TransitionState.FADE_IN;
        canvas.setDisableUI(true);
        screen1.showTransition();
    }

    @Override
    public void render(float delta) {
        if (active) {
            AudioController audio = AudioController.getInstance();
            //Cursor stuff
            InputController input = InputController.getInstance();
            input.readInput(null, null);
            if (input.didPressLeft()) {
                listener.setCursor(ScreenListener.CursorState.CLICKED);
            } else if (input.didReleaseLeft()) {
                listener.setCursor(ScreenListener.CursorState.DEFAULT);
            }

            //Screen stuff
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            counter += delta;
            //State changes
            if (currentState == TransitionState.FADE_IN && counter > SCREEN_FADE_SECONDS) {
                screen1.hide();
                currentState = TransitionState.HOLD;
                screen2.showTransition();
                counter -= SCREEN_FADE_SECONDS;
            } else if (currentState == TransitionState.HOLD && counter > SCREEN_BLACK_SECONDS) {
                audio.playSound("transition", TRANSITION, false); //Transition sound
                currentState = TransitionState.FADE_OUT;
                counter -= SCREEN_BLACK_SECONDS;
            } else if (currentState == TransitionState.FADE_OUT && counter > SCREEN_FADE_SECONDS) {
                currentState = TransitionState.DONE;
            }

            //Drawing
            cache.set(canvas.getCameraPosInScreen())
                    .sub(canvas.getViewport().getWorldWidth()/2, canvas.getViewport().getWorldHeight()/2)
                    .sub(canvas.getCameraMoveX(), canvas.getCameraMoveY());
            switch (currentState) {
            case FADE_IN:
                screen1.transitionDraw(delta);
                canvas.begin();
                if (rightToLeft)
                    canvas.draw(blackBackground, Color.WHITE, -width + (counter/SCREEN_FADE_SECONDS * width) + cache.x, cache.y, width, height);
                else
                    canvas.draw(blackBackground, Color.WHITE, width - (counter/SCREEN_FADE_SECONDS * width) + cache.x, cache.y, width, height);
                canvas.end();
                break;
            case HOLD:
                canvas.begin();
                canvas.draw(blackBackground, Color.WHITE, cache.x, cache.y, width, height);
                canvas.end();
                break;
            case FADE_OUT:
                screen2.transitionDraw(delta);
                canvas.begin();
                if (rightToLeft)
                    canvas.draw(blackBackground, Color.WHITE, 0 + ((counter % SCREEN_FADE_SECONDS)/SCREEN_FADE_SECONDS * width) + cache.x, cache.y, width, height);
                else
                    canvas.draw(blackBackground, Color.WHITE, 0 - ((counter % SCREEN_FADE_SECONDS)/SCREEN_FADE_SECONDS * width) + cache.x, cache.y, width, height);
                canvas.end();
                break;
            case DONE:
                listener.exitScreen(this, 0);
                break;
            }
            AudioController.getInstance().update();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        active = false;
        screen1 = null;
        screen2 = null;
        rightToLeft = false;
        canvas.setDisableUI(false);
    }

    @Override
    public void dispose() {
        if (blackBackground != null)
            blackBackground.dispose();
    }
    //#endregion
    //=================================
}
