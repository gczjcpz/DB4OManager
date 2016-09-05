package com.toone.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.db4o.reflect.ReflectClass;
import com.db4o.reflect.ReflectField;
import com.toone.ctrl.Ctrl;
import com.toone.ctrl.Util;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * 数据表面板
 * 
 * @author laiwj
 *
 */
public class DataPanel extends VBox {

	private Label infoPanel;
	private DataTable dataTable;
	private TabPane tabPane;

	public DataPanel(Stage primaryStage, SplitPane splitPane, Ctrl ctrl) {
		infoPanel = new Label();
		tabPane = new TabPane();
		tabPane.setVisible(false);
		tabPane.setPrefHeight(0);
		dataTable = new DataTable(primaryStage, splitPane, ctrl, tabPane);
		this.getChildren().addAll(infoPanel, dataTable, tabPane);
		VBox.setVgrow(dataTable, Priority.ALWAYS);
	}

	public void showQueryDatas(ReflectClass clazz, List<Object> allDatas, String queryTime) {
		tabPane.getTabs().clear();
		tabPane.setVisible(false);
		tabPane.setPrefHeight(0);
		dataTable.refreshTableDatas(clazz, allDatas);
		infoPanel.setText("query " + clazz.getName() + "[" + allDatas.size() + "条] in " + queryTime);
	}

	class DataTable extends TableView<Object> {

		private Stage primaryStage;
		private Ctrl ctrl;
		private TabPane tabPane;
		private SplitPane splitPane;

		public DataTable(Stage primaryStage, SplitPane splitPane, Ctrl ctrl, TabPane tabPane) {
			this.primaryStage = primaryStage;
			this.ctrl = ctrl;
			this.tabPane = tabPane;
			this.splitPane = splitPane;
			MenuItem menuItem = new MenuItem("查看本条对象详细属性");
			menuItem.setOnAction(e -> {
				Object selectedItem = this.getSelectionModel().getSelectedItem();
				if (selectedItem != null) {
					new ObjectDetailDialog(primaryStage, ctrl, ((RowNumber) selectedItem).getObject());
				}
			});
			setContextMenu(new ContextMenu(menuItem));
		}

		/**
		 * 上一次查询出来的所有数据
		 */
		private List<Object> allDatas;

		/**
		 * 筛选条件
		 */
		private ObjectBinding<Predicate<Object>> totalPredicate = new ObjectBinding<Predicate<Object>>() {
			@Override
			protected Predicate<Object> computeValue() {
				ObservableList<Predicate<Object>> dependencies = (ObservableList<Predicate<Object>>) getDependencies();
				if (dependencies == null || dependencies.isEmpty()) {
					return e -> true;
				} else {
					return e -> {
						for (Predicate<Object> item : dependencies) {
							boolean test = item.test(e);
							if (!test)
								return false;
						}
						return true;
					};
				}
			}
		};

		public void refreshTableDatas(ReflectClass clazz, List<Object> allDatas) {
			this.allDatas = allDatas;
			this.totalPredicate = null;
			this.getColumns().clear();
			this.getSelectionModel().clearSelection();
			ObservableList list = FXCollections.observableArrayList();
			for (int i = 0, size = allDatas.size(); i < size; ++i) {
				list.add(new RowNumber(i + 1, allDatas.get(i)));
			}
			this.itemsProperty().setValue(list);
			TableColumn rowNumberCol = new TableColumn("");
			rowNumberCol.setSortable(false);
			rowNumberCol.setCellValueFactory(new PropertyValueFactory("rowNumber"));
			this.getColumns().add(rowNumberCol);
			ReflectField[] fields = Util.getAllFields(clazz);
			List<ObjectProperty<Predicate<Object>>> predicates = new ArrayList<>();
			
			for (int i = 0; i < fields.length; ++i) {
				ReflectField f = fields[i];
				TableColumn col = new TableColumn();
				col.setPrefWidth(80);
				col.setPrefWidth(100);
				col.setCellValueFactory(new RowNumberPropertyValueFactory(f));
				if (Util.isPrimitiveType(f.getFieldType())) {
					col.setCellFactory(cb -> new PrimitiveTypeTableCell());
					FilterColumnNode filterColNode = new FilterColumnNode(f);
					col.setGraphic(filterColNode);
					predicates.add(filterColNode.predicateProperty());
				} else {
					col.setCellFactory(cb -> new RefTypeTableCell(primaryStage, ctrl, tabPane));
					col.setText(f.getName());
				}
				col.setSortable(false);
				this.getColumns().add(col);
			}
			this.totalPredicate = new PredicateBinding(predicates);
			this.totalPredicate.addListener((value, o, n) -> filterTableDatas(allDatas, n));
			this.refresh();
			this.scrollToColumnIndex(0);
		}

