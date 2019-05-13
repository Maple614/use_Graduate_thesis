package research;

public class Accel {
	private float x_accel, y_accel, z_accel;
    private long timestamp;
    public float getx_accel() {
        return x_accel;
    }
    public void setx_accel(float x_accel) {
        this.x_accel = x_accel;
    }
    public float gety_accel() {
        return y_accel;
    }
    public void sety_accel(float y_accel) {
        this.y_accel = y_accel;
    }
    public float getz_accel() {
        return z_accel;
    }
    public void setz_accel(float z_accel) {
        this.z_accel = z_accel;
    }
    public long gettimestamp() {
        return timestamp;
    }
    public void settimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
