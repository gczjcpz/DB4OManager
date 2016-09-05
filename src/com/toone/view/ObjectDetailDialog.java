package com.toone.view;

import java.util.Iterator;

import com.db4o.reflect.ReflectField;
import com.db4o.reflect.generic.GenericClass;
import com.toone.ctrl.Ctrl;
import com.toone.ctrl.Util;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ObjectDetailDialog {

    private static final TreeItem<NameValuePair> DETAIL_TREE_EMPTY_NODE = new TreeItem<>(new NameValuePair("detailTreeNode[empty]", null));

    private Ctrl ctrl;

    public ObjectDetailDialog(Stage primaryStage, Ctrl ctrl, Object value) {
        this.ctrl = ctrl;
        Stage dialog = new Stage();
        dialog.setTitle("对象详细属性值");
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.NONE);
        TreeView<NameValuePair> tree = buildTree(value);
		StackPane root = new StackPane(tree);
        Scene scene = new Scene(root, 400, 600);
        dialog.setScene(scene);
        dialog.setOnCloseRequest(e -> tree.getRoot().getChildren().clear());
        dialog.show();
    }

    private TreeView<NameValuePair> buildTree(Object obj) {
        TreeItem<NameValuePair> root = new TreeItem<>(new NameValuePair("root", null));
        TreeView<NameValuePair> attrTree = new TreeView<NameValuePair>(root);

        ctrl.activate(obj, 1);
        if (obj instanceof Iterable) {
            Iterator<?> iterator = ((Iterable<?>) obj).iterator();
            while (iterator.hasNext()) {
            	GenericClass actualType = Util.getActualType(obj);
                Object objectItem = iterator.next();
                TreeItem<NameValuePair> objectNode = new TreeItem<>(new NameValuePair(actualType.getName(), objectItem));
                objectNode.getChildren().add(DETAIL_TREE_EMPTY_NODE);
                objectNode.expandedProperty().addListener((item, o, n) -> doExpand(objectNode, item, o, n));
                root.getChildren().add(objectNode);
            }
            root.setExpanded(true);
        } else {
            GenericClass actualType = Util.getActualType(obj);
            TreeItem<NameValuePair> objectNode = new TreeItem<>(new NameValuePair(actualType.getName(), obj));
            ReflectField[] allFields = Util.getAllFields(actualType);
            for (ReflectField f : allFields) {
                Object fieldValue = f.get(obj);
                TreeItem<NameValuePair> fieldNode = new TreeItem<>(new NameValuePair(f.getName(), fieldValue));
                objectNode.getChildren().add(fieldNode);
                if(Util.isCollection(fieldValue)){
                	ctrl.activate(fieldValue, 1);
                }
                if (fieldValue != null && !Util.isEmptyCollection(fieldValue) && !Util.isPrimitiveType(f.getFieldType())) {
                    fieldNode.getChildren().add(DETAIL_TREE_EMPTY_NODE);
                    fieldNode.expandedProperty().addListener((item, o, n) -> doExpand(fieldNode, item, o, n));
                }
            }
            objectNode.setExpanded(true);
            root.getChildren().add(objectNode);
        }
        attrTree.setShowRoot(false);
        attrTree.setCellFactory(cb -> new AttrTreeCell(ctrl));
        return attrTree;
    }

    private void doExpand(TreeItem<NameValuePair> fieldNode, ObservableValue<? extends Boolean> item, Boolean o, Boolean n) {
        if (n) {
            ObservableList<TreeItem<NameValuePair>> children = fieldNode.getChildren();
            if (!children.isEmpty()) {
                TreeItem<NameValuePair> treeItem = children.get(0);
                if ("detailTreeNode[empty]".equals(treeItem.getValue().getName())) {
                    fieldNode.getChildren().clear();
                    Object obj = fieldNode.getValue().getValue();
                    ctrl.activate(obj, 1);
                    if (Util.isCollection(obj)) {
                        Iterator<?> iterator = ((Iterable<?>) obj).iterator();
                        while (iterator.hasNext()) {
                            Object objectItem = iterator.next();
                            GenericClass actualType = Util.getActualType(objectItem);
                            TreeItem<NameValuePair> objectNode = new TreeItem<>(new NameValuePair(actualType.getName(), objectItem));
                            objectNode.getChildren().add(DETAIL_TREE_EMPTY_NODE);
                            objectNode.expandedProperty().addListener((item1, o1, n1) -> doExpand(objectNode, item1, o1, n1));
                            fieldNode.getChildren().add(objectNode);
                        }
                    } else {
                    	GenericClass actualType = Util.getActualType(obj);
                    	ReflectField[] allFields = Util.getAllFields(actualType);
                        for (ReflectField f : allFields) {
                            Object fieldValue = f.get(obj);
                            TreeItem<NameValuePair> subFieldNode = new TreeItem<>(new NameValuePair(f.getName(), fieldValue));
                            fieldNode.getChildren().add(subFieldNode);
                            if(Util.isCollection(fieldValue)){
                            	ctrl.activate(fieldValue, 1);
                            }
                            if (fieldValue != null && !Util.isEmptyCollection(fieldValue) && !Util.isPrimitiveType(f.getFieldType())) {
                                subFieldNode.getChildren().add(DETAIL_TREE_EMPTY_NODE);
                                subFieldNode.expandedProperty().addListener((item1, o1, n1) -> doExpand(subFieldNode, item1, o1, n1));
                            }
                        }
                    }
                }
            }
        }
    }

    static class AttrTreeCell extends TreeCell<NameValuePair> {

        private Ctrl ctrl;

        public AttrTreeCell(Ctrl ctrl) {
            this.ctrl = ctrl;
        }

        @Override
        protected void updateItem(NameValuePair item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                if (item != null) {
                    setContentDisplay(ContentDisplay.RIGHT);
                    if (item.getValue() == null) {
                        setText(item.getName());
                        setGraphic(null);
                        Label textField = new Label("");
                        textField.setPrefHeight(20);
                        textField.setTextOverrun(OverrunStyle.ELLIPSIS);
                        setGraphic(textField);
                        return;
                    }
                    if (Util.isPrimitiveType(item.getValue())) {
                        setText(item.getName() + ":");
                        Label label = new Label(String.valueOf(item.getValue()));
                        label.setTextOverrun(OverrunStyle.ELLIPSIS);
                        setGraphic(label);
                        label.setOnMouseClicked(e -> switchEditMode(label, e));
                    } else if (item.getValue() instanceof Iterable) {
                        setText(item.getName() + "[集合(共" + Util.getSize((Iterable<?>) item.getValue()) + "条)]");
                        setGraphic(null);
                    } else {
                        long oid = ctrl.getOID(item.getValue());
                        String oidText = "";
                        if (oid != 0) {
                            oidText = "[引用(OID:" + oid + ")]";
                        }
                        setText(item.getName() + oidText);
                        setGraphic(null);
                    }
                } else {
                    setText(null);
                    setGraphic(null);
                }
            } else {
                setText(null);
                setGraphic(null);
            }
        }

        private void switchEditMode(Label label, MouseEvent e) {
            if (e.getClickCount() > 1) {
                TextField textField = new TextField(label.getText());
                textField.focusedProperty().addListener((i, o, n) -> {
                    if (!n) {
                        Label newLabel = new Label(textField.getText());
                        newLabel.setOnMouseClicked(e1 -> switchEditMode(newLabel, e1));
                        AttrTreeCell.this.setGraphic(newLabel);
                    }
                });
                AttrTreeCell.this.setGraphic(textField);
                textField.requestFocus();
            }
        }
    }

    static class NameValuePair {
        private String name;
        private Object value;

        public NameValuePair(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

    }
}
