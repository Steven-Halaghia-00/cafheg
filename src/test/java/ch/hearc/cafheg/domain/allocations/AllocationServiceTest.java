package ch.hearc.cafheg.domain.allocations;

import ch.hearc.cafheg.domain.common.Montant;
import ch.hearc.cafheg.infrastructure.persistence.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistence.AllocationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class AllocationServiceTest {

  private AllocationService allocationService;

  private AllocataireMapper allocataireMapper;
  private AllocationMapper allocationMapper;

  @BeforeEach
  void setUp() {
    allocataireMapper = Mockito.mock(AllocataireMapper.class);
    allocationMapper = Mockito.mock(AllocationMapper.class);

    allocationService = new AllocationService(allocataireMapper, allocationMapper);
  }

  @Test
  void findAllAllocataires_GivenEmptyAllocataires_ShouldBeEmpty() {
    Mockito.when(allocataireMapper.findAll("Geiser")).thenReturn(Collections.emptyList());
    List<Allocataire> all = allocationService.findAllAllocataires("Geiser");
    assertThat(all).isEmpty();
  }

  @Test
  void findAllAllocataires_Given2Geiser_ShouldBe2() {
    Mockito.when(allocataireMapper.findAll("Geiser"))
        .thenReturn(Arrays.asList(new Allocataire(new NoAVS("1000-2000"), "Geiser", "Arnaud"),
            new Allocataire(new NoAVS("1000-2001"), "Geiser", "Aurélie")));
    List<Allocataire> all = allocationService.findAllAllocataires("Geiser");
    assertAll(() -> assertThat(all.size()).isEqualTo(2),
        () -> assertThat(all.get(0).getNoAVS()).isEqualTo(new NoAVS("1000-2000")),
        () -> assertThat(all.get(0).getNom()).isEqualTo("Geiser"),
        () -> assertThat(all.get(0).getPrenom()).isEqualTo("Arnaud"),
        () -> assertThat(all.get(1).getNoAVS()).isEqualTo(new NoAVS("1000-2001")),
        () -> assertThat(all.get(1).getNom()).isEqualTo("Geiser"),
        () -> assertThat(all.get(1).getPrenom()).isEqualTo("Aurélie"));
  }

  @Test
  void findAllocationsActuelles() {
    Mockito.when(allocationMapper.findAll())
        .thenReturn(Arrays.asList(new Allocation(new Montant(new BigDecimal(1000)), Canton.NE,
                                                 LocalDate.now(), null), new Allocation(new Montant(new BigDecimal(2000)), Canton.FR,
            LocalDate.now(), null)));
    List<Allocation> all = allocationService.findAllocationsActuelles();
    assertAll(() -> assertThat(all.size()).isEqualTo(2),
        () -> assertThat(all.get(0).getMontant()).isEqualTo(new Montant(new BigDecimal(1000))),
        () -> assertThat(all.get(0).getCanton()).isEqualTo(Canton.NE),
        () -> assertThat(all.get(0).getDebut()).isEqualTo(LocalDate.now()),
        () -> assertThat(all.get(0).getFin()).isNull(),
        () -> assertThat(all.get(1).getMontant()).isEqualTo(new Montant(new BigDecimal(2000))),
        () -> assertThat(all.get(1).getCanton()).isEqualTo(Canton.FR),
        () -> assertThat(all.get(1).getDebut()).isEqualTo(LocalDate.now()),
        () -> assertThat(all.get(1).getFin()).isNull());
  }

  @Test
  void getParentDroitAllocation_CaseA_GivenOnlyParent1HasLucrativeActivity_ShouldReturnParent1() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, false, "Neuchatel", Canton.BE, StatutProfessionnel.INDEPENDANT, 1000),
        parent(false, true, "Bienne", Canton.NE, StatutProfessionnel.SALARIE, 9000),
        "Bienne",
        false,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent1());
  }

  @Test
  void getParentDroitAllocation_CaseA_GivenOnlyParent2HasLucrativeActivity_ShouldReturnParent2() {
    ParentDroitAllocationParameters parameters = decision(
        parent(false, true, "Neuchatel", Canton.NE, StatutProfessionnel.SALARIE, 9000),
        parent(true, false, "Bienne", Canton.BE, StatutProfessionnel.INDEPENDANT, 1000),
        "Neuchatel",
        false,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_CaseB_GivenOnlyParent1HasParentalAuthority_ShouldReturnParent1() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, true, "Neuchatel", Canton.BE, StatutProfessionnel.INDEPENDANT, 1000),
        parent(true, false, "Bienne", Canton.FR, StatutProfessionnel.SALARIE, 9000),
        "Bienne",
        false,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent1());
  }

  @Test
  void getParentDroitAllocation_CaseB_GivenOnlyParent2HasParentalAuthority_ShouldReturnParent2() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, false, "Neuchatel", Canton.BE, StatutProfessionnel.SALARIE, 9000),
        parent(true, true, "Bienne", Canton.FR, StatutProfessionnel.INDEPENDANT, 1000),
        "Neuchatel",
        false,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_CaseC_GivenSeparatedParentsAndChildLivesWithParent1_ShouldReturnParent1() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, true, "Neuchatel", Canton.BE, StatutProfessionnel.SALARIE, 1000),
        parent(true, true, "Bienne", Canton.FR, StatutProfessionnel.SALARIE, 9000),
        "Neuchatel",
        false,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent1());
  }

  @Test
  void getParentDroitAllocation_CaseC_GivenSeparatedParentsAndChildLivesWithParent2_ShouldReturnParent2() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, true, "Neuchatel", Canton.BE, StatutProfessionnel.SALARIE, 9000),
        parent(true, true, "Bienne", Canton.FR, StatutProfessionnel.SALARIE, 1000),
        "Bienne",
        false,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_CaseD_GivenParent1WorksInChildHomeCanton_ShouldReturnParent1() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, true, "Neuchatel", Canton.NE, StatutProfessionnel.SALARIE, 1000),
        parent(true, true, "Neuchatel", Canton.FR, StatutProfessionnel.SALARIE, 9000),
        "Neuchatel",
        true,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent1());
  }

  @Test
  void getParentDroitAllocation_CaseD_GivenParent2WorksInChildHomeCanton_ShouldReturnParent2() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, true, "Neuchatel", Canton.BE, StatutProfessionnel.SALARIE, 9000),
        parent(true, true, "Neuchatel", Canton.NE, StatutProfessionnel.SALARIE, 1000),
        "Neuchatel",
        true,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_CaseE_GivenParent1IsEmployeeAndParent2Independent_ShouldReturnParent1() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, true, "Neuchatel", Canton.BE, StatutProfessionnel.SALARIE, 1000),
        parent(true, true, "Neuchatel", Canton.FR, StatutProfessionnel.INDEPENDANT, 9000),
        "Neuchatel",
        true,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent1());
  }

  @Test
  void getParentDroitAllocation_CaseE_GivenParent2IsEmployeeAndParent1Independent_ShouldReturnParent2() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, true, "Neuchatel", Canton.BE, StatutProfessionnel.INDEPENDANT, 9000),
        parent(true, true, "Neuchatel", Canton.FR, StatutProfessionnel.SALARIE, 1000),
        "Neuchatel",
        true,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_CaseE_GivenBothParentsAreEmployees_ShouldReturnHighestAvsIncome() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, true, "Neuchatel", Canton.BE, StatutProfessionnel.SALARIE, 9000),
        parent(true, true, "Neuchatel", Canton.FR, StatutProfessionnel.SALARIE, 1000),
        "Neuchatel",
        true,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent1());
  }

  @Test
  void getParentDroitAllocation_CaseF_GivenBothParentsAreIndependent_ShouldReturnHighestAvsIncome() {
    ParentDroitAllocationParameters parameters = decision(
        parent(true, true, "Neuchatel", Canton.BE, StatutProfessionnel.INDEPENDANT, 1000),
        parent(true, true, "Neuchatel", Canton.FR, StatutProfessionnel.INDEPENDANT, 9000),
        "Neuchatel",
        true,
        Canton.NE);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  private ParentDroitAllocationParameters decision(
      ParentDroitAllocationParent parent1,
      ParentDroitAllocationParent parent2,
      String enfantResidence,
      boolean parentsEnsemble,
      Canton enfantCantonDomicile) {
    return new ParentDroitAllocationParameters(parent1, parent2, enfantResidence, parentsEnsemble,
        enfantCantonDomicile);
  }

  private ParentDroitAllocationParent parent(
      boolean activiteLucrative,
      boolean autoriteParentale,
      String residence,
      Canton cantonTravail,
      StatutProfessionnel statutProfessionnel,
      int revenuAvs) {
    return new ParentDroitAllocationParent(activiteLucrative, autoriteParentale, residence, cantonTravail,
        statutProfessionnel, BigDecimal.valueOf(revenuAvs));
  }

}
