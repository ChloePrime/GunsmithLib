package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability;

import cn.chloeprime.commons.lang4.StringName;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@Mod.EventBusSubscriber
public final class DurabilitySystem {
    /**
     * 判断一个物品是否支持使用耐久度系统
     */
    public static boolean hasDurabilityCapability(ItemStack stack) {
        return hasDurabilityCapability(stack.getItem());
    }

    /**
     * 判断一种物品是否支持使用耐久度系统
     */
    public static boolean hasDurabilityCapability(Item item) {
        return item instanceof IGun || item instanceof IAttachment;
    }

    public static int damageGun(@Nonnull ItemStack gunStack, int amount, @Nonnull LivingEntity owner) {
        Objects.requireNonNull(gunStack);
        Objects.requireNonNull(owner);

        // 必须拥有本模组的耐久数据才会扣耐久，
        // 避免多个 mod 同时添加耐久功能导致多重消耗。
        if (Gunsmith.getGunInfo(gunStack).flatMap(DurabilityData::of).isEmpty()) {
            return 0;
        }

        var dmgBefore = gunStack.getDamageValue();
        gunStack.hurtAndBreak(amount, owner, owner1 -> owner1.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        return gunStack.getDamageValue() - dmgBefore;
    }

    public static int damageAttachment(@Nonnull GunInfo gun, @Nonnull AttachmentType slot, int amount, @Nonnull LivingEntity owner) {
        Objects.requireNonNull(gun);
        Objects.requireNonNull(slot);
        Objects.requireNonNull(owner);

        var attach = gun.gunItem().getAttachment(gun.gunStack(), slot);
        // 必须拥有本模组的耐久数据才会扣耐久，
        // 避免多个 mod 同时添加耐久功能导致多重消耗。
        if (Gunsmith.getAttachmentInfo(attach).flatMap(DurabilityData::of).isEmpty()) {
            return 0;
        }

        var dmgBefore = attach.getDamageValue();
        var maxDamage = attach.getMaxDamage();
        attach.hurtAndBreak(amount, owner, owner1 -> owner1.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        var dmgAfter = attach.isEmpty() ? maxDamage : attach.getDamageValue();
        var dmgConsumed = dmgAfter - dmgBefore;
        if (attach.isEmpty()) {
            gun.gunItem().unloadAttachment(gun.gunStack(), slot);
        } else {
            gun.gunItem().installAttachment(gun.gunStack(), attach);
        }
        return dmgConsumed;
    }

    @SubscribeEvent
    public static void onGunShoot(GunShootEvent event) {
        builtinDamageGunOrAttachment(event.getGunItemStack(), BuiltinDurabilityDamageCases.SHOOT, event.getShooter());
    }

    @SubscribeEvent
    public static void onGunFire(GunFireEvent event) {
        builtinDamageGunOrAttachment(event.getGunItemStack(), BuiltinDurabilityDamageCases.FIRE, event.getShooter());
    }

    private static void builtinDamageGunOrAttachment(ItemStack gunStack, StringName case_, @Nullable LivingEntity shooter) {
        if (shooter == null) {
            return;
        }
        var gun = Gunsmith.getGunInfo(gunStack).orElse(null);
        if (gun == null) {
            return;
        }
        
        builtinDamageGun(gun, case_, shooter);
        for (AttachmentType slot : AttachmentType.values()) {
            builtinDamageAttachment(gun, slot, case_, shooter);
        }
    }

    private static void builtinDamageGun(GunInfo gun, StringName case_, @Nonnull LivingEntity shooter) {
        Objects.requireNonNull(shooter);

        var durData = DurabilityData.of(gun).orElse(null);
        if (durData == null) {
            return;
        }
        int cost = durData.getDamageCases().getOrDefault(case_, 0);
        if (cost <= 0) {
            return;
        }
        damageGun(gun.gunStack(), cost, shooter);
    }

    private static void builtinDamageAttachment(GunInfo gun, AttachmentType slot, StringName case_, @Nonnull LivingEntity shooter) {
        Objects.requireNonNull(shooter);

        var durData = AttachmentInfo.of(gun.gunItem().getAttachment(gun.gunStack(), slot))
                .flatMap(DurabilityData::of)
                .orElse(null);
        if (durData == null) {
            return;
        }
        int cost = durData.getDamageCases().getOrDefault(case_, 0);
        if (cost <= 0) {
            return;
        }
        damageAttachment(gun, slot, cost, shooter);
    }

    private DurabilitySystem() {

    }
}
