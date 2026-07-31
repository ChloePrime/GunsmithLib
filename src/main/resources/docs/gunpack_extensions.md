## 扩展功能通用格式:
在你的data json开头（大括号内）添加如下键值对：
```json5
{
  "gunsmithlib_extension": {
  },
}
```
此时你的data文件应该看起来像这样：
```json5
{
  // ......
  "gunsmithlib_extension": {
  },
  "ammo": "namespace:path",
  "ammo_amount": 666,
  "extended_mag_ammo_amount": [
    667,
    668,
    669
  ],
  "bolt": "open_bolt",
  // ......
}
```
## 开启武器充电功能:
在`gunsmithlib_extension`对象中加入如下键值对：
```json5
"battery": {
      // 每次射击消耗的电量，单位为FE
      "energy_per_shot": 200,
      // 充电功率，单位为FE/t
      "charge_power": 600,
      // 完全过热后是否需要装弹来解除过热锁定，就像地狱潜兵2的镰刀那样
      "needs_reload_on_full_heat": true
},
// 开启过热的视听反馈。开启后，过热时会播放音效并让武器冒烟。对所有能过热的武器都生效，而不仅仅是充电武器。
"enable_overheat_feedback": true
```
完整的data文件应该看起来像这样：
```json5
{
  // ......
  "gunsmithlib_extension": {
    "battery": {
      "energy_per_shot": 200,
      "charge_power": 600,
      "needs_reload_on_full_heat": true
    },
    "enable_overheat_feedback": true
  },
  "ammo": "namespace:path",
  "ammo_amount": 666,
  // ......
}
```
## 枪盾（支持枪械和配件）:
```json5
{
  // ......
  "gunsmithlib_extension": {
    "shield": {
      // 格挡原版伤害的角范围
      "block_vanilla_damage_angle": 60,
      // 格挡子弹的角范围
      "block_bullet_damage_angle": 120,
      // 格挡条件：
      // when_aiming      瞄准时能够格挡
      // when_not_aiming  没有瞄准时能够格挡
      // always           总是能够格挡，无论是否处于瞄准状态
      "condition": "when_aiming",
      // 换弹时是否停用格挡。优先级高于 condition
      "disable_shield_when_reloading": true,
      // 3.6.0版本新增
      // 如果为true，那么该武器无法被斧头破盾（默认false）
      "undisableable_by_axes": true
    }
  },
  // ......
}
```
## 火控（自瞄/制导）:
```json5
{
  "gunsmithlib_extension": {
    "fire_control": {
      // 能够锁定目标的角范围，单位为角度（°）
      "angular_range": 120,
      // （可选）指定最大锁定距离，如留空则使用武器的优势射程作为最大锁定距离。
      "range_override" : 500,
      // （可选）制导系统的扭矩，单位为°/tick。
      // 若留空，则子弹将在发射时直接指向目标。适用于高速动能子弹。
      // 填入的值代表弹头每tick将速度矢量转向目标方向的最大角度变化量。
      "torque": 6,
      // 4.8.0版本新增
      // 强扭矩，即与角度差正比的额外扭矩。
      // 例：强扭矩为0.01时，会在角度差为1°的时候提供0.01°/tick额外扭矩，在角度差为45°时提供0.45°/tick额外扭矩。
      "torque_lerp_rate": 0.05
    }
  },
}
```
注1：在填入torque（即使用制导模式）的情况下，武器的弹速不宜超过20，否则表现出来会比较奇怪。
<br><br>
注2：该系统已自带TaCZ Fire Control Extension的功能（可读取使用了该模组的制导武器的配置），那个模组可以扔掉了）

