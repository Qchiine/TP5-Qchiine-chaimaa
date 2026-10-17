package com.example.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.model.Salle;
import com.example.util.PaginationResult;

/**
 * Façade métier exposée à la présentation.
 *
 * <p>Le service ne manipule jamais directement l'{@code EntityManager} : il
 * délègue systématiquement au repository. C'est également ici que sont
 * appliquées les règles applicatives transverses telles que la normalisation
 * des critères de recherche, le bornage des paramètres de pagination ou la
 * journalisation des accès.</p>
 */
public interface SalleService {

    /**
     * Renvoie les salles libres sur le créneau fourni.
     *
     * @param debut début du créneau souhaité
     * @param fin   fin du créneau souhaité
     * @return les salles disponibles, triées par nom
     */
    List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    /**
     * Recherche multi-critères paginée.
     *
     * @param nom         fragment du nom recherché, éventuellement {@code null}
     * @param capaciteMin capacité minimale, éventuellement {@code null}
     * @param debut       début du créneau de disponibilité, éventuellement {@code null}
     * @param fin         fin du créneau de disponibilité, éventuellement {@code null}
     * @param page        numéro de page attendu
     * @param size        taille de page souhaitée
     * @return les salles de la page demandée
     */
    List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);

    /**
     * Compte les salles correspondant aux critères, pagination exclue.
     *
     * @param nom         fragment du nom recherché, éventuellement {@code null}
     * @param capaciteMin capacité minimale, éventuellement {@code null}
     * @param debut       début du créneau de disponibilité, éventuellement {@code null}
     * @param fin         fin du créneau de disponibilité, éventuellement {@code null}
     * @return le nombre total de salles correspondantes
     */
    long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin);

    /**
     * Variante enrichie : renvoie la page accompagnée de ses métadonnées de
     * navigation (nombre total d'éléments, nombre de pages, page courante).
     *
     * @param nom         fragment du nom recherché, éventuellement {@code null}
     * @param capaciteMin capacité minimale, éventuellement {@code null}
     * @param debut       début du créneau de disponibilité, éventuellement {@code null}
     * @param fin         fin du créneau de disponibilité, éventuellement {@code null}
     * @param page        numéro de page attendu
     * @param size        taille de page souhaitée
     * @return la page de résultats et ses métadonnées
     */
    PaginationResult<Salle> rechercherPage(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin,
                                           int page, int size);
}
