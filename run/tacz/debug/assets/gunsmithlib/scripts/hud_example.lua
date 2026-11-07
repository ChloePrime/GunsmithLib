local M = setmetatable({}, {__index = require("gunsmithlib_pure_energy_191_state_machine")})

function M.gunsmithlib_render_hud_before_hand(context, gui)
    local ext = context:gunsmithlib_extension()
    local searched = ext:search()
    if (searched == nil) then
        return
    end
    local in_world_gui = ext:gui_graphics_in_world(searched:pos(), 1, false)
    in_world_gui:blit("gunsmithlib:textures/crosshair.png", -8, -8, 16, 16, 0, 0, 0, 1, 1)
end

function M.gunsmithlib_render_hud(context, gui)
    local ext = context:gunsmithlib_extension()

    gui:draw_string(ext:translate("gunsmithlib.text.hello_hud", {}), 8, 8, gunsmithlib.new_color(255, 255, 255), true)

    local lang_with_sprintf = ext:translate("gunsmithlib.text.sprintf_example", { math.random(10, 99), math.random(10, 99) })
    gui:draw_string(lang_with_sprintf, 8, 16, gunsmithlib.new_color(255, 255, 255), true)

    local ammo_cnt = context:getAmmoCount()
    gui:draw_string("Current Ammo Amount: " .. tostring(ammo_cnt), 8, 24, gunsmithlib.new_color(255, 255, 255), true)

    gui:blit("gunsmithlib:textures/dreadnought_cat.png", 8, 32, 64, 64, 0, 0, 0, 1, 1)
end

return M;