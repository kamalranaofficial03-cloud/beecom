package bee.basket.cashcard.entity;

import bee.basket.commonutil.contract.ToModel;
import bee.basket.commonutil.model.Address;
import jakarta.persistence.*;

@Entity
@Table(name="addresses")
public class AddressEntity extends Audit implements ToModel<Address> {
    private String name;
    private String address;
    private String city;
    private String state;
    private String landmark;
    private String pincode;
    private String country;
    private String contactNo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    @Override
    public Address model(){
        return new Address(id,name,address,city,state,landmark,pincode,country,contactNo);
    }

    public static AddressEntity create(Address address){
        AddressEntity newInstance = new AddressEntity();
        newInstance.setAddress(address.address());
        newInstance.setCity(address.city());
        newInstance.setContactNo(address.contactNo());
        newInstance.setLandmark(address.landmark());
        newInstance.setCountry(address.country());
        newInstance.setState(address.state());
        newInstance.setName(address.name());
        newInstance.setPincode(address.pincode());
        return newInstance;
    }


}
