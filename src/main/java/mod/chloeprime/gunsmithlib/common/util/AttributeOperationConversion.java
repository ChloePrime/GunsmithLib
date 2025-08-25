package mod.chloeprime.gunsmithlib.common.util;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeOperationConversion {
    public enum ClassicName {
        ADDITION(0),
        MULTIPLY_BASE(1),
        MULTIPLY_TOTAL(2);

        public final int ordinal;

        ClassicName(int ordinal) {
            this.ordinal = ordinal;
        }

    }

    static final ClassicName[] OLD_NAMES = ClassicName.values();
    static final AttributeModifier.Operation[] NEW_NAMES = AttributeModifier.Operation.values();

    public static AttributeModifier.Operation upgrade(ClassicName value1201) {
        return NEW_NAMES[value1201.ordinal];
    }

    public static ClassicName downgrade(AttributeModifier.Operation value1211) {
        return OLD_NAMES[value1211.ordinal()];
    }
}
