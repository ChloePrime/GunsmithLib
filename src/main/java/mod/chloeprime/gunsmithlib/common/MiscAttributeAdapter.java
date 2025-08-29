package mod.chloeprime.gunsmithlib.common;

import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.resource.modifier.custom.EffectiveRangeModifier;
import com.tacz.guns.resource.pojo.data.gun.FeedType;
import com.tacz.guns.util.AttachmentDataUtils;
import mod.chloeprime.gunsmithlib.Config;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.FireControlAttributes;
import mod.chloeprime.gunsmithlib.common.internal.GunAttributeSyncState;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import net.minecraft.core.Holder;
import mod.chloeprime.gunsmithlib.mixin.EntityKineticBulletAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Optional;
import java.util.function.BiConsumer;

import static mod.chloeprime.gunsmithlib.api.common.GunAttributes.*;

@EventBusSubscriber
public class MiscAttributeAdapter {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void bulletDamage(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        // 近战武器不加射击伤害
        var src = event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING);
        var isMelee = src.getEntity() == src.getDirectEntity();

        var attacker = event.getAttacker();
        if (attacker == null) {
            return;
        }
        // 爆头
        if (!isMelee) {
            event.setHeadshotMultiplier((float) attacker.getAttributeValue(HEADSHOT_MULTIPLIER));
        }
        // 左键近战武器分散增益
        var coefficient = GsHelper.getBuffCoefficient(event.getGunId(), isMelee);
        var attribute = isMelee ? Attributes.ATTACK_DAMAGE : BULLET_DAMAGE;
        var oldDamage = event.getBaseAmount() / coefficient;
        var newDamage = (!isMelee && Config.USE_ATTACK_DAMAGE.get())
                ? GsHelper.getAttributeValueWithBase(attacker, attribute, GsHelper.getAttributeValueWithBase(attacker, Attributes.ATTACK_DAMAGE, oldDamage))
                : GsHelper.getAttributeValueWithBase(attacker, attribute, oldDamage);
        event.setBaseAmount((float) (newDamage * coefficient));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void bulletSpeed(InternalBulletCreateEvent event) {
        var attacker = event.getImpl().getShooter();
        if (attacker.level().isClientSide) {
            return;
        }
        var bullet = event.getImpl().getBullet();
        // 穿甲
        if (bullet instanceof EntityKineticBulletAccessor accessor) {
            accessor.setArmorIgnore((float) attacker.getAttributeValue(ARMOR_PIERCING_RATIO));
        }
        // 子弹飞行速度
        var oldMotion = bullet.getDeltaMovement();
        @SuppressWarnings("UnnecessaryLocalVariable")
        var attribute = BULLET_SPEED;
        var oldSpeed = oldMotion.length();
        var newSpeed = GsHelper.getAttributeValueWithBase(attacker, attribute, oldSpeed);
        // 速度沒有被Attribute修改的情況
        if (Math.abs(newSpeed - oldSpeed) < 1e-4) {
            return;
        }
        var direction = oldSpeed == 0 ? bullet.getLookAngle() : oldMotion.scale(1 / oldSpeed);
        bullet.setDeltaMovement(direction.scale(newSpeed));
    }

    public static double rpm(LivingEntity attacker) {
        return attacker.getAttributeValue(RPM);
    }

