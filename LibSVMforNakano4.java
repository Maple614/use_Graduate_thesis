package research;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import libsvm.svm_problem;

public class LibSVMforNakano4 extends Application{
	//画面の大きさ
	private int width = 900, height = 500;
	//各種アイテム
	private Label lb1, lb2, lb3, lb4, lb5, lb6, lb7, lb8, lb9;
	private Label sub1,sub2,sub3,sub4,sub5,sub6,sub7,sub8,sub9,sub10,sub11;
	private Button bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9, subbt;
	private TextField ntf, ntf2, ntf3, ntf4, ntf5;
	private TextField subf1,subf2,subf3,subf4,subf5,subf6,subf7,subf8,subf9,subf10,subf11;
	private TextArea ta1, ta2;
	private ChoiceBox<String> cb1, cb2;
	private CheckBox check;
	ArrayList<CheckBox> featureCB = new ArrayList<CheckBox>();
	private Slider sd;
	private VBox featureVB;
	//別ウィンドウ生成時に必要な変数
	private Stage stg;

	//格納する特徴量データのリスト
	private ArrayList<FeatureData> FD = new ArrayList<FeatureData>();
	//使用する特徴量の種類を保存する変数
	ArrayList<String> feature = new ArrayList<String>();
	//使用するラベルを保存する変数
	ArrayList<Integer> labelNum = new ArrayList<Integer>();

	//オプション用変数
	boolean Option = false, Mode = false;
	String subS1 = "3",subS2 = "0",subS3 = "0",subS4 = "1",subS5 = "0.5",subS6 = "0.1",
			subS7 = "100",subS8 = "0.001",subS9 = "1",subS10 = "0",subS11 = "0";

	//学習データ用変数とテストデータ用変数
	String StudyData = "", TestData = "";

	//探索用変数
	//テスト結果を格納する変数
	double result = 0, lap = 0, great = 0;
	//特徴量を格納する変数
	ArrayList<String> LapFeature = new ArrayList<String>(),
					GreatFeature = new ArrayList<String>();
	//探索識別用真偽値
	boolean isSearch = false, isAll = false;
	String resultFileName = "resultFile";

	//参照ファイル, この名前のファイルをプロジェクト直下に用意する
	String filename = "MyFile";

	//SVM用の変数, ライブラリ準拠
	private svm_parameter param;
	private svm_problem prob;
	private svm_model model;
	private int cross_validation;
	private int nr_fold;
	private String error_msg;

	//ここからプログラムスタート. 具体的な動きはstart(Stage stage)から
	public static void main(String args[]) {
		launch(args);
	}

