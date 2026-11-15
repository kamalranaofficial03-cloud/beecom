package bee.basket.cashcard;

import bee.basket.cashcard.entity.ProductEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface ProductRepository extends CrudRepository<ProductEntity, Long> {
	@NonNull
	List<ProductEntity> findByProductId(String productId);

}
