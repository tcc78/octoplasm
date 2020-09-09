package edu.cornell.gdiac.octoplasm.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.gdiac.octoplasm.GameCanvas;

import java.util.HashSet;

/** This class implements the general enemy class
 *
 * @author Tianlin Zhao
 */
public class EnemyEntity extends BoxEntity {
    //=========================================================================
    //#region Fields
    public enum EnemySubType {
        NORMAL_ENEMY,
        ARMORED_ENEMY,
        SPIKED_ENEMY,
        HOLE_ENEMY,
        INVINCIBLE_ENEMY
    }

    // Default physics values
    /** The density of this rocket */
    private static final float DEFAULT_DENSITY  =  1.0f;
    /** The friction of this rocket */
    private static final float DEFAULT_FRICTION = 0.1f;
    /** The restitution of this rocket */
    private static final float DEFAULT_RESTITUTION = 0f;
    /** The attacking radius of the enemy */
    private static final float DEFAULT_RANGE = 4.5f;
    /** The Default turn speed for all octopi */
    private static final float DEFAULT_TURN_SPEED = 20.0f;
    /** The turn angle (in radians) from the desired angle before we can start moving forward */
    private static final float TURN_ANGLE = (float) (Math.PI / 3f);

    /**  */
    private static final int DETECTION_TIME = (int) (0.5 * 60);

    /** Texture assets for the normal enemy models. */
    public static TextureRegion normalTexture;
    /** Texture assets for the armored enemy models. */
    public static TextureRegion armoredTexture;
    /** Texture assets for the spiked enemy models. */
    public static TextureRegion spikedTexture;
    /** Texture assets for the hole enemy models. */
    public static TextureRegion holeTexture;
    /** Texture assets for the invincible enemy models. */
    public static TextureRegion invincibleTexture;
    /** The subtype of the enemy */
    private EnemySubType enemySubType;

    private static final float normalSpeed = 7.5f;

    private static final float armoredSpeed = 7f;

    private static final float spikedSpeed = 7.5f;

    // Beginning the Section of box property
    /** Beginning the Section of box property */
    private boolean moving;
    /** Shape information for this box */
    protected PolygonShape shape;
    /** The width and height of the box */
    private Vector2 dimension;
    /** A cache value for when the user wants to access the dimensions */
    private Vector2 sizeCache;
    /** A cache value for the fixture (for resizing) */
    private Fixture geometry;
    /** Cache of the polygon vertices (for resizing) */
    private float[] vertices;
    /** A countdown for enemies chasing after octopi */
    private float detectionCounter;
    /** A countdown of the pending attack */
    private float attackCounter;
    /** If this enemy is currently chasing an octopus */
    private boolean chasing;

    /** The list that stores the octopi that this enemy is colliding with */
    private HashSet<OctopusEntity> collidingOctopi;

    // Section of physics


    /** Cached vector for vector calculations */
    private Vector2 cache;
    /** The octopus's current goal position, given in world coordinates */
    private Vector2 pathGoal;
    /** Whether or not the octopus is still alive */
    private boolean alive;
    /** Whether or not the octopus is currently turning (shouldn't change goal position if turning) */
    private boolean turning = false;
    /** The force to apply to this rocket */
    private Vector2 force;
    /** Whether this enemy is locked on to a target */
    private boolean lockedOn;
    /** The target this enemy is locked on */
    private OctopusEntity target;

    /** The previous target this enemy is locked on */
    private OctopusEntity previousTarget;

    /** Cache object for transforming the force according the object angle */
    public Affine2 affineCache = new Affine2();

    public void setPreviousTarget(OctopusEntity oct) {previousTarget = oct;}

    public OctopusEntity getPreviousTarget() {return previousTarget;}

