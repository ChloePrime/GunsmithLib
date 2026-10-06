package mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content;

import cn.chloeprime.commons.math.LinearAlgebraTypes;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import com.tacz.guns.util.ExplodeUtil;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.BulletEntityAPI;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.FragSystem;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.GunExplosiveFragData;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.mixin.common.tacz.EntityKineticBulletAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.luaj.vm2.LuaTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class BulletEntityImpl extends EntityStatesImpl implements BulletEntityAPI {
    private final EntityKineticBullet bullet;
    private final EntityKineticBulletAccessor accessor;

    public BulletEntityImpl(EntityKineticBullet bullet) {
        super(bullet);
        this.bullet = bullet;
        this.accessor = (EntityKineticBulletAccessor) bullet;
    }

    @Override
    public ControlPanel control_panel() {
        if (bullet.level().isClientSide()) {
            throw new UnsupportedOperationException("Bullet's control panel is not available at client side");
        }
        return new ControlPanelImpl();
    }

    @Override
    public Vector3d get_context_position() {
        return LinearAlgebraTypes.moj2joml(getContextPosition());
    }

    public class ControlPanelImpl implements ControlPanel {
        @Override
        public void create_fragments() {
            createFragments0(getContextPosition(), null, true);
        }

        @Override
        public void create_fragments(@Nonnull Vector3d pos) {
            Objects.requireNonNull(pos);

            createFragments0(LinearAlgebraTypes.joml2moj(pos), null, true);
        }

        @Override
        public void create_fragments(@Nonnull Vector3d pos, @Nonnull LuaTable config, boolean triggers_script_modification) {
            Objects.requireNonNull(pos);
            Objects.requireNonNull(config);

            createFragments0(LinearAlgebraTypes.joml2moj(pos), GsHelper.lua2obj(config, GunExplosiveFragData.class), triggers_script_modification);
        }

        private void createFragments0(@Nonnull Vec3 center, @Nullable GunExplosiveFragData data, boolean triggersScriptModification) {
            var bulletVelocity = bullet.getDeltaMovement();
            var bulletAaDirection = Direction.getNearest(bulletVelocity.x(), bulletVelocity.y(), bulletVelocity.z());
            var fakedHit = BlockHitResult.miss(center, bulletAaDirection, BlockPos.containing(center));
            FragSystem.createFragile(bullet, fakedHit, data, triggersScriptModification);
        }

        @Override
        public void discard() {
            bullet.discard();
        }

        @Override
        public void explode() {
            explode0(getContextPosition(), null, true);
        }

        @Override
        public void explode(@Nonnull Vector3d pos) {
            Objects.requireNonNull(pos);

            explode0(LinearAlgebraTypes.joml2moj(pos), null, true);
        }

        @Override
        public void explode(@Nonnull Vector3d pos, @Nonnull LuaTable config, boolean triggers_script_modification) {
            Objects.requireNonNull(pos);
            Objects.requireNonNull(config);

            explode0(LinearAlgebraTypes.joml2moj(pos), GsHelper.lua2obj(config, ExplosionData.class), triggers_script_modification);
        }

        @SuppressWarnings("UnstableApiUsage")
        private void explode0(Vec3 pos, @Nullable ExplosionData config, boolean triggersScriptModification) {
            if (config == null) {
                accessor.gunsmithlib$explodeAt(pos);
                return;
            }
            var owner = bullet.getOwner();
            if (!triggersScriptModification || !(owner instanceof LivingEntity shooter)) {
                ExplodeUtil.createExplosion(owner, bullet, config.getDamage(), config.getRadius(), config.isKnockback(), config.isDestroyBlock(), pos);
                return;
            }
            var gun = GsHelper.gunInfoFromBullet(bullet).orElse(null);
            if (gun == null) {
                ExplodeUtil.createExplosion(owner, bullet, config.getDamage(), config.getRadius(), config.isKnockback(), config.isDestroyBlock(), pos);
                return;
            }
            var sdh = IGunOperator.fromLivingEntity(shooter).getDataHolder();
            var damage = gun.gunItem().modifyProperty(sdh, gun.gunStack(), shooter, GunProperties.RuntimeOnly.EXPLOSION_DAMAGE, Float.class, config.getDamage());
            var radius = gun.gunItem().modifyProperty(sdh, gun.gunStack(), shooter, GunProperties.RuntimeOnly.EXPLOSION_RADIUS, Float.class, config.getRadius());
            var knockback = gun.gunItem().modifyProperty(sdh, gun.gunStack(), shooter, GunProperties.RuntimeOnly.EXPLOSION_KNOCKBACK, Boolean.class, config.isKnockback());
            var breakBlock = gun.gunItem().modifyProperty(sdh, gun.gunStack(), shooter, GunProperties.RuntimeOnly.EXPLOSION_DESTROYS_BLOCK, Boolean.class, config.isDestroyBlock());
            ExplodeUtil.createExplosion(owner, bullet, damage, radius, knockback, breakBlock, pos);
        }
    }

    private Vec3 getContextPosition() {
        return accessor.gunsmithlib$getHitPos();
    }
}
