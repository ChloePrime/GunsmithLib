## 双机共享新增常量：

| 常量名                         | 说明                       | 详细说明                           | 添加版本   |
|-----------------------------|--------------------------|--------------------------------|--------|
| `GUNSMITHLIB_INSTALLED`     | 如果安装本模组则为`true`，否则为`nil` | 可用于检测是否安装本模组，避免脚本在没有安装本模组的时候报错 | 3.7.0* |
| `GUNSMITHLIB_MAJOR_VERSION` | GunsmithLib的版本号的第一位      |                                | 3.7.0* |
| `GUNSMITHLIB_MINOR_VERSION` | GunsmithLib的版本号的第二位      |                                | 3.7.0* |
| `GUNSMITHLIB_PATCH_VERSION` | GunsmithLib的版本号的第三位      |                                | 3.7.0* |

*4.4.0前仅在客户端状态机中可用

## 双机共享扩展 API

| 函数名                                  | 函数名（v2，参数同 v1，故在此省略）                       | 说明                            | 详细说明                                                                                 | 添加版本   |
|--------------------------------------|--------------------------------------------|-------------------------------|--------------------------------------------------------------------------------------|--------|
| 不支持                                  | `ternary_op(cond, val_t, val_f)`           | 三元操作符                         | 相当于其他语言中的 `cond ? val_t : val_f`                                                     | 6.0.0  |
| 不支持                                  | `shooter_states()`                         | 获取射手状态                        | 参见 [shooter_states.md](api_classes/shooter_states.md)                                | 6.0.0  |
| 不支持                                  | `synced_data()`                            | 获取同步数据接口                      | 双端均可读，服务端中可写，详情参见 [synced_data.md](api_classes/synced_data.md)                       | 6.0.0  |
| `gunsmith_getCooldownPercent()`      | `get_cooldown_percent()`                   | 当前物品的冷却时间百分比                  | 0 为冷却完毕可以使用，1 为刚开始冷却                                                                 | 3.7.0  |
| `gunsmith_getCooldownPercent()`      | `get_cooldown_percent()`                   | 当前物品的冷却时间百分比                  | 0 为冷却完毕可以使用，1 为刚开始冷却                                                                 | 3.7.0  |
| `gunsmith_getEstimatedRange()`       | `get_estimated_range()`                    | 获取当前武器预估的射程                   | 会考虑穿透，穿透数量使用武器改装后的穿透等级                                                               | 4.4.0  |
| `gunsmith_getEstimatedRange(number)` | `get_estimated_range()`                    | 获取当前武器在指定穿透数量下预估的射程           | 假设武器绝对精准，即不考虑扩散。且起始点为玩家摄像机中心而不是枪口                                                    | 4.4.0  |
| `gunsmith_getGunId()`                | `get_gun_id()`                             | 获取当前武器的枪械 id                  |                                                                                      | 4.12.0 |
| ~~`gunsmith_getChargingTime()`~~     | ~~`get_charge_time()`~~                    | 已弃用                           |                                                                                      | 4.13.0 |
| 不支持                                  | `get_entity_by_uid(string)`                | 根据指定的 uuid 获取实体               | 使用字符串形式的 uuid                                                                        | 6.2.0  |
| 不支持                                  | `get_entity_by_uuid(java.util.UUID)`       | 根据指定的 uuid 获取实体               | 使用 uuid 对象                                                                           | 6.2.0  |
| 不支持                                  | `get_entity_state_by_uid(string)`          | 根据指定的 uuid 获取实体状态             | 使用字符串形式的 uuid                                                                        | 6.2.0  |
| 不支持                                  | `get_entity_state_by_uuid(java.util.UUID)` | 根据指定的 uuid 获取实体状态             | 使用 uuid 对象                                                                           | 6.2.0  |
| 不支持                                  | `search()`                                 | 使用枪械自带的火控系统搜索目标               | 返回值详情见 [target_search_results.md](api_classes/target_search_results.md)              | 6.2.0  |
| 不支持                                  | `search(number, number)`                   | 参数 1 为最大搜索距离，参数 2 为最大搜索角度（半径） | 角度单位为弧度。<br/>返回值详情见 [target_search_results.md](api_classes/target_search_results.md) | 6.2.0  |

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

#### 能量武器 API V2（双机共享部分）

| V2 函数名                                     | 说明                       | 详细说明                  | 添加版本  |
|--------------------------------------------|--------------------------|-----------------------|-------|
| `get_energy_stored()` 或 `energy_stored()`  | 获取武器内存储的电量               |                       | 6.2.0 |
| `get_configured_battery_capacity()`        | 获取 data 文件中设置的**电池容量上限** |                       | 6.2.0 |
| `get_configured_max_energy_input_speed()`  | 获取 data 文件中设置的**输入速度上限** | 不受 modify property 影响 | 6.2.0 |
| `get_configured_max_energy_output_speed()` | 获取 data 文件中设置的**输出速度上限** | 不受 modify property 影响 | 6.2.0 |

