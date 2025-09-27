package no.monopixel.slimcolonies.api.compatibility;

/**
 * This class is to store the methods that call the methods to check for miscellaneous compatibility problems.
 */
public final class Compatibility
{

    private Compatibility()
    {
        throw new IllegalAccessError("Utility class");
    }

    public static IJeiProxy jeiProxy = new IJeiProxy() {};
}
