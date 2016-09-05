package com.toone.view;

import java.util.List;

import com.db4o.reflect.ReflectClass;
import com.db4o.reflect.ReflectField;
import com.toone.ctrl.Ctrl;
import com.toone.ctrl.Util;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 程序主窗口
 * @author laiwj
 *
 */
public class MainFrame extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private Stage primaryStage;

    private Ctrl ctrl;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("DB4O数据库管理器");

        ctrl = new Ctrl();
        SplitPane dbExplorer = buildDBExplorer();
        VBox root = new VBox(buildCodeTextArea(), dbExplorer);
        VBox.setVgrow(dbExplorer, Priority.ALWAYS);
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(e -> ctrl.closeDB());
        primaryStage.show();
    }

    /**
     * 代码编辑框
     * @return
     * TODO 未实现
     */
    private Node buildCodeTextArea() {
        return new TextArea();
    }

    private SplitPane buildDBExplorer() {
        SplitPane splitPane = new SplitPane();
        TreeView<ClassTreeItem> classTree = buildClassTree();
        VBox dataPanel = buildDataPanel(splitPane, classTree);
        splitPane.getItems().addAll(classTree, dataPanel);
        return splitPane;
    }

    private TreeView<ClassTreeItem> buildClassTree() {
        TreeItem<ClassTreeItem> root = new TreeItem<>(new ClassTreeItem("root", false, null));
        for (ReflectClass item : ctrl.getAllUserClass()) {
            ClassTreeItem clazz = new ClassTreeItem(item.getName(), true, item);
            TreeItem<ClassTreeItem> classTreeItem = new TreeItem<>(clazz);
            if (item.getDeclaredFields() != null) {
                for (ReflectField field : Util.getAllFields(item)) {
                    ClassTreeItem fieldItem = new ClassTreeItem(field.getName(), false, item);
                    TreeItem<ClassTreeItem> fieldTreeItem = new TreeItem<>(fieldItem);
                    classTreeItem.getChildren().add(fieldTreeItem);
                }
            }
            root.getChildren().add(classTreeItem);
        }
        TreeView<ClassTreeItem> treeView = new TreeView<ClassTreeItem>(root);
        treeView.setShowRoot(false);
        treeView.setPrefWidth(650);
        treeView.setMaxWidth(650);
        return treeView;
    }

    private DataPanel buildDataPanel(SplitPane splitPane, TreeView<ClassTreeItem> classTree) {
        DataPanel dataPanel = new DataPanel(primaryStage, splitPane, ctrl);
        classTree.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                TreeItem<ClassTreeItem> selectedItem = classTree.getSelectionModel().getSelectedItem();
                if (selectedItem.getValue().isClass()) {
                    long s = System.currentTimeMillis();
                    List<Object> allDatas = ctrl.getAllData(selectedItem.getValue().getClazz().getName());
                    long t = System.currentTimeMillis();
                    String queryTime = Double.valueOf((t - s)) / 1000 + "s";
                    System.out.println(queryTime);
                    dataPanel.showQueryDatas(selectedItem.getValue().getClazz(), allDatas, queryTime);
                }
            }
        });
        return dataPanel;
    }

    private static class ClassTreeItem {
        private String name;
        private boolean isClass;

        private ReflectClass clazz;

        public ClassTreeItem(String name, boolean isClass, ReflectClass clazz) {
            this.name = name;
            this.isClass = isClass;
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public boolean isClass() {
            return isClass;
        }

        public ReflectClass getClazz() {
            return clazz;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}