package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class DiskManager {
    /**
     * Crée (dans le sous-dossier DB) un fichier Data_fileIdx.rf
     * 
     * @param fileIdx Entier correspondant à un identifiant de fichier
     */
    public static void createFile (int fileIdx) {
        String pathnameString = "./"+DBParams.DBPath+"/Data_"+fileIdx+".rf";
        File inputFile = new File(pathnameString);

        if (!inputFile.exists()) {
            
            try {
                inputFile.createNewFile();
            } catch (IOException e) {
                System.out.println("Problème d'E/S sur le fichier");
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            
            System.out.println("Fichier Data_"+fileIdx+".rf correctement créé");
        } 
        else {
            System.out.println("Le fichier Data_"+fileIdx+".rf existe déjà !");
        }
    }

    /**
     * Rajoute une page (d'octets) au fichier spécifié par fileIdx
     * 
     * @param fileIdx Entier correspondant à un identifiant de fichier
     * @return Retourne un PageId correspondant à la page nouvellement rajoutée
     */
    public static PageId addPage (int fileIdx) { 
        ByteBuffer buffer = ByteBuffer.allocate(DBParams.pageSize);
        String pathnameString = "./"+DBParams.DBPath+"/Data_"+fileIdx+".rf";
        int pageIdx = 0;

        try {
            File inputFile = new File(pathnameString);
            if (inputFile.exists()) {
                RandomAccessFile rFile = new RandomAccessFile(inputFile, "rw");

                rFile.seek(rFile.length());
                rFile.write(buffer.array());

                pageIdx = (int) ((rFile.length() / DBParams.pageSize) - 1);
                
                rFile.close();
            } else {
                System.out.println("Le fichier n'existe pas ! Créez le au préalable");
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("Le fichier n'existe pas !");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return new PageId(fileIdx, pageIdx);
    }

    /**
     * Remplit le buffer avec le contenu disque de la page identifiée par pageID
     * 
     * @param pageId  Identifiant de page
     * @param buff Un buffer
     */
    public static void readPage (PageId pageId, ByteBuffer buff) {
        String pathnameString = "./"+DBParams.DBPath+"/Data_"+pageId.getFileIdx()+".rf";
        File inputFile = new File(pathnameString);

        try {
            if (inputFile.exists()) {
                RandomAccessFile rFile = new RandomAccessFile(inputFile, "r");

                rFile.seek(pageId.getPageIdx() * DBParams.pageSize);
                rFile.read(buff.array());

                rFile.close();
            } else {
                System.out.println("Le fichier n'existe pas ! Créez le au préalable");
            }
            
        } catch (IOException e) {
            System.err.println("Pb E/S sur le buffer");
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Le buffer est null");
            e.printStackTrace();
        }
    }

    /**
     * Ecrit le contenu du buffer dans le fichier et à la position voulu grâce à pageId
     * 
     * @param pageId Identifiant de page
     * @param buff Un buffer
     */
    public static void writePage (PageId pageId, ByteBuffer buff)  {
        String pathnameString = "./"+DBParams.DBPath+"/Data_"+pageId.getFileIdx()+".rf";
        File inputFile = new File(pathnameString);

        try {
            if (inputFile.exists()) {
                RandomAccessFile rFile = new RandomAccessFile(inputFile, "rw");

                rFile.seek(pageId.getPageIdx() * DBParams.pageSize);
                rFile.write(buff.array());
                
                rFile.close();
            } else {
                System.out.println("Le fichier n'existe pas ! Créez le au préalable");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Le buffer est null");
            e.printStackTrace();
        }
    }
}
