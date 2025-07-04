import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException{

        Parent root = FXMLLoader.load(getClass().getResource("resources/loginPage.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Music Player");
        stage.setScene(scene);
        //stage.setResizable(false);
        stage.show();
    }



    public static void main(String[] args){
        launch(args);
    }
}
