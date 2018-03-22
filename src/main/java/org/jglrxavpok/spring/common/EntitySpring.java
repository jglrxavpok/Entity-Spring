package org.jglrxavpok.spring.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.jglrxavpok.spring.EntitySpringAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EntitySpring extends Entity implements IEntityAdditionalSpawnData {

    public static final DataParameter<Integer> DOMINANT_ID = EntityDataManager.createKey(EntitySpring.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> DOMINATED_ID = EntityDataManager.createKey(EntitySpring.class, DataSerializers.VARINT);

    private @Nullable NBTTagCompound dominantNBT;
    private @Nullable NBTTagCompound dominatedNBT;
    @Nullable
    public Entity dominant;
    @Nullable
    public Entity dominated;

    public EntitySpring(World worldIn) {
        super(worldIn);
        setNoGravity(true);
        noClip = true;
    }

    public EntitySpring(@Nonnull Entity dominant, @Nonnull Entity dominatedEntity) {
        super(dominant.getEntityWorld());
        this.dominant = dominant;
        this.dominated = dominatedEntity;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return Block.NULL_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return Block.NULL_AABB;
    }

    @Override
    protected void entityInit() {
        dataManager.register(DOMINANT_ID, -1);
        dataManager.register(DOMINATED_ID, -1);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        dominantNBT = compound.getCompoundTag(SpringSide.DOMINANT.name());
        dominatedNBT = compound.getCompoundTag(SpringSide.DOMINATED.name());
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
    public void onEntityUpdate() {
        motionX = 0.0;
        motionY = 0.0;
        motionZ = 0.0;
        super.onEntityUpdate();
        if(dominant != null && dominated != null) {
            if(dominant.isDead || dominated.isDead) {
                setDead();
                return;
            }
            posX = (dominant.posX + dominated.posX) /2;
            posY = (dominant.posY + dominated.posY) /2;
            posZ = (dominant.posZ + dominated.posZ) /2;

            double distSq = dominant.getDistanceSq(dominated);
            double maxDstSq;
            if(dominated instanceof EntityMinecart && dominant instanceof EntityMinecart && BlockRailBase.isRailBlock(world, dominant.getPosition()))
                maxDstSq = 0.8;
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

                double speed;
                if(dominated instanceof EntityMinecart && dominant instanceof EntityMinecart)
                    speed = 1.75;
                else
                    speed = 0.2;
                dominated.motionX += dx * Math.abs(dx) * speed;
                dominated.motionY += dy * Math.abs(dy) * speed;
                dominated.motionZ += dz * Math.abs(dz) * speed;
            }
        } else { // front and back entities have not been loaded yet
            if(dominantNBT != null && dominatedNBT != null) {
                tryToLoadFromNBT(dominantNBT).ifPresent(e -> {
                    dominant = e;
                    dataManager.set(DOMINANT_ID, e.getEntityId());
                });
                tryToLoadFromNBT(dominatedNBT).ifPresent(e -> {
                    dominated = e;
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
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        pos.setPos(compound.getInteger("X"), compound.getInteger("Y"), compound.getInteger("Z"));
        String type = compound.getString("Type");
        AxisAlignedBB searchBox = new AxisAlignedBB(pos);
        pos.release();
        List<Entity> entities = world.getEntitiesInAABBexcluding(this, searchBox, e -> e.getClass().getCanonicalName().equals(type));
        return entities.stream().findFirst();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
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
        compound.setInteger("X", (int)Math.floor(entity.posX));
        compound.setInteger("Y", (int)Math.floor(entity.posY));
        compound.setInteger("Z", (int)Math.floor(entity.posZ));
        compound.setString("Type", entity.getClass().getCanonicalName());

        globalCompound.setTag(side.name(), compound);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        if(dominated != null && dominant != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(dominant.getEntityId());
            buffer.writeInt(dominated.getEntityId());
        } else {
            buffer.writeBoolean(false);
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        if(additionalData.readBoolean()) { // has both entities
            int frontID = additionalData.readInt();
            int backID = additionalData.readInt();
            dominant = world.getEntityByID(frontID);
            dominated = world.getEntityByID(backID);
        }
    }

    // Helper methods
    public static boolean hasLinkOnSide(SpringSide side, Entity entity) {
        return streamSpringsAttachedTo(side, entity).count() != 0;
    }

    public static Stream<EntitySpring> streamSpringsAttachedTo(SpringSide side, Entity entity) {
        World world = entity.getEntityWorld();
        return world.getLoadedEntityList()
                .parallelStream()
                .filter(e -> e instanceof EntitySpring)
                .map(e -> (EntitySpring)e)
                .filter(e -> {
                    if(side == SpringSide.DOMINANT)
                        return e.dominated == entity;
                    else
                        return e.dominant == entity;
                });
    }

    public static void createSpring(Entity dominantEntity, Entity dominatedEntity) {
        EntitySpring link = new EntitySpring(dominantEntity, dominatedEntity);
        World world = link.getEntityWorld();
        world.spawnEntity(link);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    public enum SpringSide {
        DOMINANT, DOMINATED
    }
}
