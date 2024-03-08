import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 9101;
	public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            System.out.println("Connected to server: " + socket);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            InetAddress clientAddress = socket.getInetAddress();
            String clientIP = clientAddress.getHostAddress();
            out.println(clientIP);
            if (args.length > 0) {
                if (args[0].equals("list")) {
                    out.println("LIST");
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null && !serverMessage.equals("END")) {
                        System.out.println(serverMessage);
                    }
                } else if (args[0].equals("put")) {
                    if (args.length < 2) {
                        System.err.println("Error: No file specified.");
                        return;
                    }
                    String fileName = args[1];
					System.out.println("client/"+fileName);
                    File file = new File(fileName);
                    if (!file.exists() || !file.isFile()) {
                        System.err.println("Error: File does not exist.");
                        return;
                    }
                    out.println("PUT");
                    out.println(file.getName()); // Send file name to server
                    try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = fileReader.readLine()) != null) {
                            out.println(line); // Send file content line by line
                        }
                    }
                    out.println("END"); // Signal end of file
                    System.out.println("File sent to server successfully.");
                } else {
                    System.err.println("error");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}