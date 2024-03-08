import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private static final int PORT = 9101;
    private static final String SERVER_FILES_DIRECTORY = "serverFiles"; // Corrected directory declaration

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
}
