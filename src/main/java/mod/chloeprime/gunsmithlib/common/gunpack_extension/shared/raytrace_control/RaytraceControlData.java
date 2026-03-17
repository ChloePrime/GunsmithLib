package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.raytrace_control;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import mod.chloeprime.gunsmithlib.common.util.TagKeyOr;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class RaytraceControlData {
    /**
     * 忽略名单，子弹会忽略此列表中的方块。
     * <p>
     * 可以是标签（以 # 开头）或者普通方块 id（不以 # 开头）
     */
    @GunpackProperty
    private String[] ignores = DEFAULT_IGNORE_LIST;

    /**
     * 阻挡名单，子弹碰到名单中匹配的方块必定会被阻挡，即使该方块同时在 {@link #ignores} 中被匹配。
     * <p>
     * 可以是标签（以 # 开头）或者普通方块 id（不以 # 开头）
     */
    @GunpackProperty
    private String[] obstructs = EMPTY_STRING_ARRAY;

    // 下面是实现，嗯

    public final List<TagKeyOr<Block>> getIgnoreList() {
        return ignoreList.get();
    }

    public final List<TagKeyOr<Block>> getObstructList() {
        return obstructList.get();
    }

    private static final String[] DEFAULT_IGNORE_LIST = {"#tacz:bullet_ignore"};
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private transient final Supplier<List<TagKeyOr<Block>>> ignoreList = TagKeyOr.compile(Registries.BLOCK, () -> ignores);
    private transient final Supplier<List<TagKeyOr<Block>>> obstructList = TagKeyOr.compile(Registries.BLOCK, () -> obstructs);

    public static Optional<RaytraceControlData> of(ItemStack gun) {
        return GunsmithLibSharedDataExtension.forGunOrAmmo(gun, GunsmithLibSharedDataExtension::getRaytraceControlData);
    }
}
