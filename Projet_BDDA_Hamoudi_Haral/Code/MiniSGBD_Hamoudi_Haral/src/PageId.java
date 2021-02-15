package src;

public class PageId {
    private int fileIdx;
    private int pageIdx;

    /**
     * Crée un identifiant de page de type "p(n°file, n°page)"
     * 
     * @param fileIdx numéro du fichier
     * @param pageIdx numéro de la page
     */
    public PageId(int fileIdx, int pageIdx) {
        this.fileIdx = fileIdx;
        this.pageIdx = pageIdx;
    }

    /**
     * Crée un identifiant de page de type "p(n°file, n°page)"
     * avec n°file = 0 et n°page = 0
     */
    public PageId() {
        this(0,0);
    }

    /**
     * Accesseur du n°file
     * 
     * @return retourne le n° de fichier
     */
    public int getFileIdx() {
        return fileIdx;
    }

    /**
     * Accesseur du n°page
     * 
     * @return retourne le n° de page
     */
    public int getPageIdx() {
        return pageIdx;
    }

    @Override
    public boolean equals(Object obj) {
        PageId pageId = (PageId) obj;

        if (this == pageId) {
            return true;
        }
        
        return ((this.fileIdx == pageId.fileIdx) && (this.pageIdx == pageId.pageIdx));
    }
}
