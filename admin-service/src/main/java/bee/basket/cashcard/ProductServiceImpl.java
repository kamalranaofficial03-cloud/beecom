package bee.basket.cashcard;

import java.util.List;

import bee.basket.cashcard.entity.ProductEntity;
import bee.basket.commonutil.contract.service.ProductService;
import bee.basket.commonutil.model.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public List<Product> getProductByProductId(String productId) {
		List< ProductEntity> products = this.productRepository.findByProductId(productId);
	}

}
