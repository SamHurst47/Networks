import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 9101;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            // Initialize output and input streams
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (args.length > 0) {
                // Handle command line arguments
                if (args[0].equals("list")) {
                    // Send command to server to list files
                    out.println("OP1");
                    String serverMessage;
                    // Receive number of files from server
                    String lines = in.readLine();
                    if (lines.equals("0")) {
                        // Handle case where no files are stored on the server
                        System.err.println("Error: No files are stored on the server.");
                    } else {
                        // Display the list of files received from the server
                        System.out.println("Listing " + lines + " file(s) from the server:");
                        while ((serverMessage = in.readLine()) != null && !serverMessage.equals("END")) {
                            System.out.println(serverMessage);
                        }
                    }
                } else if (args[0].equals("put")) {
                    // Handle 'put' command
                    if (args.length < 2) {
                        // Ensure a filename is provided for the put command
                        System.err.println("Error: No file specified. Usage: java Client put filename");
                    }
                    String fileName = args[1];
                    File file = new File(fileName);
                    if (!file.exists() || !file.isFile()) {
                        // Check if the file exists
                        System.err.println("Error: File '" + fileName + "' does not exist.");
                        return;
                    }
                    out.println("OP2"); // Signal server for file transfer
                    out.println(file.getName()); // Send file name to server
                    try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                        String line;
                        // Send file content line by line to server
                        while ((line = fileReader.readLine()) != null) {
                            out.println(line);
                        }
                    } catch (IOException e) {
                        System.err.println("Error: Failed to read from file '" + file.getName() + "'.");
                        return;
                    }                    
                    out.println("END"); // Signal end of file
                    System.out.println("File '" + fileName + "' sent to server successfully.");
                    String result = in.readLine(); // Receive confirmation from server
                    handleServerResponse(fileName, result);
                } else {
                    // Handle invalid command
                    System.err.println("Error: Invalid command '" + args[0] + "'. Usage: java Client [list | put filename]");
                }
            } else {
                // Handle case where no command is given
                System.err.println("Error: No command given. Usage: java Client [list | put filename]");
            }
        } catch (IOException e) {
            // Handle IO exceptions
            e.printStackTrace();
        }
    }

    private static void handleServerResponse(String fileName, String result) {
        if (result.equals("F")) {
            // Handle case where file already exists on server
            System.err.println("Error: File '" + fileName + "' already exists on server.");
        } else if (result.equals("S")) {
            // Confirm successful file transfer
            System.out.println("File '" + fileName + "' successfully saved on server.");
        } else {
            // Handle any unexpected server errors
            System.err.println("Error: Server error for argument.");
        }
    }
}
