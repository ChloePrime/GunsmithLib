package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content;

import com.google.common.primitives.Floats;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.ClientShootStates;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.BaseShooterStatesImpl;
import mod.chloeprime.gunsmithlib.mixin.client.GunAnimationStateContextAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * @since 6.0.0
 */
public class ClientShooterStatesImpl extends BaseShooterStatesImpl implements ClientShootStates {

    private final GunAnimationStateContext context;

    public ClientShooterStatesImpl(LivingEntity shooter, GunAnimationStateContext context) {
        super(shooter);
        this.context = context;
    }

    @Override
    public String get_scope_type() {
        var accessor = (GunAnimationStateContextAccessor) context;
        var gunItem = accessor.getCurrentGunItem();
        var gunInterface = accessor.getIGun();
        var index = TimelessAPI
                .getClientAttachmentIndex(gunInterface.getAttachmentId(gunItem, AttachmentType.SCOPE))
                .orElse(null);
        if (index == null) {
            return SCOPE_TYPE_IRON_ZOOM;
        }
        // 对于类型单一的瞄具（即没有 sight 和 scope 混合的组合瞄具），
        // 直接根据 index 判断瞄具类型。
        var typeByIndex = getScopeTypeFromIndex(index).orElse(null);
        if (typeByIndex != null) {
            return typeByIndex;
        }
        // 对于 sight 和 scope 混合的组合瞄具，
        // 判断方式为：如果选中了倍率最低的一档，则判定为使用 sight，否则判定为使用 scope
        return Gunsmith
                .getAttachmentInfo(gunInterface.getAttachment(gunItem, AttachmentType.SCOPE))
                .flatMap(info -> getScopeTypeFromItem(info, index))
                .orElse(SCOPE_TYPE_IRON_ZOOM);
    }

    private static Optional<String> getScopeTypeFromIndex(ClientAttachmentIndex index) {
        if (index.isSight() && index.isScope()) {
            return Optional.empty();
        }
        if (index.isSight()) {
            return Optional.of(SCOPE_TYPE_SIGHT);
        }
        if (index.isScope()) {
            return Optional.of(SCOPE_TYPE_SCOPE);
        }
        return Optional.of(SCOPE_TYPE_IRON_ZOOM);
    }

    private static Optional<String> getScopeTypeFromItem(AttachmentInfo attachment, ClientAttachmentIndex display) {
        var zooms = display.getZoom();
        if (zooms == null || zooms.length == 0) {
            return Optional.of(SCOPE_TYPE_IRON_ZOOM);
        }
        var zoomIndex = attachment.attachmentItem().getZoomNumber(attachment.attachmentStack()) % zooms.length;
        var curZoom = zooms[zoomIndex];
        var minZoom = Floats.min(zooms);
        return curZoom == minZoom
                ? Optional.of(SCOPE_TYPE_SIGHT)
                : Optional.of(SCOPE_TYPE_SCOPE);
    }
}
