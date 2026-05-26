package ch.hearc.cafheg.domain.allocations;

public class AllocataireIntrouvableException extends RuntimeException {

  public AllocataireIntrouvableException(long allocataireId) {
    super("Allocataire " + allocataireId + " introuvable");
  }
}
