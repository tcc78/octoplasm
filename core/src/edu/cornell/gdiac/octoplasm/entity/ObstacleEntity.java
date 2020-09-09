package edu.cornell.gdiac.octoplasm.entity;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import edu.cornell.gdiac.octoplasm.GameplayController;
import edu.cornell.gdiac.octoplasm.MenuMode;
import edu.cornell.gdiac.octoplasm.util.FilmStrip;

/**
 * The ObstacleEntity class implements the general obstacle class.
 * This class implements the following types of obstacles:
 * 1. Destructible walls
 * 2. Respawn object
 * 3. Goal
 *
 * @author Tricia Park
 */
public class ObstacleEntity extends BoxEntity {

    //=========================================================================
    //#region Fields
    /** The obstacle subtypes */
    public enum ObstacleSubType {
        /** Subtype for fight wall */
        FIGHT_WALL,
        /** Subtype for flight wall */
        FLIGHT_WALL,
        /** Subtype for fold wall */
        FOLD_WALL,
        /** Subtype for respawn objects */
        RESPAWN,
        /** Subtype for goal door */
        GOAL,
    }

    /** The density of respawn tiles */
    private static final float RESPAWN_DENSITY = 0.1f;
    /** The friction of respawn tiles */
    private static final float RESPAWN_FRICTION = 0f;
    /** The restitution of respawn tiles */
    private static final float RESPAWN_RESTITUTION = 0.1f;
    /** The density of goal tile */
    private static final float GOAL_DENSITY = 0f;
    /** The friction of goal tile */
    private static final float GOAL_FRICTION = 0f;
    /** The restitution of goal tile */
    private static final float GOAL_RESTITUTION = 0f;
    /** The density of destructible walls */
    private static final float WALL_DENSITY = 5.0f;
    /** The friction of destructible walls */
    private static final float WALL_FRICTION = 0.1f;
    /** The restitution of destructible walls */
    private static final float WALL_RESTITUTION = 0f;

    /** Frames for flight wall */
    public static final int FLIGHT_WALL_FRAMES = 4;
    /** Currently I just divide by this, so fps will be 60/x where x is this variable */
    public static final int FLIGHT_WALL_FPS = 20;

