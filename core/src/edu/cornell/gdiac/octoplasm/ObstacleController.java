package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.octoplasm.entity.BoxEntity;
import edu.cornell.gdiac.octoplasm.entity.ObstacleEntity;
import edu.cornell.gdiac.octoplasm.entity.OctopusEntity;
import edu.cornell.gdiac.octoplasm.entity.TutorialEntity;
import edu.cornell.gdiac.octoplasm.util.FilmStrip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * TODO documentation
 *
 * @author Tricia Park
 */
public class ObstacleController {

    //=========================================================================
    //#region Fields
    /** TODO documentation */
    private static final String FLIGHT_GOAL_TEXTURE = "static_sprites/obstacles/GoalDoor-FlightOnly.png";
    private static final String FIGHT_GOAL_TEXTURE = "static_sprites/obstacles/GoalDoor-FightOnly.png";
    private static final String FOLD_GOAL_TEXTURE = "static_sprites/obstacles/GoalDoor-FoldOnly.png";
    /** TODO documentation */
    private static final String RESPAWN_TEXTURE = "static_sprites/obstacles/respawn.png";
    /** TODO documentation */
    private static final String FIGHT_WALL_TEXTURE = "static_sprites/obstacles/Cave_BoomerWall_Explosion_LitRepeatable.png";
    private static final String FIGHT_WALL_TEXTURE2 = "static_sprites/obstacles/Pirate_FightWall_ColorCoded2X.png";
    /** TODO documentation */
    private static final String FLIGHT_WALL_TEXTURE = "static_sprites/obstacles/Cave_FlightWall_Repeatable.png";
    private static final String FLIGHT_WALL_TEXTURE2 = "static_sprites/obstacles/Pirate_FoldWall_ColorCodedRepeatable.png";
    /** TODO documentation */
    private static final String FLIGHT_WALL_TEXTURE_FILMSTRIP = "static_sprites/obstacles/Cave_FlightWall_FilmStrip.png";
    private static final String FLIGHT_WALL_TEXTURE_FILMSTRIP2 = "static_sprites/obstacles/Pirate_FlightWall_FilmStrip2X.png";
    /** TODO documentation */
    private static final String FOLD_WALL_TEXTURE = "static_sprites/obstacles/Cave_FoldWall_Large.png";
    private static final String FOLD_WALL_TEXTURE2 = "static_sprites/obstacles/Pirate_FoldWall_ColorCodedRepeatable.png";
    /** TODO: @Stephen Documentation */
    private static final String MOUSE_LEFT = "ui/gameplay/tutorial/mouseLeft.png";
    /** TODO: @Stephen Documentation */
    private static final String MOUSE_RIGHT = "ui/gameplay/tutorial/mouseRight.png";
    /** TODO: @Stephen Documentation */
    private static final String CURSOR = "ui/gameplay/tutorial/cursor.png";
    /** TODO: @Stephen Documentation */
    private static final String E_KEY = "ui/gameplay/tutorial/e_key.png";
    /** TODO: @Stephen Documentation */
    private static final String W_KEY = "ui/gameplay/tutorial/w_key.png";
    /** TODO: @Stephen Documentation */
    private static final String A_KEY = "ui/gameplay/tutorial/a_key.png";
    /** TODO: @Stephen Documentation */
    private static final String S_KEY = "ui/gameplay/tutorial/s_key.png";
    /** TODO: @Stephen Documentation */
    private static final String D_KEY = "ui/gameplay/tutorial/d_key.png";
    /** TODO: @Stephen Documentation */
    private static final String ESC_KEY = "ui/gameplay/tutorial/esc.png";
    /** TODO: @Stephen Documentation */
    private static final String SPACE_KEY = "ui/gameplay/tutorial/space.png";
    /** TODO: documentation */
    private static final String LEVEL1 = "ui/gameplay/tutorial/level1_text.png";
    /** TODO: documentation */
    private static final String LEVEL2 = "ui/gameplay/tutorial/level2_text.png";
    /** TODO: documentation */
    private static final String LEVEL3 = "ui/gameplay/tutorial/level3_text.png";
    /** TODO: documentation */
    private static final String LEVEL4 = "ui/gameplay/tutorial/level4_text.png";
    /** TODO: documentation */
    private static final String LEVEL5 = "ui/gameplay/tutorial/level5_text.png";
    /** TODO: documentation */
    private static final String LEVEL6 = "ui/gameplay/tutorial/level6_text.png";
    /** TODO: documentation */
    private static final String LEVEL7 = "ui/gameplay/tutorial/level7_text.png";
    /** TODO: documentation */
    private static final String LEVEL8 = "ui/gameplay/tutorial/level8_text.png";
    /** TODO: documentation */
    private static final String LEVEL9 = "ui/gameplay/tutorial/level9_text.png";
    /** TODO: documentation */
    private static final String LEVEL10 = "ui/gameplay/tutorial/level10_text.png";
    /** TODO: documentation */
    private static final String LEVEL11 = "ui/gameplay/tutorial/level11_text.png";
    /** TODO: documentation */
    private static final String LEVEL12 = "ui/gameplay/tutorial/level12_text.png";
    /** TODO: documentation */
    private static final String LEVEL13 = "ui/gameplay/tutorial/level13_text.png";
    /** TODO: documentation */
    private static final String LEVEL14 = "ui/gameplay/tutorial/level14_text.png";
    /** TODO: documentation */
    private static final String LEVEL15 = "ui/gameplay/tutorial/level15_text.png";
    /** TODO: documentation */
    private static final String LEVEL16 = "ui/gameplay/tutorial/level16_text.png";