#### 光照获取 API（双机共享）

- 返回值类型均为 `number` (int)。

术语表：

| 术语       | 含义                                    |
|----------|---------------------------------------|
| `实时光照`   | 某个位置实际的光照，**受**昼夜循环影响。被原版用于判断某个位置能否刷怪 |
| `方块光照`   | 该位置由光源方块产生的光照                         |
| `天空光照`   | 该位置受天空照明影响的程度，**不受**昼夜循环影响            |
| `实时天空光照` | 该位置受天空照明产生的亮度，**受**昼夜循环影响             |
| `理论光照`   | 该位置在 F3 界面中显示的亮度，**不受**昼夜循环影响         |

方法列表：

| V2 函数名                                                                                                              | 说明                       | 详细说明                        | 添加版本  |
|---------------------------------------------------------------------------------------------------------------------|--------------------------|-----------------------------|-------|
| `get_level_light_i`([org.joml.Vector3i](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3i.html))        | 获取某个**整数**位置的**实时**光照    | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_level_light_i`(number(int), number(int), number(int))                                                          | 获取某个**整数**位置的**实时**光照    | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_level_light_d`([org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html))        | 获取某个**浮点数**位置的**实时**光照   | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_level_light_d`(number(double), number(double), number(double))                                                 | 获取某个**浮点数**位置的**实时**光照   | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_block_light_i`([org.joml.Vector3i](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3i.html))        | 获取某个**整数**位置的**方块**光照    | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_block_light_i`(number(int), number(int), number(int))                                                          | 获取某个**整数**位置的**方块**光照    | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_block_light_d`([org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html))        | 获取某个**浮点数**位置的**方块**光照   | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_block_light_d`(number(double), number(double), number(double))                                                 | 获取某个**浮点数**位置的**方块**光照   | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_sky_light_i`([org.joml.Vector3i](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3i.html))          | 获取某个**整数**位置的**天空**光照    | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_sky_light_i`(number(int), number(int), number(int))                                                            | 获取某个**整数**位置的**天空**光照    | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_sky_light_d`([org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html))          | 获取某个**浮点数**位置的**天空**光照   | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_sky_light_d`(number(double), number(double), number(double))                                                   | 获取某个**浮点数**位置的**天空**光照   | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_realtime_sky_light_i`([org.joml.Vector3i](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3i.html)) | 获取某个**整数**位置的**实时天空**光照  | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_realtime_sky_light_i`(number(int), number(int), number(int))                                                   | 获取某个**整数**位置的**实时天空**光照  | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_realtime_sky_light_d`([org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html)) | 获取某个**浮点数**位置的**实时天空**光照 | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_realtime_sky_light_d`(number(double), number(double), number(double))                                          | 获取某个**浮点数**位置的**实时天空**光照 | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_theoretical_light_i`([org.joml.Vector3i](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3i.html))  | 获取某个**整数**位置的**理论**光照    | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_theoretical_light_i`(number(int), number(int), number(int))                                                    | 获取某个**整数**位置的**理论**光照    | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_theoretical_light_d`([org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html))  | 获取某个**浮点数**位置的**理论**光照   | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |
| `get_theoretical_light_d`(number(double), number(double), number(double))                                           | 获取某个**浮点数**位置的**理论**光照   | 值域为 `0-15`，如果该位置未加载则返回 `-1` | 6.3.0 |

## 逻辑机（服务端）扩展 API

