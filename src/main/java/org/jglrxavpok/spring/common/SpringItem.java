package org.jglrxavpok.spring.common;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jglrxavpok.spring.EntitySpringMod;

import javax.annotation.Nullable;
import java.util.List;

public class SpringItem extends Item {

    private TranslationTextComponent springInfo = new TranslationTextComponent("item.spring.description");

    public SpringItem() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1));
        setRegistryName(EntitySpringMod.MODID, "spring");

        addPropertyOverride(new ResourceLocation("first_selected"), (stack, a, b) -> getState(stack) == State.WAITING_NEXT ? 1f : 0f);
    }

    // because 'itemInteractionForEntity' is only for Living entities
    public void onUsedOnEntity(ItemStack stack, PlayerEntity player, World world, Entity target) {
        if(world.isRemote)
            return;
        switch(getState(stack)) {
            case WAITING_NEXT: {
                Entity dominant = getDominant(world, stack);
                if(dominant == null)
                    return;
                if(dominant == target) {
                    player.sendStatusMessage(new TranslationTextComponent("item.spring.notToSelf"), true);
                } else {
                    // First entity clicked is the dominant
                    SpringEntity.createSpring(dominant, target);
                    if(!player.isCreative())
                        stack.shrink(1);
                }
                resetLinked(stack);
            }
            break;

            default: {
                setDominant(world, stack, target);
            }
            break;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(springInfo);
    }

    private void setDominant(World worldIn, ItemStack stack, Entity entity) {
        nbt(stack).putInt("linked", entity.getEntityId());
    }

    private Entity getDominant(World worldIn, ItemStack stack) {
        int id = nbt(stack).getInt("linked");
        return worldIn.getEntityByID(id);
    }

    private CompoundNBT nbt(ItemStack stack)  {
        if(stack.getTag() == null) {
            stack.setTag(new CompoundNBT());
        }
        return stack.getTag();
    }

    private void resetLinked(ItemStack itemstack) {
        nbt(itemstack).remove("linked");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        resetLinked(playerIn.getHeldItem(handIn));
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public State getState(ItemStack stack) {
        if(nbt(stack).contains("linked"))
            return State.WAITING_NEXT;
        return State.READY;
    }

    private enum State {
        WAITING_NEXT,
        READY
    }
}
