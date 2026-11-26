package mod.chloeprime.gunsmithlib.mixin.common.tacz.dfu;

import com.google.gson.JsonElement;
import com.tacz.guns.resource.convert.PackConversionPlan;
import com.tacz.guns.resource.convert.UniversalPackConverter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 把 TaCZ 通用升级器里私有的材料（Ingredient）转换逻辑暴露出来，
 * 让 GunsmithLib 自己的枪包数据也能复用同一套 1.20.1 → 1.21.1 物品/标签转换规则。
 *
 * @since 7.2.0
 */
@Mixin(value = UniversalPackConverter.class, remap = false)
public interface UniversalPackConverterAccessor {
    /**
     * TaCZ 的 {@code ingredient} 转换：应用物品标签规则、把旧版 Forge 材料类型换成 1.21.1 写法，
     * 并用 {@code Ingredient.CODEC} 校验结果。
     */
    @Invoker("ingredient")
    JsonElement gunsmithlib$convertIngredient(JsonElement input, String path, PackConversionPlan plan);
}
