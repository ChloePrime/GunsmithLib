### 简介

实体状态类型，包含一个实体大部分容易被 lua 处理的信息。
<br>
相比直接使用实体（诸如`api:getShooter()`）来说， 使用实体状态的好处

1. 不用和 Searge Name 斗争，不用频繁查询混淆表，代码可读性也更好。
2. 不用担心 1.21.1 后由于 NeoForge 不再混淆导致枪包不兼容。
3. 不用处理 `luajava` 库被移除导致某些非基础类型 API 无法调用问题

### 可用版本

自 6.0.0

### API

##### 实体部分（EntityStates）：

| 函数名                             | 返回值类型                                                                                                                                               | 说明                 | 详细说明                             | 添加版本  |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|--------------------|----------------------------------|-------|
| position()                      | [org.joml.Vector3d](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector3d.java)                                                 | 获取实体的位置            | 通常来说是脚底中心的位置                     | 6.0.0 |
| eye_position()                  | [org.joml.Vector3d](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector3d.java)                                                 | 获取实体眼部位置           |                                  | 6.0.0 |
| center_position()               | [org.joml.Vector3d](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector3d.java)                                                 | 获取实体碰撞箱中心的位置       |                                  | 6.0.0 |
| rotation()                      | [org.joml.Vector2f](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector2f.java)                                                 | 获取实体的旋转            | 单位为**弧度**。`x` 为 pitch，`y` 为 yaw。 | 6.0.0 |
| rotation_degrees()              | [org.joml.Vector2f](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector2f.java)                                                 | 获取实体的旋转角度          | 单位为**角度**。`x` 为 pitch，`y` 为 yaw。 | 6.0.0 |
| velocity_per_tick()             | [org.joml.Vector3d](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector3d.java)                                                 | 获取实体的速度            | 单位为 米/tick                       | 6.0.0 |
| velocity_per_second()           | [org.joml.Vector3d](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector3d.java)                                                 | 获取实体的速度            | 单位为 米/秒                          | 6.0.0 |
| get_look_direction()            | [org.joml.Vector3d](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector3d.java)                                                 | 获取实体视线前方在世界坐标系下的值  | 单位为 米/秒                          | 6.0.0 |
| get_entity_pose()               | `string`                                                                                                                                            | 获取射手的实体姿势[1]       |                                  | 6.0.0 |
| get_entity_pose_object()        | [net.minecraft.world.entity.Pose](https://mcsrc.dev/1/1.20.1/net/minecraft/world/entity/Pose)                                                       | 获取射手的实体姿势[1]       |                                  | 6.0.0 |
| get_random_generator()          | [java.util.random.RandomGenerator](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/random/RandomGenerator.java)[2] | 获取射手的随机数生成器        |                                  | 6.0.0 |
| is_alive()                      | `boolean `                                                                                                                                          | 获取实体是否活着           |                                  | 6.0.0 |
| is_removed()                    | `boolean `                                                                                                                                          | 获取实体是否被移除          |                                  | 6.0.0 |
| exists()                        | `boolean `                                                                                                                                          | 获取实体是否存在           | 对死亡过程中的实体返回 `true`               | 6.0.0 |
| is_moving()                     | `boolean `                                                                                                                                          | 获取射手是否正在地面上移动      |                                  | 6.0.0 |
| is_sprinting()                  | `boolean `                                                                                                                                          | 获取射手是否正在疾跑         |                                  | 6.0.0 |
| is_crouching()                  | `boolean `                                                                                                                                          | 获取射手是否正在蹲下         |                                  | 6.0.0 |
| is_crawling()                   | `boolean `                                                                                                                                          | 获取射手是否正在趴下         |                                  | 6.0.0 |
| is_swimming()                   | `boolean `                                                                                                                                          | 获取射手是否正在游泳         |                                  | 6.0.0 |
| is_on_ground()                  | `boolean `                                                                                                                                          | 获取实体是否站在地上         |                                  | 6.0.0 |
| is_silent()                     | `boolean `                                                                                                                                          | 获取实体是否被设置了静音       |                                  | 6.0.0 |
| is_affected_by_gravity()        | `boolean `                                                                                                                                          | 获取实体是否受重力影响        |                                  | 6.0.0 |
| is_fire_immune()                | `boolean `                                                                                                                                          | 获取实体是否免疫火焰         |                                  | 6.0.0 |
| is_in_water()                   | `boolean `                                                                                                                                          | 获取实体是否在水方块中        |                                  | 6.0.0 |
| is_in_rain()                    | `boolean `                                                                                                                                          | 获取实体是否淋雨           |                                  | 6.0.0 |
| is_in_bubble()                  | `boolean `                                                                                                                                          | 获取实体是否在气泡柱中        |                                  | 6.0.0 |
| is_in_water_or_rain()           | `boolean `                                                                                                                                          | 获取实体是否在水中或淋雨       |                                  | 6.0.0 |
| is_in_water_or_bubble()         | `boolean `                                                                                                                                          | 获取实体是否在水中或在气泡柱中    |                                  | 6.0.0 |
| is_in_water_or_rain_or_bubble() | `boolean `                                                                                                                                          | 获取实体是否在水中，淋雨或在气泡柱中 |                                  | 6.0.0 |
| is_under_water()                | `boolean `                                                                                                                                          | 获取实体是否完全被水淹没       |                                  | 6.0.0 |
| is_in_lava()                    | `boolean `                                                                                                                                          | 获取实体是否接触熔岩         |                                  | 6.0.0 |
| is_on_fire()                    | `boolean `                                                                                                                                          | 获取射手是否着火           |                                  | 6.0.0 |
| remaining_fire_ticks()          | `number` (long)                                                                                                                                     | 获取射手着火的剩余时间        | 单位为刻                             | 6.0.0 |
| remaining_fire_time_seconds()   | `number` (double)                                                                                                                                   | 获取射手着火的剩余时间        | 单位为秒                             | 6.0.0 |

##### 活体部分（ShooterStates）：

| 函数名                              | 返回值类型                                                                                               | 说明                                                                         | 详细说明                             | 添加版本  |
|----------------------------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|----------------------------------|-------|
| get_head_rotation()              | [org.joml.Vector2f](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector2f.java) | 获取射手头部的旋转                                                                  | 单位为**角度**。`x` 为 pitch，`y` 为 yaw。 | 6.0.0 |
| get_body_rotation()              | [org.joml.Vector2f](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector2f.java) | 获取射手身体的旋转                                                                  | 单位为**角度**。`x` 为 pitch，`y` 为 yaw。 | 6.0.0 |
| get_attribute_value(string)      | `number` (double)                                                                                   | 获取一个实体的指定 [Attribute](https://zh.minecraft.wiki/w/%E5%B1%9E%E6%80%A7) 的值   | 传入的参数为 attribute 的注册 id          | 6.0.0 |
| get_attribute_base_value(string) | `number` (double)                                                                                   | 获取一个实体的指定 [Attribute](https://zh.minecraft.wiki/w/%E5%B1%9E%E6%80%A7) 的基础值 | 传入的参数为 attribute 的注册 id          | 6.0.0 |
| get_potion_effect(string)        | [PotionEffectInstanceView]()                                                                        | 获取一个实体的给定药水效果实例[3]                                                         | 传入的参数为药水效果（Mob Effect）的注册 id     | 6.0.0 |
| get_health()                     | `number` (float)                                                                                    | 获取射手当前的生命值                                                                 |                                  | 6.0.0 |
| get_max_health()                 | `number` (float)                                                                                    | 获取射手的最大生命值                                                                 |                                  | 6.0.0 |
| get_armor()                      | `number` (double)                                                                                   | 获取射手的护甲值                                                                   |                                  | 6.0.0 |
| get_armor_toughness()            | `number` (double)                                                                                   | 获取射手的盔甲韧性                                                                  |                                  | 6.0.0 |
| get_movement_speed()             | `number` (double)                                                                                   | 获取射手的移速属性                                                                  |                                  | 6.0.0 |
| get_scale()                      | `number` (float)                                                                                    | 获取射手的缩放比例                                                                  | 在原版只用于缩放幼年形态的实体，但是可能被各种 mod 修改。  | 6.0.0 |
| get_elytra_flying_ticks()        | `number` (int)                                                                                      | 获取射手当次鞘翅飞行的时间                                                              | 单位为刻                             | 6.0.0 |
| get_elytra_flying_time_seconds() | `number` (double)                                                                                   | 获取射手当次鞘翅飞行的时间                                                              | 单位为秒                             | 6.0.0 |
| *get_movement_input()            | [org.joml.Vector3d](https://github.com/JOML-CI/JOML/blob/main/src/main/java/org/joml/Vector3d.java) | 获取实体的移动输入                                                                  | 警告：对服务端玩家无效                      | 6.0.0 |
| is_baby()                        | `boolean`                                                                                           | 判断射手是否是幼年状态                                                                |                                  | 6.0.0 |
| is_elytra_flying()               | `boolean`                                                                                           | 获取射手当前是否正在鞘翅飞行                                                             |                                  | 6.0.0 |
| is_on_climbable()                | `boolean`                                                                                           | 获取射手当前是否正爬在可攀爬物上                                                           |                                  | 6.0.0 |

##### 射手部分（ShooterStates）：

| 函数名                       | 返回值类型            | 说明              | 详细说明                         | 添加版本  |
|---------------------------|------------------|-----------------|------------------------------|-------|
| is_aiming()               | `boolean`        | 获取用户是否有效地按下了瞄准键 |                              | 6.0.0 |
| is_bolting()              | `boolean`        | 获取射手是否在拉大栓      |                              | 6.0.0 |
| aiming_progress()         | `number` (float) | 获取瞄准进度          | 范围 0-1                       | 6.0.0 |
| reload_state()            | `string`         | 获取射手的换弹状态[4]    |                              | 6.0.0 |
| reload_countdown_millis() | `number` (long)  | 获取射手的换弹冷却       | 单位为毫秒                        | 6.0.0 |
| sprint_time()             | `number` (float) | 获取玩家持枪奔跑的时长     | 单位为秒，上限为枪械数据中设置的 sprintTime。 | 6.0.0 |
| shoot_cooldown_millis()   | `number` (long)  | 获取射手的射击冷却       | 单位为毫秒                        | 6.0.0 |
| melee_cooldown_millis()   | `number` (long)  | 获取射手的近战冷却       | 单位为毫秒                        | 6.0.0 |
| draw_cooldown_millis()    | `number` (long)  | 获取射手的拔枪冷却       | 单位为毫秒                        | 6.0.0 |

### 说明：

- 标 * 的为试验性方法，使用方式可能与你想象的大相径庭

[1] pose 的可能取值为

- `STANDING`
- `FALL_FLYING`
- `SLEEPING`
- `SWIMMING`
- `SPIN_ATTACK`
- `CROUCHING`
- `LONG_JUMPING`
- `DYING`
- `CROAKING`
- `USING_TONGUE`
- `SITTING`
- `ROARING`
- `SNIFFING`
- `EMERGING`
- `DIGGING`

[2] java.util.random.RandomGenerator
的大部分用法和经典的 [java.util.Random](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/Random.java)
是一样的

[3] PotionEffectInstanceView API

| 函数名                | 返回值类型          | 说明                    | 详细说明                        | 添加版本  |
|--------------------|----------------|-----------------------|-----------------------------|-------|
| duration_ticks()   | `number` (int) | 获取药水效果实例的剩余持续时间       | 单位为刻                        | 6.0.0 |
| duration_seconds() | `number` (int) | 获取药水效果实例的剩余持续时间       | 单位为秒                        | 6.0.0 |
| amplifier()        | `number` (int) | 获取药水效果等级加成            | 值等于药水等级 - 1                 | 6.0.0 |
| level()            | `number` (int) | 获取药水效果等级              |                             | 6.0.0 |
| is_ambient()       | `boolean`      | 获取药水效果实例是否为环境效果       | 环境效果在原版由信标给予。环境效果的粒子透明度会变淡。 | 6.0.0 |
| is_visible()       | `boolean`      | 获取药水效果实例是否显示粒子        |                             | 6.0.0 |
| shows_icon()       | `boolean`      | 获取药水效果实例是否在玩家 HUD 中显示 |                             | 6.0.0 |

[4] 换弹状态的可能取值为

- `NOT_RELOADING`
- `EMPTY_RELOAD_FEEDING`
- `EMPTY_RELOAD_FINISHING`
- `TACTICAL_RELOAD_FEEDING`
- `TACTICAL_RELOAD_FINISHING`