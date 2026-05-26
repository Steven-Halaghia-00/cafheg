package ch.hearc.cafheg.domain.allocations;

public class SuppressionAllocataireInterditeException extends RuntimeException {

  public SuppressionAllocataireInterditeException(long allocataireId) {
    super("Suppression interdite: allocataire " + allocataireId + " possede deja au moins un versement");
  }
}
