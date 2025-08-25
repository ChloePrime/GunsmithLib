package mod.chloeprime.gunsmithlib.mixin;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.FeedType;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunReloadData;
import mod.chloeprime.gunsmithlib.common.util.GsHooks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModernKineticGunScriptAPI.class, remap = false)
public class MixinReloadProcedure {
    @Shadow private ItemStack itemStack;
    @Shadow private AbstractGunItem abstractGunItem;
    @Shadow private LivingEntity shooter;

    @Inject(
            method = "putAmmoInMagazine",
            at = @At("HEAD"),
            cancellable = true)
    private void postFeedEvents(int amount, CallbackInfoReturnable<Integer> cir) {
        var gun = this.itemStack;
        var kun = this.abstractGunItem;
        var shooter = this.shooter;
        if (gun == null || kun == null || shooter == null) {
            return;
        }
        var useInvAmmo = TimelessAPI.getCommonGunIndex(kun.getGunId(gun))
                .map(CommonGunIndex::getGunData)
                .map(GunData::getReloadData)
                .map(GunReloadData::getType)
                .filter(reloadType -> reloadType == FeedType.INVENTORY)
                .isPresent();
        if (useInvAmmo) {
            return;
        }
        try {
            GsHooks.onReloadFeed(kun, shooter, gun, !kun.hasBulletInBarrel(gun), () -> cir.setReturnValue(0));
        } catch (NoClassDefFoundError error) {
            if (FMLLoader.isProduction()) {
                throw error;
            }
        }
    }
}
