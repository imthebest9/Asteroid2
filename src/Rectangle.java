public class Rectangle {
    // (x,y) represents top left corner of rectangle
    double x,y, width, height;

    public Rectangle(){
        this.setPosition(0,0);
        this.setSize(1,1);
    }

    public Rectangle(double x, double y, double w, double h){
        this.setSize(w,h);
        this.setPosition(x,y);
    }

    public void setPosition(double x, double y){
        this.x = x;
        this.y = y;
    }

    public void setSize(double w, double h){
        this.width = w;
        this.height = h;
    }

    public boolean Overlaps(Rectangle other){
        // Four cases where these rectangles do not overlap
        // 1: this is to the left of other
        // 2: other is to the left of this
        // 3: this is above other
        // 4: other is above this

        boolean noOverlap
                = this.x + this.width < other.x
                || other.x + other.width < this.x
                || this.y + this.height < other.y
                || other.y + other.height < this.y;
        return !noOverlap;
    }
}
