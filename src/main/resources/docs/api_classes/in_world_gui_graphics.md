### 简介

锚定在世界坐标上的 [GuiGraphicsAPI](gui_graphics.md)，用于绘制跟随世界物体的 HUD，
参考 3D 动作游戏里敌人头顶的血条、锁定准星等效果。

通过 `context:gunsmithlib_extension():gui_graphics_in_world(...)` 获取实例。

### 可用版本

自 7.0.0

### 坐标系

- 原点为传入的世界坐标在屏幕上的投影位置，`+x` 向右、`+y` 向下。
- 每个 HUD 单位对应的屏幕像素由 `scale` 和 `distance_scaled` 决定。
- 锚点位于相机背后，或者相机尚未初始化（例如刚进入世界、还没有渲染过一帧）时，
  `visible()` 返回 `false`，此时所有绘制调用都不会产生效果，也不会抛出异常。
- 因为沿用 [GuiGraphicsAPI](gui_graphics.md) 的接口，坐标参数大多是整数，
  所以需要画很细的东西时请调大 `scale`。

### 参数

`gui_graphics_in_world(world_pos)`<br>
`gui_graphics_in_world(world_pos, scale)`<br>
`gui_graphics_in_world(world_pos, scale, distance_scaled)`

| 参数            | 类型       | 说明                                       |
|-----------------|------------|--------------------------------------------|
| world_pos       | `Vector3d` | 锚点的世界坐标                             |
| scale           | `number`   | 每个 HUD 单位对应的 GUI 像素数，默认 `1.0` |
| distance_scaled | `boolean`  | 是否随距离缩放，默认 `true`                |

`distance_scaled = true`（默认）：HUD 的视觉尺寸在世界中保持恒定 ——
锚点距离相机 8 格时 1 个 HUD 单位等于 1 个 GUI 像素，距离越远 HUD 越小、越近越大。
它还会跟随 FOV 设置和瞄准/开镜造成的视野放大一起缩放，所以开镜放大后 HUD 不会相对世界变小。
适合血条这类“贴在世界物体上”的 HUD。

`distance_scaled = false`：HUD 在屏幕上保持恒定尺寸，适合锁定准星这类 HUD（吗）。

### 示例

```lua
-- 世界内 HUD 示例:
-- 在火控系统选中的目标处画一个下界之星
function M.gunsmithlib_render_hud_before_hand(context, gui)
    local ext = context:gunsmithlib_extension()
    -- 搜索目标
    local searched = ext:search()
    -- 如果没有搜到，则直接返回
    if (searched == nil) then
        return
    end
    -- 以搜索到的位置为原点创建 In World Gui Graphics
    local in_world_gui = ext:gui_graphics_in_world(searched:pos(), 1, true)
    -- 绘制下界之星
    in_world_gui:blit("minecraft:textures/item/nether_star.png", -8, -8, 16, 16, 0, 0, 0, 1, 1)
end
```

### API

除下表外，其余方法与 [GuiGraphicsAPI](gui_graphics.md) 完全一致
（包括 `push_pose()` / `translate()` / `rotate()` / `scale()` 这些变换方法，
以及 `enable_scissor()` / `disable_scissor()`，它们都作用在本对象的坐标系上）。

| 函数名     | 返回值类型 | 说明                                                                    | 添加版本 |
|------------|------------|-------------------------------------------------------------------------|----------|
| visible()  | `boolean`  | 锚点是否可见（在相机前方且投影成功）；为 `false` 时所有绘制都是空操作  | 7.0.0    |
| distance() | `number`   | 锚点到相机的距离（单位：方块）；不可见时返回 0                          | 7.0.0    |

注意 `width()` / `height()` 返回的是可见区域在本对象坐标系下的尺寸
（即屏幕上的 GUI 像素数除以缩放），而不是屏幕像素数。

### 注意事项

1. 返回的对象只在当前回调期间有效，请不要保存到全局变量中跨帧使用。
2. 如果锚点会移出屏幕，记得用 `visible()` 判断，或者自己再做屏幕外裁剪。
3. 该 HUD 绘制在屏幕空间，不支持 Depth Test，也即不会被世界中的方块遮挡。
