import java.io.*;
import java.net.*;

public class Client{
    private static InetAddress host;
    private static final int PORT=1234;
    public static void main(String[] args) throws IOException{
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Host id not found!!");
            System.exit(-1);
        }
        listenSocket();
    } 
    public static void listenSocket(){
        Socket link = null;
        try {
            link = new Socket (host, PORT);
        } catch (IOException e){
            System.out.println("Unable to connect");
            System.exit(-1);
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
            PrintWriter out = new PrintWriter(link.getOutputStream(),true);

            BufferedReader userentry = new BufferedReader(new InputStreamReader(System.in));
            String message, response;

            do {
                System.out.print("Enter message:");
                message = userentry.readLine();
                out.println(message);
                response = in.readLine();
                System.out.println("\nSERVER>"+ response);

            } while (!message.equals("close"));
        } catch(IOException e){
            System.out.println("Message is not sent.");
        }
    }
}