    /** Obstacle sub type */
    private ObstacleSubType obstacleSubType;
    /** The texture region for fight walls */
    public static TextureRegion fightWallTexture;
    public static TextureRegion fightWallTexture2;
    /** The texture region for flight walls */
    public static TextureRegion flightWallTexture;
    /** The filmstrip for flight wall */
    public static FilmStrip flightWallFilmstrip;
    public static FilmStrip flightWallFilmstrip1;
    public static FilmStrip flightWallFilmstrip2;
    /** The texture region for fold walls */
    public static TextureRegion foldWallTexture;
    public static TextureRegion foldWallTexture2;
    /** The texture region for respawns */
    public static TextureRegion respawnTexture;
    /** The texture regions for the goal */
    public static TextureRegion goalFlightTexture;
    public static TextureRegion goalFightTexture;
    public static TextureRegion goalFoldTexture;
    /** Whether obstacle can be teleported */
    private Boolean canBeTeleported;
    /** Whether obstacle can be exploded */
    private Boolean canBeExploded;
    /** The status of the obstacle */
    private Boolean active;
    /** The sensor status of the obstacle */
    private Boolean isSensor;
    /** The frame of the flight wall animation this obstacle is on */
    private int flightFrame;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a new obstacle
     *
     * @param x initial x position of obstacle center
     * @param y initial y position of obstacle center
     * @param width the obstacle width
     * @param height the obstacle height
     * @param ost the obstacle subtype
     */
    public ObstacleEntity(float x, float y, float angle, float width, float height, ObstacleSubType ost, GameplayController.GameWorld world) {
        super(x, y, angle, width, height, EntityType.OBSTACLE);
        obstacleSubType = ost;
        active = true;
        setBodyType(BodyDef.BodyType.StaticBody);
        switch (ost) {
            case RESPAWN:
                setDensity(RESPAWN_DENSITY);
                setFriction(RESPAWN_FRICTION);
                setRestitution(RESPAWN_RESTITUTION);
                setTexture(respawnTexture);
                setSensor(true);
                isSensor = true;
                canBeTeleported = false;
                canBeExploded = false;
                break;
            case GOAL:
                setDensity(GOAL_DENSITY);
                setFriction(GOAL_FRICTION);
                setRestitution(GOAL_RESTITUTION);
                setTexture(goalFlightTexture);
                setSensor(true);
                isSensor = true;
                canBeTeleported = false;
                canBeExploded = false;
                break;
            case FIGHT_WALL:
                setDensity(WALL_DENSITY);
                setFriction(WALL_FRICTION);
                setRestitution(WALL_RESTITUTION);
                setTexture(world == GameplayController.GameWorld.CAVE ? fightWallTexture : fightWallTexture2);
                isSensor = false;
                canBeTeleported = false;
                canBeExploded = true;
                break;
            case FLIGHT_WALL:
                setDensity(WALL_DENSITY);
                setFriction(WALL_FRICTION);
                setRestitution(WALL_RESTITUTION);
                setTexture(flightWallTexture);
                if(world == GameplayController.GameWorld.CAVE){
                    flightWallFilmstrip = flightWallFilmstrip1;
                } else{
                    flightWallFilmstrip = flightWallFilmstrip2;
                }
                isSensor = false;
                canBeTeleported = false;
                canBeExploded = false;
                flightFrame = (int)(Math.random() * FLIGHT_WALL_FRAMES);
                break;
            case FOLD_WALL:
                setDensity(WALL_DENSITY);
                setFriction(WALL_FRICTION);
                setRestitution(WALL_RESTITUTION);
                setTexture(world == GameplayController.GameWorld.CAVE ? foldWallTexture : foldWallTexture2);
                isSensor = false;
                canBeTeleported = true;
                canBeExploded = false;
                break;
            default:
        }
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**
     * Returns the subtype of the obstacle(i.e. DESTRUCTIBLE_WALL, RESPAWN, or GOAL)
     *
     * @return obstacleSubType
     */
    public ObstacleSubType getObstacleSubType() { return obstacleSubType; }

    /**
     * Returns true if the obstacle can be teleported
     *
     * @return true if the obstacle can be teleported
     */
    public Boolean getCanBeTeleported() { return canBeTeleported; }

    /**
     * Returns true if the obstacle can be exploded
     *
     * @return true if the obstacle can be exploded
     */
    public Boolean getCanBeExploded() { return canBeExploded; }

    /**
     * Returns true if the obstacle is active
     *
     * @return true if the obstacle is active
     */
    public Boolean getActive() { return active; }

    /**
     * Sets whether the obstacle is active or not
     *
     * @param a active status of obstacle
     */
    public void setActive(Boolean a) { active = a; }

    /**
     * Returns true if the obstacle is a sensor
     *
     * @return true if the obstacle is a sensor
     */
    public Boolean getIsSensor() { return isSensor; }

    /**
     * Sets whether the obstacle is a sensor or not
     *
     * @param a sensor status of obstacle
     */
    public void setIsSensor(Boolean a) { isSensor = a; }

    /** get flight frame */
    public int getFlightFrame() {return flightFrame;}

    /** set flight frame */
    public void setFlightFrame(int frame) {flightFrame = frame%(FLIGHT_WALL_FRAMES * FLIGHT_WALL_FPS);}
    //#endregion
    //=================================

    /**
     * Returns the width of the textureregion for hitbox width
     *
     * @param EST the subtype of the octopus
     *
     * @return the width of the textureregion for hitbox width
     */
    public static float getTextureWidth(ObstacleSubType EST){
        switch(EST){
            case FIGHT_WALL:
                return fightWallTexture.getRegionWidth();
            case FLIGHT_WALL:
                return flightWallTexture.getRegionWidth();
            case FOLD_WALL:
                return foldWallTexture.getRegionWidth();
            case RESPAWN:
                return respawnTexture.getRegionWidth();
            case GOAL:
                return goalFlightTexture.getRegionWidth();
            default:
                return 0f;
        }
    }

    /**
     * Returns the height of the textureregion for hitbox height
     *
     * @param EST the subtype of the octopus
     *
     * @return the height of the textureregion for hitbox height
     */
    public static float getTextureHeight(ObstacleSubType EST){
        switch(EST){
            case FIGHT_WALL:
                return fightWallTexture.getRegionHeight();
            case FLIGHT_WALL:
                return flightWallTexture.getRegionHeight();
            case FOLD_WALL:
                return foldWallTexture.getRegionHeight();
            case RESPAWN:
                return respawnTexture.getRegionHeight();
            case GOAL:
                return goalFlightTexture.getRegionHeight();
            default:
                return 0f;
        }
    }
}