	//主ウィンドウの構築及び表示メソッド
	public void start(Stage stage) throws IOException {
		//各種アイテムの宣言や設定
		lb1 = new Label(" 学習量割合");

		sd = new Slider();
		sd.setMin(0);
		sd.setMax(100);
		sd.setShowTickLabels(true);
		sd.setShowTickMarks(true);
		sd.setMajorTickUnit(20);
		sd.setMinorTickCount(1);
		sd.setSnapToTicks(true);

		lb2 = new Label(" ラベル番号:");

		ntf = new TextField("0");
		ntf.setMaxWidth(45);
		ntf.setOnKeyReleased(new NumberEventHandler());

		lb3 = new Label(" 特徴量名:");

		ntf2 = new TextField("feature");
		ntf2.setMaxWidth(90);

		bt1 = new Button(" データファイル読み込み ");
		bt1.setOnAction(new ReadEventHandler());

		bt2 = new Button(" リセット ");
		bt2.setOnAction(new RisetEventHandler());

		lb4 = new Label(" リサイズ:");

		ntf3 = new TextField("1");
		ntf3.setMaxWidth(50);
		ntf3.setOnKeyReleased(new NumberEventHandler());

		bt3 = new Button(" ReSize ");
		bt3.setOnAction(new DataResizeEventHandler());

		lb5 = new Label(" 登録した特徴量 ");

		lb6 = new Label(" SVM-type:");
		cb1 = new ChoiceBox<String>(FXCollections.observableArrayList("C-SVC","nu-SVC","one-class SVM","epsilon-SVR","nu-SVR"));
		cb1.setValue("C-SVC");

		lb7 = new Label(" kernel_type:");
		cb2 = new ChoiceBox<String>(FXCollections.observableArrayList("linear","polynomial","radial basis function","sigmoid","precomputed kernel"));
		cb2.setValue("radial basis function");

		bt4 = new Button(" オプション ");
		bt4.setOnAction(new OptionEventHandler());

		lb8 = new Label(" モデル名:");

		ntf4 = new TextField("test");
		ntf4.setMaxWidth(90);

		bt5 = new Button(" 機械学習開始 ");
		bt5.setOnAction(new StudyEventHandler());

		lb9 = new Label(" 結果の出力ファイル名:");

		ntf5 = new TextField("output");
		ntf5.setMaxWidth(90);

		bt6 = new Button(" テスト開始 ");
		bt6.setOnAction(new TestEventHandler());

		bt7 = new Button(" 前向き探索 ");
		bt7.setOnAction(new ProspectiveEventHandler());

		bt8 = new Button(" 後ろ向き探索 ");
		bt8.setOnAction(new BackwardsEventHandler());

		bt9 = new Button(" 全 探 索 ");
		bt9.setOnAction(new SearchEventHandler());

		/* デバッグ用. テキストエリアの場所を把握するために使用した.
		ta1 = new TextArea("ta1 in Mainbp_Center\n");
		ta2 = new TextArea("ta2 in bp_Center\n");
		*/
		ta1 = new TextArea("Console for reading DataFile\n");

		ta1.setOnDragOver(event -> {
			if (event.getDragboard().hasFiles()) {
				event.acceptTransferModes(TransferMode.COPY);
			}
		});

		ta1.setOnDragDropped(event -> {
			Dragboard board = event.getDragboard();

			if (board.hasFiles()) {
				board.getFiles().stream().forEach(f -> {

					try {
						//複数ファイルをリスト型に記憶し,後にfor文で各ファイルを読み込


						String[] label_name = { "downstairs", "sitting", "standing", "upstairs", "walking", "cleaning", "typing", "writing" };//ntf
						String[] feature_type = { "average", "variance" };//ntf2
						String[] feature_device = { "MaLab1", "MaLab4", "phone" };//ntf2
						String[] feature_sensor_myo = { "Accel", "Gyro" };
						String[] feature_sensor_phone = { "accel", "gyro" };
						//ArrayList<Integer> ar_label
						//ArrayList<StringBuilder> ar_feature = new ArrayList<StringBuilder>();


						//ファイルが1つでも選択されていたら読み込み開始
						if (f != null) {

							String fileName = f.getName();
							if (f.isDirectory()/* f.canRead() && f.getPath().endsWith(".csv")*/) {
								int ntf_number = -1;


								File[] file_list_label = f.listFiles();
								for (File file_label : file_list_label) {
									String label = file_label.getName();

									for (int i = 0; i < label_name.length; i++) {
										int judge_number = label.indexOf(label_name[i]);
										if (judge_number != -1) {
											ntf_number = i;
											break;
										}
									}



									File[] file_list_type = file_label.listFiles();
									for(File file_type : file_list_type) {
										StringBuilder sb_type = new StringBuilder();

										String type = file_type.getName();

										for (int i = 0; i < feature_type.length; i++) {
											int judge_number = type.indexOf(feature_type[i]);
											if (judge_number != -1) {
												sb_type.append(feature_type[i]);
												sb_type.append("-");
												break;
											}
										}

										File[] file_list_csv = file_type.listFiles();
										for(File file_csv:file_list_csv) {
											StringBuilder sb_device = new StringBuilder();
											StringBuilder sb_sensor = new StringBuilder();

											String csv_name = file_csv.getName();

											for(int i = 0;i<feature_device.length;i++) {
												int judge_number = csv_name.indexOf(feature_device[i]);
												if (judge_number != -1) {
													sb_device.append(feature_device[i]);
													sb_device.append("-");

													break;
												}

											}

											for(int i = 0;i<feature_sensor_myo.length;i++) {
												int judge_number = csv_name.indexOf(feature_sensor_myo[i]);
												if (judge_number != -1) {
													sb_sensor.append(feature_sensor_myo[i]);
													break;
												}

											}
											for(int i = 0;i<feature_sensor_phone.length;i++) {
												int judge_number = csv_name.indexOf(feature_sensor_phone[i]);
												if (judge_number != -1) {
													sb_sensor.append(feature_sensor_phone[i]);
													break;
												}

											}

											StringBuilder sb_plus = new StringBuilder();

											sb_plus.append(sb_type).append(sb_device).append(sb_sensor);

											String ntg2_string = sb_plus.toString();


											ntf.setText(String.valueOf(ntf_number));
											ntf2.setText(ntg2_string);

											ta1.appendText(ntf_number+","+ntg2_string+"\n");

											BufferedReader br = new BufferedReader(new FileReader(file_csv));

											String str = null;
											// 1行目はシカト(X,Y,Zといった文字が挿入されていることを前提としているため)
											br.readLine();

											FeatureData feature_data = null;
											boolean isPrev = false;
											//すでに記憶したことのあるものと特徴量名,ラベルが同じかどうかの判定
											for (int i = 0; i < FD.size(); i++) {
												if (FD.get(i).getType_data().equals(ntg2_string)) {
													if (FD.get(i).getlabel_data() == ntf_number) {
														feature_data = FD.get(i);
														isPrev = true;
													}
												}
											}
											//初めての特徴量,もしくは初めてのラベルの場合に新しいデータの作成
											if (!isPrev) {
												feature_data = new FeatureData();
												feature_data.setType_data(ntg2_string);
												feature_data.setlabel_data(ntf_number);
												if (labelNum.indexOf(feature_data.getlabel_data()) == -1)
													labelNum.add(feature_data.getlabel_data());
											}
											//ファイルからデータを読み込んで記憶させていく
											while ((str = br.readLine()) != null) {
												// 1行分の加速度データの登録
												String[] feature = str.split(",");
												feature_data.setx_data(Float.parseFloat(feature[0]));
												feature_data.sety_data(Float.parseFloat(feature[1]));
												feature_data.setz_data(Float.parseFloat(feature[2]));
											}
											if (!isPrev)
												FD.add(feature_data);
											br.close();

											//コンソール用テキストエリアに書き込み
											ta1.appendText(f.getName() + " is finished.\n");

											ta1.appendText("Type variation = " + FD.size() + " type\n");
											ta1.appendText("feature data size = about " + feature_data.dataX.size() + " datas\n");

											ta1.appendText("All file finish!\n");

											//すでに記憶したことのある特徴量ならスルー,そうでない場合、画面左側に表示するためのチェックボックスの作成
											boolean isThere = false;
											for (int i = 0; i < featureCB.size(); i++) {
												if (featureCB.get(i).getText().equals(ntg2_string + "-X"))
													isThere = true;
											}
											if (!isThere) {
												CheckBox newcb1 = new CheckBox(ntg2_string + "-X");
												CheckBox newcb2 = new CheckBox(ntg2_string + "-Y");
												CheckBox newcb3 = new CheckBox(ntg2_string + "-Z");
												newcb1.setOnAction(new CheckEventHandler());
												newcb2.setOnAction(new CheckEventHandler());
												newcb3.setOnAction(new CheckEventHandler());
												featureVB.getChildren().add(newcb1);
												featureVB.getChildren().add(newcb2);
												featureVB.getChildren().add(newcb3);
												featureCB.add(newcb1);
												featureCB.add(newcb2);
												featureCB.add(newcb3);
											}

										}

									}


								}



							} else {
								ta1.appendText(fileName + " is not directory file\n");
								return;
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				});
				event.setDropCompleted(true);
			} else {
				System.out.println("notfile");
				event.setDropCompleted(false);
			}

		});


		ta1.setEditable(false);

		ta2 = new TextArea("Console for SVM\n");
		ta2.setEditable(false);

		//アイテムを格納するスペースの宣言と設定
		HBox dataHB = new HBox();
		dataHB.setSpacing(10);

		HBox studyHB = new HBox();
		studyHB.setSpacing(10);

		HBox testHB = new HBox();
		testHB.setSpacing(10);

		featureVB = new VBox();
		featureVB.setSpacing(2);

		//各種アイテムの格納
		dataHB.getChildren().add(lb1);
		dataHB.getChildren().add(sd);
		dataHB.getChildren().add(lb2);
		dataHB.getChildren().add(ntf);
		dataHB.getChildren().add(lb3);
		dataHB.getChildren().add(ntf2);
		dataHB.getChildren().add(bt1);
		dataHB.getChildren().add(bt2);
		dataHB.getChildren().add(lb4);
		dataHB.getChildren().add(ntf3);
		dataHB.getChildren().add(bt3);
		studyHB.getChildren().add(lb6);
		studyHB.getChildren().add(cb1);
		studyHB.getChildren().add(lb7);
		studyHB.getChildren().add(cb2);
		studyHB.getChildren().add(bt4);
		studyHB.getChildren().add(lb8);
		studyHB.getChildren().add(ntf4);
		studyHB.getChildren().add(bt5);
		testHB.getChildren().add(lb9);
		testHB.getChildren().add(ntf5);
		testHB.getChildren().add(bt6);
		testHB.getChildren().add(bt7);
		testHB.getChildren().add(bt8);
		testHB.getChildren().add(bt9);
		featureVB.getChildren().add(new Separator());
		featureVB.getChildren().add(lb5);

		ScrollPane sp = new ScrollPane();
		sp.setContent(featureVB);
		BorderPane bp = new BorderPane();
		bp.setTop(studyHB);
		bp.setCenter(ta2);
		bp.setBottom(testHB);
		BorderPane Mainbp = new BorderPane();
		Mainbp.setTop(dataHB);
		Mainbp.setCenter(ta1);
		Mainbp.setLeft(sp);
		Mainbp.setBottom(bp);

		//画面の設定
		stg = stage;
		Scene sc = new Scene(Mainbp, width, height);
		stage.setScene(sc);
		stage.setTitle("LIB-SVM_Application");
		stage.show();
	}

	//↓ ボタン等のアクションイベント関係

	//bt1が押された時に呼び出される(データファイルの読み込み)
	class ReadEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//ラベル番号と特徴量名がきちんと入力されていないと動かない
			if (!ntf.getText().equals("") && !ntf2.getText().equals("")) {
				//ファイルの読み込み部分
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("./"+filename));
				FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("csv file", "*.csv");
				fc.getExtensionFilters().add(extensionFilter);

				try {
					//複数ファイルをリスト型に記憶し,後にfor文で各ファイルを読み込む
					List<File> flr = fc.showOpenMultipleDialog(new Stage());

					//ファイルが1つでも選択されていたら読み込み開始
					if (flr != null) {
						for (File f: flr) {
							BufferedReader br = new BufferedReader(new FileReader(f));

							String str = null;
							// 1行目はシカト(X,Y,Zといった文字が挿入されていることを前提としているため)
							br.readLine();

							FeatureData feature_data = null;
							boolean isPrev = false;
							//すでに記憶したことのあるものと特徴量名,ラベルが同じかどうかの判定
							for (int i = 0; i < FD.size(); i++) {
								if (FD.get(i).getType_data().equals(ntf2.getText())) {
									if (FD.get(i).getlabel_data() == Integer.parseInt(ntf.getText())) {
										feature_data = FD.get(i);
										isPrev = true;
									}
								}
							}
							//初めての特徴量,もしくは初めてのラベルの場合に新しいデータの作成
							if (!isPrev) {
								feature_data = new FeatureData();
								feature_data.setType_data(ntf2.getText());
								feature_data.setlabel_data(Integer.parseInt(ntf.getText()));
								if(labelNum.indexOf(feature_data.getlabel_data()) == -1)labelNum.add(feature_data.getlabel_data());
							}
							//ファイルからデータを読み込んで記憶させていく
							while ((str = br.readLine()) != null) {
								// 1行分の加速度データの登録
								String[] feature = str.split(",");
								feature_data.setx_data(Float.parseFloat(feature[0]));
								feature_data.sety_data(Float.parseFloat(feature[1]));
								feature_data.setz_data(Float.parseFloat(feature[2]));
							}
							if (!isPrev) FD.add(feature_data);
							br.close();

							//コンソール用テキストエリアに書き込み
							ta1.appendText(f.getName()+" is finished.\n");

							ta1.appendText("Type variation = "+FD.size()+" type\n");
							ta1.appendText("feature data size = about "+feature_data.dataX.size()+" datas\n");
						}
						ta1.appendText("All file finish!\n");

						//すでに記憶したことのある特徴量ならスルー,そうでない場合、画面左側に表示するためのチェックボックスの作成
						boolean isThere = false;
						for (int i = 0; i < featureCB.size(); i++) {
							if (featureCB.get(i).getText().equals(ntf2.getText()+"-X")) isThere = true;
						}
						if (!isThere) {
							CheckBox newcb1 = new CheckBox(ntf2.getText()+"-X");
							CheckBox newcb2 = new CheckBox(ntf2.getText()+"-Y");
							CheckBox newcb3 = new CheckBox(ntf2.getText()+"-Z");
							newcb1.setOnAction(new CheckEventHandler());
							newcb2.setOnAction(new CheckEventHandler());
							newcb3.setOnAction(new CheckEventHandler());
							featureVB.getChildren().add(newcb1);
							featureVB.getChildren().add(newcb2);
							featureVB.getChildren().add(newcb3);
							featureCB.add(newcb1);
							featureCB.add(newcb2);
							featureCB.add(newcb3);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	//bt2が押された時に呼び出される(データのリセット)
	class RisetEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//アラートダイアログの作成と表示
			Alert alert = new Alert(AlertType.NONE, "", ButtonType.OK , ButtonType.CANCEL);
			alert.setTitle("データのリセット");
			alert.setContentText("データのリセットを行います。よろしいですか？");
			ButtonType              button  = alert.showAndWait().orElse( ButtonType.CANCEL );

			//OKをクリックした場合にデータをリセットする
			if (button.getButtonData().toString().equals("OK_DONE")) {
				FD.clear();
				feature.clear();
				featureCB.clear();
				labelNum.clear();
				ta1.clear();
				ta2.clear();
				if (featureVB.getChildren().size() >= 3) {
					int size = featureVB.getChildren().size();
					for (int i = 2; i < size; i++) featureVB.getChildren().remove(2);
				}
				Option = false;
			}
		}
	}

	//bt3が押された時に呼び出される(データのリサイズ)
	class DataResizeEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//リサイズ欄に入力してあり、データが空でない場合に動く
			if (!ntf3.getText().equals("") && FD.size() != 0) {
				//リサイズの長さを取得する
				int resizeLength = Integer.parseInt(ntf3.getText()), DataSize = 0;

				//一番数が少ないものに合わせる為に各データの量を比較
				int Min = Integer.MAX_VALUE;
				for (int i = 0; i < FD.size(); i++) {
					if (Min > FD.get(i).dataX.size()) Min = FD.get(i).dataX.size();
				}

				//リサイズの長さがデータの量より少ない場合に動く
				if (Min >= resizeLength) {
					FeatureData original, resize;
					int pointer, counter, lap, jump;

					//全ての特徴量でループ
					for (int i = 0; i < FD.size(); i++) {
						//特徴量データのコピー
						original = FD.get(i);
						//新規変数の定義
						resize = new FeatureData();
						//データサイズと間隔の設定. 間隔はデータを10分割したもの
						DataSize = original.dataX.size();
						jump = DataSize/10;

						//ラベルと特徴量名のコピー
						resize.setlabel_data(original.getlabel_data());
						resize.setType_data(original.getType_data());
						pointer = 0;
						counter = 1;
						lap = 0;

						//リサイズ分書き込むまでループ
						while (resize.dataZ.size() < resizeLength) {
							//データのコピー
							resize.setx_data(original.getx_data(pointer));
							resize.sety_data(original.gety_data(pointer));
							resize.setz_data(original.getz_data(pointer));

							//元データの長さを超えない程度にコピーする場所を飛ばす
							if (counter < 10) {
								pointer += jump;
								counter++;
							}
							else {
								//元データを飛び越える場合、最初に戻る
								lap++;
								pointer = lap;
								counter = 1;
							}
						}

						//登録されている該当データを差し替える
						FD.get(i).dataX.clear();
						FD.get(i).dataY.clear();
						FD.get(i).dataZ.clear();
						for (int j = 0; j < resize.dataX.size(); j++) {
							FD.get(i).setx_data(resize.getx_data(j));
							FD.get(i).sety_data(resize.gety_data(j));
							FD.get(i).setz_data(resize.getz_data(j));
						}
					}
				}
				//コンソール用
				ta1.appendText("Resize!! [ "+ DataSize + " ] → [ " + resizeLength + " ]\n");
			}
		}
	}

	//bt4が押された時に呼び出される(svm学習時のオプション設定画面を作成)
	class OptionEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			// 新しいウインドウを生成
            Stage newStage = new Stage();
            // モーダルウインドウに設定(こうすると元のウィンドウの操作が出来なくなる)
            newStage.initModality(Modality.APPLICATION_MODAL);
            // オーナーを設定
            newStage.initOwner(stg);

            // 新しいウインドウ内に配置するコンテンツを生成
            sub1 = new Label("degree in kernel");
            subf1 = new TextField(subS1);
            subf1.setMaxWidth(45);
            subf1.setOnKeyReleased(new NumberEventHandler());

            sub2 = new Label("gamma in kernel");
            subf2 = new TextField(subS2);
            subf2.setMaxWidth(45);
            subf2.setOnKeyReleased(new NumberEventHandler2());

            sub3 = new Label("coef0 in kernel");
            subf3 = new TextField(subS3);
            subf3.setMaxWidth(45);
            subf3.setOnKeyReleased(new NumberEventHandler2());

            sub4 = new Label("cost of C-SVC / epsilon-SVR / nu-SVR");
            subf4 = new TextField(subS4);
            subf4.setMaxWidth(45);
            subf4.setOnKeyReleased(new NumberEventHandler());

            sub5 = new Label("nu of nu-SVC / one-class-SVM / nu-SVR");
            subf5 = new TextField(subS5);
            subf5.setMaxWidth(45);
            subf5.setOnKeyReleased(new NumberEventHandler2());

            sub6 = new Label("epsilon of epsilon-SVR");
            subf6 = new TextField(subS6);
            subf6.setMaxWidth(45);
            subf6.setOnKeyReleased(new NumberEventHandler2());

            sub7 = new Label("cachesize in MB");
            subf7 = new TextField(subS7);
            subf7.setMaxWidth(45);
            subf7.setOnKeyReleased(new NumberEventHandler2());

            sub8 = new Label("tolerance of termination criterion");
            subf8 = new TextField(subS8);
            subf8.setMaxWidth(45);
            subf8.setOnKeyReleased(new NumberEventHandler2());

            sub9 = new Label("use the shrinking heuristics(0 or 1)");
            subf9 = new TextField(subS9);
            subf9.setMaxWidth(45);
            subf9.setOnKeyReleased(new NumberEventHandler());

            sub10 = new Label("train a SVC or SVR model for probability estimates(0 or 1)");
            subf10 = new TextField(subS10);
            subf10.setMaxWidth(45);
            subf10.setOnKeyReleased(new NumberEventHandler());

            sub11 = new Label("n-fold cross validation mode");
            subf11 = new TextField(subS11);
            subf11.setMaxWidth(45);
            subf11.setOnKeyReleased(new NumberEventHandler());

            check = new CheckBox("quiet mode");
            if (Mode) check.setSelected(true);
            else check.setSelected(false);
            check.setOnAction(new ModeEventHandler());

            subbt = new Button("OK");
            subbt.setOnAction(new OptionOKEventHandler());

            //画面に配置していく
            GridPane gp = new GridPane();
            gp.setPadding(new Insets(10,10,10,10));
            gp.setVgap(5);
            gp.setHgap(5);

            gp.add(sub1, 0, 0);
            gp.add(subf1, 1, 0);
            gp.add(sub2, 0, 1);
            gp.add(subf2, 1, 1);
            gp.add(sub3, 0, 2);
            gp.add(subf3, 1, 2);
            gp.add(sub4, 0, 3);
            gp.add(subf4, 1, 3);
            gp.add(sub5, 0, 4);
            gp.add(subf5, 1, 4);
            gp.add(sub6, 0, 5);
            gp.add(subf6, 1, 5);
            gp.add(sub7, 0, 6);
            gp.add(subf7, 1, 6);
            gp.add(sub8, 0, 7);
            gp.add(subf8, 1, 7);
            gp.add(sub9, 0, 8);
            gp.add(subf9, 1, 8);
            gp.add(sub10, 0, 9);
            gp.add(subf10, 1, 9);
            gp.add(sub11, 0, 10);
            gp.add(subf11, 1, 10);
            gp.add(check, 0, 11);
            gp.add(subbt, 1, 12);

            Scene sc = new Scene(gp, width/2, height * 4/5);
            newStage.setScene(sc);
            newStage.setTitle("SVM オプション");

            // 新しいウインドウを表示
            newStage.show();
		}
	}

