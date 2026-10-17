package com.example.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enveloppe immuable décrivant une tranche de résultats et ses métadonnées de
 * navigation.
 *
 * @param <T> type des éléments contenus dans la page
 */
public final class PaginationResult<T> {

    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;
    private final long totalItems;

    public PaginationResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items == null ? Collections.emptyList() : new ArrayList<>(items);
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = calculerNombreDePages(totalItems, pageSize);
    }

    /**
     * Découpe une liste complète en page : utile lorsque le filtrage a déjà été
     * fait en mémoire.
     */
    public static <T> PaginationResult<T> depuisListe(List<T> complet, int page, int taille) {
        int borne = Math.max(taille, 1);
        int depuis = Math.min(Math.max(page - 1, 0) * borne, complet.size());
        int jusqua = Math.min(depuis + borne, complet.size());
        return new PaginationResult<>(complet.subList(depuis, jusqua), page, borne, complet.size());
    }

    private static int calculerNombreDePages(long total, int taille) {
        if (taille <= 0) {
            return 0;
        }
        return (int) ((total + taille - 1) / taille);
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Vrai si une page suivante existe réellement. */
    public boolean hasNext() {
        return currentPage < totalPages;
    }

    @Override
    public String toString() {
        return "Page " + currentPage + "/" + Math.max(totalPages, 1)
                + " (" + items.size() + " élément(s) sur " + totalItems + ")";
    }
}
