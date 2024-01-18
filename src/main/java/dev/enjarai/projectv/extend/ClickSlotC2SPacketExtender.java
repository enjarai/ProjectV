package dev.enjarai.projectv.extend;

/**
 * Last dragged slot id of -2 is used to indicate that the player was in creative mode.
 */
public interface ClickSlotC2SPacketExtender {
    void projectV$setLastDraggedSlot(int slot);

    int projectV$getLastDraggedSlot();
}
