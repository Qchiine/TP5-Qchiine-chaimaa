package com.example;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.example.model.Salle;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.Creneau;
import com.example.util.JeuDeDonnees;
import com.example.util.PaginationResult;

/**
 * Programme de démonstration exécutable en Java SE.
 *
 * <p>Le déroulé est le suivant : ouverture de l'unité de persistance
 * {@code SallePU}, amorçage d'un jeu de données de démonstration, recherche des
 * salles libres sur un créneau donné, recherche multi-critères via l'API
 * Criteria, puis illustration de la pagination côté serveur.</p>
 */
public class App {

    private static final LocalDateTime CRENEAU_DEBUT = LocalDateTime.of(2024, 6, 1, 9, 0);
    private static final LocalDateTime CRENEAU_FIN = LocalDateTime.of(2024, 6, 1, 11, 0);

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        EntityManager em = emf.createEntityManager();

        try {
            JeuDeDonnees.amorcer(em);

            SalleService salleService = new SalleServiceImpl();
            ((SalleServiceImpl) salleService).setEntityManager(em);

            afficherRechercheMultiCriteres(salleService, 15, Creneau.entre(CRENEAU_DEBUT, CRENEAU_FIN));
            afficherPagination(salleService, 2, 2);
            afficherSallesDisponibles(salleService, CRENEAU_DEBUT, CRENEAU_FIN);
        } finally {
            em.close();
            emf.close();
        }
    }

    /** Requête JPQL : salles qu'aucune réservation ne recouvre sur le créneau. */
    private static void afficherSallesDisponibles(SalleService salleService, LocalDateTime debut, LocalDateTime fin) {
        List<Salle> sallesDisponibles = salleService.getAvailableRooms(debut, fin);
        System.out.println("Salles disponibles entre " + debut + " et " + fin + ":");
        for (Salle salle : sallesDisponibles) {
            System.out.println("- " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
        }
    }

    /** API Criteria : nom, capacité minimale et disponibilité combinés. */
    private static void afficherRechercheMultiCriteres(SalleService salleService, int capaciteMinimale,
                                                       Creneau creneau) {
        List<Salle> trouvees = salleService.searchRooms(null, capaciteMinimale,
                creneau.getDebut(), creneau.getFin(), 1, 10);

        System.out.println();
        System.out.println("Recherche multi-critères (capacité >= " + capaciteMinimale
                + ", créneau " + creneau.libelle() + "):");
        for (Salle salle : trouvees) {
            System.out.println("- " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
        }
    }

    /** Pagination serveur : setFirstResult / setMaxResults et comptage total. */
    private static void afficherPagination(SalleService salleService, int page, int taille) {
        long total = salleService.countRooms(null, null, null, null);
        System.out.println();
        System.out.println("Nombre total de salles enregistrées: " + total);

        PaginationResult<Salle> tranche = salleService.rechercherPage(null, null, null, null, page, taille);
        System.out.println(tranche + " -> " + tranche.getItems().size() + " salle(s) affichée(s)");
        for (Salle salle : tranche.getItems()) {
            System.out.println("- " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
        }
    }
}
