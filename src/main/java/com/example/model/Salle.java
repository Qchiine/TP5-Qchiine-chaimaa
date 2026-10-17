package com.example.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Salle réservable. Une salle est identifiée par son nom, qui sert de clé
 * fonctionnelle pour les recherches par critère.
 */
@Entity
@Table(name = "salles")
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private int capacite;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new LinkedHashSet<>();

    public Salle() {
        // requis par le fournisseur de persistance
    }

    public static Salle avec(String nom, int capacite) {
        Salle salle = new Salle();
        salle.nom = nom;
        salle.capacite = capacite;
        return salle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public Set<Reservation> getReservations() {
        return Collections.unmodifiableSet(reservations);
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations == null ? new LinkedHashSet<>() : reservations;
    }

    /** Ajoute une réservation en maintenant les deux côtés de l'association. */
    public void rattacher(Reservation reservation) {
        reservations.add(reservation);
        reservation.setSalle(this);
    }

    /** Vrai si la salle peut accueillir au moins le nombre de personnes indiqué. */
    public boolean accueille(int personnes) {
        return capacite >= personnes;
    }

    /** Vrai si la salle est libre sur tout l'intervalle demandé. */
    public boolean libreEntre(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null) {
            return true;
        }
        for (Reservation occupee : reservations) {
            if (occupee.recouvre(debut, fin)) {
                return false;
            }
        }
        return true;
    }

    /** Nombre de réservations connues, pratique pour les tests et l'affichage. */
    public int nombreDeReservations() {
        return reservations.size();
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Salle)) {
            return false;
        }
        return Objects.equals(nom, ((Salle) autre).nom);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nom);
    }

    @Override
    public String toString() {
        String creneaux = reservations.stream()
                .map(Reservation::toString)
                .collect(Collectors.joining(", "));
        return "Salle[" + nom + ", capacité=" + capacite + ", réservations=" + creneaux + "]";
    }
}
