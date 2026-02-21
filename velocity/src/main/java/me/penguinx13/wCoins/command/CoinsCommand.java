package me.penguinx13.wCoins.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.penguinx13.wcoins.api.CurrencyService;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public final class CoinsCommand implements SimpleCommand {

    private static final String ADMIN_PERMISSION = "wcoins.admin";

    private final ProxyServer proxyServer;
    private final Object plugin;
    private final CurrencyService currencyService;

    public CoinsCommand(ProxyServer proxyServer, Object plugin, CurrencyService currencyService) {
        this.proxyServer = proxyServer;
        this.plugin = plugin;
        this.currencyService = currencyService;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] arguments = invocation.arguments();

        proxyServer.getScheduler().buildTask(plugin, () -> executeInternal(source, arguments)).schedule();
    }

    private void executeInternal(CommandSource source, String[] arguments) {
        if (arguments.length == 0) {
            if (!(source instanceof Player player)) {
                source.sendMessage(Component.text("Использование: /coins <игрок>|set/add/take <игрок> <сумма>"));
                return;
            }

            long balance = currencyService.getBalance(player.getUniqueId());
            source.sendMessage(Component.text("Ваш баланс: " + balance));
            return;
        }

        if (arguments.length == 1) {
            Optional<Player> target = proxyServer.getPlayer(arguments[0]);
            if (target.isEmpty()) {
                source.sendMessage(Component.text("Игрок должен быть онлайн."));
                return;
            }

            long balance = currencyService.getBalance(target.get().getUniqueId());
            source.sendMessage(Component.text("Баланс " + target.get().getUsername() + ": " + balance));
            return;
        }

        if (arguments.length != 3) {
            source.sendMessage(Component.text("Использование: /coins set|add|take <игрок> <сумма>"));
            return;
        }

        if (!source.hasPermission(ADMIN_PERMISSION)) {
            source.sendMessage(Component.text("Недостаточно прав."));
            return;
        }

        String action = arguments[0].toLowerCase();
        Optional<Player> target = proxyServer.getPlayer(arguments[1]);
        if (target.isEmpty()) {
            source.sendMessage(Component.text("Игрок должен быть онлайн."));
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(arguments[2]);
        } catch (NumberFormatException exception) {
            source.sendMessage(Component.text("Сумма должна быть числом."));
            return;
        }

        if (amount < 0) {
            source.sendMessage(Component.text("Сумма должна быть неотрицательной."));
            return;
        }

        boolean success;
        switch (action) {
            case "set" -> success = currencyService.setBalance(target.get().getUniqueId(), amount);
            case "add" -> success = currencyService.addBalance(target.get().getUniqueId(), amount);
            case "take" -> success = currencyService.takeBalance(target.get().getUniqueId(), amount);
            default -> {
                source.sendMessage(Component.text("Неизвестное действие. Используйте set/add/take."));
                return;
            }
        }

        if (!success) {
            source.sendMessage(Component.text("Операция не выполнена."));
            return;
        }

        long newBalance = currencyService.getBalance(target.get().getUniqueId());
        source.sendMessage(Component.text("Готово. Новый баланс " + target.get().getUsername() + ": " + newBalance));
        target.get().sendMessage(Component.text("Ваш баланс обновлен. Теперь: " + newBalance));
    }
}
