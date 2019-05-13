package research;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Display_master_for_FeatureValue extends Application {
	private ArrayList<Accel> data1, data2, raw_data, lowpass_data, average_data, variance_data,
			linearInterpolation_data, linearinterpolation_standard, linearinterpolation_temp, linear_before,
			linear_after;
	private ArrayList<XYChart.Series> pastXYchart = new ArrayList<XYChart.Series>(); // グラフ初期化用
	private float lowpass_value = 0.9f;
	private Label lb1, lb2, lb3, lb4, lb5;
	private Button bt;
	private boolean existdata1 = false, existdata2 = false;
	private TextField ntf, ntf2;
	private MenuBar mb;
	private Menu[] mn = new Menu[3];
	private MenuItem[] mi_0 = new MenuItem[2];
	private MenuItem[] mi_1 = new MenuItem[6];
	private MenuItem[] mi_2 = new MenuItem[3];
	private CheckBox[] ch = new CheckBox[3];
	private TextArea ta;

	private LineChart<NumberAxis, NumberAxis> lineChart;
	private NumberAxis xAxis = new NumberAxis();
	private NumberAxis yAxis = new NumberAxis();
	private XYChart.Series x_series, y_series, z_series;
	private String filename = "";

	//線形補間（中野）で用いる変数
	boolean linear_init = true;
	long linear_startTime;
	long linear_deltaTime;
	long linear_TimeSize;
	int linear_new_datasize;
	int linear_forward=0;
	int linear_back=0;


	long Time_duration=1000;//ミリ秒
	long OverLap=Time_duration/2;

	// 画面の大きさ
	private int width = 1200, height = 700;

	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage stage) throws Exception {

		lb1 = new Label("ローパスフィルタ係数");
		lb2 = new Label("描画する軸");
		lb3 = new Label("Data-1");
		lb3.setTextFill(Color.RED);
		lb4 = new Label("Data-2");
		lb4.setTextFill(Color.RED);
		lb5 = new Label("(カット時)始点、終点");
		ntf = new TextField();
		ntf.setMaxWidth(45);
		ntf.setText(String.valueOf(lowpass_value));
		ntf2 = new TextField();
		ntf2.setMaxWidth(75);
		ntf2.setText("0,100");
		bt = new Button("再描画");
		bt.setOnAction(new UpdateEventHandler());
		for (int i = 0; i < 3; i++) {
			ch[i] = new CheckBox();
			ch[i].setSelected(true);
		}
		ch[0].setText("X軸");
		ch[1].setText("Y軸");
		ch[2].setText("Z軸");

		ta = new TextArea();
		mb = new MenuBar();
		mn[0] = new Menu("Read＆Plot");
		mn[1] = new Menu("CreateCSVFile");
		mn[2] = new Menu("Initialize");

		mi_0[0] = new MenuItem("Data1");
		mi_0[1] = new MenuItem("Data2");
		for (int i = 0; i < 2; i++) {
			mi_0[i].setOnAction(new ReadEventHandler());
		}

		mi_1[0] = new MenuItem("LowPass");
		mi_1[1] = new MenuItem("Average");
		mi_1[2] = new MenuItem("Variance");
		mi_1[3] = new MenuItem("Linear Interpolation(木下)エラー");
		mi_1[4] = new MenuItem("Linear Interpolation(中野)");
		mi_1[5] = new MenuItem("cut csv");

		for (int i = 0; i < 6; i++) {
			mi_1[i].setOnAction(new CreateEventHandler());
		}

		mi_2[0] = new MenuItem("Graph");
		mi_2[1] = new MenuItem("Data-1");
		mi_2[2] = new MenuItem("Data-2");
		for (int i = 0; i < 3; i++) {
			mi_2[i].setOnAction(new Initialize());
		}

		mn[0].getItems().addAll(mi_0[0], mi_0[1]);
		mn[1].getItems().addAll(mi_1[0], mi_1[1], mi_1[2], mi_1[3], mi_1[4], mi_1[5]);
		mn[2].getItems().addAll(mi_2[0], mi_2[1], mi_2[2]);

		mb.getMenus().addAll(mn[0], mn[1], mn[2]);
		BorderPane bp = new BorderPane();
		BorderPane bp_sub = new BorderPane();

		HBox hb = new HBox();
		hb.setAlignment(Pos.CENTER_RIGHT);
		hb.setSpacing(10);

		hb.getChildren().add(lb3);
		hb.getChildren().add(lb4);
		hb.getChildren().add(bt);
		hb.getChildren().add(lb2);
		hb.getChildren().add(ch[0]);
		hb.getChildren().add(ch[1]);
		hb.getChildren().add(ch[2]);

		hb.getChildren().add(lb5);
		hb.getChildren().add(ntf2);

		hb.getChildren().add(lb1);
		hb.getChildren().add(ntf);

		// defining the axes
		NumberAxis xAxis = new NumberAxis();
		//NumberAxis xAxis = new NumberAxis(950,1050,10);
		NumberAxis yAxis = new NumberAxis();
		// NumberAxis yAxis = new NumberAxis(0,10,1);
		xAxis.setLabel("個数");
		yAxis.setLabel("加速度");

		// creating the chart
		lineChart = new LineChart(xAxis, yAxis);
		// not symbol
		lineChart.setCreateSymbols(false);

		bp_sub.setTop(hb);
		bp_sub.setCenter(ta);
		bp_sub.setBottom(lineChart);

		bp.setTop(mb);
		bp.setCenter(bp_sub);

		Scene sc = new Scene(bp, width, height);
		sc.getStylesheets().add(getClass().getResource("LineChart.css").toExternalForm());

		stage.setScene(sc);
		stage.setTitle("Display");
		stage.show();
	}

	class ReadEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File("./Myfile"));
			FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
			fc.getExtensionFilters().add(extensionFilter);
			if (e.getSource() == mi_0[0]) { // データ１読み込み
				try {
					ta.clear();
					// データ1を保持するArrayListのインスタンスを生成
					data1 = new ArrayList<Accel>();
					File flr = fc.showOpenDialog(new Stage());
					filename = flr.getName();
					BufferedReader br = new BufferedReader(new FileReader(flr));
					String str = null;
					// 1行目はシカト（文字列だろうから）
					br.readLine();
					while ((str = br.readLine()) != null) {
						ta.appendText(str + "\n");
						// 1行分の加速度データの登録
						Accel accel = new Accel();
						String[] accel_data = str.split(",");
						accel.setx_accel(Float.parseFloat(accel_data[1]));
						accel.sety_accel(Float.parseFloat(accel_data[2]));
						accel.setz_accel(Float.parseFloat(accel_data[3]));
						accel.settimestamp(Long.parseLong(accel_data[0].replace(" ", "")));
						data1.add(accel);
					}
					draw(data1);
					br.close();
					existdata1 = true;
					lb3.setText(filename);
					lb3.setTextFill(Color.BLUE);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (e.getSource() == mi_0[1]) { // データ2読み込み
				try {
					ta.clear();
					// データ2を保持するArrayListのインスタンスを生成
					data2 = new ArrayList<Accel>();
					File flr = fc.showOpenDialog(new Stage());
					filename = flr.getName();
					BufferedReader br = new BufferedReader(new FileReader(flr));
					String str = null;
					// 1行目はシカト
					br.readLine();
					// System.out.println(br.readLine());
					while ((str = br.readLine()) != null) {
						ta.appendText(str + "\n");
						// 1行分の加速度データの登録
						Accel accel = new Accel();
						String[] accel_data = str.split(",");
						// System.out.println(accel_data[1]);
						accel.setx_accel(Float.parseFloat(accel_data[1]));
						accel.sety_accel(Float.parseFloat(accel_data[2]));
						accel.setz_accel(Float.parseFloat(accel_data[3]));
						accel.settimestamp(Long.parseLong(accel_data[0].replace(" ", "")));
						// System.out.println(raw_accel.getx_accel());
						data2.add(accel);
					}
					draw(data2);
					br.close();
					existdata2 = true;
					lb4.setText(filename);
					lb4.setTextFill(Color.BLUE);

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}
	}

	class CreateEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			if (e.getSource() == mi_1[0]) { // ローパスデータ生成
				ta.clear();
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("./Myfile"));
				FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
				fc.getExtensionFilters().add(extensionFilter);
				try {
					List<File> flr = fc.showOpenMultipleDialog(new Stage());

					for (File f : flr) {
						StringBuilder fileName = new StringBuilder();
						fileName.append(f.getName());
						String ParentFile = f.getName().replace(".csv", "");

						// 出力先を作成する true=追記, false=上書き
						FileWriter fw = new FileWriter("Myfile/" + ParentFile + "_lowpass.csv", true);
						PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
						pw.println("TimeStamp,X,Y,Z");

						raw_data = new ArrayList<Accel>();
						BufferedReader br = new BufferedReader(new FileReader(f));

						String str = null;
						// 1行目はシカト
						br.readLine();
						// System.out.println(br.readLine());
						while ((str = br.readLine()) != null) {
							// 1行分の加速度データの登録
							Accel raw_accel = new Accel();
							String[] accel_data = str.split(",");
							raw_accel.setx_accel(Float.parseFloat(accel_data[1]));
							raw_accel.sety_accel(Float.parseFloat(accel_data[2]));
							raw_accel.setz_accel(Float.parseFloat(accel_data[3]));
							raw_accel.settimestamp(Long.parseLong(accel_data[0].replace(" ", "")));
							raw_data.add(raw_accel);
						}
						br.close();

						lowpass_data = new ArrayList<Accel>();
						Accel lowpass_accel = new Accel();
						// 1つ目の加速度データ登録
						lowpass_accel.setx_accel(raw_data.get(0).getx_accel());
						lowpass_accel.sety_accel(raw_data.get(0).gety_accel());
						lowpass_accel.setz_accel(raw_data.get(0).getz_accel());
						lowpass_accel.settimestamp(raw_data.get(0).gettimestamp());
						lowpass_data.add(lowpass_accel);
						// 2つ目以降
						for (int i = 1; i < raw_data.size(); i++) {
							lowpass_accel = new Accel();
							lowpass_accel.setx_accel(lowpass_data.get(i - 1).getx_accel() * lowpass_value
									+ raw_data.get(i).getx_accel() * (1 - lowpass_value));
							lowpass_accel.sety_accel(lowpass_data.get(i - 1).gety_accel() * lowpass_value
									+ raw_data.get(i).gety_accel() * (1 - lowpass_value));
							lowpass_accel.setz_accel(lowpass_data.get(i - 1).getz_accel() * lowpass_value
									+ raw_data.get(i).getz_accel() * (1 - lowpass_value));
							lowpass_accel.settimestamp(raw_data.get(i).gettimestamp());
							lowpass_data.add(lowpass_accel);
						}

						for (int i = 0; i < lowpass_data.size(); i++) {
							pw.println(lowpass_data.get(i).gettimestamp() + "," + lowpass_data.get(i).getx_accel() + ","
									+ lowpass_data.get(i).gety_accel() + "," + lowpass_data.get(i).getz_accel());
						}
						System.out.println(f.getName() + " is finish.");
						ta.appendText(f.getName() + " is finish." + "\n");
						// ファイルに書き出す
						pw.close();
						fw.close();
					}
					System.out.println("All finish.");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (e.getSource() == mi_1[1]) { // 平均データ生成
				ta.clear();
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("./Myfile"));
				FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
				fc.getExtensionFilters().add(extensionFilter);
				try {
					List<File> flr = fc.showOpenMultipleDialog(new Stage());

					for (File f : flr) {
						StringBuilder fileName = new StringBuilder();
						fileName.append(f.getName());
						String ParentFile = f.getName().replace(".csv", "");

						lowpass_data = new ArrayList<Accel>();
						BufferedReader br = new BufferedReader(new FileReader(f));

						String str = null;
						// 1行目はシカト
						br.readLine();
						// System.out.println(br.readLine());
						while ((str = br.readLine()) != null) {
							// 1行分の加速度データの登録
							Accel lowpass_accel = new Accel();
							String[] accel_data = str.split(",");
							lowpass_accel.setx_accel(Float.parseFloat(accel_data[1]));
							lowpass_accel.sety_accel(Float.parseFloat(accel_data[2]));
							lowpass_accel.setz_accel(Float.parseFloat(accel_data[3]));
							lowpass_accel.settimestamp(Long.parseLong(accel_data[0]));
							lowpass_data.add(lowpass_accel);
						}
						br.close(); // 読み込み完了

						// 1秒ごとにした平均保持するArrayListのインスタンスを生成
						average_data = new ArrayList<Accel>(); // 1秒毎の平均値を入れるためのAccel

						// 一秒間のいくつかのデータを返す
						int data = 0; // 今参照しているデータ
						int nextstart = 0; // 次始める部分
						long startTime = lowpass_data.get(0).gettimestamp(); // １秒間のデータを取得し始める時間

						while (data < lowpass_data.size()) {// データのある限り続ける

							data = nextstart;// １秒間うちの0.5秒以上たった地点のインデックス番号
							nextstart = 0;

							ArrayList<Accel> temporaly_data = new ArrayList<Accel>();// 1秒間のデータを入れる箱
							temporaly_data.add(lowpass_data.get(data));
							long timelag = 0;// 1000ミリ秒カウント

							// １秒間分のデータをtemporaly_dataに保持する
							while (true) {
								if (timelag >= OverLap && nextstart == 0) {
									nextstart = data;// 0.5秒を超えた地点のデータを取っておく
								}
								data++;
								if (data >= lowpass_data.size()) {
									break;
								}
								timelag = lowpass_data.get(data).gettimestamp() - startTime;
								// System.out.println(startTime + " "+ timelag +
								// " " + temporaly_data.size());
								if (timelag >= Time_duration) {
									// System.out.println(startTime);
									startTime = startTime + OverLap; // (OverLap)ミリ秒ずらす
									break;
								}
								temporaly_data.add(lowpass_data.get(data));
							}

							/*
							 * for(int i=0; i<temporaly_data.size(); i++){
							 * System.out.println(temporaly_data.get(i).
							 * gettimestamp() + " " +
							 * temporaly_data.get(i).getx_accel() + " " +
							 * temporaly_data.get(i).gety_accel() + " "
							 * +temporaly_data.get(i).getz_accel()); }
							 */
							// System.out.println("");

							// 1秒分データがなかったら終了
							if (timelag < Time_duration) {
								break;
							}

							// 平均算出
							float sum_x = 0;
							float sum_y = 0;
							float sum_z = 0;

							for (int i = 0; i < temporaly_data.size(); i++) {
								sum_x += temporaly_data.get(i).getx_accel();
								sum_y += temporaly_data.get(i).gety_accel();
								sum_z += temporaly_data.get(i).getz_accel();
							}
							float ave_x = sum_x / temporaly_data.size();
							float ave_y = sum_y / temporaly_data.size();
							float ave_z = sum_z / temporaly_data.size();
							Accel average_accel = new Accel();
							// ta.appendText(ave_x + "," + ave_y + "," + ave_z +
							// "\n");
							average_accel.setx_accel(ave_x);
							average_accel.sety_accel(ave_y);
							average_accel.setz_accel(ave_z);
							average_data.add(average_accel);
						}

						// 出力先を作成する true=追記, false=上書き
						FileWriter fw = new FileWriter("./average/" + ParentFile + "_average.csv", true);
						PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
						pw.println("X,Y,Z");

						for (int i = 0; i < average_data.size(); i++) {
							pw.println(average_data.get(i).getx_accel() + "," + average_data.get(i).gety_accel() + ","
									+ average_data.get(i).getz_accel());
						}
						System.out.println(f.getName() + " is finish.");
						ta.appendText(f.getName() + " is finish." + "\n");
						// ファイルに書き出す
						pw.close();
						fw.close();
					}
					ta.appendText("All finish.");
					System.out.println("All finish.");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (e.getSource() == mi_1[2]) { // 分散作成
				ta.clear();
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("./Myfile"));
				FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
				fc.getExtensionFilters().add(extensionFilter);
				try {
					List<File> flr = fc.showOpenMultipleDialog(new Stage());

					for (File f : flr) {
						StringBuilder fileName = new StringBuilder();
						fileName.append(f.getName());
						String ParentFile = f.getName().replace(".csv", "");

						lowpass_data = new ArrayList<Accel>();
						BufferedReader br = new BufferedReader(new FileReader(f));

						String str = null;
						// 1行目はシカト
						br.readLine();
						// System.out.println(br.readLine());
						while ((str = br.readLine()) != null) {
							// 1行分の加速度データの登録
							Accel lowpass_accel = new Accel();
							String[] accel_data = str.split(",");
							lowpass_accel.setx_accel(Float.parseFloat(accel_data[1]));
							lowpass_accel.sety_accel(Float.parseFloat(accel_data[2]));
							lowpass_accel.setz_accel(Float.parseFloat(accel_data[3]));
							lowpass_accel.settimestamp(Long.parseLong(accel_data[0]));
							lowpass_data.add(lowpass_accel);
						}
						br.close(); // 読み込み完了

						// 1秒ごとにした平均保持するArrayListのインスタンスを生成
						variance_data = new ArrayList<Accel>(); // 1秒毎の平均値を入れるためのAccel

						// 一秒間のいくつかのデータを返す
						int data = 0; // 今参照しているデータ
						int nextstart = 0; // 次始める部分
						long startTime = lowpass_data.get(0).gettimestamp(); // １秒間のデータを取得し始める時間

						while (data < lowpass_data.size()) {// データのある限り続ける

							data = nextstart;// １秒間うちの0.5秒以上たった地点のインデックス番号
							nextstart = 0;

							ArrayList<Accel> temporaly_data = new ArrayList<Accel>();// 1秒間のデータを入れる箱
							temporaly_data.add(lowpass_data.get(data));
							long timelag = 0;// 1000ミリ秒カウント

							// １秒間分のデータをtemporaly_dataに保持する
							while (true) {
								if (timelag >= OverLap && nextstart == 0) {
									nextstart = data;// 0.5秒を超えた地点のデータを取っておく
								}
								data++;
								if (data >= lowpass_data.size()) {
									break;
								}
								timelag = lowpass_data.get(data).gettimestamp() - startTime;
								// System.out.println(startTime + " "+ timelag +
								// " " + temporaly_data.size());
								if (timelag >= Time_duration) {
									// System.out.println(startTime);
									startTime = startTime + OverLap; // 500ミリ秒ずらす
									break;
								}
								temporaly_data.add(lowpass_data.get(data));
							}

							/*
							 * for(int i=0; i<temporaly_data.size(); i++){
							 * System.out.println(temporaly_data.get(i).
							 * gettimestamp() + " " +
							 * temporaly_data.get(i).getx_accel() + " " +
							 * temporaly_data.get(i).gety_accel() + " "
							 * +temporaly_data.get(i).getz_accel()); }
							 */
							// System.out.println("");

							// 1秒分データがなかったら終了
							if (timelag < Time_duration) {
								break;
							}

							// 平均算出
							float ave_x = 0;
							float ave_y = 0;
							float ave_z = 0;

							for (int i = 0; i < temporaly_data.size(); i++) {
								ave_x += temporaly_data.get(i).getx_accel();
								ave_y += temporaly_data.get(i).gety_accel();
								ave_z += temporaly_data.get(i).getz_accel();
							}
							ave_x = ave_x / temporaly_data.size();
							ave_y = ave_y / temporaly_data.size();
							ave_z = ave_z / temporaly_data.size();

							// 分散算出
							float var_x = 0;
							float var_y = 0;
							float var_z = 0;
							for (int i = 0; i < temporaly_data.size(); i++) {
								var_x += (temporaly_data.get(i).getx_accel() - ave_x)
										* (temporaly_data.get(i).getx_accel() - ave_x);
								var_y += (temporaly_data.get(i).gety_accel() - ave_y)
										* (temporaly_data.get(i).gety_accel() - ave_y);
								var_z += (temporaly_data.get(i).getz_accel() - ave_z)
										* (temporaly_data.get(i).getz_accel() - ave_z);
							}
							var_x = var_x / temporaly_data.size();
							var_y = var_y / temporaly_data.size();
							var_z = var_z / temporaly_data.size();

							Accel variance_accel = new Accel();
							// ta.appendText(var_x + "," + var_y + "," + var_z +
							// "\n");

							variance_accel.setx_accel(var_x);
							variance_accel.sety_accel(var_y);
							variance_accel.setz_accel(var_z);

							variance_data.add(variance_accel);
						}

						// 出力先を作成する true=追記, false=上書き
						FileWriter fw = new FileWriter("./variance/" + ParentFile + "_variance.csv", true);
						PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
						pw.println("X,Y,Z");

						for (int i = 0; i < variance_data.size(); i++) {
							pw.println(variance_data.get(i).getx_accel() + "," + variance_data.get(i).gety_accel() + ","
									+ variance_data.get(i).getz_accel());
						}
						System.out.println(f.getName() + " is finish.");
						ta.appendText(f.getName() + " is finish." + "\n");
						// ファイルに書き出す
						pw.close();
						fw.close();
					}
					ta.appendText("All finish.");
					System.out.println("All finish.");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (e.getSource() == mi_1[3]) { // 線形補間(木下)うまくかない
				linearinterpolation_standard = new ArrayList<Accel>();
				linearinterpolation_temp = new ArrayList<Accel>();

				// 基準となるファイルの選択と登録
				try {
					FileChooser fc = new FileChooser();
					fc.setInitialDirectory(new File("./Myfile"));
					FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
					fc.getExtensionFilters().add(extensionFilter);
					File flr = fc.showOpenDialog(new Stage());
					filename = flr.getName();
					BufferedReader br = new BufferedReader(new FileReader(flr));
					String str = null;
					// 1行目はシカト（文字列だろうから）
					br.readLine();
					while ((str = br.readLine()) != null) {
						// ta.appendText(str + "\n");
						// 1行分の加速度データの登録
						Accel accel = new Accel();
						String[] accel_data = str.split(",");
						accel.setx_accel(Float.parseFloat(accel_data[1]));
						accel.sety_accel(Float.parseFloat(accel_data[2]));
						accel.setz_accel(Float.parseFloat(accel_data[3]));
						accel.settimestamp(Long.parseLong(accel_data[0]));
						linearinterpolation_standard.add(accel);
					}
					br.close();

					ta.appendText(filename + "の読み込みが完了しました。" + "\n");

					// 補間を行うデータの選択と登録
					fc = new FileChooser();
					fc.setInitialDirectory(new File("./Myfile"));
					extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
					fc.getExtensionFilters().add(extensionFilter);
					flr = fc.showOpenDialog(new Stage());
					filename = flr.getName();
					br = new BufferedReader(new FileReader(flr));
					str = null;
					// 1行目はシカト（文字列だろうから）
					br.readLine();
					while ((str = br.readLine()) != null) {
						// ta.appendText(str + "\n");
						// 1行分の加速度データの登録
						Accel accel = new Accel();
						String[] accel_data = str.split(",");
						accel.setx_accel(Float.parseFloat(accel_data[1]));
						accel.sety_accel(Float.parseFloat(accel_data[2]));
						accel.setz_accel(Float.parseFloat(accel_data[3]));
						accel.settimestamp(Long.parseLong(accel_data[0]));
						linearinterpolation_temp.add(accel);
					}
					br.close();
					ta.appendText(filename + "の読み込みが完了しました。" + "\n");

					// ここから処理
					// ミリ秒単位で一致していればそれを採用する．
					// そうでなければ，基準となるタイムスタンプを挟むように補完する側のタイムスタンプを選択する．そして補完しlinearInterpolation_dataに登録
					// 基準となるタイムスタンプを挟めないならその基準における補完は行わない．

					linearInterpolation_data = new ArrayList<Accel>();
					int standardTimeStamp_size = linearinterpolation_standard.size();
					int temp_data_size = linearinterpolation_temp.size();
					int temp_data_index = 0;
					boolean start_flag = true; // 初めのデータ合わせ

					for (int i = 0; i < standardTimeStamp_size; i++) {
						long standard_tm = linearinterpolation_standard.get(i).gettimestamp();
						long temp_tm_0 = 0;
						long temp_tm_1 = 0;

						if (start_flag) {
							temp_tm_0 = linearinterpolation_temp.get(i).gettimestamp();
							temp_tm_1 = linearinterpolation_temp.get(i + 1).gettimestamp();
						}

						if (standard_tm < temp_tm_0 && standard_tm < temp_tm_1 && start_flag) {
							continue;
						}

						point1: while (temp_data_index != temp_data_size - 1) {
							start_flag = false;

							temp_tm_0 = linearinterpolation_temp.get(temp_data_index).gettimestamp();
							temp_tm_1 = linearinterpolation_temp.get(temp_data_index + 1).gettimestamp();

							System.out.println(i + "," + standard_tm + "," + temp_tm_0 + "," + temp_tm_1);

							if (temp_tm_0 == standard_tm) {
								// 前方一致
								linearInterpolation_data.add(raw_data.get(temp_data_index));
								temp_data_index++;
								break point1;

							} else if (temp_tm_1 == standard_tm) {
								// 後方一致
								linearInterpolation_data.add(raw_data.get(temp_data_index + 1));
								temp_data_index++;
								break point1;
							} else if (temp_tm_0 < standard_tm && standard_tm < temp_tm_1) {
								// 挟まれたので補完する
								float temp_x_0 = linearinterpolation_temp.get(temp_data_index).getx_accel();
								float temp_x_1 = linearinterpolation_temp.get(temp_data_index + 1).getx_accel();
								float temp_y_0 = linearinterpolation_temp.get(temp_data_index).gety_accel();
								float temp_y_1 = linearinterpolation_temp.get(temp_data_index + 1).gety_accel();
								float temp_z_0 = linearinterpolation_temp.get(temp_data_index).getz_accel();
								float temp_z_1 = linearinterpolation_temp.get(temp_data_index + 1).getz_accel();

								float inter_x_acc = calculateLinearInterpolation(temp_tm_0, temp_x_0, temp_tm_1,
										temp_x_1, standard_tm);
								float inter_y_acc = calculateLinearInterpolation(temp_tm_0, temp_y_0, temp_tm_1,
										temp_y_1, standard_tm);
								float inter_z_acc = calculateLinearInterpolation(temp_tm_0, temp_z_0, temp_tm_1,
										temp_z_1, standard_tm);

								Accel ac = new Accel();
								ac.settimestamp(standard_tm);
								ac.setx_accel(inter_x_acc);
								ac.sety_accel(inter_y_acc);
								ac.setz_accel(inter_z_acc);
								linearInterpolation_data.add(ac);
								temp_data_index++;
								break point1;
							} else if (temp_tm_0 < standard_tm && temp_tm_1 < standard_tm) {
								// System.out.println(basic_tm+","+raw_tm_0+","+raw_tm_1);
								// どっちも前だからindexを進める
								temp_data_index++;

							} else if (temp_tm_0 == temp_tm_1) {// まったく同じtimestampが記録されていたとき
								temp_data_index++;
							}
						}

					}
					// 出力処理
					filename = filename.replace(".csv", "") + "_LinearInterpolation";
					fc.setInitialFileName(filename);
					fc.setInitialDirectory(new File("./Myfile"));
					fc.getExtensionFilters().add(extensionFilter);
					File flw = fc.showSaveDialog(new Stage());
					FileOutputStream out = null;
					OutputStreamWriter osw = null;
					BufferedWriter bw = null;
					try {
						bw = new BufferedWriter(new FileWriter(flw));
						out = new FileOutputStream(flw);
						osw = new OutputStreamWriter(out);
						bw = new BufferedWriter(osw);
						bw.append("TimeStamp,");
						bw.append("X,");
						bw.append("Y,");
						bw.append("Z").append("\r");
						for (int i = 0; i < linearInterpolation_data.size(); i++) {
							bw.append(String.valueOf(linearInterpolation_data.get(i).gettimestamp()) + ",");
							bw.append(String.valueOf(linearInterpolation_data.get(i).getx_accel()) + ",");
							bw.append(String.valueOf(linearInterpolation_data.get(i).gety_accel()) + ",");
							bw.append(String.valueOf(linearInterpolation_data.get(i).getz_accel())).append("\r");
						}
					} catch (Exception e1) {

					} finally {
						if (bw != null) {
							try {
								bw.close();
								bw = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						if (osw != null) {
							try {
								osw.close();
								osw = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						if (out != null) {
							try {
								out.close();
								out = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}

				} catch (FileNotFoundException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
			} else if (e.getSource() == mi_1[4]) { // 線形補間（中野）
				ta.clear();
				linear_init = true;
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("./Myfile"));
				FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
				fc.getExtensionFilters().add(extensionFilter);
				try {
					List<File> flr = fc.showOpenMultipleDialog(new Stage());

					for (File f: flr) {
						StringBuilder fileName = new StringBuilder();
						fileName.append(f.getName());
						String ParentFile = f.getName().replace(".csv", "");

						//出力先を作成する true=追記, false=上書き
						FileWriter fw = new FileWriter("Myfile/"+ParentFile+"_linernInterPolation.csv", true);
			            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
			            pw.println("TimeStamp,X,Y,Z");

			            linear_before = new ArrayList<Accel>();
						BufferedReader br = new BufferedReader(new FileReader(f));

						String str = null;
						// 1行目はシカト
						br.readLine();
						//System.out.println(br.readLine());
						while ((str = br.readLine()) != null) {
							// 1行分の加速度データの登録
							Accel raw_accel = new Accel();
							String[] accel_data = str.split(",");
							raw_accel.setx_accel(Float.parseFloat(accel_data[1]));
							raw_accel.sety_accel(Float.parseFloat(accel_data[2]));
							raw_accel.setz_accel(Float.parseFloat(accel_data[3]));
							raw_accel.settimestamp(Long.parseLong(accel_data[0].replace(" ", "")));
							linear_before.add(raw_accel);
						}
						br.close();

						if(linear_init) {
							initialize();
						}

						linear_after = new ArrayList<Accel>();

						//1つ目は別で時間を設定
						Accel linear_accel = new Accel();
						linear_accel.settimestamp(linear_startTime);
						linear_forward=0;
						linear_back=0;
						for(int i=0; i<linear_before.size();i++) {
							if(linear_accel.gettimestamp()==linear_before.get(i).gettimestamp()) {
								linear_accel.setx_accel(linear_before.get(i).getx_accel());
								linear_accel.sety_accel(linear_before.get(i).gety_accel());
								linear_accel.setz_accel(linear_before.get(i).getz_accel());
								linear_after.add(linear_accel);
								break;
							}else if(linear_accel.gettimestamp() > linear_before.get(i).gettimestamp()) {
								linear_back = i;
							}else if(linear_accel.gettimestamp() < linear_before.get(i).gettimestamp()) {
								linear_forward = i;
								linear_accel.setx_accel(calculateLinearInterpolation(linear_before.get(linear_back).gettimestamp(), linear_before.get(linear_back).getx_accel(),
										linear_before.get(linear_forward).gettimestamp(), linear_before.get(linear_forward).getx_accel(), linear_accel.gettimestamp()));
								linear_accel.sety_accel(calculateLinearInterpolation(linear_before.get(linear_back).gettimestamp(), linear_before.get(linear_back).gety_accel(),
										linear_before.get(linear_forward).gettimestamp(), linear_before.get(linear_forward).gety_accel(), linear_accel.gettimestamp()));
								linear_accel.setz_accel(calculateLinearInterpolation(linear_before.get(linear_back).gettimestamp(), linear_before.get(linear_back).getz_accel(),
										linear_before.get(linear_forward).gettimestamp(), linear_before.get(linear_forward).getz_accel(), linear_accel.gettimestamp()));
								linear_after.add(linear_accel);
								break;
							}
						}

						//2つ目以降は1つ前の時間を参照
						for(int i=1; i<linear_new_datasize; i++) {
							linear_accel = new Accel();
							linear_accel.settimestamp(linear_after.get(i-1).gettimestamp()+linear_deltaTime);
							linear_forward=0;
							linear_back=0;
							for(int j=0; j<linear_before.size(); j++) {
								if(linear_accel.gettimestamp()==linear_before.get(j).gettimestamp()) {
									linear_accel.setx_accel(linear_before.get(j).getx_accel());
									linear_accel.sety_accel(linear_before.get(j).gety_accel());
									linear_accel.setz_accel(linear_before.get(j).getz_accel());
									linear_after.add(linear_accel);
									break;
								}else if(linear_accel.gettimestamp() > linear_before.get(j).gettimestamp()) {
									linear_back = j;
								}else if(linear_accel.gettimestamp() < linear_before.get(j).gettimestamp()) {
									linear_forward = j;
									linear_accel.setx_accel(calculateLinearInterpolation(linear_before.get(linear_back).gettimestamp(), linear_before.get(linear_back).getx_accel(),
											linear_before.get(linear_forward).gettimestamp(), linear_before.get(linear_forward).getx_accel(), linear_accel.gettimestamp()));
									linear_accel.sety_accel(calculateLinearInterpolation(linear_before.get(linear_back).gettimestamp(), linear_before.get(linear_back).gety_accel(),
											linear_before.get(linear_forward).gettimestamp(), linear_before.get(linear_forward).gety_accel(), linear_accel.gettimestamp()));
									linear_accel.setz_accel(calculateLinearInterpolation(linear_before.get(linear_back).gettimestamp(), linear_before.get(linear_back).getz_accel(),
											linear_before.get(linear_forward).gettimestamp(), linear_before.get(linear_forward).getz_accel(), linear_accel.gettimestamp()));
									linear_after.add(linear_accel);
									break;
								}
							}
						}


						for (int i = 0; i < linear_after.size(); i++) {
							pw.println(linear_after.get(i).gettimestamp()+","
									+ linear_after.get(i).getx_accel() +","
									+ linear_after.get(i).gety_accel() +","
									+ linear_after.get(i).getz_accel() );
						}

						ta.appendText(f.getName()+" is finish." + "\n");
						//System.out.println(f.getName()+" is finish.");

			            //ファイルに書き出す
			            pw.close();
			            fw.close();
					}

					ta.appendText("All finish" + "\n");
		            //System.out.println("All finish.");
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}else if (e.getSource() == mi_1[5]) {//カット部分
				//TODO
				ta.clear();
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("./Myfile"));
				FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
				fc.getExtensionFilters().add(extensionFilter);

				String[] cut = ntf2.getText().split(",");

				int big = Integer.parseInt(cut[0]);
				int fin = Integer.parseInt(cut[1]);


				try {
					List<File> flr = fc.showOpenMultipleDialog(new Stage());
					for (File f: flr) {
						StringBuilder fileName = new StringBuilder();
						fileName.append(f.getName());
						//ファイル名を取得
						String ParentFile = f.getName().replace(".csv", "");
						//Accel型のデータを保存
						raw_data = new ArrayList<Accel>();
						BufferedReader br = new BufferedReader(new FileReader(f));

						String str = null;
						// 1行目はシカト
						br.readLine();
						// System.out.println(br.readLine());
						while ((str = br.readLine()) != null) {
							// 1行分の加速度データの登録
							Accel raw_accel = new Accel();
							String[] accel_data = str.split(",");
							raw_accel.setx_accel(Float.parseFloat(accel_data[1]));
							raw_accel.sety_accel(Float.parseFloat(accel_data[2]));
							raw_accel.setz_accel(Float.parseFloat(accel_data[3]));
							raw_accel.settimestamp(Long.parseLong(accel_data[0]));
							raw_data.add(raw_accel);
						}
						br.close(); // 読み込み完了

						//出力先を作成する true=追記, false=上書き
			            FileWriter fw = new FileWriter("Myfile/" + ParentFile + "_cut.csv", true);
						PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
						pw.println("TimeStamp,X,Y,Z");

						for (int i = big; i < fin; i++) {
							pw.println(raw_data.get(i).gettimestamp()+","+raw_data.get(i).getx_accel() + "," + raw_data.get(i).gety_accel() + ","
									+ raw_data.get(i).getz_accel());
						}
						System.out.println(f.getName() + " is finish.");
						ta.appendText(f.getName() + " is finish." + "\n");
						// ファイルに書き出す
						pw.close();
						fw.close();

					}
					ta.appendText("All finish" + "\n");
					//System.out.println("All finish.");
				} catch(Exception ex){
					System.out.println("書き込みエラー");
				}

			}
		}

	}

	public void initialize() {
        TextInputDialog iptDlg  = new TextInputDialog();
		iptDlg.setTitle("initialize");
		iptDlg.setHeaderText(null);
		iptDlg.setContentText("StartTimeを入力\n"+String.valueOf(linear_before.get(0).gettimestamp()));
		Optional<String> result = iptDlg.showAndWait();
		//ta.appendText(String.valueOf(result) + "\n");
		linear_startTime = Long.parseLong((String.valueOf(result).replace("Optional[", "")).replace("]", ""));
		ta.appendText(String.valueOf(linear_startTime) + "\n");
        TextInputDialog iptDlg2  = new TextInputDialog();
		iptDlg2.setTitle("initialize");
		iptDlg2.setHeaderText(null);
		iptDlg2.setContentText("時間の間隔を入力 ex20(ms)");
		Optional<String> result2 = iptDlg2.showAndWait();
		linear_deltaTime = Long.parseLong((String.valueOf(result2).replace("Optional[", "")).replace("]", ""));
		ta.appendText(String.valueOf(linear_deltaTime) + "\n");

		TextInputDialog iptDlg3  = new TextInputDialog();
		iptDlg3.setTitle("initialize");
		iptDlg3.setHeaderText(null);
		iptDlg3.setContentText("時間の長さを入力(s) ex60(s)");
		Optional<String> result3 = iptDlg3.showAndWait();
		linear_TimeSize = 1000L * Long.parseLong((String.valueOf(result3).replace("Optional[", "")).replace("]", ""));
		ta.appendText(String.valueOf(linear_TimeSize) + "\n");
		linear_new_datasize = (int)(linear_TimeSize/(long)linear_deltaTime);
		linear_init=false;
	}

	/**
	 *
	 * @param t0
	 * @param y0
	 * @param t1
	 * @param t1
	 * @param t
	 *            予測したい時間
	 * @return tのときの予測される値
	 */
	private float calculateLinearInterpolation(long t0, float y0, long t1, float y1, long t) {
		float y = y0 + (y1 - y0) * (t - t0) / (t1 - t0);
		return y;
	}

	class UpdateEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			// System.out.println("Update");
			// グラフの初期化
			for (int i = 0; i < pastXYchart.size(); i++) {
				XYChart.Series removeXYchart = pastXYchart.get(i);
				lineChart.getData().removeAll(removeXYchart);
				ta.clear();
				ta.appendText("グラフを初期化しました");
			}
			pastXYchart = new ArrayList<XYChart.Series>();

			// 再描画
			if (existdata1) {
				draw(data1);
			}
			if (existdata2) {
				draw(data2);
			}
		}
	}

	class Initialize implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			if (e.getSource() == mi_2[0]) {// グラフ初期化
				for (int i = 0; i < pastXYchart.size(); i++) {
					XYChart.Series removeXYchart = pastXYchart.get(i);
					lineChart.getData().removeAll(removeXYchart);
					ta.clear();
					ta.appendText("グラフを初期化しました");
				}
				pastXYchart = new ArrayList<XYChart.Series>();
			} else if (e.getSource() == mi_2[1]) {// Data-1初期化
				data1 = null;
				existdata1 = false;
				lb3.setText("Data-1");
				lb3.setTextFill(Color.RED);
				ta.clear();
				ta.appendText("Data-1を初期化しました");
			} else if (e.getSource() == mi_2[2]) {// Data-2初期化
				data2 = null;
				existdata2 = false;
				lb4.setText("Data-2");
				lb4.setTextFill(Color.RED);
				ta.clear();
				ta.appendText("Data-2を初期化しました");
			}

		}
	}

	public void draw(ArrayList<Accel> data) {
		int accel_length = data.size();
		// Prepare XYChart.Series objects by setting data
		x_series = new XYChart.Series();
		y_series = new XYChart.Series();
		z_series = new XYChart.Series();
		x_series.setName("X軸");
		y_series.setName("Y軸");
		z_series.setName("Z軸");
		if (ch[0].isSelected()) {
			for (int i = 0; i < accel_length; i++) {
				x_series.getData().add(new XYChart.Data<>(i, data.get(i).getx_accel()));
			}
			lineChart.getData().addAll(x_series);
			pastXYchart.add(x_series);
		}
		if (ch[1].isSelected()) {
			for (int i = 0; i < accel_length; i++) {
				y_series.getData().add(new XYChart.Data<>(i, data.get(i).gety_accel()));
			}
			lineChart.getData().addAll(y_series);
			pastXYchart.add(y_series);
		}
		if (ch[2].isSelected()) {
			for (int i = 0; i < accel_length; i++) {
				z_series.getData().add(new XYChart.Data<>(i, data.get(i).getz_accel()));
			}
			lineChart.getData().addAll(z_series);
			pastXYchart.add(z_series);
		}
	}

	/*
	class CreateEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			if (e.getSource() == mi_1[0]) { // ローパスデータ生成
				if (raw_data != null) {
					// ローパスフィルタをかけた加速度を保持するArrayListのインスタンスを生成
					lowpass_data = new ArrayList<Accel>();
					Accel lowpass_accel = new Accel();
					lowpass_value = Float.parseFloat(ntf.getText());
					// 1つ目の加速度データ登録
					lowpass_accel.setx_accel(raw_data.get(0).getx_accel());
					lowpass_accel.sety_accel(raw_data.get(0).gety_accel());
					lowpass_accel.setz_accel(raw_data.get(0).getz_accel());
					lowpass_accel.settimestamp(raw_data.get(0).gettimestamp());
					lowpass_data.add(lowpass_accel);
					// 2つ目以降
					for (int i = 1; i < raw_data.size(); i++) {
						lowpass_accel = new Accel();
						lowpass_accel.setx_accel(lowpass_data.get(i - 1).getx_accel() * lowpass_value
								+ raw_data.get(i).getx_accel() * (1 - lowpass_value));
						lowpass_accel.sety_accel(lowpass_data.get(i - 1).gety_accel() * lowpass_value
								+ raw_data.get(i).gety_accel() * (1 - lowpass_value));
						lowpass_accel.setz_accel(lowpass_data.get(i - 1).getz_accel() * lowpass_value
								+ raw_data.get(i).getz_accel() * (1 - lowpass_value));
						lowpass_accel.settimestamp(raw_data.get(i).gettimestamp());
						lowpass_data.add(lowpass_accel);
					}
					// テキストエリアに表示
					ta.clear();
					for (int i = 0; i < lowpass_data.size(); i++) {
						ta.appendText(lowpass_data.get(i).gettimestamp() + "," + lowpass_data.get(i).getx_accel() + ","
								+ lowpass_data.get(i).gety_accel() + "," + lowpass_data.get(i).getz_accel() + "\n");
					}
					draw(lowpass_data);

					// CSVファイル作成
					FileChooser fc = new FileChooser();
					filename = filename.replace(".csv", "") + "_lowpass";
					fc.setInitialFileName(filename);
					fc.setInitialDirectory(new File("./Myfile"));
					FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
					fc.getExtensionFilters().add(extensionFilter);
					File flw = fc.showSaveDialog(new Stage());
					FileOutputStream out = null;
					OutputStreamWriter osw = null;
					BufferedWriter bw = null;
					try {
						bw = new BufferedWriter(new FileWriter(flw));
						out = new FileOutputStream(flw);
						osw = new OutputStreamWriter(out);
						bw = new BufferedWriter(osw);
						bw.append("TimeStamp,");
						bw.append("X,");
						bw.append("Y,");
						bw.append("Z").append("\r");
						for (int i = 0; i < lowpass_data.size(); i++) {
							bw.append(String.valueOf(lowpass_data.get(i).gettimestamp()) + ",");
							bw.append(String.valueOf(lowpass_data.get(i).getx_accel()) + ",");
							bw.append(String.valueOf(lowpass_data.get(i).gety_accel()) + ",");
							bw.append(String.valueOf(lowpass_data.get(i).getz_accel())).append("\r");
						}
					} catch (Exception e1) {

					} finally {
						if (bw != null) {
							try {
								bw.close();
								bw = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						if (osw != null) {
							try {
								osw.close();
								osw = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						if (out != null) {
							try {
								out.close();
								out = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				} else {
					ta.clear();
					ta.appendText("生データを読み込んでいません");
				}
			} else if (e.getSource() == mi_1[1]) {// 平均データ作成
				ta.clear();

				if (lowpass_data != null) { /// ローパス後をとりあえず使う
					// 1秒ごとにした平均保持するArrayListのインスタンスを生成
					average_data = new ArrayList<Accel>(); // 1秒毎の平均値を入れるためのAccel

					// 一秒間のいくつかのデータを返す
					int data = 0; // 今参照しているデータ
					int nextstart = 0; // 次始める部分
					long startTime = lowpass_data.get(0).gettimestamp(); // １秒間のデータを取得し始める時間

					while (data < lowpass_data.size()) {// データのある限り続ける

						data = nextstart;// １秒間うちの0.5秒以上たった地点のインデックス番号
						nextstart = 0;

						ArrayList<Accel> temporaly_data = new ArrayList<Accel>();// 1秒間のデータを入れる箱
						temporaly_data.add(lowpass_data.get(data));
						long timelag = 0;// 1000ミリ秒カウント

						// １秒間分のデータをtemporaly_dataに保持する
						while (true) {
							if (timelag >= 500 && nextstart == 0) {
								nextstart = data;// 0.5秒を超えた地点のデータを取っておく
							}

							data++;

							if (data >= lowpass_data.size()) {
								break;
							}

							timelag = lowpass_data.get(data).gettimestamp() - startTime;
							// System.out.println(startTime + " "+ timelag + " "
							// + temporaly_data.size());

							if (timelag >= 1000) {
								System.out.println(startTime);
								startTime = startTime + 500; // 500ミリ秒ずらす
								break;
							}

							temporaly_data.add(lowpass_data.get(data));

						}

						for (int i = 0; i < temporaly_data.size(); i++) {
							System.out.println(temporaly_data.get(i).gettimestamp() + " "
									+ temporaly_data.get(i).getx_accel() + " " + temporaly_data.get(i).gety_accel()
									+ " " + temporaly_data.get(i).getz_accel());
						}
						System.out.println("");

						// 1秒分データがなかったら終了
						if (timelag < 1000) {
							break;
						}

						// 平均算出
						float sum_x = 0;
						float sum_y = 0;
						float sum_z = 0;

						for (int i = 0; i < temporaly_data.size(); i++) {
							sum_x += temporaly_data.get(i).getx_accel();
							sum_y += temporaly_data.get(i).gety_accel();
							sum_z += temporaly_data.get(i).getz_accel();
						}
						float ave_x = sum_x / temporaly_data.size();
						float ave_y = sum_y / temporaly_data.size();
						float ave_z = sum_z / temporaly_data.size();

						Accel average_accel = new Accel();

						ta.appendText(ave_x + "," + ave_y + "," + ave_z + "\n");

						average_accel.setx_accel(ave_x);
						average_accel.sety_accel(ave_y);
						average_accel.setz_accel(ave_z);

						average_data.add(average_accel);

					}

					// 幾つかのデータの平均をとる

					// CSVファイル作成
					FileChooser fc = new FileChooser();// 保存先選ぶ
					filename = filename.replace(".csv", "") + "_average";
					fc.setInitialFileName(filename);
					fc.setInitialDirectory(new File("./Myfile"));
					FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
					fc.getExtensionFilters().add(extensionFilter);
					File flw = fc.showSaveDialog(new Stage());
					FileOutputStream out = null;
					OutputStreamWriter osw = null;
					BufferedWriter bw = null;
					try {
						bw = new BufferedWriter(new FileWriter(flw));
						out = new FileOutputStream(flw);
						osw = new OutputStreamWriter(out);
						bw = new BufferedWriter(osw);
						bw.append("X,");
						bw.append("Y,");
						bw.append("Z").append("\r");
						for (int i = 0; i < average_data.size(); i++) {// 平均書き込み
							bw.append(String.valueOf(average_data.get(i).getx_accel()) + ",");
							bw.append(String.valueOf(average_data.get(i).gety_accel()) + ",");
							bw.append(String.valueOf(average_data.get(i).getz_accel())).append("\r");
						}
					} catch (Exception e1) {

					} finally {
						if (bw != null) {
							try {
								bw.close();
								bw = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						if (osw != null) {
							try {
								osw.close();
								osw = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						if (out != null) {
							try {
								out.close();
								out = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}

				}
			} else if (e.getSource() == mi_1[2]) {// 分散データ作成
				ta.clear();

				if (lowpass_data != null) { /// ローパス後をとりあえず使う
					// 1秒ごとにした平均保持するArrayListのインスタンスを生成
					variance_data = new ArrayList<Accel>(); // 1秒毎の平均値を入れるためのAccel

					// 一秒間のいくつかのデータを返す
					int data = 0; // 今参照しているデータ
					int nextstart = 0; // 次始める部分
					long startTime = lowpass_data.get(0).gettimestamp(); // １秒間のデータを取得し始める時間

					while (data < lowpass_data.size()) {// データのある限り続ける

						data = nextstart;// １秒間うちの0.5秒以上たった地点のインデックス番号
						nextstart = 0;

						ArrayList<Accel> temporaly_data = new ArrayList<Accel>();// 1秒間のデータを入れる箱
						temporaly_data.add(lowpass_data.get(data));
						long timelag = 0;// 1000ミリ秒カウント

						// １秒間分のデータをtemporaly_dataに保持する
						while (true) {
							if (timelag >= 500 && nextstart == 0) {
								nextstart = data;// 0.5秒を超えた地点のデータを取っておく
							}

							data++;

							if (data >= lowpass_data.size()) {
								break;
							}

							timelag = lowpass_data.get(data).gettimestamp() - startTime;
							// System.out.println(startTime + " "+ timelag + " "
							// + temporaly_data.size());

							if (timelag >= 1000) {
								System.out.println(startTime);
								startTime = startTime + 500; // 500ミリ秒ずらす
								break;
							}

							temporaly_data.add(lowpass_data.get(data));

						}

						for (int i = 0; i < temporaly_data.size(); i++) {
							System.out.println(temporaly_data.get(i).gettimestamp() + " "
									+ temporaly_data.get(i).getx_accel() + " " + temporaly_data.get(i).gety_accel()
									+ " " + temporaly_data.get(i).getz_accel());
						}
						System.out.println("");

						// 1秒分データがなかったら終了
						if (timelag < 1000) {
							break;
						}

						// 平均算出
						float ave_x = 0;
						float ave_y = 0;
						float ave_z = 0;

						for (int i = 0; i < temporaly_data.size(); i++) {
							ave_x += temporaly_data.get(i).getx_accel();
							ave_y += temporaly_data.get(i).gety_accel();
							ave_z += temporaly_data.get(i).getz_accel();
						}
						ave_x = ave_x / temporaly_data.size();
						ave_y = ave_y / temporaly_data.size();
						ave_z = ave_z / temporaly_data.size();

						// 分散算出
						float var_x = 0;
						float var_y = 0;
						float var_z = 0;
						for (int i = 0; i < temporaly_data.size(); i++) {
							var_x += (temporaly_data.get(i).getx_accel() - ave_x)
									* (temporaly_data.get(i).getx_accel() - ave_x);
							var_y += (temporaly_data.get(i).gety_accel() - ave_y)
									* (temporaly_data.get(i).gety_accel() - ave_y);
							var_z += (temporaly_data.get(i).getz_accel() - ave_z)
									* (temporaly_data.get(i).getz_accel() - ave_z);
						}
						var_x = var_x / temporaly_data.size();
						var_y = var_y / temporaly_data.size();
						var_z = var_z / temporaly_data.size();

						// 分散算出
						Accel variance_accel = new Accel();
						ta.appendText(var_x + "," + var_y + "," + var_z + "\n");

						variance_accel.setx_accel(var_x);
						variance_accel.sety_accel(var_y);
						variance_accel.setz_accel(var_z);

						variance_data.add(variance_accel);

					}

					// CSVファイル作成
					FileChooser fc = new FileChooser();// 保存先選ぶ
					filename = filename.replace(".csv", "") + "_variance";
					fc.setInitialFileName(filename);
					fc.setInitialDirectory(new File("./Myfile"));
					FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
					fc.getExtensionFilters().add(extensionFilter);
					File flw = fc.showSaveDialog(new Stage());
					FileOutputStream out = null;
					OutputStreamWriter osw = null;
					BufferedWriter bw = null;
					try {
						bw = new BufferedWriter(new FileWriter(flw));
						out = new FileOutputStream(flw);
						osw = new OutputStreamWriter(out);
						bw = new BufferedWriter(osw);
						bw.append("X,");
						bw.append("Y,");
						bw.append("Z").append("\r");
						for (int i = 0; i < variance_data.size(); i++) {// 平均書き込み
							bw.append(String.valueOf(variance_data.get(i).getx_accel()) + ",");
							bw.append(String.valueOf(variance_data.get(i).gety_accel()) + ",");
							bw.append(String.valueOf(variance_data.get(i).getz_accel())).append("\r");
						}
					} catch (Exception e1) {

					} finally {
						if (bw != null) {
							try {
								bw.close();
								bw = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						if (osw != null) {
							try {
								osw.close();
								osw = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						if (out != null) {
							try {
								out.close();
								out = null;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}

				}
			}
		}
	}
	*/

}
