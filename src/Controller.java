import com.sun.glass.ui.Application;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    BorderPane root = new BorderPane();
    Scene mainScene = new Scene(root);
    int combo;
    int life=2;
    int rewardCombo=0;
    int rewardBoundary=10;
    int asteroidNum;
    double width;
    double height;
    String scoreBoard;
    static boolean isClockMode;
    String theme;
    Font font;
    Stage stage;
    @FXML Spinner<Integer> asteroidNumSpinner = new Spinner<>(10,100,10);
    @FXML Spinner<Integer> timeSpinner = new Spinner<>(30,3000,30);

    LinkedList<KeyCode> keyPressedList = new LinkedList<>();
    LinkedList <Sprite> laserList = new LinkedList<>();
    LinkedList <Sprite> asteroidList = new LinkedList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        asteroidNumSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(20,100,20,10));
        timeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(30,3000,30,30));
        mainScene.setOnKeyPressed(key->{
            if(key.getCode()==KeyCode.ESCAPE){
                try {
                    root.setCenter(FXMLLoader.load(getClass().getResource("mainmenu.fxml")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void clockModeSetting(ActionEvent event) throws IOException{
        root.setCenter(FXMLLoader.load(getClass().getResource("GameSetting.fxml")));
        stage = (Stage)(((Button) event.getSource()).getScene().getWindow());
        stage.setScene(mainScene);
    }

    public void selectTheme(ActionEvent event) throws IOException {
        if(((Button)event.getSource()).getText().equals("PLAY")){
            isClockMode=false;
        }
        else {
            isClockMode = true;
        }
        root.setCenter(FXMLLoader.load(getClass().getResource("ThemeMenu.fxml")));
        stage = (Stage)(((Button) event.getSource()).getScene().getWindow());
        stage.setScene(mainScene);
    }

    public void toplaygame(ActionEvent event) throws IOException{

        if(((Button)event.getSource()).getText().equals("Void Galaxy"))
            theme = "galaxy";
        else
            theme = "aesthetic";

        Sprite background = new Sprite(theme +"/background.gif", 0);

        width = background.image.getWidth();
        height = background.image.getHeight();

        background.position.set(width/2,height/2);

        Sprite spaceship = new Sprite(theme +"/spaceship.png", 0);
        spaceship.position.set(width/2,height/2);

        Sprite reward = new Sprite(theme +"/reward.gif", 10);
        reward.remove =true;

        Canvas canvas = new Canvas(width, height);
        root.setCenter(canvas);
        GraphicsContext context = canvas.getGraphicsContext2D();

        if(theme=="galaxy"){
            font = new Font("Arial Black", 30);
            context.setFont(font);
            context.setFill(Color.WHITE);
            context.setStroke(Color.web("#4f198e"));
            context.setLineWidth(1);
        }
        else{
            font = new Font("Bodoni MT Black", 30);
            context.setFont(font);
            context.setFill(Color.WHITE);
            context.setStroke(Color.web("#ff618b"));
            context.setLineWidth(1);
        }

        if(isClockMode) {
            asteroidNum = asteroidNumSpinner.getValue();
            spaceship.aliveTime = timeSpinner.getValue();
        }
        // handle continuous inputs (as long as key is pressed)

        mainScene.setOnKeyPressed(key ->
            {
                KeyCode keyName = key.getCode();
                //avoid adding duplicates to the list
                switch (keyName){
                    case SPACE:
                        Sprite laser = new Sprite(spaceship, theme);
                        laserList.add(laser);
                        break;
                    case ESCAPE:
                        try {
                            root.setCenter(FXMLLoader.load(getClass().getResource("mainmenu.fxml")));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    default:
                        if(!keyPressedList.contains(keyName)) {
                            keyPressedList.add(keyName);
                        }
                }
            }
        );
        mainScene.setOnKeyReleased(key -> keyPressedList.remove(key.getCode()));

        for (int i = 0; i < 5; i++)
            addAsteroid(theme, spaceship);


        AnimationTimer gameloop = new AnimationTimer() {
            @Override
            public void handle(long l) {
                // process user input
                if( keyPressedList.contains(KeyCode.LEFT))
                    spaceship.rotation -= 3;
                if( keyPressedList.contains(KeyCode.RIGHT))
                    spaceship.rotation += 3;
                if( keyPressedList.contains(KeyCode.UP)){
                    spaceship.velocity.setLength(150);
                    spaceship.velocity.setAngle(spaceship.rotation);
                }
                else spaceship.velocity.setLength(0);

                if (asteroidList.isEmpty() || asteroidList.size()<20 && Math.random()<0.01)
                    addAsteroid(theme,spaceship);

                for(Sprite asteroid:asteroidList){
                    //if the spaceship collides an asteroid
                    if(asteroid.Overlaps(spaceship)){
                        life--;
                        rewardCombo=0;
                        rewardBoundary=10*life;
                        if(isClockMode) spaceship.aliveTime-=5;
                        else{
                            asteroidNum-=5;
                            if(asteroidNum<0) asteroidNum=0; //avoid negative score
                        }
                    }
                }

                for (Sprite laser:laserList){
                    //if a laser is still in the scene
                    if(background.Overlaps(laser)) {
                        for (Sprite asteroid : asteroidList) {
                            //if a laser hits an asteroid
                            if (!asteroid.remove && asteroid.Overlaps(laser)) {
                                laser.remove = true;//remove to make combo;
                                combo++;
                                rewardCombo++;
                                if(isClockMode) asteroidNum-=1;
                                else{
                                    asteroidNum ++;
                                    asteroidNum += combo/10;
                                }
                            }
                        }
                    }
                    //if a laser does not hit an asteroid
                    else {
                        laser.remove=true;
                        combo=0;
                        if(isClockMode) spaceship.aliveTime-=2;
                        else {
                            //if miss an asteroid while score is 0, lose 1 life
                            if(asteroidNum==0) {
                                rewardCombo=0;
                                rewardBoundary=10*--life;
                            }
                            else asteroidNum--;
                        }
                    }
                }

                //generate a reward when the rewardCombo reach the target
                if (rewardCombo==rewardBoundary) {
                    reward.remove=false;
                    reward.position.set(Math.random()*(width-100)+100,Math.random()*(height-100)+100);
                    reward.aliveTime=5;
                    rewardCombo=0;
                }

                //if the reward is available and the spaceship get the reward
                if(!reward.remove && reward.Overlaps(spaceship)) {
                    rewardBoundary=10*life++; //rewardBoundary increases as life increases
                    if(isClockMode) spaceship.aliveTime+=10;
                    else asteroidNum += 5;
                }
                else reward.update();

                asteroidList.forEach(Sprite::update);
                laserList.forEach(Sprite::update);

                asteroidList.removeIf(Sprite::Removed);
                laserList.removeIf(Sprite::Removed);

                spaceship.update();
                spaceship.wrap(width,height);

                context.clearRect(0,0,width,height);
                context.drawImage(background.image, 0,0);

                for (Sprite laser: laserList)
                    laser.render(context);
                for (Sprite asteroid: asteroidList) {
                    asteroid.wrap(width,height);
                    asteroid.render(context);
                }

                if(!reward.Removed())
                    reward.render(context);

                spaceship.render(context);

                if(isClockMode)
                    scoreBoard="Target: "+ asteroidNum
                            + " Combo: "+ combo
                            + " Life: "+ life
                            + String.format(" Time: %.1f",spaceship.aliveTime);
                else
                    scoreBoard="Score: " + asteroidNum*10
                            +" Combo: " + combo
                            +" Life: "
                            + life+String.format(" Time: %.1f",-spaceship.aliveTime);
                context.fillText(scoreBoard, 10, 30);
                context.strokeText(scoreBoard, 10, 30);

                /*if(life <=0){
                    try {
                        root.setCenter(FXMLLoader.load(getClass().getResource("gameover.fxml")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/
                // if there is no life, display a vbox over the top to allow user to go back to main menu
                //life=0;
                if(life <= 0){
                    this.stop();
                    //root.setEffect(new GaussianBlur());

                    VBox gameoverRoot = new VBox(250);
                    Label label = new Label("Game over");
                    label.setFont(new Font("Times New Roman", 50));
                    gameoverRoot.getChildren().add(label);
                    gameoverRoot.setStyle("-fx-background-color: rgba(255,255,255,0.8)");
                    gameoverRoot.setAlignment(Pos.CENTER);
                    gameoverRoot.setPadding(new Insets(20));

                    Button gotomainmenu = new Button("GO TO MAIN MENU");
                    gameoverRoot.getChildren().add(gotomainmenu);

                    Stage popupStage = new Stage(StageStyle.TRANSPARENT);
                    Stage window=(Stage)((Node)event.getSource()).getScene().getWindow();
                    popupStage.initOwner(window);
                    popupStage.initModality(Modality.APPLICATION_MODAL);
                    popupStage.setScene(new Scene(gameoverRoot, Color.TRANSPARENT));

                    popupStage.show();
                }
            }
        };

        gameloop.start();




        Stage window=(Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();
    }

    public void addAsteroid(String theme, Sprite spaceship){
        Sprite asteroid = new Sprite(theme+"/rock1.png",30);
        while(true){
            double x = Math.random()*width;
            double y = Math.random()*height;
            asteroid.position.set(x,y);
            if(Math.abs(spaceship.position.x-x)>spaceship.image.getHeight()+asteroid.image.getHeight()&&
                    Math.abs(spaceship.position.y-y)>spaceship.image.getHeight()+asteroid.image.getHeight())
                break;
            else asteroid.setImage(theme+"/rock2.png");
        }
        asteroid.velocity.setLength(100);
        asteroid.velocity.setAngle(360 * Math.random());
        asteroidList.add(asteroid);
    }

    public void exitGame(){
        System.exit(0);
    }
}
