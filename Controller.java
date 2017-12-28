package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.io.*;

public class Controller {

    GridPane root = new GridPane();
    private TableView<Device> table = new TableView<Device>();
    private Button unbindButton = new Button("Unbind");
    final ObservableList<Device> data = FXCollections.observableArrayList();

    void init(){

        try {
            parseCommand();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TableColumn slotCol = new TableColumn("Slot");
        slotCol.setMinWidth(100);
        slotCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("slot"));

        TableColumn nameCol = new TableColumn("Name");
        nameCol.setMinWidth(300);
        nameCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("name"));

        TableColumn classCol = new TableColumn("Class");
        classCol.setMinWidth(200);
        classCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("className"));

        TableColumn vendorCol = new TableColumn("Vendor");
        vendorCol.setMinWidth(200);
        vendorCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("vendor"));

        TableColumn pathCol = new TableColumn("Path");
        pathCol.setMinWidth(100);
        pathCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("path"));

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setItems(data);
        table.getColumns().addAll(slotCol, nameCol, classCol, vendorCol);

        initUnbindButton();


    }
    void stop(){

    }

    Parent getRoot(){
        root.add(table, 0, 0);
        root.add(unbindButton, 0,1);
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
                    String cmd = "echo 0000:"+slot+" | sudo -S tee -a "+path+"/unbind";

                    /*try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(path+"/unbind", true));
                        writer.append("0000:"+slot);
                        writer.close();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }*/

                    Process pb = null;
                    try {
                        pb = Runtime.getRuntime().exec(cmd);

                        OutputStream out = pb.getOutputStream();
                        out.write("316728pas".getBytes());

                        String line;
                        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
                        while ((line = input.readLine()) != null) {
                            System.out.println(line);
                        }
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void parseCommand() throws Exception{
        String patternSlot = "(?<=Slot:)(.*)";
        String patternClass = "(?<=Class:)(.*)";
        String patternVendor = "(?<=Vendor:)(.*)";
        String patternDevice = "(?<=Device:)(.*)";


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
