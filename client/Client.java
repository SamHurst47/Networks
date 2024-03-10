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
            System.out.println("Connected to server: " + socket);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (args.length > 0) {
                if (args[0].equals("list")) {
                    out.println("OP1");
                    String serverMessage;
                    String lines = in.readLine();
                    if (lines.equals("0")) {
                        System.err.println("Error: No files are stored on  the server.");
                        return;
                    } else {
                        System.out.println("Listing "+ lines +" file(s) from the server:");
                        while ((serverMessage = in.readLine()) != null && !serverMessage.equals("END")) {
                            System.out.println(serverMessage);
                        }
                        return;
                    }   
                } else if (args[0].equals("put")) {
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
                    out.println("OP2");
                    out.println(file.getName()); // Send file name to server
                    try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = fileReader.readLine()) != null) {
                            out.println(line); // Send file content line by line
                        }
                    }
                    out.println("END"); // Signal end of file
                    System.out.println("File '" + fileName + "' sent to server successfully.");
                    String result  = in.readLine();
                    if (result.equals("F")) {
                        System.err.println("Error: File '" + fileName + "' already exists on server.");
                        return;
                    } else if (result.equals("S")) {
                        System.out.println("File '" + fileName + "' successfully saved on server.");
                    } else {
                        System.err.println("Error: Server error for argument.");
                    }
                } else {
                    System.err.println("Error: Invalid command '"+ args[0] +"'. Usage: java Client [list | put filename]");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
