package mod.chloeprime.gunsmithlib.client.gunpack_extension.screen_shake;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.screen_shake.ScreenShakeEntryData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import rbasamoyai.ritchiesprojectilelib.effects.screen_shake.ModScreenShakeHandler;
import rbasamoyai.ritchiesprojectilelib.effects.screen_shake.ScreenShakeEffect;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @since 7.0.0
 */
public class ScreenShakeHandler extends ModScreenShakeHandler.Impl {
    @FunctionalInterface
    public interface MoveFunc {
        void move(double dx, double dy, double dz);
    }

    private @Nullable ScreenShakeEntryData data;

    public void addEffect(ScreenShakeEffect libEffect, double trX, double trY, double trZ) {
        addEffect(libEffect);
        immediatelyAddTranslateEffect(trX, trY, trZ);
    }

    public void applyTranslateEffects(MoveFunc moveFunc) {
        var partialTicks = Minecraft.getInstance().getPartialTick();
        var currentDisp = translateDisplacement
                .add(translateVelocity.mul(partialTicks, tmpBuffer1), varBuffer1)
                .add(translateAcceleration.mul(partialTicks * partialTicks * 0.5, tmpBuffer2), varBuffer1);
        moveFunc.move(currentDisp.x(), currentDisp.y(), currentDisp.z());
    }

    public void bindData(ScreenShakeEntryData data) {
        this.data = data;
    }

    public double getIntensity() {
        return Optional.ofNullable(data).map(ScreenShakeEntryData::getIntensity).orElse(1.0);
    }

    @Override
    protected void applyConstraints() {
        super.applyConstraints();
        var maxRotation = 45 * getIntensity();
        if (Math.abs(this.displacement.x) > maxRotation) {
            this.displacement = new Vec3(Math.copySign(maxRotation, this.displacement.x), this.displacement.y, this.displacement.z);
            this.velocity = this.velocity.multiply(0, 1, 1);
        }
        if (Math.abs(this.displacement.y) > maxRotation) {
            this.displacement = new Vec3(this.displacement.x, Math.copySign(maxRotation, this.displacement.y), this.displacement.z);
            this.velocity = this.velocity.multiply(1, 0, 1);
        }
        if (Math.abs(this.displacement.y) > maxRotation) {
            this.displacement = new Vec3(this.displacement.x, this.displacement.y, Math.copySign(maxRotation, this.displacement.z));
            this.velocity = this.velocity.multiply(1, 1, 0);
        }
    }

    protected void immediatelyAddTranslateEffect(double xp, double yp, double zp) {
        if (Math.abs(xp) + Math.abs(yp) + Math.abs(zp) <= 3e-6) {
            return;
        }
        translateVelocity.x += xp * this.random.nextDouble() - this.random.nextDouble() * 0.5;
        translateVelocity.y += yp * this.random.nextDouble() - this.random.nextDouble() * 0.5;
        translateVelocity.z += zp * this.random.nextDouble() - this.random.nextDouble() * 0.5;
    }

    @Override
    public void tick(Minecraft minecraft) {
        super.tick(minecraft);

        int iterations = 10;
        var dt = 1.0 / iterations;
        var restitution = Math.max(0.005, this.getRestitution());
        var drag = Math.max(0.005, this.getDrag());

        for (int i = 0; i < iterations; ++i) {
            var newRotationDisplacement = translateDisplacement
                    .add(translateVelocity.mul(dt, tmpBuffer1), varBuffer1)
                    .add(translateAcceleration.mul(0.5 * dt * dt, tmpBuffer2), varBuffer1);
            var newAccel = translateDisplacement
                    .mul(-restitution, varBuffer2)
                    .add(translateVelocity.mul(-drag, tmpBuffer1), varBuffer2);
            var newVel = translateVelocity
                    .add(translateAcceleration.add(newAccel, tmpBuffer1).mul(0.5 * dt, tmpBuffer1), varBuffer3);
            translateDisplacement.set(newRotationDisplacement);
            translateVelocity.set(newVel);
            translateAcceleration.set(newAccel);
            applyTranslateConstraints();
        }
        if (translateDisplacement.lengthSquared() < 1e-4
                && translateVelocity.lengthSquared() < 1e-4
                && translateAcceleration.lengthSquared() < 1e-4) {
            clearTranslateEffects();
        }
    }

    protected void clearTranslateEffects() {
        translateDisplacement.zero();
        translateVelocity.zero();
        translateAcceleration.zero();
    }

    protected void applyTranslateConstraints() {
        var maxOffset = 1.8 * getIntensity();
        var displacement = this.translateDisplacement;
        if (Math.abs(displacement.x) > maxOffset) {
            displacement.set(Math.copySign(maxOffset, displacement.x), displacement.y, displacement.z);
            this.translateVelocity.mul(0, 1, 1);
        }
        if (Math.abs(this.translateDisplacement.y) > maxOffset) {
            displacement.set(displacement.x, Math.copySign(maxOffset, displacement.y), displacement.z);
            this.translateVelocity.mul(1, 0, 1);
        }
        if (Math.abs(this.translateDisplacement.y) > maxOffset) {
            displacement.set(displacement.x, displacement.y, Math.copySign(maxOffset, displacement.z));
            this.translateVelocity.mul(1, 1, 0);
        }
    }

    @Override
    protected double getRestitution() {
        return Optional.ofNullable(data).map(ScreenShakeEntryData::getRestitution).orElseGet(super::getRestitution);
    }

    @Override
    protected double getDrag() {
        return Optional.ofNullable(data).map(ScreenShakeEntryData::getDrag).orElseGet(super::getDrag);
    }

    private final Vector3d translateDisplacement = new Vector3d();
    private final Vector3d translateVelocity = new Vector3d();
    private final Vector3d translateAcceleration = new Vector3d();
    private final Vector3d tmpBuffer1 = new Vector3d();
    private final Vector3d tmpBuffer2 = new Vector3d();
    private final Vector3d varBuffer1 = new Vector3d();
    private final Vector3d varBuffer2 = new Vector3d();
    private final Vector3d varBuffer3 = new Vector3d();
}
