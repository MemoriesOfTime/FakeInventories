# Fake Inventories

<p align="center">
  <a href="/README.md"><img alt="English" src="https://img.shields.io/badge/English-d9d9d9"></a>
  <a href="/README_zh.md"><img alt="中文" src="https://img.shields.io/badge/中文-d9d9d9"></a>
</p>

------

Easily create fake inventories that players can interact with.

##### [Download](https://motci.cn/job/FakeInventories/)

## Usage

### 1. Add Dependency in plugin.yml

```yaml
depend:
  - FakeInventories
```

### 2. Get Service

```java
public void onEnable() {
    RegisteredServiceProvider<FakeInventories> provider =
        getServer().getServiceManager().getProvider(FakeInventories.class);

    if (provider == null || provider.getProvider() == null) {
        getLogger().error("FakeInventories not found!");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }

    FakeInventories fakeInventories = provider.getProvider();
}
```

### 3. Create Fake Inventory

**Single Chest (27 slots):**
```java
ChestFakeInventory inventory = new ChestFakeInventory();
// or with custom title
ChestFakeInventory inventory = new ChestFakeInventory(null, "Custom Title");
```

**Double Chest (54 slots):**
```java
DoubleChestFakeInventory inventory = new DoubleChestFakeInventory();
// or with custom title
DoubleChestFakeInventory inventory = new DoubleChestFakeInventory(null, "Custom Title");
```

**Hopper (5 slots):**
```java
HopperFakeInventory inventory = new HopperFakeInventory();
// or with custom title
HopperFakeInventory inventory = new HopperFakeInventory(null, "Custom Title");
```

### 4. Set Items

```java
inventory.setItem(0, Item.get(Item.DIAMOND));
inventory.setItem(1, Item.get(Item.GOLD_INGOT, 0, 64));
```

### 5. Add Event Listener

```java
inventory.addListener(event -> {
    Player player = event.getPlayer();
    SlotChangeAction action = event.getAction();

    int slot = action.getSlot();
    Item sourceItem = action.getSourceItem();  // item before change
    Item targetItem = action.getTargetItem();  // item after change

    // Cancel the action (prevent item modification)
    event.setCancelled();

    // Handle click logic
    player.sendMessage("You clicked slot " + slot);
});
```

### 6. Show to Player

```java
player.addWindow(inventory);
```

### Complete Example

```java
public class MyPlugin extends PluginBase {

    public void openMenu(Player player) {
        ChestFakeInventory menu = new ChestFakeInventory(null, "My Menu");

        // Set menu items
        menu.setItem(0, Item.get(Item.DIAMOND).setCustomName("Option 1"));
        menu.setItem(1, Item.get(Item.EMERALD).setCustomName("Option 2"));
        menu.setItem(2, Item.get(Item.GOLD_INGOT).setCustomName("Option 3"));

        // Handle clicks
        menu.addListener(event -> {
            event.setCancelled(); // Prevent taking items

            Player p = event.getPlayer();
            int slot = event.getAction().getSlot();

            switch (slot) {
                case 0:
                    p.sendMessage("Selected Option 1");
                    break;
                case 1:
                    p.sendMessage("Selected Option 2");
                    break;
                case 2:
                    p.sendMessage("Selected Option 3");
                    break;
            }

            p.removeWindow(event.getInventory()); // Close menu
        });

        player.addWindow(menu);
    }
}
``` 

## Maven Dependency

```xml
    <repositories>
        <repository>
            <id>repo-lanink-cn</id>
            <url>https://repo.lanink.cn/repository/maven-public/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.nukkitx</groupId>
            <artifactId>fakeinventories</artifactId>
            <version>1.0.3-MOT-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
```
