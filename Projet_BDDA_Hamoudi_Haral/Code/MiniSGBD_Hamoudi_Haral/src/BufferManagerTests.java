package src;

import java.nio.ByteBuffer;

public class BufferManagerTests {
    public static void main(String[] args) {
        DBParams.DBPath = args[0];
        DBParams.pageSize = 4096;
        DBParams.frameCount = 2;

        DiskManager.createFile(1);
        ByteBuffer buffer = ByteBuffer.allocate(DBParams.pageSize);
        char c = 65;
        buffer.putChar(c);

        for (int i = 0; i < 5; i++) {
            DiskManager.addPage(1);
            DiskManager.writePage(new PageId(1,i), buffer);
            c++;
            buffer.putChar(c);
        }

        for (int i = 0; i < 3; i++) {
            ByteBuffer buff = BufferManager.getInstance().getPage(new PageId(1,i));

            while ((buff != null) && buff.hasRemaining()) {
                byte b = buff.get();
                System.out.print((char)b);
            } 
            //System.out.println();
        }

        BufferManager.getInstance().freePage(new PageId(1, 0), true);

        ByteBuffer buff = BufferManager.getInstance().getPage(new PageId(1,3));

            while ((buff != null) && buff.hasRemaining()) {
                byte b = buff.get();
                System.out.print((char)b);
            }
    }
}
