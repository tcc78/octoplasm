/*
 * BoxObject.java
 *
 * Given the name Box2D, this is your primary model class.  Most of the time,
 * unless it is a player controlled avatar, you do not even need to subclass
 * BoxObject.  Look through the code and see how many times we use this class.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.octoplasm.entity;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.octoplasm.GameCanvas;
import edu.cornell.gdiac.octoplasm.animationView.CharacterView;

import java.util.LinkedList;

/**
 * Box-shaped model to support collisions.
 *
 * Unless otherwise specified, the center of mass is as the center.
 */
public class BoxEntity extends Entity {

    //=========================================================================
    //#region Fields
    public State state = State.idle;
    public boolean isRespawned = false;
    public boolean stateChanged;
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
    /** The octopus's current goal position, given in world coordinates */
    private Vector2 pathGoal;
    /** The entity that this entity is currently colliding with (only used for selector)*/
    private LinkedList<Entity> collidingWith = new LinkedList<Entity>();
    /** int for selector that states whether goals may be placed */
    private int wallsTouched;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a new box object.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x Initial x position of the box center
     * @param y Initial y position of the box center
     * @param width	The object width in physics units
     * @param height The object width in physics units
     * @param entityType The entity type of this
     */
    public BoxEntity(float x, float y, float width, float height, EntityType entityType) {
        super(x, y, entityType);

        float w = width * getResizeScale();
        float h = height * getResizeScale();
        dimension = new Vector2(w,h);
        sizeCache = new Vector2();
        shape = new PolygonShape();
        vertices = new float[8];
        geometry = null;
        wallsTouched = 0;
        pathGoal = new Vector2();

        resize(w, h);
    }

    public BoxEntity(float x, float y, float angle, float width, float height, EntityType entityType) {
        this(x, y, width, height, entityType);
        this.setAngle(angle);
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters

    /**
     * returns number of walls touched (for selector and goal placing)
     * @return number of walls touched
     */
    public int getWallsTouched(){
        return wallsTouched;
    }

    /**
     * increments the number of walls touched (for selector and goal placing)
     */
    public void incrementWallsTouched(){
        wallsTouched++;
    }

    /**
     * decrements the number of walls touched (for selector and goal placing)
     */
    public void decrementWallsTouched(){
        wallsTouched--;
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
     * @param value the dimensions of this box
     */
    public void setDimension(Vector2 value) {
        setDimension(value.x, value.y);
    }

    /**
     * Sets the dimensions of this box
     *
     * @param width The width of this box
     * @param height The height of this box
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
        return dimension.x;
    }

    /**
     * Sets the box width
     *
     * @param value the box width
     */
    public void setWidth(float value) {
        sizeCache.set(value,dimension.y);
        setDimension(sizeCache);
    }

    /**
     * Returns the box height
     *
     * @return the box height
     */
    public float getHeight() {
        return dimension.y;
    }

    /**
     * Sets the box height
     *
     * @param value the box height
     */
    public void setHeight(float value) {
        sizeCache.set(dimension.x,value);
        setDimension(sizeCache);
    }

    /**
     * Returns the goal position
     *
     * @return the goal position
     */
    public Vector2 getGoal() {
        return new Vector2(pathGoal.x, pathGoal.y);
    }

    /**
     * Sets the goal position
     *
     * @param v the new goal position
     */
    public void setGoal(Vector2 v) {
        setGoal(v.x, v.y);
    }

    /**
     * Sets the goal position
     *
     * @param x the x value of the goal position
     * @param y the y value of the goal position
     */
    public void setGoal(float x, float y) {
        pathGoal.set(x, y);
    }
    //#endregion Getters and Setters
    //=================================

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     *
     * @param width the box width
     * @param height the box height
     */
    public void resize(float width, float height) {
        // Make the box with the center in the center
        vertices[0] = -width/2.0f;
        vertices[1] = -height/2.0f;
        vertices[2] = -width/2.0f;
        vertices[3] =  height/2.0f;
        vertices[4] =  width/2.0f;
        vertices[5] =  height/2.0f;
        vertices[6] =  width/2.0f;
        vertices[7] = -height/2.0f;
        shape.set(vertices);
    }

    /**
     * Create new fixtures for this body, defining the shape
     *
     * This is the primary method to override for custom physics objects
     */
    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixture
        fixture.shape = shape;
        geometry = body.createFixture(fixture);
        markDirty(false);
    }

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
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas  Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(shape,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }

    public enum State {
        idle, move, death, ability, win
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        if (state == State.win && this.state == State.win) return;
        if (state == this.state || ((this.state == State.death && !this.isRespawned) && state != State.win)) return;
        stateChanged = true;
        this.state = state;
    }
}