    /** The texture for respawn fight */
    private static final String RESPAWN_FIGHT = "ui/respawn/respawn_bomber.png";
    /** The texture for respawn fight when hovered over */
    private static final String RESPAWN_FIGHT_HOVER = "ui/respawn/respawn_bomber_hover.png";
    /** The texture for respawn flight */
    private static final String RESPAWN_FLIGHT = "ui/respawn/respawn_zoomer.png";
    /** The texture for respawn fight when hovered over */
    private static final String RESPAWN_FLIGHT_HOVER = "ui/respawn/respawn_zoomer_hover.png";
    /** The texture for respawn fold */
    private static final String RESPAWN_FOLD = "ui/respawn/respawn_summoner.png";
    /** The texture for respawn fight when hovered over */
    private static final String RESPAWN_FOLD_HOVER = "ui/respawn/respawn_summoner_hover.png";

    /** TODO documentation */
    private static final float INTENDED_WIDTH = 1920f;
    /** TODO documentation */
    private static final float INTENDED_HEIGHT = 1080f;
    /** The intended pixel space between menu buttons. */
    private static final float INTENDED_BUTTON_SPACING = 30f;
    /** the vertical space between the octopus button and respawn object */
    private static final float INTENDED_RESPAWN_SPACE = 170f;

    /** the game canvas */
    private GameCanvas canvas;
    /** TODO documentation */
    private Vector2 scale;
    /** TODO documentation */
    protected Array<String> assets;
    /** List that maintains the enemies */
    private ArrayList<ObstacleEntity> obstacleList;
    /** list of tutorial entities */
    private ArrayList<TutorialEntity> tutorialEntityList;
    /** the respawn table UI */
    private Table respawnTable;
    /** list of dead octopi */
    private ArrayList<OctopusEntity> deadOctopi;
    /** The texture region for respawn fight */
    private static TextureRegion respawnFightTexture;
    /** The texture region for respawn fight hover */
    private static TextureRegion respawnFightHoverTexture;
    /** The texture region for respawn flight */
    private static TextureRegion respawnFlightTexture;
    /** The texture region for respawn flight hover */
    private static TextureRegion respawnFlightHoverTexture;
    /** The texture region for respawn fold */
    private static TextureRegion respawnFoldTexture;
    /** The texture region for respawn fold hover */
    private static TextureRegion respawnFoldHoverTexture;
    /** the asset manager */
    private AssetManager manager;
    /** whether respawn is active */
    private boolean respawnActive = false;
    /** whether should be frozen */
    private boolean toFreeze = false;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a new empty set of Obstacles
     *
     * @param assets the art assets that will be passed in for handling textures
     */
    public ObstacleController(GameCanvas canvas, Vector2 scale, Array<String> assets) {
        this.assets = assets;
        this.canvas = canvas;
        this.scale = scale;
        obstacleList = new ArrayList<>();
        tutorialEntityList = new ArrayList<>();
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**
     * returns toFreeze
     *
     * @return toFreeze
     */
    public boolean getToFreeze() { return toFreeze; }

    /**
     * sets toFreeze
     *
     * @param value
     */
    public void setToFreeze(boolean value) { toFreeze = value; }

    /**
     * returns whether respawn is active
     *
     * @return whether respawn is active
     */
    public boolean isRespawnActive() { return respawnActive; }

    /**
     * sets respawnActive
     *
     * @param value
     */
    public void setRespawnActive(boolean value) { respawnActive = value; }

    /** Get the list of enemies in the controller
     *
     * @return the list of the enemies
     * */
    public ArrayList<ObstacleEntity> getObstacleList() {
        return obstacleList;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas, Rectangle bounds) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth()/bounds.getWidth();
        this.scale.y = canvas.getHeight()/bounds.getHeight();
//        if(canvas.getWidth()*9 < canvas.getHeight()*16){
//            //height should be kept, width should be bigger than canvas width
//            this.scale.x = this.scale.y;
//        } else if(canvas.getWidth()*9 > canvas.getHeight()*16){
//            this.scale.y = this.scale.x;
//        }
    }
    //#endregion
    //=================================

