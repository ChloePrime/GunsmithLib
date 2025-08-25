package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class RangefinderPapi implements Function<ItemStack, String> {
    public static final String NAME = GunsmithLib.loc("rangefinder_result").toString();
    public static final RangefinderPapi INSTANCE = new RangefinderPapi();

    private static final String FALLBACK = String.valueOf(0);
    private static final Minecraft MC = Minecraft.getInstance();

    private long lastTick = -1;
    private float lastPartialTick;
    private String lastResult = FALLBACK;

    // 此方法仅在客户端每帧执行一次，所以无需担心会卡
    @Override
    public String apply(ItemStack stack) {
        var shooter = MC.player;
        if (shooter == null) {
            return FALLBACK;
        }
        var gun = Gunsmith.getGunInfo(stack).orElse(null);
        if (gun == null) {
            return FALLBACK;
        }
        var now = shooter.level().getGameTime();
        var partial = ClientProxy.getPartialTick();
        if (lastTick == now && lastPartialTick == partial) {
            return lastResult;
        }
        lastTick = now;
        lastPartialTick = partial;
        double maxRange = GsHelper.getEstimatedMaxRange(shooter, shooter.getMainHandItem());
        double range = Rangefinder.clip(shooter, shooter.getEyePosition(), shooter.getLookAngle(), 0, maxRange).getLength();
        return lastResult = "%.2f".formatted(range);
    }

    private RangefinderPapi() {
    }
}
