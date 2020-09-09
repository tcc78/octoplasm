package edu.cornell.gdiac.octoplasm.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.octoplasm.GameCanvas;
import edu.cornell.gdiac.octoplasm.animationView.OctopusView;
import edu.cornell.gdiac.octoplasm.util.FilmStrip;

import java.util.ArrayList;
import java.util.Iterator;

/** This class implements the general octopus class
 *
 * @author Thomas Chen
 */
public class OctopusEntity extends BoxEntity {

    //=========================================================================
    //#region Fields
    /** The octopus subtypes */
    public enum OctopusSubType {
        /** Pompeius */
        EXPLODER,
        /** Flyer */
        FLYER,
        /** Teleporter */
        TELEPORTER
    }

    /** The animation types */
    public enum AnimationType {
        SMOKE,
        TIMER
    }


    private static final float INTENDED_HEIGHT_EXPLODER = 155f;
    private static final float INTENDED_HEIGHT_FLYER = 160f;
    private static final float INTENDED_HEIGHT_TELEPORTER = 160f;
    private static final float INTENDED_WIDTH = 80f;


    /** The number of frames for the explosion */
    public static final int EXPLOSION_FRAMES = 71;
    /** The number of frames for the teleport smoke */
    public static final int TELEPORT_SMOKE_FRAMES = 71;
    /** The number of frames for the flying smoke */
    public static final int FLYING_SMOKE_FRAMES = 65;
    /** The number of frames for the flying timer, the frame progresses every second (60 ticks) */
    public static final int FLYING_TIMER_FRAMES = 4;
    /** The number of frames for the fold charges */
    public static final int FOLD_CHARGES_FRAMES = 2;
    /** The number of frames an octopi cannot move after being launched*/
    private static final int LAUNCH_DURATION = 30;
    // Default physics values
    /** The density of this octopus */
    private static final float DEFAULT_DENSITY  =  1.0f;
    /** The friction of this octopus */
    private static final float DEFAULT_FRICTION = 0.1f;
    /** The restitution of this octopus */
    private static final float DEFAULT_RESTITUTION = 0f;
    /** The Default move speed for all octopi */
    private static final float DEFAULT_SPEED = 6.5f; // Note, if changing this value, must change the enemy move speed as well
    /** The Default turn speed for all octopi */
    private static final float DEFAULT_TURN_SPEED = 20.0f;
    /** The Default move speed when the flying octopus is flying */
    private static final float FLYER_SPEED = DEFAULT_SPEED * 2; // Note, if changing this value, must change the enemy move speed as well
    /** The Default turn speed when the flying octopus is flying */
    private static final float FLYER_TURN_SPEED = 20.0f;
    /** The turn angle (in radians) from the desired angle before we can start moving forward */
    private static final float TURN_ANGLE = (float) (Math.PI / 3f);
    /** The flying time in seconds */
    private static final int FLYING_TIME = 3 * 60;

    /** The texture of the exploder octopus */
    public static TextureRegion exploderTexture;
    /** The texture of the flying octopus */
    public static TextureRegion flyerTexture;
    /** The texture of the teleporter octopus */
    public static TextureRegion teleporterTexture;
    /** Texture filmstrip for the explosion */
    public static FilmStrip explosion;
    /** Texture filmstrip for the teleport */
    public static FilmStrip teleportSmoke;
    /** Texture filmstrip for the flying activation */
    public static FilmStrip flyingSmoke;
    /** Texture filmstrip for the flying activation */
    public static FilmStrip flyingTimer;
    /** Texture filmstrip for the flying activation */
    public static FilmStrip foldCharges;

