package mod.chloeprime.gunsmithlib.common.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RangefinderMarker extends Projectile {
    public static final EntityType<RangefinderMarker> TYPE = EntityType.Builder
            .<RangefinderMarker>of(RangefinderMarker::new, MobCategory.MISC)
            .sized(0.0625F, 0.0625F)
            .noSummon()
            .noSave()
            .clientTrackingRange(0)
            .build("rangefinder_marker");

    public RangefinderMarker(Level level) {
        this(TYPE, level);
    }

    public RangefinderMarker(EntityType<? extends RangefinderMarker> type, Level level) {
        super(type, level);
    }

    @Override
    public void baseTick() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public @Nonnull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity tracker) {
        throw new IllegalStateException("%ss should never be sent".formatted(getClass().getSimpleName()));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    public boolean canBeRiddenUnderFluidType(FluidType type, Entity rider) {
        return false;
    }

    @Override
    protected void addPassenger(Entity crasher) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    public @Nonnull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return !FMLLoader.isProduction() && super.shouldRender(x, y, z);
    }
}
