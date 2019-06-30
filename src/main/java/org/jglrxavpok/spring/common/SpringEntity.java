package org.jglrxavpok.spring.common;

import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.jglrxavpok.spring.EntitySpringAPI;
import org.jglrxavpok.spring.EntitySpringMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SpringEntity extends Entity implements IEntityAdditionalSpawnData {

    private static AxisAlignedBB nullBB = new AxisAlignedBB(0,0,0,0,0,0);
    public static final DataParameter<Integer> DOMINANT_ID = EntityDataManager.createKey(SpringEntity.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> DOMINATED_ID = EntityDataManager.createKey(SpringEntity.class, DataSerializers.VARINT);

    private @Nullable NBTTagCompound dominantNBT;
    private @Nullable NBTTagCompound dominatedNBT;
    @Nullable
    public Entity dominant;
    @Nullable
    public Entity dominated;

    public SpringEntity(World worldIn) {
        super(EntitySpringMod.SpringType, worldIn);
        setNoGravity(true);
        noClip = true;
    }

    public SpringEntity(@Nonnull Entity dominant, @Nonnull Entity dominatedEntity) {
        super(EntitySpringMod.SpringType, dominant.getEntityWorld());
        this.dominant = dominant;
        this.dominated = dominatedEntity;
        posX = (dominant.posX + dominated.posX)/2;
        posY = (dominant.posY + dominated.posY)/2;
        posZ = (dominant.posZ + dominated.posZ)/2;
    }

    @Override
    protected void registerData() {
        getDataManager().register(DOMINANT_ID, -1);
        getDataManager().register(DOMINATED_ID, -1);
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return nullBB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return nullBB;
    }

    public static Vec3d calculateAnchorPosition(Entity entity, SpringSide side) {
        return EntitySpringAPI.calculateAnchorPosition(entity, side);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);

        if(world.isRemote) {
            if(DOMINANT_ID.equals(key)) {
                Entity potential = world.getEntityByID(dataManager.get(DOMINANT_ID));
                if(potential != null) {
                    dominant = potential;
                }
            }
            if(DOMINATED_ID.equals(key)) {
                Entity potential = world.getEntityByID(dataManager.get(DOMINATED_ID));
                if(potential != null) {
                    dominated = potential;
                }
            }
        }
    }

    @Override
    public void baseTick() {
        motionX = 0.0;
        motionY = 0.0;
        motionZ = 0.0;
        super.baseTick();
        if(dominant != null && dominated != null) {
            if( ! dominant.isAlive() || ! dominated.isAlive()) {
                kill();
                return;
            }
            posX = (dominant.posX + dominated.posX) /2;
            posY = (dominant.posY + dominated.posY) /2;
            posZ = (dominant.posZ + dominated.posZ) /2;

            double distSq = dominant.getDistanceSq(dominated);
            double maxDstSq;
            if(dominated instanceof EntityMinecart && dominant instanceof EntityMinecart && BlockRailBase.isRail(world, dominant.getPosition()))
                maxDstSq = 1.5;
            else
                maxDstSq = 9.0;
            if(distSq > maxDstSq) {
                Vec3d frontAnchor = calculateAnchorPosition(dominant, SpringSide.DOMINATED);
                Vec3d backAnchor = calculateAnchorPosition(dominated, SpringSide.DOMINANT);
                double dist = Math.sqrt(distSq);
                double dx = (frontAnchor.x - backAnchor.x) / dist;
                double dy = (frontAnchor.y - backAnchor.y) / dist;
                double dz = (frontAnchor.z - backAnchor.z) / dist;
                final double alpha = 0.5;

                float targetYaw = computeTargetYaw(dominated.rotationYaw, frontAnchor, backAnchor);
                dominated.rotationYaw = (float) (alpha * dominated.rotationYaw + targetYaw * (1f-alpha));

                /*double speed;
                if(dominated instanceof EntityMinecart && dominant instanceof EntityMinecart)
                    speed = 1.65;
                else
                    speed = 0.2;
                dominated.motionX += dx * Math.abs(dx) * speed;
                dominated.motionY += dy * Math.abs(dy) * speed;
                dominated.motionZ += dz * Math.abs(dz) * speed;*/
                double k = 0.1;
                double l0 = 1.5;
                dominated.motionX += k*(dist-l0)*dx;
                dominated.motionY += k*(dist-l0)*dy;
                dominated.motionZ += k*(dist-l0)*dz;
            }

            if(!world.isRemote) { // send update every tick to ensure client has infos
                dataManager.set(DOMINANT_ID, dominant.getEntityId());
                dataManager.set(DOMINATED_ID, dominated.getEntityId());
                dataManager.getEntry(DOMINANT_ID).setDirty(true);
                dataManager.getEntry(DOMINATED_ID).setDirty(true);
                dataManager.dirty = true;
            }
        } else { // front and back entities have not been loaded yet
            if(dominantNBT != null && dominatedNBT != null) {
                EntitySpringMod.LOGGER.error(">>dominant {}", dominantNBT);
                EntitySpringMod.LOGGER.error(">>dominated {}", dominatedNBT);
                tryToLoadFromNBT(dominantNBT).ifPresent(e -> {
                    dominant = e;
                    EntitySpringMod.LOGGER.error(">>dominant2 {} {}", dominantNBT, e);
                    dataManager.set(DOMINANT_ID, e.getEntityId());
                });
                tryToLoadFromNBT(dominatedNBT).ifPresent(e -> {
                    dominated = e;
                    EntitySpringMod.LOGGER.error(">>dominated2 {} {}", dominatedNBT, e);
                    dataManager.set(DOMINATED_ID, e.getEntityId());
                });
            }
        }
    }

    private float computeTargetYaw(Float currentYaw, Vec3d anchorPos, Vec3d otherAnchorPos) {
        float idealYaw = (float) (Math.atan2(otherAnchorPos.x - anchorPos.x, -(otherAnchorPos.z - anchorPos.z)) * (180f/Math.PI));
        float closestDistance = Float.POSITIVE_INFINITY;
        float closest = idealYaw;
        for(int sign : Arrays.asList(-1, 0, 1)) {
            float potentialYaw = idealYaw + sign * 360f;
            float distance = Math.abs(potentialYaw - currentYaw);
            if(distance < closestDistance) {
                closestDistance = distance;
                closest = potentialYaw;
            }
        }
        return closest;
    }

    private Optional<Entity> tryToLoadFromNBT(NBTTagCompound compound) {
        try(BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain()) {
            pos.setPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z"));
            String type = compound.getString("Type");
            AxisAlignedBB searchBox = new AxisAlignedBB(pos);
            List<Entity> entities = world.getEntitiesInAABBexcluding(this, searchBox, e -> e.getClass().getCanonicalName().equals(type));
            return entities.stream().findFirst();
        }
    }

    @Override
    protected void readAdditional(NBTTagCompound compound) {
        dominantNBT = compound.getCompound(SpringSide.DOMINANT.name());
        dominatedNBT = compound.getCompound(SpringSide.DOMINATED.name());
    }

    @Override
    protected void writeAdditional(NBTTagCompound compound) {
        if(dominant != null && dominated != null) {
            writeNBT(SpringSide.DOMINANT, dominant, compound);
            writeNBT(SpringSide.DOMINATED, dominated, compound);
        } else {
            if(dominantNBT != null)
                compound.setTag(SpringSide.DOMINANT.name(), dominantNBT);
            if(dominatedNBT != null)
                compound.setTag(SpringSide.DOMINATED.name(), dominatedNBT);
        }
    }

    private void writeNBT(SpringSide side, @Nonnull Entity entity, NBTTagCompound globalCompound) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInt("X", (int)Math.floor(entity.posX));
        compound.setInt("Y", (int)Math.floor(entity.posY));
        compound.setInt("Z", (int)Math.floor(entity.posZ));
        compound.setString("Type", entity.getClass().getCanonicalName());

        globalCompound.setTag(side.name(), compound);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        if(dominated != null && dominant != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(dominant.getEntityId());
            buffer.writeInt(dominated.getEntityId());

            NBTTagCompound globalCompound = new NBTTagCompound();
            writeNBT(SpringSide.DOMINATED, dominated, globalCompound);
            writeNBT(SpringSide.DOMINANT, dominant, globalCompound);
        } else {
            buffer.writeBoolean(false);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        if(additionalData.readBoolean()) { // has both entities
            int frontID = additionalData.readInt();
            int backID = additionalData.readInt();
            dominant = world.getEntityByID(frontID);
            dominated = world.getEntityByID(backID);

            dominantNBT = additionalData.readCompoundTag();
            dominatedNBT = additionalData.readCompoundTag();
        }
    }

    // Helper methods
    public static boolean hasLinkOnSide(SpringSide side, Entity entity) {
        return streamSpringsAttachedTo(side, entity).count() != 0;
    }

    public static Stream<SpringEntity> streamSpringsAttachedTo(SpringSide side, Entity entity) {
        World world = entity.getEntityWorld();
        return world.loadedEntityList
                .parallelStream()
                .filter(e -> e instanceof SpringEntity)
                .map(e -> (SpringEntity)e)
                .filter(e -> {
                    if(side == SpringSide.DOMINANT)
                        return e.dominated == entity;
                    else
                        return e.dominant == entity;
                });
    }

    public static void createSpring(Entity dominantEntity, Entity dominatedEntity) {
        SpringEntity link = new SpringEntity(dominantEntity, dominatedEntity);
        World world = dominantEntity.getEntityWorld();
        world.spawnEntity(link);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        return super.applyPlayerInteraction(player, vec, hand);
    }

    public void kill() {
        super.remove();
        if(!world.isRemote)
            InventoryHelper.spawnItemStack(world, posX, posY, posZ, new ItemStack(EntitySpringMod.SPRING));
    }

    public enum SpringSide {
        DOMINANT, DOMINATED
    }
}
