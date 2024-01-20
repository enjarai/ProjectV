package dev.enjarai.projectv.util;

public final class ThreadLocals {
    public static final ThreadLocal<Integer> LAST_DRAGGED_SLOT_ID = ThreadLocal.withInitial(() -> -1);
}
