package com.nukkitx.fakeinventories;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.inventory.GrindstoneInventory;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import com.nukkitx.fakeinventories.inventory.FakeInventories;
import com.nukkitx.fakeinventories.inventory.FakeInventory;
import com.nukkitx.fakeinventories.inventory.GrindstoneFakeInventory;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FakeInventoriesListener implements Listener {
    private final FakeInventories fakeInventories;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPacketReceive(DataPacketReceiveEvent event) {
        DataPacket packet = event.getPacket();
        if (!(packet instanceof InventoryTransactionPacket)) {
            return;
        }

        Player player = event.getPlayer();
        FakeInventory fakeInventory = fakeInventories.getFakeInventory(player).orElse(null);
        if (!(fakeInventory instanceof GrindstoneFakeInventory)) {
            return;
        }

        InventoryTransactionPacket pk = (InventoryTransactionPacket) packet;

        // 场景 B：砂轮结果操作（SOURCE_TODO + -10/-11/-12），假砂轮不支持研磨逻辑，直接取消
        if (hasGrindstoneTodoAction(pk)) {
            event.setCancelled();
            fakeInventory.sendContents(player);
            return;
        }

        // 场景 A：普通槽位操作，将 UI 槽位重写为动态窗口 ID
        int dynamicWindowId = player.getWindowId(fakeInventory);
        if (dynamicWindowId == -1) {
            return;
        }

        boolean modified = false;
        for (NetworkInventoryAction action : pk.actions) {
            if (action.sourceType == NetworkInventoryAction.SOURCE_CONTAINER
                    && action.windowId == ContainerIds.UI) {
                if (action.inventorySlot == GrindstoneInventory.GRINDSTONE_EQUIPMENT_UI_SLOT) {
                    action.windowId = dynamicWindowId;
                    action.inventorySlot = 0;
                    modified = true;
                } else if (action.inventorySlot == GrindstoneInventory.GRINDSTONE_INGREDIENT_UI_SLOT) {
                    action.windowId = dynamicWindowId;
                    action.inventorySlot = 1;
                    modified = true;
                }
            }
        }

        // 清除 isRepairItemPart 标志，避免进入 Player.java 的修复分支
        if (modified) {
            pk.isRepairItemPart = false;
        }
    }

    private boolean hasGrindstoneTodoAction(InventoryTransactionPacket pk) {
        for (NetworkInventoryAction action : pk.actions) {
            if (action.sourceType == NetworkInventoryAction.SOURCE_TODO) {
                // -10, -11, -12 是砂轮/铁砧结果操作的 windowId
                if (action.windowId == NetworkInventoryAction.SOURCE_TYPE_ANVIL_INPUT
                        || action.windowId == NetworkInventoryAction.SOURCE_TYPE_ANVIL_MATERIAL
                        || action.windowId == NetworkInventoryAction.SOURCE_TYPE_ANVIL_RESULT) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPacketSend(DataPacketSendEvent event) {
        DataPacket packet = event.getPacket();
        if (packet instanceof UpdateBlockPacket) {
            UpdateBlockPacket updateBlock = (UpdateBlockPacket) packet;
            List<BlockVector3> positions = fakeInventories.getFakeInventoryPositions(event.getPlayer());
            if (positions != null) {
                for (BlockVector3 pos : positions) {
                    if (pos.x == updateBlock.x && pos.y == updateBlock.y && pos.z == updateBlock.z) {
                        event.setCancelled();
                        return;
                    }
                }
            }
            ;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTransaction(InventoryTransactionEvent event) {
        Map<FakeInventory, List<SlotChangeAction>> actions = new HashMap<>();
        Player source = event.getTransaction().getSource();
        long creationTime = event.getTransaction().getCreationTime();
        for (InventoryAction action : event.getTransaction().getActions()) {
            if (action instanceof SlotChangeAction) {
                SlotChangeAction slotChange = (SlotChangeAction) action;
                if (slotChange.getInventory() instanceof FakeInventory) {
                    FakeInventory inventory = (FakeInventory) slotChange.getInventory();
                    List<SlotChangeAction> slotChanges = actions.computeIfAbsent(inventory, fakeInventory -> new ArrayList<>());

                    slotChanges.add(slotChange);
                }
            }
        }

        boolean cancel = false;
        for (Map.Entry<FakeInventory, List<SlotChangeAction>> entry : actions.entrySet()) {
            for (SlotChangeAction action : entry.getValue()) {
                if (entry.getKey().onSlotChange(source, action)) {
                    cancel = true;
                }
            }
        }

        if (cancel) {
            event.setCancelled();
        }
    }
}
