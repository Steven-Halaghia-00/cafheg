package ch.hearc.cafheg.infrastructure.persistence;

import ch.hearc.cafheg.domain.common.Montant;
import ch.hearc.cafheg.domain.versements.VersementAllocation;
import ch.hearc.cafheg.domain.versements.VersementAllocationNaissance;
import ch.hearc.cafheg.domain.versements.VersementParentEnfant;
import ch.hearc.cafheg.domain.versements.VersementParentParMois;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersementMapper extends Mapper {

  private static final Logger logger = LoggerFactory.getLogger(VersementMapper.class);

  private final String QUERY_FIND_ALL_ALLOCATIONS_NAISSANCE = "SELECT V.DATE_VERSEMENT,AN.MONTANT FROM VERSEMENTS V JOIN ALLOCATIONS_NAISSANCE AN ON V.NUMERO=AN.FK_VERSEMENTS";
  private final String QUERY_FIND_ALL_VERSEMENTS = "SELECT V.DATE_VERSEMENT,A.MONTANT FROM VERSEMENTS V JOIN VERSEMENTS_ALLOCATIONS VA ON V.NUMERO=VA.FK_VERSEMENTS JOIN ALLOCATIONS_ENFANTS AE ON AE.NUMERO=VA.FK_ALLOCATIONS_ENFANTS JOIN ALLOCATIONS A ON A.NUMERO=AE.FK_ALLOCATIONS";
  private final String QUERY_FIND_ALL_VERSEMENTS_PARENTS_ENFANTS = "SELECT AL.NUMERO AS PARENT_ID, E.NUMERO AS ENFANT_ID, A.MONTANT FROM VERSEMENTS V JOIN VERSEMENTS_ALLOCATIONS VA ON V.NUMERO=VA.FK_VERSEMENTS JOIN ALLOCATIONS_ENFANTS AE ON AE.NUMERO=VA.FK_ALLOCATIONS_ENFANTS JOIN ALLOCATIONS A ON A.NUMERO=AE.FK_ALLOCATIONS JOIN ALLOCATAIRES AL ON AL.NUMERO=V.FK_ALLOCATAIRES JOIN ENFANTS E ON E.NUMERO=AE.FK_ENFANTS";
  private final String QUERY_FIND_ALL_VERSEMENTS_PARENTS_ENFANTS_PAR_MOIS = "SELECT AL.NUMERO AS PARENT_ID, A.MONTANT, V.DATE_VERSEMENT, V.MOIS_VERSEMENT FROM VERSEMENTS V JOIN VERSEMENTS_ALLOCATIONS VA ON V.NUMERO=VA.FK_VERSEMENTS JOIN ALLOCATIONS_ENFANTS AE ON AE.NUMERO=VA.FK_ALLOCATIONS_ENFANTS JOIN ALLOCATIONS A ON A.NUMERO=AE.FK_ALLOCATIONS JOIN ALLOCATAIRES AL ON AL.NUMERO=V.FK_ALLOCATAIRES JOIN ENFANTS E ON E.NUMERO=AE.FK_ENFANTS";
  private final String QUERY_EXISTS_BY_ALLOCATAIRE_ID = "SELECT 1 FROM VERSEMENTS WHERE FK_ALLOCATAIRES=? LIMIT 1";

  public List<VersementAllocationNaissance> findAllVersementAllocationNaissance() {
    logger.debug("Finding all birth allocation versements");
    Connection connection = activeJDBCConnection();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FIND_ALL_ALLOCATIONS_NAISSANCE);
      ResultSet resultSet = preparedStatement.executeQuery();
      List<VersementAllocationNaissance> versements = new ArrayList<>();
      while (resultSet.next()) {
        logger.trace("Mapping next birth allocation versement row");
        versements.add(
            new VersementAllocationNaissance(new Montant(resultSet.getBigDecimal(2)),
                resultSet.getDate(1).toLocalDate()));

      }
      return versements;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find all birth allocation versements", e);
    }
  }

  public List<VersementAllocation> findAllVersementAllocation() {
    logger.debug("Finding all allocation versements");
    Connection connection = activeJDBCConnection();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FIND_ALL_VERSEMENTS);
      ResultSet resultSet = preparedStatement.executeQuery();
      List<VersementAllocation> versements = new ArrayList<>();
      while (resultSet.next()) {
        logger.trace("Mapping next allocation versement row");
        versements.add(
            new VersementAllocation(new Montant(resultSet.getBigDecimal(2)),
                resultSet.getDate(1).toLocalDate()));

      }
      return versements;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find all allocation versements", e);
    }
  }

  public List<VersementParentEnfant> findVersementParentEnfant() {
    logger.debug("Finding parent/enfant versements");
    Connection connection = activeJDBCConnection();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FIND_ALL_VERSEMENTS_PARENTS_ENFANTS);
      ResultSet resultSet = preparedStatement.executeQuery();
      List<VersementParentEnfant> versements = new ArrayList<>();
      logger.trace("Iterating parent/enfant versement rows");
      while (resultSet.next()) {
        versements.add(
            new VersementParentEnfant(resultSet.getLong(1), resultSet.getLong(2),
                new Montant(resultSet.getBigDecimal(3))));

      }
      return versements;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find parent/enfant versements", e);
    }
  }

  public List<VersementParentParMois> findVersementParentEnfantParMois() {
    logger.debug("Finding parent/enfant versements by month");
    Connection connection = activeJDBCConnection();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_FIND_ALL_VERSEMENTS_PARENTS_ENFANTS_PAR_MOIS);
      ResultSet resultSet = preparedStatement.executeQuery();
      List<VersementParentParMois> versements = new ArrayList<>();
      while (resultSet.next()) {
        logger.trace("Mapping next parent/enfant monthly versement row");
        versements.add(
            new VersementParentParMois(resultSet.getLong(1),
                new Montant(resultSet.getBigDecimal(2)),
                resultSet.getDate(3).toLocalDate(), resultSet.getDate(4).toLocalDate()));

      }
      return versements;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find parent/enfant versements by month", e);
    }
  }

  public boolean existsByAllocataireId(long allocataireId) {
    logger.debug("Checking versement existence for allocataire {}", allocataireId);
    Connection connection = activeJDBCConnection();
    try {
      PreparedStatement preparedStatement = connection.prepareStatement(QUERY_EXISTS_BY_ALLOCATAIRE_ID);
      preparedStatement.setLong(1, allocataireId);
      ResultSet resultSet = preparedStatement.executeQuery();
      return resultSet.next();
    } catch (SQLException e) {
      throw new RuntimeException(
          "Failed to check versement existence for allocataire " + allocataireId, e);
    }
  }
}
