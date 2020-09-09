package edu.cornell.gdiac.octoplasm.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ShortArray;
import edu.cornell.gdiac.octoplasm.GameCanvas;
import edu.cornell.gdiac.octoplasm.GameplayController;

import java.util.Arrays;
import java.util.LinkedList;
/*
* PolygonObstacle.java
*
* Sometimes boxes and shapes do not cut it.  In that case, you have to bring
* out the polygons.  This class is substantially more complex than the other
* physics objects, but it will allow you to draw arbitrary shapes.
*
* Be careful with modifying this file.  Even if you have had computer
* graphics, there are A LOT of subtleties in handling the physics of
* polygons.
*
* Author: Walker M. White
* Based on original PhysicsDemo Lab by Don Holden, 2007
* LibGDX version, 2/6/2015
*/
/**
* Arbitrary polygonal-shaped model to support collisions.
*
* The polygon coordinates are all in local space, relative to the object
* center.  In addition the texture coordinates are computed automatically
* from the texture size, using the same policy as PolygonSpriteBatch.
*/
public class WallEntity extends Entity{

    //=========================================================================
    //#region Fields
    /** The wall textures */
    public enum WallTexture{
        EARTH,
        PIRATE,
        WOODEN
    }

    /** An earclipping triangular to make sure we work with convex shapes */
    private static final EarClippingTriangulator TRIANGULATOR = new EarClippingTriangulator();

    /** File to texture for walls and platforms */
    private static String EARTH_FILE = "background_wall_textures/Cave_Tile_Rock.png";
    private static String PIRATE_FILE = "background_wall_textures/Pirate_Tile_Rock_Assortment01.png";
    private static String WOODEN_FILE = "background_wall_textures/Pirate_Tile_Wood.png";
    private static String WALL_EDGE_FILE = "background_wall_textures/Cave_Edge.png";
    private static String WALL_EDGE_FILE2 = "background_wall_textures/Pirate_Edge.png";
    private static String WOODEN_EDGE_FILE = "background_wall_textures/Pirate_Edge_Wood.png";
    private static String WALL_CORNER_FILE = "background_wall_textures/Cave_Edge_Corner01.png";
    /** The texture for walls and platforms */
    protected static TextureRegion earthTile;
    protected static TextureRegion pirateTile;
    protected static TextureRegion woodenTile;
    protected TextureRegion wallEdge;
    protected static TextureRegion wallEdge1;
    protected static TextureRegion wallEdge2;
    protected static TextureRegion woodenEdge;
    protected static TextureRegion wallCorner;

    private WallTexture txtr;

    /** Shape information for this physics object */
    protected PolygonShape[] shapes;
    /** Texture information for this object */
    protected PolygonRegion region;
    /** The polygon vertices, scaled for drawing */
    private float[] scaled;
    /** The triangle indices, used for drawing */
    private short[] tridx;
    /** A cache value for the fixtures (for resizing) */
    private Fixture[] geoms;
    /** The polygon bounding box (for resizing purposes) */
    private Vector2 dimension;
    /** A cache value for when the user wants to access the dimensions */
    private Vector2 sizeCache;
    /** Cache of the polygon vertices (for resizing) */
    private float[] vertices;
    /** Cache of original vertices used during level editor */
    private float[] origVertices;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a (not necessarily convex) polygon at the origin.
     *
     * The points given are relative to the polygon's origin.  They
     * are measured in physics units.  They tile the image according
     * to the drawScale (which must be set for drawing to work
     * properly).
     *
     * @param points   The polygon vertices
     */
    public WallEntity(float[] points) {
        this(points, 0, 0);
    }

