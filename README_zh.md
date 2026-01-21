# Fake Inventories

<p align="center">
  <a href="/README.md"><img alt="English" src="https://img.shields.io/badge/English-d9d9d9"></a>
  <a href="/README_zh.md"><img alt="中文" src="https://img.shields.io/badge/中文-d9d9d9"></a>
</p>

------

一个 Nukkit/MOT 服务器插件，用于轻松创建玩家可交互的虚拟物品栏（GUI 界面）。

##### [下载地址](https://motci.cn/job/FakeInventories/)

## 功能特性

- 支持单箱子（27格）、双箱子（54格）、漏斗（5格）三种物品栏类型
- 支持自定义标题
- 支持监听玩家点击事件
- 支持取消物品操作（实现纯 GUI 菜单）
- 通过 ServiceManager 注册服务，方便其他插件调用

## 使用方法

### 1. 在 plugin.yml 中添加依赖

```yaml
depend:
  - FakeInventories
```

### 2. 获取服务

```java
public void onEnable() {
    RegisteredServiceProvider<FakeInventories> provider =
        getServer().getServiceManager().getProvider(FakeInventories.class);

    if (provider == null || provider.getProvider() == null) {
        getLogger().error("未找到 FakeInventories 插件！");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }

    FakeInventories fakeInventories = provider.getProvider();
}
```

### 3. 创建虚拟物品栏

**单箱子（27格）：**
```java
ChestFakeInventory inventory = new ChestFakeInventory();
// 或使用自定义标题
ChestFakeInventory inventory = new ChestFakeInventory(null, "自定义标题");
```

**双箱子（54格）：**
```java
DoubleChestFakeInventory inventory = new DoubleChestFakeInventory();
// 或使用自定义标题
DoubleChestFakeInventory inventory = new DoubleChestFakeInventory(null, "自定义标题");
```

**漏斗（5格）：**
```java
HopperFakeInventory inventory = new HopperFakeInventory();
// 或使用自定义标题
HopperFakeInventory inventory = new HopperFakeInventory(null, "自定义标题");
```

### 4. 设置物品

```java
inventory.setItem(0, Item.get(Item.DIAMOND));
inventory.setItem(1, Item.get(Item.GOLD_INGOT, 0, 64));
```

### 5. 添加事件监听器

```java
inventory.addListener(event -> {
    Player player = event.getPlayer();
    SlotChangeAction action = event.getAction();

    int slot = action.getSlot();
    Item sourceItem = action.getSourceItem();  // 变更前的物品
    Item targetItem = action.getTargetItem();  // 变更后的物品

    // 取消操作（阻止物品修改）
    event.setCancelled();

    // 处理点击逻辑
    player.sendMessage("你点击了第 " + slot + " 格");
});
```

### 6. 显示给玩家

```java
player.addWindow(inventory);
```

### 完整示例

```java
public class MyPlugin extends PluginBase {

    public void openMenu(Player player) {
        ChestFakeInventory menu = new ChestFakeInventory(null, "我的菜单");

        // 设置菜单物品
        menu.setItem(0, Item.get(Item.DIAMOND).setCustomName("选项 1"));
        menu.setItem(1, Item.get(Item.EMERALD).setCustomName("选项 2"));
        menu.setItem(2, Item.get(Item.GOLD_INGOT).setCustomName("选项 3"));

        // 处理点击事件
        menu.addListener(event -> {
            event.setCancelled(); // 阻止拿取物品

            Player p = event.getPlayer();
            int slot = event.getAction().getSlot();

            switch (slot) {
                case 0:
                    p.sendMessage("已选择选项 1");
                    break;
                case 1:
                    p.sendMessage("已选择选项 2");
                    break;
                case 2:
                    p.sendMessage("已选择选项 3");
                    break;
            }

            p.removeWindow(event.getInventory()); // 关闭菜单
        });

        player.addWindow(menu);
    }
}
```

## Maven 依赖

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