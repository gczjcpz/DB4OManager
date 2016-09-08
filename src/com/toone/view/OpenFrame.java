package com.toone.view;

import java.io.File;

import com.toone.tools.readWriteConfig;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * DB4OManager打开界面
 */

public class OpenFrame extends Application{

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}
	
	public void start(Stage primaryStage){
		BorderPane border = new BorderPane();
		BorderPane border_child = new BorderPane();
		
		HBox hbox_top = new HBox();
		HBox hbox_center = new HBox();
		hbox_center.setStyle("-fx-background-color: #778888;");
		HBox hbox_listView = new HBox();
		hbox_listView.setAlignment(Pos.CENTER);
		hbox_listView.setPadding(new Insets(25, 25, 0, 25));
		hbox_listView.setStyle("-fx-background-color: #778888;");
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(0, 5, 25, 25));
		
		Label l_top1 = new Label("DB4");
		Label l_top2 = new Label("Object");
		l_top2.setTextFill(Paint.valueOf("#00FF00"));
		l_top1.setFont(Font.font("Cambria", FontWeight.NORMAL, 30));
		l_top2.setFont(Font.font("Cambria", FontWeight.NORMAL, 30));
		
		ListView<String> listView = new ListView<>();
		listView.setPrefSize(250, 80);
		ObservableList<String> items = FXCollections.observableArrayList();
		items.addAll(new readWriteConfig().readCofig());
//		ObservableList<String> items = FXCollections.observableArrayList("C:\\Users\\housh\\Desktop\\da8aa896_3631_4c8a_9c4b_074b4dc22647.ecd","B","C","Djknjkhkhkh");
		listView.setItems(items);
		
		Label scenetitle = new Label("New Connection");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
		scenetitle.setTextFill(Paint.valueOf("#FFFFFF"));
		scenetitle.setPrefWidth(110);
		grid.add(scenetitle, 0, 0 );
//		grid.setGridLinesVisible(true);
		
		Label l_file = new Label("File:");
		l_file.setTranslateX(80);
		grid.add(l_file, 0, 1);
		
		TextField path = new TextField();
		grid.add(path, 1, 1);
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("打开");
		
		Button btnB = new Button("Browse");
		btnB.setPrefSize(60, 20);
		grid.add(btnB, 2, 1);
		
		Button btnO = new Button("Open");
		btnO.setPrefSize(150, 20);
		grid.add(btnO, 1, 3);
		
		listView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String old_val,
				String new_val) -> {
					path.setText(new_val);
					});
		
		btnB.setOnAction(event -> {
				File file = fileChooser.showOpenDialog(primaryStage);
				if(file!=null){
					String absolutePath = file.getAbsolutePath();
					path.setText(absolutePath);		
				}
			}
		);
		
		btnO.setOnAction(event -> {
				String DBURL = path.getText();
				if(!"".equals(DBURL)){
					if(new File(DBURL).exists()){
						try {
							new MainFrame(DBURL).start(new Stage());
							new readWriteConfig().writeCofig(DBURL);
						} catch (Exception e) {
							ShowTipsFrame.showDialog(primaryStage, e.getMessage());
							e.printStackTrace();
						};
					}else{
						ShowTipsFrame.showDialog(primaryStage, "文件不存在！");
					}
				}else{
					ShowTipsFrame.showDialog(primaryStage, "null");
				}
			}
		);
		
		hbox_top.getChildren().addAll(l_top1,l_top2);
		hbox_center.getChildren().add(grid);
		hbox_listView.getChildren().add(listView);
		border.setCenter(border_child);
		border.setTop(hbox_top);
		border_child.setCenter(hbox_center);
		border_child.setTop(hbox_listView);
		Scene scene = new Scene(border, 460, 300);
//		scene.getStylesheets().add(e);
		primaryStage.setTitle("ObjectManager");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

}
