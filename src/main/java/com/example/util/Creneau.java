package com.example.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Intervalle horaire semi-ouvert [debut, fin[ utilisé par les recherches de
 * disponibilité. Un créneau n'est valide que si sa borne basse précède
 * strictement sa borne haute.
 */
public final class Creneau {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LocalDateTime debut;
    private final LocalDateTime fin;

    private Creneau(LocalDateTime debut, LocalDateTime fin) {
        this.debut = debut;
        this.fin = fin;
    }

    public static Creneau entre(LocalDateTime debut, LocalDateTime fin) {
        Objects.requireNonNull(debut, "La borne de début est obligatoire");
        Objects.requireNonNull(fin, "La borne de fin est obligatoire");
        if (!debut.isBefore(fin)) {
            throw new IllegalArgumentException(
                    "Le créneau doit commencer avant de se terminer : " + debut + " / " + fin);
        }
        return new Creneau(debut, fin);
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    /** Indique si ce créneau partage au moins un instant avec l'intervalle fourni. */
    public boolean chevauche(LocalDateTime autreDebut, LocalDateTime autreFin) {
        return autreDebut != null && autreFin != null
                && debut.isBefore(autreFin) && fin.isAfter(autreDebut);
    }

    public String libelle() {
        return debut.format(FORMAT) + " - " + fin.format(FORMAT);
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Creneau)) {
            return false;
        }
        Creneau celui = (Creneau) autre;
        return debut.equals(celui.debut) && fin.equals(celui.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(debut, fin);
    }

    @Override
    public String toString() {
        return libelle();
    }
}
