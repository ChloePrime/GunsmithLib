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