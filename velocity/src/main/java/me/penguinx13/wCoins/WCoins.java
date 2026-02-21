package me.penguinx13.wCoins;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import javax.inject.Inject;
import java.nio.file.Path;

@Plugin(id = "wcoins", name = "WCoins", version = "1.0.0", authors = {"PENGUINx13"})
public class WCoins {

    private final Path dataDirectory;

    @Inject
    public WCoins(@DataDirectory Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public void onEnable() {

    }

    public void onDisable() {

    }
}