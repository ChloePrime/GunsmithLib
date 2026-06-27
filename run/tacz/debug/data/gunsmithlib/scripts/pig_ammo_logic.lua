local M = {}

local function get_uid(shooter)
    local is_production = shooter.getUUID == nil
    if (is_production) then
        return shooter:m_20148_()
    else
        return shooter:getUUID()
    end
end

local function run_commands(shooter, commands)
    local is_production = shooter.getUUID == nil
    if (is_production) then
        local server = shooter:m_9236_():m_7654_()
        for _i, command in pairs(commands) do
            server:m_129892_():m_230957_(server:m_129893_():m_81324_(), command)
        end
    else
        local server = shooter:level():getServer()
        for _i, command in pairs(commands) do
            server:getCommands():performPrefixedCommand(server:createCommandSourceStack():withSuppressedOutput(), command)
        end
    end
end

-- 击杀目标时在目标头顶生成🐷
function M.gunsmithlib_ammo_post_hit_entity(api, event)
    local killed = not event:get_victim():is_alive()
    if not killed then
        return
    end
    local shooter = api:gun():getShooter()
    local target_pos = event:get_victim():eye_position()
    run_commands(shooter, {
        string.format("summon minecraft:pig %f %f %f", target_pos.x, target_pos.y + 1.2, target_pos.z),
        string.format("playsound minecraft:entity.pig.ambient player @a[distance=..16] %f %f %f", target_pos.x, target_pos.y, target_pos.z),
    })
end

return M