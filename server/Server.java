import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9101;
    private static final String SERVER_FILES_DIRECTORY = "serverFiles";
    private static final String LOG_FILE = "Log.txt";
    private static final int MAX_THREADS = 20; // Maximum number of concurrent connections

    public static void main(String[] args) {
        try {
            // Delete existing log file if it exists
            File logFile = new File(LOG_FILE);
            if (logFile.exists()) {
                logFile.delete();
            }

            // Create a new log file
            logFile.createNewFile();

            // Create a fixed-size thread pool
            ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

            // Start server socket
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server listening on port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Execute a new ServerThread for each client connection
                threadPool.execute(new ServerThread(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ServerThread extends Thread {
        private Socket clientSocket;

        public ServerThread(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String clientMessage = in.readLine();
                File directory = new File(SERVER_FILES_DIRECTORY);
                String[] files = directory.list();

                if (clientMessage != null) {
                    if (clientMessage.equals("OP1")) {
                        System.out.println("Received 'LIST' command from client. Sending File Names");
                        logAction(0, clientSocket);
                        out.println(files.length);
                        if (files != null && files.length > 0) {
                            for (String file : files) {
                                out.println(file);
                            }
                            out.println("END"); // Signal the end of file list
                        } else {
                            System.out.println("No files found in SeverFiles");
                        }
                    } else if (clientMessage.equals("OP2")) {
                        System.out.println("Received 'PUT' command from client. Receiving file...");
                        logAction(1, clientSocket);
                        clientMessage = in.readLine();
                        String filename = clientMessage;
                        boolean filenameExists = false;
                        if (filename != null) {
                            for (String curfile : files) {
                                if (curfile.equals(filename)) {
                                    filenameExists = true;
                                    break;
                                }
                            }
                        }
                        if (!filenameExists) {
                            // Receive file content from the client
                            StringBuilder fileContent = new StringBuilder();
                            String line;
                            while ((line = in.readLine()) != null && !line.equals("END")) {
                                fileContent.append(line).append("\n");
                            }

                            // Write received file content to a file on the server
                            try (PrintWriter fileWriter = new PrintWriter(new FileWriter("./serverFiles/" + filename))) {
                                fileWriter.println(fileContent.toString());
                                System.out.println("File received and written to the server.");
                                out.println("S");
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.println("Error: writing file on server.");
                            }
                        } else {
                            System.err.println("Error: file already on server.");
                            out.println("F");
                        }
                    } else {
                        System.err.println("Error: Invalid command received from client.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void logAction(int type, Socket clientSocket) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                LocalDateTime timestamp = LocalDateTime.now();
                String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy|HH:mm:ss"));

                // Getting client IP address
                InetAddress clientAddress = clientSocket.getInetAddress();
                String clientIP = clientAddress.getHostAddress();

                // Determine request type
                String requestType;
                if (type == 1) {
                    requestType = "PUT";
                } else {
                    requestType = "LIST";
                }

                writer.write(formattedTimestamp + "|" + clientIP + "|" + requestType);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
