package bee.basket.commonutil.model;

import java.util.List;

public record Account(Long id, String firstName, String lastName, String username, String contactNo, List<Address> address) {
}
