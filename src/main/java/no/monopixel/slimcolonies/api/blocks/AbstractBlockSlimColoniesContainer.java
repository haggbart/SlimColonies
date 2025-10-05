package no.monopixel.slimcolonies.api.blocks;

public abstract class AbstractBlockSlimColoniesContainer<B extends AbstractBlockSlimColoniesContainer<B>> extends AbstractBlockSlimColonies<B>
{
    public AbstractBlockSlimColoniesContainer(final Properties properties)
    {
        super(properties);
    }
}
