package mod.chloeprime.gunsmithlib.common.scripting.ammo;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.AmmoHitEntityEventLua;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ammo.AmmoScriptingAPI;
import mod.chloeprime.gunsmithlib.api.util.AmmoInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Objects;
import java.util.Optional;

@ApiStatus.Internal
@EventBusSubscriber
public final class AmmoScripting {
    public static final Marker MARKER = MarkerFactory.getMarker("GunsmithLib Ammo Scripting");

    public static <T> T modifyProperty(
            T original,
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
        var ammoInfo = AmmoInfo.of(Gunsmith.createAmmoItemFromId(gunIndex.getGunData().getAmmoId())).orElse(null);
        if (ammoInfo == null) {
            return original;
        }

        var luaMethodName = "gunsmithlib_ammo_" + gunLuaMethodName;
        try {
            return GunsmithLibSharedDataExtension.forAmmo(ammoInfo).flatMap(data -> {
                var ammoApi = new AmmoScriptingAPI(gunApi, ammoInfo, data.getScriptParams());
                return data.getScript()
                        .map(script -> GsHelper.checkFunction(script.get(luaMethodName)))
                        .map(func -> func.call(CoerceJavaToLua.coerce(ammoApi), LuaValue.valueOf(id), CoerceJavaToLua.coerce(original)))
                        .map(luaValue -> type.cast(CoerceLuaToJava.coerce(luaValue, type)));
            }).orElse(original);
        } catch (Exception exception) {
            GunsmithLib.LOGGER.warn(MARKER, "Ammo {} failed to modify gun property {}", ammoInfo.ammoId(), id, exception);
            return original;
        }
    }

    @SubscribeEvent
    public static void onAmmoHitEntityPre(EntityHurtByGunEvent.Pre event) {
        onAmmoHitEntity(event, "gunsmithlib_ammo_pre_hit_entity");
    }

    @SubscribeEvent
    public static void onAmmoHitEntityPost(EntityHurtByGunEvent.Post event) {
        onAmmoHitEntity(event, "gunsmithlib_ammo_post_hit_entity");
    }

    @SubscribeEvent
    public static void onAmmoKilledEntity(EntityKillByGunEvent event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }

        var sources = Pair.of(event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING), event.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING));
        @SuppressWarnings("UnstableApiUsage")
        var wrapped = new EntityHurtByGunEvent.Post(
                event.getBullet(), event.getKilledEntity(), event.getAttacker(),
                event.getGunId(), event.getGunDisplayId(),
                event.getBaseDamage(), sources,
                event.isHeadShot(), event.getHeadshotMultiplier(), event.getLogicalSide()
        ) {
            @Override
            public void postEventToKubeJS(EntityHurtByGunEvent event) {
            }
        };

        onAmmoHitEntity(wrapped, "gunsmithlib_ammo_post_hit_entity");
    }

    private static void onAmmoHitEntity(EntityHurtByGunEvent event, String luaMethodName) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        var gun = Optional.ofNullable(event.getAttacker())
                .map(LivingEntity::getMainHandItem)
                .flatMap(Gunsmith::getGunInfo)
                .filter(gunInfo -> event.getGunId().equals(gunInfo.gunId()))
                .orElse(null);
        if (gun == null) {
            return;
        }
        var ammo = AmmoInfo.of(Gunsmith.createAmmoItemFromId(gun.index().getGunData().getAmmoId())).orElse(null);
        if (ammo == null) {
            return;
        }
        var data = GunsmithLibSharedDataExtension.forAmmo(ammo).orElse(null);
        if (data == null || data.getScript().isEmpty()) {
            return;
        }

        var shooter = Objects.requireNonNull(event.getAttacker());
        var gunApi = new ModernKineticGunScriptAPI();
        gunApi.setItemStack(gun.gunStack());
        gunApi.setShooter(shooter);
        gunApi.setDataHolder(IGunOperator.fromLivingEntity(shooter).getDataHolder());
        var ammoApi = new AmmoScriptingAPI(gunApi, ammo, data.getScriptParams());
        var luaEvent = new AmmoHitEntityEventLua(event);

        try {
            data.getScript()
                    .map(script -> GsHelper.checkFunction(script.get(luaMethodName)))
                    .ifPresent(func -> func.call(CoerceJavaToLua.coerce(ammoApi), CoerceJavaToLua.coerce(luaEvent)));
        } catch (Exception exception) {
            GunsmithLib.LOGGER.warn(MARKER, "Ammo {} failed to handle event {}", ammo.ammoId(), luaMethodName, exception);
        }
    }

    private AmmoScripting() {
    }
}
