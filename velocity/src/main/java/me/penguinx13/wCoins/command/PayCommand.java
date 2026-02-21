package me.penguinx13.wCoins.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.penguinx13.wcoins.api.CurrencyService;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public final class PayCommand implements SimpleCommand {

    private final ProxyServer proxyServer;
    private final Object plugin;
    private final CurrencyService currencyService;

    public PayCommand(ProxyServer proxyServer, Object plugin, CurrencyService currencyService) {
        this.proxyServer = proxyServer;
        this.plugin = plugin;
        this.currencyService = currencyService;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] arguments = invocation.arguments();

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("Команда доступна только игрокам."));
            return;
        }

        if (arguments.length != 2) {
            source.sendMessage(Component.text("Использование: /pay <игрок> <сумма>"));
            return;
        }

        proxyServer.getScheduler().buildTask(plugin, () -> executeInternal(player, arguments)).schedule();
    }

    private void executeInternal(Player sender, String[] arguments) {
        Optional<Player> target = proxyServer.getPlayer(arguments[0]);
        if (target.isEmpty()) {
            sender.sendMessage(Component.text("Игрок должен быть онлайн."));
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(arguments[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(Component.text("Сумма должна быть числом."));
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(Component.text("Сумма должна быть больше 0."));
            return;
        }

        if (sender.getUniqueId().equals(target.get().getUniqueId())) {
            sender.sendMessage(Component.text("Нельзя отправить валюту самому себе."));
            return;
        }

        boolean transferred = currencyService.transfer(sender.getUniqueId(), target.get().getUniqueId(), amount);
        if (!transferred) {
            sender.sendMessage(Component.text("Перевод не выполнен. Проверьте баланс."));
            return;
        }

        sender.sendMessage(Component.text("Вы отправили " + amount + " игроку " + target.get().getUsername()));
        target.get().sendMessage(Component.text("Вы получили " + amount + " от " + sender.getUsername()));
    }
}
