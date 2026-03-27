package mod.chloeprime.gunsmithlib.client.tooltip;

import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import mod.chloeprime.gunsmithlib.api.client.GunTooltipEvent;
import mod.chloeprime.gunsmithlib.api.client.RenderGunTooltipTextEvent;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.descriptial_affix.DescriptionalAffixData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy.EnergyWeaponData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.GunExplosiveData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.GunExplosiveFragData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.FireControlData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.OldFireControlData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield.ShieldData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@EventBusSubscriber(Dist.CLIENT)
public abstract class DescriptionalGunAffix {
    private static final List<DescriptionalGunAffix> ENTRIES = Collections.synchronizedList(new ArrayList<>());
    private static final int MARGIN = 4;
    private static final int LINE_HEIGHT = 10;

    public abstract boolean shouldShow(GunInfo gunInfo);
    public abstract Optional<Component> getText(GunInfo gunInfo);

    public static void register(DescriptionalGunAffix affix) {
        register(registry -> registry.add(affix));
    }

    public static void register(Consumer<List<DescriptionalGunAffix>> modifier) {
        modifier.accept(ENTRIES);
    }

    @SubscribeEvent
    public static void onTooltipHeight(GunTooltipEvent.ComputeHeight event) {
        var gun = event.getGunInfo().orElse(null);
        if (gun == null) {
            return;
        }

        var configured = DescriptionalAffixData.fromGun(gun.gunStack()).orElse(null);
        var extra = configured != null
                ? configured.getBefore().map(List::size).orElse(0) + configured.getAfter().map(List::size).orElse(0)
                : 0;

        var replace = configured == null ? null : configured.getReplace().orElse(null);
        int lines;

        if (replace != null) {
            lines = replace.size() + extra;
        } else {
            lines = extra;
            for (var entry : ENTRIES) {
                if (entry.shouldShow(gun)) {
                    lines++;
                }
            }
        }
        if (lines > 0) {
            event.pumpHeight(MARGIN + lines * LINE_HEIGHT);
        }
    }

    @SubscribeEvent
    public static void onTooltipRender(RenderGunTooltipTextEvent.AfterBaseInfo event) {
        var gun = event.getGunInfo().orElse(null);
        if (gun == null) {
            return;
        }
        var isFirst = new AtomicBoolean(true);
        var configured = DescriptionalAffixData.fromGun(gun.gunStack());
        configured.flatMap(DescriptionalAffixData::getBefore).ifPresent(before -> renderLines(before, isFirst, event));

        var replace = configured.flatMap(DescriptionalAffixData::getReplace).orElse(null);
        if (replace != null) {
            renderLines(replace, isFirst, event);
        } else {
            for (var entry : ENTRIES) {
                if (entry.shouldShow(gun)) {
                    if (isFirst.getAndSet(false)) {
                        event.pumpHeight(MARGIN);
                    }
                    var text = entry.getText(gun).orElse(Component.literal("?"));
                    event.enqueue(ctx -> render(ctx, text, true));
                }
            }
        }

        configured.flatMap(DescriptionalAffixData::getAfter).ifPresent(before -> renderLines(before, isFirst, event));
    }