    public static int ammoCapacity(int original, ItemStack gunItem) {
        var isUsingInventoryAsMagazine = Gunsmith.getGunInfo(gunItem)
                .map(gi -> gi.index().getGunData())
                .filter(gunData -> gunData.getReloadData().getType() == FeedType.INVENTORY)
                .isPresent();
        if (isUsingInventoryAsMagazine) {
            return original;
        }

        return (int) Math.round(GsHelper.evaluateItemAttribute(gunItem, AMMO_CAPACITY, original));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SuppressWarnings("UnstableApiUsage")
    public static void defaultValues(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity user)) {
            return;
        }
        if (user.level().isClientSide) {
            return;
        }
        final var interval = 2;
        if ((user.level().getGameTime() + user.hashCode()) % interval != 0) {
            return;
        }
        var newMH = user.getMainHandItem();
        var syncState = (GunAttributeSyncState) user;
        Gunsmith.getGunInfo(newMH).ifPresentOrElse(gun -> {
            syncState.gunsmith$setInGunMode(true);
            var cache = IGunOperator.fromLivingEntity(user).getCacheProperty();
            var gunData = gun.index().getGunData();

            var damage = AttachmentDataUtils.getDamageWithAttachment(newMH, gunData) / gun.index().getBulletData().getBulletAmount();
            var ap = cache == null
                    ? AttachmentDataUtils.getArmorIgnoreWithAttachment(newMH, gunData)
                    : cache.getCache(GunProperties.ARMOR_IGNORE);
            var headshot = cache == null
                    ? AttachmentDataUtils.getHeadshotMultiplier(newMH, gunData)
                    : cache.getCache(GunProperties.HEADSHOT_MULTIPLIER);
            var speed = (cache == null
                    ? gun.index().getBulletData().getSpeed()
                    : cache.getCache(GunProperties.AMMO_SPEED)) / 20;
            var rpm = cache == null
                    ? gunData.getRoundsPerMinute(gun.getFireMode())
                    : cache.getCache(GunProperties.ROUNDS_PER_MINUTE);

            var lockRange = cache != null && cache.getCache(EffectiveRangeModifier.ID) instanceof Number range
                    ? range.doubleValue()
                    : FireControlAttributes.AIM_LOCK_RANGE.get().getDefaultValue();

            setBaseValue(user, BULLET_DAMAGE, damage);
            setBaseValue(user, ARMOR_PIERCING_RATIO, ap);
            setBaseValue(user, HEADSHOT_MULTIPLIER, headshot);
            setBaseValue(user, BULLET_SPEED, speed);
            setBaseValue(user, RPM, rpm);
            setBaseValue(user, FireControlAttributes.AIM_LOCK_RANGE, lockRange);
        }, () -> {
            if (!syncState.gunsmith$isInGunMode()) {
                return;
            }
            syncState.gunsmith$setInGunMode(false);
            resetBaseValue(user, BULLET_DAMAGE);
            resetBaseValue(user, ARMOR_PIERCING_RATIO);
            resetBaseValue(user, HEADSHOT_MULTIPLIER);
            resetBaseValue(user, BULLET_SPEED);
            resetBaseValue(user, RPM);
            resetBaseValue(user, FireControlAttributes.AIM_LOCK_RANGE);
        });
    }

    private static void setBaseValue(LivingEntity owner, Holder<Attribute> attribute, double value) {
        Optional.ofNullable(owner.getAttribute(attribute)).ifPresent(ai -> ai.setBaseValue(value));
    }

    private static void resetBaseValue(LivingEntity owner, Holder<Attribute> attribute) {
        setBaseValue(owner, attribute, attribute.value().getDefaultValue());
    }

    @EventBusSubscriber
    public static class AttributeAttacher {
        @SubscribeEvent
        public static void onAttachAttributes(EntityAttributeModificationEvent event) {
            event.getTypes().forEach(et -> addAll(et, event::add,
                    BULLET_DAMAGE,
                    ARMOR_PIERCING_RATIO,
                    HEADSHOT_MULTIPLIER,
                    BULLET_SPEED,
                    V_RECOIL,
                    H_RECOIL,
                    RPM,
                    // AMMO_CAPACITY 弹匣容量只在物品上生效
                    RELOAD_SPEED,
                    // 数据同步用 Attribute，外部代码请勿使用
                    GunsmithLib.Attributes.AMMO_IN_BACKPACK));
        }

        @SafeVarargs
        private static void addAll(EntityType<? extends LivingEntity> type, BiConsumer<EntityType<? extends LivingEntity>, Holder<Attribute>> add, Holder<Attribute>... attribs) {
            for (var holder : attribs) {
                add.accept(type, holder);
            }
        }
    }
}
