package bee.basket.commonutil.constant;

import bee.basket.commonutil.contract.Value;

public enum RoleEnum implements Value<String> {
    USER("user"), ADMIN("admin");
    private final String role;
    RoleEnum(String role){
        this.role = role;
    }
    @Override
    public String value(){
        return this.role;
    }
}
