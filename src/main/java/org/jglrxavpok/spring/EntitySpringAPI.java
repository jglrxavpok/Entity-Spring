package org.jglrxavpok.spring;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jglrxavpok.spring.common.SpringEntity;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class EntitySpringAPI {
    private EntitySpringAPI() {
    }

    private static final BiFunction<Entity, SpringEntity.SpringSide, Vec3d> DEFAULT_ANCHOR_LOCATION = (e, sideArg) -> e.getPositionVector();
    private static final List<Predicate<Entity>> predicates = new ArrayList<>();
    private static final Map<Class<? extends Entity>, BiFunction<Entity, SpringEntity.SpringSide, Vec3d>> mapping = new HashMap<>();
    public static final BiFunction<Entity, SpringEntity.SpringSide, Vec3d> DEFAULT_BOAT_ANCHOR = (entity, side) -> {
        float distanceFromCenter = 0.0625f * 17f * (side == SpringEntity.SpringSide.DOMINANT ? 1f : -1f);
        double anchorX = entity.posX + MathHelper.cos((float) ((entity.rotationYaw + 90f) * Math.PI / 180f)) * distanceFromCenter;
        double anchorY = entity.posY;
        double anchorZ = entity.posZ + MathHelper.sin((float)((entity.rotationYaw + 90f) * Math.PI / 180f)) * distanceFromCenter;
        return new Vec3d(anchorX, anchorY, anchorZ);
    };

    public static List<ResourceLocation> defaultBlacklistedEntities = Arrays.asList(
        new ResourceLocation("item"),
        new ResourceLocation("xp_orb"),
        new ResourceLocation("area_effect_cloud"),
        new ResourceLocation("egg"),
        new ResourceLocation("leash_knot"),
        new ResourceLocation("painting"),
        new ResourceLocation("arrow"),
        new ResourceLocation("snowball"),
        new ResourceLocation("fireball"),
        new ResourceLocation("small_fireball"),
        new ResourceLocation("ender_pearl"),
        new ResourceLocation("eye_of_ender_signal"),
        new ResourceLocation("potion"),
        new ResourceLocation("xp_bottle"),
        new ResourceLocation("item_frame"),
        new ResourceLocation("wither_skull"),
        new ResourceLocation("spectral_arrow"),
        new ResourceLocation("shulker_bullet"),
        new ResourceLocation("dragon_fireball"),
        new ResourceLocation("dragon_fireball"),
        new ResourceLocation("llama_spit")
    );

    static {
        addGenericAnchorMapping(BoatEntity.class, DEFAULT_BOAT_ANCHOR);
    }

    private static Set<ResourceLocation> blacklisted = new HashSet<>();

    public static void blacklist(ResourceLocation entityName) {
        if(!blacklisted.contains(entityName))
            blacklisted.add(entityName);
    }

    public static boolean isValidTarget(Entity target) {
        ResourceLocation targetName = EntityType.getKey(target.getType());
        return blacklisted.stream().noneMatch(targetName::equals);
    }

    public static void addGenericAnchorMapping(Class<? extends Entity> entity, BiFunction<Entity, SpringEntity.SpringSide, Vec3d> function) {
        mapping.put(entity, function);
    }

    public static <T extends Entity> void addAnchorMapping(Class<? extends T> entity, BiFunction<T, SpringEntity.SpringSide, Vec3d> function) {
        mapping.put(entity, (e, side) -> function.apply((T) e, side));
    }

    public static Vec3d calculateAnchorPosition(Entity entity, SpringEntity.SpringSide side) {
        BiFunction<Entity, SpringEntity.SpringSide, Vec3d> function = mapping.getOrDefault(entity.getClass(), DEFAULT_ANCHOR_LOCATION);
        return function.apply(entity, side);
    }
}
