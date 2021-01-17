import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
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
    static int asteroidNum;
    static double aliveTime;
    static boolean isTimeMode;
    static AudioClip music = new AudioClip("file:src/music.mp3");
    AudioClip shoot;
    AudioClip score;
    AudioClip explode;
    double width;
    double height;
    String scoreBoard;
    String theme;
    Stage stage;
    @FXML Spinner<Integer> asteroidNumSpinner = new Spinner<>();
    @FXML Spinner<Integer> timeSpinner = new Spinner<>();

    LinkedList<KeyCode> keyPressedList = new LinkedList<>();
    LinkedList <Sprite> laserList = new LinkedList<>();
    LinkedList <Sprite> asteroidList = new LinkedList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(music.getSource()!="file:src/music.mp3")
            music.stop();
        if(!music.isPlaying()) {
            music = new AudioClip("file:src/music.mp3");
            music.setCycleCount(AudioClip.INDEFINITE);
            music.play();
        }
        asteroidNumSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(20,100,10,10));
        timeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(30,180,30,15));
        backToMainMenu();
    }

    public void gameSetting(ActionEvent event) throws IOException{
        root.setCenter(FXMLLoader.load(getClass().getResource("GameSetting.fxml")));
        stage = (Stage)(((Button) event.getSource()).getScene().getWindow());
        stage.setScene(mainScene);
    }

    public void selectTheme(ActionEvent event) throws IOException {
        if(((Button)event.getSource()).getText().equals("SURVIVAL MODE")){
            isTimeMode = false;
            asteroidNum = 0;
            aliveTime = 0;
        }
        else {
            isTimeMode = true;
            asteroidNum = asteroidNumSpinner.getValue();
            aliveTime = timeSpinner.getValue();
        }
        root.setCenter(FXMLLoader.load(getClass().getResource("ThemeMenu.fxml")));
        stage = (Stage)(((Button) event.getSource()).getScene().getWindow());
        stage.setScene(mainScene);
    }

    public void playGame(ActionEvent event){
        if(((Button)event.getSource()).getText().equals("Void Galaxy"))
            theme = "galaxy";
        else
            theme = "aesthetic";

        music.stop();
        music = new AudioClip("file:src/"+theme+"/music.mp3");
        music.setCycleCount(AudioClip.INDEFINITE);
        music.play();

        shoot = new AudioClip("file:src/"+theme+"/laser.mp3");
        score = new AudioClip("file:src/"+theme+"/score.mp3");
        explode = new AudioClip("file:src/"+theme+"/explode.mp3");

        Sprite background = new Sprite(theme +"/background.gif");
        Sprite reward = new Sprite(theme +"/reward.gif");
        Sprite elapsed = new Sprite(theme +"/elapsed.gif");
        Sprite collide = new Sprite(theme +"/collide.gif");

        width = background.image.getWidth();
        height = background.image.getHeight();

        background.position.set(width/2,height/2);

        Sprite spaceship = new Sprite(theme +"/spaceship.png");
        spaceship.generate(width/2,height/2, aliveTime);

        Canvas canvas = new Canvas(width, height);
        root.setCenter(canvas);
        GraphicsContext context = canvas.getGraphicsContext2D();

        if(theme.equals("galaxy")){
            context.setFont(new Font("Arial Black", 30));
            context.setStroke(Color.web("#4f198e"));
        }
        else{
            context.setFont(new Font("Bodoni MT Black", 30));
            context.setStroke(Color.web("#ff618b"));
        }
        context.setFill(Color.WHITE);
        context.setLineWidth(1);

        // handle continuous inputs (as long as key is pressed)

        mainScene.setOnKeyPressed(key ->
            {
                KeyCode keyName = key.getCode();
                //avoid adding duplicates to the list
                switch (keyName){
                    case SPACE:
                        if(laserList.size()<5) {
                            Sprite laser = new Sprite(spaceship, theme);
                            laserList.add(laser);
                            shoot.play();
                        }
                        break;
                    case ESCAPE:
                        backToMainMenu();
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

        AnimationTimer gameLoop = new AnimationTimer() {
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

                if (asteroidList.isEmpty() || asteroidList.size()<10 && Math.random()<0.01)
                    addAsteroid(theme,spaceship);

                for(Sprite asteroid:asteroidList){
                    //if the spaceship collides an asteroid
                    if(asteroid.overlaps(spaceship)){
                        explode.play();
                        collide.image = new Image(theme +"/collide.gif");
                        collide.generate(spaceship.position.x, spaceship.position.y, 2);
                        rewardCombo=0;
                        rewardBoundary=5*life--;
                        if(isTimeMode)
                            asteroidNum--;
                        else{
                            asteroidNum-=5;
                            if(asteroidNum<0) asteroidNum=0; //avoid negative elapsed
                        }
                    }
                }

                for (Sprite laser:laserList){
                    //if a laser is still in the scene
                    if(background.overlaps(laser)) {
                        for (Sprite asteroid : asteroidList) {
                            //if a laser hits an asteroid
                            if (!asteroid.remove && asteroid.overlaps(laser)) {
                                score.play();
                                elapsed.image = new Image(theme +"/elapsed.gif");
                                elapsed.generate(asteroid.position.x,asteroid.position.y,1);
                                laser.remove = true;//remove to make combo;
                                combo++;
                                rewardCombo++;
                                if(isTimeMode) {
                                    asteroidNum -= 1;
                                }
                                else{
                                    asteroidNum += 1 + combo/10;
                                }
                            }
                        }
                    }
                    //if a laser does not hit an asteroid
                    else {
                        laser.remove=true;
                        combo=0;
                        if(!isTimeMode){
                            //if miss an asteroid while elapsed is 0, lose 1 life
                            if(asteroidNum==0) {
                                rewardCombo=0;
                            }
                            else asteroidNum--;
                        }
                    }
                }

                //generate a reward when the rewardCombo reach the target
                if (rewardCombo==rewardBoundary) {
                    reward.generate(Math.random()*(width-100)+100,Math.random()*(height-100)+100, 5);
                    rewardCombo=0;
                }

                //if the reward is available and the spaceship get the reward
                if(!reward.remove && reward.overlaps(spaceship)) {
                    score.play();
                    rewardBoundary=10*++life; //rewardBoundary increases as life increases
                    rewardCombo=0;
                    elapsed.image = new Image(theme +"/elapsed.gif");
                    elapsed.generate(reward.position.x, reward.position.y, 1);
                    if(isTimeMode) spaceship.aliveTime+=5;
                    else asteroidNum += 5;
                }
                else reward.update();

                elapsed.update();
                collide.update();
                collide.position.set(spaceship.position.x, spaceship.position.y);
                collide.rotation = spaceship.rotation;

                asteroidList.forEach(Sprite::update);
                laserList.forEach(Sprite::update);

                asteroidList.removeIf(Sprite::isRemoved);
                laserList.removeIf(Sprite::isRemoved);

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
                spaceship.render(context);

                if(!reward.isRemoved())
                    reward.render(context);
                if(!elapsed.isRemoved())
                    elapsed.render(context);
                if(!collide.isRemoved())
                    collide.render(context);

                if(isTimeMode) {
                    if (spaceship.aliveTime < 0)
                        spaceship.aliveTime = 0;
                    scoreBoard = "Target: " + asteroidNum
                            + " Combo: " + combo
                            + " Life: " + life
                            + String.format(" Time: %.1f", spaceship.aliveTime);
                }
                else
                    scoreBoard="Score: " + asteroidNum*10
                            +" Combo: " + combo
                            +" Life: "
                            + life+String.format(" Time: %.1f",-spaceship.aliveTime);
                context.fillText(scoreBoard, 20, 30);
                context.strokeText(scoreBoard, 20, 30);

                // if there is no life, display a vbox over the top to allow user to go back to main menu
                if(life <= 0 || (isTimeMode && spaceship.aliveTime ==0)){
                    music.stop();
                    new AudioClip("file:src/failed.mp3").play();
                    this.stop();
                    //root.setEffect(new GaussianBlur());
                    String gameOver = "💫 G A M E  O V E R 💫";
                    String esc = "Press Esc to go back to Menu";
                    context.fillText(gameOver,350,225);
                    context.strokeText(gameOver, 350, 225);
                    context.fillText(esc,300,275);
                    context.strokeText(esc,300,275);
                    backToMainMenu();
                }
                else if(isTimeMode && asteroidNum==0){
                    music.stop();
                    new AudioClip("file:src/victory.mp3").play();
                    this.stop();
                    //root.setEffect(new GaussianBlur());
                    String gameOver = "🌟 YOU DID IT! 🌟";
                    String esc = "Press Esc to go back to Menu";
                    context.fillText(gameOver,370,225);
                    context.strokeText(gameOver, 370, 225);
                    context.fillText(esc,300,275);
                    context.strokeText(esc,300,275);
                    backToMainMenu();
                }
            }
        };
        gameLoop.start();
        Stage window=(Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();
    }

    public void addAsteroid(String theme, Sprite spaceship){
        Sprite asteroid = new Sprite(theme+"/asteroid1.png");
        while(true){
            double x = Math.random()*width;
            double y = Math.random()*height;
            asteroid.generate(x,y,30);
            if(Math.abs(spaceship.position.x-x)>spaceship.image.getHeight()+asteroid.image.getHeight()&&
                    Math.abs(spaceship.position.y-y)>spaceship.image.getHeight()+asteroid.image.getHeight())
                break;
            else asteroid.setImage(theme+"/asteroid2.png");
        }
        asteroid.velocity.setLength(90 + 10*life);
        asteroid.velocity.setAngle(360 * Math.random());
        asteroidList.add(asteroid);
    }

    public void rules(ActionEvent event) throws IOException {
        root.setCenter(FXMLLoader.load(getClass().getResource("Rules.fxml")));
        stage = (Stage)(((Button) event.getSource()).getScene().getWindow());
        stage.setScene(mainScene);
    }

    public void backToMainMenu(){
        mainScene.setOnKeyPressed(key->{
            if(key.getCode()==KeyCode.ESCAPE){
                try {
                    root.setCenter(FXMLLoader.load(getClass().getResource("MainMenu.fxml")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mainScene.setOnKeyPressed(esc -> {
                    if(esc.getCode()==KeyCode.ESCAPE)
                        exitGame();
                });
            }
        });
    }
    public void exitGame(){
        System.exit(0);
    }
}
