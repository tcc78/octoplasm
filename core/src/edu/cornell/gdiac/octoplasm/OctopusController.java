package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.audio.SoundBuffer;
import edu.cornell.gdiac.octoplasm.util.AudioController;
import edu.cornell.gdiac.octoplasm.entity.*;
import edu.cornell.gdiac.octoplasm.util.FilmStrip;
import edu.cornell.gdiac.octoplasm.util.PooledList;
//import jdk.internal.net.http.common.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static edu.cornell.gdiac.octoplasm.entity.OctopusEntity.*;

/**
 * TODO: Add class documentation
 * Controller for Octopus. Handles ability activations as well as player selection when choosing and moving octopus,
 * as well as for teleportation selection.
 *
 * @author Thomas Chen
 */
public class OctopusController {
    //=========================================================================
    //#region Fields
    /** The state of the music, as given by the current octopus types alive on the screen. */
    public enum MusicState {
        /** All octopi are currently alive */
        ALL,
        /** Just Fight octopi are alive */
        FIGHT,
        /** Just Fold octopi are alive */
        FOLD,
        /** Just Flight octopi are alive */
        FLIGHT,
        /** There is no Fold octopus */
        NO_FOLD,
        /** There is no Flight octopus */
        NO_FLIGHT,
        /** There is no Fight octopus */
        NO_FIGHT,
        /** No octopi are currently alive */
        NONE
    }

    /** Ray count used for Fight ability ray casting */
    private static final int RAY_COUNT = 360;
    /** The radius of the explosion for Fight */
    private static final float EXPLOSION_RADIUS = 5.0f;
    /** The radius of the grab for Fold */
    private static final float GRAB_RADIUS = 12.5f; //10.0f
    /** launch speed for objects hit by explosion*/
    private static final float LAUNCH_SPEED = 5f;
    /** Reference to the blue octopus texture */
    private static final String BLUE_TEXTURE = "static_sprites/octopus/Fold.png";
    /** Reference to the green octopus texture */
    private static final String GREEN_TEXTURE = "static_sprites/octopus/Flight.png";
    /** Reference to the orange octopus texture */
    private static final String ORANGE_TEXTURE = "static_sprites/octopus/Fight.png";
    /** Reference to the explosion texture */
    private static final String EXPLOSION_TEXTURE = "images/explosion.png";
    /** Reference to the teleport smoke texture */
    private static final String TELEPORT_SMOKE_TEXTURE = "images/teleport_smoke.png";
    /** Reference to the flying smoke texture */
    private static final String FLYING_SMOKE_TEXTURE = "images/flying_smoke.png";
    /** Reference to the flying timer texture */
    private static final String FLYING_TIMER_TEXTURE = "ui/gameplay/octopus/zoomer_timer.png";
    /** Reference to the flying timer texture */
    private static final String FOLD_CHARGE_TEXTURE = "ui/gameplay/octopus/fold_charges.png";
    /** Reference to the world pausing texture */
    private static final String OCTOGOAL_TEXTURE = "ui/gameplay/octopus/octo_goal.png";
    /** Reference to the world pausing texture */
    private static final String GOALLINE_TEXTURE = "ui/gameplay/octopus/goalline.png";
    /** Reference to the octopus selector texture */
    private static final String SELECTOR_TEXTURE = "ui/gameplay/octopus/octo_selector.png";
    /** Reference to the small circle texture */
    private static final String OCTO_CIRCLE_FILE = "ui/gameplay/octopus/circle_small.png";
    /** Reference to the large circle texture */
    private static final String INDICATOR_CIRCLE_FILE = "ui/gameplay/circle.png";

    /** Reference to the sound played during the explosion ability */
    private static final String EXPLOSION_SOUND = "sounds/gameplay/boomer-explosion.mp3"; // TODO: Remake explosion
    /**  */
    private static final String ROCK_WALL_BREAK_SOUND = "sounds/gameplay/rock_wall.wav";
    /**  */
    private static final String ENEMY_DEATH_SOUND = "sounds/gameplay/enemy_death.wav";
    /**  */
    private static final String GRAB_ABILITY_SOUND = "sounds/gameplay/grab.wav";
    /**  */
    private static final String OCTOPUS_MOVEMENT_SOUND = "sounds/gameplay/octopus_move.wav";
    /**  */
    private static final String FLIGHT_DEATH = "sounds/gameplay/flight_death.wav";
    /**  */
    private static final String OCTOPUS_SELECTION_SOUND = "sounds/gameplay/octopus_select.wav";
    /**  */
    private static final String FLIGHT_ABILITY_SOUND = "sounds/gameplay/flight_activate.wav";
    /**  */
    private static final String FLIGHT_MOVEMENT_SOUND = "sounds/gameplay/flight_movement.wav";
    /**  */
    private static final String GOAL_SET_SOUND = "sounds/gameplay/goal_set.wav";

    /** Reference to the folding speed */
    private static final float FOLDING_SPEED = 11f;

