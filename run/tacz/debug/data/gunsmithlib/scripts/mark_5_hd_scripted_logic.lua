local M = {}

-- 最大挡位瞄准时伤害×10
function M.gunsmithlib_attachment_modify_property(api, id, default)
    if (id == "damage") then
        local ext = api:gun():gunsmithlib_extension()
        local is_aiming = api:shooter_states():aiming_progress() >= 0.8
        local is_max_slot = api:attachment_item_interface():getZoomNumber(api:attachment_stack()) % 3 == 1
        local apply_buff = is_aiming and is_max_slot
        if (apply_buff) then
            local microphone = api:gun():getEntityUtil()
            microphone:sendActionBar(microphone:literal("§cJackpot!"))
        end
        return default * ext:ternary_op(apply_buff, api:script_params().damage_buff_amount, 1);
    else
        return default
    end
end

return M