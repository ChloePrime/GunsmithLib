package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.client.GunsmithLibAnimationConstant;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.core.HolderLookup;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @since 3.4.0
 */
@EventBusSubscriber
public class ShieldBehavior {
    /**
     * 判断手持一把枪的玩家是否能格挡原版伤害。<p>
     * 格挡范围为满足 {@link ShieldData#getCondition()} 的枪械和所有配件的格挡范围的最大值。
     */
    public static boolean canBlockVanillaDamage(LivingEntity user, Vec3 sourcePos, ItemStack weapon) {
        return canBlockDamage(user, sourcePos, weapon, ShieldData::blockVanillaDamageAngle);
    }

    /**
     * 判断手持一把枪的玩家是否能格挡子弹。<p>
     * 格挡范围为满足 {@link ShieldData#getCondition()} 的枪械和所有配件的格挡范围的最大值。
     */
    public static boolean canBlockBulletDamage(LivingEntity user, Vec3 bulletPos, ItemStack weapon) {
        return canBlockDamage(user, bulletPos, weapon, ShieldData::blockBulletDamageAngle);
    }

    /**
     * @since 3.6.0
     */
    public static Optional<ShieldData> getUsedShieldForBlockingVanillaDamage(
            LivingEntity user, ItemStack weapon
    ) {
        return getUsedShieldFor(user, weapon, ShieldData::blockVanillaDamageAngle);
    }

    /**
     * @since 3.6.0
     */
    public static Optional<ShieldData> getUsedShieldForBlockingBulletDamage(
            LivingEntity user, ItemStack weapon
    ) {
        return getUsedShieldFor(user, weapon, ShieldData::blockBulletDamageAngle);
    }

    private static Optional<ShieldData> getUsedShieldFor(
            LivingEntity user,
            ItemStack weapon,
            Function<ShieldData, Double> angleField) {
        Predicate<ShieldData> condition = getConditionPredicate(user);
        return getUsedShield(user.registryAccess(), weapon, data -> condition.test(data) ? angleField.apply(data) : 0);
    }

    private static boolean canBlockDamage(
            LivingEntity user, Vec3 sourcePos, ItemStack weapon,
            Function<ShieldData, Double> angleField
    ) {
        // 冷却时完全不能格挡伤害
        if (user instanceof Player player && player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) {
            return false;
        }
        Predicate<ShieldData> condition = getConditionPredicate(user);
        // 单位为弧度
        var angle = getTotalAngle(user.registryAccess(), weapon, data -> condition.test(data) ? angleField.apply(data) : 0);
        if (angle <= 1e-4) {
            return false;
        }
        return checkAngle(sourcePos.subtract(user.getEyePosition()), user.getLookAngle(), angle);
    }

    /**
     * @param offset 玩家眼睛到伤害来源的相对位置
     * @param look 玩家的视线方向
     * @param angle 格挡范围，单位为弧度
     * @return 判断当前伤害来源是否在格挡范围内
     */
    private static boolean checkAngle(Vec3 offset, Vec3 look, double angle) {
        double distance = offset.length();
        if (distance <= 1e-3) {
            return false;
        }
        // 视线方向
        double cos = offset.dot(look) / distance;
        return cos >= Math.cos(angle);
    }

    private static Predicate<ShieldData> getConditionPredicate(LivingEntity user) {
        var operator = IGunOperator.fromLivingEntity(user);
        var isReloading = operator.getSynReloadState().getStateType() != ReloadState.StateType.NOT_RELOADING;
        var isAiming = operator.getSynAimingProgress() >= 0.5F;
        return data -> {
            if (isReloading && data.disableShieldWhenReloading()) {
                return false;
            }
            var condition = data.getCondition();
            return condition == ShieldData.Condition.ALWAYS || (condition == ShieldData.Condition.WHEN_AIMING) == isAiming;
        };
    }

    private static Optional<ShieldData> getUsedShield(HolderLookup.Provider registryAccess, ItemStack weapon, Function<ShieldData, Double> field) {
        var gun = Gunsmith.getGunInfo(weapon).orElse(null);
        if (gun == null) {
            return Optional.empty();
        }
        ShieldData result = ShieldData.fromGun(gun).orElse(null);
        double maxAngle = result != null ? field.apply(result) : 0;

        for (var attachmentType : AttachmentType.values()) {
            ItemStack attachment = gun.gunItem().getAttachment(registryAccess, gun.gunStack(), attachmentType);
            ShieldData data = ShieldData.fromAttachment(attachment).orElse(null);
            if (data == null) {
                continue;
            }
            var angle = field.apply(data);
            if (angle > maxAngle) {
                maxAngle = angle;
                result = data;
            }
        }
        return Optional.ofNullable(result);
    }

    private static double getTotalAngle(HolderLookup.Provider registryAccess, ItemStack weapon, Function<ShieldData, Double> field) {
        return getUsedShield(registryAccess, weapon, field).map(field).orElse(0.0);
    }

    public static Vec3 getBetterSourcePosition(DamageSource source) {
        var override = source.sourcePositionRaw();
        if (override != null) {
            return override;
        }
        var direct = source.getDirectEntity();
        if (direct == null) {
            return null;
        }
        if (direct instanceof LivingEntity) {
            return direct.position().add(0, direct.getBbHeight() * 0.75, 0);
        } else {
            return direct.position();
        }
    }

    /**
     * 格挡子弹
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBulletHitEntity(EntityHurtByGunEvent.Pre event) {
        if (!(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (canBlockBulletDamage(victim, event.getBullet().position(), victim.getMainHandItem())) {
            victim.level().playSound(null, victim, GunsmithLib.SoundEvents.SHIELD_BLOCKS_BULLET.get(), victim.getSoundSource(), 1, 1);
            if (victim instanceof ServerPlayer player) {
                RPC.call(RPCTarget.to(player), ShieldBehavior::triggerAnimation, player, true);
            }
            event.setCanceled(true);
        }
    }

    /**
     * 格挡原版伤害时触发状态机信号
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void triggerAnimationOnBlockingVanillaDamage(LivingShieldBlockEvent event) {
        var user = event.getEntity();
        if (!user.level().isClientSide() && user instanceof ServerPlayer player && IGun.mainHandHoldGun(player)) {
            RPC.call(RPCTarget.to(player), ShieldBehavior::triggerAnimation, player, false);
        }
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void triggerAnimation(Player user, boolean isBulletDamage) {
        var key = isBulletDamage
                ? GunsmithLibAnimationConstant.GUNSMITHLIB_INPUT_SHIELD_BLOCKS_BULLET
                : GunsmithLibAnimationConstant.GUNSMITHLIB_INPUT_SHIELD_BLOCKS_DAMAGE;
        GunsmithLibClient.triggerAnimation(user, key);
    }
}
