package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.esotericsoftware.spine.Animation;

public class InputController {

    //=========================================================================
    //#region Fields
    // Sensitivity for moving crosshair with gameplay
    /** TODO documentation */
    private static final float GP_ACCELERATE = 1.0f;
    /** TODO documentation */
    private static final float GP_MAX_SPEED  = 10.0f;
    /** TODO documentation */
    private static final float GP_THRESHOLD  = 0.01f;

    /** The number of pixels away from the game screen that we allow the mouse to be read as "at the screen boundary" */
    private static final float SCREEN_PANNING_OFFSET = 20f;

    /** The singleton instance of the input controller */
    private static InputController theController = null;

    /**  */
    private GameCanvas canvas;

    // Fields to manage buttons
    /** Whether the reset button was pressed. */
    private boolean resetPressed;
    private boolean resetPrevious;
    /** Whether the button to advanced worlds was pressed. */
    private boolean nextPressed;
    private boolean nextPrevious;
    /** Whether the button to step back worlds was pressed. */
    private boolean prevPressed;
    private boolean prevPrevious;
    /** Whether a mouse button was pressed. */
    private boolean clickPressed;
    private boolean clickPrevious;
    /** Whether the left click of the mouse is pressed */
    private boolean leftMousePressed;
    private boolean leftMousePrevious;
    /** Whether the right click of the mouse is pressed */
    private boolean rightMousePressed;
    private boolean rightMousePrevious;
    /** Whether the control key is pressed */
    private boolean controlPressed;
    private boolean controlPrevious;
    /** Whether the debug toggle was pressed. */
    private boolean debugPressed;
    private boolean debugPrevious;
    /** Whether the select octopus one button was pressed. */
    private boolean onePressed;
    private boolean onePrevious;
    /** Whether the select octopus two button was pressed. */
    private boolean twoPressed;
    private boolean twoPrevious;
    /** Whether the select octopus three button was pressed. */
    private boolean threePressed;
    private boolean threePrevious;
    /** Whether the wall button was pressed. */
    private boolean wallPressed;
    private boolean wallPrevious;
    /** Whether the w button was pressed. */
    private boolean wPressed;
    private boolean wPrevious;
    /** Whether the a button was pressed. */
    private boolean aPressed;
    private boolean aPrevious;
    /** Whether the s button was pressed. */
    private boolean sPressed;
    private boolean sPrevious;
    /** Whether the d button was pressed. */
    private boolean dPressed;
    private boolean dPrevious;
    /** Whether the o button was pressed. */
    private boolean oPressed;
    private boolean oPrevious;
    /** Whether the b button was pressed. */
    private boolean bPressed;
    private boolean bPrevious;
    /** Whether the b button was pressed. */
    private boolean qPressed;
    private boolean qPrevious;
    /** Whether the e button was pressed. */
    private boolean enemySelectPressed;
    private boolean enemySelectPrevious;
    /** Whether the set camera button was pressed. */
    private boolean setCameraPressed;
    private boolean setCameraPrevious;
    /** Whether the set bounds button was pressed. */
    private boolean setBoundsPressed;
    private boolean setBoundsPrevious;
    /** Whether the escape button was pressed. */
    private boolean escPressed;
    private boolean escPrevious;
    /** Whether the backspace button was pressed. */
    private boolean backspacePressed;
    private boolean backspacePrevious;
    /** Whether the ability key is pressed. */
    private boolean abilityPressed;
    private boolean abilityPrevious;
    /** Whether the level edit toggle was pressed*/
    private boolean levelEditPressed;
    private boolean levelEditPrevious;
    /** Whether the save level button was pressed*/
    private boolean saveLevelPressed;
    private boolean saveLevelPrevious;
    /** Whether the load level button was pressed*/
    private boolean loadLevelPressed;
    private boolean loadLevelPrevious;
    /** Whether the space button was pressed. */
    private boolean spacePressed;
    private boolean spacePrevious;
    /** Whether the left arrow key was pressed. */
    private boolean leftPressed;
    private boolean leftPrevious;
    /** Whether the right arrow key was pressed. */
    private boolean rightPressed;
    private boolean rightPrevious;
    /** Whether the tutorial icon key was pressed. */
    private boolean tutorialPressed;
    private boolean tutorialPrevious;
    /** Whether the tutorial icon key was pressed. */
    private boolean tutorialPressedText;
    private boolean tutorialPreviousText;
    /** Whether the mouse is left */
    private boolean mouseIsLeft;
    /** Whether the mouse is right */
    private boolean mouseIsRight;
    /** Whether the mouse is up */
    private boolean mouseIsUp;
    /** Whether the mouse is down */
    private boolean mouseIsDown;
    /**  */
    private boolean catchedPrev;
    /** How much did we move horizontally? */
    private float horizontal;
    /** How much did we move vertically? */
    private float vertical;
    /** The crosshair position (for raddoll) */
    private Vector2 crosshair;
    /** The player's mouse position */
    private Vector2 mouse;
    /** The crosshair cache (for using as a return value) */
    private Vector2 crosscache;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a new input controller
     *
     * The input controller attempts to connect to the X-Box controller at device 0,
     * if it exists.  Otherwise, it falls back to the keyboard control.
     */
    public InputController() {
        // If we have a game-pad for id, then use it.
//        xbox = new XBox360Controller(0);
        crosshair = new Vector2();
        crosscache = new Vector2();
        mouse = new Vector2();
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**
     * Return the singleton instance of the input controller
     *
     * @return the singleton instance of the input controller
     */
    public static InputController getInstance() {
        if (theController == null) {
            theController = new InputController();
        }
        return theController;
    }

    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
    }

