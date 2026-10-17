package com.example.util;

import java.time.LocalDateTime;

import javax.persistence.EntityManager;

import com.example.model.Reservation;
import com.example.model.Salle;

/**
 * Jeu de données de démonstration. L'amorçage se fait dans une transaction
 * unique afin que les entités soient réellement persistées avant les requêtes.
 */
public final class JeuDeDonnees {

    private JeuDeDonnees() {
        throw new AssertionError("Classe utilitaire non instanciable");
    }

    public static void amorcer(EntityManager entityManager) {
        entityManager.getTransaction().begin();
        try {
            Salle ada = Salle.avec("Ada Lovelace", 24);
            Salle turing = Salle.avec("Alan Turing", 40);
            Salle hopper = Salle.avec("Grace Hopper", 12);
            Salle knuth = Salle.avec("Donald Knuth", 60);
            Salle dijkstra = Salle.avec("Edsger Dijkstra", 18);

            entityManager.persist(ada);
            entityManager.persist(turing);
            entityManager.persist(hopper);
            entityManager.persist(knuth);
            entityManager.persist(dijkstra);

            entityManager.persist(Reservation.sur(turing,
                    LocalDateTime.of(2024, 6, 1, 8, 0), LocalDateTime.of(2024, 6, 1, 10, 0)));
            entityManager.persist(Reservation.sur(knuth,
                    LocalDateTime.of(2024, 6, 1, 0, 0), LocalDateTime.of(2024, 6, 1, 23, 59)));
            entityManager.persist(Reservation.sur(hopper,
                    LocalDateTime.of(2024, 6, 1, 14, 0), LocalDateTime.of(2024, 6, 1, 16, 0)));

            entityManager.getTransaction().commit();
        } catch (RuntimeException echec) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw echec;
        }
    }
}