## 属性修饰器：
可以添加原版系统的attribute的修饰器，例如血上限，移动速度，护甲值等。<br>
本模组也添加了一些属性，具体可翻阅源代码或/attribute指令的补全列表。
```json5
{
  "gunsmithlib_extension": {
    // 这是一个数组，意味着每个武器/配件支持多个attribute modifier
    "attribute_modifiers": [
      {
        // 修饰器作用的属性
        "attribute": "gunsmithlib:reload_speed",
        // 修饰器id。
        // 请使用 https://www.uuidgenerator.net/ 生成独特的 uuid，
        // 你使用的 uuid 不应该与任何你看到的 uuid 相同，除非你明确知道这样做会导致什么后果。
        "id": "afc5d1a9-0c04-4d9a-bb94-5979919043f6",
        // 修饰器的名称。
        // 在原版中没有作用，有些mod可能会显示这个字段。
        // 可选
        "name": "My Amazing Attachment",
        // 修饰器的值
        "amount": "0.5",
        // 修饰器的运算模式
        // 可选，默认为 ADDITION（加算）
        "operation": "MULTIPLY_BASE"
      }
    ]
  },
}
```
另请参见：[Minecraft Wiki 上的属性系统介绍](https://zh.minecraft.wiki/w/%E5%B1%9E%E6%80%A7)

## 药水效果（支持枪械和配件）
```json5
{
  "gunsmithlib_extension": {
    // 带药水效果的爆炸弹药爆炸后生成的药水云的持续时间
    // 可留空，默认值为 600（30 秒），
    // 4.7.0新增
    area_effect_cloud_duration: 600,
    // 药水云的最小大小比例（结束时的大小/初始大小）
    // 可留空，默认值为 0（药水云持续时间结束时缩小到 0 大小）
    // 4.7.0新增
    area_effect_cloud_min_size_rate: 0.5,
    // 药水效果列表
    "potion_effects": [
      {
        // 状态效果 id。
        // 必填。
        "effect_id": "minecraft:levitation",
        // 状态效果持续时间，单位为 tick。
        // 可选。默认为 1 秒。
        "duration": 200,
        // 状态效果等级，1 = 1级，2 = 2级。
        // 可选，默认为 1
        "level": 4,
        // 如果为 true, 那么粒子效果会变淡，类似信标带来的状态效果的样子。
        // 可选，默认为 false。
        "is_ambient": false,
        // 如果为 false, 那么会隐藏这个状态的粒子效果。
        // 可选，默认为 true。
        "visible": true,
        // 如果为 false, 那么这个状态效果不会在客户端物品栏gui上显示。
        // 可选，默认为 true。
        "show_icon": true,
        // 每次命中施加状态效果的概率。
        // 可选，默认为 1。
        "chance": 0.25,
        // 状态效果的最大叠加等级，为 0 时则不叠加。
        // 可选，默认为 0。
        "max_stack_level": 6,
        // 如果为 true，则允许忽略目标的药水耐性添加药水效果
        // 5.5.0 新增
        "force": true,
        // 药水云的触发几率，爆炸生成药水云时覆盖上面配置的触发几率。
        // 可选，不填则使用上面的触发几率。
        // 5.3.0 版本新增
        //
        // 另外，5.3.0 版本以后如果没有任何药水效果检定成功时，
        // 将不再生成药水云，而不是生成一团没有任何效果的黑云。
        "area_cloud_chance": 1,
        // 药水云的药水等级，爆炸生成药水云时覆盖上面配置的药水等级。
        // 可选，不填则使用上面的药水等级。
        // 5.3.0 版本新增
        "area_cloud_level": 4
      }
    ]
  },
}
```
## 爆炸弹药扩展（只支持枪械，暂不支持配件）
添加于版本 4.9.0
```json5
{
  "gunsmithlib_extension": {
    "gun_explosive": {
      // 空爆距离挡位，
      // 格式为浮点数数组。
      "airburst_distances": [25, 50, 100],
      // 空爆距离的误差，推荐取值范围为 [0, 1]
      // 0 表示完全没有误差
      // 1 表示实际空爆距离会在 (0, 2x设定空爆距离) 之间随机
      "airburst_distances_distribution": 0.25,
      // 空爆测距仪的测距上限，单位为格。
      // 留空则表示只支持预设挡位，玩家不能自己测距。
      // 4.10.0 新增
      "airburst_rangefinder_max_distance": 200,
      // 近炸引信探测距离，单位为格*
      "proximity_fuse_distance": 1.5,
      // 防止这把武器的爆炸炸坏掉落物
      "prevent_destroying_loot_items": true,
      // 子母弹系统
      // 5.9.0 新增
      "fragments": {
        // 子弹片使用的枪械 id
        // 子母弹本质是在爆炸位置使用指定的枪械发射新的子弹，
        // 这个 id 就是发射新子弹时使用的枪械 id。
        // 填入的 id 必须是一个可以独立发射的枪械，拥有完整的 index，data，display，
        // 需要填入 index 的 id，不能填入 data 文件的 id。
        "config_id": "tacz:ak47",
        // 弹片数量
        "count": 64,
        // 弹片最小速度，单位 米/tick
        "min_frag_velocity": 1,
        // 弹片最大速度，单位 米/tick
        "max_frag_velocity": 1,
        // （可选）弹片的分布模式，默认为 "random"
        // 可选值：
        // - "random":  完全随机分布
        // - "uniform": 均匀分布，随机相位
        // 5.11.0 新增
        "distribution": "uniform"
      },
      //（可选）安全距离，默认值为 0
      // 6.2 版本新增
      "safety_distance": 6,
      //（可选）安全距离的影响范围，默认只影响近炸引信。
      // 下面这个示例中列出了当前所有可选的值。
      // 6.2 版本新增
      "safety_distance_flags": [
        // 阻止低于安全距离的空爆测距仪结果
        "prevents_airburst_rangefindinng",
        // 子弹飞行距离低于安全距离时阻止近炸引信工作
        "prevents_proximity_fuse",
        // 子弹飞行距离低于安全距离时阻止子弹爆炸和产生破片
        "prevents_explosion"
      ]
    }
  }
}
```
*警告：近炸引信系统是针对高速低寿命子弹而设计的，能确保在弹速极快（600m/s以上）时仍然能精准检测到碰炸位置。
请勿将近炸引信系统运用在低速或实际寿命非常长的子弹上，会造成严重的卡顿。

## 多弹种功能：
不在 data 文件中进行定义，请参见 [gun_variants.md](gun_variants.md)。<br>
display 文件中有少量多弹种功能的可配置项。
<br>
**6.0.0 更新：** 现已支持设定优先级与多个弹种组包含同一武器！

## 从创造标签页中隐藏
只对 TaCZ 的标签页生效，且只能写在枪械的 data 里。
```json5
{
  "gunsmithlib_extension": {
    "hidden": true
  },
}
```

# 5.2 版本新增内容
### 命中粒子：
可以放在枪械 data 和子弹 index 中。枪械上配置的粒子将覆盖弹药中配置的粒子。
```json5
{
  "gunsmithlib_extension": {
    "hit_particles": [
      {
        // 粒子效果的 id，支持需要额外元数据的粒子效果。
        "particle_id": "minecraft:crit",
        // dx dy dz，参考原版 particle 命令。
        // 注意，对于某些粒子来说，count 设置为 0 会有特殊的意义。
        // 具体请参阅 Minecraft Wiki
        "dx": 0,
        "dy": 0,
        "dz": 0,
        "speed": 0,
        "count": 0,
        // 如果为 true，则这个命中粒子将拥有很远的渲染距离。
        // 且如果这个子弹会产生爆炸，则隐藏爆炸的粒子效果。
        "explosive_particle_alternate": true,
        // 如果为 true，则命中方块时将使用命中的方块的
        // 且命中方块时 particle_id 将会失效。
        // 
        // 如果为 false，则命中方块时将不播放这个粒子
        // 
        // 如果留空，则无论是否命中方块都播放配置中指定的粒子。
        "is_adaptive_block_particle": false,
        // 为 true 时，粒子 id 将代表 AAA Particles 的粒子 id，且 dx, dy, dz, speed 和 count 会失效。
        // 为 false 时，使用原版粒子，且如果安装了 AAA Particles 模组，则这个条目的原版粒子不会生成。
        // 不填写时，使用原版粒子，且无论是否安装 AAA Particles 都会生成粒子。
        "is_aaa_particle": false,
      },
      {
        // 一个带元数据的粒子效果 id 示例。
        // 其中 minecraft:apple 为 item 粒子的元数据。
        "particle_id": "minecraft:item minecraft:apple",
        // ......
      },
      // AAA Particles (Effekseer 粒子) 配置示例
      {
        // 粒子 id
        "particle_id": "your_namespace:example",
        // 声明这个粒子是 AAA 粒子。
        // 如果此值为 true 且没有安装 AAA Particles 模组，则当前条目会被直接忽略。
        // 配合其他 is_aaa_particle 为 false 的粒子可以实现备选粒子功能。
        "is_aaa_particle": true,
        // AAA Particles 粒子的额外元数据：
        "aaa_particle_data": {
          // 粒子的缩放
          "scale": 1,
          // 传给对应 Effekseer 粒子的动态输入
          // 具体请参阅 https://effekseer.github.io/Help_Tool/en/ToolReference/dynamicParameter.html
          // "parameters": [1, 1, 1, 1],
          // 粒子生成时触发的 Effekseer 粒子触发器
          // 具体请参阅 https://effekseer.github.io/Help_Tool/en/ToolTutorial/15.html
          // "triggers": [0, 2]
        }
      }
    ]
  }
}
```

### 跳弹（Ricochet）系统
```json5
{
  "gunsmithlib_extension": {
    "ricochet": {
      // 最小入射角，单位为角度。
      // 如果命中方块时入射角小于这个数值，则不会触发跳弹。
      "min_angle_of_incidence": 30,
      // 最大跳弹次数。
      // 跳弹超过这个次数则必定爆炸。
      "max_ricochet_times": 1,
      // 第一次跳弹后对子弹重力的乘数，
      // 用于解决默认重力在跳弹后看着过大的问题。
      "gravity_scale": 0.5,
      // 最小弹力（0° 垂直入射时的弹力）
      "min_bounciness": 0.25,
      // 最大弹力（90° 入射角时的理论弹力）
      "max_bounciness": 1
    }
  }
}
```
- 实际反弹力度由枪弹配置，入射角和命中的表面材质共同决定。

### 射线检测控制
允许控制子弹能够 / 不能穿透某种方块。<br>
不能控制穿透层数！！！

```json5
{
  "gunsmithlib_extension": {
    "raytrace_control": {
      // 忽略名单，子弹会忽略此列表中的方块。
      // 可以是标签（以 # 开头）或者普通方块 id（不以 # 开头）
      // 可选，默认值为 ["#tacz:bullet_ignore"]
      "ignores": ["#tacz:bullet_ignore"],
      // 阻挡名单，子弹碰到名单中匹配的方块必定会被阻挡，即使该方块同时在 ignores 中被匹配。
      // 可以是标签（以 # 开头）或者普通方块 id（不以 # 开头）
      // 可选，默认为空
      "obstructs": []
    }
  }
}
```

# 6.2 版本新增内容
### 能量武器 V2 系统
只提供电池，具体用途由脚本决定。
<br/>
这样的话，只需要耗电的武器，需要耗电并消耗子弹的武器，以及以其他方式使用能量的武器都可以在一套系统里实现了！
<br/>
本套系统使用的电量均为 64 位长整数，非常先进.png

```json5
{
  "gunsmithlib_extension": {
    "battery_v2": {
      // 电池容量
      "capacity": 90000,
      // 最大充电速度，为 0 时将不能充电。
      // 单位：FE/t。
      "max_input_speed": 3000,
      // 最大放电速度，为 0 时
      // 单位：FE/t。
      "max_output_speed": 0,
      // 如果为 true，那么电池容量可以被 modify property 修改。
      // 注：对电池相关属性 modify property 时 api 中的 shooter 可能为假玩家。
      // 不建议开启，可能会占用非常多的服务器资源。
      "dynamic_capacity": false,
      // 如果为 true，那么充电速度可以被 modify property 修改。
      // 注：对电池相关属性 modify property 时 api 中的 shooter 可能为假玩家。
      // 不建议开启，可能会占用非常多的服务器资源。
      "dynamic_max_input_speed": false,
      // 如果为 true，那么放电速度可以被 modify property 修改。
      // 注：对电池相关属性 modify property 时 api 中的 shooter 可能为假玩家。
      // 不建议开启，可能会占用非常多的服务器资源。
      "dynamic_max_output_speed": false,
    }
  }
}
```

# 6.4 版本新增内容
### 弹道轨迹

弹道轨迹粒子。
<br>
仅支持 AAA 粒子，不支持原版粒子。
<br>
前方向为 Effekseer 编辑器的中的 +Z 方向。

```json5
{
  "gunsmithlib_extension": {
    "bullet_trails": [
      {
        // 必填且必须为 true
        "is_aaa_particle": true,
        // 示例特效 id，安装 AAA Particles：World 模组即可体验。
        // 如需自制特效 / 把特效放到自己的包里的话，按照 AAAPW 的路径格式把特效放进枪包的资源包文件夹里即可。
        "particle_id": "aaa_particles_world:missile_boost",
        // AAA 粒子的配置
        "aaa_particle_data": {
          // 调整缩放倍率
          "scale": 0.5
        }
      }
    ]
    // 如果你希望让 AAA 子弹轨迹和原版子弹轨迹一起出现的话，请开启下面这一行
    // 记得加逗号 :)
    // "hide_vanilla_bullet_trails": false
  }
}
```