	//bt5が押された時に呼び出される(svm学習の開始)
	class StudyEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//探索状態をオフに
			isSearch = false;

			//作成するモデル名の設定がされていないと動かない
			if (!ntf4.getText().equals("") && feature.size() != 0) {
				//svm用パラメータの作成及び設定
				param = new svm_parameter();
				param = ConfigParam(param);

				//学習用メソッドの呼び出し
				svmStudy();
			}
		}
	}

	//bt6が押された時に呼び出される(svmテストの開始)
	class TestEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//探索状態をオフに
			isSearch = false;

			//結果を出力するテキストファイル名の設定がされていないと動かない
			if (!ntf5.getText().equals("")) {
				//svmテストメソッドの呼び出し
				svmTest();
			}
		}
	}

	//bt7が押された時に呼び出される(前向きsvmテスト探索の開始)
	class ProspectiveEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//探索状態をオンに
			isSearch = true;

			//エディターのコンソールに情報を表示しないように設定する
			Mode = true;

			if (!ntf4.getText().equals("") && !ntf5.getText().equals("") && featureVB.getChildren().size() != 2) {
				resultFileName = ntf5.getText();

				//特徴量選択状態のリセット
				for (int i = 0; i < featureCB.size(); i++) featureCB.get(i).setSelected(false);

				//パラメータの初期化
				param = new svm_parameter();

				//探索用変数の初期化
				result = 0;
				lap = 0;
				great = 0;

				//前向き探索用に変数を宣言
				int num = 1;
				ArrayList<String> prevlist = new ArrayList<String>();
				for (int i = 0; i < featureCB.size(); i++) prevlist.add(featureCB.get(i).getText());

				//ここから前向き探索
				while (true) {
					//パラメータの設定
					param = ConfigParam(param);
					//選択特徴量のリセット
					feature.clear();
					lap = 0;
					//探索
					Search(prevlist, num);
					//1周した結果が前の周よりも良ければ上書きして続行, 違うならば終了
					if (great < lap) {
						GreatFeature.clear();
						for (int n = 0; n < LapFeature.size(); n++) GreatFeature.add(LapFeature.get(n));
						great = lap;
						num++;
					}
					else break;
				}
				//選択特徴量のリセット
				feature.clear();
				//一番良かった結果で再度実行する
				for (int n = 0; n < GreatFeature.size(); n++) feature.add(GreatFeature.get(n));

				//情報の表示
				ta2.appendText("Prospective Finish!!　Best feature set is...↓\n");
				for (int n = 0; n < feature.size(); n++) ta2.appendText(feature.get(n)+ " , ");
				ta2.appendText("\n");

				param = ConfigParam(param);
				isSearch = false;
				svmStudy();
				svmTest();
				Mode = false;

				//デバッグ用, 結果の確認
				//System.out.println("Result = "+result);

				//選択特徴量のリセット
				feature.clear();
			}
		}
	}

	//bt8が押された時に呼び出される(後ろ向きsvmテスト探索の開始)
	class BackwardsEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//探索状態をオンに
			isSearch = true;

			//エディターのコンソールに情報を表示しないように設定する
			Mode = true;

			if (!ntf4.getText().equals("") && !ntf5.getText().equals("") && featureVB.getChildren().size() != 2) {
				resultFileName = ntf5.getText();

				//特徴量選択状態のリセット
				for (int i = 0; i < featureCB.size(); i++) featureCB.get(i).setSelected(false);

				//パラメータの初期化
				param = new svm_parameter();

				//探索用変数の初期化
				result = 0;
				lap = 0;
				great = 0;

				//後ろ向き探索用に変数を宣言
				int num = featureCB.size();
				ArrayList<String> backlist = new ArrayList<String>();
				for (int i = 0; i < featureCB.size(); i++) backlist.add(featureCB.get(i).getText());

				//ここから後ろ向き探索
				while (true) {
					//パラメータの設定
					param = ConfigParam(param);
					//選択特徴量のリセット
					feature.clear();
					lap = 0;
					//探索
					Search(backlist, num);
					//1周した結果が前の周よりも良ければ上書きして続行, 違うならば終了
					if (great < lap) {
						GreatFeature.clear();
						for (int n = 0; n < LapFeature.size(); n++) GreatFeature.add(LapFeature.get(n));
						great = lap;
						num--;
					}
					else break;
				}
				//選択特徴量のリセット
				feature.clear();
				//一番良かった結果で再度実行する
				for (int n = 0; n < GreatFeature.size(); n++) feature.add(GreatFeature.get(n));

				//情報の表示
				ta2.appendText("Backwards Finish!!　Best feature set is...↓\n");
				for (int n = 0; n < feature.size(); n++) ta2.appendText(feature.get(n)+ " , ");
				ta2.appendText("\n");

				param = ConfigParam(param);
				isSearch = false;
				svmStudy();
				svmTest();
				Mode = false;

				//デバッグ用, 結果の確認
				//System.out.println("Result = "+result);

				//選択特徴量のリセット
				feature.clear();
			}
		}
	}

	//bt9が押された時に呼び出される(前向き全探索svmテスト探索の開始)
	class SearchEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//探索状態をオンに
			isSearch = true;
			isAll = true;

			//エディターのコンソールに情報を表示しないように設定する
			Mode = true;

			if (!ntf4.getText().equals("") && !ntf5.getText().equals("") && featureVB.getChildren().size() != 2) {
				resultFileName = ntf5.getText();

				//特徴量選択状態のリセット
				for (int i = 0; i < featureCB.size(); i++) featureCB.get(i).setSelected(false);

				//パラメータの初期化
				param = new svm_parameter();

				//探索用変数の初期化
				result = 0;
				lap = 0;
				great = 0;

				//前向き探索用に変数を宣言
				int num = 1;
				ArrayList<String> prevlist = new ArrayList<String>();
				for (int i = 0; i < featureCB.size(); i++) prevlist.add(featureCB.get(i).getText());

				//ここから前向き探索
				while (num <= featureCB.size()) {
					//パラメータの設定
					param = ConfigParam(param);
					//選択特徴量のリセット
					feature.clear();
					lap = 0;
					//探索
					Search(prevlist, num);
					//1周した結果が前の周よりも良ければ上書きして続行
					if (great <= lap) {
						GreatFeature.clear();
						for (int n = 0; n < LapFeature.size(); n++) GreatFeature.add(LapFeature.get(n));
						great = lap;
					}
					num++;
				}
				//選択特徴量のリセット
				feature.clear();
				//一番良かった結果で再度実行する
				for (int n = 0; n < GreatFeature.size(); n++) feature.add(GreatFeature.get(n));

				//情報の表示
				ta2.appendText("All search Finish!!　Best feature set is...↓\n");
				for (int n = 0; n < feature.size(); n++) ta2.appendText(feature.get(n)+ " , ");
				ta2.appendText("\n");

				param = ConfigParam(param);
				isSearch = false;
				svmStudy();
				svmTest();
				isAll = false;
				Mode = false;

				//デバッグ用, 結果の確認
				//System.out.println("Result = "+result);

				//選択特徴量のリセット
				feature.clear();
			}
		}
	}

	//特徴量のCheckBoxがチェックされた時に呼び出される
	class CheckEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//チェックボックスのソースを取得, 名前部分を抜き出す
			StringBuilder sourceName = new StringBuilder();
			sourceName.append(e.getSource().toString());
			String featureName = sourceName.substring(sourceName.indexOf("'")+1, sourceName.lastIndexOf("'"));

			//名前部分を「使用する特徴量のリスト」へ入れたり取り除いたり
			if (feature.indexOf(featureName) == -1) {
				feature.add(featureName);
				ta1.appendText(featureName+" is added\n");
			}
			else {
				feature.remove(featureName);
				ta1.appendText(featureName+" is removed\n");
			}
		}
	}

	//オプション関連のイベントアクション
	//オプションのCheckBoxがチェックされた時に呼び出される
	class ModeEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//チェックが入れられたか外されたかで真偽値を変更
			CheckBox mode = (CheckBox)e.getSource();
			if (mode.isSelected()) Mode = true;
			else Mode = false;
		}
	}

	//オプションのOKボタンが押された時に呼び出される
	class OptionOKEventHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent e) {
			//オプション用変数の書き換えを行う
			Option = true;
			subS1 = subf1.getText();
			subS2 = subf2.getText();
			subS3 = subf3.getText();
			subS4 = subf4.getText();
			subS5 = subf5.getText();
			subS6 = subf6.getText();
			subS7 = subf7.getText();
			subS8 = subf8.getText();
			subS9 = subf9.getText();
			subS10 = subf10.getText();
			subS11 = subf11.getText();

			//画面を閉じる
			((Node)e.getSource()).getScene().getWindow().hide();
		}
	}

	//↓ svm学習・テスト関連のメソッド

	//svm学習メソッド
	void svmStudy() {
		//学習データ及びテストデータの作成メソッドを呼び出す
		CreateData();

		//デバッグ用. 学習データとテストデータの中身を見る為に使用
		//System.out.println("StudyData ↓\n"+StudyData);
		//System.out.println("TestData ↓\n"+TestData);

		//学習データが存在すればsvm学習を開始する
		if (!StudyData.equals("")) {
			//ライブラリ準拠
			try {
				read_problem();
				error_msg = svm.svm_check_parameter(prob,param);

				if(error_msg != null) {
					System.err.print("ERROR: "+error_msg+"\n");
					System.exit(1);
				}

				if(cross_validation != 0) {
					do_cross_validation();
				}
				else {
					model = svm.svm_train(prob,param);
					svm.svm_save_model(ntf4.getText()+".model",model);
				}
				if (!isSearch) ta2.appendText("StudyData is created. ");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else if (!isSearch) ta2.appendText("※StudyData isn't created. ");
		if (!isSearch)  {
			if (!TestData.equals("")) ta2.appendText("TestData is created.\n");
			else ta2.appendText("※TestData isn't created.\n");
		}
	}

	//svmテストメソッド
	void svmTest() {
		//ライブラリ準拠
		int predict_probability = 0;
		try {
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(ntf5.getText()+".txt")));
			svm_model model = svm.svm_load_model(ntf4.getText()+".model");
			if (model == null) {
				System.err.print("can't open model file "+ntf4.getText()+"\n");
				System.exit(1);
			}
			if(predict_probability == 1) {
				if(svm.svm_check_probability_model(model)==0) {
					System.err.print("Model does not support probabiliy estimates\n");
					System.exit(1);
				}
			}
			else {
				if(svm.svm_check_probability_model(model)!=0) {
					ta2.appendText("Model supports probability estimates, but disabled in prediction.\n");
				}
			}

			do_svm_test(TestData,output,model,predict_probability);
			output.close();
		} catch(FileNotFoundException ex) {
			ex.printStackTrace();
		} catch(ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	//探索用メソッド
	void Search(ArrayList<String> list, int number) {
		//再帰的に探索する仕組み
		for (int i = 0; i < list.size(); i++) {
			if (number == 1) {
				feature.add(list.get(i));

				try {
					// 出力先を作成する true=追記, false=上書き
					FileWriter fw = new FileWriter("Myfile/"+resultFileName+"_Accuracy.txt", true);
					PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

					//特徴量の組み合わせ確認
					for (int n = 0; n < feature.size(); n++) {
						System.out.print(feature.get(n)+ " , ");
						pw.write(feature.get(n)+" , ");
					}
					System.out.print("\n");

					//学習とテストの実行
					svmStudy();
					svmTest();

					pw.write("Accuracy = "+result+"\n");

					// ファイルに書き出す
					pw.close();
					fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}


				//デバッグ用, 結果の確認
				//System.out.println("Result = "+result);

				//周回中最良の結果かどうかの判定
				if (result > lap && !isAll) {
					LapFeature.clear();
					for (int n = 0; n < feature.size(); n++) LapFeature.add(feature.get(n));
					lap = result;
				}
				else if (result >= lap && isAll) {
					LapFeature.clear();
					for (int n = 0; n < feature.size(); n++) LapFeature.add(feature.get(n));
					lap = result;
				}
				result = 0;

				feature.remove(feature.size()-1);
			}
			else {
				feature.add(list.get(i));
				ArrayList<String> next = new ArrayList<String>();
				for (int j = i+1; j < list.size(); j++) next.add(list.get(j));
				Search(next, number-1);
				feature.remove(feature.size()-1);
			}
		}
	}

	//svmパラメータ設定メソッド
	svm_parameter ConfigParam(svm_parameter param) {
		svm_print_interface print_func = null;

		switch(cb1.getValue()) {
			case "C-SVC":
				param.svm_type = svm_parameter.C_SVC;
				break;
			case "nu-SVC":
				param.svm_type = svm_parameter.NU_SVC;
				break;
			case "one-class SVM":
				param.svm_type = svm_parameter.ONE_CLASS;
				break;
			case "epsilon-SVR":
				param.svm_type = svm_parameter.EPSILON_SVR;
				break;
			case "nu-SVR":
				param.svm_type = svm_parameter.NU_SVR;
				break;
		}

		switch(cb2.getValue()) {
			case "linear":
				param.kernel_type = svm_parameter.LINEAR;
				break;
			case "polynomial":
				param.kernel_type = svm_parameter.POLY;
				break;
			case "radial basis function":
				param.kernel_type = svm_parameter.RBF;
				break;
			case "sigmoid":
				param.kernel_type = svm_parameter.SIGMOID;
				break;
			case "precomputed kernel":
				param.kernel_type = svm_parameter.PRECOMPUTED;
				break;
		}

		//コンソール用
		if (!isSearch) ta2.appendText("svm = "+param.svm_type+" : kernel = "+param.kernel_type+"\n");

		//オプションが設定されていたらここで変更する
		if (!Option) {
			param.degree = 3;
			param.gamma = 0;	// 1/num_features
			param.coef0 = 0;
			param.nu = 0.5;
			param.cache_size = 100;
			param.C = 1;
			param.eps = 1e-3;
			param.p = 0.1;
			param.shrinking = 1;
			param.probability = 0;
			param.nr_weight = 0;
			param.weight_label = new int[0];
			param.weight = new double[0];
			cross_validation = 0;
		}
		else {
			param.degree = Integer.parseInt(subS1);
			param.gamma = Double.parseDouble(subS2);	// 1/num_features
			param.coef0 = Double.parseDouble(subS3);
			param.nu = Double.parseDouble(subS5);
			param.cache_size = Integer.parseInt(subS7);
			param.C = Integer.parseInt(subS4);
			param.eps = Double.parseDouble(subS8);
			param.p = Double.parseDouble(subS6);
			if (Integer.parseInt(subS9) == 0 || Integer.parseInt(subS9) == 1) {
				param.shrinking = Integer.parseInt(subS9);
			}
			else param.shrinking = 1;
			if (Integer.parseInt(subS10) == 0 || Integer.parseInt(subS10) == 1) {
				param.probability = Integer.parseInt(subS10);
			}
			else param.probability = 0;
			param.nr_weight = 0;
			param.weight_label = new int[0];
			param.weight = new double[0];
			if (Integer.parseInt(subS11) != 0 && Integer.parseInt(subS11) >= 2) {
				cross_validation = 1;
				nr_fold = Integer.parseInt(subS11);
			}
			else cross_validation = 0;
		}
		/*デバッグ用, オプションの中身を見る為に使用
		System.out.println("degree: "+param.degree+"\n"
				+ "gamma: "+param.gamma+"\n"
				+ "coef0: "+param.coef0+"\n"
				+ "nu: "+param.nu+"\n"
				+ "cahe_size: "+param.cache_size+"\n"
				+ "c: "+param.C+"\n"
				+ "eps: "+param.eps+"\n"
				+ "p: "+param.p+"\n"
				+ "shrinking: "+param.shrinking+"\n"
				+ "probability: "+param.probability+"\n"
				+ "nr_weight: "+param.nr_weight+"\n"
				+ "cross_validation: "+cross_validation);
		*/
		if (Mode) print_func = svm_print_null;
		svm.svm_set_print_string_function(print_func);

		return param;
	}

	//学習用データとテストデータの作成メソッド
	void CreateData() {
		//学習割合をスライドバーから取得
		double proportion = sd.getValue() / 100;

		//学習データとテストデータ用文字列の初期化
		StudyData = "";
		TestData = "";
		boolean isLabel = false;

		//使用する特徴量の内,一番数が少ないものに合わせる為に各データの量を比較
		int Min = Integer.MAX_VALUE;
		for (int i = 0; i < feature.size(); i++) {
			for (int j = 0; j < FD.size(); j++) {
				if ((FD.get(j).type+"-X").equals(feature.get(i))
						|| (FD.get(j).type+"-Y").equals(feature.get(i))
						|| (FD.get(j).type+"-Z").equals(feature.get(i))) {
					if (Min > FD.get(j).dataX.size()) Min = FD.get(j).dataX.size();
				}
			}
		}
		//デバッグ用, 使うデータの個数確認
		//System.out.println(Min*labelNum.size());

		//各ラベル毎に作成開始
		for (int n = 0; n < labelNum.size(); n++) {
			int LN = labelNum.get(n);
			int Num = 0;
			//データの個数が一番少ない特徴量に合わせる
			while (Num < Min) {
				int index = 0;
				isLabel = false;

				//記憶したデータを参照
				for (int i = 0; i < FD.size(); i++) {
					String type = FD.get(i).getType_data();

					//各軸(x,y,z)毎に同じ動きを実行

					//使用するデータの特徴量かどうかを識別, かつラベルが重複していないか確認
					if (feature.indexOf(type+"-X") != -1 && FD.get(i).getlabel_data() == LN) {
						//最初だけラベル番号を書き込む
						if (!isLabel) {
							if (Min * proportion > Num || (proportion == 0.0 || proportion == 1)) StudyData += FD.get(i).getlabel_data() + " ";
							if (Min * proportion <= Num || (proportion == 0.0 || proportion == 1)) TestData += FD.get(i).getlabel_data() + " ";
							isLabel = true;
						}
						//学習割合以下なら学習データへ, そうでないならテストデータへ記録. 学習割合が0%か100%の場合は両方へ記述
						if (Min * proportion > Num || (proportion == 0.0 || proportion == 1)) StudyData += index + ":" + FD.get(i).getx_data(Num) + " ";
						if (Min * proportion <= Num || (proportion == 0.0 || proportion == 1)) TestData += index + ":" + FD.get(i).getx_data(Num) + " ";
						index++;
					}
					if (feature.indexOf(type+"-Y") != -1 && FD.get(i).getlabel_data() == LN) {
						if (!isLabel) {
							if (Min * proportion > Num || (proportion == 0.0 || proportion == 1)) StudyData += FD.get(i).getlabel_data() + " ";
							if (Min * proportion <= Num || (proportion == 0.0 || proportion == 1)) TestData += FD.get(i).getlabel_data() + " ";
							isLabel = true;
						}
						if (Min * proportion > Num || (proportion == 0.0 || proportion == 1)) StudyData += index + ":" + FD.get(i).gety_data(Num) + " ";
						if (Min * proportion <= Num || (proportion == 0.0 || proportion == 1)) TestData += index + ":" + FD.get(i).gety_data(Num) + " ";
						index++;
					}
					if (feature.indexOf(type+"-Z") != -1 && FD.get(i).getlabel_data() == LN) {
						if (!isLabel) {
							if (Min * proportion > Num || (proportion == 0.0 || proportion == 1)) StudyData += FD.get(i).getlabel_data() + " ";
							if (Min * proportion <= Num || (proportion == 0.0 || proportion == 1)) TestData += FD.get(i).getlabel_data() + " ";
							isLabel = true;
						}
						if (Min * proportion > Num || (proportion == 0.0 || proportion == 1)) StudyData += index + ":" + FD.get(i).getz_data(Num) + " ";
						if (Min * proportion <= Num || (proportion == 0.0 || proportion == 1)) TestData += index + ":" + FD.get(i).getz_data(Num) + " ";
						index++;
					}
				}
				if (Min * proportion > Num || (proportion == 0.0|| proportion == 1)) StudyData += "\n";
				if (Min * proportion <= Num || (proportion == 0.0 || proportion == 1)) TestData += "\n";
				Num++;
			}
		}
	}

	//学習データを読み込む
	private void read_problem() throws IOException {
		//ライブラリ準拠
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		int max_index = 0;

		//学習データ用文字列を1行ずつ分解して実行
		String[] lines = StudyData.split("\n");

		for (int i = 0; i < lines.length; i++) {
			StringTokenizer st = new StringTokenizer(lines[i]," \t\n\r\f:");

			vy.addElement(atof(st.nextToken()));
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			if(m>0) max_index = Math.max(max_index, x[m-1].index);
			vx.addElement(x);
		}

		prob = new svm_problem();
		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for(int i=0;i<prob.l;i++)
			prob.x[i] = vx.elementAt(i);
		prob.y = new double[prob.l];
		for(int i=0;i<prob.l;i++)
			prob.y[i] = vy.elementAt(i);

		if(param.gamma == 0 && max_index > 0) param.gamma = 1.0/max_index;

		if(param.kernel_type == svm_parameter.PRECOMPUTED)
			for(int i=0;i<prob.l;i++) {
				if (prob.x[i][0].index != 0) {
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					System.exit(1);
				}
				if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index) {
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					System.exit(1);
				}
			}
	}

	//svm学習時に使用するメソッド(ライブラリ準拠)
	private void do_cross_validation() {
		int i;
		int total_correct = 0;
		double total_error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		double[] target = new double[prob.l];

		svm.svm_cross_validation(prob,param,nr_fold,target);
		if(param.svm_type == svm_parameter.EPSILON_SVR || param.svm_type == svm_parameter.NU_SVR) {
			for(i=0;i<prob.l;i++) {
				double y = prob.y[i];
				double v = target[i];
				total_error += (v-y)*(v-y);
				sumv += v;
				sumy += y;
				sumvv += v*v;
				sumyy += y*y;
				sumvy += v*y;
			}
			ta2.appendText("Cross Validation Mean squared error = "+total_error/prob.l+"\n");
			ta2.appendText("Cross Validation Squared correlation coefficient = "+
							((prob.l*sumvy-sumv*sumy)*(prob.l*sumvy-sumv*sumy))/
							((prob.l*sumvv-sumv*sumv)*(prob.l*sumyy-sumy*sumy))+"\n");
		}
		else {
			for(i=0;i<prob.l;i++)
				if(target[i] == prob.y[i])
					++total_correct;
			ta2.appendText("Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
		}
	}

	//svmのテストを行うメソッド
	private void do_svm_test(String input, DataOutputStream output, svm_model model, int predict_probability) throws IOException{
		int correct = 0;
		int total = 0;
		double error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

		int svm_type=svm.svm_get_svm_type(model);
		//テストデータ用文字列を1行ずつに分解して実行
		String[] line = input.split("\n");

		for (int i = 0; i < line.length; i++) {
			StringTokenizer st = new StringTokenizer(line[i]," \t\n\r\f:");

			double target = atof(st.nextToken());
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}

			double v = svm.svm_predict(model,x);
			output.writeBytes(v+"\n");

			if(v == target) ++correct;
			error += (v-target)*(v-target);
			sumv += v;
			sumy += target;
			sumvv += v*v;
			sumyy += target*target;
			sumvy += v*target;
			++total;
		}
		if(svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
			ta2.appendText("Mean squared error = "+error/total+" (regression)\n");
			ta2.appendText("Squared correlation coefficient = "+
				 ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
				 ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
				 " (regression)\n");
		}
		else  {
			String message = " Accuracy = "+(double)correct/total*100 + "% ("+correct+"/"+total+") (classification)\n";
			if (!isSearch) ta2.appendText(message);
			output.writeBytes(message);
			result = (double)correct/total*100;
		}
	}

	//↓ ちょっとしたメソッド集...

	//テキストフィールド内のデータ制限(int用)
	class NumberEventHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent event) {
			// TODO Auto-generated method stub
			TextField tmp = (TextField) event.getSource();
			//正規表現を使用して半角数字以外を弾くように設定
			if (!tmp.getText().matches("\\d*")) tmp.setText("");
		}
	}
	//テキストフィールド内のデータ制限(double用)
	class NumberEventHandler2 implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent event) {
			// TODO Auto-generated method stub
			TextField tmp = (TextField) event.getSource();
			//正規表現を使用して半角数字と小数点以外を弾くように設定
			if (!(tmp.getText().matches("\\d*") || tmp.getText().matches("\\d{1,}\\.\\d*"))) tmp.setText("");
		}
	}
	//情報非表示用に必要なメソッド
	private static svm_print_interface svm_print_null = new svm_print_interface(){
		public void print(String s) {}
	};
	//StringをDoubleに変換するメソッド
	private static double atof(String s) {
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			System.err.print("NaN or Infinity in input\n");
			System.exit(1);
		}
		return(d);
	}
	//Stringをintに変換するメソッド
	private static int atoi(String s) {
		return Integer.parseInt(s);
	}
}
