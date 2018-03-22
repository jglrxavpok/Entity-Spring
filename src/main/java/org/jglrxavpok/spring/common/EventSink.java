package org.jglrxavpok.spring.common;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import org.jglrxavpok.spring.EntitySpringAPI;
import org.jglrxavpok.spring.EntitySpringMod;

@Mod.EventBusSubscriber(modid = EntitySpringMod.MODID)
public class EventSink {

    @SubscribeEvent
    public static void entityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if(!event.getItemStack().isEmpty() && event.getItemStack().getItem() instanceof SpringItem) {
            if(EntitySpringAPI.isValidTarget(target)) {
                SpringItem item = (SpringItem) event.getItemStack().getItem();
                item.onUsedOnEntity(event.getItemStack(), event.getEntityPlayer(), event.getWorld(), target);
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt) {
        evt.getRegistry().register(EntitySpringMod.instance.springItemInstance);
    }

    @SubscribeEvent
    public static void registerEntity(RegistryEvent.Register<EntityEntry> evt) {
        evt.getRegistry().register(EntityEntryBuilder.create()
                .entity(EntitySpring.class)
                .id(new ResourceLocation(EntitySpringMod.MODID, "spring_entity"), 0)
                .name("spring_entity")
                .factory(EntitySpring::new)
                .tracker(64, 3, false)
                .build());
    }
}
