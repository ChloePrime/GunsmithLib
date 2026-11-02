package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.GuiGraphicsAPI;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.ClientGunScripting;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Optional;

/**
 * 在 HUD 渲染完成后调用动画状态机脚本的 {@code gunsmithlib_render_hud} 钩子，
 * 允许脚本直接往 HUD 上绘制内容。
 * <p>
 * 钩子签名：{@code function M.gunsmithlib_render_hud(context, gui)}，
 * 其中 {@code context} 为状态机 context，{@code gui} 为 {@link GuiGraphicsAPI}。
 *
 * @since 7.0.0
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public final class RenderHudHook {
    public static final String HOOK_NAME = "gunsmithlib_render_hud";

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        var stack = Optional.ofNullable(Minecraft.getInstance().player)
                .map(LivingEntity::getMainHandItem)
                .orElse(null);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        var asr = AnimScriptReady.of(stack).orElse(null);
        if (asr == null) {
            return;
        }
        var gui = new GuiGraphicsAPI(event.getGuiGraphics());
        try {
            asr.runWithContext(() -> Optional.of(asr.script())
                    .map(script -> GsHelper.checkFunction(script.get(HOOK_NAME)))
                    .ifPresent(func -> func.call(
                            CoerceJavaToLua.coerce(asr.context()),
                            CoerceJavaToLua.coerce(gui))));
        } catch (Exception exception) {
            GunsmithLib.LOGGER.warn(ClientGunScripting.MARKER, "Failed to invoke script hook {}", HOOK_NAME, exception);
        }
    }

    private RenderHudHook() {
    }
}
