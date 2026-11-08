package bee.basket.commonutil.constant;

import bee.basket.commonutil.service.Value;

public enum PermissionEnum implements Value<String> {
    READ("read"), WRITE("write");
    private final String permission;
    PermissionEnum(String permission){
        this.permission  = permission;
    }
    @Override
    public String value(){
        return this.permission;
    }
}
