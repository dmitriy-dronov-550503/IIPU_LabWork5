package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Controller {

    AnchorPane root = new AnchorPane();
    private TableView<Device> table = new TableView<Device>();
    private Button unbindButton = new Button("Unbind");
    private Button bindButton = new Button("Bind");
    final ObservableList<Device> data = FXCollections.observableArrayList();



    void init(){

        try {
            parseCommand();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TableColumn slotCol = new TableColumn("Slot");
        slotCol.setMinWidth(50);
        slotCol.prefWidthProperty().bind(table.widthProperty().divide(11));
        slotCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("slot"));

        TableColumn nameCol = new TableColumn("Name");
        nameCol.prefWidthProperty().bind(table.widthProperty().divide(3.6));
        nameCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("name"));

        TableColumn classCol = new TableColumn("Class");
        classCol.prefWidthProperty().bind(table.widthProperty().divide(4.75));
        classCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("className"));

        TableColumn vendorCol = new TableColumn("Vendor");
        vendorCol.prefWidthProperty().bind(table.widthProperty().divide(4.75));
        vendorCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("vendor"));

        TableColumn pathCol = new TableColumn("Path");
        pathCol.prefWidthProperty().bind(table.widthProperty().divide(4.75));
        pathCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("path"));


        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setItems(data);
        table.getColumns().addAll(slotCol, nameCol, classCol, vendorCol, pathCol);

        initUnbindButton();
        initBindButton();

    }

    void stop(){

    }

    Parent getRoot(){
        HBox bottomPanel = new HBox();
        AnchorPane.setBottomAnchor(bottomPanel, 0.0);
        bottomPanel.setSpacing(4.0);
        bottomPanel.getChildren().addAll(unbindButton, bindButton);

        AnchorPane.setBottomAnchor(table, 26.0);
        AnchorPane.setLeftAnchor(table,0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        AnchorPane.setTopAnchor(table,0.0);

        root.getChildren().addAll(table, bottomPanel);
        return root;
    }

    void initUnbindButton(){
        unbindButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (TablePosition pos:
                     table.getSelectionModel().getSelectedCells()) {
                    String slot = table.getItems().get(pos.getRow()).getSlot();
                    String path = table.getItems().get(pos.getRow()).getPath();
                    String appDirectory = System.getProperty("user.dir")+"/src/sample";
                    try {
                        DeviceManager deviceManager = new DeviceManager(slot, path);
                        deviceManager.unbind();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    void initBindButton(){
        bindButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (TablePosition pos:
                        table.getSelectionModel().getSelectedCells()) {
                    String slot = table.getItems().get(pos.getRow()).getSlot();
                    String path = table.getItems().get(pos.getRow()).getPath();
                    String appDirectory = System.getProperty("user.dir")+"/src/sample";
                    try {
                        DeviceManager deviceManager = new DeviceManager(slot, path);
                        deviceManager.bind();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    void parseCommand() throws Exception{
        Process p = Runtime.getRuntime().exec("lspci -vmm -k");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        int returnCode = p.waitFor();
        if (returnCode >= 2) {
            System.out.println("OS Error: Unable to Find File or other OS error.");
        }

        Device device = new Device();
        while (br.ready()) {
            String line = br.readLine();
            int matchIndex = line.indexOf("Slot:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Slot:".length()).trim();
                device.setSlot(line);
                continue;
            }
            matchIndex = line.indexOf("Class:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Class:".length()).trim();
                device.setClassName(line);
                continue;
            }
            matchIndex = line.indexOf("Vendor:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Vendor:".length()).trim();
                device.setVendor(line);
                continue;
            }
            matchIndex = line.indexOf("Device:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Device:".length()).trim();
                device.setName(line);
                continue;
            }
            matchIndex = line.indexOf("Driver:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Driver:".length()).trim();
                device.setPath("/sys/bus/pci/drivers/"+line);
                continue;
            }

            if(line.equals("")){
                data.add(device);
                device = new Device();
            }
        }
    }

    String match(String str, String matcher){
        String temp;
        try{
            temp = str.substring(str.indexOf(matcher)).trim();
        }
        catch (Exception ex){
            return "";
        }
        return temp;
    }

}
