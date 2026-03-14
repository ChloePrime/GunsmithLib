package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

@SuppressWarnings("unused")
public interface RangefinderExtension {
    /**
     * 获取这把武器预估的射程。
     * 可用于在状态机中控制激光武器的激光块的长度。
     *
     * @return 这把武器预估的射程
     */
    double get_estimated_range();

    /**
     * 获取这把武器在指定穿透数量下预估的射程。
     * 可用于在状态机中控制激光武器的激光块的长度。
     *
     * @param pierce 能穿透多少个敌人，1 = 能穿透一个或击中两个敌人
     * @return 这把武器在指定穿透数量下预估的射程
     */
    double get_estimated_range(int pierce);
}
