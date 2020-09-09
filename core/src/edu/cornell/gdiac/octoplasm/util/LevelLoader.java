package edu.cornell.gdiac.octoplasm.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Json;
import com.fasterxml.jackson.core.*;
import edu.cornell.gdiac.octoplasm.EnemyController;
import edu.cornell.gdiac.octoplasm.GameplayController;
import edu.cornell.gdiac.octoplasm.ObstacleController;
import edu.cornell.gdiac.octoplasm.OctopusController;
import edu.cornell.gdiac.octoplasm.entity.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * TODO: documentation
 *
 * @author Jarrett Coleman
 */
public class LevelLoader {

    //=========================================================================
    //#region Fields
    // Physics constants for initialization
    /** Density of non-crate objects */
    public static final float BASIC_DENSITY   = 0.0f;
    /** Density of the crate objects */
    private static final float CRATE_DENSITY   = 1.0f;
    /** Friction of non-crate objects */
    public static final float BASIC_FRICTION  = 0.1f;
    /** Friction of the crate objects */
    private static final float CRATE_FRICTION  = 0.3f;
    /** Collision restitution for all objects */
    public static final float BASIC_RESTITUTION = 0.1f;
    /** Threshold for generating sound on collision */
    private static final float SOUND_THRESHOLD = 1.0f;

    //JSON parser
    public static JsonFactory factory = new JsonFactory();
    //#endregion
    //=================================

