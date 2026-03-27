package mod.chloeprime.gunsmithlib.common.util;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;

import java.util.ArrayDeque;
import java.util.Deque;

public final class LauncherContext {
    public static final ThreadLocal<Deque<GunInfo>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private LauncherContext() {
    }
}
