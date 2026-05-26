package ch.hearc.cafheg.domain.allocations;

public class ModificationAllocataireSansChangementException extends RuntimeException {

  public ModificationAllocataireSansChangementException(long allocataireId) {
    super("Modification impossible: ni le nom ni le prenom de l'allocataire " + allocataireId + " n'a change");
  }
}