    // An ArrayList for all the animations that this octopus makes
    /** Cache list for placing animations */
    private ArrayList<animationObject> animationList = new ArrayList();
    /** The subtype of the octopus */
    private OctopusSubType octopusSubType;
    /** Cached vector for vector calculations */
    private Vector2 cache;
    /** Whether the octopus's ability is active */
    private boolean abilityActive;
    /** Whether the octopus is alive */
    private boolean alive;
    /** Whether the octopus is moving */
    private boolean moving;
    /** Whether the octopus is turning */
    private boolean turning;
    /** Speed of the octopi while it is in its initial "boost" phase of movement*/
    private float boosted = DEFAULT_SPEED*2;
    /** whether the octopi just turned more than 90 degrees*/
    private boolean bigTurn = false;
    /** whether to play the boost sound*/
    private boolean playBoost = false;
    /** how long since the octopi's last boost*/
    private int boostCount = 0;
    /** whether this is the first boost*/
    private boolean firstBoost = false;
    /** whether we are doing a reboost*/
    private boolean reBoostFlag = false;
    /** how long the octopus is launched for*/
    private int launched;
    //Ability specific fields
    /** Whether or not this octopus is flying */
    private boolean flying = false;
    /** The amount of time this octopus has left to fly */
    private int flyingCounter = -1;
    /** entity that the teleport octopus wants to teleport over **/
    private Entity teleportEntity = null;
    /** number of grab charges left for Fold **/
    private int grabCharges = 0;
    /** currently grab */
    private boolean isGrab = false;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a new octopus
     * @param x The x coordinate
     * @param y The y coordinate
     * @param w The width of the octopus
     * @param h The height of the octopus
     * @param octopusSubType The subtype of the octopus
     */
    public OctopusEntity(float x, float y, float w, float h,OctopusSubType octopusSubType) {
        super(x,y,w,h,EntityType.OCTOPUS);
        switch (octopusSubType) {
            case EXPLODER:
                setTexture(exploderTexture);
                break;
            case FLYER:
                setTexture(flyerTexture);
                break;
            case TELEPORTER:
                grabCharges = 2;
                setTexture(teleporterTexture);
                break;
            default:
        }
        this.setPosition(x,y);
        this.octopusSubType = octopusSubType;
        this.teleportEntity = null;
        launched = 0;
        cache = new Vector2();
        //Set category bits to 0x0002 to disable octopus on octopus collisions, default for all other entities is 0x0001
        // For more info: https://www.iforce2d.net/b2dtut/collision-filtering
        fixture.filter.categoryBits = 0x0002;
        fixture.filter.maskBits = (short) (0xFFFF ^ 0x0002);
        setDensity(DEFAULT_DENSITY);
        setFriction(DEFAULT_FRICTION);
        setRestitution(DEFAULT_RESTITUTION);
        setAlive(true);
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters

    /**
     * returns whether to play the boost sound
     * @return whether to play the boost sound
     */
    public boolean getPlayBoost(){
        return playBoost;
    }
    /**
     * Sets the velocity of the octopus
     *
     * @param v the velocity of the octopus
     */
    public void setVelocity(Vector2 v) {
        setVelocity(v.x, v.y);
    }

    /**
     * Sets the velocity of the octopus
     *
     * @param horizontal the horizontal velocity
     * @param vertical the vertical velocity
     */
    public void setVelocity(float horizontal, float vertical) {
        playBoost = false;
        if(bigTurn||firstBoost){
            playBoost = true;
            firstBoost = false;
            bigTurn = false;
            boostCount = 120;
            boosted = DEFAULT_SPEED*2;
            reBoostFlag = false;
        }else if(boostCount == 0){
            playBoost = true;
            boostCount = 120;
            boosted = DEFAULT_SPEED*1.75f;
            reBoostFlag = true;
        }
        boostCount--;
        float len = new Vector2(horizontal,vertical).len();
        body.setLinearVelocity(cache.set(horizontal, vertical).nor().scl(
                flying ? FLYER_SPEED : (len < DEFAULT_SPEED/4 ? len*4:boosted)));
        if(boosted > DEFAULT_SPEED){
            if(!reBoostFlag) boosted -= DEFAULT_SPEED/20;
            else boosted -= DEFAULT_SPEED/30;
        }
    }

    /**
     * returns how long the octopus is launched for
     * @return how long the octopus is launched for
     */
    public int getLaunched(){
        return launched;
    }

    /**
     * sets the octopus to being launched
     */
    public void setLaunched(){
        launched = LAUNCH_DURATION;
    }

    /**
     * Returns the box width
     *
     * @return the box width
     */
    public float getWidth() {
        return getTextureWidth(octopusSubType);
    }

    /**
     * Returns the box width
     *
     * @return the box width
     */
    public float getHeight() {
        return getTextureHeight(octopusSubType);
    }

    /**
     * decrements how long the octopus is launched for
     */
    public void decrementLaunched(){
        launched--;
    }

    /**
     * TODO documentation
     *
     * @return whether the octopus is done flying
     */
    public boolean doneFlying() {return flying && flyingCounter <= 0;}

    /**
     * Decrements the flying counter
     */
    public void decFlyingCounter() {flyingCounter--;}

    /**
     * TODO documentation
     *
     * @return whether the octopus is flying
     */
    public boolean getFlying() {return flying;}

    /**
     * Sets whether the octopus is flying or not, and if so, sets the flying counter
     *
     * @param flyingSet if the octopus is flying or not
     */
    public void setFlying(boolean flyingSet) {
        if (!this.flying && flyingSet) {
            this.flying = true;
            flyingCounter = FLYING_TIME;
        }
    }

    /**
     * Sets the octopus to not flying
     */
    public void setNotFlying() {
        this.flying = false;
    }

    /**
     * TODO documentation
     *
     * @return the teleport entity if octopus is a teleporter, otherwise null
     */
    public Entity getTeleportEntity() {return getOctopusSubType() == OctopusSubType.TELEPORTER ? teleportEntity : null;}

    /**
     * Sets the teleport entity of the octopus
     *
     * @param target the targeted teleport entity
     */
    public void setTeleportEntity(Entity target) {this.teleportEntity = target;}

    /**
     * Get number of grab charges
     */
    public int getGrabCharges() {return this.grabCharges;}
    /**
     * Get number of grab charges
     */
    public void setGrabCharges(int num) {this.grabCharges = num;}
    /**
     * Decrements grab charges by one
     */
    public void decGrabCharges() {this.grabCharges--; setIsGrab(true);}

    /** set is grab */
    public void setIsGrab(boolean b) {this.isGrab = b;}

    /** get is grab */
    public boolean getIsGrab() {return this.isGrab;}

    /**
     * Returns the width of the textureregion for hitbox width
     *
     * @param OST the subtype of the octopus
     *
     * @return the width of the textureregion for hitbox width
     */
    public static float getTextureWidth(OctopusEntity.OctopusSubType OST){
        switch(OST){
            case FLYER:
                return flyerTexture.getRegionWidth();
            case EXPLODER:
                return exploderTexture.getRegionWidth();
            case TELEPORTER:
                return teleporterTexture.getRegionWidth();
            default:
                return 0f;
        }
    }
    /**
     * Returns the height of the textureregion for hitbox height
     *
     * @param OST the subtype of the octopus
     *
     * @return the height of the textureregion for hitbox height
     */
    public static float getTextureHeight(OctopusEntity.OctopusSubType OST){
        switch(OST){
            case FLYER:
                return flyerTexture.getRegionHeight();
            case EXPLODER:
                return exploderTexture.getRegionHeight();
            case TELEPORTER:
                return teleporterTexture.getRegionHeight();
            default:
                return 0f;
        }
    }

    /**
     * TODO documentation
     *
     * @return the subtype of the octopus
     */
    public OctopusSubType getOctopusSubType() {return octopusSubType;}

    /**
     * TODO documentation
     *
     * @return whether the octopus has activated its ability
     */
    public boolean isAbilityActive() { return abilityActive && alive; }

    /**
     * sets the ability to active or not active depending on the parameter
     *
     * @param abilityActive if the ability is active
     */
    public void setAbilityActive(boolean abilityActive) {
        this.abilityActive = abilityActive;
    }

    /**
     * TODO documentation
     *
     * @return if the octopus is alive
     */
    public boolean isAlive() { return alive; }

    /**
     * Sets the octopus to alive or dead
     *
     * @param alive if the octopus is alive
     */
    public void setAlive(boolean alive) { this.alive = alive; }

    /**
     * TODO documentation
     *
     * @return if the octopus is moving
     */
    public boolean isMoving() {
        moving = moving && body.getPosition().sub(getGoal()).len() > 0.1f;
        return moving && alive;
    }

    /**
     * Sets the octopus to moving or not moving
     *
     * @param moving if the octopus is moving
     */
    public void setMoving(boolean moving) { this.moving = moving; }

    /**
     * TODO documentation
     *
     * @return if the octopus is turning
     */
    public boolean isTurning() {
        return turning;
    }
    //#endregion
    //=================================

    /** Object type for placing the position of an animation and what frame it is on **/
    private class animationObject {
        /** Position to place the animation */
        private Vector2 position;
        /** Current frame of the animation */
        private int frame;
        /** The type of animation */
        private AnimationType type;

        /** Constructor that takes in position */
        private animationObject(Vector2 position, AnimationType type) {
            this.position = position;
            this.frame = 0;
            this.type = type;
        }

        /** Constructor that takes in position */
        private animationObject(Vector2 position, AnimationType type, int frame) {
            this.position = position;
            this.frame = frame;
            this.type = type;
        }

        private Vector2 getPosition() {return this.position;}
        private AnimationType getType() {return type;}
        private int getFrame() {return frame;}
        private void incFrame() {frame++;}
    }

    /**
     * Activates the physics in the world.
     *
     * @param world Box2D world to store body
     *
     * @return if physics are activated or not
     */
    public boolean activatePhysics(World world) {
        // Get the box body from our parent class
        if (!super.activatePhysics(world)) {
            return false;
        }
        this.setFixedRotation(true);
        return true;
    }

    /**
     * Moves the octopus to its goal.
     */
    public boolean moveToGoal() {
        if(getGoal() == null || !isMoving()){
            firstBoost = true;
        }
        if (getGoal() != null && isMoving()) {
            cache.set(getGoal()).sub(body.getPosition()).rotateRad(-body.getAngle());
            /* The angle difference between the body's angle and the cache's angle in world coordinates. */
            float desiredAngle = (float) Math.atan2(-cache.x, cache.y);
            if(Math.abs(desiredAngle) > Math.PI/2) bigTurn = true;
            if (desiredAngle > TURN_ANGLE) {
                if (state!=State.ability) this.setState(State.idle);
                this.setVelocity(0 , 0);
                this.setAngularVelocity(flying ? FLYER_TURN_SPEED : DEFAULT_TURN_SPEED);
                turning = true;
            } else if (desiredAngle < -TURN_ANGLE) {
                if (state!=State.ability) this.setState(State.idle);
                this.setVelocity(0 , 0);
                this.setAngularVelocity(-(flying ? FLYER_TURN_SPEED : DEFAULT_TURN_SPEED));
                turning = true;
            } else {
                //If we are close enough to the goal angle, change states, zero angular velocity, and set the angle to what it should be.
                if (state!=State.ability) this.setState(State.move);
                this.setAngularVelocity(0);
                this.setAngle(body.getAngle() + desiredAngle);
                this.setVelocity(cache.set(getGoal()).sub(body.getPosition()));
                turning = false;
            }
        } else {
            if (state!=State.ability) this.setState(State.idle);
            this.setVelocity(0 , 0);
            this.setAngularVelocity(0);
            turning = false;
        }
        return getGoal() != null && isMoving();
    }

    /**
     * Revives the current octopus entity
     *
     * @param position The position where the octopus should be respawned.
     * */
    public void respawn(Vector2 position) {
        setPosition(position);
        setAlive(true);
        setActive(true);
    }


    /**
     * Adds an animation to the animation list
     *
     * @param position the position to place the animation
     * @param type the type of animation (ex. smoke, timer)
     * */
    public void addAnimation(Vector2 position, AnimationType type) {
        animationList.add(new animationObject(position, type));
    }

    /**
     * Adds an animation to the animation list
     *
     * @param position the position to place the animation
     * @param type the type of animation (ex. smoke, timer)
     * */
    public void addAnimation(Vector2 position, AnimationType type, int frame) {
        animationList.add(new animationObject(position, type, frame));
    }

    /**
     * Draws the octopus.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (isActive() && launched == 0) {
            super.draw(canvas);
        } else if (isActive()) {
            super.draw(canvas, Color.DARK_GRAY);
        }
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
     * TODO documentation
     *
     * @param canvas
     * @param frozen
     */
    public void drawEffects(GameCanvas canvas, boolean frozen) {
        if (getOctopusSubType() == OctopusSubType.FLYER && !isAlive()) return;
        if (octopusSubType == OctopusSubType.TELEPORTER) {
            if (grabCharges > 0 && isAlive()) {
                foldCharges.setFrame(grabCharges - 1);
                canvas.draw(foldCharges, com.badlogic.gdx.graphics.Color.WHITE,foldCharges.getRegionWidth()/2f,
                        foldCharges.getRegionHeight()/2f,getPosition().x*drawScale.x,
                        getPosition().y*drawScale.y,0f,2,2);
            }
        }
        // Explosion explode Animation
        Iterator<animationObject> it = animationList.iterator();
        while (it.hasNext()) {
            animationObject a = it.next();
            Vector2 pos = a.getPosition();
            int frame = a.getFrame();
            switch (octopusSubType) {
//                case EXPLODER:
//                    if (frame >= 0 && frame < EXPLOSION_FRAMES) {
//                        explosion.setFrame(frame);
//                        if (!frozen)
//                            a.incFrame();
//                        canvas.draw(explosion, com.badlogic.gdx.graphics.Color.WHITE,explosion.getRegionWidth()/2f,
//                                explosion.getRegionHeight()/2f,pos.x*drawScale.x,pos.y*drawScale.y,0f,2,2);
//                    } else if (frame >= EXPLOSION_FRAMES) {
//                        it.remove();
////                        setActive(false);
//                    }
//                    break;
                case FLYER:
                    switch (a.getType()) {
                        // Flying smoke Animation
//                        case SMOKE:
//                            if (frame >= 0 && frame < FLYING_SMOKE_FRAMES) {
//                                flyingSmoke.setFrame(frame);
//                                if (!frozen)
//                                    a.incFrame();
//                                canvas.draw(flyingSmoke, com.badlogic.gdx.graphics.Color.WHITE,flyingSmoke.getRegionWidth()/2f,
//                                        flyingSmoke.getRegionHeight()/2f,pos.x*drawScale.x,pos.y*drawScale.y,0f,2,2);
//                            } else if (frame >= FLYING_SMOKE_FRAMES) {
//                                it.remove();
//                            }
//                            break;
                        // Flying timer Animation
                        case TIMER:
                            frame = frame/45;
                            if (frame >= 0 && frame < FLYING_TIMER_FRAMES) {
                                flyingTimer.setFrame(frame);
                                if (!frozen)
                                    a.incFrame();
                                canvas.draw(flyingTimer, com.badlogic.gdx.graphics.Color.WHITE,flyingTimer.getRegionWidth()/2f,
                                        flyingTimer.getRegionHeight()/2f,pos.x*drawScale.x,pos.y*drawScale.y,0f,2,2);
                            } else if (frame >= FLYING_TIMER_FRAMES) {
                                it.remove();
                            }
                            break;
                        default:
                    }
                    break;
                case TELEPORTER:
//                    // Teleport smoke Animation
//                    if (frame >= 0 && frame < TELEPORT_SMOKE_FRAMES) {
//                        teleportSmoke.setFrame(frame);
//                        if (!frozen)
//                            a.incFrame();
//                        canvas.draw(teleportSmoke, com.badlogic.gdx.graphics.Color.WHITE,teleportSmoke.getRegionWidth()/2f,
//                                teleportSmoke.getRegionHeight()/2f,pos.x*drawScale.x,pos.y*drawScale.y,0f,2,2);
//                    } else if (frame >= TELEPORT_SMOKE_FRAMES) {
//                        it.remove();
//                    }
//                    break;
                default:
            }
        }
    }
}
