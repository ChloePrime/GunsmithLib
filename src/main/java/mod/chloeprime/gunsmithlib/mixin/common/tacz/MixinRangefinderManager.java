package mod.chloeprime.gunsmithlib.mixin.common.tacz;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.pojo.display.gun.Rangefinder;
import com.tacz.guns.client.resource.pojo.display.gun.RangefinderManager;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.util.block.BlockRayTrace;
import mod.chloeprime.gunsmithlib.api.common.GunAttributes;
import mod.chloeprime.gunsmithlib.client.GunsmithClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @since 7.0.0
 */
@Mixin(value = RangefinderManager.class, remap = false)
public class MixinRangefinderManager {
    private static @Unique boolean gunsmithlib$showImpactWhenHittingBlock;
    private static @Unique double gunsmithlib$blockDistance;
    private static @Unique boolean gunsmithlib$hitBlock;
    @Shadow private static boolean showImpactPrediction;
    @Shadow private static boolean hitEntity;

    @Inject(
            method = "updateResult",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/block/BlockRayTrace;rayTraceBlocks(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"))
    private static void captureBlockTraceResult1(LocalPlayer player, ItemStack stack, Rangefinder config, CallbackInfo ci) {
        gunsmithlib$blockDistance = Double.NaN;
        gunsmithlib$hitBlock = false;
        gunsmithlib$showImpactWhenHittingBlock = GunsmithClientConfig.OFFICIAL_FIRE_CONTROL_ON_BLOCKS.get();
    }

    @ModifyExpressionValue(
            method = "updateResult",
            at = @At(value = "INVOKE", ordinal = 0, remap = true, target = "Lnet/minecraft/world/phys/Vec3;distanceTo(Lnet/minecraft/world/phys/Vec3;)D"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/block/BlockRayTrace;rayTraceBlocks(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;")))
    private static double captureBlockTraceResult2(double distance) {
        gunsmithlib$blockDistance = distance;
        gunsmithlib$hitBlock = true;
        return distance;
    }

    @Inject(method = "updateResult", at = @At("TAIL"))
    private static void showImpactIconWhenHittingBlock1(LocalPlayer player, ItemStack stack, Rangefinder config, CallbackInfo ci) {
        if (hitEntity) {
            return;
        }
        if (gunsmithlib$showImpactWhenHittingBlock && gunsmithlib$hitBlock) {
            if (!Double.isNaN(gunsmithlib$blockDistance)) {
                showImpactPrediction = true;
                if (stack.getItem() instanceof IGun iGun) {
                    updateImpactPrediction(player, stack, iGun, config, gunsmithlib$blockDistance);
                }
            }
        }
    }

    @ModifyExpressionValue(
            method = "updateImpactPrediction",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lcom/tacz/guns/client/resource/pojo/display/gun/RangefinderManager;hitEntity:Z"))
    private static boolean showImpactIconWhenHittingBlock2(boolean original) {
        return original || (gunsmithlib$showImpactWhenHittingBlock && gunsmithlib$hitBlock);
    }

    @Inject(method = "getAmmoSpeed", at = @At("HEAD"), cancellable = true)
    private static void useAmmoSpeedAttribute(LocalPlayer player, ItemStack stack, BulletData bulletData, CallbackInfoReturnable<Float> cir) {
        if (player != null) {
            var scale = AmmoConfig.GLOBAL_BULLET_SPEED_MODIFIER.get();
            cir.setReturnValue((float) (player.getAttributeValue(GunAttributes.BULLET_SPEED.get()) * 20 * scale));
        }
    }

    @WrapOperation(
            method = "updateImpactPrediction",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/client/resource/pojo/display/gun/RangefinderManager;calculateDrop(DDFFF)D"))
    private static double addPass2ToFixBlockHitDropPrecision(
            double passedDistance, double lookY, float speedPerTick, float gravity, float friction, Operation<Double> original,
            LocalPlayer player, ItemStack stack, IGun iGun, Rangefinder config, double givenDistance
    ) {
        var originalDrop = (double) original.call(passedDistance, lookY, speedPerTick, gravity, friction);
        int passes = GunsmithClientConfig.OFFICIAL_FIRE_CONTROL_ON_BLOCK_CALIBRATION_PASSES.get();
        if (hitEntity || passes <= 0) {
            return originalDrop;
        }
        var maxDistance = config.getMaxDistance();
        var partialTicks = Minecraft.getInstance().getFrameTime();
        var start = player.getEyePosition(partialTicks);
        var look = player.getViewVector(partialTicks);

        var distance = passedDistance;
        var drop = originalDrop;
        for (int i = 0; i < passes; i++) {
            var end = start.add(look.scale(distance)
                    .with(Direction.Axis.Y, 0)
                    .add(0, drop + lookY * distance, 0)
                    .normalize()
                    .scale(maxDistance));
            var newBlockResult = BlockRayTrace.rayTraceBlocks(player.level(), new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            distance = start.distanceTo(newBlockResult.getLocation());
            drop = calculateDrop(distance, lookY, speedPerTick, gravity, friction);
        }
        return drop;
    }

    @Shadow
    private static void updateImpactPrediction(LocalPlayer player, ItemStack stack, IGun iGun, Rangefinder config, double distance) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static double calculateDrop(double distance, double lookY, float speedPerTick, float gravity, float friction) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}
