package me.penguinx13.wCoins;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.penguinx13.wCoins.command.CoinsCommand;
import me.penguinx13.wCoins.command.PayCommand;
import me.penguinx13.wCoins.config.PluginConfig;
import me.penguinx13.wCoins.currency.DatabaseManager;
import me.penguinx13.wCoins.currency.MySqlCurrencyService;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "wcoins", name = "WCoins", version = "1.0.0", authors = {"PENGUINx13"})
public class WCoins {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;

    private final DatabaseManager databaseManager = new DatabaseManager();

    @Inject
    public WCoins(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            PluginConfig config = PluginConfig.load(dataDirectory);
            databaseManager.connect(config);
            databaseManager.initializeSchema();

            MySqlCurrencyService currencyService = new MySqlCurrencyService(databaseManager.getDataSource());

            CommandMeta coinsMeta = proxyServer.getCommandManager().metaBuilder("coins")
                    .aliases("money", "balance")
                    .plugin(this)
                    .build();
            proxyServer.getCommandManager().register(coinsMeta, new CoinsCommand(proxyServer, this, currencyService));

            CommandMeta payMeta = proxyServer.getCommandManager().metaBuilder("pay")
                    .plugin(this)
                    .build();
            proxyServer.getCommandManager().register(payMeta, new PayCommand(proxyServer, this, currencyService));

            logger.info("WCoins включен. Конфиг: {}", config.getConfigPath());
        } catch (IOException exception) {
            logger.error("Не удалось загрузить конфиг WCoins", exception);
        } catch (Exception exception) {
            logger.error("Не удалось инициализировать WCoins", exception);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        databaseManager.close();
        logger.info("WCoins выключен.");
    }
}
