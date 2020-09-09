package edu.cornell.gdiac.octoplasm;

/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do
 * anything until loading is complete. You know those loading screens with the inane tips
 * that want to be helpful?  That is asynchronous loading.
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the
 * start of the game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */


import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.octoplasm.util.FilmStrip;
import edu.cornell.gdiac.octoplasm.util.ScreenListener;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LoadingMode extends SuperController implements Screen, InputProcessor, ControllerListener {

    //=========================================================================
    //#region Fields
    // Textures necessary to support the loading screen
    private enum ScreenState {
        LOGO,
        LOADING_BAR
    }

    private static final String BACKGROUND_FILE = "backgrounds/loading.png";
    private static final String PROGRESS_FILE = "ui/loading/progressbar.png";
    private static final String PLAY_BTN_FILE = "ui/loading/play.png";
    private static final String BLACK_BACKGROUND = "backgrounds/black.png";
    private static final String SOUP_ANIMATION = "ui/loading/soup_spritesheet.png";

    private static final int SOUP_SPRITESHEET_COLS = 5;
    private static final int SOUP_SPRITESHEET_ROWS = 2;
    private static final int SOUP_SPRITESHEET_FRAMES = 8;

    /**  */
    private static final float LOGO_SECONDS = 30;
    /**  */
    private static final float LOGO_LOOPS = 5;
    /**  */
    private static final float LOGO_ANIMATION_PERCENT = 0.6f;

    /** Default budget for asset loader (do nothing but load 60 fps) */
    private static final int DEFAULT_BUDGET = 15;
    /** Standard window size (for scaling) */
    private static final int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static final int STANDARD_HEIGHT = 700;
    /** Ratio of the bar width to the screen */
    private static final float BAR_WIDTH_RATIO  = 0.66f;
    /** Ration of the bar height to the screen */
    private static final float BAR_HEIGHT_RATIO = 0.18f;

    /** Height of the progress bar */
    private static int PROGRESS_HEIGHT = 30;
    /** Width of the rounded cap on left or right */
    private static int PROGRESS_CAP    = 15;

    /** Width of the middle portion in texture atlas */
    private static final int PROGRESS_MIDDLE = 200;

    /** Amount to scale the play button */
    private static float BUTTON_SCALE  = 0.75f;
    /** Start button for XBox controller on Windows */
    private static final int WINDOWS_START = 7;
    /** Start button for XBox controller on Mac OS X */
    private static final int MAC_OS_X_START = 4;

    /** Background texture for start-up */
    private Texture background;
    /**  */
    private Texture blackBackground;
    /** Play button to display when done */
    private Texture playButton;
    /** Texture atlas to support a progress bar */
    private Texture statusBar;
    // statusBar is a "texture atlas." Break it up into parts.
    /** Left cap to the status background (grey region) */
    private TextureRegion statusBkgLeft;
    /** Middle portion of the status background (grey region) */
    private TextureRegion statusBkgMiddle;
    /** Right cap to the status background (grey region) */
    private TextureRegion statusBkgRight;
    /** Left cap to the status forground (colored region) */
    private TextureRegion statusFrgLeft;
    /** Middle portion of the status forground (colored region) */
    private TextureRegion statusFrgMiddle;
    /** Right cap to the status forground (colored region) */
    private TextureRegion statusFrgRight;
    /**  */
    private FilmStrip soupAnimation;

    /** Track all loaded assets (for unloading purposes). */
    private Array<String> assets;
    /** AssetManager to be loading in the background */
    private AssetManager manager;
    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** The width of the progress bar */
    private int width;
    /** The y-coordinate of the center of the progress bar */
    private int centerY;
    /** The x-coordinate of the center of the progress bar */
    private int centerX;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;
    /** Current progress (0 to 1) of the asset manager */
    private float progress;
    /** The current state of the play button */
    private int   pressState;
    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int   budget;
    /** Support for the X-Box start button in place of play button */
    private int   startButton;
    /** Whether or not this player mode is still active */
    private boolean active;
    /**  */
    private ScreenState screenState;
    /**  */
    private float logoCounter;
    /**  */
    private Color soupColor;
    /**  */
    private Table playTable;
    /**  */
    private TextureRegion play_button;
    /**  */
    private boolean tabling = true;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a LoadingMode with the default budget, size and position.
     *
     * @param manager The AssetManager to load in the background
     */
    public LoadingMode(GameCanvas canvas, AssetManager manager) {
        this(canvas, manager,DEFAULT_BUDGET);
    }

    /**
     * Creates a LoadingMode with the default size and position.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param manager The AssetManager to load in the background
     * @param millis The loading budget in milliseconds
     */
    public LoadingMode(GameCanvas canvas, AssetManager manager, int millis) {
        this.manager = manager;
        this.canvas  = canvas;
        budget = millis;
        screenState = ScreenState.LOADING_BAR;
//        screenState = ScreenState.LOGO;
        logoCounter = 0;
        assets = new Array<>();

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        // Load the next images immediately.
        playButton = null;
        background = new Texture(BACKGROUND_FILE);
        statusBar  = new Texture(PROGRESS_FILE);
        blackBackground = new Texture(BLACK_BACKGROUND);

        soupAnimation = new FilmStrip(new Texture(SOUP_ANIMATION),SOUP_SPRITESHEET_ROWS,SOUP_SPRITESHEET_COLS,SOUP_SPRITESHEET_FRAMES);
        soupAnimation.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        soupColor = new Color(Color.WHITE);

        // No progress so far.
        progress   = 0;
        pressState = 0;
        active = false;

        // Break up the status bar texture into regions
        statusBkgLeft   = new TextureRegion(statusBar,0,0,PROGRESS_CAP,PROGRESS_HEIGHT);
        statusBkgRight  = new TextureRegion(statusBar,statusBar.getWidth()-PROGRESS_CAP,0,PROGRESS_CAP,PROGRESS_HEIGHT);
        statusBkgMiddle = new TextureRegion(statusBar,PROGRESS_CAP,0,PROGRESS_MIDDLE,PROGRESS_HEIGHT);

        int offset = statusBar.getHeight()-PROGRESS_HEIGHT;
        statusFrgLeft   = new TextureRegion(statusBar,0,offset,PROGRESS_CAP,PROGRESS_HEIGHT);
        statusFrgRight  = new TextureRegion(statusBar,statusBar.getWidth()-PROGRESS_CAP,offset,PROGRESS_CAP,PROGRESS_HEIGHT);
        statusFrgMiddle = new TextureRegion(statusBar,PROGRESS_CAP,offset,PROGRESS_MIDDLE,PROGRESS_HEIGHT);

        startButton = (System.getProperty("os.name").equals("Mac OS X") ? MAC_OS_X_START : WINDOWS_START);
        Gdx.input.setInputProcessor(this);
        // Let ANY connected controller start the game.
//        for(Controller controller : Controllers.getControllers()) {
//            controller.addListener(this);
//        }
        active = true;
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**
     * Returns the budget for the asset loader.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @return the budget in milliseconds
     */
    public int getBudget() {
        return budget;
    }

    /**
     * Sets the budget for the asset loader.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param millis the budget in milliseconds
     */
    public void setBudget(int millis) {
        budget = millis;
    }

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 2;
    }
    //#endregion
    //=================================

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        statusBkgLeft = null;
        statusBkgRight = null;
        statusBkgMiddle = null;

        statusFrgLeft = null;
        statusFrgRight = null;
        statusFrgMiddle = null;

        background.dispose();
        statusBar.dispose();
        background = null;
        statusBar  = null;
        if (playButton != null) {
            playButton.dispose();
            playButton = null;
        }
    }

    /**
     * Preloads the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void preLoadContent(AssetManager manager) {
        manager.load(BACKGROUND_FILE, Texture.class);
        assets.add(BACKGROUND_FILE);
        manager.load(PLAY_BTN_FILE, Texture.class);
        assets.add(PLAY_BTN_FILE);

        manager.finishLoading();

        createPlayTable(manager);
    }

    /**
     *
     * @param manager
     */
    public void createPlayTable(AssetManager manager) {
        playTable = new Table();
        playTable.setHeight(heightY);
        playTable.setWidth(centerX*2f);
        playTable.center();

//        TextureRegionDrawable settingsBG = new TextureRegionDrawable(manager.get(BACKGROUND_FILE, Texture.class));
//        playTable.setBackground(settingsBG);

        Texture buttonTexture = manager.get(PLAY_BTN_FILE, Texture.class);
        ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(buttonTexture);
        buttonStyle.imageOver = new TextureRegionDrawable(buttonTexture).tint(Color.LIGHT_GRAY);
        ImageButton play_btn = new ImageButton(buttonStyle);
        play_btn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pressState = 2;
            }
        });

        playTable.add(play_btn).center().padTop(play_btn.getHeight()*4f);
    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        InputController input = InputController.getInstance();
        input.readInput(null, null);
        if (input.didPressLeft()) {
            listener.setCursor(ScreenListener.CursorState.CLICKED);
        } else if (input.didReleaseLeft()) {
            listener.setCursor(ScreenListener.CursorState.DEFAULT);
        }
        if (playButton == null) {
            manager.update(budget);
            this.progress = manager.getProgress();
            if (progress >= 1.0f) {
                this.progress = 1.0f;
                playButton = new Texture(PLAY_BTN_FILE);
                playButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            }
        }
        switch (screenState) {
        case LOGO:
            logoCounter += delta;
            if (logoCounter < LOGO_SECONDS - LOGO_SECONDS * LOGO_ANIMATION_PERCENT) {
                int frame = (int) ((logoCounter / (LOGO_SECONDS * LOGO_ANIMATION_PERCENT)) * (SOUP_SPRITESHEET_FRAMES * LOGO_LOOPS)) % SOUP_SPRITESHEET_FRAMES;
                soupAnimation.setFrame(frame);
            } else {
                float alphaChange = (LOGO_SECONDS - (LOGO_SECONDS - LOGO_SECONDS * LOGO_ANIMATION_PERCENT)) * delta;
                soupColor.sub(0,0,0,alphaChange);
            }
            if (logoCounter > LOGO_SECONDS || input.didClick() || input.didFreeze())
                screenState = ScreenState.LOADING_BAR;
            break;
        case LOADING_BAR:
            logoCounter += delta;
            if (logoCounter < LOGO_SECONDS - LOGO_SECONDS * LOGO_ANIMATION_PERCENT) {
//                int frame = (int) ((logoCounter / (LOGO_SECONDS * LOGO_ANIMATION_PERCENT)) * (SOUP_SPRITESHEET_FRAMES * LOGO_LOOPS)) % SOUP_SPRITESHEET_FRAMES;
//                soupAnimation.setFrame(frame);
            } else {
//                float alphaChange = (LOGO_SECONDS - (LOGO_SECONDS - LOGO_SECONDS * LOGO_ANIMATION_PERCENT)) * delta;
//                soupColor.sub(0,0,0,alphaChange);
            }
            break;
        }
    }

    @Override
    public void transitionDraw(float delta) {
        draw(delta);
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw(float delta) {
        canvas.begin();
        switch (screenState) {
        case LOGO:
            canvas.draw(blackBackground, Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.draw(soupAnimation, soupColor, soupAnimation.getRegionWidth()/2f, soupAnimation.getRegionHeight()/2f,
                    canvas.getWidth()/2f, canvas.getHeight()/2f, 0, 0.5f, 0.5f);
            break;
        case LOADING_BAR:
//            canvas.draw(blackBackground, Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());
//            canvas.draw(soupAnimation, soupColor, soupAnimation.getRegionWidth()/2f, soupAnimation.getRegionHeight()/2f,
//                    canvas.getWidth()/2f, canvas.getHeight()/2f, 0, 0.5f, 0.5f);
            canvas.draw(background, Color.WHITE, 0, 0,canvas.getWidth()*scale, canvas.getHeight()*scale);
            if (playButton == null) {
                drawProgress(canvas);
            } else if (tabling) {
//                Color tint = (pressState == 1 ? Color.GRAY : Color.WHITE);
//                canvas.draw(playButton, tint, playButton.getWidth() / 2, playButton.getHeight() / 2,
//                        centerX, centerY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
//                createPlayTable(manager);
                canvas.addTable(playTable);
                playTable.setVisible(true);
                canvas.setStageAsInputProcessor();
                tabling = false;
            }
            break;
        }
        canvas.end();
        canvas.drawStage(delta);
    }

    /**
     * Updates the progress bar according to loading progress
     *
     * The progress bar is composed of parts: two rounded caps on the end,
     * and a rectangle in a middle.  We adjust the size of the rectangle in
     * the middle to represent the amount of progress.
     *
     * @param canvas The drawing context
     */
    private void drawProgress(GameCanvas canvas) {
        canvas.draw(statusBkgLeft,   Color.WHITE, centerX-width/2f, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
        canvas.draw(statusBkgRight,  Color.WHITE, centerX+width/2f-scale*PROGRESS_CAP, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
        canvas.draw(statusBkgMiddle, Color.WHITE, centerX-width/2f+scale*PROGRESS_CAP, centerY, width-2*scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
        canvas.draw(statusFrgLeft,   Color.WHITE, centerX-width/2f, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
        if (progress > 0) {
            float span = progress*(width-2*scale*PROGRESS_CAP);
            canvas.draw(statusFrgRight,  Color.WHITE, centerX-width/2f+scale*PROGRESS_CAP+span, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
            canvas.draw(statusFrgMiddle, Color.WHITE, centerX-width/2f+scale*PROGRESS_CAP, centerY, span, scale*PROGRESS_HEIGHT);
        } else {
            canvas.draw(statusFrgRight,  Color.WHITE, centerX-width/2f+scale*PROGRESS_CAP, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
        }
    }

    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);

            // We are are ready, notify our listener
            if (isReady() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)canvas.getWidth())/STANDARD_WIDTH;
        float sy = ((float)canvas.getHeight())/STANDARD_HEIGHT;
        scale = Math.min(sx, sy);

        this.width = (int)(BAR_WIDTH_RATIO*canvas.getWidth());
        centerY = (int)(BAR_HEIGHT_RATIO*canvas.getHeight());
        centerX = canvas.getWidth()/2;
        heightY = canvas.getHeight();
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    @Override
    public void showTransition() {

    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (playButton == null || pressState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;
        screenX *= canvas.getViewport().getScreenWidth()/canvas.getViewport().getWorldWidth();
        screenY *= canvas.getViewport().getScreenHeight()/canvas.getViewport().getWorldHeight();

        // TODO: Fix scaling
        // Play button is a circle.
//        float radius = BUTTON_SCALE*scale*playButton.getWidth()/2.0f;
//        float dist = (screenX-centerX)*(screenX-centerX)+(screenY-centerY)*(screenY-centerY);
//        if (dist < radius*radius) {
//            pressState = 1;
//        }

        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressState == 1) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        if (buttonCode == startButton && pressState == 0) {
            pressState = 1;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
        if (pressState == 1 && buttonCode == startButton) {
            pressState = 2;
            return false;
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param character the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released.
     *
     * We allow key commands to start the game this time.
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.N || keycode == Input.Keys.P) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param amount the amount of scroll from the wheel
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(int amount) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }

    /**
     * Called when a POV on the Controller moved. (UNSUPPORTED)
     *
     * The povCode is controller specific. The value is a cardinal direction.
     *
     * @param controller The game controller
     * @param povCode 	The POV controller moved
     * @param value 	The direction of the POV
     * @return whether to hand the event to other listeners.
     */
    public boolean povMoved (Controller controller, int povCode, PovDirection value) {
        return true;
    }

    /**
     * Called when an x-slider on the Controller moved. (UNSUPPORTED)
     *
     * The x-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when a y-slider on the Controller moved. (UNSUPPORTED)
     *
     * The y-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when an accelerometer value on the Controller changed. (UNSUPPORTED)
     *
     * The accelerometerCode is controller specific. The value is a Vector3 representing
     * the acceleration on a 3-axis accelerometer in m/s^2.
     *
     * @param controller The game controller
     * @param accelerometerCode The accelerometer adjusted
     * @param value A vector with the 3-axis acceleration
     * @return whether to hand the event to other listeners.
     */
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return true;
    }

}