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

## HUD 绘制

### 基本用法

自 7.0.0 起，动画状态机脚本可以实现 `gunsmithlib_render_hud(context, gui)` 函数。
GunsmithLib 会在 HUD 渲染完成后调用它，`gui` 为 [GuiGraphicsAPI](api_classes/gui_graphics.md)：

```lua
function M.gunsmithlib_render_hud(context, gui)
    gui:draw_string("Hello, HUD!", 8, 8, gunsmithlib.new_color(255, 255, 255), true)
end
```

这个钩子只在主手持有枪械时触发。

完整 API 列表和注意事项请参考 [gui_graphics.md](api_classes/gui_graphics.md)。

如果需要绘制跟随世界物体的 HUD（例如敌人头顶的血条、锁定准星），
可以用 `gunsmithlib_extension():gui_graphics_in_world(world_pos[, scale[, distance_scaled]])`
获取锚定在世界坐标上的 [InWorldGuiGraphics](api_classes/in_world_gui_graphics.md)：

```lua
function M.gunsmithlib_render_hud(context, gui)
    local hud = context:gunsmithlib_extension():gui_graphics_in_world(pos, 1.0, true)
    if not hud:visible() then
        return
    end
    hud:fill(-12, -2, 12, 1, gunsmithlib.new_color(0, 0, 0, 160))
end
```

### 在手部渲染之前绘制 HUD

自 7.0.0 起，动画状态机脚本还可以实现 `gunsmithlib_render_hud_before_hand(context, gui)` 函数。

GunsmithLib 会在**世界渲染结束之后、手部渲染之前**调用它。
可用于绘制不会显示在手臂之上的内容，例如血条和锁定准星。

`gui` 为 [GuiGraphicsAPI](api_classes/gui_graphics.md)：

```lua
function M.gunsmithlib_render_hud_before_hand(context, gui)
    gui:draw_string("Behind Hand!", 427, 320, gunsmithlib.new_color(255, 255, 255), true)
end
```

渲染顺序大致是：

```
世界渲染（含实体、粒子、天气） → 这个钩子 → 手和手持物 → 原版 HUD / GUI
```

注意事项：

1. 只在主手持有带脚本的枪械时触发。
2. 绘制期间 GunsmithLib 会关闭深度测试、画完之后恢复，因此 HUD 不会被世界几何挡住。
3. 它画在原版 HUD 之前，因此会被原版 HUD 和任何打开的 GUI 盖住。