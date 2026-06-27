package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.BaseShooterStatesImpl;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.EntityStatesImpl;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.ServerShooterStatesImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Optional;

public record AmmoHitEntityEventLua(
        EntityHurtByGunEvent impl,
        boolean is_writeable
) {
    public AmmoHitEntityEventLua(EntityHurtByGunEvent event) {
        this(event, event instanceof EntityHurtByGunEvent.Pre);
    }

    /**
     * 获得子弹状态。
     *
     * @return 子弹状态
     */
    public EntityStates get_bullet() {
        return new EntityStatesImpl(get_bullet_entity());
    }

    /**
     * 获得子弹实体对象。
     * 这是一个面向高级用户的 API。获取实体状态请使用 {@link #get_bullet()}
     *
     * @return 子弹实体对象
     */
    @ApiStatus.Experimental
    public Entity get_bullet_entity() {
        return impl.getBullet();
    }

    /**
     * 获取这次命中的基础伤害。
     *
     * @return 这次命中的基础伤害
     */
    public float get_base_damage() {
        return impl.getBaseAmount();
    }

    /**
     * 获取这次命中的爆头倍率。
     *
     * @return 这次命中的爆头倍率
     */
    public float get_headshot_multiplier() {
        return impl.getHeadshotMultiplier();
    }

    /**
     * 获取这次命中的枪械 display 的 id。
     *
     * @return 这次命中的枪械 display 的 id
     */
    public ResourceLocation get_gun_display_id() {
        return impl.getGunDisplayId();
    }

    /**
     * 获得射手状态。
     *
     * @return 射手状态
     */
    public @Nullable ShooterStates get_shooter() {
        return Optional.ofNullable(get_shooter_entity())
                .map(shooter -> shooter.level().isClientSide()
                        ? new BaseShooterStatesImpl(shooter)
                        : new ServerShooterStatesImpl(shooter))
                .orElse(null);
    }

    /**
     * 获得射手实体对象。
     * 这是一个面向高级用户的 API。获取射手状态请使用 {@link #get_bullet()}
     *
     * @return 射手实体对象
     */
    public @Nullable LivingEntity get_shooter_entity() {
        return impl.getAttacker();
    }

    /**
     * 获取这次命中包含爆头倍率的基础伤害。
     *
     * @return 这次命中包含爆头倍率的基础伤害
     */
    public float get_total_damage() {
        return impl.getBaseAmount() * (impl.isHeadShot() ? impl.getHeadshotMultiplier() : 1);
    }

    /**
     * 获得射手状态。
     *
     * @return 射手状态
     */
    public @Nullable EntityStates get_victim() {
        return Optional.ofNullable(get_victim_entity())
                .map(hitTarget -> {
                    if (hitTarget instanceof LivingEntity victim) {
                        return victim.level().isClientSide()
                                ? new BaseShooterStatesImpl(victim)
                                : new ServerShooterStatesImpl(victim);
                    } else {
                        return new EntityStatesImpl(hitTarget);
                    }
                })
                .orElse(null);
    }

    /**
     * 获得射手实体对象。
     * 这是一个面向高级用户的 API。获取射手状态请使用 {@link #get_bullet()}
     *
     * @return 射手实体对象
     */
    public @Nullable Entity get_victim_entity() {
        return impl.getHurtEntity();
    }

    /**
     * 获取这次命中是否爆头。
     *
     * @return 这次命中是否爆头
     */
    public boolean is_headshot() {
        return impl.isHeadShot();
    }


    // 修改事件的方法，仅适用于 gunsmithlib_ammo_pre_hit_entity 等 pre 事件。


    /**
     * 设置这次命中的基础伤害。
     *
     * @param amount 新的基础伤害
     */
    public void set_base_damage(float amount) {
        ensureWriteAccess().setBaseAmount(amount);
    }

    /**
     * 设置这次命中的爆头倍率。
     *
     * @param amount 新的爆头倍率
     */
    public void set_headshot_multiplier(float amount) {
        ensureWriteAccess().setHeadshotMultiplier(amount);
    }

    /**
     * 设置这次命中是否爆头。
     *
     * @param isHeadshot 如果为 {@code true}，则这次命中视作爆头。
     */
    public void set_is_headshot(float isHeadshot) {
        ensureWriteAccess().setBaseAmount(isHeadshot);
    }


    private EntityHurtByGunEvent.Pre ensureWriteAccess() {
        if (!is_writeable() || !(impl instanceof EntityHurtByGunEvent.Pre event)) {
            throw new UnsupportedOperationException("This event is not writeable");
        }
        return event;
    }
}
