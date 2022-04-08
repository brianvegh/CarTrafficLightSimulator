//  Brian Vegh
//  UMCG CMSC-335 Project 3
//  December 15, 2020
//  Debug.java - Utility class that alows print statements to be turned on or off throughout program


/**
 * Utility class that alows print statements to be turned on or off throughout program
 */
public final class Debug {

    static boolean DEBUG_ON = false;
    private Debug() {
    }

    /**
     * prints Objext toString with System.out if DEBUG_ON = true
     * @param o
     */
    public static void print(Object o) {
        String string = o.toString();
        if (DEBUG_ON) {
            string = "DEBUG_PRINTER---- \"" + string + "\"";
            System.out.println(string);
        }
    }

    /**
     * prints String toString with System.out if DEBUG_ON = true
     */
    public static void print(String string) {
        if (DEBUG_ON) {
            string = "DEBUG_PRINTER---- \"" + string + "\"";
            System.out.println(string);
        }
    }
    /**
     * turns Debug on
     */
    public static void TURN_ON() {
        DEBUG_ON = true;
    }
    /**
     * turns Debug off
     */
    public static void TURN_OFF() {
        DEBUG_ON = false;
    }


}
