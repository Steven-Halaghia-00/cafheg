package ch.hearc.cafheg.domain.allocations;

import java.util.Objects;

public record ParentDroitAllocationResult(String parent) {

  public static final String PARENT_1 = "Parent1";
  public static final String PARENT_2 = "Parent2";

  public ParentDroitAllocationResult {
    Objects.requireNonNull(parent, "parent");
  }

  public static ParentDroitAllocationResult parent1() {
    return new ParentDroitAllocationResult(PARENT_1);
  }

  public static ParentDroitAllocationResult parent2() {
    return new ParentDroitAllocationResult(PARENT_2);
  }
}
