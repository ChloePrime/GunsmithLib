package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks;

import com.google.common.collect.MapMaker;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

public class GunBarCache {
    public record Frame(
            long ticks,
            float partialTicks
    ) {
        public static Frame ZERO = new Frame(0, 0);

        public static Frame current() {
            return new Frame(ClientProxy.getGameTime(), ClientProxy.getPartialTicks());
        }
    }

    static final Map<ItemStack, GunBarCache> CACHE = new MapMaker().weakKeys().makeMap();
    static final ThreadLocal<MutableInt> CALL_STACK = ThreadLocal.withInitial(MutableInt::new);

    public OptionalDouble getBarWidth() {
        float width = barWidth;
        return Float.isFinite(width) ? OptionalDouble.of(width) : OptionalDouble.empty();
    }

    public Optional<Color> getBarColor() {
        return Optional.ofNullable(barColor);
    }

    /**
     * 上一次计算耐久条的时间
     */
    Frame timestamp = Frame.ZERO;

    /**
     * 为 NaN 时代表不显示耐久条。
     */
    float barWidth = Float.NaN;

    /**
     * 为 null 时代表使用默认颜色。
     */
    @Nullable
    Color barColor = null;
}
