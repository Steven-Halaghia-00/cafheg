package ch.hearc.cafheg.infrastructure.persistence;

import ch.hearc.cafheg.domain.allocations.Allocation;
import ch.hearc.cafheg.domain.allocations.Canton;
import ch.hearc.cafheg.domain.common.Montant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllocationMapper extends Mapper {

  private static final Logger logger = LoggerFactory.getLogger(AllocationMapper.class);

  private static final String QUERY_FIND_ALL = "SELECT * FROM ALLOCATIONS";

  public List<Allocation> findAll() {
    logger.debug("Finding all allocations");

    Connection connection = activeJDBCConnection();
    try {
      logger.debug("Executing SQL: {}", QUERY_FIND_ALL);
      PreparedStatement preparedStatement = connection
          .prepareStatement(QUERY_FIND_ALL);
      ResultSet resultSet = preparedStatement.executeQuery();
      List<Allocation> allocations = new ArrayList<>();
      while (resultSet.next()) {
        logger.trace("Mapping next allocation row");
        allocations.add(
            new Allocation(new Montant(resultSet.getBigDecimal(2)),
                Canton.fromValue(resultSet.getString(3)), resultSet.getDate(4).toLocalDate(),
                resultSet.getDate(5) != null ? resultSet.getDate(5).toLocalDate() : null));
      }
      return allocations;
    } catch (SQLException e) {
      logger.error("Failed to find all allocations", e);
      throw new RuntimeException(e);
    }

  }
}
