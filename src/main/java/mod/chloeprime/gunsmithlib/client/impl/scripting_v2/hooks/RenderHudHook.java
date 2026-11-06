package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.GuiGraphicsAPI;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.ClientGunScripting;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content.GuiGraphicsAPIImpl;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Optional;

/**
 * 在 HUD 层和手部渲染之前两个时机调用动画状态机脚本的绘制钩子。
 * <ul>
 * <li>{@code gunsmithlib_render_hud(context, gui)}：在 HUD 层绘制，
 *     {@code gui} 为 {@link GuiGraphicsAPI}，和原版 HUD 一起渲染（手部之后）。</li>
 * <li>{@code gunsmithlib_render_hud_before_hand(context, gui)}：在世界渲染结束、手部渲染之前绘制，
 *     {@code gui} 为原版的 {@link GuiGraphics}。绘制期间会关闭深度测试；
 *     原版随后会清一次深度缓冲再画手部，因此手和手持物会盖在 HUD 之上。
 *     这个时机第一人称和第三人称都会触发。</li>
 * </ul>
 * 两个钩子都只在主手持有带脚本的枪械时触发。
 *
 * @since 7.0.0
 */
@EventBusSubscriber(Dist.CLIENT)
public final class RenderHudHook {
    /**
     * 常规 HUD 钩子的名字。
     */
    public static final String HOOK_NAME = "gunsmithlib_render_hud";

    /**
     * 手部渲染之前的 HUD 钩子的名字。
     */
    public static final String BEFORE_HAND_HOOK_NAME = "gunsmithlib_render_hud_before_hand";

    public static void withHudMode(Runnable code) {
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        var savedProjection = RenderSystem.getProjectionMatrix();
        var savedSorting = RenderSystem.getVertexSorting();
        // 和原版 HUD 完全一致的 GUI 正交投影（近平面 1000，远平面为原版 GUI 的默认值 21000）
        var guiProjection = new Matrix4f().setOrtho(
                0.0F,
                (float) ((double) window.getWidth() / window.getGuiScale()),
                (float) ((double) window.getHeight() / window.getGuiScale()),
                0.0F, 1000.0F, GUI_FAR_PLANE);
        var modelViewStack = RenderSystem.getModelViewStack();

        RenderSystem.setProjectionMatrix(guiProjection, VertexSorting.ORTHOGRAPHIC_Z);
        modelViewStack.pushMatrix();
        modelViewStack.identity();
        modelViewStack.translate(0, 0, 1000F - GUI_FAR_PLANE);
        RenderSystem.applyModelViewMatrix();
        try {
            var bufferSource = minecraft.renderBuffers().bufferSource();
            // 关掉深度测试，让 HUD 直接盖在这个时机已经渲染好的画面上
            RenderSystem.disableDepthTest();
            code.run();
            bufferSource.endBatch();
        } finally {
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(savedProjection, savedSorting);
        }
    }

    /**
     * 原版 GUI 正交投影的远平面，和 {@code ForgeHooksClient.getGuiFarPlane()} 的默认值一致。
     */
    private static final float GUI_FAR_PLANE = 21000.0F;

    /**
     * 在 HUD 渲染完成后调用脚本，此时手部已经画完，HUD 会盖在手部之上。
     */
    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        var asr = findMainHandScript().orElse(null);
        if (asr == null) {
            return;
        }
        invokeHook(asr, HOOK_NAME, new GuiGraphicsAPIImpl(event.getGuiGraphics()));
    }

    /**
     * 在世界渲染结束、手部渲染之前调用脚本，此时手部还没画，因此手部会盖在 HUD 之上。
     * <p>
     * 挂在 {@code RenderLevelStageEvent.Stage.AFTER_LEVEL} 上，第一人称和第三人称都会触发。
     */
    @SubscribeEvent
    public static void onBeforeRenderingHand(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        // 和原版 HUD 保持一致：F1 隐藏界面时不画
        if (minecraft.options.hideGui) {
            return;
        }
        var asr = findMainHandScript().orElse(null);
        if (asr == null) {
            return;
        }
        renderBeforeHand(minecraft, asr);
    }

    /**
     * 在世界渲染结束、手部渲染之前绘制 HUD。
     * <p>
     * 这个时机原版还没切换到 GUI 的投影矩阵，所以这里临时切回 GUI 的正交投影和单位模型视图矩阵，
     * 画完之后再恢复；绘制期间关闭深度测试，避免 HUD 被世界几何挡住。
     * 原版随后会清一次深度缓冲再渲染手部，因此手和手持物会盖在 HUD 之上。
     */
    private static void renderBeforeHand(Minecraft minecraft, AnimScriptReady asr) {
        var bufferSource = minecraft.renderBuffers().bufferSource();
        var graphics = new GuiGraphicsAPIImpl(new GuiGraphics(minecraft, bufferSource));
        withHudMode(() -> invokeHook(asr, BEFORE_HAND_HOOK_NAME, graphics));
    }

    /**
     * 取得主手物品对应的动画状态机脚本。
     *
     * @return 主手物品对应的动画状态机脚本，不存在时返回 {@code null}
     */
    private static Optional<AnimScriptReady> findMainHandScript() {
        var stack = Optional.ofNullable(Minecraft.getInstance().player)
                .map(LivingEntity::getMainHandItem)
                .orElse(null);
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return AnimScriptReady.of(stack);
    }

    private static void invokeHook(AnimScriptReady asr, String hookName, GuiGraphicsAPI gui) {
        try {
            asr.runWithContext(() -> Optional.of(asr.script())
                    .map(script -> GsHelper.checkFunction(script.get(hookName)))
                    .ifPresent(func -> func.call(
                            CoerceJavaToLua.coerce(asr.context()),
                            CoerceJavaToLua.coerce(gui))));
        } catch (Exception exception) {
            GunsmithLib.LOGGER.warn(ClientGunScripting.MARKER, "Failed to invoke script hook {}", hookName, exception);
        }
    }

    private RenderHudHook() {
    }
}
