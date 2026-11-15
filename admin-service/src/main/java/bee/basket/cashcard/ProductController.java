package bee.basket.cashcard;

import java.util.List;

import bee.basket.commonutil.model.response.ProductRes;
import bee.basket.commonutil.model.response.ProductsRes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("products")
public class ProductController {

	private final ProductServiceImpl productService;

	public ProductController(ProductServiceImpl productService) {
		this.productService = productService;
	}

	@GetMapping("/list/{productId}")
	public ResponseEntity<ProductsRes<?>> findByCashCardId(@PathVariable ("productId")String productId, Authentication authentication) {
		var products = this.productService.getProductByProductId(productId);
		return ResponseEntity.ok(new ProductsRes<>(products, "Success"));
	}

}
