package Main.controller;

import Main.Model.ThreadClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Controller implements  Observer {
    private Socket socket;
    private DataOutputStream bufferDeSalida = null;

    byte[] ipBytes = {(byte) 192, (byte) 168, (byte) 0, (byte) 7};
    InetAddress ipLocal = null;
    private final int PORT = 3001;
    private ArrayList<String> listUUsuarios = new ArrayList<>();
    final FileChooser fileChooser = new FileChooser();
    private Image imagen1 ;
    private  ArrayList<String> historial = new ArrayList<>();
    private  String myUser = "";


    @FXML
    private Button btnSalir;

    @FXML
    private TextField txtDestino;

    @FXML
    private TextField ipServer;

    @FXML
    private TextField nameUser;

    @FXML
    private ListView<String> listChat;

    @FXML
    private TextField txtEnviar;

    @FXML
    private Button btnEnviar;

    @FXML
    private Circle circle;

    @FXML
    private ComboBox<String> boxGrupos;

    @FXML
    private ImageView myImage;

    @FXML
    private Label userId;

    @FXML
    void btnConectarOnMouseClicked(MouseEvent event) {
        try {
            ipLocal = InetAddress.getByAddress(ipBytes);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            socket = new Socket(ipLocal, PORT);
            listChat.getItems().add( "Chat creado");
            bufferDeSalida = new DataOutputStream(socket.getOutputStream());
            bufferDeSalida.flush();

            //bufferDeSalida.writeUTF(nameUser.getText());

            ThreadClient cliente = new ThreadClient(socket,listChat);
            cliente.addObserver(this);
            new Thread(cliente).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnSalirOnMouseClicked(MouseEvent event) {
        Platform.exit();
        System.exit(1);

    }

    @FXML
    void btnCerrarOnMouseClicked(MouseEvent event) {
        try {
            socket.close();
            System.out.println("Cerrando...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnEnviarOnMouseClicked(MouseEvent event) {
        try {
            String tipoMnesaje = "3:";
            String paquete_envio = "";

            paquete_envio = tipoMnesaje + myUser +":"+ boxGrupos.getSelectionModel().getSelectedItem().toString() +":"+ txtEnviar.getText();
            System.out.println(myUser);
            bufferDeSalida.writeUTF(paquete_envio);
            bufferDeSalida.flush();

            Platform.runLater( () -> listChat.getItems().add("Tu: " + txtEnviar.getText()));


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    void selectChat(ActionEvent event) {

    }

    @FXML
    void addImage(ActionEvent event) {

        String tipoMnesaje = "2:";
        String buscado = "";
        try {

            File file = fileChooser.showOpenDialog(new Stage());

            if(file.isFile() &&
                    (file.getName().contains(".jpg") || file.getName().contains(".png") || file.getName().contains(".bmp") || file.getName().contains(".gif"))){

                buscado = file.toURI().toURL().toString();
                imagen1 = new Image(buscado);
                Platform.runLater(() -> myImage.setImage(imagen1));

                System.out.println(buscado);
            }

            String paquete_envio = tipoMnesaje + myUser + ":" + boxGrupos.getSelectionModel().getSelectedItem().toString() + ":" + buscado +":"+ buscado;
            bufferDeSalida.writeUTF(paquete_envio);
            bufferDeSalida.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }





    @Override
    public void update(Observable o, Object arg) {
        //Platform.runLater(() -> myImage.setImage(null));
        String paquete_recibido = (String) arg;
        System.out.println("update cliente funcionando");
        System.out.println(paquete_recibido);
        String[] datagrama = paquete_recibido.split(":" );

        switch (datagrama[0]){
            case "1":
                System.out.println("MENSAJE DEL SERVIDOR");
                System.out.println(paquete_recibido);
                //Platform.runLater(() -> boxGrupos.getItems().add());
                listUUsuarios.clear();
                Platform.runLater(() -> boxGrupos.getItems().clear() );
                Platform.runLater(() -> userId.setText(datagrama[2]));
                myUser = datagrama[2];
                System.out.println(listUUsuarios.isEmpty());
                boxGrupos.getItems().addAll(listUUsuarios);
                System.out.println(datagrama.length);
                for(int x=0; x < datagrama.length ; x++){

                    if( x > 2){
                        listUUsuarios.add(datagrama[x]);
                    }

                }

                Platform.runLater(() -> boxGrupos.getItems().addAll(listUUsuarios) );
                break;

            case "2":
                System.out.println("LLEGO UNA IMMAGEN");
                Platform.runLater(() -> listChat.getItems().add(("\n" + datagrama[3] + ": " + datagrama[4] + ":" + datagrama[5])));
                System.out.println(datagrama[6]);
                String loadImage =  datagrama[6] + ":" +  datagrama[7];
                imagen1 = new Image( loadImage );
                Platform.runLater(() -> myImage.setImage(imagen1));
                break;

            case "3":
                System.out.println("LLEGO UN MENSAJE");
                Platform.runLater(() -> listChat.getItems().add((datagrama[3] + ": " + datagrama[4] )));


                break;

            case "4":


                break;

            default:
                System.out.println("ALGO NO FUNCIONA");
                break;
        }


    }
}
