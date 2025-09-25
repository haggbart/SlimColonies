package no.monopixel.slimcolonies.api.util.constant;

import org.jetbrains.annotations.NonNls;

/**
 * Constants for suppression keys.
 */
public final class Suppression
{
    /**
     * Suppress warnings for unchecked type conversions.
     * <p>
     * We sometimes need this for complicated typings.
     */
    @NonNls
    public static final String UNCHECKED = "unchecked";

    /**
     * Suppress warnings for raw type annotation.
     * <p>
     * We sometimes need this for complicated typings.
     */
    @NonNls
    public static final String RAWTYPES = "rawtypes";

    /**
     * Suppress warnings for deprecations.
     * <p>
     * We sometimes need this for minecraft methods we have to keep support for.
     */
    @NonNls
    public static final String DEPRECATION = "deprecation";


    private Suppression()
    {
        //empty default
    }
}