    /**
     * Parses from JSON with the following format:
     * walls: array of float[]
     * wallTexture: unndecided
     * following 3 must be same length (last is 2x length)
     * OT: array of strings corresponding to object type
     * dynamic: array of booleans corresponding to dynamic or not
     * objectPos: array of doubles corresponding to x and then y positions of each obstacle
     * following 2 must be same length (last is 2x length)
     * ET: array of strings corresponding to enemy type
     * enemyPos: array of doubles corresponding to x and then y positions of each enemy
     * following 2 must be same length (last is 2x length)
     * octT: array of strings corresponding to octopi type
     * octopusPos: array of doubles corresponding to x and then y positions of each octopus
     * bounds: array of floats corresponding to width then height of level bounds
     *
     * @param s name of JSON (not including .json or filepath to levels folder)
     *
     * @return the level model constructed from the json
     *
     * @throws IOException
     */
    public static LevelModel parseJson(String s) throws IOException {
        FileHandle f = Gdx.files.internal("levels/" + s + ".json");
        JsonParser parser = factory.createParser(f.readString());

//        JSONParser parser = new JSONParser();
//        FileReader reader = new FileReader("levels/" + s + ".json");

        LinkedList<float[]> walls = new LinkedList<>();
        LinkedList<WallEntity.WallTexture> wallTexture = new LinkedList<>();

        LinkedList<ObstacleEntity.ObstacleSubType> OT = new LinkedList<>();
        LinkedList<Vector2> objectPos = new LinkedList<>();
        LinkedList<Float> objectAngle = new LinkedList<>();

        LinkedList<EnemyEntity.EnemySubType> ET = new LinkedList<>();
        LinkedList<Vector2> enemyPos = new LinkedList<>();

        LinkedList<OctopusEntity.OctopusSubType> octT = new LinkedList<>();
        LinkedList<Vector2> octopusPos = new LinkedList<>();

        LinkedList<TutorialEntity.TutorialSubType> tutorialT = new LinkedList<>();
        LinkedList<Vector2> tutorialPos = new LinkedList<>();
        LinkedList<Float> tutorialAngle = new LinkedList<>();

        Vector2 initCamPos = new Vector2();

        Rectangle bounds = new Rectangle();
        while(!parser.isClosed()){
            JsonToken jsonToken = parser.nextToken();
            if(JsonToken.FIELD_NAME.equals(jsonToken)){
                String fieldName = parser.getCurrentName();
//                System.out.println(fieldName);

                jsonToken = parser.nextToken();
                if ("CAM".equals(fieldName)) {
                    LinkedList<Float> camPos = new LinkedList<Float>();
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            camPos.add((float) parser.getValueAsDouble());
                            jsonToken = parser.nextToken();
                        }
                    }
                    initCamPos.x = camPos.get(0);
                    initCamPos.y = camPos.get(1);
                }
                else if("walls".equals(fieldName)){
                    LinkedList<float[]> toAdd = new LinkedList<float[]>();
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        //Should be START_ARRAY of first element of array or END_ARRAY if no walls
                        jsonToken = parser.nextToken();
                        //outer array
                        LinkedList<Float> toAdd2 = new LinkedList<Float>();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            //inner array
                            if(JsonToken.START_ARRAY.equals(jsonToken)) {
                                //should be first float
                                jsonToken = parser.nextToken();
                                while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                                    toAdd2.add((float) parser.getValueAsDouble());
                                    //should be next float or END_ARRAY
                                    jsonToken = parser.nextToken();
                                }
                                float[] floats = new float[toAdd2.size()];
                                int i = 0;
                                for (Float fl : toAdd2){
                                    floats[i++] = fl;
                                }
                                toAdd2 = new LinkedList<>();
                                toAdd.add(floats);
                                //should be START_ARRAY of next wall or END_ARRAY if no more walls
                                jsonToken = parser.nextToken();
                            }
                        }

                    }
                    walls = toAdd;
                } else if ("wallTexture".equals(fieldName)) {
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            String str = parser.getValueAsString();
                            switch (str) {
                                //TODO: list out all cases
                                //if not a recognized wall type, throw error
                                case "EARTH":
                                    wallTexture.add(WallEntity.WallTexture.EARTH);
                                    break;
                                case "WOODEN":
                                    wallTexture.add(WallEntity.WallTexture.WOODEN);
                                    break;
                                case "PIRATE":
                                    wallTexture.add(WallEntity.WallTexture.PIRATE);
                                    break;
                                default:
                                    System.out.println("Error: unrecognized wall texture received");
                                    break;
                            }
                            jsonToken = parser.nextToken();
                        }
                    }
                } //Objects
                else if("OT".equals(fieldName)){
                    LinkedList<ObstacleEntity.ObstacleSubType> toAdd = new LinkedList<>();
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        //Should be START_ARRAY of first element of array or END_ARRAY if no obstacles
                        //should be first string
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            switch(parser.getValueAsString()){
                                case "GOAL":
                                    toAdd.add(ObstacleEntity.ObstacleSubType.GOAL);
                                    break;
//                                case "DESTRUCTIBLE_WALL":
//                                    toAdd.add(ObstacleEntity.ObstacleSubType.DESTRUCTIBLE_WALL);
//                                    break;
                                case "FIGHT_WALL":
                                    toAdd.add(ObstacleEntity.ObstacleSubType.FIGHT_WALL);
                                    break;
                                case "FLIGHT_WALL":
                                    toAdd.add(ObstacleEntity.ObstacleSubType.FLIGHT_WALL);
                                    break;
                                case "FOLD_WALL":
                                    toAdd.add(ObstacleEntity.ObstacleSubType.FOLD_WALL);
                                    break;
                                case "RESPAWN":
                                    toAdd.add(ObstacleEntity.ObstacleSubType.RESPAWN);
                                    break;
                                default:
                                    System.out.println("Error, unrecognized obstacle type");
                                    break;
                            }
                            //should be next string or END_ARRAY
                            jsonToken = parser.nextToken();
                        }

                    }
                    OT = toAdd;
                } else if("objectPos".equals(fieldName)){
                    LinkedList<Vector2> toAdd = new LinkedList<Vector2>();
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        //should be first element (x value)
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            float x = (float)parser.getValueAsDouble();
                            //should be next element (y value)
                            jsonToken = parser.nextToken();
                            float y = (float)parser.getValueAsDouble();
                            toAdd.add(new Vector2(x,y));
                            //should be next element or END_ARRAY
                            jsonToken = parser.nextToken();
                        }

                    }
                    objectPos = toAdd;
                } else if("objectAngle".equals(fieldName)){
                    LinkedList<Float> toAdd = new LinkedList<Float>();
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        //should be first element (x value)
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            float x = (float)parser.getValueAsDouble();
                            toAdd.add(x);
                            //should be next element or END_ARRAY
                            jsonToken = parser.nextToken();
                        }

                    }
                    objectAngle = toAdd;
                } //Enemies
                else if("ET".equals(fieldName)){
                    LinkedList<EnemyEntity.EnemySubType> toAdd = new LinkedList<EnemyEntity.EnemySubType>();
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        //should be first string
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            switch(parser.getValueAsString()){
                                case "NORMAL_ENEMY":
                                    toAdd.add(EnemyEntity.EnemySubType.NORMAL_ENEMY);
                                    break;
                                case "ARMORED_ENEMY":
                                    toAdd.add(EnemyEntity.EnemySubType.ARMORED_ENEMY);
                                    break;
                                case "SPIKED_ENEMY":
                                    toAdd.add(EnemyEntity.EnemySubType.SPIKED_ENEMY);
                                    break;
                                case "HOLE_ENEMY":
                                    toAdd.add(EnemyEntity.EnemySubType.HOLE_ENEMY);
                                    break;
                                case "INVINCIBLE_ENEMY":
                                    toAdd.add(EnemyEntity.EnemySubType.INVINCIBLE_ENEMY);
                                    break;
                                default:
                                    System.out.println("Error, unrecognized enemy type");
                                    break;
                            }
                            //should be next string or END_ARRAY
                            jsonToken = parser.nextToken();
                        }

                    }
                    ET = toAdd;
                } else if("enemyPos".equals(fieldName)){
                    LinkedList<Vector2> toAdd = new LinkedList<Vector2>();
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        //should be first element (x value)
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            float x = (float)parser.getValueAsDouble();
                            //should be next element (y value)
                            jsonToken = parser.nextToken();
                            float y = (float)parser.getValueAsDouble();
                            toAdd.add(new Vector2(x,y));
                            //should be next element or END_ARRAY
                            jsonToken = parser.nextToken();
                        }

                    }
                    enemyPos = toAdd;
                } //Octopi
                else if("octT".equals(fieldName)){
                    LinkedList<OctopusEntity.OctopusSubType> toAdd = new LinkedList<OctopusEntity.OctopusSubType>();
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        //should be first string
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            switch(parser.getValueAsString()){
                                case "EXPLODER":
                                    toAdd.add(OctopusEntity.OctopusSubType.EXPLODER);
                                    break;
                                case "FLYER":
                                    toAdd.add(OctopusEntity.OctopusSubType.FLYER);
                                    break;
                                case "TELEPORTER":
                                    toAdd.add(OctopusEntity.OctopusSubType.TELEPORTER);
                                    break;
                                default:
                                    System.out.println("Error, unrecognized octopi type");
                                    break;
                            }
                            //should be next string or END_ARRAY
                            jsonToken = parser.nextToken();
                        }
                    }
                    octT = toAdd;
                } else if("octopusPos".equals(fieldName)) {
                    LinkedList<Vector2> toAdd = new LinkedList<Vector2>();
                    if (JsonToken.START_ARRAY.equals(jsonToken)) {
                        //should be first element (x value)
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            float x = (float) parser.getValueAsDouble();
                            //should be next element (y value)
                            jsonToken = parser.nextToken();
                            float y = (float) parser.getValueAsDouble();
                            toAdd.add(new Vector2(x, y));
                            //should be next element or END_ARRAY
                            jsonToken = parser.nextToken();
                        }
                    }
                    octopusPos = toAdd;
                } //Bounds
                else if("bounds".equals(fieldName)){
                    if (JsonToken.START_ARRAY.equals(jsonToken)) {
                        //should be first element (x value)
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            float width = (float) parser.getValueAsDouble();
                            //should be next element (y value)
                            jsonToken = parser.nextToken();
                            float height = (float) parser.getValueAsDouble();
                            bounds = new Rectangle(0,0,width,height);
                            //should be next element or END_ARRAY
                            jsonToken = parser.nextToken();
                        }
                    }
                } //Tutorial
                else if("TT".equals(fieldName)) {
                    LinkedList<TutorialEntity.TutorialSubType> toAdd = new LinkedList<>();
                    if (JsonToken.START_ARRAY.equals(jsonToken)) {
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            switch(parser.getValueAsString()){
                                case "MOUSE_LEFT":
                                    toAdd.add(TutorialEntity.TutorialSubType.MOUSE_LEFT);
                                    break;
                                case "MOUSE_RIGHT":
                                    toAdd.add(TutorialEntity.TutorialSubType.MOUSE_RIGHT);
                                    break;
                                case "CURSOR":
                                    toAdd.add(TutorialEntity.TutorialSubType.CURSOR);
                                    break;
                                case "E_KEY":
                                    toAdd.add(TutorialEntity.TutorialSubType.E_KEY);
                                    break;
                                case "W_KEY":
                                    toAdd.add(TutorialEntity.TutorialSubType.W_KEY);
                                    break;
                                case "A_KEY":
                                    toAdd.add(TutorialEntity.TutorialSubType.A_KEY);
                                    break;
                                case "S_KEY":
                                    toAdd.add(TutorialEntity.TutorialSubType.S_KEY);
                                    break;
                                case "D_KEY":
                                    toAdd.add(TutorialEntity.TutorialSubType.D_KEY);
                                    break;
                                case "SPACE_KEY":
                                    toAdd.add(TutorialEntity.TutorialSubType.SPACE_KEY);
                                    break;
                                case "ESC_KEY":
                                    toAdd.add(TutorialEntity.TutorialSubType.ESC_KEY);
                                    break;
                                case "TEXT1":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT1);
                                    break;
                                case "TEXT2":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT2);
                                    break;
                                case "TEXT3":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT3);
                                    break;
                                case "TEXT4":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT4);
                                    break;
                                case "TEXT5":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT5);
                                    break;
                                case "TEXT6":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT6);
                                    break;
                                case "TEXT7":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT7);
                                    break;
                                case "TEXT8":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT8);
                                    break;
                                case "TEXT9":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT9);
                                    break;
                                case "TEXT10":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT10);
                                    break;
                                case "TEXT11":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT11);
                                    break;
                                case "TEXT12":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT12);
                                    break;
                                case "TEXT13":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT13);
                                    break;
                                case "TEXT14":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT14);
                                    break;
                                case "TEXT15":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT15);
                                    break;
                                case "TEXT16":
                                    toAdd.add(TutorialEntity.TutorialSubType.TEXT16);
                                    break;
                                default:
                                    System.out.println("Error, unrecognized tutorial type");
                                    break;
                            }
                            //should be next string or END_ARRAY
                            jsonToken = parser.nextToken();
                        }
                    }
                    tutorialT = toAdd;
                }
                else if("tutorialAngle".equals(fieldName)){
                    LinkedList<Float> toAdd = new LinkedList<>();
                    if(JsonToken.START_ARRAY.equals(jsonToken)) {
                        //should be first element (x value)
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            float x = (float)parser.getValueAsDouble();
                            toAdd.add(x);
                            //should be next element or END_ARRAY
                            jsonToken = parser.nextToken();
                        }
                    }
                    tutorialAngle = toAdd;
                }
                else if("tutorialPos".equals(fieldName)){
                    LinkedList<Vector2> toAdd = new LinkedList<>();
                    if (JsonToken.START_ARRAY.equals(jsonToken)) {
                        //should be first element (x value)
                        jsonToken = parser.nextToken();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            float x = (float) parser.getValueAsDouble();
                            //should be next element (y value)
                            jsonToken = parser.nextToken();
                            float y = (float) parser.getValueAsDouble();
                            toAdd.add(new Vector2(x, y));
                            //should be next element or END_ARRAY
                            jsonToken = parser.nextToken();
                        }
                    }
                    tutorialPos = toAdd;
                }
                else{
                    System.out.println("Error: unrecognized field name");
                }
            }
        }
        return new LevelModel(walls,wallTexture,OT,objectPos, objectAngle,
                ET,enemyPos,octT,octopusPos, tutorialT, tutorialPos, tutorialAngle,
                bounds, initCamPos);
    }

    /**
     * Immediately adds the object to the physics world
     *
     * @param obj The object to add
     */
    public static void addObject(Entity obj, PooledList<Entity> objects, World world, Rectangle bounds) {
        assert inBounds(obj,bounds) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    //TODO: why is this not used?
    /**
     * Adds a physics object in to the insertion queue.
     *
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     *
     * @param obj The object to add
     */
    public static void addQueuedObject(Entity obj, PooledList<Entity> addQueue, Rectangle bounds) {
        assert inBounds(obj,bounds) : "Object is not in bounds";
        addQueue.add(obj);
    }



    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    public static boolean inBounds(Entity obj, Rectangle bounds) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }



    /**
     * Lays out the game geography.
     *
     * @param world the world in which to put the objects
     * @param objects the list of objects to update from the level model
     * @param model the model from which to get the objects
     * @param scale the scale of the world
     * @param OC the octopuscontroller
     * @param EC the enemycontroller
     * @return the bounds of the level
     */
    public static Rectangle populateLevel(World world, PooledList<Entity> objects, LevelModel model, Vector2 scale,
                                          OctopusController OC, EnemyController EC, ObstacleController BC, GameplayController.GameWorld area)
            throws Exception {
        verifyLevelModel(model);
        //Add walls
        int i = 1;
        for(float[] WALL1: model.walls){
            float[] wallsScaled = new float[WALL1.length];
            //descale walls so that they can be rescaled for various screen sizes
            int j = 0;
            for(float f : WALL1){
                wallsScaled[j] = j/2 == 0 ? f*scale.x : f*scale.y;
                j++;
            }
            WallEntity wall1 = new WallEntity(wallsScaled, 0, 0);
            wall1.setBodyType(BodyDef.BodyType.StaticBody);
            wall1.setDensity(BASIC_DENSITY);
            wall1.setFriction(BASIC_FRICTION);
            wall1.setRestitution(BASIC_RESTITUTION);
            wall1.setDrawScale(scale);
            wall1.setTexture(model.wallTexture.get(i-1));
            wall1.setName("wall"+i);
            addObject(wall1, objects, world, model.bounds);
            i++;
        }
        //Add Obstacles
        ListIterator<ObstacleEntity.ObstacleSubType> obstacleType = model.OT.listIterator();
        ListIterator<Vector2> obstaclePos = model.objectPos.listIterator();
        ListIterator<Float> obstacleAngle = model.objectAngle.listIterator();
        while(obstacleType.hasNext()){
            ObstacleEntity.ObstacleSubType obstacleT = obstacleType.next();
            Vector2 obstacleP = obstaclePos.next();
            Float obstacleAng = obstacleAngle.next();
            ObstacleEntity obstacle = new ObstacleEntity(obstacleP.x*scale.x, obstacleP.y*scale.y, obstacleAng,
                    ObstacleEntity.getTextureWidth(obstacleT)/scale.x,
                    ObstacleEntity.getTextureHeight(obstacleT)/scale.y,obstacleT, area);
            obstacle.setDrawScale(scale);
            addObject(obstacle, objects, world, model.bounds);
            BC.addObstacle(obstacle);
        }
        //Add Tutorial Icons
        ListIterator<TutorialEntity.TutorialSubType> tutorialType = model.TT.listIterator();
        ListIterator<Vector2> tutorialPos = model.tutorialPos.listIterator();
        ListIterator<Float> tutorialAngle = model.tutorialAngle.listIterator();
        while(tutorialType.hasNext()){
            TutorialEntity.TutorialSubType tutorialSubType = tutorialType.next();
            Vector2 tutorialP = tutorialPos.next();
            Float tutorialAng = tutorialAngle.next();
            TutorialEntity tutorialEntity = new TutorialEntity(tutorialP.x*scale.x, tutorialP.y*scale.y, tutorialAng,
                    TutorialEntity.getTextureWidth(tutorialSubType)/scale.x,
                    TutorialEntity.getTextureHeight(tutorialSubType)/scale.x, tutorialSubType);
            tutorialEntity.setDrawScale(scale);
            addObject(tutorialEntity, objects, world, model.bounds);
            BC.addTutorial(tutorialEntity);
        }
        //Add enemies
        ListIterator<EnemyEntity.EnemySubType> enemyType = model.ET.listIterator();
        ListIterator<Vector2> enemyPos = model.enemyPos.listIterator();
        while(enemyType.hasNext()){
            EnemyEntity.EnemySubType enemyT = enemyType.next();
            Vector2 enemyP = enemyPos.next();
            EnemyEntity enemy = new EnemyEntity(enemyP.x*scale.x,enemyP.y*scale.y,
                    EnemyEntity.getTextureWidth(enemyT)/scale.x,
                    EnemyEntity.getTextureHeight(enemyT)/scale.y,enemyT);
            enemy.setDrawScale(scale);
            addObject(enemy,objects,world, model.bounds);
            EC.addEnemy(enemy);
        }
        //Add Octopi
        i = 0;
        ListIterator<OctopusEntity.OctopusSubType> octopusType = model.octT.listIterator();
        ListIterator<Vector2> octopusPos = model.octopusPos.listIterator();
        while(octopusType.hasNext()){
            OctopusEntity.OctopusSubType octopusT = octopusType.next();
            Vector2 octopusP = octopusPos.next();
            OctopusEntity octopus = new OctopusEntity(octopusP.x*scale.x,octopusP.y*scale.y,
                    OctopusEntity.getTextureWidth(octopusT)/scale.x,
                    OctopusEntity.getTextureHeight(octopusT)/scale.y,octopusT);
            octopus.setDrawScale(scale);
            addObject(octopus,objects, world, model.bounds);
            OC.addOctopus(octopus);
            i++;
        }
        return model.bounds;
    }

    /**
     * Verifies a level model to be containing a valid level.
     * Checks list lengths.
     *
     * @param model
     */
    private static void verifyLevelModel(LevelModel model) throws Exception {
        boolean valid = (model.OT.size() == model.objectPos.size() && model.OT.size() == model.objectAngle.size());
        valid = valid && (model.ET.size() == model.enemyPos.size());
        valid = valid && (model.octT.size() == model.octopusPos.size());
        valid = valid && (model.TT.size() == model.tutorialAngle.size());
        valid = valid && (model.TT.size() == model.tutorialPos.size());
        valid = valid && (model.walls.size() == model.wallTexture.size());
        if (!valid) {
            throw new Exception("Level not valid");
        }
    }

    /**
     * Creates a json file from a saved levelmodel
     *
     * @param model the model from which to create the json
     * @return the json created from the model
     */
    public static void generateJson(LevelModel model, String path) throws IOException {
        JsonGenerator generator = factory.createGenerator(new File("levels/" + path + ".json"), JsonEncoding.UTF8);
        generator.writeStartObject();
        //Walls
        generator.writeArrayFieldStart("walls");
        ListIterator<float[]> iterator = model.walls.listIterator();
        while(iterator.hasNext()){
            generator.writeStartArray();
            float[] wall = iterator.next();
            for(int i = 0; i < wall.length;i++){
                generator.writeNumber(wall[i]);
            }
            generator.writeEndArray();
        }
        generator.writeEndArray();
        //Initial Camera Position
        generator.writeArrayFieldStart("CAM");
        generator.writeNumber(model.initCamPos.x);
        generator.writeNumber(model.initCamPos.y);
        generator.writeEndArray();
        //WallTexture
        generator.writeArrayFieldStart("wallTexture");
        ListIterator<WallEntity.WallTexture> walliterator = model.wallTexture.listIterator();
        while(walliterator.hasNext()){
            WallEntity.WallTexture wall = walliterator.next();
            switch(wall){
                case PIRATE:
                    generator.writeString("PIRATE");
                    break;
                case WOODEN:
                    generator.writeString("WOODEN");
                    break;
                case EARTH:
                    generator.writeString("EARTH");
                    break;
            }
        }
        generator.writeEndArray();
        //Objects
        generator.writeArrayFieldStart("OT");
        ListIterator<ObstacleEntity.ObstacleSubType> OTiterator = model.OT.listIterator();
        while(OTiterator.hasNext()){
            ObstacleEntity.ObstacleSubType s = OTiterator.next();
            switch(s){
                case GOAL:
                    generator.writeString("GOAL");
                    break;
                case RESPAWN:
                    generator.writeString("RESPAWN");
                    break;
                case FIGHT_WALL:
                    generator.writeString("FIGHT_WALL");
                    break;
                case FLIGHT_WALL:
                    generator.writeString("FLIGHT_WALL");
                    break;
                case FOLD_WALL:
                    generator.writeString("FOLD_WALL");
                    break;
//                case DESTRUCTIBLE_WALL:
//                    generator.writeString("DESTRUCTIBLE_WALL");
//                    break;
            }
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("objectPos");
        ListIterator<Vector2> pIterator = model.objectPos.listIterator();
        while(pIterator.hasNext()){
            Vector2 v = pIterator.next();
            generator.writeNumber(v.x);
            generator.writeNumber(v.y);
        }
        generator.writeEndArray();
        //Object angles
        generator.writeArrayFieldStart("objectAngle");
        ListIterator<Float> angIterator = model.objectAngle.listIterator();
        while(angIterator.hasNext()){
            Float f = angIterator.next();
            generator.writeNumber(f);
        }
        generator.writeEndArray();
        //Enemies
        generator.writeArrayFieldStart("ET");
        ListIterator<EnemyEntity.EnemySubType> ETiterator = model.ET.listIterator();
        while(ETiterator.hasNext()){
            EnemyEntity.EnemySubType s = ETiterator.next();
            switch(s){
                case NORMAL_ENEMY:
                    generator.writeString("NORMAL_ENEMY");
                    break;
                case ARMORED_ENEMY:
                    generator.writeString("ARMORED_ENEMY");
                    break;
                case SPIKED_ENEMY:
                    generator.writeString("SPIKED_ENEMY");
                    break;
                case HOLE_ENEMY:
                    generator.writeString("HOLE_ENEMY");
                    break;
                case INVINCIBLE_ENEMY:
                    generator.writeString("INVINCIBLE_ENEMY");
                    break;
            }
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("enemyPos");
        pIterator = model.enemyPos.listIterator();
        while(pIterator.hasNext()){
            Vector2 v = pIterator.next();
            generator.writeNumber(v.x);
            generator.writeNumber(v.y);
        }
        generator.writeEndArray();
        //Octopi
        generator.writeArrayFieldStart("octT");
        ListIterator<OctopusEntity.OctopusSubType> octTiterator = model.octT.listIterator();
        while(octTiterator.hasNext()){
            OctopusEntity.OctopusSubType s = octTiterator.next();
            switch(s){
                case FLYER:
                    generator.writeString("FLYER");
                    break;
                case TELEPORTER:
                    generator.writeString("TELEPORTER");
                    break;
                case EXPLODER:
                    generator.writeString("EXPLODER");
                    break;
            }
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("octopusPos");
        pIterator = model.octopusPos.listIterator();
        while(pIterator.hasNext()){
            Vector2 v = pIterator.next();
            generator.writeNumber(v.x);
            generator.writeNumber(v.y);
        }
        generator.writeEndArray();
        //Tutorial Objects
        generator.writeArrayFieldStart("TT");
        ListIterator<TutorialEntity.TutorialSubType> tutIterator = model.TT.listIterator();
        while(tutIterator.hasNext()){
            TutorialEntity.TutorialSubType s = tutIterator.next();
            switch(s){
                case MOUSE_LEFT:
                    generator.writeString("MOUSE_LEFT");
                    break;
                case MOUSE_RIGHT:
                    generator.writeString("MOUSE_RIGHT");
                    break;
                case CURSOR:
                    generator.writeString("CURSOR");
                    break;
                case E_KEY:
                    generator.writeString("E_KEY");
                    break;
                case W_KEY:
                    generator.writeString("W_KEY");
                    break;
                case A_KEY:
                    generator.writeString("A_KEY");
                    break;
                case S_KEY:
                    generator.writeString("S_KEY");
                    break;
                case D_KEY:
                    generator.writeString("D_KEY");
                    break;
                case SPACE_KEY:
                    generator.writeString("SPACE_KEY");
                    break;
                case ESC_KEY:
                    generator.writeString("ESC_KEY");
                    break;
                case TEXT1:
                    generator.writeString("TEXT1");
                    break;
                case TEXT2:
                    generator.writeString("TEXT2");
                    break;
                case TEXT3:
                    generator.writeString("TEXT3");
                    break;
                case TEXT4:
                    generator.writeString("TEXT4");
                    break;
                case TEXT5:
                    generator.writeString("TEXT5");
                    break;
                case TEXT6:
                    generator.writeString("TEXT6");
                    break;
                case TEXT7:
                    generator.writeString("TEXT7");
                    break;
                case TEXT8:
                    generator.writeString("TEXT8");
                    break;
                case TEXT9:
                    generator.writeString("TEXT9");
                    break;
                case TEXT10:
                    generator.writeString("TEXT10");
                    break;
                case TEXT11:
                    generator.writeString("TEXT11");
                    break;
                case TEXT12:
                    generator.writeString("TEXT12");
                    break;
                case TEXT13:
                    generator.writeString("TEXT13");
                    break;
                case TEXT14:
                    generator.writeString("TEXT14");
                    break;
                case TEXT15:
                    generator.writeString("TEXT15");
                    break;
                case TEXT16:
                    generator.writeString("TEXT16");
                    break;
            }
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("tutorialPos");
        pIterator = model.tutorialPos.listIterator();
        while(pIterator.hasNext()){
            Vector2 v = pIterator.next();
            generator.writeNumber(v.x);
            generator.writeNumber(v.y);
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("tutorialAngle");
        angIterator = model.tutorialAngle.listIterator();
        while(angIterator.hasNext()){
            Float f = angIterator.next();
            generator.writeNumber(f);
        }
        generator.writeEndArray();
        //bounds
        generator.writeArrayFieldStart("bounds");
        generator.writeNumber(model.bounds.width);
        generator.writeNumber(model.bounds.height);
        generator.writeEndArray();
        generator.writeEndObject();
        generator.close();
    }
}