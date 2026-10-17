package com.example.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import com.example.repository.SalleRepositoryImpl;
import com.example.util.PaginationResult;

/**
 * Implémentation du service métier. Les règles applicatives (normalisation des
 * critères, bornage de la pagination, journalisation) sont concentrées ici pour
 * garder le repository purement technique.
 */
@Stateless
public class SalleServiceImpl implements SalleService {

    private static final Logger JOURNAL = Logger.getLogger(SalleServiceImpl.class.getName());

    private static final int TAILLE_PAGE_PAR_DEFAUT = 5;

    @Inject
    private SalleRepository salleRepository;

    public SalleServiceImpl() {
        // constructeur requis par le conteneur EJB
    }

    /** Injection manuelle pour l'exécution hors conteneur (Java SE). */
    public void setSalleRepository(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    /**
     * Permet au programme de test Java SE de brancher directement un
     * {@code EntityManager} sans passer par l'injection CDI.
     */
    public void setEntityManager(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "EntityManager obligatoire");
        SalleRepositoryImpl depot = new SalleRepositoryImpl();
        depot.setEntityManager(entityManager);
        this.salleRepository = depot;
    }

    private SalleRepository depot() {
        if (salleRepository == null) {
            throw new IllegalStateException("Aucun repository disponible : injection manquante");
        }
        return salleRepository;
    }

    @Override
    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        List<Salle> disponibles = depot().findAvailableRooms(debut, fin);
        JOURNAL.fine(() -> disponibles.size() + " salle(s) libre(s) sur le créneau demandé");
        return disponibles;
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut,
                                   LocalDateTime fin, int page, int size) {
        return depot().searchRooms(nom, capaciteMin, debut, fin, pageCorrigee(page), tailleCorrigee(size));
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        return depot().countRooms(nom, capaciteMin, debut, fin);
    }

    @Override
    public PaginationResult<Salle> rechercherPage(String nom, Integer capaciteMin, LocalDateTime debut,
                                                  LocalDateTime fin, int page, int size) {
        int numeroPage = pageCorrigee(page);
        int taille = tailleCorrigee(size);

        long total = depot().countRooms(nom, capaciteMin, debut, fin);
        if (total == 0L) {
            return new PaginationResult<>(Collections.<Salle>emptyList(), numeroPage, taille, 0L);
        }

        List<Salle> pageDemandee = depot().searchRooms(nom, capaciteMin, debut, fin, numeroPage, taille);
        return new PaginationResult<>(pageDemandee, numeroPage, taille, total);
    }

    private int pageCorrigee(int page) {
        return page > 0 ? page : 1;
    }

    private int tailleCorrigee(int size) {
        return size > 0 ? size : TAILLE_PAGE_PAR_DEFAUT;
    }
}
