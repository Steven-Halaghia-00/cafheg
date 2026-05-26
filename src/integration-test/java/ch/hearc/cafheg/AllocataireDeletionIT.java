package ch.hearc.cafheg;

import ch.hearc.cafheg.domain.allocations.AllocationService;
import ch.hearc.cafheg.domain.allocations.SuppressionAllocataireInterditeException;
import ch.hearc.cafheg.infrastructure.persistence.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistence.AllocationMapper;
import ch.hearc.cafheg.infrastructure.persistence.Database;
import ch.hearc.cafheg.infrastructure.persistence.VersementMapper;
import com.zaxxer.hikari.HikariDataSource;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AllocataireDeletionIT {

    private static final long DELETABLE_ALLOCATAIRE_ID = 1001L;
    private static final long PROTECTED_ALLOCATAIRE_ID = 1002L;

    @Autowired
    private DataSource dataSource;

    private AllocationService allocationService;
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void setUpService() throws Exception {
        HikariDataSource hikariDataSource = dataSource.unwrap(HikariDataSource.class);
        new Database().start(
                hikariDataSource.getJdbcUrl(),
                hikariDataSource.getUsername(),
                hikariDataSource.getPassword());

        allocationService = new AllocationService(
                new AllocataireMapper(),
                new AllocationMapper(),
                new VersementMapper());
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @BeforeEach
    void resetDatabaseWithDbUnit() throws Exception {
        try (InputStream dataset = Objects.requireNonNull(
                getClass().getResourceAsStream("/allocataire-delete-dataset.xml"));
             Connection connection = dataSource.getConnection()) {
            IDatabaseConnection dbUnitConnection = new DatabaseConnection(connection, "public");
            dbUnitConnection.getConfig().setProperty(
                    DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                    new PostgresqlDataTypeFactory());

            IDataSet dataSet = new FlatXmlDataSetBuilder().build(dataset);
            DatabaseOperation.CLEAN_INSERT.execute(dbUnitConnection, dataSet);
        }
    }

    @Test
    void deleteAllocataire_GivenExistingAllocataireWithoutVersement_ShouldDeleteAllocataire() {
        deleteAllocataire(DELETABLE_ALLOCATAIRE_ID);

        assertThat(countAllocataires(DELETABLE_ALLOCATAIRE_ID)).isZero();
    }

    @Test
    void deleteAllocataire_GivenExistingAllocataireWithVersement_ShouldRefuseDeletion() {
        assertThatThrownBy(() -> deleteAllocataire(PROTECTED_ALLOCATAIRE_ID))
                .isInstanceOf(SuppressionAllocataireInterditeException.class)
                .hasMessageContaining(String.valueOf(PROTECTED_ALLOCATAIRE_ID));

        assertThat(countAllocataires(PROTECTED_ALLOCATAIRE_ID)).isOne();
        assertThat(countVersements(PROTECTED_ALLOCATAIRE_ID)).isOne();
    }

    private void deleteAllocataire(long allocataireId) {
        Database.inTransaction(() -> {
            allocationService.deleteAllocataire(allocataireId);
            return null;
        });
    }

    private int countAllocataires(long allocataireId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ALLOCATAIRES WHERE NUMERO = ?",
                Integer.class,
                allocataireId);
    }

    private int countVersements(long allocataireId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM VERSEMENTS WHERE FK_ALLOCATAIRES = ?",
                Integer.class,
                allocataireId);
    }
}
