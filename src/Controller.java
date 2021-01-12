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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
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
    static double gameTime;
    double width;
    double height;
    String scoreBoard;
    static boolean isClockMode;
    String theme;
    Stage stage;
    @FXML Spinner<Integer> asteroidNumSpinner = new Spinner<>();
    @FXML Spinner<Integer> timeSpinner = new Spinner<>();

    LinkedList<KeyCode> keyPressedList = new LinkedList<>();
    LinkedList <Sprite> laserList = new LinkedList<>();
    LinkedList <Sprite> asteroidList = new LinkedList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        asteroidNumSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(20,100,20,10));
        timeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(30,300,30,30));
        backToMainMenu();
    }

    public void clockModeSetting(ActionEvent event) throws IOException{
        root.setCenter(FXMLLoader.load(getClass().getResource("GameSetting.fxml")));
        stage = (Stage)(((Button) event.getSource()).getScene().getWindow());
        stage.setScene(mainScene);
    }

    public void selectTheme(ActionEvent event) throws IOException {
        if(((Button)event.getSource()).getText().equals("SURVIVAL MODE")){
            isClockMode=false;
        }
        else {
            isClockMode = true;
            asteroidNum = asteroidNumSpinner.getValue();
            gameTime = timeSpinner.getValue();
        }
        root.setCenter(FXMLLoader.load(getClass().getResource("ThemeMenu.fxml")));
        stage = (Stage)(((Button) event.getSource()).getScene().getWindow());
        stage.setScene(mainScene);

    }

    public void PlayGame(ActionEvent event){

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

        if(isClockMode) {
            spaceship.aliveTime = gameTime;
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
                                rewardBoundary=10*life;

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

                if(isClockMode) {
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
                context.fillText(scoreBoard, 10, 30);
                context.strokeText(scoreBoard, 10, 30);

                // if there is no life, display a vbox over the top to allow user to go back to main menu
                if(life <= 0 || (isClockMode && spaceship.aliveTime <=0)){
                    this.stop();

                    //root.setEffect(new GaussianBlur());
                    String gameover = "G A M E  O V E R🤞";
                    String esc = "Press Esc to go back to Menu";
                    context.fillText(gameover,400,225);
                    context.strokeText(gameover, 400, 225);
                    context.fillText(esc,300,275);
                    context.strokeText(esc,300,275);
                    //context.strokeText(esc,400,250);
                    //context.fillRect(500,250,300,300);
                    backToMainMenu();
                }
                else if(isClockMode && asteroidNum == 0){
                    this.stop();
                    //root.setEffect(new GaussianBlur());
                    String gameover = "   Y O U  D I D I T!   ";
                    String esc = "Press Esc to go back to Menu";
                    context.fillText(gameover,400,225);
                    context.strokeText(gameover, 400, 225);
                    context.fillText(esc,300,275);
                    context.strokeText(esc,300,275);
                    //context.strokeText(esc,400,250);
                    //context.fillRect(500,250,300,300);
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
        Sprite asteroid = new Sprite(theme+"/asteroid1.png",30);
        while(true){
            double x = Math.random()*width;
            double y = Math.random()*height;
            asteroid.position.set(x,y);
            if(Math.abs(spaceship.position.x-x)>spaceship.image.getHeight()+asteroid.image.getHeight()&&
                    Math.abs(spaceship.position.y-y)>spaceship.image.getHeight()+asteroid.image.getHeight())
                break;
            else asteroid.setImage(theme+"/asteroid2.png");
        }
        asteroid.velocity.setLength(100 + 10*life);
        asteroid.velocity.setAngle(360 * Math.random());
        asteroidList.add(asteroid);
    }

    public void Rule(ActionEvent event) throws IOException {
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
