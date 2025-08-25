package mod.chloeprime.gunsmithlib.common.util;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * A Percentile Based Attribute is one which always displays modifiers as percentages, even addition ones.<br>
 * This is used for attributes that would not make sense being displayed as flat additions (ex +0.05 Life Steal).
 */
@Deprecated(since = "MC1.21", forRemoval = true)
public class PercentBasedAttribute extends RangedAttribute {
    public PercentBasedAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax) {
        super(pDescriptionId, pDefaultValue, pMin, pMax);
    }
}
