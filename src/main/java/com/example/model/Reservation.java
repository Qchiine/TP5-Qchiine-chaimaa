package com.example.model;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Occupation d'une salle sur un intervalle de temps semi-ouvert [debut, fin[.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(nullable = false)
    private LocalDateTime debut;

    @Column(nullable = false)
    private LocalDateTime fin;

    public Reservation() {
        // requis par le fournisseur de persistance
    }

    public static Reservation sur(Salle salle, LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null || !debut.isBefore(fin)) {
            throw new IllegalArgumentException("Créneau de réservation invalide : " + debut + " -> " + fin);
        }
        Reservation reservation = new Reservation();
        reservation.debut = debut;
        reservation.fin = fin;
        salle.rattacher(reservation);
        return reservation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public void setDebut(LocalDateTime debut) {
        this.debut = debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    /** Vrai si cette occupation partage au moins un instant avec l'intervalle fourni. */
    public boolean recouvre(LocalDateTime autreDebut, LocalDateTime autreFin) {
        return autreDebut != null && autreFin != null
                && debut.isBefore(autreFin) && fin.isAfter(autreDebut);
    }

    /** Durée de l'occupation, exprimée en minutes. */
    public long dureeEnMinutes() {
        return java.time.temporal.ChronoUnit.MINUTES.between(debut, fin);
    }

    /** Vrai si l'occupation appartient entièrement à la journée indiquée. */
    public boolean surLaJournee(java.time.LocalDate journee) {
        return journee != null
                && !debut.toLocalDate().isBefore(journee)
                && !fin.toLocalDate().isAfter(journee);
    }

    /** Deux réservations sont équivalentes si elles couvrent le même créneau. */
    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Reservation)) {
            return false;
        }
        Reservation celle = (Reservation) autre;
        return Objects.equals(debut, celle.debut) && Objects.equals(fin, celle.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(debut, fin);
    }

    @Override
    public String toString() {
        return debut + " → " + fin;
    }
}
