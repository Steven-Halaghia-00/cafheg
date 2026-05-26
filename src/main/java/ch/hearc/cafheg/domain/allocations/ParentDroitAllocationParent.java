package ch.hearc.cafheg.domain.allocations;

import java.math.BigDecimal;

public record ParentDroitAllocationParent(
    boolean activiteLucrative,
    boolean autoriteParentale,
    String residence,
    Canton cantonTravail,
    StatutProfessionnel statutProfessionnel,
    BigDecimal revenuAvs) {

  public ParentDroitAllocationParent {
    residence = defaultString(residence);
    revenuAvs = revenuAvs == null ? BigDecimal.ZERO : revenuAvs;
  }

  private static String defaultString(String value) {
    return value == null ? "" : value;
  }
}
