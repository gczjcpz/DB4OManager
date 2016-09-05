package com.toone.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.db4o.reflect.ReflectField;
import com.db4o.reflect.generic.GenericClass;
import com.toone.ctrl.Ctrl;
import com.toone.ctrl.Util;
import com.toone.view.DataPanel.DataTable.RowNumber;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Cursor;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * 数据表格里的引用类型的单元格
 * 
 * @author laiwj
 *
 */
class RefTypeTableCell extends TableCell<Object, Object> {
	static private Image ico_preview = new Image(RefTypeTableCell.class.getResourceAsStream("ref.png"), 15, 15, false,
			false);
	static private Image ico_detail = new Image(RefTypeTableCell.class.getResourceAsStream("ref.png"), 15, 15, false,
			false);

	private Stage primaryStage;
	private Ctrl ctrl;

	private TabPane tabPane;

	public RefTypeTableCell(Stage primaryStage, Ctrl ctrl, TabPane tabPane) {
		this.primaryStage = primaryStage;
		this.tabPane = tabPane;
		this.ctrl = ctrl;
	}

	@Override
	protected void updateItem(Object value, boolean empty) {
		super.updateItem(value, empty);
		if (!empty) {
			if (value != null) {
				ImageView view = new ImageView(ico_preview);
				view.setOnMouseEntered(e -> view.setCursor(Cursor.HAND));
				view.setOnMouseExited(e -> view.setCursor(null));
				view.setOnMouseClicked(e -> openRefTableView(this.getTableColumn().getText(), value));

				ImageView detail = new ImageView(ico_detail);
				detail.setOnMouseEntered(e -> detail.setCursor(Cursor.HAND));
				detail.setOnMouseExited(e -> detail.setCursor(null));
				detail.setOnMouseClicked(e -> openDetailView(this.getTableColumn().getText(), value));

				HBox buttonBox = new HBox(5, view, detail);
				setGraphic(buttonBox);
				if(Util.isCollection(value)){
					setText("[集合]");
				}else{
					setText(this.getTableColumn().getText());
				}
				setPrefHeight(20);
			} else {
				setGraphic(null);
				setText("null");
			}
		} else {
			setText(null);
			setGraphic(null);
		}

	}

	private void openDetailView(String colTitle, Object value) {
		new ObjectDetailDialog(primaryStage, ctrl, value);
	}

	private void openRefTableView(String colTitle, Object value) {
		if (!tabPane.isVisible()) {
			tabPane.setPrefHeight(200);
			tabPane.setVisible(true);
		}

		Tab tab = tabPane.getTabs().stream().reduce(null, (f, s) -> {
			if (isOpen(f, colTitle)) {
				return f;
			} else if (isOpen(s, colTitle)) {
				return s;
			} else {
				return null;
			}
		});
		if (tab == null) {
			tab = new Tab(colTitle);
			tab.setUserData(colTitle);
			tabPane.getTabs().add(tab);
		}
		ctrl.activate(value, 1);
		if (Util.isCollection(value)) {
			ArrayList<Object> dataList = new ArrayList<>();
			Iterator<?> iterator = ((Iterable<?>) value).iterator();
			while (iterator.hasNext()) {
				Object next = iterator.next();
				dataList.add(next);
			}
			tab.setContent(buildRefTable(Util.getActualType(value), dataList));
		} else {
			List<Object> dataList = new ArrayList<>();
			dataList.add(value);
			tab.setContent(buildRefTable(Util.getActualType(value), dataList));
		}
		tabPane.getSelectionModel().select(tab);
	}

	private boolean isOpen(Tab tab, String value) {
		if (tab == null)
			return false;
		return tab.getText().equals(value);
	}

	private TableView buildRefTable(GenericClass genericClass, List<?> allData) {
		TableView tableView = new TableView();
		try {
			tableView.itemsProperty().setValue(FXCollections.observableArrayList(allData));

			TableColumn oidCol = new TableColumn("OID");
			oidCol.setPrefWidth(100);
			oidCol.setCellValueFactory(cb -> {
				if (cb == null)
					return null;
				Object value = ((CellDataFeatures) cb).getValue();
				Object oid = ctrl.getOID(value);
				return new ReadOnlyObjectWrapper(oid);
			});
			tableView.getColumns().add(oidCol);
			ReflectField[] fields = Util.getAllFields(genericClass);
			for (ReflectField f : fields) {
				TableColumn col = new TableColumn(f.getName());
				col.setPrefWidth(80);
				col.setCellValueFactory(new NullablePropertyValueFactory(f));
				if (!Util.isPrimitiveType(f.getFieldType())) {
					col.setCellFactory(cb -> new RefTableCell());
				}
				tableView.getColumns().add(col);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return tableView;
	}

	private class NullablePropertyValueFactory implements Callback<CellDataFeatures<Object, Object>, ObservableValue<Object>> {

		private ReflectField field;
		public NullablePropertyValueFactory(ReflectField f) {
			this.field = f;
		}

		@Override
		public ObservableValue<Object> call(CellDataFeatures<Object, Object> param) {
        	Object fieldValue = field.get(param.getValue());
        	return fieldValue == null ? null : new ReadOnlyObjectWrapper<Object>(fieldValue);
		}
	}

	/**
	 * 引用表格的表单元格
	 * 
	 * @author laiwj
	 *
	 */
	class RefTableCell extends TableCell<Object, Object> {

		@Override
		protected void updateItem(Object value, boolean empty) {
			super.updateItem(value, empty);
			if (!empty) {
				if (value != null) {
					ImageView detail = new ImageView(ico_detail);
					detail.setOnMouseEntered(e -> detail.setCursor(Cursor.HAND));
					detail.setOnMouseExited(e -> detail.setCursor(null));
					detail.setOnMouseClicked(e -> openDetailView(this.getTableColumn().getText(), value));

					HBox buttonBox = new HBox(detail);
					setGraphic(buttonBox);
					if(Util.isCollection(value)){
						setText("[集合]");
					}else{
						setText(this.getTableColumn().getText());
					}
				} else {
					setGraphic(null);
					setText("null");
				}
			}
		}
	}
}