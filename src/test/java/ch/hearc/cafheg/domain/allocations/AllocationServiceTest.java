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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
  void getParentDroitAllocation_GivenOnlyParent1HasLucrativeActivity_ShouldReturnParent1() {
    Map<String, Object> parameters = validParentDecisionParameters(true, false, 1000, 9000);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent1());
  }

  @Test
  void getParentDroitAllocation_GivenOnlyParent2HasLucrativeActivity_ShouldReturnParent2() {
    Map<String, Object> parameters = validParentDecisionParameters(false, true, 9000, 1000);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_GivenBothParentsHaveLucrativeActivityAndParent1EarnsMore_ShouldReturnParent1() {
    Map<String, Object> parameters = validParentDecisionParameters(true, true, 5000, 3000);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent1());
  }

  @Test
  void getParentDroitAllocation_GivenBothParentsHaveLucrativeActivityAndParent2EarnsMore_ShouldReturnParent2() {
    Map<String, Object> parameters = validParentDecisionParameters(true, true, 3000, 5000);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_GivenBothParentsHaveLucrativeActivityAndEqualSalaries_ShouldReturnParent2() {
    Map<String, Object> parameters = validParentDecisionParameters(true, true, 3000, 3000);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_GivenNoParameters_ShouldReturnParent2() {
    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(Collections.emptyMap());

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_GivenResidenceAndTogetherValuesWithHigherParent2Salary_ShouldReturnParent2() {
    Map<String, Object> parameters = new HashMap<>(validParentDecisionParameters(true, true, 2500, 3000));
    parameters.put("enfantResidence", "Neuchatel");
    parameters.put("parent1Residence", "Neuchatel");
    parameters.put("parent2Residence", "Neuchatel");
    parameters.put("parentsEnsemble", true);

    ParentDroitAllocationResult result = allocationService.getParentDroitAllocation(parameters);

    assertThat(result).isEqualTo(ParentDroitAllocationResult.parent2());
  }

  @Test
  void getParentDroitAllocation_GivenNonStringResidence_ShouldThrowClassCastException() {
    Map<String, Object> parameters = new HashMap<>(validParentDecisionParameters(true, false, 3000, 2000));
    parameters.put("enfantResidence", 123);

    assertThrows(ClassCastException.class, () -> allocationService.getParentDroitAllocation(parameters));
  }

  @Test
  void getParentDroitAllocation_GivenNonBooleanActivity_ShouldThrowClassCastException() {
    Map<String, Object> parameters = new HashMap<>(validParentDecisionParameters(true, false, 3000, 2000));
    parameters.put("parent1ActiviteLucrative", "true");

    assertThrows(ClassCastException.class, () -> allocationService.getParentDroitAllocation(parameters));
  }

  @Test
  void getParentDroitAllocation_GivenNonNumberSalary_ShouldThrowClassCastException() {
    Map<String, Object> parameters = new HashMap<>(validParentDecisionParameters(true, false, 3000, 2000));
    parameters.put("parent1Salaire", "3000");

    assertThrows(ClassCastException.class, () -> allocationService.getParentDroitAllocation(parameters));
  }

  private Map<String, Object> validParentDecisionParameters(
      boolean parent1ActiviteLucrative,
      boolean parent2ActiviteLucrative,
      Number parent1Salaire,
      Number parent2Salaire) {
    return Map.of(
        "enfantResidence", "Neuchatel",
        "parent1Residence", "Neuchatel",
        "parent2Residence", "Bienne",
        "parentsEnsemble", false,
        "parent1ActiviteLucrative", parent1ActiviteLucrative,
        "parent2ActiviteLucrative", parent2ActiviteLucrative,
        "parent1Salaire", parent1Salaire,
        "parent2Salaire", parent2Salaire
    );
  }

}
