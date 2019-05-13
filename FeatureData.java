package research;

import java.util.ArrayList;

class FeatureData {
	String type;
	ArrayList<Float> dataX = new ArrayList<Float>(),
					dataY = new ArrayList<Float>(),
					dataZ = new ArrayList<Float>();
	int label;
	
	public int getlabel_data() {
		return label;
	}

	public void setlabel_data(int label_data) {
		this.label = label_data;
	}
	
	public float getx_data(int i) {
		return dataX.get(i);
	}

	public void setx_data(float x_data) {
		this.dataX.add(x_data);
	}

	public float gety_data(int i) {
		return dataY.get(i);
	}

	public void sety_data(float y_data) {
		this.dataY.add(y_data);
	}

	public float getz_data(int i) {
		return dataZ.get(i);
	}

	public void setz_data(float z_data) {
		this.dataZ.add(z_data);
	}
	
	public String getType_data() {
		return type;
	}
	
	public void setType_data(String type_data) {
		this.type = type_data;
	}
}