    //=========================================================================
    //#region Mouse
    /**
     * Returns the amount of sideways movement.
     *
     * -1 = left, 1 = right, 0 = still
     *
     * @return the amount of sideways movement.
     */
    public float getHorizontal() {
        return horizontal;
    }

    /**
     * Returns the amount of vertical movement.
     *
     * -1 = down, 1 = up, 0 = still
     *
     * @return the amount of vertical movement.
     */
    public float getVertical() {
        return vertical;
    }

    /**
     * Returns the current position of the crosshairs on the screen.
     *
     * This value does not return the actual reference to the crosshairs position.
     * That way this method can be called multiple times without any fair that
     * the position has been corrupted.  However, it does return the same object
     * each time.  So if you modify the object, the object will be reset in a
     * subsequent call to this getter.
     *
     * @return the current position of the crosshairs on the screen.
     */
    public Vector2 getCrossHair() {
        return crosscache.set(crosshair);
    }

    /**
     * Returns the current position of the mouse on the screen.
     *
     * This is the unscaled, unbounded position of the mouse on the game screen.
     * Does not return a reference to the actual position.
     *
     * @return the current position of the mouse on the screen.
     */
    public Vector2 getMouse() { return crosscache.set(mouse);}

    /**
     * Returns true if any mouse button has been pressed.
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didClick() {
        return clickPressed && !clickPrevious;
    }

    /**
     * Returns true if the left mouse button was just pressed down.
     *
     * @return If the left mouse button was just pressed down.
     */
    public boolean didClickLeft() {
        return leftMousePressed && !leftMousePrevious && !controlPressed;
    }

    /**
     * Returns true if the left mouse button is currently down.
     *
     * This is a sustained button. It will returns true as long as the player
     * holds it down.
     *
     * @return If the left mouse button is currently down.
     */
    public boolean didPressLeft() {
        return leftMousePressed;
    }

    /**
     * Returns true if the left mouse button was just released.
     *
     * @return If the left mouse button was just released.
     */
    public boolean didReleaseLeft() {
        return !leftMousePressed && leftMousePrevious;
    }

    /**
     * Returns true if the right mouse button was just released.
     *
     * @return If the right mouse button was just released.
     */
    public boolean didReleaseRight() {
        return !rightMousePressed && rightMousePrevious;
    }

    /**
     * Returns true if the right mouse button was just pressed down.
     *
     * @return If the right mouse button was just pressed down.
     */
    public boolean didClickRight() {
        return (rightMousePressed && !rightMousePrevious) || (leftMousePressed && controlPressed);
    }

    /**
     * Returns true if the mouse is on the top side of the screen, as given by bounds in {@link #readInput}.
     *
     * @return true if the mouse is on the top side of the screen.
     */
    public boolean isUp(){return mouseIsUp;}

    /**
     * Returns true if the mouse is on the bottom side of the screen, as given by bounds in {@link #readInput}.
     *
     * @return true if the mouse is on the bottom side of the screen.
     */
    public boolean isDown(){return mouseIsDown;}

    /**
     * Returns true if the mouse is on the bottom side of the screen, as given by bounds in {@link #readInput}.
     *
     * @return true if the mouse is on the bottom side of the screen.
     */
    public boolean isLeft(){return mouseIsLeft;}

