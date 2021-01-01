public class Vector {
    public double x, y;

    public Vector(){
        set(0,0);
    }

    public void set(double x, double y){
        this.x = x;
        this.y = y;
    }

    public void add(double dx, double dy){
        x += dx;
        y += dy;
    }

    public void multiply(double m){
        x *= m;
        y *= m;
    }

    public double getLength(){
        return Math.sqrt(x * x + y * y);
    }

    public void setLength(double L){
        double currentLength = getLength();
        //if current length is zero, then current angle is undefined
        //assume current angle is zero (pointing to the right)
        if(currentLength ==  0){
            set(L,0);
        }
        else{
            //scale vector down to length one
            multiply( L/currentLength);
        }
    }

    public void setAngle(double angleDegrees){
        double L = this.getLength();
        double angleRadians = Math.toRadians(angleDegrees);
        this.x = L * Math.sin(angleRadians);
        this.y = -L * Math.cos(angleRadians);
    }
}
