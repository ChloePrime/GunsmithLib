local M = {}

function M.gunsmith_is_shield_working(api)
    print("gunsmith_is_shield_working")
    return api:getAmmoAmount() > 0
end

return M