    /**
     * Creates a (not necessarily convex) polygon
     *
     * The points given are relative to the polygon's origin.  They
     * are measured in physics units.  They tile the image according
     * to the drawScale (which must be set for drawing to work
     * properly).
     *
     * @param points   The polygon vertices
     * @param x  Initial x position of the polygon center
     * @param y  Initial y position of the polygon center
     */
    public WallEntity(float[] points, float x, float y) {
        super(x, y, EntityType.WALL);
        // Object has yet to be deactivated
//        toRemove = false;
        origVertices = points.clone();
        // Allocate the body information
        bodyinfo = new BodyDef();
        bodyinfo.awake  = true;
        bodyinfo.allowSleep = true;
        bodyinfo.gravityScale = 1.0f;
        bodyinfo.position.set(x,y);
        bodyinfo.fixedRotation = false;
        // Objects are physics objects unless otherwise noted
        bodyinfo.type = BodyDef.BodyType.DynamicBody;

        // Allocate the fixture information
        // Default values are okay
        fixture = new FixtureDef();

        // Allocate the mass information, but turn it off
        masseffect = false;
        massdata = new MassData();

        // Set the default drawing scale
        drawScale = new Vector2(1,1);
        origin = new Vector2();
        body = null;
        assert points.length % 2 == 0;

        // Compute the bounds.
        initShapes(points);
        initBounds();
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**returns vertices
     * @return vertices
     */
    public float[] getVertices(){
        return vertices;
    }
    /** returns original vertices
     * @return the original vertices */
    public float[] getOrigVertices() { return origVertices; }

    /**
     * TODO documentation
     *
     * @param wallTexture
     * @return
     */
    public static String getTextureFile(WallTexture wallTexture) {
        switch(wallTexture){
            case EARTH:
                return EARTH_FILE;
            case PIRATE:
                return PIRATE_FILE;
            case WOODEN:
                return WOODEN_FILE;
            default:
                return EARTH_FILE;
        }
    }

    /**
     * TODO documentation
     *
     * @param wallTexture
     * @return
     */
    public static TextureRegion getTextureRegion(WallTexture wallTexture) {
        switch(wallTexture){
            case EARTH:
                return earthTile;
            case PIRATE:
                return pirateTile;
            case WOODEN:
                return woodenTile;
            default:
                return earthTile;
        }
    }

    /**
     * TODO documentation
     *
     * @param earth
     */
    public static void setWallTextures(TextureRegion earth, TextureRegion pirate, TextureRegion wood){

        earthTile = earth;
        pirateTile = pirate;
        woodenTile = wood;
    }

    public static String getWallEdgeFile(WallTexture tex){
        switch(tex){
            case EARTH:
                return WALL_EDGE_FILE;
            case WOODEN:
                return WOODEN_EDGE_FILE;
            case PIRATE:
                return WALL_EDGE_FILE2;
        }
        return "ERROR";
    }
    public WallTexture getWallTexture(){
        return txtr;
    }

    public static void setWallEdgeTexture(TextureRegion wallEdgeTexture,TextureRegion wallEdge2Texture, TextureRegion woodenEdgeTexture){
        wallEdge1 = wallEdgeTexture;
        wallEdge2 = wallEdge2Texture;
        woodenEdge = woodenEdgeTexture;

    }
    public static String getWallCornerFile(){
        return WALL_CORNER_FILE;
    }
    public static TextureRegion getWallCornerTexture(){
        return wallCorner;
    }
    public static void setWallCornerTexture(TextureRegion wallCornerTexture){
        wallCorner = wallCornerTexture;
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
        resize(width, height);
        markDirty(true);
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
    //#endregion
    //=================================

    /**
     * Initializes the bounding box (and drawing scale) for this polygon
     */
    private void initBounds() {
        float minx = vertices[0];
        float maxx = vertices[0];
        float miny = vertices[1];
        float maxy = vertices[1];

        for(int ii = 2; ii < vertices.length; ii += 2) {
            if (vertices[ii] < minx) {
                minx = vertices[ii];
            } else if (vertices[ii] > maxx) {
                maxx = vertices[ii];
            }
            if (vertices[ii+1] < miny) {
                miny = vertices[ii+1];
            } else if (vertices[ii] > maxy) {
                maxy = vertices[ii+1];
            }
        }
        dimension = new Vector2((maxx-minx), (maxy-miny));
        sizeCache = new Vector2(dimension);
    }

    /**
     * Initializes the Box2d shapes for this polygon
     *
     * If the texture is not null, this method also allocates the PolygonRegion
     * for drawing.  However, the points in the polygon region may be rescaled
     * later.
     *
     * @param points The polygon vertices
     */
    private void initShapes(float[] points) {
        // Triangulate
        ShortArray array = TRIANGULATOR.computeTriangles(points);
        trimColinear(points,array);

        tridx = new short[array.items.length];
        System.arraycopy(array.items, 0, tridx, 0, tridx.length);

        // Allocate space for physics triangles.
        int tris = array.items.length / 3;
        vertices = new float[tris*6];
        shapes = new PolygonShape[tris];
        geoms  = new Fixture[tris];
        for(int ii = 0; ii < tris; ii++) {
            for(int jj = 0; jj < 3; jj++) {
                vertices[6*ii+2*jj  ] = points[2*array.items[3*ii+jj]  ];
                vertices[6*ii+2*jj+1] = points[2*array.items[3*ii+jj]+1];
            }
            shapes[ii] = new PolygonShape();
            shapes[ii].set(vertices,6*ii,6);
        }

        // Draw the shape with the appropriate scaling factor
        scaled = new float[points.length];
        for(int ii = 0; ii < points.length; ii+= 2) {
            scaled[ii  ] = points[ii  ]*drawScale.x;
            scaled[ii+1] = points[ii+1]*drawScale.y;
        }
        if (texture != null) {
            // WARNING: PolygonRegion constructor by REFERENCE
            region = new PolygonRegion(texture,scaled,tridx);
        }

    }

    /**
     * Removes colinear vertices from the given triangulation.
     *
     * For some reason, the LibGDX triangulator will occasionally return colinear
     * vertices.
     *
     * @param points The polygon vertices
     * @param indices The triangulation indices
     */
    private void trimColinear(float[] points, ShortArray indices) {
        int colinear = 0;
        for(int ii = 0; ii < indices.size/3-colinear; ii++) {
            float t1 = points[2*indices.items[3*ii  ]]*(points[2*indices.items[3*ii+1]+1]-points[2*indices.items[3*ii+2]+1]);
            float t2 = points[2*indices.items[3*ii+1]]*(points[2*indices.items[3*ii+2]+1]-points[2*indices.items[3*ii  ]+1]);
            float t3 = points[2*indices.items[3*ii+2]]*(points[2*indices.items[3*ii  ]+1]-points[2*indices.items[3*ii+1]+1]);
            if (Math.abs(t1+t2+t3) < 0.0000001f) {
                indices.swap(3*ii  ,  indices.size-3*colinear-3);
                indices.swap(3*ii+1,  indices.size-3*colinear-2);
                indices.swap(3*ii+2,  indices.size-3*colinear-1);
                colinear++;
            }
        }
        indices.size -= 3*colinear;
        indices.shrink();
    }

    /**
     * Resize this polygon (stretching uniformly out from origin)
     *
     * @param width The new width
     * @param height The new height
     */
    private void resize(float width, float height) {
        float scalex = width/dimension.x;
        float scaley = height/dimension.y;

        for(int ii = 0; ii < shapes.length; ii++) {
            for(int jj = 0; jj < 3; jj++) {
                vertices[6*ii+2*jj  ] *= scalex;
                vertices[6*ii+2*jj+1] *= scaley;
            }
            shapes[ii].set(vertices,6*ii,6);
        }

        // Reset the drawing shape as well
        for(int ii = 0; ii < scaled.length; ii+= 2) {
            scaled[ii  ] *= scalex;
            scaled[ii+1] *= scaley;
        }

        dimension.set(width,height);
    }

    /**
     * Create new fixtures for this body, defining the shape
     *
     * This is the primary method to override for custom physics objects
     */
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixtures
        for(int ii = 0; ii < shapes.length; ii++) {
            fixture.shape = shapes[ii];
            geoms[ii] = body.createFixture(fixture);
        }
        markDirty(false);
    }

    /**
     * Release the fixtures for this body, reseting the shape
     *
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geoms[0] != null) {
            for(Fixture fix : geoms) {
                body.destroyFixture(fix);
            }
        }
    }

    /**
     * Sets the object texture for drawing purposes.
     *
     * In order for drawing to work properly, you MUST set the drawScale.
     * The drawScale converts the physics units to pixels.
     *
     * @param s the object texture for drawing purposes.
     */
    public void setTexture(WallTexture s) {
        switch(s){
            case EARTH:
                texture = getTextureRegion(WallEntity.WallTexture.EARTH);
                txtr = WallTexture.EARTH;
                wallEdge = wallEdge1;
                break;
            case WOODEN:
                texture = getTextureRegion(WallTexture.WOODEN);
                txtr = WallTexture.WOODEN;
                wallEdge = woodenEdge;
                break;
            case PIRATE:
                texture = getTextureRegion(WallTexture.PIRATE);
                txtr = WallTexture.PIRATE;
                wallEdge = wallEdge2;
                break;
        }
        region = new PolygonRegion(texture,scaled,tridx);
    }

    /**
     * Sets the object texture for drawing purposes.
     *
     * In order for drawing to work properly, you MUST set the drawScale.
     * The drawScale converts the physics units to pixels.
     *
     * @param value the object texture for drawing purposes.
     */
    public void setTexture(TextureRegion value) {
        texture = value;
        region = new PolygonRegion(texture,scaled,tridx);
    }

    /**
     * Sets the drawing scale for this physics object
     *
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @param x the x-axis scale for this physics object
     * @param y the y-axis scale for this physics object
     */
    public void setDrawScale(float x, float y) {
        assert x != 0 && y != 0 : "Scale cannot be 0";
        float dx = x/drawScale.x;
        float dy = y/drawScale.y;
        // Reset the drawing shape as well
        for(int ii = 0; ii < scaled.length; ii+= 2) {
            scaled[ii  ] *= dx;
            scaled[ii+1] *= dy;
        }
        if (texture != null) {
            region = new PolygonRegion(texture,scaled,tridx);
        }
        drawScale.set(x,y);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (region != null) {
            canvas.draw(region, Color.WHITE,0,0,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1,1);
            float[] vertices = getOrigVertices();
            for(int i = 2; i < vertices.length+2; i = i + 2) {
                Vector2 prev;
                Vector2 curr;
                if (i < vertices.length) {
                    prev = new Vector2(vertices[i-2], vertices[i-1]);
                    curr = new Vector2(vertices[i], vertices[i+1]);
                } else {
                    prev = new Vector2(vertices[i-2], vertices[i-1]);
                    curr = new Vector2(vertices[0], vertices[1]);
                }
                Vector2 cache2 = new Vector2(prev.x - curr.x, prev.y - curr.y);
                Vector2 cache = new Vector2((prev.x + curr.x)/2, (prev.y + curr.y)/2);
                wallEdge.setRegionHeight((int)(cache2.len()*drawScale.y));
                canvas.draw(wallEdge, com.badlogic.gdx.graphics.Color.WHITE, wallEdge.getRegionWidth() / 2f,
                        wallEdge.getRegionHeight() / 2f, cache.x * drawScale.x, cache.y * drawScale.y, cache2.rotate90(1).angleRad(), 1, 1);
            }
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        for(PolygonShape tri : shapes) {
            canvas.drawPhysics(tri,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
        }
    }

}

