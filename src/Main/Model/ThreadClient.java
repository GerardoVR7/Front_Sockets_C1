
package Main.Model;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ThreadLocalRandom;

public class ThreadClient extends Observable implements Runnable {
    private Socket socket;
    private DataInputStream bufferDeEntrada = null;
    private ListView<String> listChat;

    public ThreadClient(Socket socket, ListView<String> listChat) {
        this.socket = socket;
        this.listChat = listChat;
    }

    public void run() {

        try {
            bufferDeEntrada = new DataInputStream(socket.getInputStream());

            String st = "";
            do {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(1000L)+100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    st = bufferDeEntrada.readUTF();

                    String[] array = st.split(":");

                    /*if (!array[0].equals("2")) {
                        listChat.getItems().add(st);
                    }*/

                    System.out.println("MENSAJE EN EL HILO");
                    this.setChanged();
                    this.notifyObservers(st);
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }while (!st.equals("FIN"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


