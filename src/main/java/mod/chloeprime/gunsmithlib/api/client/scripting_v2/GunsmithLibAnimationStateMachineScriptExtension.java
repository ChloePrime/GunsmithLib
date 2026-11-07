package mod.chloeprime.gunsmithlib.api.client.scripting_v2;

import com.google.common.base.Suppliers;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.resource.index.CommonGunIndex;
import mod.chloeprime.gunsmithlib.api.client.GunsmithLibAnimationConstant;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.ClientShootStates;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.GuiGraphicsAPI;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.InWorldGuiGraphics;
import mod.chloeprime.gunsmithlib.api.common.GunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.GunsmithLibCommonScriptExtension;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.client.AbstractGunAnimationStateContextExtension;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content.ClientShooterStatesImpl;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content.GuiGraphicsAPIImpl;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content.InWorldGuiGraphicsImpl;
import mod.chloeprime.gunsmithlib.client.util.WorldToScreenProjection;
import mod.chloeprime.gunsmithlib.common.AbstractCommonScriptingExtension;
import mod.chloeprime.gunsmithlib.common.util.LinearAlgebraTypes;
import mod.chloeprime.gunsmithlib.mixin.client.minecraft.I18nAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.luaj.vm2.LuaTable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 示例用法：{@code api:gunsmithlib_extension():play_overheat_sound()}
 *
 * @see GunScriptAPIExtension#gunsmithlib_extension()
 * @since 5.6.0
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class GunsmithLibAnimationStateMachineScriptExtension extends GunsmithLibCommonScriptExtension {
    /**
     * 检测是否能安全调用 {@link #get_previous_gun_id()}。
     *
     * @return 如果为 true，则调用 {@link #get_previous_gun_id()} 不会抛出异常。
     */
    public boolean has_previous_gun_id() {
        return GunsmithLibClient.getPreviousGunId().isPresent();
    }

    /**
     * 获取弹种切换前的枪械 id。
     * <p>
     * 警告：只能在
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_VARIANT_SWITCHED} 和
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED}
     * 时调用，
     * 其他时候调用会抛出异常。
     *
     * @return 弹种切换时旧枪械 index 的 id
     */
    public String get_previous_gun_id() {
        var ret = (String) GunsmithLibClient.getPreviousGunId().orElse(null);
        if (ret == null) {
            throw new IllegalStateException("get_previous_gun_id can only be called during gun variant switching.");
        }
        return ret;
    }

    /**
     * 获取弹种切换前枪械弹匣内的子弹数量。
     * <p>
     * 警告：只能在
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_VARIANT_SWITCHED} 和
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED}
     * 时调用，
     * 其他时候调用会返回 {@code -1}。
     *
     * @return 弹种切换前枪械弹匣内的子弹数量
     */
    public int get_previous_ammo_amount() {
        return GunsmithLibClient.getPreviousAmmoAmount();
    }

    /**
     * 获取弹种切换前枪膛内是否有子弹。
     * <p>
     * 警告：只能在
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_VARIANT_SWITCHED} 和
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED}
     * 时调用，
     * 其他时候调用会返回 {@code false}。
     *
     * @return 弹种切换前枪膛内是否有子弹
     */
    public boolean get_previous_has_bullet_in_barrel() {
        return GunsmithLibClient.prevHasAmmoInBarrel();
    }

    /**
     * 获取弹种切换前枪械内累计子弹数量（弹匣 + 枪膛）。
     * <p>
     * 警告：只能在
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_VARIANT_SWITCHED} 和
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED}
     * 时调用，
     * 其他时候调用会返回 {@code -1}。
     *
     * @return 弹种切换前枪械内累计子弹数量（弹匣 + 枪膛）
     */
    public int get_previous_total_ammo_amount() {
        return get_previous_ammo_amount() + (get_previous_has_bullet_in_barrel() ? 1 : 0);
    }

    /**
     * 获取枪械 data 里配置的逻辑脚本的脚本参数。
     * 不要修改返回的 Table！会出现线程安全问题！
     *
     * @return 枪械 data 里配置的逻辑脚本的脚本参数
     * @since 6.2
     */
    public LuaTable logic_script_params() {
        return gunInfo()
                .map(GunInfo::index)
                .map(CommonGunIndex::getScriptParam)
                .orElseGet(fallbackParams);
    }

    /**
     * 获取客户端射手的各种状态的接口。
     * 如果在玩家未进入游戏时不小心触发状态机钩子，那么此时调用该方法会返回 {@code nil}
     *
     * @return 客户端射手的各种状态的接口
     */
    @Override
    public @Nullable ClientShootStates shooter_states() {
        return Optional.ofNullable(Minecraft.getInstance().player)
                .map(shooter -> new ClientShooterStatesImpl(shooter, this.api))
                .orElse(null);
    }

    /**
     * 获取一个封装了新建 {@link GuiGraphics} 实例的 API 对象。
     * <p>
     * 每次调用都会创建一个新的 {@link GuiGraphics}（使用全新的 {@code PoseStack}），
     * 不会受到当前 HUD 渲染状态的影响。
     * <p>
     * 返回的对象只在当前回调期间有效，请不要跨帧保存。
     * 建议在 HUD 渲染期间（例如 {@code gunsmithlib_modify_display_property} 回调中）使用。
     *
     * @return 封装了新建 {@link GuiGraphics} 实例的 API 对象
     * @since 7.0.0
     */
    public GuiGraphicsAPI gui_graphics() {
        var minecraft = Minecraft.getInstance();
        var graphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
        return new GuiGraphicsAPIImpl(graphics);
    }

    /**
     * 创建一个锚定在世界坐标上的 HUD 绘制对象，用于绘制跟随世界物体的 HUD
     * （例如敌人头顶的血条、锁定准星）。
     * <p>
     * 返回对象的坐标系原点为 {@code worldPos} 在屏幕上的投影位置，
     * {@code +x} 向右、{@code +y} 向下，其余绘制方法与 {@link GuiGraphicsAPI} 完全一致。
     * 默认会根据近大远小进行缩放。
     * <p>
     * 锚点位于相机背后或相机尚未初始化时 {@link InWorldGuiGraphics#visible()} 为 {@code false}，
     * 此时所有绘制调用都不会产生效果。
     * <p>
     * 与 {@link #gui_graphics()} 一样，返回的对象只在当前回调期间有效，请不要跨帧保存。
     *
     * @param worldPos 锚点的世界坐标
     * @return 锚定在世界坐标上的 HUD 绘制对象
     * @since 7.0.0
     */
    public InWorldGuiGraphics gui_graphics_in_world(Vector3d worldPos) {
        return gui_graphics_in_world(worldPos, 1.0F);
    }

    /**
     * 创建一个锚定在世界坐标上的 HUD 绘制对象，并指定缩放。
     *
     * @param worldPos 锚点的世界坐标
     * @param scale    每个 HUD 单位对应的 GUI 像素数
     * @return 锚定在世界坐标上的 HUD 绘制对象
     * @see #gui_graphics_in_world(Vector3d)
     * @since 7.0.0
     */
    public InWorldGuiGraphics gui_graphics_in_world(Vector3d worldPos, float scale) {
        return gui_graphics_in_world(worldPos, scale, true);
    }

    /**
     * 创建一个锚定在世界坐标上的 HUD 绘制对象，并指定缩放和是否随距离缩放。
     * <p>
     * {@code distanceScaled} 为 {@code true} 时 HUD 的视觉尺寸在世界中保持恒定：
     * 锚点距离相机 {@code InWorldGuiGraphicsImpl.REFERENCE_DISTANCE}（8）格时
     * 1 个 HUD 单位等于 1 个 GUI 像素，距离越远 HUD 越小、越近越大，
     * 并且会跟随 FOV 与瞄准缩放一起缩放（开镜放大时 HUD 也会同步放大）。
     * 适合血条这类“贴在世界物体上”的 HUD。
     * 为 {@code false} 时 HUD 在屏幕上保持恒定尺寸，适合锁定准星这类 HUD。
     *
     * @param worldPos       锚点的世界坐标
     * @param scale          每个 HUD 单位对应的 GUI 像素数
     * @param distanceScaled 是否随距离缩放
     * @return 锚定在世界坐标上的 HUD 绘制对象
     * @since 7.0.0
     */
    public InWorldGuiGraphics gui_graphics_in_world(Vector3d worldPos, float scale, boolean distanceScaled) {
        var minecraft = Minecraft.getInstance();
        var graphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
        return InWorldGuiGraphicsImpl.at(graphics, LinearAlgebraTypes.joml2moj(worldPos), scale, distanceScaled);
    }

    /**
     * 将世界坐标转换为用于 HUD 渲染的屏幕坐标。
     * <p>
     * 返回值的坐标系与 {@link GuiGraphicsAPI}（以及原版 {@link GuiGraphics}）一致，
     * 都是 GUI 缩放后的屏幕坐标，可以直接用于绘制。
     * <p>
     * 投影用的是本帧世界渲染实际使用的视图投影矩阵，
     * 所以瞄准缩放、疾跑等造成的 FOV 变化都会被正确计算。
     * <p>
     * 如果相机尚未初始化（例如刚进入世界、还没有渲染过一帧），
     * 或者传入的坐标位于相机背后，那么会返回 {@code nil}。
     *
     * @param worldPos 输入的世界坐标
     * @return 该世界坐标在屏幕上的屏幕坐标（浮点数，可直接用于绘制），无法投影时返回 {@code null}
     * @since 7.0.0
     */
    public @Nullable Vector2f world_pos_to_screen_pos(Vector3d worldPos) {
        return WorldToScreenProjection.worldToScreen(LinearAlgebraTypes.joml2moj(worldPos));
    }

    /**
     * 将一个翻译键转换为翻译后的文本并进行。
     * 如果没有传入 {@code args}, 则不会进行字符串格式化。
     * <p>
     * 警告：1.21.1 的 LuaJ 调用 vararg Java 方法有 bug 且无法使用 Mixin 修复。在上游修复之前如果你要做跨版本兼容，
     * 那么需要将 vararg 手动转换成数组传入。例：{@code translate("text.xxx.xxx")} -> {@code translate("text.xxx.xxx", {})}，
     * {@code translate("text.xxx.xxx", arg1, arg2)} -> {@code translate("text.xxx.xxx", { arg1, arg2 })}
     *
     * @param langKey 本地化键
     * @return 译文或输入的本地化键
     * @since 7.1.0
     */
    public final String translate(String langKey, Object... args) {
        return args.length == 0
                ? I18nAccessor.getLanguage().getOrDefault(langKey)
                : I18n.get(langKey, args);
    }

    // 下面是内部 API

    private final GunAnimationStateContext api;
    private final AbstractGunAnimationStateContextExtension v1;
    private final Supplier<LuaTable> fallbackParams = Suppliers.memoize(LuaTable::new);

    @ApiStatus.Internal
    public GunsmithLibAnimationStateMachineScriptExtension(GunAnimationStateContext ctx) {
        super((AbstractCommonScriptingExtension) ctx);
        this.api = ctx;
        this.v1 = (AbstractGunAnimationStateContextExtension) ctx;
    }
}
