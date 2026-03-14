package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import org.luaj.vm2.LuaValue;

@SuppressWarnings("unused")
public interface BetterAsyncExtension {
    /**
     * 在给定的游戏刻延迟后执行 lua 函数。
     * 倒计时结束后如果用户切换别的 id 的武器或者没有持枪时会自动停止执行。
     *
     * @param value lua 函数
     * @param delayTicks 延迟
     */
    void async_run_delayed(LuaValue value, int delayTicks);

    /**
     * 以指定的游戏刻间隔循环执行 lua 函数。
     * 执行前如果用户切换别的 id 的武器或者没有持枪时会自动终止循环。
     * <p>
     * 传入的 lua 函数需要返回一个 bool 值。
     * 如果返回 false 则终止循环。
     *
     * @param value lua 函数
     * @param period 循环周期
     * @param count 循环次数
     */
    void async_run_cycled(LuaValue value, int period, int count);
}
