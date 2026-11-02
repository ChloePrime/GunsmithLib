## 概览
v2 将 v1 中的扩展函数专门放到一个新对象里，
脚本在将其存储为一个局部变量后可大幅降低单行长度。

### 示例：
旧写法：
```lua
function M.shoot(api)
    api:shootOnce(api:isShootingNeedConsumeAmmo())
    api:gunsmith_asyncRunDelayed(your_callback, 2, 20, api:gunsmith_getGunId(), ext:gunsmith_getEstimatedRange())
end
```
看，单行长度飙到天上去了对吧<br>
那来看看新版的写法吧：
```lua
function M.shoot(api)
    local ext = api:gunsmithlib_extension()
    api:shootOnce(api:isShootingNeedConsumeAmmo())
    ext:async_run_cycled(your_callback, 2, 20, ext:get_gun_id(), ext:get_estimated_range())
end
```
行长度是不是短了不少 =w=

### HUD 绘制

自 7.0.0 起，动画状态机脚本可以实现 `gunsmithlib_render_hud(context, gui)` 函数。
GunsmithLib 会在 HUD 渲染完成后调用它，`gui` 为 [GuiGraphicsAPI](api_classes/gui_graphics.md)：

```lua
function M.gunsmithlib_render_hud(context, gui)
    gui:draw_string("Hello, HUD!", 8, 8, gunsmithlib.new_color(255, 255, 255), true)
end
```

这个钩子只在主手持有枪械时触发。

完整 API 列表和注意事项请参考 [gui_graphics.md](api_classes/gui_graphics.md)。