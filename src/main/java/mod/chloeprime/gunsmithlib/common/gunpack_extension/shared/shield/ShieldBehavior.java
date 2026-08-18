package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.client.GunsmithLibAnimationConstant;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.common.util.GunPartIterator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * @since 3.4.0
 */
@Mod.EventBusSubscriber
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
            Function<ShieldData, Double> angleField
    ) {
        return getUsedShield(user, weapon, angleField);
    }

    private static boolean canBlockDamage(
            LivingEntity user, Vec3 sourcePos, ItemStack weapon,
            Function<ShieldData, Double> angleField
    ) {
        // 冷却时完全不能格挡伤害
        if (user instanceof Player player && player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) {
            return false;
        }
        // 单位为弧度
        var angle = getTotalAngle(user, weapon, angleField);
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

    private static boolean testConditionPredicate(LivingEntity user, ShieldData data, BooleanSupplier customPredicate) {
        var operator = IGunOperator.fromLivingEntity(user);
        var isReloading = operator.getSynReloadState().getStateType() != ReloadState.StateType.NOT_RELOADING;
        var isAiming = operator.getSynAimingProgress() >= 0.5F;
        if (data.getCondition() == ShieldData.Condition.CUSTOM) {
            if (user.isRemoved()) {
                return false;
            }
            return customPredicate.getAsBoolean();
        }
        if (isReloading && data.disableShieldWhenReloading()) {
            return false;
        }
        var condition = data.getCondition();
        return condition == ShieldData.Condition.ALWAYS || (condition == ShieldData.Condition.WHEN_AIMING) == isAiming;
    }

    private static Optional<ShieldData> getUsedShield(LivingEntity user, ItemStack weapon, Function<ShieldData, Double> field) {
        var gun = Gunsmith.getGunInfo(weapon).orElse(null);
        if (gun == null) {
            return Optional.empty();
        }
        return GunPartIterator.iterate(gun,
                gi -> ShieldData.fromGun(gi).filter(shield -> testConditionPredicate(user, shield, () -> gi.runScript(user, "gunsmith_is_shield_working", Boolean.class).orElse(false))),
                ami -> Optional.empty(),
                ati -> ShieldData.fromAttachment(ati).filter(shield -> testConditionPredicate(user, shield, () -> ati.runScript(user, gun, "gunsmithlib_attachment_is_shield_working", Boolean.class).orElse(false)))
        ).max(Comparator.comparing(field));
    }

    private static double getTotalAngle(LivingEntity user, ItemStack weapon, Function<ShieldData, Double> field) {
        return getUsedShield(user, weapon, field).map(field).orElse(0.0);
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
        if (event.getBullet() == null) {
            return;
        }
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
    public static void triggerAnimationOnBlockingVanillaDamage(ShieldBlockEvent event) {
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
