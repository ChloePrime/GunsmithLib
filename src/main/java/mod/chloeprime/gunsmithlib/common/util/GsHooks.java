package mod.chloeprime.gunsmithlib.common.util;

import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.api.common.GunReloadFeedEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public class GsHooks {
    public static void onReloadFeed(IGun gun, LivingEntity shooter, ItemStack gunItem, boolean loadBarrel, Runnable canceller) {
        var gunInfo = GsHelper.unpack(gun, gunItem).orElse(null);
        if (gunInfo == null) {
            return;
        }
        // pre
        var canceled = NeoForge.EVENT_BUS.post(new GunReloadFeedEvent.Pre(shooter, gunInfo, loadBarrel)).isCanceled();
        if (canceled) {
            canceller.run();
            return;
        }

        // Post
        NeoForge.EVENT_BUS.post(new GunReloadFeedEvent.Post(shooter, gunInfo, loadBarrel));
    }
}
