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