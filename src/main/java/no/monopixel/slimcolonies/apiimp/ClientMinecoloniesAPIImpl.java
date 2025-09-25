package no.monopixel.slimcolonies.apiimp;

import no.monopixel.slimcolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import no.monopixel.slimcolonies.core.client.render.modeltype.registry.ModelTypeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;

public class ClientMinecoloniesAPIImpl extends CommonMinecoloniesAPIImpl
{
    private final IModelTypeRegistry modelTypeRegistry = new ModelTypeRegistry();

    @Override
    public IModelTypeRegistry getModelTypeRegistry()
    {
        return modelTypeRegistry;
    }

    @Override
    public void onRegistryNewRegistry(final NewRegistryEvent event)
    {
        super.onRegistryNewRegistry(event);
    }
}
