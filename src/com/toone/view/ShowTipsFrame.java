package com.toone.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ShowTipsFrame {

	public static void showDialog(Stage primaryStage, String tips) {
        Stage messageDialog = new Stage();
        messageDialog.setTitle("提示");
        messageDialog.setResizable(false);
        messageDialog.initOwner(primaryStage);
        messageDialog.initModality(Modality.APPLICATION_MODAL);
        
        Button confirm = new Button("确定");
        confirm.setPrefSize(50, 25);
        confirm.setOnAction(e -> messageDialog.close());
        
        HBox bottom = new HBox(confirm);
        bottom.setAlignment(Pos.CENTER);
        
        Text tip = new Text(tips);
        
        HBox center = new HBox(tip);
        center.setAlignment(Pos.CENTER); 
        
        BorderPane border = new BorderPane();
        
        border.setCenter(center);
        border.setBottom(bottom);
        border.setPadding(new Insets(10, 5, 10, 5));
        messageDialog.setScene(new Scene(border,200,100));
        messageDialog.show();
    }
	
}
