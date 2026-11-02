### 简介

对原版 `net.minecraft.client.gui.GuiGraphics` 的封装，供动画状态机脚本在 HUD 渲染期间绘制内容。

通过 `context:gunsmithlib_extension():gui_graphics()` 获取实例。
每次调用都会创建一个新的 `GuiGraphics`（使用全新的 `PoseStack`），不会受到当前 HUD 渲染状态的影响。

也可以通过 `gunsmithlib_render_hud(context, gui)` 钩子获取 GunsmithLib 传入的实例，
该实例封装的是当前 HUD 渲染所用的 `GuiGraphics`，可以直接在上面绘制 HUD 内容。

### 可用版本

自 7.0.0

### 示例

```lua
function M.gunsmithlib_render_hud(context, gui)
    -- 在屏幕左上角画一行字
    gui:draw_string("Hello, HUD!", 8, 8, gunsmithlib.new_color(255, 255, 255), true)
    -- 画一个半透明的红色矩形
    gui:fill(100, 100, 160, 130, gunsmithlib.new_color(255, 0, 0, 128))
end
```

### API

| 函数名                                                                                                                              | 返回值类型     | 说明                                                                | 添加版本 |
|-------------------------------------------------------------------------------------------------------------------------------------|----------------|---------------------------------------------------------------------|----------|
| width()                                                                                                                             | `number` (int) | 获取 GUI 的缩放后宽度                                               | 7.0.0    |
| height()                                                                                                                            | `number` (int) | 获取 GUI 的缩放后高度                                               | 7.0.0    |
| push_pose()                                                                                                                         | `nil`          | 压入一次矩阵变换                                                    | 7.0.0    |
| pop_pose()                                                                                                                          | `nil`          | 弹出最近一次压入的矩阵变换                                          | 7.0.0    |
| translate(x, y, z)                                                                                                                  | `nil`          | 平移坐标系                                                          | 7.0.0    |
| scale(x, y, z)                                                                                                                      | `nil`          | 缩放坐标系                                                          | 7.0.0    |
| rotate(axis, degrees)                                                                                                               | `nil`          | 绕指定坐标轴旋转坐标系，axis 为 "x"、"y" 或 "z"                     | 7.0.0    |
| fill(min_x, min_y, max_x, max_y, color)                                                                                             | `nil`          | 绘制实心矩形                                                        | 7.0.0    |
| fill_gradient(x1, y1, x2, y2, color_from, color_to)                                                                                 | `nil`          | 绘制渐变色矩形                                                      | 7.0.0    |
| h_line(min_x, max_x, y, color)                                                                                                      | `nil`          | 绘制水平线                                                          | 7.0.0    |
| v_line(x, min_y, max_y, color)                                                                                                      | `nil`          | 绘制竖直线                                                          | 7.0.0    |
| render_outline(x, y, width, height, color)                                                                                          | `nil`          | 绘制空心矩形（边框）                                                | 7.0.0    |
| set_color(color)                                                                                                                    | `nil`          | 设置后续渲染的颜色，颜色为 `java.awt.Color` 对象                    | 7.0.0    |
| draw_string(text, x, y, color, drop_shadow)                                                                                         | `number` (int) | 绘制一行文本（文本按翻译键解析，x/y 向下取整），返回文本宽度        | 7.0.0    |
| draw_string_centered(text, x, y, color)                                                                                             | `nil`          | 绘制一行居中文本（文本按翻译键解析）                                | 7.0.0    |
| draw_string_wrapped(text, x, y, line_width, color)                                                                                  | `nil`          | 绘制自动换行的文本（文本按翻译键解析）                              | 7.0.0    |
| blit(texture, x, y, u, v, width, height)                                                                                            | `nil`          | 绘制贴图，贴图尺寸按 256x256 处理                                   | 7.0.0    |
| blit(texture, x, y, width, height, z, u, v, u_width, v_height)                                                                      | `nil`          | 绘制贴图，直接指定 UV 坐标和渲染层级                                | 7.0.0    |
| blit_nine_sliced(texture, x, y, width, height, slice_size, u, v, source_width, source_height)                                       | `nil`          | 九宫格拉伸绘制贴图，四边切片大小相同，贴图尺寸按 256x256 处理       | 7.0.0    |
| blit_nine_sliced(texture, x, y, width, height, left_slice, top_slice, right_slice, bottom_slice, u, v, source_width, source_height) | `nil`          | 九宫格拉伸绘制贴图，四边切片大小可分别指定，贴图尺寸按 256x256 处理 | 7.0.0    |
| blit_repeating(texture, x, y, width, height, u, v, source_width, source_height)                                                     | `nil`          | 重复平铺绘制贴图，贴图尺寸按 256x256 处理                           | 7.0.0    |
| blit_repeating(texture, x, y, width, height, u, v, source_width, source_height, texture_width, texture_height)                      | `nil`          | 重复平铺绘制贴图，可指定贴图总尺寸                                  | 7.0.0    |
| render_item(stack, x, y)                                                                                                            | `nil`          | 绘制一个物品                                                        | 7.0.0    |
| render_item_decorations(stack, x, y)                                                                                                | `nil`          | 绘制物品的角标（数量、耐久条等）                                    | 7.0.0    |
| enable_scissor(min_x, min_y, max_x, max_y)                                                                                          | `nil`          | 开启矩形裁剪区域                                                    | 7.0.0    |
| disable_scissor()                                                                                                                   | `nil`          | 关闭最近一次开启的裁剪区域                                          | 7.0.0    |

颜色参数均为 `java.awt.Color` 对象，可使用 `gunsmithlib.new_color(r, g, b)` 或
`gunsmithlib.new_color(r, g, b, a)` 创建（分量范围可以是 0-255 或 0.0-1.0）。

所有文本参数都会按翻译键解析（内部使用 `Component.translatable`）：
没有对应翻译资源时会原样显示传入的文本，因此既可以直接传字面文本，也可以传翻译键。

两个 `blit` 重载的参数含义不同：
`blit(texture, x, y, u, v, width, height)` 的 `u`、`v` 是**像素**坐标，
而 `blit(texture, x, y, width, height, z, u, v, u_width, v_height)` 的
`u`、`v`、`u_width`、`v_height` 是 **0-1** 的 UV 坐标，`z` 为渲染层级。

### 注意事项

1. 返回的对象只在当前回调期间有效，请不要保存到全局变量中跨帧使用。
2. `push_pose()` 必须和 `pop_pose()` 成对使用，`enable_scissor()` 必须和 `disable_scissor()` 成对使用，
   否则会影响后续的 HUD 渲染。
3. 建议在 HUD 渲染期间（例如 `gunsmithlib_render_hud` 回调中）绘制；
   在其他时机绘制可能不会显示在正确的位置。
