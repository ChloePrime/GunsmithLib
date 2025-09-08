## 由本模组添加的 Attribute
这些 Attribute，以及所有原版和其他的 Attribute，都可以以 Attribute Modifier 的形式添加在枪械和配件上，作为配件功能的一部分哦~



| Attribute                          | 说明          | 取值范围        | 默认值                      | 说明               |
|------------------------------------|-------------|-------------|--------------------------|------------------|
| `gunsmithlib:bullet_damage`        | 单个弹片造成的基础伤害 | R (任意实数)    | 未安装GunsmithLib时枪械的面板伤害   |                  |
| `gunsmithlib:armor_piercing_ratio` | 穿甲率         | [0,1]       | 未安装GunsmithLib时枪械的面板穿甲率  |                  |
| `gunsmithlib:headshot_multiplier`  | 爆头倍率        | R (任意实数)    | 未安装GunsmithLib时枪械的面板爆头倍率 | 1.0 = 100%       |
| `gunsmithlib:bullet_speed`         | 子弹飞行速度      | [0, +∞]     | 未安装GunsmithLib时的子弹飞行速度   | 单位 米/tick        |
| `gunsmithlib:horz_recoil`          | 横向后坐力百分比    | R (任意实数)    | 1 (100%)                 |                  |
| `gunsmithlib:vert_recoil`          | 纵向后坐力百分比    | R (任意实数)    | 1 (100%)                 |                  |
| `gunsmithlib:rpm`                  | 射速          | [1, 1200]   | 未安装GunsmithLib时的射速       | 单位 发/分钟          |
| `gunsmithlib:ammo_capacity`        | 弹匣容量        | [0, 2^31-1] | 未安装GunsmithLib时的弹匣容量     | 只能出现在物品上，对实体无效   |
| `gunsmithlib:reload_speed`         | 换弹速度的倍率     | [0, +∞]     | 1 (100%)                 |                  |
| `gunsmithlib:aim_lock_range`       | 最大火控锁定距离    | [0, 1024]   | 枪械的面板有效射程                | 和枪械自带的火控距离之间取最大值 |
| `gunsmithlib:aim_lock_angle`       | 火控锁定角范围     | [0, 360]    | 0                        | 单位为角度            |

## 示例：
使用配件 data 扩展实现的火控瞄准镜
```json5
{
  //......
  "gunsmithlib_extension": {
    "attribute_modifiers": [
      {
        // 修饰器作用的属性
        "attribute": "gunsmithlib:aim_lock_angle",
        // 修饰器id。
        // 请使用 https://www.uuidgenerator.net/ 生成独特的 uuid，
        // 你使用的 uuid 不应该与任何你看到的 uuid 相同，除非你明确知道这样做会导致什么后果。
        "id": "928c3d3d-b850-4604-824b-b31a4dc01b38",
        "name": "Fire Control System",
        // 修饰器的值
        "amount": "60",
        // 修饰器的运算模式
        // 可选，默认为 ADDITION（加算）
        "operation": "ADDITION"
      }
    ]
  },
  //......
}
```
