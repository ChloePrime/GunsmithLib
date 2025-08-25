package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

public class FireControlAttributes {
    private static final DeferredRegister<Attribute> DFR = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, GunsmithLib.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> AIM_LOCK_RANGE = DFR.register("aim_lock_range", () -> new RangedAttribute(
            "attribute.name.%s.aim_lock_range".formatted(GunsmithLib.MOD_ID), 0, 0, 1024
    ).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> AIM_LOCK_ANGLE = DFR.register("aim_lock_angle", () -> new RangedAttribute(
            "attribute.name.%s.aim_lock_angle".formatted(GunsmithLib.MOD_ID), 0, 0, 360
    ).setSyncable(true));

    @ApiStatus.Internal
    public static void init(IEventBus modbus) {
        DFR.register(modbus);
        modbus.addListener(FireControlAttributes::onCreateAttributes);
    }

    private static void onCreateAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(type -> event.add(type, AIM_LOCK_ANGLE));
    }
}
