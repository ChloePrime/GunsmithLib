-- 这个脚本会让武器开火后的 2 秒内持续发射冒烟粒子

local M = {}

-- 这是一个 async_run_cycled 的回调函数。
-- 第一个参数固定为 api 对象（ModernKineticGunScriptAPI）。
-- 第二个参数为循环计数。第一次调用时为 0，第二次为 1，以此类推。
-- 后面可以添加额外的参数，由调用 async_xxx 函数时额外传入。
--
-- 回调函数可以返回 boolean 或不返回。
-- > 返回 boolean 时，如果返回值为 false 则会终止循环。
-- > 不返回任何值时不会发生任何事情，循环将会继续执行。
local function smoking(api, _i, particle_id)
    -- 获取扩展 API 对象。
    local ext = api:gunsmithlib_extension()
    -- 生成粒子。
    -- get_muzzle_position() 会返回一个粗略估算的枪口位置（这个其实是 1.18.2 上旧版神化枪械的遗产，但是还能用）
    -- 第二个参数是粒子的数据。结构类似 data 文件中的 hit_particles（HitParticleData）。
    ext:spawn_particle(ext:get_muzzle_position(), {
      {
        ["particle_id"] = particle_id,
        ["dx"] = 0,
        ["dy"] = 1,
        ["dz"] = 0,
        ["speed"] = 0.125,
        ["count"] = 0,
      }
    })
end

function M.shoot(api)
    -- 开火，没什么好说的。
    -- 如果不写的话这把枪就只能冒烟而不会发射子弹了。
    api:shootOnce(api:isShootingNeedConsumeAmmo())
    -- async_run_cycled 会规划循环异步任务。
    -- 第一个参数是回调函数。
    -- 第二个参数是执行间隔，第三个参数是执行次数。
    -- 之后可以添加任意个参数，这些额外的参数会作为额外参数传入回调函数中。
    api:gunsmithlib_extension():async_run_cycled(smoking, 2, 20, "minecraft:large_smoke")
end

return M