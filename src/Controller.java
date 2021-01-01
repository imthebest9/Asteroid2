import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class Controller {
    @FXML
    private Button gotogame;
    @FXML
    private Button dynamicallocation;

    int score;

    public void toplaygame(ActionEvent event) throws IOException {
        BorderPane root = new BorderPane();
        Scene mainScene = new Scene(root);

        Canvas canvas = new Canvas(800, 600);
        GraphicsContext context = canvas.getGraphicsContext2D();
        root.setCenter(canvas);

        // handle continuous inputs (as long as key is pressed)
        ArrayList<String> keyPressedList = new ArrayList<String>();
        // handle discrete inputs (once per key pressed)
        ArrayList <String> keyJustPressedList = new ArrayList<String>();

        mainScene.setOnKeyPressed(
                (KeyEvent event2) ->
                {
                    String keyName = event2.getCode().toString();
                    //avoid adding duplicates to the list
                    if(!keyPressedList.contains(keyName)) {
                        keyPressedList.add(keyName);
                        keyJustPressedList.add(keyName);
                    }
                }
        );

        mainScene.setOnKeyReleased(
                (KeyEvent event2) ->
                {
                    String keyName = event2.getCode().toString();
                    if(keyPressedList.contains(keyName))
                        keyPressedList.remove(keyName);
                }
        );

        Sprite background = new Sprite("image/space-01.png");
        background.position.set(400,300);

        Sprite spaceship = new Sprite("image/spaceshipv2.png");
        spaceship.position.set(100,300);

        ArrayList <Sprite> laserList = new ArrayList<Sprite>();
        ArrayList <Sprite> asteroidList = new ArrayList<Sprite>();

        int asteroidCount = 6;
        for(int i=0; i< asteroidCount; i++){
            Sprite asteroid = new Sprite("image/rock.png");
            double x = 500 * Math.random() + 300; // 300-800
            double y = 400 * Math.random() + 100; // 100-500
            asteroid.position.set(x,y);
            double angle = 360 * Math.random();
            asteroid.velocity.setLength(50);
            asteroid.velocity.setAngle(angle);
            asteroidList.add(asteroid);
        }

        score = 0;
        AnimationTimer gameloop = new AnimationTimer() {
            @Override
            public void handle(long l) {
                // process user input
                if( keyPressedList.contains("LEFT"))
                    spaceship.rotation -= 3;
                if( keyPressedList.contains("RIGHT"))
                    spaceship.rotation += 3;
                if( keyPressedList.contains("UP")){
                    spaceship.velocity.setLength(150);
                    spaceship.velocity.setAngle( spaceship.rotation);
                }
                else{
                    spaceship.velocity.setLength(0);
                }
                if( keyJustPressedList.contains("SPACE")){
                    Sprite laser = new Sprite("image/laser.png");
                    laser.position.set( spaceship.position.x, spaceship.position.y);
                    laser.velocity.setLength(400);
                    laser.velocity.setAngle( spaceship.rotation);
                    laserList.add(laser);
                }
                // after processing user input, clear justPressedList
                keyJustPressedList.clear();

                spaceship.update(1/60.0);
                for(Sprite asteroid: asteroidList)
                    asteroid.update(1/60.0);
                // update laser, destroy it if 2 secs are passed
                for (int i=0; i < laserList.size(); i++){
                    Sprite laser = laserList.get(i);
                    laser.update(1/60.0);
                    if (laser.elapsedTime > 2)
                        laserList.remove(i);
                }

                // when laser overlaps asteroid, remove both
                for(int laserNum=0; laserNum < laserList.size(); laserNum++){
                    Sprite laser = laserList.get(laserNum);
                    for(int asteroidNum=0; asteroidNum < asteroidList.size(); asteroidNum++){
                        Sprite asteroid = asteroidList.get(asteroidNum);
                        if(laser.Overlaps(asteroid)){
                            laserList.remove(laserNum);
                            asteroidList.remove(asteroidNum);
                            score += 100;
                        }
                    }
                }

                background.render(context);
                spaceship.render(context);
                for (Sprite laser: laserList)
                    laser.render(context);
                for (Sprite asteroid: asteroidList)
                    asteroid.render(context);

                // draw score on screen
                context.setFill(Color.WHITE);
                context.setStroke(Color.GREEN);
                context.setFont(new Font("Arial Black",48));
                context.setLineWidth(3);
                String text = "Score: " + score;
                int textX = 500;
                int textY = 50;
                context.fillText(text, textX, textY);
                context.strokeText(text, textX, textY);
            }
        };

        gameloop.start();

        Stage window=(Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();
    }
}
