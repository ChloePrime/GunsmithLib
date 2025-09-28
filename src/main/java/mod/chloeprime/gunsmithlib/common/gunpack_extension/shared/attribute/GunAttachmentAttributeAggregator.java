package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.attribute;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.EnhancedAttachmentData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.common.util.AttributeUtil;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@EventBusSubscriber
public class GunAttachmentAttributeAggregator {
    public static ItemAttributeModifiers getGunIndexAttributeModifiers(CommonGunIndex index) {
        return ((EnhancedGunData) index.getGunData())
                .gunsmith$getGunsmithLibExtension()
                .map(GunsmithLibSharedDataExtension::getBakedAttributeModifiers)
                .orElse(ItemAttributeModifiers.EMPTY);
    }

    public static ItemAttributeModifiers getAttachmentIndexAttributeModifiers(CommonAttachmentIndex index) {
        return ((EnhancedAttachmentData) index.getData())
                .gunsmith$getGunsmithLibExtension()
                .map(GunsmithLibSharedDataExtension::getBakedAttributeModifiers)
                .orElse(ItemAttributeModifiers.EMPTY);
    }

    public static ItemAttributeModifiers getAttachmentAttributeModifiers(ItemStack stack) {
        var override = stack.getOrDefault(GunsmithLib.DataComponents.ATTACHMENT_ATTRIBUTES, ItemAttributeModifiers.EMPTY);
        if (!override.modifiers().isEmpty()) {
            return override;
        }
        var ati = Gunsmith.getAttachmentInfo(stack).orElse(null);
        return ati != null ? getAttachmentIndexAttributeModifiers(ati.index()) : ItemAttributeModifiers.EMPTY;
    }

    private static final AttachmentType[] ATTACHMENT_TYPE_REGISTRY = AttachmentType.values();
    private static final ThreadLocal<Map<Pair<Holder<Attribute>, AttributeModifier.Operation>, AttributeModifier>> MERGE_BUFFER = ThreadLocal.withInitial(HashMap::new);

    @SubscribeEvent
    public static void onGunAttribute(ItemAttributeModifierEvent event) {
        var slot = EquipmentSlotGroup.MAINHAND;
        var stack = event.getItemStack();
        var gun = Gunsmith.getGunInfo(stack).orElse(null);
        if (gun == null) {
            return;
        }
        var buffer = MERGE_BUFFER.get();
        try {
            buffer.clear();
            // 使用 AttributeModifiers 标签覆盖的情况
            if (stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers().isEmpty()) {
                getGunIndexAttributeModifiers(gun.index()).forEach(slot,  (a, am) -> putMerge(buffer, a, am));
            }
            // 叠加配件的modifiers
            ClientProxy.getRegistryAccess().ifPresent(registryAccess -> {
                for (var attachmentType : ATTACHMENT_TYPE_REGISTRY) {
                    ItemStack attachment = gun.gunItem().getAttachment(registryAccess, gun.gunStack(), attachmentType);
                    getAttachmentAttributeModifiers(attachment).forEach(slot, (a, am) -> putMerge(buffer, a, am));
                }
            });
            buffer.forEach((key, modifier) -> event.addModifier(key.getLeft(), modifier, slot));
        } finally {
            buffer.clear();
        }
    }

    private static void putMerge(
            Map<Pair<Holder<Attribute>, AttributeModifier.Operation>, AttributeModifier> buffer,
            Holder<Attribute> attribute,
            AttributeModifier modifier
    ) {
        var operation = modifier.operation();
        var key = Pair.of(attribute, operation);
        var currentModifier = buffer.get(key);
        if (currentModifier == null) {
            buffer.put(key, modifier);
            return;
        }
        var newAmount = switch (operation) {
            case ADD_VALUE, ADD_MULTIPLIED_BASE -> currentModifier.amount() + modifier.amount();
            case ADD_MULTIPLIED_TOTAL -> (1 + currentModifier.amount()) * (1 + modifier.amount()) - 1;
        };
        var newModifier = new AttributeModifier(currentModifier.id(), newAmount, operation);
        buffer.put(key, newModifier);
    }

    public static void attachmentAttributeModifierTooltip(ItemStack attachment, List<Component> tooltip, Player player, Item.TooltipContext context, TooltipFlag flag) {
        var ati = Gunsmith.getAttachmentInfo(attachment).orElse(null);
        if (ati == null) {
            return;
        }
        var modifiers = getAttachmentAttributeModifiers(attachment);
        if (modifiers.modifiers().isEmpty()) {
            return;
        }

        tooltip.add(CommonComponents.EMPTY);
        tooltip.add(Component.translatable("gunsmithlib.item.modifiers.attachment").withStyle(ChatFormatting.GRAY));
        AttributeUtil.applyTextFor(attachment, tooltip::add, getSortedModifiers(attachment, modifiers), AttributeTooltipContext.of(player, context, flag));
    }

    public static Multimap<Holder<Attribute>, AttributeModifier> getSortedModifiers(ItemStack stack, ItemAttributeModifiers modifiers) {
        var map = LinkedListMultimap.<Holder<Attribute>, AttributeModifier>create();
        modifiers.forEach(EquipmentSlotGroup.MAINHAND, (attr, modifier) -> {
            if (attr != null && modifier != null) {
                map.put(attr, modifier);
            } else {
                GunsmithLib.LOGGER.debug("Detected broken attribute modifier entry on attachment {}.  Attr={}, Modif={}", stack, attr, modifier);
            }
        });
        return map;
    }
}
