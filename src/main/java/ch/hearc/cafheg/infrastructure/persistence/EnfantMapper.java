package ch.hearc.cafheg.infrastructure.persistence;

import ch.hearc.cafheg.domain.allocations.NoAVS;
import ch.hearc.cafheg.domain.versements.Enfant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnfantMapper extends Mapper {

  private static final Logger logger = LoggerFactory.getLogger(EnfantMapper.class);

  private final String QUERY_FIND_ENFANT_BY_ID = "SELECT NO_AVS, NOM, PRENOM FROM ENFANTS WHERE NUMERO=?";

  public Enfant findById(long id) {
    logger.debug("Finding enfant by id {}", id);
    Connection connection = activeJDBCConnection();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FIND_ENFANT_BY_ID);
      preparedStatement.setLong(1, id);
      ResultSet resultSet = preparedStatement.executeQuery();
      logger.trace("Moving to enfant result row");
      resultSet.next();
      return new Enfant(new NoAVS(resultSet.getString(1)),
          resultSet.getString(2), resultSet.getString(3));
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find enfant by id " + id, e);
    }
  }

}
