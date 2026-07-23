package com.nukkitx.fakeinventories.inventory;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;

public class FakeSlotChangeEvent implements Cancellable {
    private final Player player;
    private final FakeInventoryLike inventory;
    private final SlotChangeAction action;
    private boolean cancelled = false;

    FakeSlotChangeEvent(Player player, FakeInventoryLike inventory, SlotChangeAction action) {
        this.player = player;
        this.inventory = inventory;
        this.action = action;
    }

    public Player getPlayer() {
        return player;
    }

    public SlotChangeAction getAction() {
        return action;
    }

    /**
     * @return the legacy {@link FakeInventory} view, or {@code null} when the
     *         source inventory is a SAI-compatible variant that does not extend
     *         {@link FakeInventory} (e.g. {@link FurnaceFakeInventory}); use
     *         {@link #getInventoryLike()} for those.
     */
    public FakeInventory getInventory() {
        return inventory instanceof FakeInventory ? (FakeInventory) inventory : null;
    }

    /**
     * @return the source inventory regardless of concrete parent class. Prefer
     *         this over {@link #getInventory()} when SAI-compatible variants must
     *         be handled.
     */
    public FakeInventoryLike getInventoryLike() {
        return inventory;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled() {
        this.cancelled = true;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}