    /**
     * Add an enemy to the controller
     *
     * @param obstacle the enemy that is going to be handled by the controller
     */
    public void addObstacle(ObstacleEntity obstacle) {
        this.obstacleList.add(obstacle);
    }

    /**
     * removes enemy from enemylist
     *
     * @param obstacle to remove from enemylist
     */
    public void removeObstacle(ObstacleEntity obstacle){
        this.obstacleList.remove(obstacle);
    }

    /**
     * Empties the enemy list
     */
    public void emptyObstacleList() {
        obstacleList.clear();
    }

    /**
     * Add an enemy to the controller
     *
     * @param tutorialEntity the enemy that is going to be handled by the controller
     */
    public void addTutorial(TutorialEntity tutorialEntity) {
        this.tutorialEntityList.add(tutorialEntity);
    }

    /**
     * removes enemy from enemylist
     *
     * @param tutorialEntity to remove from enemylist
     */
    public void removeTutorial(TutorialEntity tutorialEntity){
        this.tutorialEntityList.remove(tutorialEntity);
    }

    /**
     * Empties the enemy list
     */
    public void emptyTutorialList() {
        tutorialEntityList.clear();
    }

    /** Resets this controller */
    public void reset() {
        respawnActive = false;
        respawnTable = null;
        toFreeze = false;
        emptyObstacleList();
        emptyTutorialList();
    }

    /**
     * Update the obstacle states
     *
     * @param dt Number of seconds since last animation frame
     * @param frozen
     * @param world
     */
    public void update(float dt, boolean frozen, World world, ArrayList<OctopusEntity> deadOctopi) {
        if (frozen) return;
        this.deadOctopi = deadOctopi;
        for (ObstacleEntity o : obstacleList) {
            if (o.getObstacleSubType() == ObstacleEntity.ObstacleSubType.FLIGHT_WALL && o.getIsSensor()) {
                o.setActive(false);
            }
            if (o.getIsDead()) {
                o.setActive(false);
            }
        }
    }

