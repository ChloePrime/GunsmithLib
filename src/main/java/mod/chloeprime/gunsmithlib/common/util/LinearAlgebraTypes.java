package mod.chloeprime.gunsmithlib.common.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class LinearAlgebraTypes {
    public static Vector3d moj2joml(Vec3 mojVec) {
        return new Vector3d(mojVec.x(), mojVec.y(), mojVec.z());
    }

    public static Vec3 joml2moj(Vector3d vec) {
        return new Vec3(vec.x(), vec.y(), vec.z());
    }
}
