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
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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


    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
    private void myAction() throws Exception{
        ProcessBuilder builder = new ProcessBuilder();
        String appDirectory = System.getProperty("user.dir")+"/src/sample";


        builder.command("gksudo","bash", appDirectory+"/bind.sh");
        builder.directory(new File(appDirectory));
        Process process = builder.start();
        //OutputStream out = process.getOutputStream();
        //out.write("\n\r316728pas\n\r".getBytes());
        //out.close();
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
    }
    public static boolean runWithPrivileges() {
        InputStreamReader input;
        OutputStreamWriter output;

        try {
            //Create the process and start it.
            Process pb = new ProcessBuilder("gksudo").start();
            output = new OutputStreamWriter(pb.getOutputStream());
            input = new InputStreamReader(pb.getInputStream());

            int bytes, tryies = 0;
            char buffer[] = new char[1024];
            while ((bytes = input.read(buffer, 0, 1024)) != -1) {
                if(bytes == 0)
                    continue;
                //Output the data to console, for debug purposes
                String data = String.valueOf(buffer, 0, bytes);
                System.out.println(data);
                // Check for password request
                if (data.contains("[sudo] password")) {
                    System.out.println("Password requested");
                    // Here you can request the password to user using JOPtionPane or System.console().readPassword();
                    // I'm just hard coding the password, but in real it's not good.
                    char password[] = new char[]{'p'};
                    output.write(password);
                    output.write('\n');
                    output.flush();
                    // erase password data, to avoid security issues.
                    Arrays.fill(password, '\0');
                    tryies++;
                }
            }

            return tryies < 3;
        } catch (IOException ex) {
        }

        return false;
    }

    void initUnbindButton(){
        unbindButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {


                String appDirectory = System.getProperty("user.dir")+"/src/sample";
                try{
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(appDirectory+"/unbind.sh"));
                        writer.write("hello");
                        writer.close();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                    //myAction();
                    /*String[] command =
                            {
                                    "bash",
                            };
                    Process p = Runtime.getRuntime().exec(command);
                    new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
                    new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
                    PrintWriter stdin = new PrintWriter(p.getOutputStream());
                    stdin.println("bash unbind");
                    // write any other commands you want here
                    stdin.close();
                    int returnCode = p.waitFor();
                    System.out.println("Return code = " + returnCode);*/
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }


                /*
                for (TablePosition pos:
                     table.getSelectionModel().getSelectedCells()) {
                    String slot = table.getItems().get(pos.getRow()).getSlot();
                    String path = table.getItems().get(pos.getRow()).getPath();
                    //String cmd = "sudo echo 0000:"+slot+" | sudo tee -a "+path+"/unbind";
                    String cmd = "gksudo bash -c 'echo \"0000:"+slot+"\" > "+path+"/unbind'";
                    System.out.println("Result: "+cmd);
*/




                    /*try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(path+"/unbind", true));
                        writer.append("0000:"+slot);
                        writer.close();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }*/

/*
                    Process pb = null;
                    try {
                        pb = Runtime.getRuntime().exec(cmd);

                        //OutputStream out = pb.getOutputStream();
                        //out.write(("316728pas"+13).getBytes());


                        String line;
                        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
                        while ((line = input.readLine()) != null) {
                            System.out.println(line);
                        }
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/
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
