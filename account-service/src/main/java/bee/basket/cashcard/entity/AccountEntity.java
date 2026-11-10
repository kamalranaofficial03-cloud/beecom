package bee.basket.cashcard.entity;

import bee.basket.commonutil.contract.ToModel;
import bee.basket.commonutil.model.Account;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name="accounts")
public class AccountEntity extends Audit implements ToModel<Account> {
    @Column(name="contact_no")
    private String contactNo;
    @Column(name="first_name")
    private String firstName;
    @Column(name="last_name")
    private String lastName;
    @Column(name="username", unique = true, nullable = false)
    private String username;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account_id")
    private List<AddressEntity> addresses = new ArrayList<>();

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<AddressEntity> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressEntity> addresses) {
        this.addresses = addresses;
    }

    @Override
    public Account model(){
        return new Account(id, firstName, lastName,username,contactNo, addresses.stream().map(a-> a.model()).collect(Collectors.toUnmodifiableList()));
    }

    public static AccountEntity create(Account account){
        AccountEntity newInstance = new AccountEntity();
        newInstance.setFirstName(account.firstName());
        newInstance.setLastName(account.lastName());
        newInstance.setContactNo(account.contactNo());
        newInstance.setUsername(account.username());
        return newInstance;
    }
}
