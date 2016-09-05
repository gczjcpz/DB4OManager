package com.toone.view;

import java.util.function.Predicate;

import com.db4o.reflect.ReflectField;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

public class FilterPopup extends Popup {

	FilterPopup(Window primaryStage, Node popupNode, double x, double y, ReflectField field, ObjectProperty<Predicate<Object>> predicate) {
		this.setAutoHide(true);
		this.setHideOnEscape(true);
		VBox popupRootPane = new VBox();
		popupRootPane.setPrefSize(100, 100);
		popupRootPane.setStyle("-fx-background-color: #ffffff;-fx-border-color:#039ed3");
		Label label = new Label("i am popup");
		Button confirm = new Button("确定");
		confirm.setOnAction(e -> {
			predicate.setValue(item -> {
				Object fieldValue = field.get(item);
				return true;
			});
		});
		popupRootPane.getChildren().addAll(label, confirm);
		this.getContent().add(popupRootPane);
		this.show(popupNode, x, y);
	}

}