    //=========================================================================
    //#region Assets and Asset Loading
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
        this.manager = manager;
        manager.load(FLIGHT_GOAL_TEXTURE, Texture.class);
        assets.add(FLIGHT_GOAL_TEXTURE);
        manager.load(FIGHT_GOAL_TEXTURE, Texture.class);
        assets.add(FIGHT_GOAL_TEXTURE);
        manager.load(FOLD_GOAL_TEXTURE, Texture.class);
        assets.add(FOLD_GOAL_TEXTURE);
        manager.load(RESPAWN_TEXTURE, Texture.class);
        assets.add(RESPAWN_TEXTURE);
        manager.load(FIGHT_WALL_TEXTURE, Texture.class);
        assets.add(FIGHT_WALL_TEXTURE);
        manager.load(FLIGHT_WALL_TEXTURE, Texture.class);
        assets.add(FLIGHT_WALL_TEXTURE);
        manager.load(FLIGHT_WALL_TEXTURE_FILMSTRIP, Texture.class);
        assets.add(FLIGHT_WALL_TEXTURE_FILMSTRIP);
        manager.load(FOLD_WALL_TEXTURE, Texture.class);
        assets.add(FOLD_WALL_TEXTURE);
        manager.load(FIGHT_WALL_TEXTURE2, Texture.class);
        assets.add(FIGHT_WALL_TEXTURE2);
        manager.load(FLIGHT_WALL_TEXTURE2, Texture.class);
        assets.add(FLIGHT_WALL_TEXTURE2);
        manager.load(FLIGHT_WALL_TEXTURE_FILMSTRIP2, Texture.class);
        assets.add(FLIGHT_WALL_TEXTURE_FILMSTRIP2);
        manager.load(FOLD_WALL_TEXTURE2, Texture.class);
        assets.add(FOLD_WALL_TEXTURE2);

        manager.load(RESPAWN_FIGHT, Texture.class);
        assets.add(RESPAWN_FIGHT);
        manager.load(RESPAWN_FIGHT_HOVER, Texture.class);
        assets.add(RESPAWN_FIGHT_HOVER);
        manager.load(RESPAWN_FLIGHT, Texture.class);
        assets.add(RESPAWN_FLIGHT);
        manager.load(RESPAWN_FLIGHT_HOVER, Texture.class);
        assets.add(RESPAWN_FLIGHT_HOVER);
        manager.load(RESPAWN_FOLD, Texture.class);
        assets.add(RESPAWN_FOLD);
        manager.load(RESPAWN_FOLD_HOVER, Texture.class);
        assets.add(RESPAWN_FOLD_HOVER);

        manager.load(MOUSE_LEFT, Texture.class);
        assets.add(MOUSE_LEFT);
        manager.load(MOUSE_RIGHT, Texture.class);
        assets.add(MOUSE_RIGHT);
        manager.load(CURSOR, Texture.class);
        assets.add(CURSOR);
        manager.load(E_KEY, Texture.class);
        assets.add(E_KEY);
        manager.load(W_KEY, Texture.class);
        assets.add(W_KEY);
        manager.load(A_KEY, Texture.class);
        assets.add(A_KEY);
        manager.load(S_KEY, Texture.class);
        assets.add(S_KEY);
        manager.load(D_KEY, Texture.class);
        assets.add(D_KEY);
        manager.load(SPACE_KEY, Texture.class);
        assets.add(SPACE_KEY);
        manager.load(ESC_KEY, Texture.class);
        assets.add(ESC_KEY);

