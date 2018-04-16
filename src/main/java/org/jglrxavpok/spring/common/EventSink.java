package org.jglrxavpok.spring.common;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import org.jglrxavpok.spring.EntitySpringAPI;
import org.jglrxavpok.spring.EntitySpringMod;

@Mod.EventBusSubscriber(modid = EntitySpringMod.MODID)
public class EventSink {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void entityInteract(PlayerInteractEvent.EntityInteract event) {
        handleEvent(event, event.getTarget());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void entitySpecificInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        handleEvent(event, event.getTarget());
    }

    private static void handleEvent(PlayerInteractEvent event, Entity target) {
        if(!event.getItemStack().isEmpty()) {
            Item item = event.getItemStack().getItem();
            if(item instanceof ItemSpring) {
                if(EntitySpringAPI.isValidTarget(target)) {
                    ItemSpring itemSpring = (ItemSpring) item;
                    itemSpring.onUsedOnEntity(event.getItemStack(), event.getEntityPlayer(), event.getWorld(), target);
                    event.setCanceled(true);
                    event.setCancellationResult(EnumActionResult.SUCCESS);
                }
            } else if(item instanceof ItemSpringCutter) {
                ItemSpringCutter cutter = (ItemSpringCutter) item;
                cutter.onUsedOnEntity(event.getItemStack(), event.getEntityPlayer(), event.getWorld(), target);
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt) {
        evt.getRegistry().registerAll(
                EntitySpringMod.instance.itemSpringInstance,
                EntitySpringMod.instance.cutterItemInstance
        );
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