    /** The move speed more all octopi */
    private float speed;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructor
    /**
     * Initializes an enemy
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param w The width of the enemy
     * @param h The height of the enemy
     * @param subType The subtype of the enemy
     */
    public EnemyEntity(float x, float y,float w, float h, EnemySubType subType) {
        super(x,y,w,h,EntityType.ENEMY);
        this.enemySubType = subType;
        switch (subType) {
            case NORMAL_ENEMY:
                speed = normalSpeed;
                setTexture(normalTexture);
                break;
            case ARMORED_ENEMY:
                speed = armoredSpeed;
                setTexture(armoredTexture);
                break;
            case SPIKED_ENEMY:
                speed = spikedSpeed;
                setTexture(spikedTexture);
                break;
            case HOLE_ENEMY:
                //TODO: change?
                speed = spikedSpeed;
                setTexture(holeTexture);
                break;
            case INVINCIBLE_ENEMY:
                speed = spikedSpeed;
                setTexture(invincibleTexture);
                break;
            default:
        }
        pathGoal = new Vector2();
        this.setPosition(x,y);
        cache = new Vector2();
        setDensity(DEFAULT_DENSITY);
        setDensity(DEFAULT_DENSITY);
        setFriction(DEFAULT_FRICTION);
        setRestitution(DEFAULT_RESTITUTION);
        setName("enemy");
        setAlive(true);
        collidingOctopi = new HashSet<OctopusEntity>();
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**
     * Returns the width of the textureregion for hitbox width
     * @param EST the subtype of the octopus
     * @return the width of the textureregion for hitbox width
     */
    public static float getTextureWidth(EnemySubType EST){
        switch(EST){
            case NORMAL_ENEMY:
                return normalTexture.getRegionWidth();
            case ARMORED_ENEMY:
                return armoredTexture.getRegionWidth();
            case SPIKED_ENEMY:
                return spikedTexture.getRegionWidth();
            case HOLE_ENEMY:
                return holeTexture.getRegionWidth();
            case INVINCIBLE_ENEMY:
                return invincibleTexture.getRegionWidth();
            default:
                return 0f;
        }
    }

    /**
     * Returns the height of the textureregion for hitbox height
     * @param EST the subtype of the octopus
     * @return the height of the textureregion for hitbox height
     */
    public static float getTextureHeight(EnemySubType EST){
        switch(EST){
            case NORMAL_ENEMY:
                return normalTexture.getRegionHeight();
            case ARMORED_ENEMY:
                return armoredTexture.getRegionHeight();
            case SPIKED_ENEMY:
                return spikedTexture.getRegionHeight();
            case HOLE_ENEMY:
                return holeTexture.getRegionHeight();
            case INVINCIBLE_ENEMY:
                return invincibleTexture.getRegionHeight();
            default:
                return 0f;
        }
    }

    /**
     * Resets the counter clock to 3 seconds
     */
    public void resetCountDownClock() {
        attackCounter = 3 * 60;
        detectionCounter = DETECTION_TIME;
        chasing = false;
    }

    /**
     * decrease the counter clock by 1 frame-time
     */
    public void decrementClock() {
        attackCounter -= 1;
    }

    /**
     * Decrements the detection clock by 1, updating the chasing flag in enemy entity once it reaches zero.
     */
    public void decrementDetectionCounter() {
        detectionCounter -= 1;
        chasing = detectionCounter <= 0;
    }

    /**
     * Returns the time remains for the enemy to launch an attack
     * @return the frame-time remains before an attack
     */
    public float timeRemain() {
        return attackCounter;
    }

    /**
     * Returns the current value in the detection counter as it counts down from the
     * detection time.
     *
     * @return the current value of the detection counter.
     */
    public float getDetectionCounter() {
        return detectionCounter;
    }

    /**
     * Returns the frames needed for this enemy to detect you.
     *
     * @return the frames needed for this enemy to detect you.
     */
    public float getMaxDetection() {
        return DETECTION_TIME;
    }

    /**
     * Returns the dimensions of this box
     *
     * This method does NOT return a reference to the dimension vector. Changes to this
     * vector will not affect the shape.  However, it returns the same vector each time
     * its is called, and so cannot be used as an allocator.
     *
     * @return the dimensions of this box
     */
    public Vector2 getDimension() {
        return sizeCache.set(dimension);
    }

    /**
     * Sets the dimensions of this box
     *
     * This method does not keep a reference to the parameter.
     *
     * @param value  the dimensions of this box
     */
    public void setDimension(Vector2 value) {
        setDimension(value.x, value.y);
    }

    /**
     * Sets the dimensions of this box
     *
     * @param width   The width of this box
     * @param height  The height of this box
     */
    public void setDimension(float width, float height) {
        dimension.set(width, height);
        markDirty(true);
        resize(width, height);
    }

    /**
     * Returns the box width
     *
     * @return the box width
     */
    public float getWidth() {
        return getTextureWidth(enemySubType);
    }

    /**
     * Sets the box width
     *
     * @param value  the box width
     */
    public void setWidth(float value) {
        sizeCache.set(value,dimension.y);
        setDimension(sizeCache);
    }

    /**
     * Returns the box width
     *
     * @return the box width
     */
    public float getHeight() {
        return getTextureHeight(enemySubType);
    }

    /**
     * Sets the box width
     *
     * @param value  the box width
     */
    public void setHeight(float value) {
        sizeCache.set(value,dimension.y);
        setDimension(sizeCache);
    }

    /**
     * Get the speed of the enemy
     * @return the speed of the entity
     */
    public float getSpeed() {
        return speed;
    }

    public float getRange() { return DEFAULT_RANGE; }

    public boolean hasTarget() { return target != null; }
    /**
     * Make the target octopus to be chased by this enemy
     *
     * @param octopus the octopus we want the enemy to focus on
     */
    public void setTarget(OctopusEntity octopus) {
        target = octopus;
    }

    /**
     * Abandon the current target and set it to null
     */
    public void abortTarget() {
        target = null;
    }

    /**
     * Return whether or not this enemy is chasing a target.
     *
     * @return whether or not this enemy is chasing a target.
     */
    public boolean isChasing() {
        return chasing;
    }

    /**
     * Get the octopus that the enemy is chasing after
     * @return the octopus entity that the enemy is chasing.
     */
    public OctopusEntity getTarget() {
        return target;
    }

    /**
     * Set the goal position for the enemy
     * @param v the position that the enemy to trying to move towards to.
     */
    public void setGoal(Vector2 v) {
        setGoal(v.x, v.y);
    }

    /**
     * Set the goal position for the enemy
     * @param x the horizontal axis position that the enemy to trying to move towards to.
     * @param y the vertical axis position that the enemy to trying to move towards to.
     */
    public void setGoal(float x, float y) {
        pathGoal.set(x, y);
    }

    /**
     * Get the goal position of the enemy
     * @return the goal position of the enemy.
     */
    public Vector2 getGoal() {
        return new Vector2(pathGoal.x, pathGoal.y);
    }

    /**
     * Set the enemy's status of aliveness
     * @param alive the boolean status of the enemy aliveness wants to be set to.
     */
    public void setAlive(boolean alive) {this.alive = alive;}

    /**
     * Get the aliveness status of the enemy entity
     * @return the boolean status of the enemy aliveness
     */
    public boolean isAlive() {return alive;}

    /**
     * Set the velocity for the enemy
     * @param v the vector that the enemy would be set as velocity
     */
    public void setVelocity(Vector2 v) {
        setVelocity(v.x, v.y);
    }

    /**
     * Set the velocity for the enemy
     * @param horizontal the horizontal velocity that the enemy would be set to
     * @param vertical the vertical velocity that the enemy would be set to
     */
    public void setVelocity(float horizontal, float vertical) {
        body.setLinearVelocity(cache.set(horizontal, vertical).nor().scl(speed));
    }

    /**
     * Get the type of enemy the current enemy is
     * @return the subtype of the enemy.
     */
    public EnemySubType getEnemySubType(){return enemySubType;}

    /**
     * Returns whether the enemy is colliding with a specific octopus
     *
     * @param oct OctopusEntity of the investigating octopus.
     *
     * @return true if they are colliding.
     */
    public boolean isColliding(OctopusEntity oct) {
        return collidingOctopi.contains(oct);
    }

    /**
     * Take note that an octopus is colliding with the enemy
     *
     * @param oct OctopusEntity of the colliding octopus.
     */
    public void addCollisionOctopi(OctopusEntity oct) {
        collidingOctopi.add(oct);
    }

    /**
     * Remove an octopus from the list of colliding octopi with the enemy
     *
     * @param oct OctopusEntity of the removed octopus.
     */
    public void removeCollisionOctopi(OctopusEntity oct) {
        collidingOctopi.remove(oct);
    }
    //#endregion
    //=================================


    /**
     * Release the fixtures for this body, reseting the shape
     *
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }
    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * Implementations of this method should NOT retain a reference to World.
     * That is a tight coupling that we should avoid.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // Get the box body from our parent class
        if (!super.activatePhysics(world)) {
            return false;
        }

        //#region INSERT CODE HERE
        // Insert code here to prevent the body from rotating
        this.setBodyType(BodyType.DynamicBody);
        this.setFixedRotation(true);
        //#endregion

        return true;
    }
    /**
     * Draws the octopus silhouette at its current position.
     *
     * @param canvas Drawing context
     */
    public void drawSilhouette(GameCanvas canvas, Vector2 scale) {
        canvas.draw(texture, Color.GRAY, texture.getRegionWidth()/2f, texture.getRegionHeight()/2f,
                getPosition().x * scale.x, getPosition().y * scale.y, getAngle(), resizeScale * 1.4f, resizeScale * 1.4f);
    }

    /**
     * Moves the octopus to its goal.
     */
    public void moveToGoal() {
        //boolean m = moving;
        float xx = getGoal().x;
        float yy = getGoal().y;
        if (getGoal() != null && getGoal().x >= 0 && getGoal().y >= 0) {
            cache.set(getGoal()).sub(body.getPosition()).rotateRad(-body.getAngle());
            /* The angle difference between the body's angle and the cache's angle in world coordinates. */
            float desiredAngle = (float) Math.atan2(-cache.x, cache.y);
            if (desiredAngle > TURN_ANGLE) {
                this.setAngularVelocity(DEFAULT_TURN_SPEED);
                turning = true;
            } else if (desiredAngle < -TURN_ANGLE) {
                this.setAngularVelocity(-(DEFAULT_TURN_SPEED));
                turning = true;
            } else {
                this.setAngularVelocity(0);
                this.setAngle(body.getAngle() + desiredAngle);
                turning = false;
            }
            this.setVelocity(cache.set(getGoal()).sub(body.getPosition()).scl(getSpeed()));
        } else {
            this.setVelocity(0 , 0);
            this.setAngularVelocity(0);
            turning = false;
        }
    }

    /**
     * Draws this enemy to the canvas. If it is being grabbed, draws it in a different color tint.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (grabbed) {
            super.draw(canvas, Color.GOLDENROD);
        } else {
            super.draw(canvas);
        }
    }
}
