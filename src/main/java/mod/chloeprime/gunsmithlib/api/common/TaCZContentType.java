package mod.chloeprime.gunsmithlib.api.common;

import com.google.common.base.Suppliers;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Supplier;

public enum TaCZContentType {
    /**
     * Gun, guns, guns
     *
     * @see IGun
     */
    GUN {
        @Override
        public Optional<ResourceLocation> getId(ItemStack stack) {
            return stack.getItem() instanceof IGun gun
                    ? Optional.of(gun.getGunId(stack)).filter(id -> !DefaultAssets.EMPTY_GUN_ID.equals(id))
                    : Optional.empty();
        }

        @Override
        public void setId(ItemStack stack, ResourceLocation value) {
            if (stack.getItem() instanceof IGun gun) {
                gun.setGunId(stack, value);
            }
        }

        @Override
        public ItemStack createWithId(ResourceLocation id, RegistryAccess registry) {
            var fireMode = TimelessAPI.getCommonGunIndex(id)
                    .map(CommonGunIndex::getGunData)
                    .map(GunData::getFireModeSet)
                    .flatMap(set -> set.stream().findFirst())
                    .orElse(FireMode.UNKNOWN);
            return GunItemBuilder.create()
                    .setId(id)
                    .setFireMode(fireMode)
                    .forceBuild(registry);
        }
    },

    /**
     * Bullets
     *
     * @see IAmmo
     */
    AMMO {
        @Override
        public Optional<ResourceLocation> getId(ItemStack stack) {
            return stack.getItem() instanceof IAmmo ammo
                    ? Optional.of(ammo.getAmmoId(stack)).filter(id -> !DefaultAssets.EMPTY_AMMO_ID.equals(id))
                    : Optional.empty();
        }

        @Override
        public void setId(ItemStack stack, ResourceLocation value) {
            if (stack.getItem() instanceof IAmmo ammo) {
                ammo.setAmmoId(stack, value);
            }
        }

        @Override
        public ItemStack createWithId(ResourceLocation id, RegistryAccess registry) {
            return AmmoItemBuilder.create().setId(id).build();
        }
    },

    /**
     * Attachments
     *
     * @see IAttachment
     */
    ATTACHMENT {
        @Override
        public Optional<ResourceLocation> getId(ItemStack stack) {
            return stack.getItem() instanceof IAttachment attach
                    ? Optional.of(attach.getAttachmentId(stack)).filter(id -> !DefaultAssets.EMPTY_ATTACHMENT_ID.equals(id))
                    : Optional.empty();
        }

        @Override
        public void setId(ItemStack stack, ResourceLocation value) {
            if (stack.getItem() instanceof IAttachment attach) {
                attach.setAttachmentId(stack, value);
            }
        }

        @Override
        public ItemStack createWithId(ResourceLocation id, RegistryAccess registry) {
            return AttachmentItemBuilder.create().setId(id).build();
        }
    };

    /**
     * Return the content type of the given item stack.
     *
     * @param stack the given item stack.
     * @return the content type of that stack.
     */
    public static Optional<TaCZContentType> of(ItemStack stack) {
        for (var type : VALUES.get()) {
            if (type.getId(stack).isPresent()) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * Get content ID of {@code stack} within this content type
     *
     * @param stack the potential TaCZ content stack
     * @return the content id of that stack.
     */
    public abstract Optional<ResourceLocation> getId(ItemStack stack);

    /**
     * Set content ID of {@code stack} within this content type
     *
     * @param stack the potential TaCZ content stack
     * @param value the new content id of that stack.
     */
    public abstract void setId(ItemStack stack, ResourceLocation value);

    /**
     * Set an item stack of this content type with
     *
     * @param id       the content id
     * @param registry registry access, added in MC 1.21.1.
     * @return the content stack of this type with the given id.
     */
    public abstract ItemStack createWithId(ResourceLocation id, RegistryAccess registry);

    private static final Supplier<TaCZContentType[]> VALUES = Suppliers.memoize(TaCZContentType::values);
}