    /**
     * Returns true if the mouse is on the right side of the screen, as given by bounds in {@link #readInput}.
     *
     * @return true if the mouse is on the right side of the screen.
     */
    public boolean isRight(){return mouseIsRight;}
    //#endregion
    //=================================

    //=========================================================================
    //#region Keyboard

    /** Returns true if control button was pressed. */
    public boolean isControlPressed() {return controlPressed;}

    /**
     * Returns true if the reset button was pressed.
     *
     * @return true if the reset button was pressed.
     */
    public boolean didReset() {
        return resetPressed && !resetPrevious;
    }

    /**
     * Returns true if the player wants to go to the next level.
     *
     * @return true if the player wants to go to the next level.
     */
    public boolean didAdvance() {
        return nextPressed && !nextPrevious;
    }

    /**
     * Returns true if the player wants to go to the previous level.
     *
     * @return true if the player wants to go to the previous level.
     */
    public boolean didRetreat() {
        return prevPressed && !prevPrevious;
    }

    /**
     * Returns true if the player wants to go toggle the debug mode.
     *
     * @return true if the player wants to go toggle the debug mode.
     */
    public boolean didDebug() {
//        return debugPressed && !debugPrevious;
        return false;
    }

    /**
     * Returns true if the select octopus one button was pressed
     *
     * @return true if the select octopus one button was pressed
     */
    public boolean didSelectOne() {return onePressed && !onePrevious;}

    /**
     * Returns true if the select octopus two button was pressed
     *
     * @return true if the select octopus two button was pressed
     */
    public boolean didSelectTwo() {return twoPressed && !twoPrevious;}

    /**
     * Returns true if the select octopus three button was pressed
     *
     * @return true if the select octopus three button was pressed
     */
    public boolean didSelectThree() {return threePressed && !threePrevious;}

    /** Returns true if the w button was pressed. */
    public boolean didSelectWall() {return wallPressed && !wallPrevious;}

    /** Returns true if the w button was pressed. */
    public boolean wIsHeld() {return wPressed;}

    /** Returns true if the w button was pressed. */
    public boolean aIsHeld() {return aPressed;}

    /** Returns true if the w button was pressed. */
    public boolean sIsHeld() {return sPressed;}

    /** Returns true if the w button was pressed. */
    public boolean dIsHeld() {return dPressed;}

    /** Returns true if the b button was pressed. */
    public boolean didSelectObstacle() {return bPressed && !bPrevious;}

    /** Returns true if the o button was pressed. */
    public boolean didSelectOctopus() {return oPressed && !oPrevious;}

    /** Returns true if the e button was pressed. */
    public boolean didSelectEnemy() {return enemySelectPressed && !enemySelectPrevious;}

    /** Returns true if the escape was pressed. */
    public boolean didSelectEscape() {return escPressed && !escPrevious;}

    /** Returns true if the select octopus three button was pressed. */
    public boolean didSelectBackspace() {return backspacePressed && !backspacePrevious;}

    /** Returns true if the activate octopus ability button was pressed. */
    public boolean didAbility() {return abilityPressed && !abilityPrevious;}

    /** Returns true if the toggle level edit mode button was pressed. */
    public boolean didLevelEdit(){return levelEditPressed && !levelEditPrevious;}

    /** Returns true if the save level button was pressed. */
    public boolean didSaveLevel(){return saveLevelPressed && !saveLevelPrevious;}

    /** Returns true if the save level button was pressed. */
    public boolean didLoadLevel(){return loadLevelPressed && !loadLevelPrevious;}

    /** Returns true if the freeze screen button was pressed. */
    public boolean didFreeze() {return spacePressed && !spacePrevious;}

    /** Returns true if the left arrow key was pressed. */
    public boolean didRotateLeft() {return leftPressed && !leftPrevious;}

    /** Returns true if the right arrow key was pressed. */
    public boolean didRotateRight() {return rightPressed && !rightPrevious;}

    /** Returns true if the tutorial icon button was pressed */
    public boolean didTutorialIcon() {
        return tutorialPressed && !tutorialPrevious;
    }

    public boolean didTutorialText() {
        return tutorialPressedText && !tutorialPreviousText;
    }

    /** Returns true if the set camera key was pressed. */
    public boolean didSetCamera() {return setCameraPressed && !setCameraPrevious;}

    /** Returns true if the set bounds key was pressed. */
    public boolean didSetBounds() {return setBoundsPressed && !setBoundsPrevious;}