    private static void renderLines(List<String> lines, AtomicBoolean first, RenderGunTooltipTextEvent event) {
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            if (first.getAndSet(false)) {
                event.pumpHeight(MARGIN);
            }
            var hasPrefix = I18n.get(line).startsWith("§|");
            var text = Component.translatable(line).withStyle(ChatFormatting.WHITE);
            event.enqueue(ctx -> render(ctx, text, !hasPrefix));
        }
    }

    private static int render(RenderGunTooltipTextEvent.RenderContext ctx, Component text, boolean addPrefix) {
        var realText = addPrefix ? withPrefix(text) : text;
        ctx.font().drawInBatch(realText, ctx.x(), ctx.y(), 0xffffff, false, ctx.matrix(), ctx.buffer(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
        return LINE_HEIGHT;
    }

    @ApiStatus.Internal
    public static void init() {
        register(new Explosivity());
        register(new ProgrammableExplosivity());
        register(new ChildBullet());
        register(new Debuff());
        register(new SmartAmmo());
        register(new Chargeable());
        register(new Heat());
        register(new Shield());
    }

    private static Component withPrefix(Component content) {
        return Component.literal("")
                .append(Component.translatable("gunsmithlib.affix.prefix").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .append(content);
    }

    public static abstract class DescriptionalGunAffixBase extends DescriptionalGunAffix {
        public final Component text;

        public DescriptionalGunAffixBase(Component text) {
            this.text = text;
        }

        public Component getTextImpl(GunInfo gunInfo) {
            return text;
        }

        @Override
        public Optional<Component> getText(GunInfo gunInfo) {
            return shouldShow(gunInfo) ? Optional.of(getTextImpl(gunInfo)) : Optional.empty();
        }
    }

    public static class Explosivity extends DescriptionalGunAffixBase {
        public Explosivity() {
            super(Component.translatable("gunsmithlib.affix.explosive").withStyle(ChatFormatting.WHITE));
        }

        @Override
        public boolean shouldShow(GunInfo gunInfo) {
            return Optional.ofNullable(gunInfo.index().getBulletData().getExplosionData())
                    .filter(ExplosionData::isExplode)
                    .isPresent();
        }
    }

    public static class ProgrammableExplosivity extends DescriptionalGunAffixBase {
        private static final DescriptionalGunAffix BASE_EXPLOSIVE = new Explosivity();

        public ProgrammableExplosivity() {
            super(Component.translatable("gunsmithlib.affix.explosive_prgrammable").withStyle(ChatFormatting.WHITE));
        }

        @Override
        public boolean shouldShow(GunInfo gunInfo) {
            if (!BASE_EXPLOSIVE.shouldShow(gunInfo)) {
                return false;
            }
            return GunExplosiveData
                    .fromGun(gunInfo)
                    .filter(ProgrammableExplosivity::isEnabled)
                    .isPresent();
        }

        private static boolean isEnabled(GunExplosiveData data) {
            return data.getProximityFuseDistance() > 0
                    || data.getAirburstRangefinderMaxDistance().isPresent()
                    || !data.getAirburstDistances().isEmpty();
        }
    }

    public static class ChildBullet extends DescriptionalGunAffixBase {
        private static final Component FRAG = Component.translatable("gunsmithlib.affix.frag").withStyle(ChatFormatting.WHITE);
        private static final Component CLUSTER = Component.translatable("gunsmithlib.affix.cluster").withStyle(ChatFormatting.WHITE);

        public ChildBullet() {
            super(FRAG);
        }

        @Override
        public boolean shouldShow(GunInfo gunInfo) {
            return GunExplosiveFragData.of(gunInfo).isPresent();
        }

        @Override
        public Optional<Component> getText(GunInfo gunInfo) {
            return isClusterBomb(gunInfo) ? Optional.of(CLUSTER) : super.getText(gunInfo);
        }

        private boolean isClusterBomb(GunInfo gunInfo) {
            return GunExplosiveFragData.of(gunInfo)
                    .flatMap(GunExplosiveFragData::getConfigSource)
                    .flatMap(Gunsmith::getGunInfo)
                    .map(gi -> gi.index().getBulletData().getExplosionData())
                    .filter(ExplosionData::isExplode)
                    .isPresent();
        }
    }

    public static class Debuff extends DescriptionalGunAffixBase {
        public Debuff() {
            super(Component.translatable("gunsmithlib.affix.debuff").withStyle(ChatFormatting.WHITE));
        }

        @Override
        public boolean shouldShow(GunInfo gunInfo) {
            return GunsmithLibSharedDataExtension
                    .forGunOrAmmo(gunInfo, GunsmithLibSharedDataExtension::getPotionEffects)
                    .filter(effects -> !effects.isEmpty() && effects.stream()
                            .map(PotionEffectData::getEffect)
                            .flatMap(Optional::stream)
                            .anyMatch(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL))
                    .isPresent();
        }
    }

    public static class SmartAmmo extends DescriptionalGunAffixBase {
        private static final double WEAK_ANGLE_THRESHOLD = 12;
        private static final Component WEAK_ANGLE_TEXT = Component.translatable("gunsmithlib.affix.aim_assist").withStyle(ChatFormatting.WHITE);

        public SmartAmmo() {
            super(Component.translatable("gunsmithlib.affix.fire_control").withStyle(ChatFormatting.WHITE));
        }

        @Override
        public boolean shouldShow(GunInfo gunInfo) {
            var oldConfig = oldFireControlData(gunInfo)
                    .map(fcd -> fcd.aimConeAngle() > 0)
                    .isPresent();
            var newConfig = FireControlData
                    .fromGun(gunInfo)
                    .filter(fcd -> fcd.getAngularRange() > 0)
                    .isPresent();
            return oldConfig || newConfig;
        }

        @Override
        public Component getTextImpl(GunInfo gunInfo) {
            @SuppressWarnings("deprecation")
            var oldConfig = oldFireControlData(gunInfo)
                    .map(OldFireControlData::aimConeAngle)
                    .orElse(0F);
            var newConfig = FireControlData.fromGun(gunInfo)
                    .map(FireControlData::getAngularRange)
                    .orElse(0.0);
            return Math.max(oldConfig, newConfig) < WEAK_ANGLE_THRESHOLD
                    ? WEAK_ANGLE_TEXT
                    : super.getTextImpl(gunInfo);
        }

        @SuppressWarnings("deprecation")
        private static Optional<OldFireControlData> oldFireControlData(GunInfo gunInfo) {
            return ((EnhancedGunData) gunInfo.index().getGunData()).gunsmith$getOldFireControlSystemData();
        }
    }

    public static class Chargeable extends DescriptionalGunAffixBase {
        public Chargeable() {
            super(Component.translatable("gunsmithlib.affix.chargeable").withStyle(ChatFormatting.WHITE));
        }

        @Override
        public boolean shouldShow(GunInfo gunInfo) {
            return EnergyWeaponData.runtime(gunInfo).isPresent();
        }
    }

    public static class Heat extends DescriptionalGunAffixBase {
        public Heat() {
            super(Component.translatable("gunsmithlib.affix.heat").withStyle(ChatFormatting.WHITE));
        }

        @Override
        public boolean shouldShow(GunInfo gunInfo) {
            var heatBarHidden = GunsmithLibGunDisplayExtension.of(gunInfo.gunStack())
                    .filter(GunsmithLibGunDisplayExtension::hideHeatBarOverlay)
                    .isPresent();
            if (heatBarHidden) {
                return false;
            }
            return gunInfo.index().getGunData().hasHeatData();
        }
    }

    public static class Shield extends DescriptionalGunAffixBase {
        public Shield() {
            super(Component.translatable("gunsmithlib.affix.shield").withStyle(ChatFormatting.WHITE));
        }

        @Override
        public boolean shouldShow(GunInfo gunInfo) {
            return ShieldData.fromGun(gunInfo)
                    .filter(shield -> shield.blockVanillaDamageAngle() > 0 || shield.blockBulletDamageAngle() > 0)
                    .isPresent();
        }
    }
}
