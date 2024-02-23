import java.io.*;
import java.net.*;

public class Server{
    private static ServerSocket servSock;
    private static final int PORT=1234;
    public static void main(String[] args) throws IOException{
        System.out.println("Opening port .....");
        while (true) {
            listenSocket();
        }
    }
    public static void listenSocket(){
        try {
            servSock = new ServerSocket(PORT);
        } catch(IOException e) {
            System.out.println("Unable to create socket with port no:1234!");
            System.exit(-1);
        }
        Socket link = null;
        try{
            link = servSock.accept();
        } catch(IOException e) {
            System.out.println("Accept failed: Port 1234");
        }
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
            PrintWriter out = new PrintWriter(link.getOutputStream(),true);
            int numMessages = 0;
            String message = in.readLine();
            while(!message.equals("close")){
                System.out.println("Message recieved.");
                numMessages ++;
                out.println("Message" + numMessages+ ":" + message);
                message = in.readLine();
            }
        } catch(IOException e) {
            System.out.println("Message is not recieved");
        }
    }


}