package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class encryptorController {
	
	private Stage primaryStage=new Stage();
	File file;
	String algorithm="";
	
    @FXML
    private Label titleLbl;
    
    @FXML
    private Label fileAddressLbl;
    
    @FXML
    private Button fileBtn;
    
    @FXML
    private Button decryptButton;
    
    @FXML
    private PasswordField pwdInput;
    
    @FXML
    private ToggleGroup radioMenu;
    
    @FXML
    private Button resetBtn;
    
    /* only the first radio button is given 
    a name as I want to reset the GUI with the 
    first algorithm selected in the toggle group*/
    @FXML
    private RadioButton md1Radio;
    
    @FXML
    private CheckBox openFileCheckbox;
    
    //updates GUI when mouse enters file label boundary
    @FXML
    void dragDetected(DragEvent event) {
    	fileAddressLbl.setBackground(new Background(new BackgroundFill(
    			Color.GREENYELLOW, new CornerRadii(10.0), Insets.EMPTY)));
    	fileAddressLbl.setText("      Drop files here");
    }
    
    //updates GUI when mouse leaves file label boundary
    @FXML
    void dragDetectExit(DragEvent event) {
    	try {
    		if (file.exists()) {
    			fileAddressLbl.setBackground(new Background(new BackgroundFill(
    					Color.GREENYELLOW,  new CornerRadii(10.0), Insets.EMPTY)));
    			fileAddressLbl.setText(file.getName());
    		}
    	}catch(Exception e) {
			fileAddressLbl.setBackground(new Background(new BackgroundFill(
					Color.rgb(199, 204, 214),  CornerRadii.EMPTY, Insets.EMPTY)));
			fileAddressLbl.setText("      Drop files here");
    	}
    	
    }
    
    //checks if mouse is dragged over label with a file
    @FXML
    void dragDetectedOver(DragEvent event) {
    	if (event.getGestureSource() != fileAddressLbl &&
                event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    	event.consume();
    }
    
    //initialises input file when a file is dropped onto the file label
    @FXML
    void dragDrop(DragEvent event) {
    	Dragboard board=event.getDragboard();
    	boolean success=false;
    	if (board.hasFiles()) {
    		try {
        		file=board.getFiles().get(0);
        		success=true;
        		event.setDropCompleted(success);
        		event.consume();
        		setUpFileNodes();
    		}catch(Exception e) {
    			success=false;
    		}
    	}
    	
    }
    
    @FXML
    void openFileChooser(MouseEvent event) {
    	try {
    		//this button acts as a choose file and a encryption button
    		if(!fileBtn.getText().equals("Encrypt")) {
        		FileChooser fileChooser=new FileChooser();
            	file=fileChooser.showOpenDialog(this.getStage());
            	//update GUI
            	setUpFileNodes();
        	}else {
        		//get selected algorithm
        		RadioButton algoRadio=(RadioButton) radioMenu.getSelectedToggle();
            	algorithm=algoRadio.getText();
        		
            	//check if user wants to encrypt file without a password
        		if (pwdInput.getText().equals("")) {
        			Alert noPasswordAlert=new Alert(AlertType.NONE,
        					"Are you sure you want to encrypt without a password?",
        					ButtonType.YES,ButtonType.NO);
        			noPasswordAlert.showAndWait();
        			if (noPasswordAlert.getResult()==ButtonType.YES) {
        				checkCancel(false);
        			}
        		}else {
        			checkCancel(false);
        		}
        	}
    	}catch(Exception e) {
    		reset();
    	}
    	
    }
    
    //method to check if the user cancelled selecting a file directory 
    //calls encryption/decryption method if a directory is selected
    public void checkCancel(boolean callDecryptMethod) throws Exception {
    	DirectoryChooser directoryChooser=new DirectoryChooser();
    	File address=directoryChooser.showDialog(primaryStage);
    	
    	if(address!=null) {
    		if (callDecryptMethod) {
    			decrypt(pwdInput.getText(),address);
    		}else {
    			encryptFile(pwdInput.getText(),address);
    		}
    	}
    }
    
    @FXML
    void decryptFile(MouseEvent event) {
    	try {
    		//decrypt button 
    		RadioButton algoRadio=(RadioButton) radioMenu.getSelectedToggle();
        	algorithm=algoRadio.getText();
    		checkCancel(true);
    	}catch(Exception e) {
    		System.out.println(e);
    		//informs user that decryption failed
    		Alert failedDecryption=new Alert(
    				AlertType.INFORMATION,"Failed to decrypt file,"
    						+ "make sure the password is correct and the"
    						+ " correct decryption algorithm is selected",
    						ButtonType.OK);
    		failedDecryption.showAndWait();
    		
    		//resets GUI
    		reset();
    	}
    	
    }
    
    //calls rest() as rest() is also used in code in another method
    @FXML
    void reset(MouseEvent event) {
    	reset();
    }
    
    //resets GUI to default state during a decryption error
    // or if the user cancels encryption/decryption
    void reset() {
    	fileAddressLbl.setBackground(new Background(new BackgroundFill(
    			Color.rgb(199, 204, 214), CornerRadii.EMPTY, Insets.EMPTY)));
    	fileAddressLbl.setText("      Drag files here");
    	file=null;
    	decryptButton.setVisible(false);
    	fileBtn.setText("Choose File");
    	pwdInput.clear();
    	radioMenu.getSelectedToggle().setSelected(false);
    	md1Radio.setSelected(true);
    	resetBtn.setVisible(false);
    	openFileCheckbox.setSelected(false);
    }
    
    //updates GUI when a file is chosen
    public void setUpFileNodes() {
    	fileAddressLbl.setBackground(new Background(new BackgroundFill(
    			Color.GREENYELLOW, new CornerRadii(10.0), Insets.EMPTY)));
		fileAddressLbl.setText(file.getName());
		fileBtn.setText("Encrypt");
		decryptButton.setText("Decrypt");
		decryptButton.setVisible(true);
		resetBtn.setVisible(true);
    }
    
    //opens explorer window to specified address
    public void openFileLocation(String address) throws IOException {
    	if (openFileCheckbox.isSelected()) {
    		Runtime.getRuntime().exec("explorer.exe /open,"+address);
    	}
    }

    public void encryptFile(String password,File address) throws Exception {
    	try {
    		byte[] fileBytes=new byte[(int)file.length()];
        	byte[] encryptedBytes=new byte[(int) file.length()];
        	
        	//generate an encryption key based on a user provided password
            PBEKeySpec keySpec=new PBEKeySpec(password.toCharArray()); 
            SecretKeyFactory keyFactory=SecretKeyFactory.getInstance(algorithm);
        	SecretKey key=keyFactory.generateSecret(keySpec);
        	
        	//generate a 64bit salt
        	byte[] salt=new byte[8];
        	Random rand=new Random();
        	rand.nextBytes(salt);
        	
        	//create parameters for password based encryption/decryption
        	PBEParameterSpec parameterSpec;
        	byte[] iv=new byte[16];
        	if (algorithm.contains("AES")) {
            	rand.nextBytes(iv);
            	IvParameterSpec ivSpec=new IvParameterSpec(iv);
            	parameterSpec=new PBEParameterSpec(salt,2000,ivSpec);
        	}else {
        	    parameterSpec=new PBEParameterSpec(salt,2000);
        	}
            
        	//read data to be encrypted
        	FileInputStream inputFileBytes=new FileInputStream(file);
         	inputFileBytes.read(fileBytes);
         	
        	//construct cipher with specified encryption algorithm
         	//initialise cipher with encrypt mode, encryption key and parameters for encryption
        	Cipher cipher=Cipher.getInstance(algorithm);
        	cipher.init(Cipher.ENCRYPT_MODE, key,parameterSpec);
        	
        	//encrypts data with 8-bit padding (DES)
        	encryptedBytes=cipher.doFinal(fileBytes);
        	
        	//create an output file to specified location and link a stream to the file
        	File outputFile=new File(address.getAbsolutePath()+"\\"+"encrypt"+file.getName());
        	FileOutputStream outputFileBytes=new FileOutputStream(outputFile);
        	
        	//write salt to file as during decryption the salt MUST be known
        	outputFileBytes.write(salt);
        	
        	//write iv to file as during decryption the salt MUST be known when using AES encryption
        	if (algorithm.contains("AES")) {
        		outputFileBytes.write(iv);
        	}
        	
        	//write encrypted data to output file
        	outputFileBytes.write(encryptedBytes);
        	
        	//clear and close file streams
        	inputFileBytes.close();
        	outputFileBytes.flush();
        	outputFileBytes.close();
        	
        	//call method to open explorer window to output address
        	openFileLocation(address.getAbsolutePath());
    	}catch(Exception e) {
    		throw e;
    	}
    }
    
    public void decrypt(String password,File address) throws Exception{
    	
    	try {
    		byte[] fileBytesPadding=new byte[(int)file.length()];
        	byte[] encryptedBytes=new byte[(int) file.length()];
        	
        	//generate an decryption key based on a user provided password
        	PBEKeySpec keySpec=new PBEKeySpec(password.toCharArray()); 
            SecretKeyFactory keyFactory=SecretKeyFactory.getInstance(algorithm);
        	SecretKey key=keyFactory.generateSecret(keySpec);
        	
        	//create stream to read data from input file
        	FileInputStream inputFileBytes=new FileInputStream(file);
        	
        	//create a salt byte and read the salt(plain text) from the file
        	byte[] salt=new byte[8];
        	inputFileBytes.read(salt);
        	
        	//create an iv byte and read the iv(plain text) from the file if AES encryption is in use
        	byte[] iv=new byte[16];
        	if (algorithm.contains("AES")) {
            	inputFileBytes.read(iv);	
        	}
        	
        	//read in encrypted data
        	inputFileBytes.read(encryptedBytes);
        	
        	//create parameters for password based decryption
        	PBEParameterSpec parameterSpec;
        	if(algorithm.contains("AES")) {
        		IvParameterSpec ivSpec=new IvParameterSpec(iv);
                parameterSpec=new PBEParameterSpec(salt,2000,ivSpec);
        	}else {
                parameterSpec=new PBEParameterSpec(salt,2000);	
        	}
           
        	//construct cipher with specified decryption algorithm
         	//initialise cipher with decrypt mode, decryption key and parameters for decryption
        	Cipher cipher=Cipher.getInstance(algorithm);
        	cipher.init(Cipher.DECRYPT_MODE, key,parameterSpec);
     
        	//decrypts data with 8-bit padding
        	fileBytesPadding=cipher.update(encryptedBytes);
        
        	int padding=0;
        	
        	//work out the number of padded bytes added during encryption
        	//algorithms using RC4_40 or RC4_128 pads an extra 9 bits
        	//other algorithms pad up to 8-bits depending on input data (8 bit blocks)
        	if (algorithm.equals("PBEWithSHA1AndRC4_40")||algorithm.equals("PBEWithSHA1AndRC4_128")) {
        		padding+=8;	
        	}else {
        		if(verifyPadding(fileBytesPadding)) {
            		padding=fileBytesPadding[fileBytesPadding.length-1];
            	}
        	}
        	
        	//create output file at specified location and link an output stream to file
        	File outputFile=new File(address.getAbsolutePath()+"\\"+"decrypt"+file.getName());
        	FileOutputStream outputFileBytes=new FileOutputStream(outputFile);
        	
        	//write bytes to file, ignore padding bytes at the end of the array
        	outputFileBytes.write(fileBytesPadding, 0,fileBytesPadding.length-padding);
        	
        	//clear and close file streams
        	inputFileBytes.close();
        	outputFileBytes.flush();
        	outputFileBytes.close();
        	
        	//call method to open explorer window to output address
        	openFileLocation(address.getAbsolutePath());
    	}catch(Exception e) {
    		throw e;
    	}
    	
    }

    public Boolean verifyPadding(byte[] paddedArray) {
    	//check the padding number at the end of byte array
    	int padNum=paddedArray[paddedArray.length-1];
    	
    	//PKCS5Padding pads bytes with same number of bytes and duplicate data
    	//For example: padding data with 4 bytes
    	//would add 4 bytes [4,4,4,4]
    	// 7 byte padding would add [7,7,7,7,7,7,7]
    	byte[] padArrayCheck=new byte[padNum];
    	
    	//extract the padding array from file data
    	for(int x=0;x<padNum;x++) {
    		padArrayCheck[x]=paddedArray[paddedArray.length-(padNum-x)];
    	}
    	
    	//check the array is the correct size
    	//check every item in the array is the same
    	boolean allSame=false;
    	if (padNum==1 && padArrayCheck.length==1) {
    		allSame=true;
    	}else {
    		for (int x=0;x<padArrayCheck.length-1;x++) {
    			if (padArrayCheck[x]==padArrayCheck[x+1]) {
    				allSame=true;
    			}else {
    				allSame=false;
    			}
    		}
    	}
    	
    	return allSame;
    }
    
    //allows access to primaryStage in Main.java from this controller
    public void setStage(Stage stage) {
    	primaryStage=stage;
    }
    
    public Stage getStage() {
    	return primaryStage;
    }

}
