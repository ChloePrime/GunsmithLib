## 双机共享新增常量：

| 常量名                         | 说明                       | 详细说明                           | 添加版本   |
|-----------------------------|--------------------------|--------------------------------|--------|
| `GUNSMITHLIB_INSTALLED`     | 如果安装本模组则为`true`，否则为`nil` | 可用于检测是否安装本模组，避免脚本在没有安装本模组的时候报错 | 3.7.0* |
| `GUNSMITHLIB_MAJOR_VERSION` | GunsmithLib的版本号的第一位      |                                | 3.7.0* |
| `GUNSMITHLIB_MINOR_VERSION` | GunsmithLib的版本号的第二位      |                                | 3.7.0* |
| `GUNSMITHLIB_PATCH_VERSION` | GunsmithLib的版本号的第三位      |                                | 3.7.0* |

*4.4.0前仅在客户端状态机中可用

## 状态机新增常量：

| 常量名                                       | 说明                | 详细说明                                     | 添加版本   |
|-------------------------------------------|-------------------|------------------------------------------|--------|
| `GUNSMITHLIB_INPUT_COOLDOWN_START`        | 破盾（开始冷却）时触发的状态名   | 值为 `"gunsmithlib:cooldown_start"`        | 3.7.0  |
| `GUNSMITHLIB_INPUT_SHIELD_BLOCKS_DAMAGE`  | 枪盾挡住非子弹伤害时触发      | 值为 `"gunsmithlib:shield_blocks_damage"`  | 4.11.0 |
| `GUNSMITHLIB_INPUT_SHIELD_BLOCKS_BULLET`  | 枪盾挡住子弹时触发         | 值为 `"gunsmithlib:shield_blocks_bullet"`  | 4.11.0 |
| `GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED` | 切换武器部位时触发         | 值为 `"gunsmithlib:current_part_switched"` | 4.12.0 |
| `GUNSMITHLIB_INPUT_VARIANT_SWITCHED`      | 切换武器变体且不需要重新装弹时触发 | 值为 `"gunsmithlib:variant_switched"`      | 4.12.0 |

## 双机共享扩展 API

| 函数名                                  | 说明                  | 详细说明                              | 添加版本   |
|--------------------------------------|---------------------|-----------------------------------|--------|
| `gunsmith_getCooldownSeconds()`      | 获取当前物品的冷却时间         | 单位为秒                              | 3.7.0  |
| `gunsmith_getCooldownPercent()`      | 当前物品的冷却时间百分比        | 0 为冷却完毕可以使用，1 为刚开始冷却              | 3.7.0  |
| `gunsmith_getEstimatedRange()`       | 获取当前武器预估的射程         | 会考虑穿透，穿透数量使用武器改装后的穿透等级            | 4.4.0  |
| `gunsmith_getEstimatedRange(number)` | 获取当前武器在指定穿透数量下预估的射程 | 假设武器绝对精准，即不考虑扩散。且起始点为玩家摄像机中心而不是枪口 | 4.4.0  |
| `gunsmith_getGunId()`                | 获取当前武器的枪械 id        |                                   | 4.12.0 |

示例：

```lua
local function runInspectAnimation(context)
    if (GUNSMITHLIB_INSTALLED ~= nil) then
        print(context:gunsmith_getCooldownPercent())
    end
end
```