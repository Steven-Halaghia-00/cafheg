package ch.hearc.cafheg.infrastructure.persistence;

import ch.hearc.cafheg.domain.allocations.AllocataireIntrouvableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocataireMapperTest {

  @Test
  void findById_GivenUnknownAllocataire_ShouldThrowBeforeReadingColumns() throws SQLException {
    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);
    AllocataireMapper mapper = new TestAllocataireMapper(connection);

    Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false);

    assertThatThrownBy(() -> mapper.findById(99L))
        .isInstanceOf(AllocataireIntrouvableException.class)
        .hasMessageContaining("99");

    Mockito.verify(resultSet, Mockito.never()).getString(Mockito.anyInt());
  }

  private static class TestAllocataireMapper extends AllocataireMapper {

    private final Connection connection;

    private TestAllocataireMapper(Connection connection) {
      this.connection = connection;
    }

    @Override
    protected Connection activeJDBCConnection() {
      return connection;
    }
  }
}
