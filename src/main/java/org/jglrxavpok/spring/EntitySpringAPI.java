package org.jglrxavpok.spring;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jglrxavpok.spring.common.EntitySpring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class EntitySpringAPI {
    private EntitySpringAPI() {
    }

    private static final BiFunction<Entity, EntitySpring.SpringSide, Vec3d> DEFAULT_ANCHOR_LOCATION = (e, sideArg) -> e.getPositionVector();
    private static final List<Predicate<Entity>> predicates = new ArrayList<>();
    private static final Map<Class<? extends Entity>, BiFunction<Entity, EntitySpring.SpringSide, Vec3d>> mapping = new HashMap<>();
    public static final BiFunction<Entity, EntitySpring.SpringSide, Vec3d> CART_DEFAULT_ANCHOR_90DEG_OFFSET = (entity, side) -> {
        float distanceFromCenter = 0.0625f * 17f * (side == EntitySpring.SpringSide.DOMINANT ? 1f : -1f);
        double anchorX = entity.posX + MathHelper.cos((float) ((entity.rotationYaw + 90f) * Math.PI / 180f)) * distanceFromCenter;
        double anchorY = entity.posY;// + 0.0625f * 16f;
        double anchorZ = entity.posZ + MathHelper.sin((float)((entity.rotationYaw + 90f) * Math.PI / 180f)) * distanceFromCenter;
        return new Vec3d(anchorX, anchorY, anchorZ);
    };

    public static final BiFunction<Entity, EntitySpring.SpringSide, Vec3d> CART_DEFAULT_ANCHOR = (entity, side) -> {
        float distanceFromCenter = 0.0625f * 17f * (side == EntitySpring.SpringSide.DOMINANT ? 1f : -1f);
        double anchorX = entity.posX + MathHelper.cos((float) ((entity.rotationYaw) * Math.PI / 180f)) * distanceFromCenter;
        double anchorY = entity.posY;// + 0.0625f * 16f;
        double anchorZ = entity.posZ + MathHelper.sin((float)((entity.rotationYaw) * Math.PI / 180f)) * distanceFromCenter;
        return new Vec3d(anchorX, anchorY, anchorZ);
    };

    static {
        registerTargetPredicate(e -> e instanceof EntityBoat);
        registerTargetPredicate(e -> e instanceof EntityMinecart);
        registerTargetPredicate(e -> e instanceof EntityLivingBase);

        addGenericAnchorMapping(EntityBoat.class, CART_DEFAULT_ANCHOR_90DEG_OFFSET);
        addGenericAnchorMapping(EntityMinecart.class, CART_DEFAULT_ANCHOR);
        addGenericAnchorMapping(EntityMinecartChest.class, CART_DEFAULT_ANCHOR);
        addGenericAnchorMapping(EntityMinecartCommandBlock.class, CART_DEFAULT_ANCHOR);
        addGenericAnchorMapping(EntityMinecartContainer.class, CART_DEFAULT_ANCHOR);
        addGenericAnchorMapping(EntityMinecartEmpty.class, CART_DEFAULT_ANCHOR);
        addGenericAnchorMapping(EntityMinecartFurnace.class, CART_DEFAULT_ANCHOR);
        addGenericAnchorMapping(EntityMinecartHopper.class, CART_DEFAULT_ANCHOR);
        addGenericAnchorMapping(EntityMinecartMobSpawner.class, CART_DEFAULT_ANCHOR);
        addGenericAnchorMapping(EntityMinecartTNT.class, CART_DEFAULT_ANCHOR);
    }

    public static boolean isValidTarget(Entity target) {
        return predicates.parallelStream().anyMatch(e -> e.test(target));
    }

    public static void registerTargetPredicate(Predicate<Entity> predicate) {
        predicates.add(predicate);
    }

    public static void addGenericAnchorMapping(Class<? extends Entity> entity, BiFunction<Entity, EntitySpring.SpringSide, Vec3d> function) {
        mapping.put(entity, function);
    }

    public static <T extends Entity> void addAnchorMapping(Class<T> entity, BiFunction<T, EntitySpring.SpringSide, Vec3d> function) {
        mapping.put(entity, (e, side) -> function.apply((T) e, side));
    }

    public static Vec3d calculateAnchorPosition(Entity entity, EntitySpring.SpringSide side) {
        BiFunction<Entity, EntitySpring.SpringSide, Vec3d> function = mapping.getOrDefault(entity.getClass(), DEFAULT_ANCHOR_LOCATION);
        return function.apply(entity, side);
    }
}
