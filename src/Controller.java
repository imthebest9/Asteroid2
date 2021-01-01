import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.LinkedList;

public class Controller {

    int score;
    int combo;
    int life=2;
    int rewardBoundary=20;
    double width;
    double height;

    public void toplaygame(ActionEvent event) {
        BorderPane root = new BorderPane();
        Scene mainScene = new Scene(root);
        Sprite background = new Sprite("image/pastel.gif");

        width = background.image.getWidth();
        height = background.image.getHeight();

        background.position.set(width/2,height/2);

        Sprite spaceship = new Sprite("image/barbie.png");
        spaceship.position.set(width/2,height/2);

        Canvas canvas = new Canvas(width, height);
        GraphicsContext context = canvas.getGraphicsContext2D();
        root.setCenter(canvas);

        // handle continuous inputs (as long as key is pressed)
        LinkedList<KeyCode> keyPressedList = new LinkedList<>();

        LinkedList <Sprite> laserList = new LinkedList<>();
        LinkedList <Sprite> asteroidList = new LinkedList<>();

        mainScene.setOnKeyPressed(key ->
                {
                    KeyCode keyName = key.getCode();
                    //avoid adding duplicates to the list
                    switch (keyName){
                        case SPACE:
                            Sprite laser = new Sprite(spaceship);
                            laserList.add(laser);
                            break;
                        case ESCAPE:
                            System.exit(0);
                        default:
                            if(!keyPressedList.contains(keyName)) {
                                keyPressedList.add(keyName);
                            }
                    }
                }
        );

        mainScene.setOnKeyReleased(key -> keyPressedList.remove(key.getCode()));

        score = 0;
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
                else{
                    spaceship.velocity.setLength(0);
                }

                if (Math.random()<0.01 && asteroidList.size()<10){
                    Sprite asteroid = new Sprite("image/lovesick.png");
                    while(true){
                        double x = Math.random()*width;
                        double y = Math.random()*height;
                        asteroid.position.set(x,y);
                        if(Math.abs(spaceship.position.x-x)>50 && Math.abs(spaceship.position.y-y)>50)
                            break;
                        else asteroid.setImage("image/donut.png");
                    }
                    asteroid.velocity.setLength(50);
                    asteroid.velocity.setAngle(360 * Math.random());
                    asteroidList.add(asteroid);
                }

                for (Sprite laser:laserList){
                    if(!laser.Overlaps(background)) {
                        laser.remove=true;
                        score-=100;
                        combo=0;
                    }
                    for(Sprite asteroid:asteroidList){
                        if(laser.Overlaps(asteroid)){
                            laser.remove=true;
                            asteroid.remove=true;
                            score += 100;
                            combo++;
                        }
                    }
                }

                for(Sprite asteroid:asteroidList){
                    if(asteroid.Overlaps(spaceship) && !asteroid.remove){
                        asteroid.remove=true;
                        life--;
                    }
                }

                asteroidList.forEach(Sprite::update);
                laserList.forEach(Sprite::update);

                asteroidList.removeIf(Sprite::getRemove);
                laserList.removeIf(Sprite::getRemove);

                spaceship.update();
                spaceship.wrap(width,height);

                context.clearRect(0,0,width,height);
                context.drawImage(background.image, 0,0);
                spaceship.render(context);

                for (Sprite laser: laserList)
                    laser.render(context);

                for (Sprite asteroid: asteroidList) {
                    asteroid.wrap(width,height);
                    asteroid.render(context);
                }

                if (combo == rewardBoundary) {
                    life++;
                    rewardBoundary+=20;
                }

                // draw score on screen
                context.setFill(Color.WHITE);
                context.setStroke(Color.GREEN);
                context.setFont(new Font("Arial Black",30));
                context.setLineWidth(1);
                String text = "Score: " + score + " Combo: " + combo + " Life: " + life;
                int textX = 10;
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
