package ch.hearc.cafheg.domain.allocations;

import ch.hearc.cafheg.infrastructure.persistence.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistence.AllocationMapper;
import ch.hearc.cafheg.infrastructure.persistence.VersementMapper;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllocationService {

  private static final Logger logger = LoggerFactory.getLogger(AllocationService.class);

  private final AllocataireMapper allocataireMapper;
  private final AllocationMapper allocationMapper;
  private final VersementMapper versementMapper;

  public AllocationService(
      AllocataireMapper allocataireMapper,
      AllocationMapper allocationMapper,
      VersementMapper versementMapper) {
    this.allocataireMapper = allocataireMapper;
    this.allocationMapper = allocationMapper;
    this.versementMapper = versementMapper;
  }

  public List<Allocataire> findAllAllocataires(String likeNom) {
    logger.info("Searching allocataires with name filter {}", likeNom);
    return allocataireMapper.findAll(likeNom);
  }

  public List<Allocation> findAllocationsActuelles() {
    return allocationMapper.findAll();
  }

  public Allocataire updateAllocataire(long allocataireId, Allocataire modification) {
    Objects.requireNonNull(modification, "modification");
    return updateAllocataire(allocataireId, modification.getNom(), modification.getPrenom());
  }

  public Allocataire updateAllocataire(long allocataireId, String nom, String prenom) {
    logger.info("Updating allocataire {}", allocataireId);
    Objects.requireNonNull(nom, "nom");
    Objects.requireNonNull(prenom, "prenom");

    if (!allocataireMapper.existsById(allocataireId)) {
      logger.warn("Cannot update allocataire {} because it does not exist", allocataireId);
      throw new AllocataireIntrouvableException(allocataireId);
    }

    Allocataire allocataire = allocataireMapper.findById(allocataireId);
    boolean nomChange = !Objects.equals(allocataire.getNom(), nom);
    boolean prenomChange = !Objects.equals(allocataire.getPrenom(), prenom);

    if (!nomChange && !prenomChange) {
      logger.warn("Cannot update allocataire {} because no name value changed", allocataireId);
      throw new ModificationAllocataireSansChangementException(allocataireId);
    }

    Allocataire allocataireModifie = new Allocataire(
        allocataire.getNoAVS(),
        nom,
        prenom);
    allocataireMapper.updateNomPrenom(allocataireId, allocataireModifie.getNom(), allocataireModifie.getPrenom());
    return allocataireModifie;
  }

  public void deleteAllocataire(long allocataireId) {
    logger.info("Deleting allocataire {}", allocataireId);

    if (!allocataireMapper.existsById(allocataireId)) {
      logger.warn("Cannot delete allocataire {} because it does not exist", allocataireId);
      throw new AllocataireIntrouvableException(allocataireId);
    }

    if (versementMapper.existsByAllocataireId(allocataireId)) {
      logger.warn("Cannot delete allocataire {} because versements already exist", allocataireId);
      throw new SuppressionAllocataireInterditeException(allocataireId);
    }

    allocataireMapper.deleteById(allocataireId);
  }

  public ParentDroitAllocationResult getParentDroitAllocation(ParentDroitAllocationParameters parameters) {
    logger.info("Determining which parent has allocation rights");
    Objects.requireNonNull(parameters, "parameters");

    if(hasOnlyParent1LucrativeActivity(parameters)) {
      return ParentDroitAllocationResult.parent1();
    }

    if(hasOnlyParent2LucrativeActivity(parameters)) {
      return ParentDroitAllocationResult.parent2();
    }

    if(hasOnlyParent1ParentalAuthority(parameters)) {
      return ParentDroitAllocationResult.parent1();
    }

    if(hasOnlyParent2ParentalAuthority(parameters)) {
      return ParentDroitAllocationResult.parent2();
    }

    if(!parameters.parentsEnsemble()) {
      return parentLivingWithChild(parameters);
    }

    ParentDroitAllocationResult parentWorkingInChildHomeCanton = parentWorkingInChildHomeCanton(parameters);
    if(parentWorkingInChildHomeCanton != null) {
      return parentWorkingInChildHomeCanton;
    }

    if(isEmployee(parameters.parent1()) && isIndependent(parameters.parent2())) {
      return ParentDroitAllocationResult.parent1();
    }

    if(isEmployee(parameters.parent2()) && isIndependent(parameters.parent1())) {
      return ParentDroitAllocationResult.parent2();
    }

    return parentWithHighestAvsIncome(parameters);
  }

  private boolean hasOnlyParent1LucrativeActivity(ParentDroitAllocationParameters parameters) {
    return parameters.parent1().activiteLucrative() && !parameters.parent2().activiteLucrative();
  }

  private boolean hasOnlyParent2LucrativeActivity(ParentDroitAllocationParameters parameters) {
    return parameters.parent2().activiteLucrative() && !parameters.parent1().activiteLucrative();
  }

  private boolean hasOnlyParent1ParentalAuthority(ParentDroitAllocationParameters parameters) {
    return parameters.parent1().autoriteParentale() && !parameters.parent2().autoriteParentale();
  }

  private boolean hasOnlyParent2ParentalAuthority(ParentDroitAllocationParameters parameters) {
    return parameters.parent2().autoriteParentale() && !parameters.parent1().autoriteParentale();
  }

  private ParentDroitAllocationResult parentLivingWithChild(ParentDroitAllocationParameters parameters) {
    boolean childLivesWithParent1 = livesWithChild(parameters.parent1(), parameters.enfantResidence());
    boolean childLivesWithParent2 = livesWithChild(parameters.parent2(), parameters.enfantResidence());

    if(childLivesWithParent1 && !childLivesWithParent2) {
      return ParentDroitAllocationResult.parent1();
    }

    if(childLivesWithParent2 && !childLivesWithParent1) {
      return ParentDroitAllocationResult.parent2();
    }

    return parentWithHighestAvsIncome(parameters);
  }

  private boolean livesWithChild(ParentDroitAllocationParent parent, String enfantResidence) {
    return parent.residence().equals(enfantResidence);
  }

  private ParentDroitAllocationResult parentWorkingInChildHomeCanton(ParentDroitAllocationParameters parameters) {
    boolean parent1WorksInChildHomeCanton = worksInCanton(parameters.parent1(), parameters.enfantCantonDomicile());
    boolean parent2WorksInChildHomeCanton = worksInCanton(parameters.parent2(), parameters.enfantCantonDomicile());

    if(parent1WorksInChildHomeCanton && !parent2WorksInChildHomeCanton) {
      return ParentDroitAllocationResult.parent1();
    }

    if(parent2WorksInChildHomeCanton && !parent1WorksInChildHomeCanton) {
      return ParentDroitAllocationResult.parent2();
    }

    return null;
  }

  private boolean worksInCanton(ParentDroitAllocationParent parent, Canton canton) {
    return canton != null && canton == parent.cantonTravail();
  }

  private boolean isEmployee(ParentDroitAllocationParent parent) {
    return parent.statutProfessionnel() == StatutProfessionnel.SALARIE;
  }

  private boolean isIndependent(ParentDroitAllocationParent parent) {
    return parent.statutProfessionnel() == StatutProfessionnel.INDEPENDANT;
  }

  private ParentDroitAllocationResult parentWithHighestAvsIncome(ParentDroitAllocationParameters parameters) {
    return parameters.parent1().revenuAvs().compareTo(parameters.parent2().revenuAvs()) > 0
        ? ParentDroitAllocationResult.parent1()
        : ParentDroitAllocationResult.parent2();
  }
}
