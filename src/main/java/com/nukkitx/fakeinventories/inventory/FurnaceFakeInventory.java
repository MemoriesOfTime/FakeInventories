package com.nukkitx.fakeinventories.inventory;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityFurnace;
import cn.nukkit.inventory.FurnaceInventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FurnaceFakeInventory extends FurnaceInventory implements FakeInventoryLike {
    private static final BlockVector3 ZERO = new BlockVector3(0, 0, 0);

    @Getter
    @Setter
    private String name;

    private final Map<Player, List<BlockVector3>> blockPositions = new ConcurrentHashMap<>();
    private final List<FakeInventoryListener> listeners = new CopyOnWriteArrayList<>();
    private boolean closed = false;
    private String title;

    public FurnaceFakeInventory() {
        this(null);
    }

    public FurnaceFakeInventory(InventoryHolder holder) {
        this(holder, null);
    }

    public FurnaceFakeInventory(InventoryHolder holder, String title) {
        super(null, InventoryType.FURNACE);
        this.title = title == null ? InventoryType.FURNACE.getDefaultTitle() : title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? InventoryType.FURNACE.getDefaultTitle() : title;
    }

    @Override
    public BlockEntityFurnace getHolder() {
        // FurnaceInventory casts this.holder to BlockEntityFurnace; null is a valid cast
        // target and keeps any defensive instanceof checks in Nukkit callers safely false.
        return null;
    }

    @Override
    public void onSlotChange(int index, Item before, boolean send) {
        // Skip FurnaceInventory.onSlotChange which would call getHolder().scheduleUpdate()
        // and NPE; we have no real block entity. Mirroring BaseInventory.onSlotChange is
        // sufficient — fake furnaces are not adjacent to real hoppers so hopper notify
        // (skipped via ContainerInventory.onSlotChange) is also unnecessary.
        if (send) {
            this.sendSlot(index, this.getViewers());
        }
    }

    @Override
    public boolean setItemByPlayer(Player player, int index, Item item, boolean send) {
        // Skip FurnaceInventory.setItemByPlayer's releaseExperience against the result
        // slot which would NPE on the null holder; just write the item directly.
        return setItem(index, item, send);
    }

    @Override
    public void onOpen(Player who) {
        checkForClosed();
        this.viewers.add(who);
        if (FakeInventory.open.putIfAbsent(who, this) != null) {
            throw new IllegalStateException("Inventory was already open");
        }

        List<BlockVector3> blocks = onOpenBlock(who);
        blockPositions.put(who, blocks);

        onFakeOpen(who, blocks);
    }

    protected void onFakeOpen(Player who, List<BlockVector3> blocks) {
        BlockVector3 blockPosition = blocks.isEmpty() ? ZERO : blocks.get(0);

        ContainerOpenPacket containerOpen = new ContainerOpenPacket();
        containerOpen.windowId = who.getWindowId(this);
        containerOpen.type = this.getType().getNetworkType();
        containerOpen.x = blockPosition.x;
        containerOpen.y = blockPosition.y;
        containerOpen.z = blockPosition.z;

        who.dataPacket(containerOpen);

        this.sendContents(who);
    }

    protected List<BlockVector3> onOpenBlock(Player who) {
        BlockVector3 blockPosition = new BlockVector3((int) who.x, ((int) who.y) + 2, (int) who.z);

        placeFurnace(who, blockPosition);

        return Collections.singletonList(blockPosition);
    }

    private void placeFurnace(Player who, BlockVector3 pos) {
        UpdateBlockPacket updateBlock = new UpdateBlockPacket();
        updateBlock.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(who.getGameVersion(), BlockID.FURNACE, 0);
        updateBlock.flags = UpdateBlockPacket.FLAG_ALL_PRIORITY;
        updateBlock.x = pos.x;
        updateBlock.y = pos.y;
        updateBlock.z = pos.z;

        who.dataPacket(updateBlock);

        BlockEntityDataPacket blockEntityData = new BlockEntityDataPacket();
        blockEntityData.x = pos.x;
        blockEntityData.y = pos.y;
        blockEntityData.z = pos.z;
        blockEntityData.namedTag = getNbt(pos, getName());

        who.dataPacket(blockEntityData);
    }

    @Override
    public void onClose(Player who) {
        super.onClose(who);
        FakeInventory.open.remove(who, this);

        List<BlockVector3> blocks = blockPositions.remove(who);
        if (blocks == null) {
            return;
        }

        for (int i = 0, size = blocks.size(); i < size; i++) {
            final int index = i;
            Server.getInstance().getScheduler().scheduleDelayedTask(() -> {
                BlockVector3 blockPosition = blocks.get(index);
                UpdateBlockPacket updateBlock = new UpdateBlockPacket();
                updateBlock.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(who.getGameVersion(), who.getLevel().getFullBlock(blockPosition.x, blockPosition.y, blockPosition.z));
                updateBlock.flags = UpdateBlockPacket.FLAG_ALL_PRIORITY;
                updateBlock.x = blockPosition.getX();
                updateBlock.y = blockPosition.getY();
                updateBlock.z = blockPosition.getZ();

                who.dataPacket(updateBlock);
            }, 2 + i, false);
        }
    }

    @Override
    public List<BlockVector3> getPosition(Player player) {
        checkForClosed();
        return blockPositions.getOrDefault(player, null);
    }

    public void addListener(FakeInventoryListener listener) {
        Preconditions.checkNotNull(listener);
        checkForClosed();
        listeners.add(listener);
    }

    public void removeListener(FakeInventoryListener listener) {
        checkForClosed();
        listeners.remove(listener);
    }

    @Override
    public boolean onSlotChange(Player source, SlotChangeAction action) {
        if (!listeners.isEmpty()) {
            FakeSlotChangeEvent event = new FakeSlotChangeEvent(source, this, action);
            for (FakeInventoryListener listener : listeners) {
                listener.onSlotChange(event);
            }
            return event.isCancelled();
        }
        return false;
    }

    @Override
    public void close() {
        Preconditions.checkState(!closed, "Already closed");
        getViewers().forEach(player -> player.removeWindow(this));
        closed = true;
    }

    private void checkForClosed() {
        Preconditions.checkState(!closed, "Already closed");
    }

    private static byte[] getNbt(BlockVector3 pos, String name) {
        CompoundTag tag = new CompoundTag()
                .putString("id", BlockEntity.FURNACE)
                .putInt("x", pos.x)
                .putInt("y", pos.y)
                .putInt("z", pos.z)
                .putString("CustomName", name == null ? "Furnace" : name);

        try {
            return NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create NBT for furnace");
        }
    }
}