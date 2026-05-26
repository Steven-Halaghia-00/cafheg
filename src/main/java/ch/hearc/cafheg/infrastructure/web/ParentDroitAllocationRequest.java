package ch.hearc.cafheg.infrastructure.web;

import ch.hearc.cafheg.domain.allocations.Canton;
import ch.hearc.cafheg.domain.allocations.ParentDroitAllocationParameters;
import ch.hearc.cafheg.domain.allocations.ParentDroitAllocationParent;
import ch.hearc.cafheg.domain.allocations.StatutProfessionnel;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public record ParentDroitAllocationRequest(
    String enfantResidence,
    boolean parentsEnsemble,
    Canton enfantCantonDomicile,
    boolean parent1ActiviteLucrative,
    boolean parent1AutoriteParentale,
    String parent1Residence,
    Canton parent1CantonTravail,
    StatutProfessionnel parent1StatutProfessionnel,
    @JsonAlias("parent1Salaire") BigDecimal parent1RevenuAvs,
    boolean parent2ActiviteLucrative,
    boolean parent2AutoriteParentale,
    String parent2Residence,
    Canton parent2CantonTravail,
    StatutProfessionnel parent2StatutProfessionnel,
    @JsonAlias("parent2Salaire") BigDecimal parent2RevenuAvs) {

  public ParentDroitAllocationParameters toParameters() {
    return new ParentDroitAllocationParameters(
        new ParentDroitAllocationParent(
            parent1ActiviteLucrative,
            parent1AutoriteParentale,
            parent1Residence,
            parent1CantonTravail,
            parent1StatutProfessionnel,
            parent1RevenuAvs),
        new ParentDroitAllocationParent(
            parent2ActiviteLucrative,
            parent2AutoriteParentale,
            parent2Residence,
            parent2CantonTravail,
            parent2StatutProfessionnel,
            parent2RevenuAvs),
        enfantResidence,
        parentsEnsemble,
        enfantCantonDomicile);
  }
}
