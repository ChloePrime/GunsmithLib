## 扩展功能通用格式:
参考 [gunpack_extensions.md](gunpack_extensions.md) 中的添加方式，
只不过添加在 display json 而不是 data json 里
```json5
{
  "gunsmithlib_extension": {
  },
}
```
此时你的 display 文件应该看起来像这样：
```json5
{
  // ......
  "gunsmithlib_extension": {
  },
  "model": "......",
  "texture": "......",
  // ......
}
```
## 隐藏热量条 HUD:
在`gunsmithlib_extension`对象中加入如下键值对：
```json5
"hide_heat_bar_overlay": true
```
完整的data文件应该看起来像这样：
```json5
{
  // ......
  "gunsmithlib_extension": {
    "hide_heat_bar_overlay": true
  },
  "model": "......",
  "texture": "......",
  // ......
}
```
## 弹匣剩余弹药显示模式覆盖:
4.8.0版本新增，可以控制右下角剩余弹药计数器的显示风格。
```json5
{
  "gunsmithlib_extension": {
    // 可选值有 default, battery, counter
    // default: 默认，如果为充电武器则显示电池，否则显示计数器
    // battery: 显示电池，无论是否为充电武器
    // counter: 显示计数器，无论是否为充电武器
    "current_ammo_display_type": "battery"
  },
}
```