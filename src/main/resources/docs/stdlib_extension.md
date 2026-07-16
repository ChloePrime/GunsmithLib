### 简介

对 lua 标准库的扩展。放在 `gunsmithlib` 全局对象下。

### 可用版本

自 6.2.0

### 示例

```lua
print(gunsmithlib.new_color(0, 255, 255):toString())
```

### API

| 函数名                                                                                                                | 返回值类型                                                                                                           | 说明                          | 详细说明                             | 添加版本  |
|--------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|-----------------------------|----------------------------------|-------|
| new_identifier(string, string?)                                                                                    | [net.minecraft.resources.ResourceLocation](https://mcsrc.dev/1/1.20.1/net/minecraft/resources/ResourceLocation) | `new_resource_location` 的别名 | 同 `new_resource_location`        | 6.2.0 |
| new_resource_location(string, string?)                                                                             | [net.minecraft.resources.ResourceLocation](https://mcsrc.dev/1/1.20.1/net/minecraft/resources/ResourceLocation) | 构造新的 ResourceLocation 对象    | 支持 1 或 2 个参数，默认命名空间为 `minecraft` | 6.2.0 |
| new_color(number, number, number, number?)                                                                         | [java.awt.Color](https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/Color.html)           | 构造新的 AWT Color 对象           | Alpha 默认为 255 (1.0)              | 6.2.0 |
| int_color_from_rgb(number, number, number, number?)                                                                | `number` (int)                                                                                                  | 将 RGB 颜色编码至 32 位整数中         |                                  | 6.2.0 |
| uuid_from_string(string)                                                                                           | [java.util.UUID](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/UUID.html)              | 将字符串形式的 UUID 转化为 UUID 对象    |                                  | 6.2.0 |
| uuid_to_string([java.util.UUID](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/UUID.html)) | `string`                                                                                                        | 将 UUID 对象转化为其字符串形式          |                                  | 6.2.0 |
