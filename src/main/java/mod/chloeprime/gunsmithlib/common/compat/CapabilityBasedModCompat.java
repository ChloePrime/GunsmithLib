package mod.chloeprime.gunsmithlib.common.compat;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber
public class CapabilityBasedModCompat {
    public static boolean hasAmmoToConsume(LivingEntity user, ItemStack gunStack) {
        if (user.level().isClientSide) {
            return getClientSyncedAmmoCountInBackpack(user) > 0;
        }
        return consumeAmmoFromPlayer(user, gunStack, 1, true) > 0;
    }

    public static int consumeAmmoFromPlayer(LivingEntity user, ItemStack gunStack, int requested, boolean simulation) {
        if (user.level().isClientSide) {
            return Math.min(requested, getClientSyncedAmmoCountInBackpack(user));
        }
        var gun = Gunsmith.getGunInfo(gunStack).orElse(null);
        if (gun == null) {
            return 0;
        }
        var capability = ForgeCapabilities.ITEM_HANDLER;
        var inventory = user.getCapability(capability, null).resolve().orElse(null);
        if (inventory == null) {
            return 0;
        }
        int found = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            // 镜喵背包右键放下后，Capability 会滞留在放下之前的物品格子里，
            // 这里让物品为空时视作没有 Capability
            var invItem = inventory.getStackInSlot(i);
            if (invItem.isEmpty()) {
                continue;
            }
            var backpack = invItem.getCapability(capability).resolve().orElse(null);
            if (backpack == null) {
                continue;
            }
            for (int j = 0; j < backpack.getSlots(); j++) {
                var stack = backpack.getStackInSlot(j);
                if (stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gunStack, stack)) {
                    found += backpack.extractItem(j, Math.min(requested - found, stack.getCount()), simulation).getCount();
                    if (found >= requested) {
                        return found;
                    }
                }
            }
        }
        if (found > 0 && !simulation) {
            var before = getClientSyncedAmmoCountInBackpack(user);
            setClientSyncedAmmoCountInBackpack(user, before - found);
        }
        return found;
    }

    @SubscribeEvent
    public static void refreshAmmoInBackpack(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        var user = event.player;
        if (user.level().isClientSide) {
            return;
        }
        var dataKey = GunsmithLib.Attributes.AMMO_IN_BACKPACK.get();
        var dataContainer = Objects.requireNonNull(user.getAttribute(dataKey));
        var existingValue = dataContainer.getBaseValue();
        if (IGun.mainHandHoldGun(user)) {
            // existingValue 为 -1 时说明玩家之前没有拿枪，需要立即刷新
            // 否则每 1 秒刷新一次
            if (existingValue >= 0) {
                var updateInterval = 20;
                var salt = user.getUUID().getLeastSignificantBits() & 0x7FFFFFFF;
                var now = user.level().getGameTime();
                if ((now + salt) % updateInterval != 0) {
                    return;
                }
            }
        } else {
            // existingValue >= 0 时说明玩家之前拿枪，
            // 但是玩家现在并没有拿着，所以把它设置成 -1，即没拿枪时的状态
            if (existingValue >= 0) {
                dataContainer.setBaseValue(-1);
                return;
            }
        }
        var ammo = consumeAmmoFromPlayer(user, user.getMainHandItem(), Integer.MAX_VALUE, true);
        dataContainer.setBaseValue(ammo);
    }

    public static int getClientSyncedAmmoCountInBackpack(LivingEntity user) {
        var dataKey = GunsmithLib.Attributes.AMMO_IN_BACKPACK.get();
        return Math.max(0, Math.round((float) user.getAttributeValue(dataKey)));
    }

    public static void setClientSyncedAmmoCountInBackpack(LivingEntity user, int value) {
        var dataKey = GunsmithLib.Attributes.AMMO_IN_BACKPACK.get();
        Objects.requireNonNull(user.getAttribute(dataKey)).setBaseValue(Math.max(0, value));
    }
}
