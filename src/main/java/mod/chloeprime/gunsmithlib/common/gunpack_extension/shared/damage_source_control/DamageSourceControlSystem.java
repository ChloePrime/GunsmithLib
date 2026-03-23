package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.damage_source_control;

import cn.chloeprime.commons.rpg.DamageSources;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import mod.chloeprime.gunsmithlib.Config;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.util.TagKeyOr;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DamageSourceControlSystem {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void setMasterType(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        var shooter = event.getAttacker();
        var gun = shooter != null
                ? shooter.getMainHandItem()
                : Gunsmith.createGunItemFromId(event.getGunId());
        var data = (DamageSourceControlData) GunsmithLibSharedDataExtension
                .forGunOrAmmo(gun, GunsmithLibSharedDataExtension::getDamageSourceControlData)
                .orElse(null);
        if (data == null) {
            return;
        }
        var master = data.getMasterType().orElse(null);
        if (master != null) {
            var oldSource = event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING);
            var newSource = new DamageSource(master, oldSource.getDirectEntity(), oldSource.getEntity(), oldSource.sourcePositionRaw());
            event.setDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING, newSource);
        }
        var masterAp = data.getMasterApType().orElse(null);
        if (masterAp != null) {
            var oldSource = event.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING);
            var newSource = new DamageSource(masterAp, oldSource.getDirectEntity(), oldSource.getEntity(), oldSource.sourcePositionRaw());
            event.setDamageSource(GunDamageSourcePart.ARMOR_PIERCING, newSource);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void injectDamageSourceTags(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        var shooter = event.getAttacker();
        var gun = shooter != null
                ? shooter.getMainHandItem()
                : Gunsmith.createGunItemFromId(event.getGunId());
        var data = DamageSourceControlData.of(gun);
        if (data.isEmpty()) {
            return;
        }
        var victimIsCreativePlayer = event.getHurtEntity() instanceof Player player && player.getAbilities().instabuild;
        for (var datum : data) {
            var source = event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING);
            var sourceAp = event.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING);
            for (var is : datum.getIsList()) {
                if (is instanceof TagKeyOr.Tag<DamageType> tag) {
                    // 阻止无敌贯穿 tag 伤害创造玩家
                    if (victimIsCreativePlayer && DamageTypeTags.BYPASSES_INVULNERABILITY.equals(tag.value())) {
                        continue;
                    }
                    injectIs(source, sourceAp, tag.value());
                } else if (is instanceof TagKeyOr.Object<DamageType> obj) {
                    logUnsupported(obj.value());
                }
            }
            for (var not : datum.getIsNotList()) {
                if (not instanceof TagKeyOr.Tag<DamageType> tag) {
                    injectIsNot(source, sourceAp, tag.value());
                } else if (not instanceof TagKeyOr.Object<DamageType> obj) {
                    logUnsupported(obj.value());
                }
            }
        }
    }

    private static void injectIs(DamageSource source, DamageSource sourceAp, TagKey<DamageType> tag) {
        DamageSources.injectIs(source, tag);
        if (Config.ALTERNATIVE_ARMOR_PIERCING_FORMULA.get()) {
            return;
        }
        DamageSources.injectIs(sourceAp, tag);
    }

    private static void injectIsNot(DamageSource source, DamageSource sourceAp, TagKey<DamageType> tag) {
        DamageSources.injectIsNot(source, tag);
        if (Config.ALTERNATIVE_ARMOR_PIERCING_FORMULA.get()) {
            return;
        }
        DamageSources.injectIsNot(sourceAp, tag);
    }

    private static void logUnsupported(Holder<DamageType> holder) {
        if (holder instanceof Holder.Reference<DamageType> ref) {
            GunsmithLib.LOGGER.error("""
                            Trying to inject {} into damage source.
                            Damage type control only supports injecting tags,
                            Injecting damage type values is not supported currently.""", ref.key().location());
        } else {
            GunsmithLib.LOGGER.error("""
                            Damage type control only supports injecting tags,
                            Injecting damage type values is not supported currently.""");
        }
    }
}
