import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;




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







public class Login extends Validation {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML private Button loginButton;
    @FXML private Label label;
    @FXML private TextField loginUsernameField;
    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    @FXML private AnchorPane loginPagePane;
    @FXML private Label loginTitle;
    @FXML private Label dontHaveAccountLabel;
    @FXML private Button goToRegister;
    @FXML private Label emailLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-


    private final File credentialsFile = new File("users.txt");

    @FXML
    public void initialize(){

        Tooltip emailToolTip =  new Tooltip("Enter Email");
        loginEmailField.setTooltip(emailToolTip);

        Tooltip usernameTooltip = new Tooltip("Enter your Username");
        loginUsernameField.setTooltip(usernameTooltip);

        Tooltip passwordTooltip = new Tooltip("Enter your Password");
        loginPasswordField.setTooltip(passwordTooltip);



        styleLoginPage();


    }



    public void switchToRegisterPage(ActionEvent e) throws IOException {

        root = FXMLLoader.load(getClass().getResource("resources/registerPage.fxml"));
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(root);



        stage.setScene(scene);
        stage.show();
    }


    public void login(){
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText().trim();
        String email = loginEmailField.getText().trim();

        if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
            AlertUtil.showAlert("Error", "Please fill in all fields!");
            return;
        }

        if(!isValidEmail(email)){
            AlertUtil.showAlert("Error", "Invalid Email format! Please enter a valid Email");
            return;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(credentialsFile))){
            String line;
            boolean userFound = false;

            while((line = reader.readLine()) != null){
                String[] parts = line.split(":", 3);

                if(parts.length == 3){
                    String fileUsername = parts[0];
                    String fileEmail = parts[1];
                    String filePassword = parts[2];

                    if(fileUsername.equalsIgnoreCase(username)){
                        userFound = true;

                        if(!fileEmail.equals(email)){
                            AlertUtil.showAlert("Error", "Incorrect Email");
                        }
                        else if(!filePassword.equals(password)){
                            AlertUtil.showAlert("Error","Incorrect Password");

                        }
                        else{
                            AlertUtil.showAlert("Success", "Login Successful! Welcome " + username);

                            //  Switch to main player;
                            switchToPlayer();

                        }
                        break;
                    }

                }
            }

            if(!userFound){
                AlertUtil.showAlert("Error", "User does not Exist!");
            }

        }
        catch(IOException e){
            AlertUtil.showAlert("Error", "Failed to fetch Users!");
        }


    }


    private void switchToPlayer() throws IOException {

        root = FXMLLoader.load(getClass().getResource("resources/player.fxml"));
        stage = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(root);



        stage.setScene(scene);
        stage.show();
    }

    

    //======================================== Styling ============================================================

    private void styleLoginPage() {
        // Background Gradient
        BackgroundFill backgroundFill = new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3E5151")),
                new Stop(1, Color.web("#DECBA4"))
            ),
            CornerRadii.EMPTY,
            Insets.EMPTY
        );
        loginPagePane.setBackground(new Background(backgroundFill));

        // Title
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        loginTitle.setTextFill(Color.WHITE);
        loginTitle.setEffect(new DropShadow(5, Color.BLACK));

        // Labels
        Label[] labels = {emailLabel, usernameLabel, passwordLabel, dontHaveAccountLabel};
        for (Label lbl : labels) {
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            lbl.setTextFill(Color.BLACK);
        }

        // Register Button
        goToRegister.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        goToRegister.setTextFill(Color.WHITE);
        // Using TRANSPARENT background to allow the border to be visible and for hover effect.
        goToRegister.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        goToRegister.setBorder(new Border(new BorderStroke(
            Color.WHITE,
            BorderStrokeStyle.SOLID,
            new CornerRadii(5),
            new BorderWidths(1)
        )));
        goToRegister.setOnMouseEntered(e -> {
            goToRegister.setTextFill(Color.BLACK);
            goToRegister.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY)));
        });
        goToRegister.setOnMouseExited(e -> {
            goToRegister.setTextFill(Color.WHITE);
            goToRegister.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        });

        // Input Fields
        // Applying visual styles via helper method; FXML will manage dimensions.
        styleField(loginUsernameField, "Username");
        styleField(loginEmailField, "Email");
        styleField(loginPasswordField, "Password");

        // Login Button
        loginButton.setTextFill(Color.WHITE);
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        loginButton.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, new CornerRadii(10), Insets.EMPTY)));
        loginButton.setPadding(new Insets(10, 20, 10, 20));
        loginButton.setOnMouseEntered(e -> loginButton.setBackground(new Background(new BackgroundFill(Color.BLUE, new CornerRadii(10), Insets.EMPTY))));
        loginButton.setOnMouseExited(e -> loginButton.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, new CornerRadii(10), Insets.EMPTY))));
    }

    /**
     * Applies visual styling to a TextField, without setting its preferred width.
     * @param field The TextField to style.
     * @param promptText The prompt text to display in the field.
     */
    private void styleField(TextField field, String promptText) {
        field.setPromptText(promptText);
        field.setFont(Font.font("Arial", 14));
        field.setBackground(new Background(new BackgroundFill(Color.web("#ffffffdd"), new CornerRadii(10), Insets.EMPTY)));
        field.setPadding(new Insets(10));
        // Removed `field.setPrefWidth(250);` to allow FXML to control the width.
    }

}
