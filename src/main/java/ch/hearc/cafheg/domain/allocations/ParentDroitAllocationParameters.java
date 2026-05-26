package ch.hearc.cafheg.domain.allocations;

import java.util.Objects;

public record ParentDroitAllocationParameters(
    ParentDroitAllocationParent parent1,
    ParentDroitAllocationParent parent2,
    String enfantResidence,
    boolean parentsEnsemble,
    Canton enfantCantonDomicile) {

  public ParentDroitAllocationParameters {
    parent1 = Objects.requireNonNull(parent1, "parent1");
    parent2 = Objects.requireNonNull(parent2, "parent2");
    enfantResidence = enfantResidence == null ? "" : enfantResidence;
  }
}
