package mod.chloeprime.gunsmithlib.mixin.client.minecraft;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(I18n.class)
public interface I18nAccessor {
    @Accessor static Language getLanguage() {
        throw new AbstractMethodError();
    }
}
