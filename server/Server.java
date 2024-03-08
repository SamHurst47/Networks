import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Server {
    private static final int PORT = 9101;
    private static final String SERVER_FILES_DIRECTORY = "serverFiles"; // Corrected directory declaration
    private static final String LOG_FILE = "Log.txt";
    private static int first = 0;
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String clientMessage = in.readLine();
                
                File directory = new File(SERVER_FILES_DIRECTORY);
                String[] files = directory.list();
                
                if (clientMessage != null && clientMessage.equals("LIST")) {
                    System.out.println("Received 'LIST' command from client. Sending File Names");
                    logAction(0,serverSocket);
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    if (files != null && files.length > 0) {
                        for (String file : files) {
                            out.println(file);
                        }
                        out.println("END"); // Signal the end of file list
                    } else {
                        out.println("No files found in server directory.");
                        out.println("END"); // Signal the end of communcations list
                    } 
				} else if (clientMessage != null && clientMessage.equals("PUT")) {
    				System.out.println("Received 'PUT' command from client. Receiving file...");
                    logAction(1,serverSocket);
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
                        try (PrintWriter fileWriter = new PrintWriter(new FileWriter("./serverFiles/"+filename))) {
                            fileWriter.println(fileContent.toString());
                            System.out.println("File received and written to the server.");
                        } catch (IOException e) {	
                            e.printStackTrace();
                            System.err.println("Error writing file on server.");
                        }
                    } else {
                        System.err.println("Error file already on server.");
                    }
                } else {
                    System.err.println("Invalid command received from client.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logAction(int type, ServerSocket clientSocket) {
        try {
            // Check if it's the first time logAction is called in this session
            if (first == 0) {
                
                File logFile = new File(LOG_FILE);
                boolean created = logFile.createNewFile(); // Attempt to create new file
                
                if (!created) {
                    System.err.println("Failed to create log file."); // Print error message if file creation fails
                }
                first = 1;
            }
            
            // Now, open the file for writing
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
