package src;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BufferManager {
    private static BufferManager INSTANCE;

    private Frame [] bufferPool;

    private BufferManager() {
        bufferPool = new Frame [DBParams.frameCount];
    }

    /**
     * Permet d'obtenir l'unique instance du BufferManager
     * 
     * @return retourne l'instance du BufferManager
     */
    public static BufferManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BufferManager();
        }

        return INSTANCE;
    }

    /**
     * Recherche une frame dans le Buffer Manager selon le pageId qu'il contient
     * 
     * @param pageId identifiant de la page
     * @return retourne la frame trouvée, null sinon
     */
    private Frame searchFrame(PageId pageId) {
        for (Frame frame : bufferPool) {
            if ((frame != null) && frame.getPageId().equals(pageId)) {
                return frame;
            }
        }

        return null;
    }

    /**
     * Cherche l'indice d'une place vide dans le bufferPool
     * 
     * @return retourne l'indice de la place vide, -1 si pas de place
     */
    private int caseFree() {
        int i = 0;
        for (Frame frame : bufferPool) {
            if (frame == null) {
                return i;
            }
            i++;
        }

        return -1;
    }

    /**
     * Politique de remplacement LRU
     * 
     * @return retourne la frame ayant le temps de unpin le plus lointain, null si toutes les frames sont en cours d'utilisation
     */
    private Frame politiqueLRU() {
        ArrayList<Frame> listFrame = new ArrayList<>(); //Frames candidates
        for (Frame frame : bufferPool) {
            if ((frame.getPinCount() == 0) && (!frame.getUnpinned().equals(null))){
                listFrame.add(frame);
            }
        }

        Frame leastFrame = null;
        for (int i = 0; i < listFrame.size() -1 ; i++) {
           for (int j = 1; j < listFrame.size(); j++) {
               if (listFrame.get(i).getUnpinned().before(listFrame.get(j).getUnpinned())) {
                   leastFrame = listFrame.get(i);
               } else {
                   leastFrame = listFrame.get(j);
               }
           }
        }

        if (listFrame.size() == 1) {
            leastFrame = listFrame.get(0);
        }

        return leastFrame;
    }

    /**
     * Cette méthode doit répondre à une demande de page venant des couches plus hautes, et donc 
     * retourner un des buffers associés à une frame. Le buffer sera rempli avec le contenu de la page désignée par l’argument pageId.
     * 
     * Attention : ne pas créer de buffer supplémentaire, « récupérer » simplement celui qui correspond à la bonne frame, 
     * après l’avoir rempli si besoin par un appel au DiskManager.
     * 
     * Attention aussi : cette méthode devra s’occuper du remplacement du contenu d’une frame si
     * besoin (donc politique de remplacement).
     * 
     * @param pageId identifiant de la page
     * @return retourne le buffer avec le contenu de la page lue, null si toutes les frames sont en cours d'utilisation
     */
    public ByteBuffer getPage (PageId pageId) {
        Frame frame = searchFrame(pageId);

        if (frame != null) {
            frame.incrementPinCount();
            return frame.getPage();
        } else {
            frame = new Frame(pageId);
            DiskManager.readPage(pageId, frame.getPage());
            frame.incrementPinCount();
            int indiceCaseVide = caseFree();
            
            if (indiceCaseVide != -1) {
                bufferPool[indiceCaseVide] = frame;
                return frame.getPage();
            } else {
                Frame leastFrame = politiqueLRU();
                if (leastFrame != null) {
                    for (int i = 0; i < bufferPool.length; i++) {
                        if (bufferPool[i].equals(leastFrame)) {
                            bufferPool[i].savePage();
                            bufferPool[i] = frame;
                            break;
                        }
                    }

                    return frame.getPage();
                } else {
                    System.out.println("Toutes les pages sont en cours d'utilisation");
                    return null;
                }
            }
        }
    }

    /**
     * Cette méthode devra décrémenter le pin_count et actualiser le flag dirty de la page (et aussi
     * potentiellement actualiser des informations concernant la politique de remplacement).
     * 
     * @param pageId identifiant de la page
     * @param valdirty vérifie si la page a été modifiée
     */
    public void freePage (PageId pageId, boolean valdirty) {
        Frame frame = searchFrame(pageId);

        if (frame != null) {
            frame.decrementPinCount();
            /**
             * On modifie le dirty que lorsque valdirty vaut true
             * car si le dernier free sur une frame à un valdirty false
             * alors que la page de cette même frame a été précédemment modifiée dans le passé
             * les modifications ne seront pas sauvegardées
             */
            if (valdirty) {
                frame.setDirty(valdirty);
            }
            frame.setUnpinned();
        } else {
            System.out.println("La page à libérer n'est dans aucune frame");
        }
    }

    /**
     *  Cette méthode s’occupe de :
     * ◦ l’écriture de toutes les pages dont le flag dirty = 1 sur disque
     * ◦ la remise à 0 de tous les flags/informations et contenus des buffers (buffer pool « vide »)
     */
    public void flushAll() {
        if (bufferPool.length > 0) {
            for (Frame frame : bufferPool) {
                if (frame != null) {
                    frame.savePage();
                    frame.resetFrame();
                }
            }
        }
    }

    /**
     * Réinitialise le Buffer Manager :
     * - Réinitialise les frames
     * - Supprime les frames
     */
    public void reset() {
        
        for (Frame frame : bufferPool) {
            if (frame != null) {
                frame.resetFrame();
            }
        }

        bufferPool = new Frame [DBParams.frameCount];
    }
}
