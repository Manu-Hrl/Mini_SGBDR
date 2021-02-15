package src;

import java.nio.ByteBuffer;

public class DiskManagerTests {
    public static void main(String[] args) throws Exception{
        DBParams.DBPath = args[0];
        DBParams.pageSize = 4096;
        
        ByteBuffer buffer = ByteBuffer.allocate(DBParams.pageSize);
        testCreateFile(1);
        PageId pi = testAddPage(1);
        PageId pitest = new PageId(1,2);
        buffer.putChar('B');
        testWritePage(pitest, buffer);
        buffer.clear();
        testReadPage(pitest, buffer);
        
    }

    private static void testCreateFile(int fileIdx) {
        DiskManager.createFile(fileIdx);
    }

    private static PageId testAddPage(int fileIdx) {
        return DiskManager.addPage(fileIdx);
    }

    private static void testReadPage(PageId pageId, ByteBuffer buff) {
        DiskManager.readPage(pageId, buff);
    }

    private static void testWritePage(PageId pageId, ByteBuffer buff) {
        DiskManager.writePage(pageId, buff);
    }
}
