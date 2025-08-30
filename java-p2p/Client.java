import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;



public class Client {
    private String clientID;
    private String trackerIP;
    private int trackerPort;
    private int clientPort;
    private String fileDirectory;
    
    private BufferedReader input;
    private BufferedReader fileReader;
    private PrintWriter output;

    private Socket trackerSocket;
    private ServerSocket clientSocket;

    private Map<String, Peer.FileInfo> fileToPiecesMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Peer> ClientData = new ConcurrentHashMap<>();


    public Random random = new Random();

    public Client(String filePath) throws FileNotFoundException, IOException{
        try {
            this.fileReader = new BufferedReader(new FileReader(filePath));
            this.trackerIP = fileReader.readLine().trim(); // First line: tracker URL
            this.trackerPort = Integer.parseInt(fileReader.readLine().trim()); // Second line: tracker Port
            this.clientID = fileReader.readLine().trim();
            this.clientPort = random.nextInt((65535 - 49152) + 1) + 49152;

            File file = new File(filePath);
            String parentDirectory = file.getParent();
            this.fileDirectory = parentDirectory;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class Peer{
        
        private String peerID;
        private String peerIP;
        private int peerPort;
        private ConcurrentHashMap<String, FileInfo> FileInfoMap = new ConcurrentHashMap<>();

        private Socket peerSocket; // Main socket for commands and control messages
        private boolean Connected = false;

        private BufferedReader input;
        private PrintWriter output;

        public Peer(){}

        public static class FileInfo{
            private String fileName;
            private int fileSize;
            private List<String> piece;

            public FileInfo(String Name,int Size,List<String> pieceArray){
                this.fileName = Name;
                this.fileSize = Size;
                this.piece = pieceArray;
            }
            public boolean isComplete() {
                int pieceSize = 1024; // Assuming each piece is 1KB
                int requiredPieces = (int) Math.ceil((double) this.fileSize / pieceSize);
                return piece.size() >= requiredPieces;
            }
        
        }
        
        public void printInfo() {
            System.out.println("Peer ID: " + peerID);
            System.out.println("Peer IP: " + peerIP);
            System.out.println("Peer Port: " + peerPort);
            System.out.println("Files:");
            for (FileInfo fileInfo : FileInfoMap.values()) {
                System.out.println("  File Name: " + fileInfo.fileName);
                System.out.println("  File Size: " + fileInfo.fileSize);
                System.out.println("  Pieces: " + String.join(", ", fileInfo.piece));
            }
        }
        public void peerlistener() {
            new Thread(() -> {
                try {
                    while(!peerSocket.isClosed()){
                        String message = input.readLine();
                        System.out.println("PEER: "+message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public int findHighestFileSize(String baseName) {
        int highestSize = 0;
        for (Peer peer : ClientData.values()) {
            Peer.FileInfo fileInfo = peer.FileInfoMap.get(baseName);
            if (fileInfo != null) {
                highestSize = Math.max(highestSize, fileInfo.fileSize);
            }
        }
        return highestSize;
    }

    public void updateMetadata(String directoryPath, String metadataFilePath) throws IOException {
        File directory = new File(directoryPath);
        File[] partFiles = directory.listFiles((dir, name) -> name.matches(".*\\.part\\d+"));
        File[] txtFiles = directory.listFiles((dir, name) -> name.endsWith(".txt") && !name.equals("Meta.txt"));

        
    
        List<String> metadataContents = new ArrayList<>();
        metadataContents.add(trackerIP); // IP Address
        metadataContents.add(Integer.toString(trackerPort)); // Port Number
        metadataContents.add(clientID); // Client ID
    
        if ((partFiles == null || partFiles.length == 0) && (txtFiles == null || txtFiles.length == 0)) {
            System.out.println("No .part or .txt files found in the directory.");
            metadataContents.add("0"); // Number of Files
        } else {
            Map<String, List<File>> fileGroups = new TreeMap<>();  
            // First group complete .txt files
            if (txtFiles != null) {
                for (File txtFile : txtFiles) {
                    String baseName = txtFile.getName().substring(0, txtFile.getName().lastIndexOf('.'));
                    fileGroups.put(baseName, new ArrayList<>(Collections.singletonList(txtFile)));
                }
            }
    
            // Then group .part files only if there is no corresponding complete .txt file
            if (partFiles != null) {
                for (File partFile : partFiles) {
                    String[] parts = partFile.getName().split("\\.");
                    String baseName = parts[0];
                    boolean txtFileExists = new File(directory, baseName + ".txt").exists();
                    if (!txtFileExists) {
                        fileGroups.putIfAbsent(baseName, new ArrayList<>());
                        fileGroups.get(baseName).add(partFile);
                    }
                }
            }
    
            System.out.println("\nGrouped files:");
            for (Map.Entry<String, List<File>> entry : fileGroups.entrySet()) {
                System.out.println("Base name: " + entry.getKey());
                for (File file : entry.getValue()) {
                    System.out.println("File " + file.getName());
                }
            }
    
            metadataContents.add(Integer.toString(fileGroups.size())); // Number of Files
    
            for (Map.Entry<String, List<File>> entry : fileGroups.entrySet()) {
                List<File> files = entry.getValue();
                String fileName = entry.getKey();
    
                metadataContents.add(fileName);
    
                // Calculate file size based on the number of parts
                int fileSizeInKB = findHighestFileSize(fileName);

                if (fileSizeInKB == 0) {
                    fileSizeInKB = files.stream().mapToInt(file -> (int) Math.ceil((double) file.length() / 1024)).sum();
                }
                metadataContents.add(fileSizeInKB + " kb");
    
                // List indices from 0 to fileSize - 1
                StringBuilder pieces = new StringBuilder();
                if (files.stream().anyMatch(file -> file.getName().endsWith(".txt"))) {
                    for (int i = 0; i < fileSizeInKB; i++) {
                        if (i > 0) {
                            pieces.append(" ");
                        }
                        pieces.append(i);
                    }
                } else {
                    for (File partFile : files) {
                        String partName = partFile.getName();
                        int partIndex = Integer.parseInt(partName.substring(partName.lastIndexOf("part") + 4));
                        if (pieces.length() > 0) {
                            pieces.append(" ");
                        }
                        pieces.append(partIndex);
                    }
                }
    
                metadataContents.add(pieces.toString()); // List of Piece Indices
            }
        }
    
        metadataContents.add("1 kb");
        Path metadataPath = Paths.get(metadataFilePath);
        Files.write(metadataPath, metadataContents, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    
        System.out.println("Metadata file has been updated.");
        parseClientinfoAfter();
    }
    
    public void splitFile() throws IOException {
        int chunkSize = 1024; // 1KB
        byte[] buffer = new byte[chunkSize];

        File directory = new File(fileDirectory);
        File[] txtFiles = directory.listFiles((dir, name) -> name.endsWith(".txt") && !name.equals("Meta.txt"));

        if (txtFiles == null || txtFiles.length == 0) {
            System.out.println("No .txt files found in the directory.");
            return;
        }
        for (File originalFile : txtFiles) {
            String filePath = originalFile.getAbsolutePath();
            String fileName = originalFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            
            try (FileInputStream fis = new FileInputStream(filePath);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                int bytesRead;
                int chunkIndex = 0;
                List<String> chunks = new ArrayList<>();

                while ((bytesRead = bis.read(buffer)) != -1) {
                    String chunkFileName = String.format("%s.part%d", fileName, chunkIndex);
                    try (FileOutputStream fos = new FileOutputStream(new File(fileDirectory, chunkFileName));
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    chunks.add(Integer.toString(chunkIndex));
                    chunkIndex++;
                }
                System.out.println("Split file: " + fileName + " into " + chunkIndex + " parts.");
                int fileSizeInKB = (int) Math.ceil((double) originalFile.length() / 1024);
                Peer.FileInfo fileInfo = new Peer.FileInfo(baseName, fileSizeInKB, chunks);
                fileToPiecesMap.put(baseName, fileInfo);
                ClientData.get(clientID).FileInfoMap.put(baseName, fileInfo);
            }
        }
    }

    public void mergeFiles() throws IOException {
        Peer localPeer = ClientData.get(clientID);
        File directory = new File(fileDirectory);

        // Iterate through each file in the local peer's FileInfoMap
        for (Map.Entry<String, Peer.FileInfo> entry : localPeer.FileInfoMap.entrySet()) {
            String baseFileName = entry.getKey();
            Peer.FileInfo fileInfo = entry.getValue();
    
            // Check if the file is complete
            if (!fileInfo.isComplete()) {
                System.out.println("File is not complete: " + baseFileName);
                continue;
            }
    
            File[] partFiles = directory.listFiles((dir, name) -> name.matches(baseFileName + "\\.txt\\.part\\d+"));
    
            if (partFiles == null || partFiles.length == 0) {
                System.out.println("No files to merge found for: " + baseFileName);
                continue;
            }
    
            // Sort files by their part number to ensure correct merging order
            Arrays.sort(partFiles, (f1, f2) -> {
                int num1 = Integer.parseInt(f1.getName().replace(baseFileName + ".txt.part", ""));
                int num2 = Integer.parseInt(f2.getName().replace(baseFileName + ".txt.part", ""));
                return Integer.compare(num1, num2);
            });
    
            // Create output file based on the baseFileName
            File outputFile = new File(directory, baseFileName + ".txt");
    
            try (FileOutputStream fos = new FileOutputStream(outputFile, true);
                 BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
                for (File partFile : partFiles) {
                    Files.copy(partFile.toPath(), mergingStream);
                }
                System.out.println("All parts have been merged into " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("Error merging files: " + e.getMessage());
                throw e;
            }
        }
    }

    public void connectToAllPeer() throws UnknownHostException, IOException{
        for (Map.Entry<String, Peer> entry : ClientData.entrySet()) {
            Peer peer = entry.getValue();
            if (peer.Connected == false && !peer.peerID.equals(clientID)) {
                try {
                    // Attempt to connect to the peer
                    peer.peerSocket = new Socket(peer.peerIP, peer.peerPort);
                    System.out.println("Connecting to: " + peer.peerID);
                    peer.Connected = true;
                    // Set up individual input and output streams for each peer
                    peer.input = new BufferedReader(new InputStreamReader(peer.peerSocket.getInputStream()));
                    peer.output = new PrintWriter(peer.peerSocket.getOutputStream(), true);
                    peer.peerlistener();
                    peer.output.println(clientID);
                    peer.output.println(clientID + ": Connected to " + peer.peerID +" Peer server");
                } catch (IOException e) {
                    // Handle connection errors specifically for this peer without stopping the entire process
                    System.err.println("Failed to connect to " + peer.peerID + ": " + e.getMessage());
                    peer.Connected = false;  // Reset connected status on failure
                }
            }
        }
    }
    
    public void startPeerServer() {
        new Thread(() -> {
            try {
                clientSocket = new ServerSocket(clientPort);
                
                System.out.println("Peer server started on port " + clientPort);
                
                while (!Thread.currentThread().isInterrupted()) {
                    Socket peerSocket  = clientSocket.accept();
                    new Thread(new PeerHandler(peerSocket)).start();
                }
            } catch (IOException e) {
                System.out.println("Server stopped accepting connections.");
            }
        }).start();
    }
    
    public class PeerHandler implements Runnable {
        private Socket peerHandlerSocket;
        private BufferedReader input;
        private PrintWriter output;
        private String ConnectedClientID;
        
        public PeerHandler(Socket peerHandlerSocket) {
            this.peerHandlerSocket = peerHandlerSocket;
            try {
                input = new BufferedReader(new InputStreamReader(peerHandlerSocket.getInputStream()));
                output = new PrintWriter(peerHandlerSocket.getOutputStream(), true);
                ConnectedClientID = input.readLine();
                System.out.println(input.readLine());
                output.println(clientID + " Peer server established connection to " + ConnectedClientID);
            } catch (IOException e) {
                System.out.println("Error setting up streams: " + e.getMessage());
            }
            
        }
        private void closePeerConnection() {
            try {
                if (peerHandlerSocket != null && !peerHandlerSocket.isClosed()) {
                    peerHandlerSocket.close();
                }
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
                System.out.println("Peer connection closed: " + ConnectedClientID);
            } catch (IOException e) {
                System.err.println("Error closing peer connection: " + e.getMessage());
            }
        }

        private void sendFilePiece(String fileName, String pieceId, Integer port,String IP) {
            String piecePath = fileDirectory + "/" + fileName + ".txt.part" + pieceId;
            File pieceFile = new File(piecePath);
            if (!pieceFile.exists()) {
                System.out.println("File piece does not exist: " + pieceFile.getName());
                return;
            }
            // Each transfer uses a new port that the receiver is listening on
            try (Socket peerSocket = new Socket(IP, port); // getListeningPort() needs to be defined in Peer
                 FileInputStream fis = new FileInputStream(pieceFile);
                 BufferedOutputStream bos = new BufferedOutputStream(peerSocket.getOutputStream())) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                bos.flush();
                System.out.println("File piece sent successfully: " + pieceId);
            } catch (FileNotFoundException fnfe) {
                System.out.println("File not found: " + fnfe.getMessage());
            } catch (IOException ioe) {
                System.out.println("Failed to send file piece: " + ioe.getMessage());
            }
        }
        @Override
        public void run() {
            try {
                while (!peerHandlerSocket.isClosed()) {
                    try {
                        String command = input.readLine();
                        System.out.println(command);
                        if (command != null) {
                            switch (command) {
                                case "DOWNLOAD":
                                    command = input.readLine();
                                    String[] parts = command.split(" ");
                                    sendFilePiece(parts[0],parts[1],Integer.parseInt(parts[2]),parts[3]);
                                    break;
                                case "REQUEST_PEER_INFO":
                                    System.out.println(command);
                                    break;
                                case "CLOSE_CONNECTION":
                                    closePeerConnection();
                                    break;
                                // Handle other commands as needed
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in peer handler: " + e.getMessage());
            } finally {
                closePeerConnection(); // Ensure resources are cleaned up
            }
        }
    }

    public void downloadPieceFromString(String fileName, String pieceId){
        for (Peer peer : ClientData.values()) {
            Peer.FileInfo fileInfo = peer.FileInfoMap.get(fileName);  // Use fileIndex to get FileInfo
            if (fileInfo != null && fileInfo.piece.contains(pieceId)) {
                if (downloadFromPeer(peer, fileName, pieceId)) {
                    System.out.println("Successfully downloaded piece " + pieceId + " of file " + fileName + " from " + peer.peerID);
                    return; // Stop after successful download
                } else {
                    System.out.println("Failed to download piece " + pieceId + " of file " + fileName + " from " + peer.peerID);
                }
            }
        }
        System.out.println("Piece " + pieceId + " of file " + fileName + " not found on any connected peer.");
    }

    private boolean downloadFromPeer(Peer peer, String fileName, String pieceId) {
        System.out.println("Downloading " + pieceId + " of " + fileName);
        File localPiece = new File(fileDirectory + "/" + fileName + ".txt.part" + pieceId);
        localPiece.getParentFile().mkdirs();  // Ensure directory exists
        
        Integer Port = clientPort+1;
        try (ServerSocket serverSocket = new ServerSocket(Port)){
            peer.output.println("DOWNLOAD");
            peer.output.println(fileName + " "+ pieceId +" "+ Port + " " + ClientData.get(clientID).peerIP);
            Socket fileTransferSocket = serverSocket.accept(); // Accept connection
            BufferedInputStream bis = new BufferedInputStream(fileTransferSocket.getInputStream());
            try (FileOutputStream fos = new FileOutputStream(localPiece)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            System.out.println("File piece downloaded successfully: " + pieceId);
            return true;
        } catch (IOException e) {
            System.out.println("Error setting up download: " + e.getMessage());
            return false;
        }
    }

    public void loadBalancedDownload(String fileName) throws IOException, InterruptedException {
        Map<String, List<Integer>> peerToMissingPiecesMap = new HashMap<>();

        peerToMissingPiecesMap.clear();
        
        // Get local FileInfo and the pieces this client already has
        Peer.FileInfo localFileInfo = ClientData.get(clientID).FileInfoMap.get(fileName);
        if (localFileInfo == null) {
            System.out.println("FileInfo not found locally for file: " + fileName + ". Searching peers...");
            // Search for the file info in other peers
            for (Peer peer : ClientData.values()) {
                if (!peer.peerID.equals(clientID)) {  // Avoid checking the local client again
                    Peer.FileInfo peerFileInfo = peer.FileInfoMap.get(fileName);
                    if (peerFileInfo != null) {
                        localFileInfo = new Peer.FileInfo(peerFileInfo.fileName, peerFileInfo.fileSize, new ArrayList<>());
                        ClientData.get(clientID).FileInfoMap.put(fileName, localFileInfo);
                        System.out.println("Found and copied FileInfo from peer: " + peer.peerID);
                        break;
                    }
                }
            }
            if (localFileInfo == null) {
                System.out.println("No peer has FileInfo for " + fileName + "; cannot proceed with download.");
                return;
            }
        }
    
        Set<String> localPieces = new HashSet<>(localFileInfo.piece);
        // Initialize mapping for each peer to an empty list
        for (Peer peer : ClientData.values()) {
            peerToMissingPiecesMap.put(peer.peerID, new ArrayList<>());
        }

        int totalPieces = localFileInfo != null ? localFileInfo.fileSize : 0;

        // Iterate over each piece index
        for (int i = 0; i <= totalPieces; i++) {
            if (!localPieces.contains(String.valueOf(i))) {  // Check if piece is missing locally
                // Check each peer to see if they have this missing piece
                for (Map.Entry<String, Peer> entry : ClientData.entrySet()) {
                    String peerId = entry.getKey();
                    Peer peer = entry.getValue();
                    Peer.FileInfo fileInfo = peer.FileInfoMap.get(fileName);
                    if (fileInfo != null && fileInfo.piece.contains(String.valueOf(i))) {
                        peerToMissingPiecesMap.get(peerId).add(i);
                    }
                }
            }
        }
        // Remove any peers that do not have any of the missing pieces
        peerToMissingPiecesMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        System.out.println("Peers and their available missing pieces:");
        peerToMissingPiecesMap.forEach((peerId, pieces) -> {
            System.out.println("Peer " + peerId + " has these missing pieces: " + pieces);
        });
        
       // Prepare a list of peers for round-robin scheduling
        List<String> peerIds = new ArrayList<>(peerToMissingPiecesMap.keySet());
        int peerIndex = 0;
        int downloadedPieces = localPieces.size();
        int totalToDownload = totalPieces;

        // Download missing pieces using round-robin scheduling
        for (int i = 0; i < totalPieces; i++) {
            System.out.println(localPieces.contains(String.valueOf(i)));
            if (!localPieces.contains(String.valueOf(i))) {
                while (true) {
                    String currentPeerId = peerIds.get(peerIndex);
                    Peer currentPeer = ClientData.get(currentPeerId);

                    if (currentPeer != null && peerToMissingPiecesMap.get(currentPeerId).contains(i)) {
                        if (downloadFromPeer(currentPeer, fileName, String.valueOf(i))) {
                            downloadedPieces++;
                            int progress = (int) ((double) downloadedPieces / totalToDownload * 100);
                            System.out.println("Download progress: " + progress + "%");
                            peerIndex = (peerIndex + 1) % peerIds.size(); // Move to the next peer in round-robin fashion
                            break;
                        }
                    }
                    peerIndex = (peerIndex + 1) % peerIds.size();
                }
            }
        }
        
        updateMetadata(fileDirectory,fileDirectory+"/Meta.txt");

        mergeFiles();
    }
    
    public void checkFileAvailability() {
        HashMap<String, Peer.FileInfo> aggregatedFiles = new HashMap<>();
    
        // Aggregate pieces information from all peers
        for (Peer peer : ClientData.values()) {
            for (Peer.FileInfo fileInfo : peer.FileInfoMap.values()) {
                if (aggregatedFiles.containsKey(fileInfo.fileName)) {
                    Peer.FileInfo existingFileInfo = aggregatedFiles.get(fileInfo.fileName);
                    List<String> updatedPieces = new ArrayList<>(existingFileInfo.piece);
                    updatedPieces.addAll(fileInfo.piece); // Combine pieces from current fileInfo
                    aggregatedFiles.put(fileInfo.fileName, new Peer.FileInfo(fileInfo.fileName, fileInfo.fileSize, updatedPieces));
                } else {
                    aggregatedFiles.put(fileInfo.fileName, new Peer.FileInfo(fileInfo.fileName, fileInfo.fileSize, fileInfo.piece));
                }
            }
        }
    
        // Now check if each file can be completely downloaded
        for (Peer.FileInfo fileInfo : aggregatedFiles.values()) {
            if (fileInfo.isComplete()) {
                System.out.println("Able to download the file: " + fileInfo.fileName);
            } else {
                System.out.println("Unable to download the file: " + fileInfo.fileName + " (Not enough pieces)");
            }
        }
    }

    private void printAllpPeerInfo(){
        for (Map.Entry<String, Peer> entry : ClientData.entrySet()) {
            
            entry.getValue().printInfo();
        }
    }

    private void listenforPeerInfo() throws NumberFormatException, IOException{
        int numPeers = Integer.parseInt(input.readLine());
        for(int i = 0;i <numPeers;i++){
            Peer peer = new Peer();
            peer.peerID = input.readLine();
            peer.peerIP = input.readLine();
            peer.peerPort = Integer.parseInt(input.readLine());
            int numFiles = Integer.parseInt(input.readLine());

            for(int j = 0;j <numFiles;j++){
                
                String fileName = input.readLine();
                int fileSize = Integer.parseInt(input.readLine());
                String[] Spliter = input.readLine().split("\\s+");
                List<String> piece = Arrays.asList(Spliter);

                Peer.FileInfo fileInfo = new Peer.FileInfo(fileName,fileSize,piece);
                peer.FileInfoMap.put(fileName, fileInfo);
                
            }
            ClientData.remove(peer.peerID);
            ClientData.put(peer.peerID, peer);
        }
    }

    private void parseClientinfo() throws NumberFormatException, IOException{
        try{
            output.println("PARSE_CLIENT_INFO");
            output.println(clientID);
            output.println(clientPort);
            int fileCount = Integer.parseInt(fileReader.readLine().trim());
            if (fileCount > 0){ 
                output.println(fileCount); //file count
                for (int i = 0;i <fileCount;i++){
                    output.println(fileReader.readLine().trim()); //File Name
                    String[] Spliter = fileReader.readLine().split("\\s+");
                    output.println(Spliter[0]); //file size
                    output.println(fileReader.readLine()); //File piece client have
                }
                output.println(fileReader.readLine().trim());
            }
            else {
                output.println(0);
            }
            output.println("REQUEST_PEER_INFO");
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseClientinfoAfter() throws NumberFormatException, IOException{
        try{
            fileReader = new BufferedReader(new FileReader(fileDirectory+"/Meta.txt"));
            output.println("PARSE_CLIENT_INFO");
            output.println(clientID);
            output.println(clientPort);
            fileReader.readLine();
            fileReader.readLine();
            fileReader.readLine();
            int fileCount = Integer.parseInt(fileReader.readLine().trim());
            if (fileCount > 0){ 
                output.println(fileCount); //file count
                for (int i = 0;i <fileCount;i++){
                    output.println(fileReader.readLine().trim()); //File Name
                    String[] Spliter = fileReader.readLine().split("\\s+");
                    output.println(Spliter[0]); //file size
                    output.println(fileReader.readLine()); //File piece client have
                }
                output.println(fileReader.readLine().trim());
            }
            else {
                output.println(0);
            }
            output.println("REQUEST_PEER_INFO");
            fileReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteIfNotFull(){
        File directory = new File(fileDirectory);
        File[] allFiles = directory.listFiles();

        if (allFiles == null) {
            System.out.println("Directory is empty or does not exist.");
            return;
        }

        Set<String> completeFiles = new HashSet<>();
        Map<String, List<File>> partFilesMap = new HashMap<>();

        // Identify all complete and part files
        for (File file : allFiles) {
            String fileName = file.getName();
            if (fileName.endsWith(".txt") && !fileName.contains(".part")) {
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                completeFiles.add(baseName);
            } else if (fileName.contains(".part")) {
                String baseName = fileName.substring(0, fileName.indexOf('.'));
                partFilesMap.computeIfAbsent(baseName, k -> new ArrayList<>()).add(file);
            }
        }

        // Log the complete files
        System.out.println("Complete files:");
        for (String completeFile : completeFiles) {
            System.out.println(completeFile);
        }

        // Log the part files map
        System.out.println("Part files map:");
        for (Map.Entry<String, List<File>> entry : partFilesMap.entrySet()) {
            System.out.println("Base name: " + entry.getKey());
            for (File partFile : entry.getValue()) {
                System.out.println("  Part file: " + partFile.getName());
            }
        }

        // Delete part files if the complete file exists
        for (String completeFile : completeFiles) {
            List<File> partFiles = partFilesMap.get(completeFile);
            if (partFiles != null) {
                for (File partFile : partFiles) {
                    if (partFile.delete()) {
                        System.out.println("Deleted part file: " + partFile.getName());
                    } else {
                        System.out.println("Failed to delete part file: " + partFile.getName());
                    }
                }
            }
        }
    }

    private void startCommandInputThread() {
        Thread commandInputThread = new Thread(() -> {
            Map<String, String> commandMap = new HashMap<>();
            commandMap.put("PI", "PEER_INFO");
            commandMap.put("PC", "PEER_CONNECT");
            commandMap.put("PDF", "PEER_DOWNLOAD_FILE");
            commandMap.put("PDP", "PEER_DOWNLOAD_PIECE");
            commandMap.put("SD", "SHUTDOWN");
            commandMap.put("MP", "MERGE_PIECE");
            commandMap.put("UM", "UPDATE_META");
            commandMap.put("SP", "SPLIT_PIECE");
            commandMap.put("CU", "CLEAN_UP");

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    String command = scanner.nextLine();
                    command = commandMap.getOrDefault(command, command); 
                    switch (command) {
                        case "PEER_INFO":
                            printAllpPeerInfo();
                            break;
                        case "PEER_CONNECT":
                            connectToAllPeer();
                            break;
                        case "PEER_DOWNLOAD_FILE":
                            System.out.println("Type in which file to download: ");
                            checkFileAvailability();
                            command = scanner.nextLine();
                            loadBalancedDownload(command);
                            break;
                        case "PEER_DOWNLOAD_PIECE": //for testing download function
                            System.out.println("file index piece");
                            command = scanner.nextLine();
                            String[] parts = command.split(" ");
                            String fileName =parts[0];
                            String pieceId = parts[1];
                            downloadPieceFromString(fileName,pieceId);
                            break;
                        case "SHUTDOWN":
                            output.println("CLOSE_CONNECTION");
                            closeResources();
                        break;
                        case "MERGE_PIECE" :
                            mergeFiles();
                            break;
                        case "UPDATE_META":
                            updateMetadata(fileDirectory,fileDirectory+"/Meta.txt");
                            break;
                        case "SPLIT_PIECE":
                            splitFile();
                            break;
                        case "CLEAN_UP":
                            deleteIfNotFull();
                            break;
                        default:
                            System.out.println("Unknown command.");
                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error handling command input: " + e.getMessage());
                e.printStackTrace();
            }
        });
        commandInputThread.start();
    }

    private void startListeningThread() {
        Thread listeningThread = new Thread(() -> {
            try {
                String command;
                while ((command = input.readLine()) != null) {
                    switch (command) {
                        case "SERVER_CONNECTED":
                            System.out.println("SERVER:" +command);
                            String message = input.readLine();
                            System.out.println(message);
                            break;
                        case "REQUEST_PEER_INFO":
                            System.out.println("SERVER:" +command);
                            listenforPeerInfo();
                            break;
                        case "DOWNLOAD":
                            break;
                        case "CLOSE":
                            return;  // Exit the loop and end the thread if close command is received
                        default:
                            System.out.println("Received unknown command: " + command);
                            break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Error while listening to server: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        listeningThread.start();
    }

    private void closeResources() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (trackerSocket != null && !trackerSocket.isClosed()) {
                trackerSocket.close();
            }
            deleteIfNotFull();
            
            System.out.println("Resources closed successfully.");
            System.exit(0); // Exit the program
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered. Cleaning up resources...");
            closeResources();
        }));
    }

    private void connectServer(String trackerIP,int trackerPort) throws UnknownHostException, IOException{
        this.trackerSocket = new Socket(trackerIP, trackerPort);
        
        this.input = new BufferedReader(new InputStreamReader(trackerSocket.getInputStream()));
        this.output = new PrintWriter(trackerSocket.getOutputStream(), true);

        startListeningThread();
    }
    public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Client <metainfo file path>");
            System.exit(1);
        }
        Client localClient = new Client(args[0]);
        localClient.installShutdownHook();
        localClient.connectServer(localClient.trackerIP, localClient.trackerPort);
        localClient.parseClientinfo();
        localClient.updateMetadata(localClient.fileDirectory, args[0]);
        localClient.startCommandInputThread();
        localClient.startPeerServer();

        localClient.splitFile();
    }
}
