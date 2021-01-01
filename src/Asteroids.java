import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Asteroids extends Application {
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
        Parent menuroot = FXMLLoader.load(getClass().getResource("mainmenu.fxml"));
        mainStage.setScene(new Scene(menuroot, 800, 600));
        mainStage.show();
    }
}