    /** Returns true if the right arrow key was pressed. */
    public boolean didSelectQ() {return qPressed && !qPrevious;}
    //#endregion
    //=================================

    //#endregion
    //=================================

    /**
     * Reads the input for the player and converts the result into game logic.
     *
     * The method provides both the input bounds and the drawing scale.  It needs
     * the drawing scale to convert screen coordinates to world coordinates. The
     * bounds are for the crosshair. They cannot go outside of this zone.
     *
     * If both bounds and scale are passed in as null, the position will not be clamped and the
     * crosshair position will not be updated.
     *
     * @param bounds The input bounds for the crosshair.
     * @param scale The drawing scale
     */
    public void readInput(Rectangle bounds, Vector2 scale) {
        // Copy state from last animation frame, helps us ignore buttons that are held down
        clickPrevious = clickPressed;
        leftMousePrevious = leftMousePressed;
        rightMousePrevious = rightMousePressed;
        resetPrevious  = resetPressed;
        debugPrevious  = debugPressed;
        nextPrevious = nextPressed;
        prevPrevious = prevPressed;
        controlPrevious = controlPressed;

        setBoundsPrevious = setBoundsPressed;
        setCameraPrevious = setCameraPressed;
        onePrevious = onePressed;
        twoPrevious = twoPressed;
        threePrevious = threePressed;
        spacePrevious = spacePressed;
        abilityPrevious = abilityPressed;
        levelEditPrevious = levelEditPressed;
        saveLevelPrevious = saveLevelPressed;
        loadLevelPrevious = loadLevelPressed;
        wallPrevious = wallPressed;
        wPrevious = wPressed;
        aPrevious = aPressed;
        sPrevious = sPressed;
        dPrevious = dPressed;
        oPrevious = oPressed;
        bPrevious = bPressed;
        enemySelectPrevious = enemySelectPressed;
        escPrevious = escPressed;
        backspacePrevious = backspacePressed;
        leftPrevious = leftPressed;
        rightPrevious = rightPressed;
        qPrevious = qPressed;
        tutorialPrevious = tutorialPressed;
        tutorialPreviousText = tutorialPressedText;

        readKeyboard(bounds, scale);
    }

    /**
     * Reads input from the keyboard.
     *
     * This controller reads from the keyboard regardless of whether or not an X-Box
     * controller is connected.  However, if a controller is connected, this method
     * gives priority to the X-Box controller.
     *
     */
    private void readKeyboard(Rectangle bounds, Vector2 scale) {
        // Give priority to gamepad results
        resetPressed = Gdx.input.isKeyPressed(Input.Keys.R);
        debugPressed = Gdx.input.isKeyPressed(Input.Keys.MINUS);
        prevPressed = Gdx.input.isKeyPressed(Input.Keys.P);
        nextPressed = Gdx.input.isKeyPressed(Input.Keys.N);
        onePressed  = Gdx.input.isKeyPressed(Input.Keys.NUM_1);
        twoPressed  = Gdx.input.isKeyPressed(Input.Keys.NUM_2);
        threePressed  = Gdx.input.isKeyPressed(Input.Keys.NUM_3);
        abilityPressed = Gdx.input.isKeyPressed(Input.Keys.E);
        levelEditPressed = Gdx.input.isKeyPressed(Input.Keys.L);
        saveLevelPressed = Gdx.input.isKeyPressed(Input.Keys.K);
        loadLevelPressed = Gdx.input.isKeyPressed(Input.Keys.J);
        spacePressed  = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        wallPressed = Gdx.input.isKeyPressed(Input.Keys.T);
        wPressed = Gdx.input.isKeyPressed(Input.Keys.W);
        aPressed = Gdx.input.isKeyPressed(Input.Keys.A);
        sPressed = Gdx.input.isKeyPressed(Input.Keys.S);
        dPressed = Gdx.input.isKeyPressed(Input.Keys.D);
        oPressed = Gdx.input.isKeyPressed(Input.Keys.O);
        bPressed = Gdx.input.isKeyPressed(Input.Keys.B);
        setCameraPressed = Gdx.input.isKeyPressed(Input.Keys.C);
        setBoundsPressed = Gdx.input.isKeyPressed(Input.Keys.H);
        enemySelectPressed = Gdx.input.isKeyPressed(Input.Keys.E);
        escPressed = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
        backspacePressed = Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE);
        leftPressed = Gdx.input.isKeyJustPressed(Input.Keys.LEFT);
        rightPressed = Gdx.input.isKeyJustPressed(Input.Keys.RIGHT);
        qPressed = Gdx.input.isKeyJustPressed(Input.Keys.Q);
        tutorialPressed = Gdx.input.isKeyPressed(Input.Keys.Y);
        tutorialPressedText = Gdx.input.isKeyPressed(Input.Keys.U);
        controlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

