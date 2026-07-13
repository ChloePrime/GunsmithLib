-- 因为演示武器是拿 QBZ 191 改的，
-- 所以这里的 parent 引用 QBZ-191 的状态机。
local M = setmetatable({}, {__index = require("tacz_qbz_191_state_machine")})

-- 修改客户端显示属性。
-- 对于部分属性来说，这个方法中的 context 变量无法执行动画相关的方法。
function M.gunsmithlib_modify_display_property(context, id, original)
    local ext = context:gunsmithlib_extension()
    local params = ext:logic_script_params()
    if id == "gunsmithlib_displayed_ammo_amount" then
        -- 修改显示的弹量，
        -- 这样才能让在不在主手上的能量武器能正常刷新 Tooltip 中显示的弹容量。
        return math.floor(ext:energy_stored() / params.shoot_cost)
    elseif id == "gunsmithlib_durability_bar_length" then
        -- 修改耐久条长度
        -- GunsmithLib 使用 0.0f-1.0f 来表示耐久条长度，而非 Java Modding 中的 0 - 13。
        return ext:energy_stored() / ext:get_configured_battery_capacity()
    elseif id == "gunsmithlib_durability_bar_color" then
        -- 修改耐久条颜色。
        -- 返回值的类型需要为 java.awt.Color。
        -- 这里使用 gunsmithlib 添加的新标准库函数创建 Color。
        -- 具体 API 请参考 mod.chloeprime.gunsmithlib.common.scripting.lang.GunsmithLuaLib
        return gunsmithlib.new_color(0, 255, 255)
    end
    return original
end

return M