package com.example.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.example.model.Reservation;
import com.example.model.Salle;

/**
 * Implémentation JPA du contrat {@link SalleRepository}.
 *
 * <p>La sélection de disponibilité s'appuie sur une sous-requête corrélée
 * {@code NOT EXISTS} : une salle reste candidate tant qu'aucune occupation ne
 * croise l'intervalle demandé. La recherche multi-critères est, elle, construite
 * dynamiquement via l'API Criteria afin de n'ajouter que les restrictions
 * réellement fournies par l'appelant.</p>
 */
@Stateless
public class SalleRepositoryImpl implements SalleRepository {

    private static final String REQUETE_DISPONIBILITE =
            "SELECT s FROM Salle s "
            + "WHERE NOT EXISTS ("
            + "    SELECT r FROM Reservation r "
            + "    WHERE r.salle = s AND r.debut < :fin AND r.fin > :debut"
            + ") "
            + "ORDER BY s.nom";

    @PersistenceContext(unitName = "SallePU")
    private EntityManager entityManager;

    public SalleRepositoryImpl() {
        // constructeur requis par le conteneur EJB
    }

    /** Injection manuelle utilisée hors conteneur (test en Java SE). */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        exigerCreneau(debut, fin);
        return entityManager.createQuery(REQUETE_DISPONIBILITE, Salle.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin,
                                   int page, int size) {
        CriteriaBuilder constructeur = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> requete = constructeur.createQuery(Salle.class);
        Root<Salle> racine = requete.from(Salle.class);
        requete.select(racine)
                .where(restrictions(constructeur, requete, racine, nom, capaciteMin, debut, fin))
                .orderBy(constructeur.asc(racine.get("nom")));

        int taillePage = size > 0 ? size : 1;
        int numeroPage = page > 0 ? page : 1;

        TypedQuery<Salle> requeteTypee = entityManager.createQuery(requete);
        requeteTypee.setFirstResult((numeroPage - 1) * taillePage);
        requeteTypee.setMaxResults(taillePage);
        return requeteTypee.getResultList();
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        CriteriaBuilder constructeur = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> decomposition = constructeur.createQuery(Long.class);
        Root<Salle> racine = decomposition.from(Salle.class);
        decomposition.select(constructeur.count(racine));

        Predicate[] conditions = restrictions(constructeur, decomposition, racine, nom, capaciteMin, debut, fin);
        if (conditions.length > 0) {
            decomposition.where(conditions);
        }

        Long total = entityManager.createQuery(decomposition).getSingleResult();
        return total == null ? 0L : total;
    }

    /**
     * Traduit les critères facultatifs en tableau de prédicats. Les critères
     * absents sont simplement ignorés, ce qui rend la requête réutilisable par
     * la version « select » comme par la version « count ».
     */
    private Predicate[] restrictions(CriteriaBuilder constructeur, CriteriaQuery<?> requete, Root<Salle> racine,
                                     String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        List<Predicate> conditions = new ArrayList<>();

        String motif = motifNom(nom);
        if (motif != null) {
            conditions.add(constructeur.like(constructeur.lower(racine.<String>get("nom")), motif));
        }

        if (capaciteMin != null) {
            conditions.add(constructeur.greaterThanOrEqualTo(racine.<Integer>get("capacite"), capaciteMin));
        }

        if (debut != null && fin != null) {
            conditions.add(constructeur.not(racine.get("id").in(sallesOccupees(constructeur, requete, debut, fin))));
        }

        return conditions.toArray(new Predicate[conditions.size()]);
    }

    private Subquery<Long> sallesOccupees(CriteriaBuilder constructeur, CriteriaQuery<?> requete,
                                          LocalDateTime debut, LocalDateTime fin) {
        Subquery<Long> occupees = requete.subquery(Long.class);
        Root<Reservation> occupation = occupees.from(Reservation.class);
        occupees.select(occupation.get("salle").<Long>get("id"));
        occupees.where(constructeur.and(
                constructeur.lessThan(occupation.<LocalDateTime>get("debut"), fin),
                constructeur.greaterThan(occupation.<LocalDateTime>get("fin"), debut)));
        return occupees;
    }

    private String motifNom(String nom) {
        if (nom == null) {
            return null;
        }
        String epure = nom.trim().toLowerCase(Locale.ROOT);
        return epure.isEmpty() ? null : "%" + epure + "%";
    }

    private void exigerCreneau(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null) {
            throw new IllegalArgumentException("Un créneau de recherche complet est requis");
        }
        if (!debut.isBefore(fin)) {
            throw new IllegalArgumentException("Bornes de créneau incohérentes : " + debut + " / " + fin);
        }
    }
}
