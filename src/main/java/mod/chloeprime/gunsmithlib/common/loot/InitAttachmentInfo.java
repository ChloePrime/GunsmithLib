package mod.chloeprime.gunsmithlib.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.api.item.IAttachment;
import mod.chloeprime.gunsmithlib.api.common.GunLootFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class InitAttachmentInfo extends LootItemConditionalFunction {
    private final ResourceLocation attachmentId;

    public static final MapCodec<InitAttachmentInfo> CODEC = RecordCodecBuilder.mapCodec(builder -> commonFields(builder).and(
            ResourceLocation.CODEC.fieldOf("attachment_id").forGetter(func -> func.attachmentId)
    ).apply(builder, InitAttachmentInfo::new));

    public InitAttachmentInfo(
            List<LootItemCondition> conditions,
            ResourceLocation attachmentId
    ) {
        super(conditions);
        this.attachmentId = attachmentId;
    }

    public static Builder<?> initAttachmentInfo(ResourceLocation attachmentId) {
        return simpleBuilder((conditions) -> new InitAttachmentInfo(conditions, attachmentId));
    }

    public ResourceLocation getAttachmentId() {
        return attachmentId;
    }

    @Override
    public @Nonnull ItemStack run(ItemStack stack, LootContext context) {
        if (stack.getItem() instanceof IAttachment attachmentItem) {
            attachmentItem.setAttachmentId(stack, this.attachmentId);
        }
        return stack;
    }

    @Override
    public @Nonnull LootItemFunctionType<InitAttachmentInfo> getType() {
        return GunLootFunctions.INIT_ATTACHMENT_INFO.get();
    }
}
