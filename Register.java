import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.*;




import javafx.geometry.Insets;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class Register extends Validation {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML private Button registerButton;
    @FXML private TextField registerUsernameField;
    @FXML private TextField registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField registerConfirmPasswordField;


    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    @FXML private AnchorPane registerPagePane;
    @FXML private Label registerTitle;
    @FXML private Label emailLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label confirmPasswordLabel;
    @FXML private Label haveAnAccountLabel;
    @FXML private Button goToLogin;

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    private final File credentialsFile = new File("users.txt");

    @FXML
    public void initialize(){
        Tooltip usernameTooltip = new Tooltip("Enter your Username");
        registerUsernameField.setTooltip(usernameTooltip);

        Tooltip emailToolTip =  new Tooltip("Enter Email");
        registerEmailField.setTooltip(emailToolTip);

        Tooltip passwordTooltip = new Tooltip("Password must be at least 8 characters long and must include:\n- 1 uppercase letter\n- 1 lowercase letter\n- 1 number\n- 1 special character");
        registerPasswordField.setTooltip(passwordTooltip);

        Tooltip confirmPasswordTooltip = new Tooltip("Retype your Password to confirm");
        registerConfirmPasswordField.setTooltip(confirmPasswordTooltip);


        styleRegisterPage();
    }



    public void switchToLoginPage(ActionEvent e) throws IOException{

        root = FXMLLoader.load(getClass().getResource("resources/loginPage.fxml"));
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }



    public void register(){

        String username = registerUsernameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText().trim();
        String confirmPassword = registerConfirmPasswordField.getText().trim();


        if(username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            AlertUtil.showAlert("Error", "Please fill in all fields!");
            return;
        }

        if(!isValidEmail(email)){
            AlertUtil.showAlert("Error", "Invalid Email format! Please enter a valid Email");
            return;
        }

        if(!password.equals(confirmPassword)){
            AlertUtil.showAlert("Error", "Passwords should match");
            return;
        }

        if(!isPasswordStrong(password)){
            AlertUtil.showAlert("Weak Password", "Password must be at least 8 characters long and must include:\n- 1 uppercase letter\n- 1 lowercase letter\n- 1 number\n- 1 special character");
            return;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(credentialsFile))){

            String line;

            while((line = reader.readLine()) != null){
                String[] parts = line.split(":", 3);

                if(parts.length == 3){

                    if(parts[0].equals(username)){
                        AlertUtil.showAlert("Error","Username already exists! Try Logging in");
                        return;
                    }

                    if(parts[1].equals(email)) {
                        AlertUtil.showAlert("Error", "Email already in Use! Try Logging in or use a different email");
                        return;
                    }
                }
            }
        }
        catch(IOException e){
            //  do something like, IDK
            AlertUtil.showAlert("Critical Error", "Failed to create Credentials File or File not Found");
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(credentialsFile, true))){
            writer.write(username + ":" + email + ":" + password);
            writer.newLine();
            AlertUtil.showAlert("Success", "Registration successful!");



            //  Switch to main page
            switchToPlayer();



        }
        catch(IOException e){
            AlertUtil.showAlert("Error", "Failed to save user!");
        }

    }


    private void switchToPlayer() throws IOException {

        root = FXMLLoader.load(getClass().getResource("resources/player.fxml"));
        stage = (Stage) registerButton.getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }




    //=================================== STYLING ========================================================

    private void styleRegisterPage() {
        // Background Gradient
        BackgroundFill backgroundFill = new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3E5151")),
                new Stop(1, Color.web("#DECBA4"))
            ),
            CornerRadii.EMPTY,
            Insets.EMPTY
        );
        registerPagePane.setBackground(new Background(backgroundFill));

        // Title
        registerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        registerTitle.setTextFill(Color.WHITE);
        registerTitle.setEffect(new DropShadow(5, Color.BLACK));

        // Labels
        Label[] labels = {emailLabel, usernameLabel, passwordLabel, confirmPasswordLabel, haveAnAccountLabel};
        for (Label lbl : labels) {
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            lbl.setTextFill(Color.BLACK);
        }

        // Input Fields
        // Applying visual styles via helper method; FXML will manage dimensions.
        styleField(registerUsernameField, "Username");
        styleField(registerEmailField, "Email");
        styleField(registerPasswordField, "Password");
        styleField(registerConfirmPasswordField, "Confirm Password");

        // Register Button
        registerButton.setTextFill(Color.WHITE);
        registerButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        registerButton.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, new CornerRadii(10), Insets.EMPTY)));
        registerButton.setPadding(new Insets(10, 20, 10, 20));
        registerButton.setOnMouseEntered(e ->
            registerButton.setBackground(new Background(new BackgroundFill(Color.BLUE, new CornerRadii(10), Insets.EMPTY)))
        );
        registerButton.setOnMouseExited(e ->
            registerButton.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, new CornerRadii(10), Insets.EMPTY)))
        );

        // Go to Login Button
        goToLogin.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        goToLogin.setTextFill(Color.WHITE);
        // Using TRANSPARENT background to allow the border to be visible and for hover effect.
        goToLogin.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        goToLogin.setBorder(new Border(new BorderStroke(
            Color.WHITE,
            BorderStrokeStyle.SOLID,
            new CornerRadii(5),
            new BorderWidths(1)
        )));
        goToLogin.setOnMouseEntered(e -> {
            goToLogin.setTextFill(Color.BLACK);
            goToLogin.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY)));
        });
        goToLogin.setOnMouseExited(e -> {
            goToLogin.setTextFill(Color.WHITE);
            goToLogin.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        });
    }

    private void styleField(TextField field, String promptText) {
        field.setPromptText(promptText);
        field.setFont(Font.font("Arial", 14));
        field.setBackground(new Background(new BackgroundFill(Color.web("#ffffffdd"), new CornerRadii(10), Insets.EMPTY)));
        field.setPadding(new Insets(10));
        // Removed `field.setPrefWidth(250);` to allow FXML to control the width.
    }

}
