### 简介

一些可被状态机修改，写法类似 `modify_prpoerty` 的参数。
<br/>
入口函数名为 `gunsmithlib_modify_display_property`，参数为 (context, id, original)，分别为状态机 context，属性 id，属性原来的值。

### 案例

[pure_energy_191_state_machine.lua](scripting_snippets/pure_energy_191_state_machine.lua)

### 属性列表

| id                                  | 类型                                                                                                              | 说明             | 默认值      | 添加版本  |
|-------------------------------------|-----------------------------------------------------------------------------------------------------------------|----------------|----------|-------|
| `gunsmithlib_displayed_ammo_amount` | `number` (int)                                                                                                  | 武器 GUI 中显示的弹药量 | 武器内的弹药量  | 6.2.0 |
| `gunsmithlib_durability_bar_length` | `number` (float)                                                                                                | 耐久条的长度，范围 0-1  | -1       | 6.2.0 |
| `gunsmithlib_durability_bar_color`  | [java.awt.Color](https://github.com/openjdk/jdk/blob/master/src/java.desktop/share/classes/java/awt/Color.java) | 耐久条的颜色         | 原版耐久条的颜色 | 6.2.0 |

### 注意事项

1. 修改属性时传入的 context 无法执行动画相关方法！