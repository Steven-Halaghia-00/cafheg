package ch.hearc.cafheg.infrastructure.web;

import ch.hearc.cafheg.domain.allocations.Allocataire;
import ch.hearc.cafheg.domain.allocations.AllocataireIntrouvableException;
import ch.hearc.cafheg.domain.allocations.Allocation;
import ch.hearc.cafheg.domain.allocations.AllocationService;
import ch.hearc.cafheg.domain.allocations.ModificationAllocataireSansChangementException;
import ch.hearc.cafheg.domain.allocations.SuppressionAllocataireInterditeException;
import ch.hearc.cafheg.domain.versements.VersementService;
import ch.hearc.cafheg.infrastructure.pdf.PDFExporter;
import ch.hearc.cafheg.infrastructure.persistence.AllocataireMapper;
import ch.hearc.cafheg.infrastructure.persistence.AllocationMapper;
import ch.hearc.cafheg.infrastructure.persistence.Database;
import ch.hearc.cafheg.infrastructure.persistence.EnfantMapper;
import ch.hearc.cafheg.infrastructure.persistence.VersementMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

@RestController
@Tag(name = "CAFHEG API")
public class RESTController {

    private final AllocationService allocationService;
    private final VersementService versementService;
    private final boolean useTransactions;

    public RESTController() {
        this(new AllocationService(new AllocataireMapper(), new AllocationMapper(), new VersementMapper()),
             new VersementService(new VersementMapper(), new AllocataireMapper(), new PDFExporter(new EnfantMapper())));
    }

    RESTController(AllocationService allocationService, VersementService versementService) {
        this(allocationService, versementService, true);
    }

    RESTController(AllocationService allocationService, VersementService versementService, boolean useTransactions) {
        this.allocationService = allocationService;
        this.versementService = versementService;
        this.useTransactions = useTransactions;
    }

    /*
    // Headers de la requête HTTP doit contenir "Content-Type: application/json"
    // BODY de la requête HTTP à transmettre afin de tester le endpoint
    {
        "enfantResidence" : "Neuchâtel",
        "parent1Residence" : "Neuchâtel",
        "parent2Residence" : "Bienne",
        "parentsEnsemble" : true,
        "enfantCantonDomicile" : "NE",
        "parent1ActiviteLucrative" : true,
        "parent1AutoriteParentale" : true,
        "parent1CantonTravail" : "BE",
        "parent1StatutProfessionnel" : "SALARIE",
        "parent1RevenuAvs" : 2500,
        "parent2ActiviteLucrative" : true,
        "parent2AutoriteParentale" : true,
        "parent2CantonTravail" : "FR",
        "parent2StatutProfessionnel" : "SALARIE",
        "parent2RevenuAvs" : 3000
    }
     */
    @PostMapping("/droits/quel-parent")
    public String getParentDroitAllocation(@RequestBody ParentDroitAllocationRequest params) {
        return inTransaction(() -> allocationService.getParentDroitAllocation(params.toParameters()).parent());
    }

    @GetMapping("/allocataires")
    public List<Allocataire> allocataires(
            @RequestParam(value = "startsWith", required = false) String start
    ) {
        return inTransaction(() -> allocationService.findAllAllocataires(start));
    }

    @DeleteMapping("/allocataires/{allocataireId}")
    public ResponseEntity<Void> deleteAllocataire(@PathVariable("allocataireId") long allocataireId) {
        inTransaction(() -> {
            allocationService.deleteAllocataire(allocataireId);
            return null;
        });
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/allocataires/{allocataireId}")
    public ResponseEntity<Allocataire> updateAllocataire(
            @PathVariable("allocataireId") long allocataireId,
            @RequestBody ModificationAllocataireRequest request) {
        if (request == null || request.hasBlankRequiredField()) {
            return ResponseEntity.badRequest().build();
        }

        Allocataire allocataire = inTransaction(
                () -> allocationService.updateAllocataire(allocataireId, request.nom(), request.prenom()));
        return ResponseEntity.ok(allocataire);
    }

    @ExceptionHandler(AllocataireIntrouvableException.class)
    public ResponseEntity<String> allocataireIntrouvable(AllocataireIntrouvableException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(SuppressionAllocataireInterditeException.class)
    public ResponseEntity<String> suppressionAllocataireInterdite(
            SuppressionAllocataireInterditeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(ModificationAllocataireSansChangementException.class)
    public ResponseEntity<String> modificationAllocataireSansChangement(
            ModificationAllocataireSansChangementException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @GetMapping("/allocations")
    public List<Allocation> allocations() {
        return inTransaction(allocationService::findAllocationsActuelles);
    }

    @GetMapping("/allocations/{year}/somme")
    public BigDecimal sommeAs(@PathVariable("year") int year) {
        return inTransaction(() -> versementService.findSommeAllocationParAnnee(year).getValue());
    }

    @GetMapping("/allocations-naissances/{year}/somme")
    public BigDecimal sommeAns(@PathVariable("year") int year) {
        return inTransaction(
                () -> versementService.findSommeAllocationNaissanceParAnnee(year).getValue());
    }

    @GetMapping(value = "/allocataires/{allocataireId}/allocations", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdfAllocations(@PathVariable("allocataireId") int allocataireId) {
        byte[] pdf = inTransaction(() -> versementService.exportPDFAllocataire(allocataireId));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"allocations_" + allocataireId + ".pdf\"");
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping(value = "/allocataires/{allocataireId}/versements", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdfVersements(@PathVariable("allocataireId") int allocataireId) {
        byte[] pdf = inTransaction(() -> versementService.exportPDFVersements(allocataireId));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"versements_" + allocataireId + ".pdf\"");
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    private <T> T inTransaction(Supplier<T> supplier) {
        if (useTransactions) {
            return Database.inTransaction(supplier);
        }

        return supplier.get();
    }
}
