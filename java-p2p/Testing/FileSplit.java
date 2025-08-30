package Testing;

import java.io.*;

public class FileSplit {
    public static void splitFile(String filePath) throws IOException {
        int chunkSize = 1024; // 1KB
        byte[] buffer = new byte[chunkSize];

        try (FileInputStream fis = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            File originalFile = new File(filePath);
            String fileName = originalFile.getName();
            
            int bytesRead;
            int chunkIndex = 0;

            while ((bytesRead = bis.read(buffer)) != -1) {
                String chunkFileName = String.format("%s.part%d", fileName, chunkIndex);
                try (FileOutputStream fos = new FileOutputStream(chunkFileName);
                    BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    bos.write(buffer, 0, bytesRead);
                }
                chunkIndex++;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Testing.FileSplit <file-path>");
            System.exit(1);
        }
        String filePath = args[0];
        try {
            splitFile(filePath);
            System.out.println("File split successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}