        // Directional controls
        horizontal = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontal -= 1.0f;
        }

        vertical = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vertical += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vertical -= 1.0f;
        }

        // Mouse results
        leftMousePressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        rightMousePressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
        clickPressed = leftMousePressed || rightMousePressed;
        readMousePosition(bounds, scale);
    }

    private void readMousePosition(Rectangle bounds, Vector2 scale) {
        int viewportX = canvas.getViewport().getScreenX();
        int viewportY = canvas.getViewport().getScreenY();
        float viewportSclX = canvas.getViewport().getWorldWidth()/canvas.getViewport().getScreenWidth();
        float viewportSclY = canvas.getViewport().getWorldHeight()/canvas.getViewport().getScreenHeight();
        mouse.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        mouse.sub(viewportX, viewportY);
        mouse.scl(viewportSclX, viewportSclY);
        //If within large resized world, mouse pos is in terms of libgdx pixel units
        //If not, in terms of position on desktop window
        if (bounds != null && scale != null) {

            crosshair.set(mouse);
            crosshair.scl(1 / scale.x, 1 / scale.y);
            //Order of operations on Y value: Invert, Add Height, sub viewportY, mult viewportScl, Div scale
            clampPosition(bounds, scale);
//            System.out.println("mouse: " + mouse);
//            System.out.println("crosshair: " + crosshair);
//            System.out.println("scale: " + scale);
//            System.out.println("bounds: " + bounds);
//            System.out.println();

            if (Gdx.input.isCursorCatched()) {
                if (catchedPrev) {
                    //If the cursor was already catched, treat this correctly
                    Gdx.input.setCursorPosition((int) ((crosshair.x * scale.x / viewportSclX) + viewportX),
                            Gdx.graphics.getHeight() - (int) ((crosshair.y * scale.y / viewportSclY) + viewportY));
                } else {
                    //We are just switching from not being catched
                    //Crosshair is in terms of the screen
                    Gdx.input.setCursorPosition((int) (((crosshair.x + bounds.x) * scale.x / viewportSclX) + viewportX),
                            Gdx.graphics.getHeight() - (int) (((crosshair.y + bounds.y) * scale.y / viewportSclY) + viewportY));
                }
            }
            catchedPrev = Gdx.input.isCursorCatched();
        }
    }

    /**
     * Clamp the cursor position so that it does not go outside the window
     *
     * While this is not usually a problem with mouse control, this is critical
     * for the gamepad controls.
     */
    private void clampPosition(Rectangle bounds, Vector2 scale) {
        if (Gdx.input.isCursorCatched() && catchedPrev) {
            mouseIsRight = crosshair.x + SCREEN_PANNING_OFFSET / scale.x >= bounds.x + bounds.width;
            mouseIsLeft = crosshair.x - SCREEN_PANNING_OFFSET / scale.x <= bounds.x;
            mouseIsUp = crosshair.y + SCREEN_PANNING_OFFSET / scale.y >= bounds.y + bounds.height;
            mouseIsDown = crosshair.y - SCREEN_PANNING_OFFSET / scale.y <= bounds.y;
            mouse.x = Math.max(bounds.x * scale.x, Math.min(canvas.getViewport().getWorldWidth() + bounds.x * scale.x, mouse.x));
            mouse.y = Math.max(bounds.y * scale.y, Math.min(canvas.getViewport().getWorldHeight() + bounds.y * scale.x, mouse.y));
            crosshair.x = Math.max(bounds.x, Math.min(bounds.x + bounds.width, crosshair.x));
            crosshair.y = Math.max(bounds.y, Math.min(bounds.y + bounds.height, crosshair.y));
        } else {
            mouse.add(bounds.x * scale.x, bounds.y * scale.y);
        }
    }

    /** Stores information needed by the view for a character state. */
    public static class StateView {
        public Animation animation;
        public boolean loop;
        // Controls the start frame when changing from another animation to this animation.
        public ObjectFloatMap<Animation> startTimes = new ObjectFloatMap();
        public float defaultStartTime;
    }
}
