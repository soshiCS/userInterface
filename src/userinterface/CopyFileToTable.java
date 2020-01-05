/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userinterface;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CopyFileToTable extends Application {
  // Text file info
      public static String encrypt(String strClearText,String strKey) throws Exception{
	String strData="";
	
	try {
		SecretKeySpec skeyspec=new SecretKeySpec(strKey.getBytes(),"Blowfish");
		Cipher cipher=Cipher.getInstance("Blowfish");
		cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
		byte[] encrypted=cipher.doFinal(strClearText.getBytes());
		strData=new String(encrypted);
		
	} catch (Exception e) {
		e.printStackTrace();
		throw new Exception(e);
	}
	return strData;
}
    public static String decrypt(String strEncrypted,String strKey) throws Exception{
	String strData="";
	
	try {
		SecretKeySpec skeyspec=new SecretKeySpec(strKey.getBytes(),"Blowfish");
		Cipher cipher=Cipher.getInstance("Blowfish");
		cipher.init(Cipher.DECRYPT_MODE, skeyspec);
		byte[] decrypted=cipher.doFinal(strEncrypted.getBytes());
		strData=new String(decrypted);
		
	} catch (Exception e) {
		e.printStackTrace();
		throw new Exception(e);
	}
	return strData;
}
      OutputStream toServer = null;
  InputStream fromServer = null;
      final Map<String, Object> user = new HashMap<>();
  
  private TextField tfFilename = new TextField();
  private TextArea taFile = new TextArea();

  // JDBC and table info
  private ComboBox<String> cboURL = new ComboBox<>();
  private ComboBox<String> cboDriver = new ComboBox<>();
  private TextField tfUsername = new TextField();
  private PasswordField pfPassword = new PasswordField();
  private TextField tfTableName = new TextField();
  private TextField Name = new TextField();
  private TextField lastName  = new TextField();
private TextField Query = new TextField();
  private Button btViewFile = new Button("View File");
  private Button btCopy = new Button("insert");
  private Button btQuery = new Button("Query");
  private Label lblStatus = new Label();

  @Override // Override the start method in the Application class
  public void start(Stage primaryStage) throws IOException{
        

      Socket socket = new Socket("localhost", 8000);
     OutputStream out = socket.getOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
      InputStream inp = socket.getInputStream();
     fromServer = new DataInputStream(socket.getInputStream());
      toServer = new DataOutputStream(socket.getOutputStream());

 // BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));


    cboURL.getItems().addAll(FXCollections.observableArrayList(
      "jdbc:mysql://localhost/javabook",
      "jdbc:mysql://liang.armstrong.edu/javabook",
      "jdbc:odbc:exampleMDBDataSource",
      "jdbc:oracle:thin:@liang.armstrong.edu:1521:orcl"));
    cboURL.getSelectionModel().selectFirst();
    
    cboDriver.getItems().addAll(FXCollections.observableArrayList(
      "com.mysql.jdbc.Driver", "sun.jdbc.odbc.dbcOdbcDriver",
      "oracle.jdbc.driver.OracleDriver"));
    cboDriver.getSelectionModel().selectFirst();
    
    // Create UI for connecting to the database 
    GridPane gridPane = new GridPane();
    gridPane.add(new Label("JDBC Driver"), 0, 0);
    gridPane.add(new Label("Database URL"), 0, 1);
    gridPane.add(new Label("Username"), 0, 2);
    gridPane.add(new Label("Password"), 0, 3);
    gridPane.add(new Label("Table Name"), 0, 6);
    gridPane.add(new Label("First Name"),0,4);
    gridPane.add(new Label("Last Name"),0,5);
    gridPane.add(cboURL, 1, 0);
    gridPane.add(cboDriver, 1, 1);
    gridPane.add(tfUsername, 1, 2);
    gridPane.add(pfPassword, 1, 3);
    gridPane.add(Name, 1, 4);
    gridPane.add(lastName, 1, 5);
    gridPane.add(tfTableName, 1, 6);
    
    HBox hBoxConnection = new HBox(10);
    hBoxConnection.getChildren().addAll(lblStatus, btCopy,btQuery);
    hBoxConnection.setAlignment(Pos.CENTER_RIGHT);

    VBox vBoxConnection = new VBox(5);
    vBoxConnection.getChildren().addAll(
      new Label("Target Database Table"),
      gridPane, hBoxConnection);
    
    gridPane.setStyle("-fx-border-color: black;");

    BorderPane borderPaneFileName = new BorderPane();
    borderPaneFileName.setLeft(new Label("Filename"));
    borderPaneFileName.setCenter(tfFilename);
    borderPaneFileName.setRight(btViewFile);
            
    BorderPane borderPaneFileContent = new BorderPane();
    borderPaneFileContent.setTop(borderPaneFileName);
    borderPaneFileContent.setCenter(taFile);
    
    BorderPane borderPaneFileSource = new BorderPane();
    borderPaneFileSource.setTop(new Label("Source Text File"));
    borderPaneFileSource.setCenter(borderPaneFileContent);
                
    SplitPane sp = new SplitPane();
    sp.getItems().addAll(borderPaneFileSource, vBoxConnection);
    
    // Create a scene and place it in the stage
    Scene scene = new Scene(sp, 680, 230);
    primaryStage.setTitle("CopyFileToTable"); // Set the stage title
    primaryStage.setScene(scene); // Place the scene in the stage
    primaryStage.show(); // Display the stage    

 //   btViewFile.setOnAction(e -> showFile());
    btCopy.setOnAction(e -> {

        // if(tfUsername.getText().trim() !=""&& pfPassword.getText().trim() != ""&&Name.getText().trim() != ""&&lastName.getText().trim() != ""){
        try {
                    DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
      //  DataInputStream fromServer = new DataInputStream(socket.getInputStream());
      String encrypted = encrypt(pfPassword.getText().trim(),"ad");
        user.put("username",tfUsername.getText().trim());
    user.put("password",pfPassword.getText().trim());
    user.put("firstname",Name.getText().trim());
    user.put("lastname", lastName.getText().trim());
    user.put("action", "insert");
            //toServer.writeUTF(cboDriver.getSelectionModel().getSelectedItem().trim());
          //toServer.writeUTF(cboURL.getSelectionModel().getSelectedItem().trim());
          //toServer.writeBoolean(user);
          objectOutputStream.reset();
          objectOutputStream.writeObject(user);
          objectOutputStream.flush();
          toServer.flush();
        
        //  toServer.writeUTF(Query.getText().trim());

        
          System.out.println("it is sent");
        }
        catch (Exception ex) {
          lblStatus.setText(ex.toString());
        } 
        /* else {
             try {
                 user.put("action", "nothing");
                 objectOutputStream.reset();
                 objectOutputStream.writeObject(user);
             } catch (IOException ex) {
                 Logger.getLogger(CopyFileToTable.class.getName()).log(Level.SEVERE, null, ex);
             }
         }*/
    });
    btQuery.setOnAction(e->{
        
  //         if(((String)tfUsername.getText()).compareTo("")!= 0 && ((String) pfPassword.getText()).compareTo("")!=0){
try{
         DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
        DataInputStream fromServer = new DataInputStream(socket.getInputStream());
    user.put("action", "query");
 user.put("username",tfUsername.getText().trim());
    user.put("password",pfPassword.getText().trim());
         objectOutputStream.reset();
    objectOutputStream.writeObject(user);
    objectOutputStream.flush();
    boolean bool = true;
System.out.println("nagaeidimon");
    
    String name = fromServer.readUTF();
     String lastname = fromServer.readUTF();  
     fromServer.reset();
     bool = false;
taFile.appendText(name+ " ");
taFile.appendText(lastname + "\n");
//fromServer.reset();
System.out.println("gaeidimon");

    
}        catch (Exception ex) {
          lblStatus.setText(ex.toString());
        }
//}

    });

  }

  /** Display the file in the text area */

  /**
   * The main method is only needed for the IDE with limited
   * JavaFX support. Not needed for running from the command line.
   */
  public static void main(String[] args) {
    launch(args);
  }
}

