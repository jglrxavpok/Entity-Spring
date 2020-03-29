package org.jglrxavpok.spring.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jglrxavpok.spring.EntitySpringMod;
import org.jglrxavpok.spring.common.SpringEntity;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = EntitySpringMod.MODID, value = Dist.CLIENT)
public class ClientEventSink {

    @SubscribeEvent
    public static void preInit(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(SpringEntity.class, RenderSpringEntity::new);
    }

}
