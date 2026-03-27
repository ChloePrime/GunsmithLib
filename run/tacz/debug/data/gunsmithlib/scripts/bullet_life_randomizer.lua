local M = {}

function M.modify_property(_api, id, default)
    if (id == "explosion_delay") then
        return default * 1.5 ^ (2.0 * math.random() - 1)
    else
        return default
    end
end

return M