package org.jglrxavpok.spring.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jglrxavpok.spring.EntitySpringAPI;
import org.jglrxavpok.spring.EntitySpringMod;

@Mod.EventBusSubscriber(modid = EntitySpringMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
            if(item instanceof SpringItem) {
                if(EntitySpringAPI.isValidTarget(target)) {
                    SpringItem springItem = (SpringItem) item;
                    springItem.onUsedOnEntity(event.getItemStack(), event.getEntityPlayer(), event.getWorld(), target);
                    event.setCanceled(true);
                    event.setCancellationResult(EnumActionResult.SUCCESS);
                }
            } else if(item instanceof SpringCutterItem) {
                SpringCutterItem cutter = (SpringCutterItem) item;
                cutter.onUsedOnEntity(event.getItemStack(), event.getEntityPlayer(), event.getWorld(), target);
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt) {
        evt.getRegistry().registerAll(
                EntitySpringMod.SPRING,
                EntitySpringMod.CUTTER
        );
    }

    @SubscribeEvent
    public static void registerEntity(RegistryEvent.Register<EntityType<?>> evt) {
        EntitySpringMod.SpringType = EntityType.Builder.create(SpringEntity.class, SpringEntity::new)
                .tracker(64, 3, false)
                .build("spring_entity")
                .setRegistryName(new ResourceLocation(EntitySpringMod.MODID, "spring_entity"));
        evt.getRegistry().register(EntitySpringMod.SpringType);
    }
}
