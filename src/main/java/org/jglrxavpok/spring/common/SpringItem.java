package org.jglrxavpok.spring.common;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.jglrxavpok.spring.EntitySpringMod;

import javax.annotation.Nullable;
import java.util.List;

public class SpringItem extends Item {

    private TextComponentTranslation springInfo = new TextComponentTranslation("item.spring.description");

    public SpringItem() {
        setRegistryName(EntitySpringMod.MODID, "spring");
        setCreativeTab(CreativeTabs.TOOLS);
        setUnlocalizedName("spring");

        setMaxStackSize(1);
        addPropertyOverride(new ResourceLocation("firstSelected"), (stack, a, b) -> getState(stack) == State.WAITING_NEXT ? 1f : 0f);
    }

    // because 'itemInteractionForEntity' is only for Living entities
    public void onUsedOnEntity(ItemStack stack, EntityPlayer player, World world, Entity target) {
      /*  if(world.isRemote)
            return;*/
        if(player.isSneaking()) {
            // delete attached springs
            EntitySpring.streamSpringsAttachedTo(EntitySpring.SpringSide.DOMINANT, target).forEach(Entity::setDead);
            EntitySpring.streamSpringsAttachedTo(EntitySpring.SpringSide.DOMINATED, target).forEach(Entity::setDead);
            return;
        }
        switch(getState(stack)) {
            case WAITING_NEXT: {
                Entity dominant = getDominant(world, stack);
                if(dominant == null)
                    return;
                if(dominant == target) {
                    player.sendStatusMessage(new TextComponentTranslation("item.spring.notToSelf"), true);
                } else {
                    // First entity clicked is the dominant
                    EntitySpring.createSpring(dominant, target);
                }
                resetLinked(stack);
                if(!player.capabilities.isCreativeMode)
                    stack.shrink(1);
            }
            break;

            default: {
                setDominant(world, stack, target);
            }
            break;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(springInfo.getUnformattedComponentText());
    }

    private void setDominant(World worldIn, ItemStack stack, Entity entity) {
        nbt(stack).setInteger("linked", entity.getEntityId());
    }

    private Entity getDominant(World worldIn, ItemStack stack) {
        int id = nbt(stack).getInteger("linked");
        return worldIn.getEntityByID(id);
    }

    private NBTTagCompound nbt(ItemStack stack)  {
        if(stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    private void resetLinked(ItemStack itemstack) {
        nbt(itemstack).removeTag("linked");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn,  EnumHand handIn) {
        resetLinked(playerIn.getHeldItem(handIn));
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public State getState(ItemStack stack) {
        if(nbt(stack).hasKey("linked"))
            return State.WAITING_NEXT;
        return State.READY;
    }

    private enum State {
        WAITING_NEXT,
        READY
    }
}
