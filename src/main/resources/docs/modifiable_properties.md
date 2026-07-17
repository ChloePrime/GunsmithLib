### 简介

一些可被逻辑脚本 `modify_prpoerty` 修改的参数。

### 属性列表

| id                                         | 类型                | 说明             | 默认值                   | 添加版本  |
|--------------------------------------------|-------------------|----------------|-----------------------|-------|
| `gunsmithlib_energy_capacity`              | `number` (long)   | 电池容量           | 需要在 data 中手动开启才可被脚本修改 | 6.2.0 |
| `gunsmithlib_max_energy_input_speed`       | `number` (long)   | 最大充电速度         | 需要在 data 中手动开启才可被脚本修改 | 6.2.0 |
| `gunsmithlib_max_energy_output_speed`      | `number` (long)   | 最大放电速度         | 需要在 data 中手动开启才可被脚本修改 | 6.2.0 |
|                                            |                   |                |                       |
| `gunsmithlib_measured_airburst_distance`   | `number` (double) | 测距与测得的空爆距离     | 使用空爆测距仪时触发            | 6.2.0 |
| `gunsmithlib_programmed_airburst_distance` | `number` (double) | 火控计算机传给子弹的空爆距离 | 子弹发射时触发               | 6.2.0 |
| `gunsmithlib_proximity_fuse_distance`      | `number` (double) | 近炸引信的探测距离      | 子弹发射时触发               | 6.2.0 |
| `gunsmithlib_frag_count`                   | `number` (int)    | 破片炸弹的破片数量      | 子弹爆炸即将产生破片时触发         | 6.2.0 |
| `gunsmithlib_safety_distance`              | `number` (double) | 爆炸物的安全距离       |                       | 6.2.0 |