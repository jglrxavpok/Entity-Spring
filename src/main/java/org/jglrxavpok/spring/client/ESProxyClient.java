package org.jglrxavpok.spring.client;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.jglrxavpok.spring.common.ESProxyCommon;
import org.jglrxavpok.spring.common.EntitySpring;

public class ESProxyClient extends ESProxyCommon {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntitySpring.class, RenderSpringEntity::new);
    }
}
