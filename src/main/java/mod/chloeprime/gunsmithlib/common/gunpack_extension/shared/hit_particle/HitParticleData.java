package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import mod.chloeprime.gunsmithlib.common.util.LuaFormula;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 命中粒子。
 * 可以写在枪械 data 和子弹 data 中。
 *
 * @since 5.2.0
 */
public class HitParticleData {
    /**
     * 粒子效果的 id，支持需要额外参数的粒子 id。
     * 请参阅 <a href=https://zh.minecraft.wiki/w/%E5%91%BD%E4%BB%A4/particle?variant=zh-cn#%E7%B2%92%E5%AD%90%E6%89%80%E6%8E%A5%E6%94%B6%E7%9A%84%E5%8F%82%E6%95%B0>Minecraft Wiki 上的 Particle 命令</a>（无视所有基岩版相关的部分你）
     */
    @GunpackProperty
    private String particle_id = "minecraft:explosion";

    /**
     * 粒子效果的 delta<p>
     * 详情请参阅 <a href=https://zh.minecraft.wiki/w/%E5%91%BD%E4%BB%A4/particle?variant=zh-cn#%E5%8F%82%E6%95%B0>Minecraft Wiki 上的 Particle 命令</a>（无视所有基岩版相关的部分你）
     */
    @GunpackProperty
    private double dx, dy, dz;

    /**
     * 粒子效果的 speed<p>
     * 详情请参阅 <a href=https://zh.minecraft.wiki/w/%E5%91%BD%E4%BB%A4/particle?variant=zh-cn#%E5%8F%82%E6%95%B0>Minecraft Wiki 上的 Particle 命令</a>（无视所有基岩版相关的部分你）
     */
    @GunpackProperty
    private String speed = String.valueOf(0.0);

    /**
     * 粒子效果的 count<p>
     * 详情请参阅 <a href=https://zh.minecraft.wik/w/%E5%91%BD%E4%BB%A4/particle?variant=zh-cn#%E5%8F%82%E6%95%B0>Minecraft Wiki 上的 Particle 命令</a>（无视所有基岩版相关的部分你）
     */
    @GunpackProperty
    private String count = String.valueOf(0.0);

    /**
     * 如果为 true，则这个命中粒子将拥有很远的渲染距离。
     * 且如果这个子弹会产生爆炸，则隐藏爆炸的粒子效果。
     */
    @GunpackProperty
    private boolean explosive_particle_alternate;

    /**
     * 如果为 {@code true}，则命中方块时将使用命中的方块的破坏粒子，
     * 且命中方块时 {@link #particle_id} 将会失效。
     * <p>
     * 如果为 {@code false}，则命中方块时将不播放这个粒子。
     * <p>
     * 如果留空，则无论是否命中方块都播放配置中指定的粒子。
     */
    @GunpackProperty
    private @Nullable Boolean is_adaptive_block_particle = null;

    /**
     * 为 true 时，粒子 id 将代表 AAA Particles 的粒子 id，dx, dy, dz, speed 和 count 会失效。
     * 为 false 时，使用原版粒子，且如果安装了 AAA Particles 模组，则这个条目的原版粒子不会生成。
     * 不填写时，使用原版粒子，且无论是否安装 AAA Particles 都会生成粒子。
     */
    @GunpackProperty
    private @Nullable Boolean is_aaa_particle = null;

    /**
     * AAA 粒子的附加数据。
     */
    @GunpackProperty
    private @Nullable AAAParticleData aaa_particle_data;

    public @Nullable ResourceLocation getParticleId() {
        return ResourceLocation.tryParse(particle_id);
    }

    public @Nullable ParticleOptions getParticle(HolderLookup<ParticleType<?>> registry) {
        if (particle == null && !error) {
            try {
                particle = ParticleArgument.readParticle(new StringReader(particle_id), registry);
            } catch (CommandSyntaxException ex) {
                GunsmithLib.LOGGER.warn("Failed to decode particle type string {}", particle_id, ex);
                error = true;
                return null;
            }
        }
        return particle;
    }

    public double getDX() {
        return dx;
    }

    public double getDY() {
        return dy;
    }

    public double getDZ() {
        return dz;
    }

    public double getSpeed() {
        return speedFormula.eval();
    }

    public int getCount() {
        return (int) (countFormula.eval() + 1e-12);
    }

    public boolean isExplosiveParticleAlternate() {
        return explosive_particle_alternate;
    }

    public @Nullable Boolean isAdaptiveBlockParticle() {
        return is_adaptive_block_particle;
    }

    public @Nullable Boolean isAaaParticle() {
        return is_aaa_particle;
    }

    public @Nullable AAAParticleData getAaaParticleData() {
        return aaa_particle_data;
    }

    // 下面是代码

    /**
     * 获取这把武器上实际生效的 particle data。
     * 枪上配置的优先，其次使用子弹配置上的。
     *
     * @param gun 武器物品
     * @return 这把武器上实际生效的 particle data。
     */
    public static List<HitParticleData> of(ItemStack gun) {
        var onGun = Gunsmith.getGunInfo(gun)
                .flatMap(GunsmithLibSharedDataExtension::forGun)
                .map(GunsmithLibSharedDataExtension::getHitParticles)
                .orElse(List.of());
        if (!onGun.isEmpty()) {
            return onGun;
        }
        var onAmmo = Gunsmith.getAmmoInfo(gun)
                .flatMap(GunsmithLibSharedDataExtension::forAmmo)
                .map(GunsmithLibSharedDataExtension::getHitParticles)
                .orElse(List.of());
        if (!onAmmo.isEmpty()) {
            return onAmmo;
        }
        return List.of();
    }

    private final LuaFormula speedFormula = new LuaFormula(() -> speed);
    private final LuaFormula countFormula = new LuaFormula(() -> count);
    private ParticleOptions particle;
    private boolean error;
}
