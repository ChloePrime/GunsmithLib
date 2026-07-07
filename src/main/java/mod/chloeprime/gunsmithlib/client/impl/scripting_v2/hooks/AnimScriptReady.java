package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.resource.GunDisplayInstance;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.ClientGunScriptingAPI;
import mod.chloeprime.gunsmithlib.client.internal.EnhancedLuaStateMachine;
import mod.chloeprime.gunsmithlib.mixin.client.GunAnimationStateContextAccessor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaTable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

public record AnimScriptReady(
        @Nonnull ItemStack stack,
        @Nonnull LuaTable script,
        @Nonnull GunAnimationStateContext context
) {
    public static Optional<AnimScriptReady> of(ItemStack stack) {
        var asm = TimelessAPI.getGunDisplay(stack)
                .map(GunDisplayInstance::getAnimationStateMachine)
                .orElse(null);
        if (asm == null) {
            return Optional.empty();
        }
        var script = EnhancedLuaStateMachine.getScript(asm).orElse(null);
        if (script == null) {
            return Optional.empty();
        }
        var context = Optional
                .ofNullable(asm.getContext())
                .filter(ctx -> ((GunAnimationStateContextAccessor) ctx).getCurrentGunItem() == stack)
                .orElseGet(() -> Util.make(new ClientGunScriptingAPI(), ctx -> {
                    ctx.setPartialTicks(Minecraft.getInstance().getPartialTick());
                    ctx.setCurrentGunItem(stack);
                }));
        return Optional.of(new AnimScriptReady(stack, script, context));
    }

    public void runWithContext(Runnable code) {
        supplyWithContext(() -> {
            code.run();
            return (Void) null;
        });
    }

    public <T> T supplyWithContext(Supplier<T> code) {
        var ctx = this.context;
        var before = ((GunAnimationStateContextAccessor) ctx).getCurrentGunItem();
        if (before == this.stack) {
            return code.get();
        }

        try {
            ctx.setCurrentGunItem(this.stack);
            return code.get();
        } finally {
            ctx.setCurrentGunItem(before);
        }
    }
}
