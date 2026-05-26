package ch.hearc.cafheg.infrastructure.web;

import ch.hearc.cafheg.domain.allocations.Allocataire;
import ch.hearc.cafheg.domain.allocations.AllocataireIntrouvableException;
import ch.hearc.cafheg.domain.allocations.AllocationService;
import ch.hearc.cafheg.domain.allocations.ModificationAllocataireSansChangementException;
import ch.hearc.cafheg.domain.allocations.NoAVS;
import ch.hearc.cafheg.domain.allocations.SuppressionAllocataireInterditeException;
import ch.hearc.cafheg.domain.versements.VersementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RESTControllerTest {

  private AllocationService allocationService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    allocationService = Mockito.mock(AllocationService.class);
    VersementService versementService = Mockito.mock(VersementService.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new RESTController(allocationService, versementService, false))
        .build();
  }

  @Test
  void updateAllocataire_GivenNomPrenom_ShouldUpdateAndReturnAllocataireWithoutChangingNoAvs() throws Exception {
    when(allocationService.updateAllocataire(1L, "Vuillaume", "Theo"))
        .thenReturn(new Allocataire(new NoAVS("1000-2000"), "Vuillaume", "Theo"));

    mockMvc.perform(put("/allocataires/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nom": "Vuillaume",
                  "prenom": "Theo",
                  "noAVS": "9999-9999"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nom").value("Vuillaume"))
        .andExpect(jsonPath("$.prenom").value("Theo"))
        .andExpect(jsonPath("$.noAVS.value").value("1000-2000"));

    verify(allocationService).updateAllocataire(1L, "Vuillaume", "Theo");
  }

  @Test
  void updateAllocataire_GivenBlankNom_ShouldReturnBadRequestAndNotCallService() throws Exception {
    mockMvc.perform(put("/allocataires/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nom": " ",
                  "prenom": "Theo"
                }
                """))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(allocationService);
  }

  @Test
  void updateAllocataire_GivenUnknownAllocataire_ShouldReturnNotFound() throws Exception {
    when(allocationService.updateAllocataire(99L, "Vuillaume", "Theo"))
        .thenThrow(new AllocataireIntrouvableException(99L));

    mockMvc.perform(put("/allocataires/99")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nom": "Vuillaume",
                  "prenom": "Theo"
                }
                """))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("99")));
  }

  @Test
  void updateAllocataire_GivenNoChange_ShouldReturnBadRequest() throws Exception {
    when(allocationService.updateAllocataire(1L, "Kendrick", "Deguzman"))
        .thenThrow(new ModificationAllocataireSansChangementException(1L));

    mockMvc.perform(put("/allocataires/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nom": "Kendrick",
                  "prenom": "Deguzman"
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Modification impossible")));
  }

  @Test
  void deleteAllocataire_GivenExistingAllocataireWithoutVersement_ShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/allocataires/21"))
        .andExpect(status().isNoContent());

    verify(allocationService).deleteAllocataire(21L);
  }

  @Test
  void deleteAllocataire_GivenExistingAllocataireWithVersement_ShouldReturnConflict() throws Exception {
    Mockito.doThrow(new SuppressionAllocataireInterditeException(1L))
        .when(allocationService).deleteAllocataire(1L);

    mockMvc.perform(delete("/allocataires/1"))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("Suppression interdite")));
  }

  @Test
  void deleteAllocataire_GivenUnknownAllocataire_ShouldReturnNotFound() throws Exception {
    Mockito.doThrow(new AllocataireIntrouvableException(99L))
        .when(allocationService).deleteAllocataire(99L);

    mockMvc.perform(delete("/allocataires/99"))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("99")));
  }
}
