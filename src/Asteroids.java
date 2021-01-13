import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class Asteroids extends Application{

    public static void main (String[] args)  {
        try{
            launch(args);
        }
        catch (Exception error){
            error.printStackTrace();
        }
        finally {
            System.exit(0);
        }
    }
    public void start(Stage mainStage) throws IOException{
        mainStage.setTitle("Asteroids");
        Parent menuRoot = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        mainStage.setScene(new Scene(menuRoot));
        mainStage.initStyle(StageStyle.UNDECORATED);
        mainStage.show();
        mainStage.getScene().setOnKeyPressed(key -> {
            if(key.getCode()== KeyCode.ESCAPE)
                System.exit(0);
        });
    }
}