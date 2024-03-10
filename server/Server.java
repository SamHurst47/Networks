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
    private static final int MAX_THREADS = 20;

    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        try {
            // Display server start message
            System.out.println("Server: Server Started.");

            // Delete existing log file if it exists
            File logFile = new File(LOG_FILE);
            if (logFile.exists()) {
                logFile.delete();
            }

            // Create a new log file
            logFile.createNewFile();
            System.out.println("Server: Log file generated.");

            // Create a fixed-size thread pool for handling client connections
            ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

            // Start server socket and accept incoming client connections
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Server: Client connected.");

                    // Handle each client connection in a separate thread
                    threadPool.execute(new ServerThread(clientSocket));

                }
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
                // Set up input and output streams for the client socket
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Read client command
                String clientMessage = in.readLine();
                File directory = new File(SERVER_FILES_DIRECTORY);
                String[] files = directory.list();

                // Process client command
                if (clientMessage != null) {
                    if (clientMessage.equals("OP1")) {
                        // Handle LIST command
                        handleListCommand(out, files);
                    } else if (clientMessage.equals("OP2")) {
                        // Handle PUT command
                        handlePutCommand(in, out, files);
                    } else {
                        // Invalid command received
                        System.err.println("Server Error: Invalid command '" + clientMessage + "' received from client.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Close client socket
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Server: Client Disconnected.");
        }
    }

    private static void handleListCommand(PrintWriter out, String[] files) {
        try {
            System.out.println("Server: Received 'LIST' command.");
            logAction(0);
            out.println(files.length); // Send number of files to client
            if (files != null && files.length > 0) {
                // Send list of files to client
                for (String file: files) {
                    out.println(file);
                }
                out.println("END"); // Signal the end of file list
                System.out.println("Server: Files on Server list sent to client.");
            } else {
                // No files on server
                System.err.println("Server Error: No files on Server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handlePutCommand(BufferedReader in , PrintWriter out, String[] files) {
        try {
            System.out.println("Server: Received 'PUT' command.");
            logAction(1);
            String filename = in.readLine(); // Receive filename from client
            boolean filenameExists = false;
            if (filename != null) {
                // Check if filename already exists on server
                for (String curfile: files) {
                    if (curfile.equals(filename)) {
                        filenameExists = true;
                        break;
                    }
                }
            }
            if (!filenameExists) {
                // Receive file content from client and save to server
                receiveFile(in, filename);
                out.println("S"); // Signal successful file transfer to client
            } else {
                // File with same name already exists on server
                System.err.println("Server Error: File '" + filename + "' already exists on server.");
                out.println("F"); // Signal file exists error to client
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(BufferedReader in , String filename) {
        try {
            // Create server files directory if it doesn't exist
            File directory = new File(SERVER_FILES_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            // Create file to save received content
            File file = new File(directory, filename);
            PrintWriter fileWriter = new PrintWriter(new FileWriter(file));
            String line;
            // Receive file content line by line from client
            while ((line = in.readLine()) != null && !line.equals("END")) {
                fileWriter.println(line);
            }
            fileWriter.close(); // Close file writer
            System.out.println("Server: File '" + filename + "' received and successfully written to the server.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Server Error: Failed to write file '" + filename + "' on server.");
        }
    }

    private static void logAction(int type) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            LocalDateTime timestamp = LocalDateTime.now();
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy|HH:mm:ss"));
            String requestType = (type == 1) ? "PUT" : "LIST";
            InetAddress localhost = InetAddress.getLocalHost();
            String clientIP = localhost.getHostAddress().trim();

            // Write log entry to log file
            writer.write(formattedTimestamp + "|" + clientIP + "|" + requestType);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}