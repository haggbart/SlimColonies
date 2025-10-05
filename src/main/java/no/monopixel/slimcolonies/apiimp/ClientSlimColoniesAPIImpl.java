package no.monopixel.slimcolonies.apiimp;

import net.minecraftforge.registries.NewRegistryEvent;
import no.monopixel.slimcolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import no.monopixel.slimcolonies.core.client.render.modeltype.registry.ModelTypeRegistry;

public class ClientSlimColoniesAPIImpl extends CommonSlimColoniesAPIImpl
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
