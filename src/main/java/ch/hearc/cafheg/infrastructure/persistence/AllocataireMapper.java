package ch.hearc.cafheg.infrastructure.persistence;

import ch.hearc.cafheg.domain.allocations.Allocataire;
import ch.hearc.cafheg.domain.allocations.AllocataireIntrouvableException;
import ch.hearc.cafheg.domain.allocations.NoAVS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllocataireMapper extends Mapper {

  private static final Logger logger = LoggerFactory.getLogger(AllocataireMapper.class);

  private static final String QUERY_FIND_ALL = "SELECT NOM,PRENOM,NO_AVS FROM ALLOCATAIRES";
  private static final String QUERY_FIND_WHERE_NOM_LIKE = "SELECT NOM,PRENOM,NO_AVS FROM ALLOCATAIRES WHERE NOM LIKE ?";
  private static final String QUERY_FIND_WHERE_NUMERO = "SELECT NO_AVS, NOM, PRENOM FROM ALLOCATAIRES WHERE NUMERO=?";
  private static final String QUERY_EXISTS_BY_ID = "SELECT 1 FROM ALLOCATAIRES WHERE NUMERO=?";
  private static final String QUERY_DELETE_BY_ID = "DELETE FROM ALLOCATAIRES WHERE NUMERO=?";
  private static final String QUERY_UPDATE_NOM_PRENOM = "UPDATE ALLOCATAIRES SET NOM=?, PRENOM=? WHERE NUMERO=?";

  public List<Allocataire> findAll(String likeNom) {
    logger.debug("Finding allocataires with name filter {}", likeNom);
    Connection connection = activeJDBCConnection();
    try {
      PreparedStatement preparedStatement;
      if (likeNom == null) {
        logger.debug("Executing SQL: {}", QUERY_FIND_ALL);
        preparedStatement = connection
            .prepareStatement(QUERY_FIND_ALL);
      } else {

        logger.debug("Executing SQL: {}", QUERY_FIND_WHERE_NOM_LIKE);
        preparedStatement = connection
            .prepareStatement(QUERY_FIND_WHERE_NOM_LIKE);
        preparedStatement.setString(1, likeNom + "%");
      }
      logger.trace("Allocating allocataire result list");
      List<Allocataire> allocataires = new ArrayList<>();

      logger.debug("Executing allocataire search query");
      try (ResultSet resultSet = preparedStatement.executeQuery()) {

        logger.debug("Mapping allocataires from result set");
        while (resultSet.next()) {
          logger.trace("Mapping next allocataire row");
          allocataires
              .add(new Allocataire(new NoAVS(resultSet.getString(3)), resultSet.getString(2),
                  resultSet.getString(1)));
        }
      }
      logger.debug("Found {} allocataires", allocataires.size());
      return allocataires;
    } catch (SQLException e) {
      logger.error("Failed to find allocataires with name filter {}", likeNom, e);
      throw new RuntimeException(e);
    }
  }

  public Allocataire findById(long id) {
    logger.debug("Finding allocataire by id {}", id);
    Connection connection = activeJDBCConnection();
    try {
      logger.debug("Executing SQL: {}", QUERY_FIND_WHERE_NUMERO);
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FIND_WHERE_NUMERO);
      preparedStatement.setLong(1, id);
      ResultSet resultSet = preparedStatement.executeQuery();
      logger.trace("Moving to allocataire result row");
      if (!resultSet.next()) {
        logger.debug("Allocataire {} not found", id);
        throw new AllocataireIntrouvableException(id);
      }
      logger.debug("Mapping allocataire {}", id);
      return new Allocataire(new NoAVS(resultSet.getString(1)),
          resultSet.getString(2), resultSet.getString(3));
    } catch (SQLException e) {
      logger.error("Failed to find allocataire by id {}", id, e);
      throw new RuntimeException(e);
    }
  }

  public boolean existsById(long id) {
    logger.debug("Checking allocataire existence by id {}", id);
    Connection connection = activeJDBCConnection();
    try {
      logger.debug("Executing SQL: {}", QUERY_EXISTS_BY_ID);
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_EXISTS_BY_ID);
      preparedStatement.setLong(1, id);
      ResultSet resultSet = preparedStatement.executeQuery();
      return resultSet.next();
    } catch (SQLException e) {
      logger.error("Failed to check allocataire existence by id {}", id, e);
      throw new RuntimeException(e);
    }
  }

  public void deleteById(long id) {
    logger.debug("Deleting allocataire by id {}", id);
    Connection connection = activeJDBCConnection();
    try {
      logger.debug("Executing SQL: {}", QUERY_DELETE_BY_ID);
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_DELETE_BY_ID);
      preparedStatement.setLong(1, id);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to delete allocataire by id {}", id, e);
      throw new RuntimeException(e);
    }
  }

  public void updateNomPrenom(long id, String nom, String prenom) {
    logger.debug("Updating allocataire name by id {}", id);
    Connection connection = activeJDBCConnection();
    try {
      logger.debug("Executing SQL: {}", QUERY_UPDATE_NOM_PRENOM);
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_UPDATE_NOM_PRENOM);
      preparedStatement.setString(1, nom);
      preparedStatement.setString(2, prenom);
      preparedStatement.setLong(3, id);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to update allocataire name by id {}", id, e);
      throw new RuntimeException(e);
    }
  }
}
