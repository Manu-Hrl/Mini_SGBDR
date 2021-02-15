package src;

import java.nio.ByteBuffer;
import java.util.Date;

public class Frame {
    private ByteBuffer buffer;
    private PageId pageId;
    private int pin_count;
    private boolean dirty;
    private Date unpinned;

    /**
     * Construit une case (frame) contenant les pages disque.
     * le pint_count est initialisé à 0, le dirty à false, buffer prend la taille d'une page et unpinned vaut null au départ.
     * 
     * @param pageId le PageId de la page qui s’y trouve chargée (si la case n’est pas libre)
     */
    public Frame(PageId pageId) {
        this.pageId = pageId;
        buffer = ByteBuffer.allocate(DBParams.pageSize);
        pin_count = 0;
        dirty = false;
        unpinned = null;
    }

    /**
     * Incrémente le pin_count
     */
    public void incrementPinCount() {
        pin_count++;
    }

    /**
     * Décrémente le pin_count
     */
    public void decrementPinCount() {
        if (pin_count > 0) {
            pin_count--;
        }
    }

    /**
     * Réinitialise la frame pour la vider
     */
    public void resetFrame() {
        pageId = null;
        dirty = false;
		pin_count = 0;
    }

    /**
     * Lis la page de la frame
     * 
     * @return retourne le buffer avec le contenu de la page
     */
    public ByteBuffer getPage() {
        return buffer;
    }

    /**
     * Sauvegarde la page courante de la frame si le dirty est true
     */
    public void savePage() {
        if (dirty) {
            DiskManager.writePage(pageId, buffer);
        }
    }
    
    /**
     * Accesseur du flag dirty
     * 
     * @return retourne la valeur de dirty (true ou false)
     */
    public boolean getDirty() {
        return dirty;
    }

    /**
     * Accesseur du pin_count
     * 
     * @return retourne la valeur du pin_count
     */
    public int getPinCount() {
        return pin_count;
    }

    /**
     * Accesseur du pageId de la frame
     * 
     * @return retourne le pageId
     */
    public PageId getPageId() {
        return pageId;
    }

    /**
     * Accesseur du buffer de la frame
     * 
     * @return retourne le buffer
     */
    public ByteBuffer getByteBuffer() {
        return buffer;
    }

    /**
     * Accesseur du dernier temps du unpin de la frame
     * 
     * @return retourne la valeur de unpinned
     */
    public Date getUnpinned() {
        return unpinned;
    }
    
    /**
     * Modificateur du flag dirty
     * 
     * @param dirty booléen associé au Free(pageId, dirty)
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Modificateur du temps du unpin lors d'un Free
     */
    public void setUnpinned() {
        this.unpinned = new Date();
    }

    @Override
    public boolean equals(Object obj) {
        Frame f = (Frame) obj;

        if (this == f) {
            return true;
        }

        return ((this.pageId == f.pageId) && (this.buffer.equals(f.buffer)) && (this.dirty == f.dirty) && (this.pin_count == f.pin_count)
                && (this.unpinned.equals(f.unpinned)));
    }
}
