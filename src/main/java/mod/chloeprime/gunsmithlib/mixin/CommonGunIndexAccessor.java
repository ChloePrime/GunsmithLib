package mod.chloeprime.gunsmithlib.mixin;

import com.tacz.guns.resource.index.CommonGunIndex;
import org.luaj.vm2.LuaTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CommonGunIndex.class, remap = false)
public interface CommonGunIndexAccessor {
    @Accessor void setScript(LuaTable value);
    @Accessor void setScriptParam(LuaTable value);
}
