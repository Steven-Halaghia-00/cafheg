package ch.hearc.cafheg;

import ch.hearc.cafheg.domain.allocations.Allocataire;
import ch.hearc.cafheg.domain.allocations.AllocationService;
import ch.hearc.cafheg.domain.allocations.ModificationAllocataireSansChangementException;
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
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AllocataireUpdateIT {

    private static final long UPDATED_ALLOCATAIRE_ID = 1101L;
    private static final String ORIGINAL_NO_AVS = "756.0000.0000.11";
    private static final String ORIGINAL_PRENOM = "Alice";
    private static final String ORIGINAL_NOM = "Original";

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
                getClass().getResourceAsStream("/allocataire-update-dataset.xml"));
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
    void updateAllocataire_GivenChangedNomAndPrenom_ShouldUpdateThemAndKeepNoAvs() {
        Allocataire updated = updateAllocataire(UPDATED_ALLOCATAIRE_ID, "Nouveau", "Prenom");

        Map<String, Object> storedAllocataire = findAllocataire(UPDATED_ALLOCATAIRE_ID);
        assertThat(updated.getNoAVS().getValue()).isEqualTo(ORIGINAL_NO_AVS);
        assertThat(storedAllocataire)
                .containsEntry("nom", "Nouveau")
                .containsEntry("prenom", "Prenom")
                .containsEntry("no_avs", ORIGINAL_NO_AVS);
    }

    @Test
    void updateAllocataire_GivenSameNomAndPrenom_ShouldRefuseUpdateAndKeepDatabaseUnchanged() {
        assertThatThrownBy(() -> updateAllocataire(UPDATED_ALLOCATAIRE_ID, ORIGINAL_NOM, ORIGINAL_PRENOM))
                .isInstanceOf(ModificationAllocataireSansChangementException.class)
                .hasMessageContaining(String.valueOf(UPDATED_ALLOCATAIRE_ID));

        assertThat(findAllocataire(UPDATED_ALLOCATAIRE_ID))
                .containsEntry("nom", ORIGINAL_NOM)
                .containsEntry("prenom", ORIGINAL_PRENOM)
                .containsEntry("no_avs", ORIGINAL_NO_AVS);
    }

    private Allocataire updateAllocataire(long allocataireId, String nom, String prenom) {
        return Database.inTransaction(() -> allocationService.updateAllocataire(allocataireId, nom, prenom));
    }

    private Map<String, Object> findAllocataire(long allocataireId) {
        return jdbcTemplate.queryForMap(
                "SELECT NOM, PRENOM, NO_AVS FROM ALLOCATAIRES WHERE NUMERO = ?",
                allocataireId);
    }
}
