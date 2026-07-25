package mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content;

import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.LightGetterExtension;
import mod.chloeprime.gunsmithlib.common.util.LightType;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

/**
 * @since 6.3
 */
public interface BaseLightGetterExtensionImpl extends LightGetterExtension {
    @Override
    default int get_level_light_i(Vector3i pos) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(pos.x(), pos.y(), pos.z()), LightType.REALTIME);
    }

    @Override
    default int get_level_light_i(int x, int y, int z) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(x, y, z), LightType.REALTIME);
    }

    @Override
    default int get_level_light_d(Vector3d pos) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(pos.x(), pos.y(), pos.z()), LightType.REALTIME);
    }

    @Override
    default int get_level_light_d(double x, double y, double z) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(x, y, z), LightType.REALTIME);
    }


    @Override
    default int get_block_light_i(Vector3i pos) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(pos.x(), pos.y(), pos.z()), LightType.BLOCK);
    }

    @Override
    default int get_block_light_i(int x, int y, int z) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(x, y, z), LightType.BLOCK);
    }

    @Override
    default int get_block_light_d(Vector3d pos) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(pos.x(), pos.y(), pos.z()), LightType.BLOCK);
    }

    @Override
    default int get_block_light_d(double x, double y, double z) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(x, y, z), LightType.BLOCK);
    }


    @Override
    default int get_sky_light_i(Vector3i pos) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(pos.x(), pos.y(), pos.z()), LightType.SKY);
    }

    @Override
    default int get_sky_light_i(int x, int y, int z) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(x, y, z), LightType.SKY);
    }

    @Override
    default int get_sky_light_d(Vector3d pos) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(pos.x(), pos.y(), pos.z()), LightType.SKY);
    }

    @Override
    default int get_sky_light_d(double x, double y, double z) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(x, y, z), LightType.SKY);
    }


    @Override
    default int get_realtime_sky_light_i(Vector3i pos) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(pos.x(), pos.y(), pos.z()), LightType.REALTIME_SKY);
    }

    @Override
    default int get_realtime_sky_light_i(int x, int y, int z) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(x, y, z), LightType.REALTIME_SKY);
    }

    @Override
    default int get_realtime_sky_light_d(Vector3d pos) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(pos.x(), pos.y(), pos.z()), LightType.REALTIME_SKY);
    }

    @Override
    default int get_realtime_sky_light_d(double x, double y, double z) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(x, y, z), LightType.REALTIME_SKY);
    }


    @Override
    default int get_theoretical_light_i(Vector3i pos) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(pos.x(), pos.y(), pos.z()), LightType.THEORETICAL);
    }

    @Override
    default int get_theoretical_light_i(int x, int y, int z) {
        return gunsmithlib$internal$uniGetLight(new BlockPos(x, y, z), LightType.THEORETICAL);
    }

    @Override
    default int get_theoretical_light_d(Vector3d pos) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(pos.x(), pos.y(), pos.z()), LightType.THEORETICAL);
    }

    @Override
    default int get_theoretical_light_d(double x, double y, double z) {
        return gunsmithlib$internal$uniGetLight(BlockPos.containing(x, y, z), LightType.THEORETICAL);
    }


    @ApiStatus.Internal
    int gunsmithlib$internal$uniGetLight(BlockPos pos, @Nonnull LightType layer);
}