		private void filterTableDatas(List<Object> allDatas, Predicate<Object> n) {
			List<Object> filterDatas = allDatas.stream().filter(n).collect(Collectors.toList());
			ObservableList list = FXCollections.observableArrayList();
			for (int i = 0, size = allDatas.size(); i < size; ++i) {
				list.add(new RowNumber(i + 1, allDatas.get(i)));
			}
			this.setItems(list);
			this.refresh();
		}

		private class PrimitiveTypeTableCell extends TableCell<Object, Object> {
			@Override
			protected void updateItem(Object item, boolean empty) {
				super.updateItem(item, empty);
				if (!empty) {
					if (item != null) {
						setText(String.valueOf(item));
						setTextOverrun(OverrunStyle.ELLIPSIS);
						setGraphic(null);
						setPrefHeight(20);
					} else {
						setText(null);
						setGraphic(null);
					}
				} else {
					setText(null);
					setGraphic(null);
				}
			}
		}

		public class RowNumber {

			private long rowNumber;
			private Object object;

			public RowNumber(long rowNumber, Object object) {
				this.rowNumber = rowNumber;
				this.object = object;
			}

			public long getRowNumber() {
				return rowNumber;
			}

			public void setRowNumber(long rowNumber) {
				this.rowNumber = rowNumber;
			}

			public Object getObject() {
				return object;
			}

			public void setObject(Object object) {
				this.object = object;
			}
		}

		private class RowNumberPropertyValueFactory implements Callback<CellDataFeatures<Object, Object>, ObservableValue<Object>> {

			private ReflectField field;

			public RowNumberPropertyValueFactory(ReflectField f) {
				this.field = f;
			}

			@Override
			public ObservableValue<Object> call(CellDataFeatures<Object, Object> param) {
				Object object = ((RowNumber) param.getValue()).getObject();
				Object fieldValue = field.get(object);
				return fieldValue == null ? null : new ReadOnlyObjectWrapper<Object>(fieldValue);
			}
		}

		/**
		 * 带过滤器的表头
		 * 
		 * @author laiwj
		 *
		 */
		private class FilterColumnNode extends BorderPane {

			private FilterPopup filterPopup;
			
			private ObjectProperty<Predicate<Object>> predicate = new SimpleObjectProperty<>(e -> true);			
			FilterColumnNode(ReflectField field) {
				Label label = new Label(field.getName());
				ImageView popupNode = new ImageView(new Image(getClass().getResourceAsStream("down_triangle.png"), 8, 8, false, false));
				popupNode.setVisible(false);
				this.setCenter(label);
				StackPane value = new StackPane(popupNode);
				this.setRight(value);
				HBox.setHgrow(label, Priority.ALWAYS);

				this.setOnMouseEntered(e -> popupNode.setVisible(true));
				this.setOnMouseExited(e -> {
					if (filterPopup == null || !filterPopup.isShowing()) {
						popupNode.setVisible(false);
					}
				});
				popupNode.setOnMouseEntered(e -> popupNode.setCursor(Cursor.HAND));
				popupNode.setOnMouseExited(e -> popupNode.setCursor(Cursor.DEFAULT));
				popupNode.setOnMouseClicked(e -> {
					if (filterPopup == null || !filterPopup.isShowing()) {
						double y = primaryStage.getY() + primaryStage.getScene().getY() + splitPane.getLayoutY() + dataTable.getLayoutY() + this.getHeight();
						filterPopup = new FilterPopup(primaryStage, popupNode, e.getScreenX(), y, field, predicate);
						filterPopup.setOnHidden(event -> popupNode.setVisible(false));
					}
				});
			}

			public Predicate<Object> getPredicate() {
				return predicate.get();
			}

			public void setPredicate(Predicate<Object> predicate) {
				this.predicate.set(predicate);
			}
			
			public ObjectProperty<Predicate<Object>> predicateProperty(){
				return predicate;
			}
		}
		
		private class PredicateBinding extends ObjectBinding<Predicate<Object>>{

			private List<ObjectProperty<Predicate<Object>>> ps;
			PredicateBinding(List<ObjectProperty<Predicate<Object>>> ps){
				this.ps = ps;
				ObjectProperty<Predicate<Object>>[] arr = new ObjectProperty[ps.size()];
				ps.toArray(arr);
				super.bind(arr);
			}
			
			@Override
			protected Predicate<Object> computeValue() {
				if (ps == null || ps.isEmpty()) {
					return e -> true;
				} else {
					return e -> {
						for (ObjectProperty<Predicate<Object>> item : ps) {
							System.out.println(item);
							System.out.println(e);
							boolean test = item.getValue().test(e);
							if (!test)
								return false;
						}
						return true;
					};
				}
			}
			
		}
	}
}
