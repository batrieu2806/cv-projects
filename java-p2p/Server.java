import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Server {
    private ServerSocket serverSocket;
    private static ConcurrentHashMap<String, ClientInfo> ClientData = new ConcurrentHashMap<>();
    static int port;
    
    public Server(int Inport) throws IOException {
        this.serverSocket = new ServerSocket(Inport);
        port = Inport;
        System.out.println("Server started on port " + port);
    }

    public static void broadcastRequestPeers() {
        synchronized (ClientData) {
            for (ClientInfo client : ClientData.values()) {
                client.requestPeers();
            }
        }
    }
    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept(); // Accept a new client connection
                // Start a new thread for each client using the ClientHandler class
                new Thread(new ClientHandler(clientSocket)).start();
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static class ClientInfo{
        private String clientID;
        private String clientIP;
        private int clientPort;
        private PrintWriter out;
        private BufferedReader in;
        private ConcurrentHashMap<Integer, FileInfo> FileInfoMap = new ConcurrentHashMap<>();

        public ClientInfo(){}

        public static class FileInfo{
            private String fileName;
            private int fileSize;
            private List<String> piece;

            public FileInfo(){}
        }
        public void printInfo() {
            System.out.println("Client ID: " + clientID);
            System.out.println("Client IP: " + clientIP);
            System.out.println("Client Port: " + clientPort);
            System.out.println("Files:");
            for (FileInfo fileInfo : FileInfoMap.values()) {
                System.out.println("  File Name: " + fileInfo.fileName);
                System.out.println("  File Size: " + fileInfo.fileSize);
                System.out.println("  Pieces: " + String.join(", ", fileInfo.piece));
            }
        }
        public void requestPeers() {
            out.println("REQUEST_PEER_INFO");
            synchronized (ClientData) {
                out.println(ClientData.size());
                for (Map.Entry<String, ClientInfo> entry : ClientData.entrySet()) {
                    ClientInfo client = entry.getValue();
                    out.println(client.clientID);
                    out.println(client.clientIP);
                    out.println(client.clientPort);
                    out.println(client.FileInfoMap.size());
                    for (Map.Entry<Integer, ClientInfo.FileInfo> fileInfoEntry : client.FileInfoMap.entrySet()) {
                        ClientInfo.FileInfo fileInfo = fileInfoEntry.getValue();
                        out.println(fileInfo.fileName);
                        out.println(fileInfo.fileSize);
                        out.println(String.join(" ", fileInfo.piece));
                    }
                }
            }
        }
    }


    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ClientInfo localClient;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            localClient = new ClientInfo();
            localClient.out =  new PrintWriter(clientSocket.getOutputStream(), true);
            localClient.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
        }

        private void parseClientinfo() throws IOException{
            localClient.clientID = localClient.in.readLine();
            localClient.clientIP = clientSocket.getInetAddress().getHostAddress();
            localClient.clientPort = Integer.parseInt(localClient.in.readLine());

            localClient.FileInfoMap.clear();
            
            System.out.println("clientID: " + localClient.clientID);
            int fileCount = Integer.parseInt(localClient.in.readLine()); //file count
            System.out.println("fileCount: " + fileCount);
            for (int i = 0;i <fileCount;i++){
                ClientInfo.FileInfo fileInfo = new ClientInfo.FileInfo();
                fileInfo.fileName = localClient.in.readLine();
                System.out.println("fileName: " + fileInfo.fileName);
                fileInfo.fileSize = Integer.parseInt(localClient.in.readLine());
                String filePiece = localClient.in.readLine();
                fileInfo.piece = Arrays.asList(filePiece.split("\\s+"));
                localClient.FileInfoMap.put(i,fileInfo);
            }
            synchronized (ClientData) {
                ClientData.remove(localClient.clientID);
                ClientData.put(localClient.clientID, localClient);
            }
        }

        private void removeClient() {
            synchronized (ClientData) {
                if (localClient.clientID != null && ClientData.containsKey(localClient.clientID)) {
                    ClientData.remove(localClient.clientID);
                    System.out.println("Removed client " + localClient.clientID + " from active clients list.");
                }
            }
        }
        private void closeResources() {
            try {
                if (localClient.out != null) {
                    localClient.out.close();
                }
                if (localClient.in != null) {
                    localClient.in.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }

        public void run() {
            try{
                localClient.out.println("SERVER_CONNECTED");
                localClient.out.println("Connected to tracker server On port " + port);
                while (!clientSocket.isClosed()) {
                    try {
                        String command = localClient.in.readLine();
                        if (command != null) {
                            switch (command) {
                                case "PARSE_CLIENT_INFO":
                                    System.out.println(command);
                                    parseClientinfo();
                                    break;
                                case "REQUEST_PEER_INFO":
                                    System.out.println(command);
                                    broadcastRequestPeers();
                                    break;
                                case "CLOSE_CONNECTION":
                                    removeClient();
                                    clientSocket.close();
                                    break;
                                // Handle other commands as needed
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading client command: " + e.getMessage());
                        break; // Exit the loop and end the thread on exception
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in client handler: " + e.getMessage());
            } finally {
                removeClient();
                closeResources();
                broadcastRequestPeers();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        try {
            Server server = new Server(5000);
            server.start();
        } catch (IOException e) {
            System.out.println("Could not start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