    /** Reference to the player octopus objects, (array of size 3) */
    private ArrayList<OctopusEntity> octopusList;
    /** The currently selected octopus. */
    private OctopusEntity activeOctopus;
    /** The selected octopus to teleport. */
    private OctopusEntity teleportOctopus;
    /** Whether the teleport ability is currently active */
    private boolean teleportSelectionActive = false;
    /** Whether the teleport ability has been queued on an octopus */
    private boolean teleportQueued = false;
    /** Cache object for setting vector positions */
    private Vector2 cache;
    /** The world scale */
    private Vector2 scale;
    /** Reference to the game canvas */
    private GameCanvas canvas;
    /** Texture asset for circle crosshair */
    private TextureRegion smallCircleTexture;
    /** Texture assets for the octopus selector */
    private TextureRegion octoSelectorTexture;
    /** Texture asset for the range indicator */
    private TextureRegion indicatorCircle;
    /** Texture assets for the goal lines. Index Corresponds to index in octopusList. */
    //TODO: this is currently public because level editor uses this to draw the lines for walls
    public TextureRegion lineTexture;
    /** Texture asset for octopus goal texture */
    private TextureRegion octogoalTexture;
    /** Track all loaded assets (for unloading purposes) */
    protected Array<String> assets;
    /** TODO documentation */
    private RayCastCallback callback;
    /** TODO documentation */
    private ArrayList<Entity> explosionConfirmedCache = new ArrayList<Entity>();
    private HashMap<Entity,Float> explosionDetectedCache = new HashMap<Entity,Float>();
    /** List of dead Octopi */
    private ArrayList<OctopusEntity> deadOctopi;
    /** TODO documentation */
    private MusicState currentMusicState;
    /** TODO documentation */
    private boolean changedMusicState;
    private float wall_fraction = 1f;
    int teleCount = 0;
    public ObstacleEntity fightCollide = null;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a new empty set of Octopi
     *
     * @param scale
     * @param canvas
     * @param assets
     */
    public OctopusController(Vector2 scale, GameCanvas canvas, Array<String> assets) {
        this.scale = scale;
        this.canvas = canvas;
        this.assets = assets;
        this.octopusList = new ArrayList<OctopusEntity>();
        this.cache = new Vector2();
        deadOctopi = new ArrayList<>();
        callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                Entity entity = (Entity) fixture.getBody().getUserData();
                if (!entity.isActive()) return 1;
                switch (entity.getEntityType()) {
                    case WALL:
                        wall_fraction = Math.min(fraction,wall_fraction);
                        return 1;
                    case OBSTACLE:
                        if (((ObstacleEntity)entity).getObstacleSubType() != ObstacleEntity.ObstacleSubType.FIGHT_WALL) {
                            wall_fraction = Math.min(fraction,wall_fraction);
                        } else if (wall_fraction > fraction) {
                            if(!explosionDetectedCache.containsKey(entity)) {
//                                System.out.println("destructive wall deteccted");
                                explosionDetectedCache.put(entity, fraction);
                            }
                        }
                        return 1;
                    default:
//                        System.out.println("octopus deteccted");
                        if(!explosionDetectedCache.containsKey(entity) && wall_fraction > fraction)
                            explosionDetectedCache.put(entity,fraction);
                        return 1;
                }
            }
        };
        this.currentMusicState = MusicState.NONE;
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**
     * gets the active octopus
     */
    public OctopusEntity getActiveOctopus() { return activeOctopus; }

    /**
     * resets the active octopus
     */
    public void resetActiveOctopus() { activeOctopus = null; }

    /**
     * returns the list of dead octopi
     *
     * @return the list of dead octopi
     */
    public ArrayList<OctopusEntity> getDeadOctopi() { return deadOctopi; }

    /**
     * returns whether the player is currently selecting an Entity to teleport
     *
     * @return whether the player is currently selecting an Entity to teleport
     */
    public boolean getTeleportSelectionActive() {
        return teleportSelectionActive;
    }

    /**
     * returns whether the player has selected an Entity to teleport
     *
     * @return whether the player has selected an Entity to teleport
     */
    public boolean getTeleportQueued() {
        return teleportQueued;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     * @param bounds The game bounds in Box2d coordinates
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

    /**
     * Returns a pointer to the current octopus list.
     *
     * @return The current octopus list
     */
    public ArrayList<OctopusEntity> getOctopusList(){return octopusList;}

    /**
     * Empties the octopus list
     */
    public void emptyOctopusList() {
        octopusList.clear();
        activeOctopus = null;
        teleportOctopus = null;
    }

    /**
     * Whether or not the music state has been changed.
     *
     * @return Whether or not the music state has been changed.
     */
    public boolean changedMusicState() {
        return changedMusicState;
    }

    /**
     * Returns the current music state of this controller.
     *
     * @return The current music state of the controller.
     */
    public MusicState getMusicState() {
        return currentMusicState;
    }
    //#endregion
    //=================================

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
        // Octopus goal destination texture
        manager.load(OCTOGOAL_TEXTURE, Texture.class);
        assets.add(OCTOGOAL_TEXTURE);

        // Octopus Goal Path texture
        manager.load(GOALLINE_TEXTURE, Texture.class);
        assets.add(GOALLINE_TEXTURE);

        // Octopus textures
        manager.load(BLUE_TEXTURE, Texture.class);
        assets.add(BLUE_TEXTURE);
        manager.load(ORANGE_TEXTURE, Texture.class);
        assets.add(ORANGE_TEXTURE);
        manager.load(GREEN_TEXTURE, Texture.class);
        assets.add(GREEN_TEXTURE);

        // Octopus Selector & Selector Circle textures
        manager.load(SELECTOR_TEXTURE, Texture.class);
        assets.add(SELECTOR_TEXTURE);
        manager.load(OCTO_CIRCLE_FILE, Texture.class);
        assets.add(OCTO_CIRCLE_FILE);

        // Explosion textures
        manager.load(EXPLOSION_TEXTURE, Texture.class);
        assets.add(EXPLOSION_TEXTURE);
        manager.load(TELEPORT_SMOKE_TEXTURE, Texture.class);
        assets.add(TELEPORT_SMOKE_TEXTURE);
        manager.load(FLYING_SMOKE_TEXTURE, Texture.class);
        assets.add(FLYING_SMOKE_TEXTURE);
        // Timer textures
        manager.load(FLYING_TIMER_TEXTURE, Texture.class);
        assets.add(FLYING_TIMER_TEXTURE);
        // Charge texture
        manager.load(FOLD_CHARGE_TEXTURE, Texture.class);
        assets.add(FOLD_CHARGE_TEXTURE);
        //Sounds
        manager.load(EXPLOSION_SOUND, Sound.class);
        assets.add(EXPLOSION_SOUND);
        manager.load(ROCK_WALL_BREAK_SOUND, Sound.class);
        assets.add(ROCK_WALL_BREAK_SOUND);
        manager.load(ENEMY_DEATH_SOUND, Sound.class);
        assets.add(ENEMY_DEATH_SOUND);
        manager.load(GRAB_ABILITY_SOUND, Sound.class);
        assets.add(GRAB_ABILITY_SOUND);
        manager.load(OCTOPUS_MOVEMENT_SOUND, Sound.class);
        assets.add(OCTOPUS_MOVEMENT_SOUND);
        manager.load(FLIGHT_DEATH, Sound.class);
        assets.add(FLIGHT_DEATH);
        manager.load(OCTOPUS_SELECTION_SOUND, Sound.class);
        assets.add(OCTOPUS_SELECTION_SOUND);
        manager.load(FLIGHT_ABILITY_SOUND, Sound.class);
        assets.add(FLIGHT_ABILITY_SOUND);
        manager.load(FLIGHT_MOVEMENT_SOUND, Sound.class);
        assets.add(FLIGHT_MOVEMENT_SOUND);
        manager.load(GOAL_SET_SOUND, Sound.class);
        assets.add(GOAL_SET_SOUND);
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
        AudioController audio = AudioController.getInstance();

        //todo to make setters for these
        OctopusEntity.explosion = createFilmStrip(manager,EXPLOSION_TEXTURE,8,10, OctopusEntity.EXPLOSION_FRAMES);
        OctopusEntity.teleportSmoke = createFilmStrip(manager,TELEPORT_SMOKE_TEXTURE, 8, 10, OctopusEntity.TELEPORT_SMOKE_FRAMES);
        OctopusEntity.flyingSmoke = createFilmStrip(manager,FLYING_SMOKE_TEXTURE, 7, 10, OctopusEntity.FLYING_SMOKE_FRAMES);
        //TODO: Change Flying Timer to be a different texture
        OctopusEntity.flyingTimer = createFilmStrip(manager,FLYING_TIMER_TEXTURE,1,4, OctopusEntity.FLYING_TIMER_FRAMES);
        OctopusEntity.foldCharges = createFilmStrip(manager,FOLD_CHARGE_TEXTURE,1,2, OctopusEntity.FOLD_CHARGES_FRAMES);

        OctopusEntity.teleporterTexture = createTexture(manager,BLUE_TEXTURE,false);
        OctopusEntity.flyerTexture = createTexture(manager,GREEN_TEXTURE,false);
        OctopusEntity.exploderTexture = createTexture(manager,ORANGE_TEXTURE,false);
        indicatorCircle = createTexture(manager, INDICATOR_CIRCLE_FILE, false);
        octoSelectorTexture = createTexture(manager,SELECTOR_TEXTURE,false);
        smallCircleTexture = createTexture(manager,OCTO_CIRCLE_FILE,false);
        octogoalTexture = createTexture(manager,OCTOGOAL_TEXTURE,false);
        lineTexture = createTexture(manager,GOALLINE_TEXTURE,true);

        audio.allocateSound(manager, EXPLOSION_SOUND);
        audio.allocateSound(manager, ROCK_WALL_BREAK_SOUND);
        audio.allocateSound(manager, ENEMY_DEATH_SOUND);
        audio.allocateSound(manager, GRAB_ABILITY_SOUND);
        audio.allocateSound(manager, OCTOPUS_MOVEMENT_SOUND);
        audio.allocateSound(manager, FLIGHT_DEATH);
        audio.allocateSound(manager, OCTOPUS_SELECTION_SOUND);
        audio.allocateSound(manager, FLIGHT_ABILITY_SOUND);
        audio.allocateSound(manager, FLIGHT_MOVEMENT_SOUND);
        audio.allocateSound(manager, GOAL_SET_SOUND);
    }
    //#endregion
    //=================================

    /**
     * resets OctopusController to factory settings
     */
    public void reset(){
        emptyOctopusList();
        deadOctopi.clear();
        activeOctopus = null;
        teleportOctopus = null;
        teleportSelectionActive = false;
        teleportQueued = false;
        cache = new Vector2();
        teleCount = 0;
        explosionConfirmedCache = new ArrayList<Entity>();
        explosionDetectedCache = new HashMap<Entity,Float>();
    }

    /**
     * Adds an octopus to the octopus list. There can only be a maximum of three octopus.
     *
     * @param octopus the octopus entity to add
     */
    public void addOctopus(OctopusEntity octopus){
        if(octopusList.size() <= 3) octopusList.add(octopus);
        else throw new AssertionError("There is no empty spot to add an octopus.");
    }

    /**
     * Deletes an octopus to the octopus list. Throws an exception if the octopus is one that is not already in the list.
     * Should be called during level editing, not when an octopus dies, so it can be revived later.
     * @param octopus the octopus entity to delete
     */
    public void removeOctopus(OctopusEntity octopus){
        octopusList.remove(octopus);
    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code for the octopi.
     *
     * @param dt Number of seconds since last animation frame
     * @param frozen Whether the game is frozen or not
     * @param objects The list of objects in the world
     */
    public void update(float dt, boolean frozen, PooledList<Entity> objects, World world, BoxEntity selector) {
        InputController input = InputController.getInstance();
        AudioController audio = AudioController.getInstance();
        selectOctopusOrEntity(input, objects, selector);
        //This sets whether the ability is currently activated or not.
        for (OctopusEntity o : octopusList) {
            o.stateChanged = false;
            if (o.isRespawned) {
                if (o.getOctopusSubType() == OctopusSubType.TELEPORTER)
                    o.setGrabCharges(2);
                o.setState(State.idle);
            }
            if (o.getOctopusSubType()==OctopusSubType.TELEPORTER && o.isAbilityActive() && (o.getTeleportEntity() == null || !o.getTeleportEntity().isActive()) && o.getGrabCharges() < 1) {
                o.setIsDead(true);
                if (!deadOctopi.contains(o)) deadOctopi.add(o);
            } else {
//                o.setIsGrab(false);
            }
            if (o.getIsDead()) {
                if (o.getOctopusSubType() != OctopusSubType.EXPLODER) o.setActive(false);
                o.setAlive(false);
                o.setAbilityActive(false);
            }

            //This sets Fold back to dynamic
            if (o.getOctopusSubType() == OctopusSubType.TELEPORTER && !o.isAbilityActive() && o.getGrabCharges() > 0)
                o.setBodyType(BodyDef.BodyType.DynamicBody);
        }
        if (input.didAbility() && activeOctopus != null) {
            //If the ability key is pressed while the teleporter octopus is active
            if (activeOctopus.getOctopusSubType() == OctopusEntity.OctopusSubType.TELEPORTER) {
                if (!activeOctopus.isAlive()) {
                    teleportSelectionActive = false;
                } else {
                    if (!teleportSelectionActive && !activeOctopus.getIsGrab()) {
                        teleportSelectionActive = true;
                        activeOctopus.setAbilityActive(true);
                    } else {
                        teleportSelectionActive = false;
                        teleCount = 0;
                        activeOctopus.setAbilityActive(false);
                        activeOctopus.setTeleportEntity(null);
                    }
                    if(teleportQueued && activeOctopus.getTeleportEntity() != null) {
                        activeOctopus.getTeleportEntity().setOctopusTeleportEntity(null);
                        activeOctopus.setTeleportEntity(null);
                        teleCount = 0;
                    }
                    teleportQueued = false;
                }
                teleportOctopus = null;
            } else if (activeOctopus.getOctopusSubType() == OctopusEntity.OctopusSubType.FLYER && !activeOctopus.getFlying()) {
                activeOctopus.setAbilityActive(!activeOctopus.isAbilityActive());
                audio.playSound("flightAbility", FLIGHT_ABILITY_SOUND, false); //Flight Ability Activate
            } else if (activeOctopus.getOctopusSubType() == OctopusEntity.OctopusSubType.EXPLODER) {
                activeOctopus.setAbilityActive(!activeOctopus.isAbilityActive());
            }
        }

        // Octopus Ability Code
        if (!frozen) {
            for (OctopusEntity o : octopusList) {
                // TELEPORTER octopus code
                Entity teleportEntity = o.getTeleportEntity();

                //teleportEntity will always be null if OctopusEntity o is not a TELEPORTER
                if (o.getOctopusSubType()==OctopusSubType.TELEPORTER && o.isAlive() && teleportEntity != null && !teleportEntity.isActive()) {
                    o.setState(State.idle);
                    o.setBodyType(BodyDef.BodyType.DynamicBody);
                    o.setTeleportEntity(null);
                } else if (o.getOctopusSubType()==OctopusSubType.TELEPORTER && o.isAlive() && teleportEntity == null && o.getState()==State.ability) {
                    o.setState(State.idle);
                    o.setBodyType(BodyDef.BodyType.DynamicBody);
                }
                if (o.isAlive() && teleportEntity != null && teleCount > 0) {
                    teleCount--;
                    if (teleportEntity.getOctopusTeleportEntity() == o) {
                        o.decGrabCharges();
                        o.setState(State.ability);
                        teleportEntity.setSensor(true);
                        teleportEntity.setGrabbed(true);
                        audio.playSound("grab", GRAB_ABILITY_SOUND, false); //Grab sound

                        if (teleportEntity.getEntityType() == EntityType.OBSTACLE) {
                            selector.removeCollidingWith(teleportEntity);
                            teleportEntity.setBodyType(BodyDef.BodyType.DynamicBody);
                        }
                        teleportEntity.setGrabbed(true);
                        o.setBodyType(BodyDef.BodyType.StaticBody);
                        setFoldVelocity(o, teleportEntity);
                        if (teleportOctopus != null && o.getGrabCharges() < 1) {
                            activeOctopus = teleportOctopus;
                        }
                        teleportQueued = false;
                        teleportSelectionActive = false;
                    } else {
                        /* this means a later teleport octopus selected the same entity during the pause, so the entity
                         * will ultimately end up to the later octopus's location
                         */
                        o.decGrabCharges();
                        o.setTeleportEntity(null);
                        o.setAbilityActive(false);
                        teleportSelectionActive = false;
                        if(o.getGrabCharges() < 1) {
                            o.setState(State.death);
                            o.setActive(false);
                            if (!deadOctopi.contains(o)) deadOctopi.add(o);
                            o.setAlive(false);
                        } else {
                            o.setState(State.idle);
                        }
                    }
                }

                // EXPLODER octopus code
                if (o.getOctopusSubType() == OctopusEntity.OctopusSubType.EXPLODER && (o.isAbilityActive() || (!o.isAlive() && o.isActive()))) {
                    if (o.isGrabbed() && fightCollide != null) {
                        fightCollide.setIsDead(true);
                    }
                    o.addAnimation(o.getPosition().cpy(), OctopusEntity.AnimationType.SMOKE);
                    explosion(o, world);
                    if (activeOctopus == o)
                        activeOctopus = null;
                    o.setState(State.death);
                    o.setActive(false);
                    o.setAlive(false);
                    o.setAbilityActive(false);
                    teleportSelectionActive = false;
                    if (!deadOctopi.contains(o)) deadOctopi.add(o);
                    audio.playSound("fightAbility", EXPLOSION_SOUND, false, true);
                }
                if (!o.isAlive() && o.isActive()) {
                    o.setActive(false);
                    o.setAbilityActive(false);
                    teleportSelectionActive = false;
                    if (!deadOctopi.contains(o)) deadOctopi.add(o);
                }

                // FLYING octopus code
                if (o.getOctopusSubType() == OctopusEntity.OctopusSubType.FLYER && o.isAbilityActive()) {
                    o.setState(State.ability);
                    o.decFlyingCounter();
                    if(!o.getFlying()) {
                        o.setFlying(true);
                        o.setPosition(o.getPosition().x, o.getPosition().y + 0.02f);
                        o.addAnimation(o.getPosition().cpy(), OctopusEntity.AnimationType.SMOKE);
                        o.addAnimation(o.getPosition(), OctopusEntity.AnimationType.TIMER);
                    } else if (o.doneFlying()) {
                        if(activeOctopus == o)
                            activeOctopus = null;
                        o.setState(State.death);
                        o.setActive(false);
                        o.setAlive(false);
                        o.setAbilityActive(false);
                        teleportSelectionActive = false;
                        if (!deadOctopi.contains(o)) deadOctopi.add(o);
                        audio.playSound("flightDeath", FLIGHT_DEATH, false);
                    }
                }
                if (!o.isAlive() && o.isActive()) {
                    o.setState(State.death);
                    o.setActive(false);
                    o.setAbilityActive(false);
                    teleportSelectionActive = false;
                }
            }
        }
        //Move the octopus to the goal
        for (OctopusEntity octopus : octopusList) {
            if (!frozen && octopus.isAlive() && octopus.getLaunched() == 0 && !octopus.isGrabbed()) {
                if (octopus.moveToGoal()) {
                    if (octopus.getFlying() && !audio.isActive("flightMove")) {
                        audio.playSound("flightMove", FLIGHT_MOVEMENT_SOUND, false); //Movement sound
                    } else if (octopus.getPlayBoost()) {
                        audio.playSound("octopusMove", OCTOPUS_MOVEMENT_SOUND, false); //Movement sound
                    }
                }
            }
            else if (!octopus.isAlive() && octopus.getState() != State.win) {
                octopus.setState(State.death);
                octopus.setVelocity(0, 0);
                octopus.setAngularVelocity(0);
            } else if (octopus.getLaunched() > 0 && octopus.isAlive()){
                octopus.setState(State.idle);
                if(octopus.getLaunched() == 1) octopus.setVelocity(0,0);
                octopus.decrementLaunched();
            }
        }
        updateMusicState();
    }

    /**
     * Updates the current music state based on the octopi that are currently alive.
     */
    public void updateMusicState() {
        boolean fight = false;
        boolean flight = false;
        boolean fold = false;
        for (OctopusEntity octopusEntity : octopusList) {
            if (octopusEntity != null && octopusEntity.isAlive()) {
                switch (octopusEntity.getOctopusSubType()) {
                    case FLYER:
                        flight = true;
                        break;
                    case EXPLODER:
                        fight = true;
                        break;
                    case TELEPORTER:
                        fold = true;
                        break;
                    default:
                }
            }
        }
        int b = (fight ? 4 : 0) | (flight ? 2 : 0) | (fold ? 1 : 0);
        MusicState newState;
        switch(b) {
            case 0b111:
                newState = MusicState.ALL;
                break;
            case 0b011:
                newState = MusicState.NO_FIGHT;
                break;
            case 0b101:
                newState = MusicState.NO_FLIGHT;
                break;
            case 0b110:
                newState = MusicState.NO_FOLD;
                break;
            case 0b001:
                newState = MusicState.FOLD;
                break;
            case 0b010:
                newState = MusicState.FLIGHT;
                break;
            case 0b100:
                newState = MusicState.FIGHT;
                break;
            case 0b000:
            default:
                newState = MusicState.NONE;
                break;
        }
        changedMusicState = newState != currentMusicState;
        currentMusicState = newState;
    }

    /**
     * Helper method that selects an Octopus or Entity depending on what conditions are active when you click.
     *
     * @param input The player input
     * @param objects The list of entities in the world
     */
    public void selectOctopusOrEntity(InputController input, PooledList<Entity> objects, BoxEntity selector) {
        AudioController audio = AudioController.getInstance();
        //Octopus selection
        if (input.didSelectOne() && !octopusList.isEmpty()) {
            teleportSelectionActive = false;
            activeOctopus = octopusList.get(0);
            audio.playSound("octopusSelect", OCTOPUS_SELECTION_SOUND, false); //Octopus Selection
        } else if (input.didSelectTwo() && octopusList.size() >= 2) {
            teleportSelectionActive = false;
            activeOctopus = octopusList.get(1);
            audio.playSound("octopusSelect", OCTOPUS_SELECTION_SOUND, false); //Octopus Selection
        } else if (input.didSelectThree() && octopusList.size() >= 3) {
            teleportSelectionActive = false;
            activeOctopus = octopusList.get(2);
            audio.playSound("octopusSelect", OCTOPUS_SELECTION_SOUND, false); //Octopus Selection
        }
        if (input.didSelectQ()) {
            teleportSelectionActive = false;
            if (activeOctopus == octopusList.get(0)) {
                if (octopusList.size() > 1 && octopusList.get(1).isActive()) {
                    activeOctopus = octopusList.get(1);
                } else if (octopusList.size() > 1 && !octopusList.get(2).isActive()) {
                    activeOctopus = octopusList.get(2);
                }
            } else if (octopusList.size() > 1 && activeOctopus == octopusList.get(1)) {
                if (octopusList.size() > 2 && octopusList.get(2).isActive()) {
                    activeOctopus = octopusList.get(2);
                } else if (octopusList.get(0).isActive()) {
                    activeOctopus = octopusList.get(0);
                }
            } else if (octopusList.size() > 2 && activeOctopus == octopusList.get(2)) {
                if (octopusList.get(0).isActive()) {
                    activeOctopus = octopusList.get(0);
                } else if (octopusList.get(1).isActive()) {
                    activeOctopus = octopusList.get(1);
                }
            } else {
                if (octopusList.get(0).isActive()) {
                    activeOctopus = octopusList.get(0);
                } else if (octopusList.get(1).isActive()) {
                    activeOctopus = octopusList.get(1);
                } else {
                    activeOctopus = octopusList.get(2);
                }
            }
            audio.playSound("octopusSelect", OCTOPUS_SELECTION_SOUND, false); //Octopus Selection
        }
        //Mouse Clicking
        if (input.didClick()) {
            cache.set(input.getCrossHair().x, input.getCrossHair().y);
            if (input.didClickLeft()) {
                if(selector.getCollidingWithSize() > 0) {
                    Entity e = selector.getClosestColliding();
                    if (teleportSelectionActive && e.getEntityType()!=EntityType.WALL && e.getEntityType()!=EntityType.SELECTOR) {
                        //todo right now center has to be within range, do we want to keep it that way?
                        if (e != activeOctopus && Math.sqrt(Math.pow(activeOctopus.getPosition().x - e.getPosition().x, 2) +
                                Math.pow(activeOctopus.getPosition().y - e.getPosition().y, 2)) < GRAB_RADIUS) {
                            if (e.getEntityType() != EntityType.OBSTACLE || (e.getEntityType() == EntityType.OBSTACLE
                                    && ((ObstacleEntity)e).getCanBeTeleported())) {
                                if (e.getEntityType() != EntityType.ENEMY || (e.getEntityType() == EntityType.ENEMY
                                        && ((EnemyEntity)e).getEnemySubType() != EnemyEntity.EnemySubType.SPIKED_ENEMY)) {
                                    activeOctopus.setTeleportEntity(e);
                                    e.setOctopusTeleportEntity(activeOctopus);
                                    if (e.getEntityType() == EntityType.OCTOPUS) {
                                        teleportOctopus = (OctopusEntity) e;
                                    }
                                    teleportSelectionActive = false;
                                    teleportQueued = true;
                                    teleCount++;
                                }
                            }
                        }
                    } else {
                        if (e.getEntityType() == Entity.EntityType.OCTOPUS && ((OctopusEntity)e).getTeleportEntity() == null) {
                            activeOctopus = (OctopusEntity) e;
                            audio.playSound("octopusSelect", OCTOPUS_SELECTION_SOUND, false); //Octopus Selection
                        }
                    }
                }
            } else if (input.didClickRight() && selector.getWallsTouched() == 0) {
                if(activeOctopus != null){
                    audio.playSound("goalSet", GOAL_SET_SOUND, false); //Goal Set
                    activeOctopus.setGoal(cache);
                    activeOctopus.setMoving(true);
                    teleportSelectionActive = false;
                }
            }
        }
    }

    /**
     * TODO documentation
     *
     * @param exploder
     * @param world
     */
    public void explosion(OctopusEntity exploder, World world) {
        AudioController audio = AudioController.getInstance();
        Vector2 p1 = exploder.getPosition();
        Vector2 p2 = new Vector2();
        Vector2 vec = new Vector2();
        for (int i = 0; i < RAY_COUNT; i++) {
            float angle = (float) (2*Math.PI/RAY_COUNT)*i;
            vec.x = (float)(EXPLOSION_RADIUS*Math.cos(angle));
            vec.y = (float)(EXPLOSION_RADIUS*Math.sin(angle));
            p2.set(p1.cpy().add(vec));
            world.rayCast(callback,p1,p2);
            for (Entity ent : explosionDetectedCache.keySet()) {
                if (explosionDetectedCache.get(ent) < wall_fraction) {
                    explosionConfirmedCache.add(ent);
                }
            }
            explosionDetectedCache.clear();
            wall_fraction = 1.0f;
        }
        for (Entity e : explosionConfirmedCache) {
            switch(e.getEntityType()) {
                case ENEMY:
                    EnemyEntity enemy = (EnemyEntity) e;
                    if (enemy.getEnemySubType() != EnemyEntity.EnemySubType.ARMORED_ENEMY &&
                            enemy.getEnemySubType() != EnemyEntity.EnemySubType.INVINCIBLE_ENEMY) {
                        enemy.setState(State.death);
                        enemy.setActive(false);
                        enemy.setAlive(false);
                        audio.playSound("enemyDeath", ENEMY_DEATH_SOUND, false); //Enemy Death sound
                    }
                    break;
                case OCTOPUS:
                    OctopusEntity oct = (OctopusEntity) e;
                    oct.setLaunched();
                    setExplosionVelocity(exploder, e);
                    break;
                case OBSTACLE:
                    ObstacleEntity obstacle = (ObstacleEntity) e;
                    if (obstacle.getObstacleSubType() == ObstacleEntity.ObstacleSubType.FIGHT_WALL) {
                        if (obstacle.getActive())
                            audio.playSound("rockWallBreak", ROCK_WALL_BREAK_SOUND, false); //Rock Break sound
                        obstacle.setState(State.death);
                        obstacle.setActive(false);
                        // TODO: add animation
                    } else {
                        // TODO: change to move more smoothly
//                        setExplosionVelocity(exploder, e);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * TODO documentation
     *
     * @param exploder
     * @param other
     * @return
     */
    private void setExplosionVelocity(OctopusEntity exploder, Entity other) {
        Vector2 exploderPos = exploder.getPosition();
        Vector2 otherPos = other.getPosition();
        Vector2 difference = new Vector2(otherPos.x - exploderPos.x, otherPos.y - exploderPos.y);
        other.setLinearVelocity(difference.nor().scl(LAUNCH_SPEED));
    }

    /**
     * TODO documentation
     *
     * @param fold
     * @param other
     * @return
     */
    public void setFoldVelocity(OctopusEntity fold, Entity other) {
        Vector2 foldPos = fold.getPosition();
        Vector2 otherPos = other.getPosition();
        Vector2 difference = new Vector2(foldPos.x - otherPos.x, foldPos.y - otherPos.y);
        if (difference.cpy().scl(scale).len() <= FOLDING_SPEED/2) {
            other.setOctopusTeleportEntity(null);
            fold.setTeleportEntity(null);
            if (fold.getGrabCharges() < 1) {
                fold.setIsDead(true);
                if (!deadOctopi.contains(fold)) deadOctopi.add(fold);
            }
            fold.setIsGrab(false);
//            fold.setBodyType(BodyDef.BodyType.DynamicBody);
            fold.setAbilityActive(false);
            other.setLinearVelocity(new Vector2(0,0));
            other.setGrabbed(false);
            other.setSensor(false);
        } else {
            other.setLinearVelocity(difference.nor().scl(FOLDING_SPEED));
        }
    }

    /**
     * Draw the octopi, explosion animations, selectors, and goal lines for the octopi.
     *
     * @param delta The drawing context
     * @param frozen Whether the game is frozen
     */
    public void draw(float delta,boolean frozen) {
        // Draw non-active models
        canvas.begin();
        for (OctopusEntity oct : octopusList) {
            if (oct.getTexture() != null && oct.isAlive()) {
                oct.draw(canvas);
            }
            //todo fix this hack, also make flyer death animation
//            if (oct != activeOctopus) {
//                if (oct.isAbilityActive()
//                        && (oct.getOctopusSubType() != OctopusSubType.TELEPORTER || oct.getTeleportEntity() != null)
//                        && (oct.getOctopusSubType() != OctopusSubType.FLYER || !oct.getFlying())) {
//                    oct.drawSilhouette(canvas, scale);
//                }
            oct.characterView.skeleton.getColor().a = 1;
            oct.drawSkeleton(canvas);
//            }
        }
        //Draw active octopus last
        //todo fix this hack
        if (activeOctopus != null && (activeOctopus.isAlive() || activeOctopus.getOctopusSubType() == OctopusSubType.EXPLODER)) {
            if (activeOctopus.isAbilityActive()
                    && (activeOctopus.getOctopusSubType() != OctopusSubType.TELEPORTER || activeOctopus.getTeleportEntity() != null)
                    && (activeOctopus.getOctopusSubType() != OctopusSubType.FLYER || !activeOctopus.getFlying())) {
                activeOctopus.drawSilhouette(canvas, scale);
            }
            activeOctopus.characterView.skeleton.getColor().a = 1;
            activeOctopus.drawSkeleton(canvas);
        }

        // Goals & Goal lines
        for (OctopusEntity oct : octopusList) {
            if (oct.getState() == State.win) continue;
            boolean shouldShowGoalLines = oct != null &&
                    (oct.getOctopusSubType() == OctopusSubType.FLYER ||
                            oct.getOctopusSubType() == OctopusSubType.EXPLODER && !oct.isAbilityActive() ||
                            oct.getOctopusSubType() == OctopusSubType.TELEPORTER && oct.getTeleportEntity() == null ||
                            !oct.isGrabbed());
            if (shouldShowGoalLines && oct.isMoving()) {
                //TODO: Remove new Vector2 Initialization in the gameplay loop
                Vector2 goal = oct.getGoal();
                cache.set(oct.getPosition());
                canvas.draw(octogoalTexture, Color.WHITE, octogoalTexture.getRegionWidth() / 2f,
                        octogoalTexture.getRegionHeight() / 2f, goal.x * scale.x, goal.y * scale.y, 0f, 1, 1);

                Vector2 cache2 = new Vector2(cache.x - goal.x, cache.y - goal.y);
                cache.set((cache.x + goal.x) / 2, (cache.y + goal.y) / 2);
                lineTexture.setRegionHeight((int) (cache2.len() * scale.y));
                Color lineColor = (oct.isAbilityActive() && oct.getOctopusSubType() == OctopusSubType.FLYER) ? Color.SKY : Color.WHITE;
                canvas.draw(lineTexture, lineColor, lineTexture.getRegionWidth() / 2f,
                        lineTexture.getRegionHeight() / 2f, cache.x * scale.x, cache.y * scale.y, cache2.rotate90(1).angleRad(), 1, 1);
            }
        }

        // Octopus selectors
        if (activeOctopus != null && activeOctopus.isAlive()) {
            canvas.draw(octoSelectorTexture, Color.WHITE, octoSelectorTexture.getRegionWidth() / 2f,
                    octoSelectorTexture.getRegionHeight() / 2f, activeOctopus.getPosition().x * scale.x, activeOctopus.getPosition().y * scale.y, 0f, 1, 1);
            canvas.draw(smallCircleTexture, Color.WHITE, smallCircleTexture.getRegionWidth() / 2f,
                    smallCircleTexture.getRegionHeight() / 2f, activeOctopus.getPosition().x * scale.x, activeOctopus.getPosition().y * scale.y, 0f, 1, 1);
        }

        //Draw octopus effects
        for (OctopusEntity o : octopusList) {
            if (o != null) {
                o.drawEffects(canvas, frozen);
            }

            if (o.getOctopusSubType() == OctopusSubType.TELEPORTER) {
                if (teleportSelectionActive && activeOctopus == o) {
                    cache.set(o.getPosition());
                    float width = GRAB_RADIUS * scale.x * 2f;
                    float height = GRAB_RADIUS * scale.y * 2f;
                    canvas.draw(indicatorCircle, Color.YELLOW, width/2f, height/2f,
                            cache.x*scale.x, cache.y*scale.y, width, height);
                }
            }
        }


        //Indication Lines
        if (frozen) {
            for (OctopusEntity oct : octopusList) {
                switch (oct.getOctopusSubType()) {
                case FLYER:
                    break;
                case EXPLODER:
                    //Explosion Circle Indicator
                    if (oct.isAbilityActive()) {
                        cache.set(oct.getPosition());
                        float width = EXPLOSION_RADIUS * scale.x * 2f;
                        float height = EXPLOSION_RADIUS * scale.y * 2f;
                        canvas.draw(indicatorCircle, Color.RED, width/2f, height/2f,
                                cache.x*scale.x, cache.y*scale.y, width, height);
                    }
                    break;
                case TELEPORTER:
                    //Teleport Indication Lines
                    if (oct.getTeleportEntity() != null && oct.isAlive() && oct.isActive()) {
                        //TODO: Remove new Vector2 Initialization in the gameplay loop
                        cache.set(oct.getPosition());
                        Vector2 entityPos = oct.getTeleportEntity().getPosition();
                        Vector2 cache2 = new Vector2(cache.x - entityPos.x, cache.y - entityPos.y);
                        cache.set((cache.x + entityPos.x)/2, (cache.y + entityPos.y)/2);
                        lineTexture.setRegionHeight((int)(cache2.len()*scale.y));
                        canvas.draw(lineTexture, Color.YELLOW, lineTexture.getRegionWidth() / 2f,
                                lineTexture.getRegionHeight() / 2f, cache.x * scale.x, cache.y * scale.y, cache2.rotate90(1).angleRad(), 1, 1);
                    }
                    break;
                }
            }
        }

        canvas.end();
    }

    //=========================================================================
    //#region Helper texture functions
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
    //#endregion
    //=================================

}