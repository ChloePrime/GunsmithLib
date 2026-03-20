## 双机共享新增常量：

| 常量名                         | 说明                       | 详细说明                           | 添加版本   |
|-----------------------------|--------------------------|--------------------------------|--------|
| `GUNSMITHLIB_INSTALLED`     | 如果安装本模组则为`true`，否则为`nil` | 可用于检测是否安装本模组，避免脚本在没有安装本模组的时候报错 | 3.7.0* |
| `GUNSMITHLIB_MAJOR_VERSION` | GunsmithLib的版本号的第一位      |                                | 3.7.0* |
| `GUNSMITHLIB_MINOR_VERSION` | GunsmithLib的版本号的第二位      |                                | 3.7.0* |
| `GUNSMITHLIB_PATCH_VERSION` | GunsmithLib的版本号的第三位      |                                | 3.7.0* |

*4.4.0前仅在客户端状态机中可用

## 双机共享扩展 API

| 函数名                                  | 函数名（v2，参数同 v1，故在此省略）     | 说明                  | 详细说明                              | 添加版本   |
|--------------------------------------|--------------------------|---------------------|-----------------------------------|--------|
| `gunsmith_getCooldownSeconds()`      | `get_cooldown_seconds()` | 获取当前物品的冷却时间         | 单位为秒                              | 3.7.0  |
| `gunsmith_getCooldownPercent()`      | `get_cooldown_percent()` | 当前物品的冷却时间百分比        | 0 为冷却完毕可以使用，1 为刚开始冷却              | 3.7.0  |
| `gunsmith_getEstimatedRange()`       | `get_estimated_range()`  | 获取当前武器预估的射程         | 会考虑穿透，穿透数量使用武器改装后的穿透等级            | 4.4.0  |
| `gunsmith_getEstimatedRange(number)` | `get_estimated_range()`  | 获取当前武器在指定穿透数量下预估的射程 | 假设武器绝对精准，即不考虑扩散。且起始点为玩家摄像机中心而不是枪口 | 4.4.0  |
| `gunsmith_getGunId()`                | `get_gun_id()`           | 获取当前武器的枪械 id        |                                   | 4.12.0 |
| `gunsmith_getChargingTime()`         | `get_charge_time()`      | 获取这次射击已经蓄力的时间       | 单位为秒。`burst` 模式下只在蓄力完成后第一次射击时有效   | 4.13.0 |

#### 异步 API
| 函数名                                                                        | 函数名（v2，参数同 v1，故在此省略） | 说明   | 详细说明  | 添加版本  |
|----------------------------------------------------------------------------|----------------------|------|-------|-------|
| `gunsmith_asyncRunDelayed(function(api, ...), number, ...)`                | `async_run_delayed`  | 延迟执行 | 基于游戏刻 | 5.4.0 |
| `gunsmith_asyncRunCycled(function(api, number, ...), number, number, ...)` | `async_run_cycled`   | 异步循环 | 基于游戏刻 | 5.4.0 |

- `gunsmith_asyncRunDelayed` 中传入的函数（第一个变量）需要有 1 个参数。参数内容为 API 实例。
- `gunsmith_asyncRunCycled` 中传入的函数（第一个变量）需要有 2 个参数。第一个参数为 API 实例，第二个参数为当前循环计数。

#### 异步 API 示例：
```lua
local function debug_print(api, i)
    print("wawa "..tostring(i))
    return true
end

function M.shoot(api)
    api:shootOnce(api:isShootingNeedConsumeAmmo())
    api:gunsmith_asyncRunDelayed(debug_print, 50)
    api:gunsmith_asyncRunCycled(debug_print, 20, 5)
end
```
开火后将在一段时间内异步输出以下内容：

| 延迟  | 输出内容       |
|-----|------------|
| 20  | `wawa 0`   |
| 40  | `wawa 1`   |
| 50  | `wawa nil` |
| 60  | `wawa 2`   |
| 80  | `wawa 3`   |
| 100 | `wawa 4`   |

