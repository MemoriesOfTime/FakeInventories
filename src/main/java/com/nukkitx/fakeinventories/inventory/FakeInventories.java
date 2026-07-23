package com.nukkitx.fakeinventories.inventory;

import cn.nukkit.Player;
import cn.nukkit.math.BlockVector3;

import java.util.List;
import java.util.Optional;

public class FakeInventories {
    public List<BlockVector3> getFakeInventoryPositions(Player player) {
        FakeInventoryLike inventory = FakeInventory.open.get(player);
        if (inventory == null) {
            return null;
        }
        return inventory.getPosition(player);
    }

    /**
     * @return the legacy {@link FakeInventory} view of the currently open fake
     *         inventory, or empty. Returns empty for variants that do not extend
     *         {@link FakeInventory} (e.g. {@link FurnaceFakeInventory}); use
     *         {@link #getFakeInventoryLike(Player)} for those.
     */
    public Optional<FakeInventory> getFakeInventory(Player player) {
        FakeInventoryLike inv = FakeInventory.open.get(player);
        return Optional.ofNullable(inv instanceof FakeInventory ? (FakeInventory) inv : null);
    }

    /**
     * @return the currently open fake inventory regardless of concrete parent
     *         class, or empty. Prefer this over {@link #getFakeInventory(Player)}
     *         when SAI-compatible variants must be handled.
     */
    public Optional<FakeInventoryLike> getFakeInventoryLike(Player player) {
        return Optional.ofNullable(FakeInventory.open.get(player));
    }

    /**
     * @return Chest inventory
     * @deprecated Use {@link ChestFakeInventory} constructor
     */
    @Deprecated
    public ChestFakeInventory createChestInventory() {
        return new ChestFakeInventory();
    }

    /**
     * @return Double chest inventory
     * @deprecated Use {@link DoubleChestFakeInventory} constructor
     */
    @Deprecated
    public DoubleChestFakeInventory createDoubleChestInventory() {
        return new DoubleChestFakeInventory();
    }

    public void removeFakeInventory(FakeInventory inventory) {
        if (inventory != null) {
            inventory.close();
        }
    }

    public void removeFakeInventory(FakeInventoryLike inventory) {
        if (inventory != null) {
            inventory.close();
        }
    }
}
