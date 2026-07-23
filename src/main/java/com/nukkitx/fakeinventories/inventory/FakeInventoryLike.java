package com.nukkitx.fakeinventories.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.math.BlockVector3;

import java.util.List;

/**
 * Common marker for any fake inventory (virtual-block-backed GUI) regardless of
 * its concrete Nukkit parent class.
 *
 * <p>Legacy fake inventories extend {@link FakeInventory}; SAI-compatible
 * variants (e.g. {@link FurnaceFakeInventory}) may need to extend a Nukkit
 * class such as {@code FurnaceInventory} directly, so they implement this
 * interface instead. Listeners and the registered service use this interface
 * to treat all variants uniformly.</p>
 */
public interface FakeInventoryLike {

    /**
     * @return the Nukkit inventory registered in the player's window map.
     */
    default Inventory getNukkitInventory() {
        if (this instanceof Inventory inventory) {
            return inventory;
        }
        throw new IllegalStateException("FakeInventoryLike implementations must also implement Inventory");
    }

    /**
     * @return virtual block positions currently placed for {@code player}, or
     *         {@code null} when the inventory is not open for that player.
     */
    List<BlockVector3> getPosition(Player player);

    /**
     * Dispatch a slot-change event to this inventory's listeners.
     *
     * @return {@code true} when a listener cancelled the change.
     */
    boolean onSlotChange(Player source, SlotChangeAction action);

    /**
     * Close the inventory for every viewer. Default implementation is a no-op;
     * concrete fake inventories override to invoke {@code removeWindow} on each
     * viewer and mark themselves as closed.
     */
    default void close() {
    }
}
