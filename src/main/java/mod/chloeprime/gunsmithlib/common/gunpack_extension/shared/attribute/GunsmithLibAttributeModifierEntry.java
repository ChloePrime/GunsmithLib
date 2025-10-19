package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.attribute;

import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.common.util.AttributeOperationConversion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * 参见 <a href="https://zh.minecraft.wiki/w/%E5%B1%9E%E6%80%A7">Minecraft Wiki 上的 Attribute</a>
 */
public class GunsmithLibAttributeModifierEntry {
    /**
     * 要作用在什么属性上。<p>
     * 必填
     */
    @GunpackProperty
    private ResourceLocation attribute;

    /**
     * 属性修饰器 id。<p>
     * 必填
     */
    @GunpackProperty
    private UUID id;

    /**
     * 属性修饰器名称，该名称可能被神化模组和一些其他的调试功能看到。<p>
     * 可选
     */
    @GunpackProperty
    private String name = "Gun / Attachment";

    /**
     * 属性修饰器的值，<p>
     * 必填
     */
    @GunpackProperty
    private double amount;

    /**
     * 属性修饰器的运算模式 <p>
     * 可选，默认为加法
     */
    @GunpackProperty
    private AttributeOperationConversion.ClassicName operation = AttributeOperationConversion.ClassicName.ADDITION;

    // 下面是代码
    private transient Pair<Holder<Attribute>, AttributeModifier> bakedResult;
    private transient int valid = 0;
    private transient final AtomicReference<ResourceLocation> id121 = new AtomicReference<>();

    public final ResourceLocation getAttributeId() {
        return attribute;
    }

    public final UUID getModifierId() {
        return id;
    }

    public final ResourceLocation getModifierIdForMC121() {
        return id121.updateAndGet(_cache -> _cache != null ? _cache : generateResourceLocationFromUUID(id));
    }

    public final String getModifierName() {
        return name;
    }

    public final double getAmount() {
        return amount;
    }

    public final AttributeModifier.Operation getOperation() {
        return AttributeOperationConversion.upgrade(operation);
    }

    public final Optional<Holder<Attribute>> getAttribute() {
        return BuiltInRegistries.ATTRIBUTE.getHolder(getAttributeId()).map(Function.identity());
    }

    public Optional<Pair<Holder<Attribute>, AttributeModifier>> getModifier() {
        if (valid == -1) {
            return Optional.empty();
        }
        if (valid == 1) {
            return Optional.of(Objects.requireNonNull(bakedResult));
        }
        bakedResult = bake();
        valid = bakedResult != null ? 1 : -1;
        return Optional.ofNullable(bakedResult);
    }

    private static ResourceLocation generateResourceLocationFromUUID(UUID uuid) {
        return GunsmithLib.loc(uuid.toString().replace('-', '_'));
    }

    private @Nullable Pair<Holder<Attribute>, AttributeModifier> bake() {
        if (id == null) {
            return null;
        }
        var attribute = getAttribute().orElse(null);
        if (attribute == null) {
            return null;
        }
        return Pair.of(attribute, new AttributeModifier(getModifierIdForMC121(), getAmount(), getOperation()));
    }
}
