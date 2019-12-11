package org.jglrxavpok.spring.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.world.World;
import org.jglrxavpok.spring.EntitySpringMod;

public class SpringCutterItem extends Item {

    public SpringCutterItem() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(64));
        setRegistryName(EntitySpringMod.MODID, "cutter");
    }

    // because 'itemInteractionForEntity' is only for Living entities
    public void onUsedOnEntity(ItemStack stack, PlayerEntity player, World world, Entity target) {
        if(world.isRemote) {
            world.playSound(target.posX, target.posY, target.posZ, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 10f, 1f, false);
            return;
        }
        if(!player.isCreative())
            stack.damageItem(1, player, item -> {});
        world.getServer().enqueue(new TickDelayedTask(0, new Runnable() {
            @Override
            public void run() {
                SpringEntity.streamSpringsAttachedTo(SpringEntity.SpringSide.DOMINANT, target).forEach(SpringEntity::kill);
                SpringEntity.streamSpringsAttachedTo(SpringEntity.SpringSide.DOMINATED, target).forEach(SpringEntity::kill);
            }
        }));

    }
}
