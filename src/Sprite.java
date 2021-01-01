import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Sprite {
    public Vector position;
    public Vector velocity;
    public double rotation; //degrees
    public Rectangle boundary;
    public Image image;
    public boolean remove;

    public Sprite(String imageFileName){
        position = new Vector();
        velocity = new Vector();
        boundary = new Rectangle();
        rotation = 0;
        remove=false;
        setImage(imageFileName);
    }

    public Sprite(Sprite spaceship){
        position = new Vector();
        velocity = new Vector();
        boundary = new Rectangle();
        image = new Image("image/laser.png");
        rotation = spaceship.rotation;
        remove = false;
        position.set(spaceship.position.x,spaceship.position.y);
        velocity.setLength(500);
        velocity.setAngle(rotation);
        boundary.setSize(image.getWidth(),image.getWidth());
    }

    public void setImage(String imageFileName){
        image = new Image(imageFileName);
        boundary.setSize(image.getWidth(),image.getHeight());
    }

    public Rectangle getBoundary(){
        this.boundary.setPosition(
                position.x - boundary.width/2,
                position.y - boundary.height/2);
        return this.boundary;
    }

    public boolean Overlaps(Sprite other){
        return getBoundary().Overlaps(other.getBoundary());
    }

    public void wrap (double screenWidth, double screenHeight){

        double length = image.getHeight()/2;

        if(position.x + length < 0)
            position.x = screenWidth + length;
        if(position.x > screenWidth + length)
            position.x = -length / 2;
        if(position.y + length < 0)
            position.y = screenHeight + length;
        if(position.y > screenHeight + length)
            position.y = -length;
    }

    public void update(){
        // update position according to velocity
        double deltaTime = 1/60.0;
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
    }

    public void render(GraphicsContext context){
        context.save();
        context.translate(position.x,position.y);
        context.rotate(rotation);
        //context.translate( -image.getWidth()/2, -image.getHeight()/2);
        context.drawImage(image, -image.getWidth()/2, -image.getHeight()/2);
        context.restore();
    }

    public boolean getRemove(){return remove;}
}