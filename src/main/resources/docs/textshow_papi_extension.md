# 本模组新增的文本占位符：
| 占位符名称                              | 作用                | 备注                        | 添加版本   |
|------------------------------------|-------------------|---------------------------|--------|
| `%gunsmithlib:rangefinder_result%` | 显示预估射程（测距）        | 不考虑穿透，即为0穿透下的结果           | 4.4.0  |
| `%gunsmithlib:airburst_distance%`  | 当前武器的空爆距离         |                           | 4.12.0 |
| `%gunsmithlib:ammo_stock%`         | 当前玩家的弹药储备         | 整数                        | 6.4.0  |
| `%gunsmithlib:player_pos%`         | 当前玩家的位置           | 格式为 "x, y, z"，精确到小数点后 1 位 | 6.4.0  |
| `%gunsmithlib:target_pos%`         | 当前瞄准目标的位置         | 格式为 "x, y, z"，精确到小数点后 1 位 | 6.4.0  |
| `%gunsmithlib:heat_percent%`       | 当前武器的热量百分比        | 整数，0-100，不含百分号            | 6.4.0  |
| `%gunsmithlib:block_light%`        | 当前玩家受到的**方块**光照   | 0-15 范围内的整数               | 6.4.0  |
| `%gunsmithlib:sky_light%`          | 当前玩家受到的**天空**光照   | 0-15 范围内的整数               | 6.4.0  |
| `%gunsmithlib:realtime_light%`     | 当前玩家受到的**实时**光照   | 0-15 范围内的整数               | 6.4.0  |
| `%gunsmithlib:realtime_sky_light%` | 当前玩家受到的**实时天空**光照 | 0-15 范围内的整数               | 6.4.0  |
| `%gunsmithlib:theoretical_light%`  | 当前玩家受到的**理论**光照   | 0-15 范围内的整数               | 6.4.0  |
| `%gunsmithlib:target_name%`        | 当前瞄准目标的名字         | 字符串，不含格式字符                | 6.4.0  |
| `%gunsmithlib:target_size%`        | 当前瞄准目标的参考大小       | 精确到小数点后 1 位               | 6.4.0  |