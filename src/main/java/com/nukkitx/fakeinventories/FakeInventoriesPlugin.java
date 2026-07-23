package com.nukkitx.fakeinventories;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.service.ServicePriority;
import com.nukkitx.fakeinventories.inventory.FakeInventories;

public class FakeInventoriesPlugin extends PluginBase {
    private static final String SAI_COMPATIBILITY_API = "cn.nukkit.event.inventory.ItemStackRequestActionEvent";

    private final FakeInventories fakeInventories = new FakeInventories();

    @Override
    public void onEnable() {
        if (!hasSaiCompatibilityApi(getClass().getClassLoader())) {
            getLogger().warning("This Nukkit-MOT build lacks the server-authoritative inventory compatibility API; fake inventory click listeners may not work for modern clients.");
        }

        // register service
        getServer().getServiceManager().register(FakeInventories.class, fakeInventories, this, ServicePriority.HIGHEST);

        // register listener
        getServer().getPluginManager().registerEvents(new FakeInventoriesListener(fakeInventories), this);
    }

    @Override
    public void onDisable() {
        // deregister service
        getServer().getServiceManager().cancel(this);
    }

    static boolean hasSaiCompatibilityApi(ClassLoader classLoader) {
        try {
            Class.forName(SAI_COMPATIBILITY_API, false, classLoader);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
