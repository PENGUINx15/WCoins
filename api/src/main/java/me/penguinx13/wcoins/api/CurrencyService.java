package me.penguinx13.wcoins.api;

import java.util.UUID;

public interface CurrencyService {

    long getBalance(UUID playerId);

    boolean setBalance(UUID playerId, long amount);

    boolean addBalance(UUID playerId, long amount);

    boolean takeBalance(UUID playerId, long amount);

    boolean transfer(UUID fromPlayerId, UUID toPlayerId, long amount);
}
