package com.toone.view;

import java.io.File;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
		
		HBox hbox_top = new HBox();
		VBox vbox_center = new VBox();
		vbox_center.setStyle("-fx-background-color: #778888;");
		
		Label l_top1 = new Label("DB4");
		Label l_top2 = new Label("Object");
		l_top2.setTextFill(Paint.valueOf("#00FF00"));
		l_top1.setFont(Font.font("Cambria", FontWeight.NORMAL, 30));
		l_top2.setFont(Font.font("Cambria", FontWeight.NORMAL, 30));
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 50, 25, 25));
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("打开");
		
		Label scenetitle = new Label("New Connection");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
		scenetitle.setTextFill(Paint.valueOf("#FFFFFF"));
		scenetitle.setPrefWidth(110);
		grid.add(scenetitle, 0, 0 );
//		grid.setGridLinesVisible(true);
		Label l_file = new Label("File:");
		l_file.setTranslateX(60);
		grid.add(l_file, 0, 1);
		
		TextField path = new TextField();
		grid.add(path, 1, 1);
		
		Button btnB = new Button("Browse");
		btnB.setPrefSize(50, 20);
		grid.add(btnB, 2, 1);
		
		Button btnO = new Button("OPen");
		btnO.setPrefSize(150, 20);
		grid.add(btnO, 1, 3);
		
		btnB.setOnAction(event -> {
				path.setText("");
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
						} catch (Exception e) {
							e.printStackTrace();
						};
					}
				}else{
					ShowTipsFrame.showDialog(primaryStage, "打开出错");
				}
			}
		);
		
		hbox_top.getChildren().addAll(l_top1,l_top2);
		vbox_center.getChildren().add(grid);
		border.setCenter(vbox_center);
		border.setTop(hbox_top);
		Scene scene = new Scene(border, 400, 250);
		primaryStage.setTitle("ObjectManager");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

}
