# Fake Inventories

Easily create fake inventories that players can interact with.

##### [Download](https://motci.cn/job/FakeInventories/)

## Usage

```java
    public void onEnable() {
        RegisteredServiceProvider<FakeInventories> provider = getServer().getServiceManager().getProvider(FakeInventories.class);
        
        if (provider == null || provider.getProvider() == null) {
            this.getServer().getPluginManager().disablePlugin(this);
        }
        
        ...
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

## Todo

- Add Hopper inventory