        manager.load(LEVEL1, Texture.class);
        assets.add(LEVEL1);
        manager.load(LEVEL2, Texture.class);
        assets.add(LEVEL2);
        manager.load(LEVEL3, Texture.class);
        assets.add(LEVEL3);
        manager.load(LEVEL4, Texture.class);
        assets.add(LEVEL4);
        manager.load(LEVEL5, Texture.class);
        assets.add(LEVEL5);
        manager.load(LEVEL6, Texture.class);
        assets.add(LEVEL6);
        manager.load(LEVEL7, Texture.class);
        assets.add(LEVEL7);
        manager.load(LEVEL8, Texture.class);
        assets.add(LEVEL8);
        manager.load(LEVEL9, Texture.class);
        assets.add(LEVEL9);
        manager.load(LEVEL10, Texture.class);
        assets.add(LEVEL10);
        manager.load(LEVEL11, Texture.class);
        assets.add(LEVEL11);
        manager.load(LEVEL12, Texture.class);
        assets.add(LEVEL12);
        manager.load(LEVEL13, Texture.class);
        assets.add(LEVEL13);
        manager.load(LEVEL14, Texture.class);
        assets.add(LEVEL14);
        manager.load(LEVEL15, Texture.class);
        assets.add(LEVEL15);
        manager.load(LEVEL16, Texture.class);
        assets.add(LEVEL16);
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
        ObstacleEntity.goalFlightTexture = createTexture(manager, FLIGHT_GOAL_TEXTURE, false);
        ObstacleEntity.goalFightTexture = createTexture(manager, FIGHT_GOAL_TEXTURE, false);
        ObstacleEntity.goalFoldTexture = createTexture(manager, FOLD_GOAL_TEXTURE, false);
        ObstacleEntity.respawnTexture = createTexture(manager, RESPAWN_TEXTURE, false);
        ObstacleEntity.fightWallTexture = createTexture(manager, FIGHT_WALL_TEXTURE, false);
        ObstacleEntity.flightWallTexture = createTexture(manager, FLIGHT_WALL_TEXTURE, false);
        ObstacleEntity.foldWallTexture = createTexture(manager, FOLD_WALL_TEXTURE, false);
        ObstacleEntity.fightWallTexture2 = createTexture(manager, FIGHT_WALL_TEXTURE2, false);
        ObstacleEntity.foldWallTexture2 = createTexture(manager, FOLD_WALL_TEXTURE2, false);

