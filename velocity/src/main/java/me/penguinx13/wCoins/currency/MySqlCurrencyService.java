package me.penguinx13.wCoins.currency;

import me.penguinx13.wcoins.api.CurrencyService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class MySqlCurrencyService implements CurrencyService {

    private final DataSource dataSource;

    public MySqlCurrencyService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public long getBalance(UUID playerId) {
        String ensure = "INSERT INTO balances (player_uuid, balance) VALUES (?, 0) ON DUPLICATE KEY UPDATE player_uuid = player_uuid";
        String select = "SELECT balance FROM balances WHERE player_uuid = ?";

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(ensure)) {
                preparedStatement.setString(1, playerId.toString());
                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(select)) {
                preparedStatement.setString(1, playerId.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getLong("balance");
                    }
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to get balance", exception);
        }

        return 0;
    }

    @Override
    public boolean setBalance(UUID playerId, long amount) {
        if (amount < 0) {
            return false;
        }

        String sql = "INSERT INTO balances (player_uuid, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = VALUES(balance)";
        return executeUpdate(sql, playerId, amount);
    }

    @Override
    public boolean addBalance(UUID playerId, long amount) {
        if (amount < 0) {
            return false;
        }

        String sql = "INSERT INTO balances (player_uuid, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = balance + VALUES(balance)";
        return executeUpdate(sql, playerId, amount);
    }

    @Override
    public boolean takeBalance(UUID playerId, long amount) {
        if (amount < 0) {
            return false;
        }

        String sql = "UPDATE balances SET balance = balance - ? WHERE player_uuid = ? AND balance >= ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, amount);
            preparedStatement.setString(2, playerId.toString());
            preparedStatement.setLong(3, amount);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to take balance", exception);
        }
    }

    @Override
    public boolean transfer(UUID fromPlayerId, UUID toPlayerId, long amount) {
        if (amount <= 0 || fromPlayerId.equals(toPlayerId)) {
            return false;
        }

        String ensure = "INSERT INTO balances (player_uuid, balance) VALUES (?, 0) ON DUPLICATE KEY UPDATE player_uuid = player_uuid";
        String withdraw = "UPDATE balances SET balance = balance - ? WHERE player_uuid = ? AND balance >= ?";
        String deposit = "UPDATE balances SET balance = balance + ? WHERE player_uuid = ?";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement preparedStatement = connection.prepareStatement(ensure)) {
                    preparedStatement.setString(1, fromPlayerId.toString());
                    preparedStatement.executeUpdate();
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(ensure)) {
                    preparedStatement.setString(1, toPlayerId.toString());
                    preparedStatement.executeUpdate();
                }

                int withdrawn;
                try (PreparedStatement preparedStatement = connection.prepareStatement(withdraw)) {
                    preparedStatement.setLong(1, amount);
                    preparedStatement.setString(2, fromPlayerId.toString());
                    preparedStatement.setLong(3, amount);
                    withdrawn = preparedStatement.executeUpdate();
                }

                if (withdrawn == 0) {
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(deposit)) {
                    preparedStatement.setLong(1, amount);
                    preparedStatement.setString(2, toPlayerId.toString());
                    preparedStatement.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to transfer balance", exception);
        }
    }

    private boolean executeUpdate(String sql, UUID playerId, long amount) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, playerId.toString());
            preparedStatement.setLong(2, amount);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update balance", exception);
        }
    }
}
