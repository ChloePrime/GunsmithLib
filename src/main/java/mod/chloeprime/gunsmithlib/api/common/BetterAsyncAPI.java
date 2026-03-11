package mod.chloeprime.gunsmithlib.api.common;

import org.luaj.vm2.LuaValue;

@SuppressWarnings("unused")
public interface BetterAsyncAPI {
    /**
     * 在给定的游戏刻延迟后执行 lua 函数。
     * 倒计时结束后如果用户切换别的 id 的武器或者没有持枪时会自动停止执行。
     *
     * @param value lua 函数
     * @param delayTicks 延迟
     */
    void gunsmith_asyncRunDelayed(LuaValue value, int delayTicks);

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
    void gunsmith_asyncRunCycled(LuaValue value, int period, int count);
}
