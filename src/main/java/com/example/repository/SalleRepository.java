package com.example.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.example.model.Salle;

/**
 * Point d'accès unique aux données de salles.
 *
 * <p>Cette interface isole complètement la couche métier du fournisseur de
 * persistance : aucune classe cliente n'a besoin de connaître JPQL, l'API
 * Criteria ou même l'existence d'un {@code EntityManager}. Elle sert aussi de
 * frontière pour les tests, puisqu'une implémentation en mémoire peut être
 * substituée sans toucher au service.</p>
 *
 * <p>Les trois opérations ci-dessous couvrent les besoins de l'application :
 * lister les salles libres sur un intervalle de temps, rechercher des salles
 * selon plusieurs critères combinables, et compter les résultats d'une
 * recherche afin de dériver les métadonnées de pagination.</p>
 */
public interface SalleRepository {

    /**
     * Liste les salles qu'aucune réservation ne recouvre sur l'intervalle
     * demandé.
     *
     * @param debut borne basse de l'intervalle recherché (inclusive)
     * @param fin   borne haute de l'intervalle recherché (exclusive)
     * @return les salles libres, triées par nom ; liste vide si aucune
     */
    List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    /**
     * Recherche multi-critères. Chaque critère est facultatif : passer
     * {@code null} revient à ne pas le prendre en compte.
     *
     * @param nom         fragment insensible à la casse cherché dans le nom
     * @param capaciteMin capacité minimale exigée
     * @param debut       borne basse du créneau de disponibilité
     * @param fin         borne haute du créneau de disponibilité
     * @param page        numéro de page attendu, à partir de 1
     * @param size        nombre maximal de salles par page
     * @return la tranche de résultats correspondant à la page demandée
     */
    List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);

    /**
     * Compte les salles qui satisfont exactement les mêmes critères que
     * {@link #searchRooms}, sans tenir compte de la pagination.
     *
     * @param nom         fragment insensible à la casse cherché dans le nom
     * @param capaciteMin capacité minimale exigée
     * @param debut       borne basse du créneau de disponibilité
     * @param fin         borne haute du créneau de disponibilité
     * @return le nombre total d'éléments correspondants
     */
    long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin);
}
