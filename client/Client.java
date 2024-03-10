import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 9101;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Check if command line arguments are provided
            if (args.length > 0) {
                // Check the command provided
                if (args[0].equals("list")) {
                    // Handle the 'list' command
                    handleListCommand(out, in);
                } else if (args[0].equals("put")) {
                    // Handle the 'put' command
                    handlePutCommand(args, out, in);
                } else {
                    // Print error message for invalid command
                    System.err.println("Error: Invalid command '" + args[0] + "'. Usage: java Client [list | put filename]");
                }
            } else {
                // Print error message for no command provided
                System.err.println("Error: No command given. Usage: java Client [list | put filename]");
            }
        } catch (IOException e) {
            // Handle IOException
            e.printStackTrace();
        }
    }

    private static void handleListCommand(PrintWriter out, BufferedReader in ) throws IOException {
        out.println("OP1"); // Send command to server to list files
        String serverMessage;
        String lines = in.readLine(); // Receive number of files from server
        if (lines.equals("0")) {
            System.err.println("Error: No files are stored on the server.");
        } else {
            System.out.println("Listing " + lines + " file(s) from the server:");
            while ((serverMessage = in.readLine()) != null && !serverMessage.equals("END")) {
                System.out.println(serverMessage); // Display the list of files received from the server
            }
        }
    }

    private static void handlePutCommand(String[] args, PrintWriter out, BufferedReader in ) throws IOException {
        if (args.length < 2) {
            System.err.println("Error: No file specified. Usage: java Client put filename");
            return;
        }
        String fileName = args[1];
        File file = new File(fileName);
        if (!file.exists() || !file.isFile()) {
            System.err.println("Error: File '" + fileName + "' does not exist.");
            return;
        }
        out.println("OP2"); // Signal server for file transfer
        out.println(file.getName()); // Send file name to server
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                out.println(line); // Send file content line by line to server
            }
        } catch (IOException e) {
            System.err.println("Error: Failed to read from file '" + file.getName() + "'.");
            return;
        }
        out.println("END"); // Signal end of file
        System.out.println("File '" + fileName + "' sent to server successfully.");
        String result = in.readLine(); // Receive confirmation from server
        handleServerResponse(fileName, result);
    }

    private static void handleServerResponse(String fileName, String result) {
        if (result.equals("F")) {
            System.err.println("Error: File '" + fileName + "' already exists on server.");
        } else if (result.equals("S")) {
            System.out.println("File '" + fileName + "' successfully saved on server.");
        } else {
            System.err.println("Error: Server error for argument.");
        }
    }
}