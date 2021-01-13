public class Boundary {
    // (x,y) represents top left corner of rectangle
    double x,y,width,height;

    public Boundary(){
        this.setPosition(0,0);
        this.setSize(1,1);
    }

    public void setPosition(double x, double y){
        this.x = x;
        this.y = y;
    }

    public void setSize(double w, double h){
        this.width = w;
        this.height = h;
    }

    public boolean overlaps(Boundary other){
        // Four cases where these rectangles do not overlap
        // 1: this is to the left of other
        // 2: other is to the left of this
        // 3: this is above other
        // 4: other is above this
        return !(x + width < other.x
                || other.x + other.width < x
                || y + height < other.y
                || other.y + other.height < y);
    }
}