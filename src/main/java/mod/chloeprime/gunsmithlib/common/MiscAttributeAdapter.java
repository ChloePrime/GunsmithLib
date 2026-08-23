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
import mod.chloeprime.gunsmithlib.mixin.common.tacz.EntityKineticBulletAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.BiConsumer;

import static mod.chloeprime.gunsmithlib.api.common.GunAttributes.*;

@Mod.EventBusSubscriber
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
            event.setHeadshotMultiplier((float) attacker.getAttributeValue(HEADSHOT_MULTIPLIER.get()));
        }
        // 左键近战武器分散增益
        var coefficient = GsHelper.getBuffCoefficient(event.getGunId(), isMelee);
        var attribute = isMelee ? Attributes.ATTACK_DAMAGE : BULLET_DAMAGE.get();
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
            accessor.setArmorIgnore((float) attacker.getAttributeValue(ARMOR_PIERCING_RATIO.get()));
        }
        // 子弹飞行速度
        var oldMotion = bullet.getDeltaMovement();
        var attribute = BULLET_SPEED.get();
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
        var attribute = RPM.get();
        return attacker.getAttributeValue(attribute);
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

    @SubscribeEvent
    @SuppressWarnings("UnstableApiUsage")
    public static void defaultValues(LivingEvent.LivingTickEvent event) {
        var user = event.getEntity();
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

            setBaseValue(user, BULLET_DAMAGE.get(), damage);
            setBaseValue(user, ARMOR_PIERCING_RATIO.get(), ap);
            setBaseValue(user, HEADSHOT_MULTIPLIER.get(), headshot);
            setBaseValue(user, BULLET_SPEED.get(), speed);
            setBaseValue(user, RPM.get(), rpm);
            setBaseValue(user, FireControlAttributes.AIM_LOCK_RANGE.get(), lockRange);
        }, () -> {
            if (!syncState.gunsmith$isInGunMode()) {
                return;
            }
            syncState.gunsmith$setInGunMode(false);
            resetBaseValue(user, BULLET_DAMAGE.get());
            resetBaseValue(user, ARMOR_PIERCING_RATIO.get());
            resetBaseValue(user, HEADSHOT_MULTIPLIER.get());
            resetBaseValue(user, BULLET_SPEED.get());
            resetBaseValue(user, RPM.get());
            resetBaseValue(user, FireControlAttributes.AIM_LOCK_RANGE.get());
        });
    }

    private static void setBaseValue(LivingEntity owner, Attribute attribute, double value) {
        Optional.ofNullable(owner.getAttribute(attribute))
                .ifPresent(ai -> ai.setBaseValue(value));
    }

    private static void resetBaseValue(LivingEntity owner, Attribute attribute) {
        setBaseValue(owner, attribute, attribute.getDefaultValue());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
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
        private static void addAll(EntityType<? extends LivingEntity> type, BiConsumer<EntityType<? extends LivingEntity>, Attribute> add, RegistryObject<? extends Attribute>... attribs) {
            for (RegistryObject<? extends Attribute> a : attribs)
                add.accept(type, a.get());
        }
    }
}
