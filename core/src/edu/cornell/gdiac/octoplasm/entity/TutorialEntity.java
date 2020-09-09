package edu.cornell.gdiac.octoplasm.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TutorialEntity extends BoxEntity {
    //=========================================================================
    //#region Fields
    public enum TutorialSubType {
        CURSOR,
        MOUSE_LEFT,
        MOUSE_RIGHT,
        E_KEY,
        W_KEY,
        A_KEY,
        S_KEY,
        D_KEY,
        ESC_KEY,
        SPACE_KEY,
        TEXT1,
        TEXT2,
        TEXT3,
        TEXT4,
        TEXT5,
        TEXT6,
        TEXT7,
        TEXT8,
        TEXT9,
        TEXT10,
        TEXT11,
        TEXT12,
        TEXT13,
        TEXT14,
        TEXT15,
        TEXT16
    }

    public static TextureRegion mouseLeftTexture;
    public static TextureRegion mouseRightTexture;
    public static TextureRegion mouseCursorTexture;
    public static TextureRegion eKeyTexture;
    public static TextureRegion wKeyTexture;
    public static TextureRegion aKeyTexture;
    public static TextureRegion sKeyTexture;
    public static TextureRegion dKeyTexture;
    public static TextureRegion spaceKeyTexture;
    public static TextureRegion escKeyTexture;
    public static TextureRegion text1;
    public static TextureRegion text2;
    public static TextureRegion text3;
    public static TextureRegion text4;
    public static TextureRegion text5;
    public static TextureRegion text6;
    public static TextureRegion text7;
    public static TextureRegion text8;
    public static TextureRegion text9;
    public static TextureRegion text10;
    public static TextureRegion text11;
    public static TextureRegion text12;
    public static TextureRegion text13;
    public static TextureRegion text14;
    public static TextureRegion text15;
    public static TextureRegion text16;
    //TODO: Need E button press texture
    //TODO: Maybe need WASD button press texture

    /** Tutorial sub type */
    private TutorialSubType tutorialSubType;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a tutorial object
     */
    public TutorialEntity(float x, float y, float angle, float w, float h, TutorialSubType type) {
        super(x, y, angle, w, h, EntityType.TUTORIAL);
        switch (type) {
            case CURSOR:
                setTexture(mouseCursorTexture);
                break;
            case MOUSE_LEFT:
                setTexture(mouseLeftTexture);
                break;
            case MOUSE_RIGHT:
                setTexture(mouseRightTexture);
                break;
            case E_KEY:
                setTexture(eKeyTexture);
                break;
            case W_KEY:
                setTexture(wKeyTexture);
                break;
            case A_KEY:
                setTexture(aKeyTexture);
                break;
            case S_KEY:
                setTexture(sKeyTexture);
                break;
            case D_KEY:
                setTexture(dKeyTexture);
                break;
            case SPACE_KEY:
                setTexture(spaceKeyTexture);
                break;
            case ESC_KEY:
                setTexture(escKeyTexture);
                break;
            case TEXT1:
                setTexture(text1);
                break;
            case TEXT2:
                setTexture(text2);
                break;
            case TEXT3:
                setTexture(text3);
                break;
            case TEXT4:
                setTexture(text4);
                break;
            case TEXT5:
                setTexture(text5);
                break;
            case TEXT6:
                setTexture(text6);
                break;
            case TEXT7:
                setTexture(text7);
                break;
            case TEXT8:
                setTexture(text8);
                break;
            case TEXT9:
                setTexture(text9);
                break;
            case TEXT10:
                setTexture(text10);
                break;
            case TEXT11:
                setTexture(text11);
                break;
            case TEXT12:
                setTexture(text12);
                break;
            case TEXT13:
                setTexture(text13);
                break;
            case TEXT14:
                setTexture(text14);
                break;
            case TEXT15:
                setTexture(text15);
                break;
            case TEXT16:
                setTexture(text16);
                break;
        }
        setDensity(0f);
        setFriction(0f);
        setRestitution(0f);
        tutorialSubType = type;

        //No Collisions with anything
        //fixture.filter.categoryBits = 0x0080;
        //fixture.filter.maskBits = 0x0000;
        setSensor(true);
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters

    public TutorialSubType getTutorialSubType() {
        return tutorialSubType;
    }

    /**
     * Returns the width of the textureregion for hitbox width
     *
     * @param EST the subtype of the octopus
     *
     * @return the width of the textureregion for hitbox width
     */
    public static float getTextureWidth(TutorialSubType EST){
        switch(EST){
            case MOUSE_LEFT:
                return mouseLeftTexture.getRegionWidth();
            case MOUSE_RIGHT:
                return mouseRightTexture.getRegionWidth();
            case CURSOR:
                return mouseCursorTexture.getRegionWidth();
            case E_KEY:
                return eKeyTexture.getRegionWidth();
            case W_KEY:
                return wKeyTexture.getRegionWidth();
            case A_KEY:
                return aKeyTexture.getRegionWidth();
            case S_KEY:
                return sKeyTexture.getRegionWidth();
            case D_KEY:
                return dKeyTexture.getRegionWidth();
            case SPACE_KEY:
                return spaceKeyTexture.getRegionWidth();
            case ESC_KEY:
                return escKeyTexture.getRegionWidth();
            case TEXT1:
                return text1.getRegionWidth();
            case TEXT2:
                return text2.getRegionWidth();
            case TEXT3:
                return text3.getRegionWidth();
            case TEXT4:
                return text4.getRegionWidth();
            case TEXT5:
                return text5.getRegionWidth();
            case TEXT6:
                return text6.getRegionWidth();
            case TEXT7:
                return text7.getRegionWidth();
            case TEXT8:
                return text8.getRegionWidth();
            case TEXT9:
                return text9.getRegionWidth();
            case TEXT10:
                return text10.getRegionWidth();
            case TEXT11:
                return text11.getRegionWidth();
            case TEXT12:
                return text12.getRegionWidth();
            case TEXT13:
                return text13.getRegionWidth();
            case TEXT14:
                return text14.getRegionWidth();
            case TEXT15:
                return text15.getRegionWidth();
            case TEXT16:
                return text16.getRegionWidth();
            default:
                return 0f;
        }
    }

    /**
     * Returns the width of the textureregion for hitbox width
     *
     * @param EST the subtype of the octopus
     *
     * @return the width of the textureregion for hitbox width
     */
    public static float getTextureHeight(TutorialSubType EST){
        switch(EST){
            case MOUSE_LEFT:
                return mouseLeftTexture.getRegionHeight();
            case MOUSE_RIGHT:
                return mouseRightTexture.getRegionHeight();
            case CURSOR:
                return mouseCursorTexture.getRegionHeight();
            case E_KEY:
                return eKeyTexture.getRegionHeight();
            case W_KEY:
                return wKeyTexture.getRegionHeight();
            case A_KEY:
                return aKeyTexture.getRegionHeight();
            case S_KEY:
                return sKeyTexture.getRegionHeight();
            case D_KEY:
                return dKeyTexture.getRegionHeight();
            case SPACE_KEY:
                return spaceKeyTexture.getRegionHeight();
            case ESC_KEY:
                return escKeyTexture.getRegionHeight();
            case TEXT1:
                return text1.getRegionHeight();
            case TEXT2:
                return text2.getRegionHeight();
            case TEXT3:
                return text3.getRegionHeight();
            case TEXT4:
                return text4.getRegionHeight();
            case TEXT5:
                return text5.getRegionHeight();
            case TEXT6:
                return text6.getRegionHeight();
            case TEXT7:
                return text7.getRegionHeight();
            case TEXT8:
                return text8.getRegionHeight();
            case TEXT9:
                return text9.getRegionHeight();
            case TEXT10:
                return text10.getRegionHeight();
            case TEXT11:
                return text11.getRegionHeight();
            case TEXT12:
                return text12.getRegionHeight();
            case TEXT13:
                return text13.getRegionHeight();
            case TEXT14:
                return text14.getRegionHeight();
            case TEXT15:
                return text15.getRegionHeight();
            case TEXT16:
                return text16.getRegionHeight();
            default:
                return 0f;
        }
    }
    //#endregion
    //=================================
}
