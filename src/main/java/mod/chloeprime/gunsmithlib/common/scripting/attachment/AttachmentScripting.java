package mod.chloeprime.gunsmithlib.common.scripting.attachment;

import com.google.common.collect.ImmutableList;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.attachment.AttachmentScriptingAPI;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApiStatus.Internal
public final class AttachmentScripting {
    public static final Marker MARKER = MarkerFactory.getMarker("GunsmithLib Attachment Scripting");

    public static final List<AttachmentType> SCRIPT_ORDER = injectModdedAttachmentTypesToAttachmentOrders(
            AttachmentType.EXTENDED_MAG,
            AttachmentType.MUZZLE,
            AttachmentType.LASER,
            AttachmentType.GRIP,
            AttachmentType.SCOPE,
            AttachmentType.STOCK
    );

    public static <T> T modifyProperty(
            T original, IGun gunInterface,
            ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter,
            String gunLuaMethodName, String id, Class<T> type
    ) {
        var gunApi = new ModernKineticGunScriptAPI();
        gunApi.setItemStack(gunItem);
        gunApi.setShooter(shooter);
        gunApi.setDataHolder(dataHolder);

        var gunIndex = gunApi.getGunIndex();
        if (gunIndex == null) {
            return original;
        }

        var luaMethodName = "gunsmithlib_attachment_" + gunLuaMethodName;
        var value = new MutableObject<>(original);
        for (var attachmentType : AttachmentScripting.SCRIPT_ORDER) {
            var attachmentInfo = Optional.of(gunInterface.getAttachment(gunItem, attachmentType))
                    .filter(attach -> !attach.isEmpty())
                    .flatMap(AttachmentInfo::of)
                    .orElse(null);
            if (attachmentInfo == null) {
                continue;
            }
            try {
                GunsmithLibSharedDataExtension.forAttachment(attachmentInfo).ifPresent(data -> {
                    var attachApi = new AttachmentScriptingAPI(gunApi, attachmentType, attachmentInfo, data.getScriptParams());
                    data.getScript()
                            .map(script -> GsHelper.checkFunction(script.get(luaMethodName)))
                            .map(func -> func.call(CoerceJavaToLua.coerce(attachApi), LuaValue.valueOf(id), CoerceJavaToLua.coerce(value.getValue())))
                            .map(luaValue -> type.cast(CoerceLuaToJava.coerce(luaValue, type)))
                            .ifPresent(value::setValue);
                });
            } catch (Exception exception) {
                GunsmithLib.LOGGER.warn(MARKER, "Attachment {} failed to modify gun property {}", attachmentInfo.attachmentId(), id, exception);
                return value.getValue();
            }
        }

        return value.getValue();
    }

    @SuppressWarnings("SameParameterValue")
    private static
    List<AttachmentType> injectModdedAttachmentTypesToAttachmentOrders(AttachmentType... hardcoded) {
        var hardcodeSet = Set.of(hardcoded);
        var builder = ImmutableList.<AttachmentType>builder();
        builder.addAll(Arrays.asList(hardcoded));
        for (var type : AttachmentType.values()) {
            if (hardcodeSet.contains(type)) {
                continue;
            }
            builder.add(type);
        }
        return builder.build();
    }

    private AttachmentScripting() {
    }
}
