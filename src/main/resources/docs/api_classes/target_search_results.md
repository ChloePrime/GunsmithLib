### 简介

火控系统的目标搜索算法的返回结果。

### 可用版本

自 6.2.0

### API

| 函数名              | 返回值类型                                                                                             | 说明               | 详细说明            | 添加版本  |
|------------------|---------------------------------------------------------------------------------------------------|------------------|-----------------|-------|
| target()         | [EntityStates](shooter_states.md)                                                                 | 搜索到的目标           |                 | 6.2.0 |
| *target_entity() | [net.minecraft.world.entity.Entity](https://mcsrc.dev/1/1.20.1/net/minecraft/world/entity/Entity) | 搜索到的目标实体         |                 | 6.2.0 |
| pos()            | [org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html)           | 搜索到的位置           | 优先级：头部 > 身体 > 脚 | 6.2.0 |
| relative_pos()   | [org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html)           | 搜索到的位置和实体坐标的相对坐标 | 优先级：头部 > 身体 > 脚 | 6.2.0 |

### 说明：

- 标 * 的为试验性方法，用法并不像你下意识里认为的那样，仅限高级用户使用。