| 函数名（v1）                                             | 函数名（v2，参数同 v1，故在此省略）                   | 说明                                          | 详细说明                                         | 添加版本  |
|-----------------------------------------------------|----------------------------------------|---------------------------------------------|----------------------------------------------|-------|
| 不支持                                                 | `get_shooter_position()`               | 获取射手脚底的坐标                                   |                                              | 5.6.0 |
| 不支持                                                 | `get_muzzle_position()`                | 获取射手枪口的坐标                                   | 估算的坐标，不准确                                    | 5.6.0 |
| 不支持                                                 | `get_front_vector()`                   | 获取射手的视线方向                                   | 返回值必定是单位向量                                   | 4.4.0 |
| `gunsmith_playOverheatSound()`                      | `play_overheat_sound()`                | 播放充电武器过热时内置的过热特效                            |                                              | 3.3.0 |
| `gunsmith_triggerAnimationStateTransition(string)`n | `trigger_animation_state_transition()` | 触发客户端状态转移（transition）                       |                                              | 5.4.0 |
| `gunsmith_addEffect(table)`                         | `add_potion_effect()`                  | 为射手添加药水效果                                   | table 的结构类似 data 中药水效果部分                     | 5.6.0 |
| `gunsmith_addEffectTo(LivingEntity, table)`         | `add_potion_effect_to()`               | 为指定实体添加药水效果                                 | table 的结构类似 data 中药水效果部分                     | 5.6.0 |
| `gunsmith_spawnParticle(Vector3d, table[])`         | `spawn_particle()`                     | 在指定位置播放粒子效果                                 | table 的结构类似 data 中命中粒子部分                     | 5.6.0 |
| 不支持                                                 | `get_root_gun_id()`                    | 获取发射器的 id                                   | 对于**子母弹**的情况，获取母弹射物的发射器的 id                  | 5.9.0 |
| 不支持                                                 | `get_root_gun_api()`                   | 在指定位置播放粒子效果                                 | 对于**子母弹**的情况，获取母弹射物的发射器的 api 对象              | 5.9.0 |
| 不支持                                                 | `unload_all_ammo()`                    | 卸载所有弹药并向射手返回卸下来的弹药                          | 包括枪膛内的弹药                                     | 6.1.0 |
| 不支持                                                 | `unload_all_attachments()`             | 卸载所有配件并向射手返回卸下来的配件                          | 可用于次抛武器抛弃前卸下配件                               | 6.1.0 |
| 不支持                                                 | `discard_weapon()`                     | 抛弃该武器，将武器的堆叠大小减少 1，通常情况下会导致武器消失并触发 put away | 建议调用前调用 `#unload_all_attachments` 以保留玩家安装的配件 | 6.1.0 |

#### 能量武器 API V2（逻辑机（服务端））

| V2 函数名                                       | 说明                 | 详细说明                                                        | 添加版本  |
|----------------------------------------------|--------------------|-------------------------------------------------------------|-------|
| `set_energy_stored(number)`                  | 设置电池内存储的电量         |                                                             | 6.2.0 |
| `privileged_extract_energy(number)`          | 从电池中抽取电量，返回实际抽取的电量 | 忽视输出速度上限，适合用于枪械机制导致的耗能                                      | 6.2.0 |
| `privileged_extract_energy(number, boolean)` | 从电池中抽取电量，返回实际抽取的电量 | 忽视输出速度上限，适合用于枪械机制导致的耗能。<br/>第二个参数传入 true 则只返回能抽取的电量，但是不消耗电量 | 6.2.0 |
| `privileged_receive_energy(number)`          | 向电池中存入电量，返回实际存入的电量 | 忽视输入速度上限，适合用于枪械机制产生的能量。                                     | 6.2.0 |
| `privileged_receive_energy(number, boolean)` | 向电池中存入电量，返回实际存入的电量 | 忽视输入速度上限，适合用于枪械机制产生的能量。<br/>第二个参数传入 true 则只返回能抽取的电量，但是不消耗电量 | 6.2.0 |
| `extract_energy(number)`                     | 从电池中抽取电量，返回实际抽取的电量 |                                                             | 6.2.0 |
| `extract_energy(number, boolean)`            | 从电池中抽取电量，返回实际抽取的电量 | 第二个参数传入 true 则只返回能抽取的电量，但是不消耗电量                             | 6.2.0 |
| `receive_energy(number, boolean)`            | 向电池中存入电量，返回实际存入的电量 |                                                             | 6.2.0 |
| `receive_energy(number, boolean)`            | 向电池中存入电量，返回实际存入的电量 | 第二个参数传入 true 则只返回能存入的电量，但是不存入电量                             | 6.2.0 |

## 逻辑机（服务端）新增入口点

这些入口点和 tacz 本体的 `shoot`, `start_bolt` 等入口点一样，在特定的时机被 Java 代码调用。

| 函数名                                   | 说明    | 详细说明 | 添加版本   |
|---------------------------------------|-------|------|--------|
| ~~`gunsmithlib_begin_charging(api)`~~ | 已弃用   |      | 4.13.0 |
| `gunsmithlib_on_draw(api)`            | 拔枪时触发 |      | 6.1.0  |
| `gunsmithlib_on_put_away(api)`        | 收枪时触发 |      | 6.1.0  |

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
| 不支持     | `logic_script_params()`               | 获取枪械 data 里配置的逻辑脚本的脚本参数   |      | 6.2.0 |

## 软依赖 GunsmithLib 时安全调用扩展 API 的示例

```lua
local function runInspectAnimation(context)
    if (GUNSMITHLIB_INSTALLED ~= nil) then
        print(context:gunsmith_getCooldownPercent())
    end
end
```