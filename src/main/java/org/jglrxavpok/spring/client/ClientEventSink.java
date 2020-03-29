package org.jglrxavpok.spring.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jglrxavpok.spring.EntitySpringMod;
import org.jglrxavpok.spring.common.SpringEntity;

@OnlyIn(Dist.CLIENT)
public class ClientEventSink {

    public static void preInit(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(SpringEntity.class, RenderSpringEntity::new);
    }

}
