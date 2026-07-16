-- 需要 GunsmithLib 版本 >= 6.2.0

-- 因为演示武器是拿 QBZ 191 改的，
-- 所以这里的 parent 引用 QBZ-191 的逻辑机。
local M = setmetatable({}, {__index = require("tacz_xmag_reload_logic")})

-- 将能量同步到武器剩余弹量中。
-- 这是为了在能量不足时让左键射击不再播放射击动画，以及潜在地为了和其他附属兼容。
local function update_shoot_lock(api)
    local ext = api:gunsmithlib_extension()
    local ammo_amount = math.floor(ext:energy_stored() / api:getScriptParams().shoot_cost)
    api:getAbstractGunItem():setCurrentAmmoCount(api:getItemStack(), ammo_amount)
end

function M.gunsmithlib_on_draw(api)
    -- 初始化时，
    -- 将能量同步到武器剩余弹量中。
    update_shoot_lock(api)
end

function M.shoot(api)
    local params = api:getScriptParams()
    local ext = api:gunsmithlib_extension()
    local shoot_cost = params.shoot_cost
    -- 先模拟抽取能量，以测试武器剩余能量是否充足。
    -- 因为这是武器能量的主要用途，
    -- 所以使用忽视放电上限抽取能量的方法。
    if (ext:privileged_extract_energy(shoot_cost, true) >= shoot_cost) then
        -- 如果能量充足，那么抽取能量并击发。
        ext:privileged_extract_energy(shoot_cost)
        api:shootOnce(false)
    end
    -- 将能量同步到武器剩余弹量中。
    update_shoot_lock(api)
end

return M