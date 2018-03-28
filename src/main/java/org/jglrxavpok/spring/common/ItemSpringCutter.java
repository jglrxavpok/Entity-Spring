package org.jglrxavpok.spring.common;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.jglrxavpok.spring.EntitySpringMod;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSpringCutter extends Item {

    public ItemSpringCutter() {
        setRegistryName(EntitySpringMod.MODID, "cutter");
        setCreativeTab(CreativeTabs.TOOLS);
        setUnlocalizedName("cutter");

        setMaxStackSize(1);
        setMaxDamage(64);
    }

    // because 'itemInteractionForEntity' is only for Living entities
    public void onUsedOnEntity(ItemStack stack, EntityPlayer player, World world, Entity target) {
        if(world.isRemote) {
            world.playSound(target.posX, target.posY, target.posZ, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 10f, 1f, false);
            return;
        }
        if(!player.isCreative())
            stack.damageItem(1, player);
        EntitySpring.streamSpringsAttachedTo(EntitySpring.SpringSide.DOMINANT, target).forEach(EntitySpring::kill);
        EntitySpring.streamSpringsAttachedTo(EntitySpring.SpringSide.DOMINATED, target).forEach(EntitySpring::kill);
    }
}
