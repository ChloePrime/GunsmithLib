package mod.chloeprime.gunsmithlib.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.api.TimelessAPI;
import mod.chloeprime.gunsmithlib.api.common.GunLootFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class IsAttachmentInstalledInDatabase implements LootItemCondition {
    private final ResourceLocation attachmentId;

    public static final MapCodec<IsAttachmentInstalledInDatabase> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("attachment_id").forGetter(cond -> cond.attachmentId)
    ).apply(builder, IsAttachmentInstalledInDatabase::new));

    public IsAttachmentInstalledInDatabase(ResourceLocation attachmentId) {
        this.attachmentId = attachmentId;
    }

    public static IsAttachmentInstalledInDatabase.Builder isAttachmentInstalledInDatabase(ResourceLocation attachmentId) {
        return new Builder().attachmentId(attachmentId);
    }

    public static class Builder implements LootItemCondition.Builder {
        private ResourceLocation attachmentId;

        public Builder attachmentId(ResourceLocation attachmentId) {
            this.attachmentId = attachmentId;
            return this;
        }

        @Nonnull
        public IsAttachmentInstalledInDatabase build() {
            return new IsAttachmentInstalledInDatabase(this.attachmentId);
        }
    }

    public ResourceLocation getAttachmentId() {
        return this.attachmentId;
    }

    @Override
    public boolean test(LootContext context) {
        return TimelessAPI.getCommonAttachmentIndex(this.attachmentId).isPresent();
    }

    @Override
    public @Nonnull LootItemConditionType getType() {
        return GunLootFunctions.IS_ATTACHMENT_INSTALLED_IN_DATABASE.get();
    }
}
