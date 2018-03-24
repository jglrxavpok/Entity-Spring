package org.jglrxavpok.spring.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jglrxavpok.spring.EntitySpringMod;
import org.jglrxavpok.spring.common.ESProxyCommon;
import org.jglrxavpok.spring.common.EntitySpring;

public class ESProxyClient extends ESProxyCommon {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntitySpring.class, RenderSpringEntity::new);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(EntitySpringMod.instance.springItemInstance, 0, new ModelResourceLocation(EntitySpringMod.MODID+":spring", "inventory"));
    }
}
