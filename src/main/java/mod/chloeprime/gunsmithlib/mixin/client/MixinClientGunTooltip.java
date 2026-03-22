package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import com.tacz.guns.item.GunTooltipPart;
import com.tacz.guns.resource.index.CommonGunIndex;
import mod.chloeprime.gunsmithlib.api.client.GunTooltipContext;
import mod.chloeprime.gunsmithlib.api.client.GunTooltipEvent;
import mod.chloeprime.gunsmithlib.api.client.RenderGunTooltipTextEvent;
import mod.chloeprime.gunsmithlib.api.common.GunAttributes;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.client.ClientInternalEvents;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;

@Mixin(value = ClientGunTooltip.class, remap = false)
public class MixinClientGunTooltip {
    @ModifyExpressionValue(
            method = "getText",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/AttachmentDataUtils;getDamageWithAttachment(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)D"))
    private double makeDisplayedDamageConsiderAttributeModifiers(double original) {
        var gun = this.gun;
        var gunIndex = this.gunIndex;
        if (gun == null || gunIndex == null || gun.isEmpty()) {
            return original;
        }
        var shrapnel = gunIndex.getBulletData().getBulletAmount();
        return GsHelper.evaluateItemAttribute(gun, GunAttributes.BULLET_DAMAGE, original / shrapnel) * shrapnel;
    }

    @ModifyExpressionValue(
            method = "getText",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/AttachmentDataUtils;getArmorIgnoreWithAttachment(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)D"))
    private double makeDisplayedArmorPiercingConsiderAttributeModifiers(double original) {
        var gun = this.gun;
        if (gun == null || gun.isEmpty()) {
            return original;
        }
        return GsHelper.evaluateItemAttribute(gun, GunAttributes.ARMOR_PIERCING_RATIO, original);
    }

    @ModifyExpressionValue(
            method = "getText",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/AttachmentDataUtils;getHeadshotMultiplier(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)D"))
    private double makeDisplayedHeadshotMultiplierConsiderAttributeModifiers(double original) {
        var gun = this.gun;
        if (gun == null || gun.isEmpty()) {
            return original;
        }
        return GsHelper.evaluateItemAttribute(gun, GunAttributes.HEADSHOT_MULTIPLIER, original);
    }

    @Shadow @Final private ItemStack gun;
    @Shadow @Final private CommonGunIndex gunIndex;

    // GunTooltipEvent

    @Inject(method = "<init>", at = @At("TAIL"))
    private void afterInit(GunTooltip tooltip, CallbackInfo ci) {
        var self = (ClientGunTooltip) (Object) this;
        var event = new GunTooltipEvent.Initialize(new GunTooltipContext(self, gunsmithlib$gun()));
        MinecraftForge.EVENT_BUS.post(event);
    }

    @ModifyReturnValue(method = "getHeight", at = @At("RETURN"))
    private int modifyHeight(int original) {
        var self = (ClientGunTooltip) (Object) this;
        var event = new GunTooltipEvent.ComputeHeight(new GunTooltipContext(self, gunsmithlib$gun()), original);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getHeight();
    }

    @WrapOperation(
            method = "renderText", remap = true,
            at = @At(value = "INVOKE", ordinal = 0, remap = false, target = "Lcom/tacz/guns/client/tooltip/ClientGunTooltip;shouldShow(Lcom/tacz/guns/item/GunTooltipPart;)Z"),
            slice = @Slice(from = @At(value = "FIELD", remap = false, opcode = Opcodes.GETSTATIC, target = "Lcom/tacz/guns/item/GunTooltipPart;DESCRIPTION:Lcom/tacz/guns/item/GunTooltipPart;")))
    private boolean onRenderTooltip0(
            ClientGunTooltip instance, GunTooltipPart part, Operation<Boolean> original,
            Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer
    ) {
        var ctx = new RenderGunTooltipTextEvent.RenderContext(font, x, gunsmithlib$lastYOffset, matrix, buffer);
        return gunsmith$onRenderTooltip(instance, ctx, original.call(instance, part), RenderGunTooltipTextEvent.BeforeDescription::new);
    }

    @WrapOperation(
            method = "renderText", remap = true,
            at = @At(value = "INVOKE", ordinal = 0, remap = false, target = "Lcom/tacz/guns/client/tooltip/ClientGunTooltip;shouldShow(Lcom/tacz/guns/item/GunTooltipPart;)Z"),
            slice = @Slice(from = @At(value = "FIELD", remap = false, opcode = Opcodes.GETSTATIC, target = "Lcom/tacz/guns/item/GunTooltipPart;AMMO_INFO:Lcom/tacz/guns/item/GunTooltipPart;")))
    private boolean onRenderTooltip1(
            ClientGunTooltip instance, GunTooltipPart part, Operation<Boolean> original,
            Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer
    ) {
        var ctx = new RenderGunTooltipTextEvent.RenderContext(font, x, gunsmithlib$lastYOffset, matrix, buffer);
        return gunsmith$onRenderTooltip(instance, ctx, original.call(instance, part), RenderGunTooltipTextEvent.AfterDescription::new);
    }

    @WrapOperation(
            method = "renderText", remap = true,
            at = @At(value = "INVOKE", ordinal = 0, remap = false, target = "Lcom/tacz/guns/client/tooltip/ClientGunTooltip;shouldShow(Lcom/tacz/guns/item/GunTooltipPart;)Z"),
            slice = @Slice(from = @At(value = "FIELD", remap = false, opcode = Opcodes.GETSTATIC, target = "Lcom/tacz/guns/item/GunTooltipPart;BASE_INFO:Lcom/tacz/guns/item/GunTooltipPart;")))
    private boolean onRenderTooltip2(
            ClientGunTooltip instance, GunTooltipPart part, Operation<Boolean> original,
            Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer
    ) {
        var ctx = new RenderGunTooltipTextEvent.RenderContext(font, x, gunsmithlib$lastYOffset, matrix, buffer);
        return gunsmith$onRenderTooltip(instance, ctx, original.call(instance, part), RenderGunTooltipTextEvent.AfterAmmoInfo::new);
    }

    @WrapOperation(
            method = "renderText", remap = true,
            at = @At(value = "INVOKE", ordinal = 0, remap = false, target = "Lcom/tacz/guns/client/tooltip/ClientGunTooltip;shouldShow(Lcom/tacz/guns/item/GunTooltipPart;)Z"),
            slice = @Slice(from = @At(value = "FIELD", remap = false, opcode = Opcodes.GETSTATIC, target = "Lcom/tacz/guns/item/GunTooltipPart;EXTRA_DAMAGE_INFO:Lcom/tacz/guns/item/GunTooltipPart;")))
    private boolean onRenderTooltip3(
            ClientGunTooltip instance, GunTooltipPart part, Operation<Boolean> original,
            Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer
    ) {
        var ctx = new RenderGunTooltipTextEvent.RenderContext(font, x, gunsmithlib$lastYOffset, matrix, buffer);
        return gunsmith$onRenderTooltip(instance, ctx, original.call(instance, part), RenderGunTooltipTextEvent.AfterBaseInfo::new);
    }

    @WrapOperation(
            method = "renderText", remap = true,
            at = @At(value = "INVOKE", ordinal = 0, remap = false, target = "Lcom/tacz/guns/client/tooltip/ClientGunTooltip;shouldShow(Lcom/tacz/guns/item/GunTooltipPart;)Z"),
            slice = @Slice(from = @At(value = "FIELD", remap = false, opcode = Opcodes.GETSTATIC, target = "Lcom/tacz/guns/item/GunTooltipPart;UPGRADES_TIP:Lcom/tacz/guns/item/GunTooltipPart;")))
    private boolean onRenderTooltip4(
            ClientGunTooltip instance, GunTooltipPart part, Operation<Boolean> original,
            Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer
    ) {
        var ctx = new RenderGunTooltipTextEvent.RenderContext(font, x, gunsmithlib$lastYOffset, matrix, buffer);
        return gunsmith$onRenderTooltip(instance, ctx, original.call(instance, part), RenderGunTooltipTextEvent.AfterExtraDamageInfo::new);
    }

    @WrapOperation(
            method = "renderText", remap = true,
            at = @At(value = "INVOKE", ordinal = 0, remap = false, target = "Lcom/tacz/guns/client/tooltip/ClientGunTooltip;shouldShow(Lcom/tacz/guns/item/GunTooltipPart;)Z"),
            slice = @Slice(from = @At(value = "FIELD", remap = false, opcode = Opcodes.GETSTATIC, target = "Lcom/tacz/guns/item/GunTooltipPart;PACK_INFO:Lcom/tacz/guns/item/GunTooltipPart;")))
    private boolean onRenderTooltip5(
            ClientGunTooltip instance, GunTooltipPart part, Operation<Boolean> original,
            Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer
    ) {
        var ctx = new RenderGunTooltipTextEvent.RenderContext(font, x, gunsmithlib$lastYOffset, matrix, buffer);
        return gunsmith$onRenderTooltip(instance, ctx, original.call(instance, part), RenderGunTooltipTextEvent.AfterUpgradeTip::new);
    }

    @Inject(method = "renderText", remap = true, at = @At("TAIL"))
    private void onRenderTooltip6(
            Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer,
            CallbackInfo ci
    ) {
        var self = (ClientGunTooltip) (Object) this;
        var ctx = new RenderGunTooltipTextEvent.RenderContext(font, x, gunsmithlib$lastYOffset, matrix, buffer);
        gunsmith$onRenderTooltip(self, ctx, true, RenderGunTooltipTextEvent.AfterPackInfo::new);
        gunsmithlib$lastYOffset = 0;
        gunsmithlib$lastEvent = null;
    }

    @ModifyVariable(
            method = "renderText", remap = true,
            at = @At(value = "INVOKE", remap = false, target = "Lcom/tacz/guns/client/tooltip/ClientGunTooltip;shouldShow(Lcom/tacz/guns/item/GunTooltipPart;)Z"),
            name = "yOffset")
    private int getHeight(int yOffset) {
        return this.gunsmithlib$lastYOffset = yOffset;
    }

    @ModifyVariable(
            method = "renderText", remap = true,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, remap = false, target = "Lcom/tacz/guns/client/tooltip/ClientGunTooltip;shouldShow(Lcom/tacz/guns/item/GunTooltipPart;)Z"),
            name = "yOffset")
    private int pumpHeight(int yOffset) {
        var event = gunsmithlib$lastEvent;
        return event == null ? yOffset : yOffset + event.getHeight();
    }

    @Unique
    private boolean gunsmith$onRenderTooltip(
            ClientGunTooltip instance, RenderGunTooltipTextEvent.RenderContext ctx, boolean original,
            BiFunction<GunTooltipContext, RenderGunTooltipTextEvent.RenderContext, ? extends RenderGunTooltipTextEvent> eventConstructor
    ) {
        var context = new GunTooltipContext(instance, gunsmithlib$gun());
        var event = this.gunsmithlib$lastEvent = eventConstructor.apply(context, ctx);
        var internal = MinecraftForge.EVENT_BUS.post(new ClientInternalEvents.RenderGunTooltipTextPre(event));
        var external = MinecraftForge.EVENT_BUS.post(event);
        var canceled = internal || external;
        if (!canceled) {
            event.doRender();
        }
        return original && !canceled;
    }

    @Unique
    private @Nullable GunInfo gunsmithlib$gun() {
        return GsHelper.unpack(iGun, gun).orElse(null);
    }

    private @Unique int gunsmithlib$lastYOffset = 0;
    private @Unique RenderGunTooltipTextEvent gunsmithlib$lastEvent;
    @Shadow @Final private IGun iGun;
}
