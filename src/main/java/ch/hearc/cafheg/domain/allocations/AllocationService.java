package ch.hearc.cafheg.domain.allocations;

import ch.hearc.cafheg.infrastructure.persistence.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistence.AllocationMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AllocationService {

  private static final String ENFANT_RESIDENCE = "enfantResidence";
  private static final String PARENT_1_ACTIVITE_LUCRATIVE = "parent1ActiviteLucrative";
  private static final String PARENT_1_RESIDENCE = "parent1Residence";
  private static final String PARENT_2_ACTIVITE_LUCRATIVE = "parent2ActiviteLucrative";
  private static final String PARENT_2_RESIDENCE = "parent2Residence";
  private static final String PARENTS_ENSEMBLE = "parentsEnsemble";
  private static final String PARENT_1_SALAIRE = "parent1Salaire";
  private static final String PARENT_2_SALAIRE = "parent2Salaire";

  private final AllocataireMapper allocataireMapper;
  private final AllocationMapper allocationMapper;

  public AllocationService(
      AllocataireMapper allocataireMapper,
      AllocationMapper allocationMapper) {
    this.allocataireMapper = allocataireMapper;
    this.allocationMapper = allocationMapper;
  }

  public List<Allocataire> findAllAllocataires(String likeNom) {
    System.out.println("Rechercher tous les allocataires");
    return allocataireMapper.findAll(likeNom);
  }

  public List<Allocation> findAllocationsActuelles() {
    return allocationMapper.findAll();
  }

  public ParentDroitAllocationResult getParentDroitAllocation(Map<String, Object> parameters) {
    System.out.println("Déterminer quel parent a le droit aux allocations");
    ParentDroitAllocationParameters decisionParameters = toParentDroitAllocationParameters(parameters);

    if(decisionParameters.parent1ActiviteLucrative() && !decisionParameters.parent2ActiviteLucrative()) {
      return ParentDroitAllocationResult.parent1();
    }

    if(decisionParameters.parent2ActiviteLucrative() && !decisionParameters.parent1ActiviteLucrative()) {
      return ParentDroitAllocationResult.parent2();
    }

    return decisionParameters.parent1Salaire().doubleValue() > decisionParameters.parent2Salaire().doubleValue()
        ? ParentDroitAllocationResult.parent1()
        : ParentDroitAllocationResult.parent2();
  }

  private ParentDroitAllocationParameters toParentDroitAllocationParameters(Map<String, Object> parameters) {
    return new ParentDroitAllocationParameters(
        (String) parameters.getOrDefault(ENFANT_RESIDENCE, ""),
        (Boolean) parameters.getOrDefault(PARENT_1_ACTIVITE_LUCRATIVE, false),
        (String) parameters.getOrDefault(PARENT_1_RESIDENCE, ""),
        (Boolean) parameters.getOrDefault(PARENT_2_ACTIVITE_LUCRATIVE, false),
        (String) parameters.getOrDefault(PARENT_2_RESIDENCE, ""),
        (Boolean) parameters.getOrDefault(PARENTS_ENSEMBLE, false),
        (Number) parameters.getOrDefault(PARENT_1_SALAIRE, BigDecimal.ZERO),
        (Number) parameters.getOrDefault(PARENT_2_SALAIRE, BigDecimal.ZERO)
    );
  }

  private record ParentDroitAllocationParameters(
      String enfantResidence,
      Boolean parent1ActiviteLucrative,
      String parent1Residence,
      Boolean parent2ActiviteLucrative,
      String parent2Residence,
      Boolean parentsEnsemble,
      Number parent1Salaire,
      Number parent2Salaire) {
  }
}
