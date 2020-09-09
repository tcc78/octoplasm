package edu.cornell.gdiac.octoplasm.util;

/**
 * The class representation of the json file with level list information.
 *
 * @author Stephen Chin
 */
public class LevelList {
    /** The number of implemented cave levels in the game. */
    public int numberCaveLevels;
    /** The number of implemented ship levels in the game. */
    public int numberShipLevels;
    /** The number of implemented ship levels in the game. */
    public int numberOceanLevels;
    /** The text level information about the cave levels. Each sub array pertains to one level.
     *  Each sub array should be of length 3. The values in the array are encoded as:
     *      arr[i][0] = Level Name
     *      arr[i][1] = Level Flavor Text
     *      arr[i][2] = Types of octopi in this level
     *  Length of this array should equal numberCaveLevels. */
    public String[][] caveLevelsInfo;
    /** The text level information about the ship levels. Each sub array pertains to one level.
     *  Each sub array should be of length 3. The values in the array are encoded as:
     *      arr[i][0] = Level Name
     *      arr[i][1] = Level Flavor Text
     *      arr[i][2] = Types of octopi in this level
     *  Length of this array should equal numberShipLevels. */
    public String[][] shipLevelsInfo;
    /** The text level information about the ocean levels. Each sub array pertains to one level.
     *  Each sub array should be of length 3. The values in the array are encoded as:
     *      arr[i][0] = Level Name
     *      arr[i][1] = Level Flavor Text
     *      arr[i][2] = Types of octopi in this level
     *  Length of this array should equal numberOceanLevels. */
    public String[][] oceanLevelsInfo;

    /**
     * A default constructor for a LevelList object. Initializes all level numbers as zero and
     * ass leveInfo arrays with length 0x0.
     */
    public LevelList() {
        numberCaveLevels = 0;
        numberOceanLevels = 0;
        numberOceanLevels = 0;
        caveLevelsInfo = new String[0][0];
        shipLevelsInfo = new String[0][0];
        oceanLevelsInfo = new String[0][0];
    }

    /**
     * Creates a level list representation. To be well formed, level list vales should follow variable constraints
     * laid out in field documentation. For more information, look at the documentation for specific variable fields
     *
     * @param numberCaveLevels The number of cave levels in the game.
     * @param numberShipLevels The number of ship levels in the game.
     * @param numberOceanLevels The number of ocean levels in the game.
     * @param caveLevelsInfo The information related to each level in the cave.
     * @param shipLevelsInfo The information related to each level in the ship.
     * @param oceanLevelsInfo The information related to each level in the ocean.
     */
    public LevelList(int numberCaveLevels, int numberShipLevels, int numberOceanLevels,
                     String[][] caveLevelsInfo, String[][] shipLevelsInfo, String[][] oceanLevelsInfo) {
        this.numberCaveLevels = numberCaveLevels;
        this.numberShipLevels = numberShipLevels;
        this.numberOceanLevels = numberOceanLevels;
        this.caveLevelsInfo = caveLevelsInfo;
        this.shipLevelsInfo = shipLevelsInfo;
        this.oceanLevelsInfo = oceanLevelsInfo;
    }
}