## 逻辑机（服务端）扩展 API
| 函数名（v1）                                            | 函数名（v2，参数同 v1，故在此省略）                   | 说明                    | 详细说明                     | 添加版本  |
|----------------------------------------------------|----------------------------------------|-----------------------|--------------------------|-------|
| 不支持                                                | `get_shooter_position()`               | 获取射手脚底的坐标             |                          | 5.6.0 |
| 不支持                                                | `get_muzzle_position()`                | 获取射手枪口的坐标             | 估算的坐标，不准确                | 5.6.0 |
| 不支持                                                | `get_front_vector()`                   | 获取射手的视线方向             | 返回值必定是单位向量               | 4.4.0 |
| `gunsmith_playOverheatSound()`                     | `play_overheat_sound()`                | 播放充电武器过热时内置的过热特效      |                          | 3.3.0 |
| `gunsmith_triggerAnimationStateTransition(string)` | `trigger_animation_state_transition()` | 触发客户端状态转移（transition） |                          | 5.4.0 |
| `gunsmith_addEffect(table)`                        | `add_potion_effect()`                  | 为射手添加药水效果             | table 的结构类似 data 中药水效果部分 | 5.6.0 |
| `gunsmith_addEffectTo(LivingEntity, table)`        | `add_potion_effect_to()`               | 为指定实体添加药水效果           | table 的结构类似 data 中药水效果部分 | 5.6.0 |
| `gunsmith_spawnParticle(Vector3d, table[])`        | `spawn_particle()`                     | 在指定位置播放粒子效果           | table 的结构类似 data 中命中粒子部分 | 5.6.0 |

## 逻辑机（服务端）新增入口点
这些入口点和 tacz 本体的 `shoot`, `start_bolt` 等入口点一样，在特定的时机被 Java 代码调用。

| 函数名                               | 说明            | 详细说明 | 添加版本   |
|-----------------------------------|---------------|------|--------|
| `gunsmithlib_begin_charging(api)` | 当手中的武器开始蓄力时触发 |      | 4.13.0 |

## 状态机（客户端）新增常量：

| 常量名                                       | 说明                     | 详细说明                                     | 添加版本   |
|-------------------------------------------|------------------------|------------------------------------------|--------|
| `GUNSMITHLIB_INPUT_COOLDOWN_START`        | 破盾（开始冷却）时触发的状态名        | 值为 `"gunsmithlib:cooldown_start"`        | 3.7.0  |
| `GUNSMITHLIB_INPUT_SHIELD_BLOCKS_DAMAGE`  | 枪盾挡住非子弹伤害时触发           | 值为 `"gunsmithlib:shield_blocks_damage"`  | 4.11.0 |
| `GUNSMITHLIB_INPUT_SHIELD_BLOCKS_BULLET`  | 枪盾挡住子弹时触发              | 值为 `"gunsmithlib:shield_blocks_bullet"`  | 4.11.0 |
| `GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED` | 切换武器部位时触发              | 值为 `"gunsmithlib:current_part_switched"` | 4.12.0 |
| `GUNSMITHLIB_INPUT_VARIANT_SWITCHED`      | 切换武器 variant（弹种，模式）时触发 | 值为 `"gunsmithlib:variant_switched"`      | 4.12.0 |
| `GUNSMITHLIB_INPUT_BEGIN_CHARGING`        | 当手中的武器开始蓄力时触发          | 值为 `"gunsmithlib:begin_charging"`        | 4.13.0 |

## 状态机（客户端）扩展 API
| 函数名（v1） | 函数名（v2）                               | 说明                        | 详细说明 | 添加版本  |
|---------|---------------------------------------|---------------------------|------|-------|
| 不支持     | `has_previous_gun_id()`               | 检测是否处于弹种切换的过程中            |      | 5.8.0 |
| 不支持     | `get_previous_gun_id()`               | 获取弹种切换前的枪械 id             |      | 5.8.0 |
| 不支持     | `get_previous_ammo_amount()`          | 获取弹种切换前枪械弹匣内的子弹数量         |      | 5.8.0 |
| 不支持     | `get_previous_has_bullet_in_barrel()` | 获取弹种切换前枪膛内是否有子弹           |      | 5.8.0 |
| 不支持     | `get_previous_total_ammo_amount()`    | 获取弹种切换前枪械内累计子弹数量（弹匣 + 枪膛） |      | 5.8.0 |

## 软依赖 GunsmithLib 时安全调用扩展 API 的示例

```lua
local function runInspectAnimation(context)
    if (GUNSMITHLIB_INSTALLED ~= nil) then
        print(context:gunsmith_getCooldownPercent())
    end
end
```