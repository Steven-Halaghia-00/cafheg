package ch.hearc.cafheg.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ModificationAllocataireRequest(String nom, String prenom) {

  public boolean hasBlankRequiredField() {
    return isBlank(nom) || isBlank(prenom);
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
