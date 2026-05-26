package ch.hearc.cafheg.infrastructure.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
  private static final Logger logger = LoggerFactory.getLogger(Database.class);

  /** Pool de connections JDBC */
  private static DataSource dataSource;

  /** Connection JDBC active par utilisateur/thread (ThreadLocal) */
  private static final ThreadLocal<Connection> connection = new ThreadLocal<>();

  /**
   * Retourne la transaction active ou throw une Exception si pas de transaction
   * active.
   * @return Connection JDBC active
   */
  static Connection activeJDBCConnection() {
    if (connection.get() == null) {
      throw new RuntimeException("Pas de connection JDBC active");
    }
    return connection.get();
  }

  /**
   * Exécution d'une fonction dans une transaction.
   * @param inTransaction La fonction a exécuter au travers d'une transaction
   * @param <T> Le type du retour de la fonction
   * @return Le résultat de l'exécution de la fonction
   */
  public static <T> T inTransaction(Supplier<T> inTransaction) {
    logger.debug("Starting database transaction");
    try {
      logger.trace("Opening JDBC connection for transaction");
      connection.set(dataSource.getConnection());
      return inTransaction.get();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during database transaction", e);
      throw new RuntimeException(e);
    } finally {
      try {
        logger.trace("Closing JDBC connection for transaction");
        connection.get().close();
      } catch (SQLException e) {
        logger.error("Failed to close JDBC connection after transaction", e);
        throw new RuntimeException(e);
      }
      logger.debug("Database transaction finished");
      connection.remove();
    }
  }

  DataSource dataSource() {
    return dataSource;
  }

  /**
   * Initialisation du pool de connections.
   */
  public void start(String jdbcUrl, String username, String password) {
    logger.info("Initializing datasource");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    config.setMaximumPoolSize(20);
    config.setDriverClassName("org.postgresql.Driver");
    dataSource = new HikariDataSource(config);
    logger.info("Datasource initialized");
  }
}
