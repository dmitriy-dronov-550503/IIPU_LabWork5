package sample;

public class Device {
    private String slot="", name="", className="", vendor="", path="";

    public Device(String slot, String name, String className, String vendor, String path) {
        this.slot = slot;
        this.name = name;
        this.className = className;
        this.vendor = vendor;
        this.path = path;
    }

    public Device(){

    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
