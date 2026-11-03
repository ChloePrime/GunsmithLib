### 简介

对 lua 标准库的扩展。放在 `gunsmithlib` 全局对象下。

### 可用版本

自 6.2.0

### 示例

```lua
print(gunsmithlib.new_color(0, 255, 255):toString())
```

### API

| 函数名                                                                                                             | 返回值类型                                                                                                      | 说明                                 | 详细说明                                       | 添加版本 |
|--------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|--------------------------------------|------------------------------------------------|----------|
| new_identifier(string, string?)                                                                                    | [net.minecraft.resources.ResourceLocation](https://mcsrc.dev/1/1.20.1/net/minecraft/resources/ResourceLocation) | `new_resource_location` 的别名       | 同 `new_resource_location`                     | 6.2.0    |
| new_resource_location(string, string?)                                                                             | [net.minecraft.resources.ResourceLocation](https://mcsrc.dev/1/1.20.1/net/minecraft/resources/ResourceLocation) | 构造新的 ResourceLocation 对象       | 支持 1 或 2 个参数，默认命名空间为 `minecraft` | 6.2.0    |
| new_color(number, number, number, number?)                                                                         | [java.awt.Color](https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/Color.html)           | 构造新的 AWT Color 对象              | Alpha 默认为 255 (1.0)                         | 6.2.0    |
| int_color_from_rgb(number, number, number, number?)                                                                | `number` (int)                                                                                                  | 将 RGB 颜色编码至 32 位整数中        |                                                | 6.2.0    |
| uuid_from_string(string)                                                                                           | [java.util.UUID](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/UUID.html)              | 将字符串形式的 UUID 转化为 UUID 对象 |                                                | 6.2.0    |
| uuid_to_string([java.util.UUID](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/UUID.html)) | `string`                                                                                                        | 将 UUID 对象转化为其字符串形式       |                                                | 6.2.0    |
| new_vector3(number, number?, number?)                                                                              | [org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html)                         | 构造新的 Vector3d 对象               | 1 个参数时三个分量相同，其余同 `new_vector3d`  | 7.0.0    |
| new_vector3d(number, number?, number?)                                                                             | [org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html)                         | 构造新的 Vector3d 对象               | 支持 1 个参数（三个分量相同）或 3 个参数       | 7.0.0    |
| new_style_builder()                                                                                                | `TextStyleBuilder`                                                                                              | 构造新的文本样式构建器               | 用于 `push_style()`，见下文                    | 7.0.0    |

`new_vector3()` 和 `new_vector3d()` 返回的都是 [org.joml.Vector3d](https://javadoc.io/doc/org.joml/joml/latest/org/joml/Vector3d.html)，
写 `gunsmithlib.new_vector3(x, y, z)` 时传入三个分量，写 `gunsmithlib.new_vector3(x)` 时三个分量都等于 `x`。

`new_style_builder()` 返回一个可链式调用的文本样式构建器，
支持 `bold()`、`italic()`、`underline()`、`strikethrough()`、`obfuscated()`、`color(color)`、`font(font_id)`，
每个开关都另有接受 `boolean` 的重载（如 `bold(false)` 表示关闭），最后用 `create()` 取出 `Style`。
构建出来的样式用于 [GuiGraphicsAPI](api_classes/gui_graphics.md) 的 `push_style()`：

```lua
local style = gunsmithlib.new_style_builder():bold():color(gunsmithlib.new_color(255, 200, 0)):create()
```