        TutorialEntity.mouseCursorTexture = createTexture(manager, CURSOR, false);
        TutorialEntity.mouseLeftTexture = createTexture(manager, MOUSE_LEFT, false);
        TutorialEntity.mouseRightTexture = createTexture(manager, MOUSE_RIGHT, false);
        TutorialEntity.eKeyTexture = createTexture(manager, E_KEY, false);
        TutorialEntity.wKeyTexture = createTexture(manager, W_KEY, false);
        TutorialEntity.aKeyTexture = createTexture(manager, A_KEY, false);
        TutorialEntity.sKeyTexture = createTexture(manager, S_KEY, false);
        TutorialEntity.dKeyTexture = createTexture(manager, D_KEY, false);
        TutorialEntity.spaceKeyTexture = createTexture(manager, SPACE_KEY, false);
        TutorialEntity.escKeyTexture = createTexture(manager, ESC_KEY, false);
        TutorialEntity.text1 = createTexture(manager, LEVEL1, false);
        TutorialEntity.text2 = createTexture(manager, LEVEL2, false);
        TutorialEntity.text3 = createTexture(manager, LEVEL3, false);
        TutorialEntity.text4 = createTexture(manager, LEVEL4, false);
        TutorialEntity.text5 = createTexture(manager, LEVEL5, false);
        TutorialEntity.text6 = createTexture(manager, LEVEL6, false);
        TutorialEntity.text7 = createTexture(manager, LEVEL7, false);
        TutorialEntity.text8 = createTexture(manager, LEVEL8, false);
        TutorialEntity.text9 = createTexture(manager, LEVEL9, false);
        TutorialEntity.text10 = createTexture(manager, LEVEL10, false);
        TutorialEntity.text11 = createTexture(manager, LEVEL11, false);
        TutorialEntity.text12 = createTexture(manager, LEVEL12, false);
        TutorialEntity.text13 = createTexture(manager, LEVEL13, false);
        TutorialEntity.text14 = createTexture(manager, LEVEL14, false);
        TutorialEntity.text15 = createTexture(manager, LEVEL15, false);
        TutorialEntity.text16 = createTexture(manager, LEVEL16, false);
        ObstacleEntity.flightWallFilmstrip1 = createFilmStrip(manager,FLIGHT_WALL_TEXTURE_FILMSTRIP,1,4, ObstacleEntity.FLIGHT_WALL_FRAMES);
        ObstacleEntity.flightWallFilmstrip2= createFilmStrip(manager,FLIGHT_WALL_TEXTURE_FILMSTRIP2,1,4, ObstacleEntity.FLIGHT_WALL_FRAMES);

//        respawnFightTexture = createTexture(manager, RESPAWN_FIGHT, false);
//        respawnFightHoverTexture = createTexture(manager, RESPAWN_FIGHT_HOVER, false);
//        respawnFlightTexture = createTexture(manager, RESPAWN_FLIGHT, false);
//        respawnFlightHoverTexture = createTexture(manager, RESPAWN_FLIGHT_HOVER, false);
//        respawnFoldTexture = createTexture(manager, RESPAWN_FOLD, false);
//        respawnFoldHoverTexture = createTexture(manager, RESPAWN_FOLD_HOVER, false);
    }
    //#endregion
    //=================================

    /**
     * Returns a newly loaded texture region for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * whether or not the texture should repeat) after loading.
     *
     * @param manager 	Reference to global asset manager.
     * @param file		The texture (region) file
     * @param repeat	Whether the texture should be repeated
     *
     * @return a newly loaded texture region for the given file.
     */
    protected TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
        if (manager.isLoaded(file)) {
            TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (repeat) {
                region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            }
            return region;
        }
        return null;
    }

    /**
     * Returns a newly loaded filmstrip for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * the number of animation frames) after loading.
     *
     * @param manager 	Reference to global asset manager.
     * @param file		The texture (region) file
     * @param rows 		The number of rows in the filmstrip
     * @param cols 		The number of columns in the filmstrip
     * @param size 		The number of frames in the filmstrip
     *
     * @return a newly loaded texture region for the given file.
     */
    protected FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
        if (manager.isLoaded(file)) {
            FilmStrip strip = new FilmStrip(manager.get(file, Texture.class),rows,cols,size);
            strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return strip;
        }
        return null;
    }

    /**
     * Draw the obstacles
     *
     * @param delta The drawing context
     * @param frozen Whether the game is frozen
     */
    public void draw(float delta,boolean frozen,OctopusController OC) {
        // Draw non-active models
        canvas.begin();
        for (ObstacleEntity o : obstacleList) {
            if (o != null) {
                if (o.getActive() && o.isActive()) {
                    if (o.getObstacleSubType() == ObstacleEntity.ObstacleSubType.FLIGHT_WALL) {
                        int frame = o.getFlightFrame();
                        if (!frozen) o.setFlightFrame(frame + 1);
                        ObstacleEntity.flightWallFilmstrip.setFrame(frame / ObstacleEntity.FLIGHT_WALL_FPS);
                        canvas.draw(ObstacleEntity.flightWallFilmstrip, com.badlogic.gdx.graphics.Color.WHITE, ObstacleEntity.flightWallFilmstrip.getRegionWidth() / 2f,
                                ObstacleEntity.flightWallFilmstrip.getRegionHeight() / 2f, o.getPosition().x * scale.x, o.getPosition().y * scale.y, o.getAngle(), 1, 1);
                    } else if (o.getObstacleSubType() == ObstacleEntity.ObstacleSubType.GOAL){
                        boolean fold = false;
                        boolean fight = false;
                        boolean flight = false;
                        switch(OC.getMusicState()){
                            case NONE:
                                break;
                            case FOLD:
                                fold = true;
                                break;
                            case FIGHT:
                                fight = true;
                                break;
                            case FLIGHT:
                                flight = true;
                                break;
                            case NO_FOLD:
                                fight = true;
                                flight = true;
                                break;
                            case NO_FIGHT:
                                fold = true;
                                flight = true;
                                break;
                            case NO_FLIGHT:
                                fold = true;
                                fight = true;
                                break;
                            case ALL:
                                fold = true;
                                fight = true;
                                flight = true;
                                break;
                        }
                        if(flight){
                            canvas.draw(ObstacleEntity.goalFlightTexture, com.badlogic.gdx.graphics.Color.WHITE,
                                    ObstacleEntity.goalFlightTexture.getRegionWidth() / 2f,
                                    ObstacleEntity.goalFlightTexture.getRegionHeight() / 2f,
                                    o.getPosition().x * scale.x, o.getPosition().y * scale.y, o.getAngle(), 1, 1);
                        }
                        if(fight){
                            canvas.draw(ObstacleEntity.goalFightTexture, com.badlogic.gdx.graphics.Color.WHITE,
                                    ObstacleEntity.goalFightTexture.getRegionWidth() / 2f,
                                    ObstacleEntity.goalFightTexture.getRegionHeight() / 2f,
                                    o.getPosition().x * scale.x, o.getPosition().y * scale.y, o.getAngle(), 1, 1);
                        }
                        if(fold){
                            canvas.draw(ObstacleEntity.goalFoldTexture, com.badlogic.gdx.graphics.Color.WHITE,
                                    ObstacleEntity.goalFoldTexture.getRegionWidth() / 2f,
                                    ObstacleEntity.goalFoldTexture.getRegionHeight() / 2f,
                                    o.getPosition().x * scale.x, o.getPosition().y * scale.y, o.getAngle(), 1, 1);
                        }
                    } else o.draw(canvas);
                }
            }
        }

        for (TutorialEntity t : tutorialEntityList) {
            if (t != null) {
                t.draw(canvas);
            }
        }
        canvas.end();
    }

    /**
     * creates the table UI for respawn
     *
     * @param respawn
     */
    public void createRespawnTable(final ObstacleEntity respawn) {
        ImageButton.ImageButtonStyle buttonStyle;
        Texture buttonTexture;
        Texture buttonTextureHover;
        Table buttonsTable = new Table();
        ImageButton button1 = null;
        ImageButton button2 = null;
        //Create Table
        ArrayList<String> textures = new ArrayList<String>();
        ArrayList<String> texturesHover = new ArrayList<String>();
        respawnTable = new Table();
        float xPos = respawn.getX()*scale.x - (canvas.getCameraPosInScreen().x - canvas.getViewport().getWorldWidth()/2f);
        float yPos = respawn.getY()*scale.x - (canvas.getCameraPosInScreen().y - canvas.getViewport().getWorldHeight()/2f);
        respawnTable.setPosition(xPos, yPos);
        respawnTable.setHeight(170f);
        respawnTable.bottom();
        for (OctopusEntity o : deadOctopi) {
            switch (o.getOctopusSubType()) {
                case EXPLODER:
                    textures.add(RESPAWN_FIGHT);
                    texturesHover.add(RESPAWN_FIGHT_HOVER);
                    break;
                case FLYER:
                    textures.add(RESPAWN_FLIGHT);
                    texturesHover.add(RESPAWN_FLIGHT_HOVER);
                    break;
                case TELEPORTER:
                    textures.add(RESPAWN_FOLD);
                    texturesHover.add(RESPAWN_FOLD_HOVER);
                    break;
            }
        }
        switch (deadOctopi.size()) {
            case 0:
                break;
            case 1:
                buttonStyle = new ImageButton.ImageButtonStyle();
                buttonTexture = manager.get(textures.get(0), Texture.class);
                buttonTextureHover = manager.get(texturesHover.get(0), Texture.class);
                buttonStyle.imageUp = new TextureRegionDrawable(buttonTexture);
                buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
                buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
                buttonStyle.imageOver = new TextureRegionDrawable(buttonTextureHover);
                buttonStyle.imageOver.setMinWidth(buttonStyle.imageOver.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
                buttonStyle.imageOver.setMinHeight(buttonStyle.imageOver.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
                button1 = new ImageButton(buttonStyle);
                break;
            case 2:
                // button 1
                buttonStyle = new ImageButton.ImageButtonStyle();
                buttonTexture = manager.get(textures.get(0), Texture.class);
                buttonTextureHover = manager.get(texturesHover.get(0), Texture.class);
                buttonStyle.imageUp = new TextureRegionDrawable(buttonTexture);
                buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
                buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
                buttonStyle.imageOver = new TextureRegionDrawable(buttonTextureHover);
                buttonStyle.imageOver.setMinWidth(buttonStyle.imageOver.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
                buttonStyle.imageOver.setMinHeight(buttonStyle.imageOver.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
                button1 = new ImageButton(buttonStyle);
                // button 2
                buttonStyle = new ImageButton.ImageButtonStyle();
                buttonTexture = manager.get(textures.get(1), Texture.class);
                buttonTextureHover = manager.get(texturesHover.get(1), Texture.class);
                buttonStyle.imageUp = new TextureRegionDrawable(buttonTexture);
                buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
                buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
                buttonStyle.imageOver = new TextureRegionDrawable(buttonTextureHover);
                buttonStyle.imageOver.setMinWidth(buttonStyle.imageOver.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
                buttonStyle.imageOver.setMinHeight(buttonStyle.imageOver.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
                button2 = new ImageButton(buttonStyle);
                break;
        }
        if (button1 != null) {
            button1.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    toFreeze = false;
                    respawnActive = false;
                    Gdx.input.setCursorCatched(true);
                    respawnTable.setVisible(false);
                    respawn.setActive(false);
                    respawn.markRemoved(true);
                    OctopusEntity oct = deadOctopi.get(0);
                    oct.setPosition(respawn.getPosition());
                    oct.setBodyType(BodyDef.BodyType.DynamicBody);
                    oct.setIsDead(false);
                    oct.setAbilityActive(false);
                    oct.setNotFlying();
                    oct.isRespawned = true;
                    oct.setOctopusTeleportEntity(null);
                    oct.setGrabbed(false);
                    oct.setSensor(false);
                    oct.setAlive(true);
                    oct.setActive(true);
                    oct.setGoal(oct.getPosition());
                    deadOctopi.remove(oct);
                }
            });
            buttonsTable.add(button1);
        }
        if (button2 != null) {
            button2.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    toFreeze = false;
                    respawnActive = false;
                    Gdx.input.setCursorCatched(true);
                    respawnTable.setVisible(false);
                    respawn.setActive(false);
                    respawn.markRemoved(true);
                    OctopusEntity oct = deadOctopi.get(1);
                    oct.setPosition(respawn.getPosition());
                    oct.setBodyType(BodyDef.BodyType.DynamicBody);
                    oct.setIsDead(false);
                    oct.setAbilityActive(false);
                    oct.setNotFlying();
                    oct.isRespawned = true;
                    oct.setOctopusTeleportEntity(null);
                    oct.setGrabbed(false);
                    oct.setSensor(false);
                    oct.setAlive(true);
                    oct.setActive(true);
                    oct.setGoal(oct.getPosition());
                    deadOctopi.remove(oct);
                }
            });
            buttonsTable.add(button2);
        }
        respawnTable.add(buttonsTable).expand();
        canvas.addTable(respawnTable);
    }
}
