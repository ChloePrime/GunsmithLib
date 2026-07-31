package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.HitParticleData;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.compat.aaap.AaaParticleProxy;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * AAA 开火粒子系统
 *
 * @since 6.4
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class ShootParticleSystem {
    @SubscribeEvent
    public static void onClientGunFire(GunFireEvent event) {
        if (event.getLogicalSide().isServer()) {
            return;
        }
        // Read shoot particle data
        var data = ClientEffekHelper.get(event.getGunItemStack(), GunsmithLibSharedDataExtension::getShootParticles);
        if (data.isEmpty()) {
            return;
        }
        // Compute offset
        var mc = Minecraft.getInstance();
        var shooter = event.getShooter();
        var player = mc.cameraEntity;
        if (player == null && shooter == null) {
            return;
        }
        var user = Objects.requireNonNullElse(shooter, player);
        var partial = mc.getPartialTick();
        var front = user.getViewVector(partial);
        Vector3f localOffset;
        if (player == shooter && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            localOffset = GunItemRendererWrapper.muzzleRenderOffset.mul(1, 1, -1, VEC_BUFFER_L);
        } else {
            localOffset = VEC_BUFFER_L.set(THIRD_PERSON_OFFSET);
        }
        var yRot = Mth.lerp(partial, user.yRotO, user.getYRot());
        var mat = GsHelper.getModelMatrix(yRot, front, MAT_BUFFER);
        var offset = mat.transform(localOffset, VEC_BUFFER_O);
        // Spawn
        for (HitParticleData entry : data) {
            var id = entry.getParticleId();
            if (id == null) {
                continue;
            }
            var mojPos = user.getEyePosition().add(offset.x(), offset.y(), offset.z());
            AaaParticleProxy.addParticleZP(user.level(), false, id, mojPos, front, 1, entry.getAaaParticleData());
        }
    }

    private static final Vector3f VEC_BUFFER_L = new Vector3f();
    private static final Vector3f VEC_BUFFER_O = new Vector3f();
    private static final Matrix3f MAT_BUFFER = new Matrix3f();

    private static final Vector3f THIRD_PERSON_OFFSET = new Vector3f(0.06F, -0.08F, 0.8F);

    private ShootParticleSystem() {
    }
}
