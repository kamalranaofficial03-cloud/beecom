package bee.basket.commonutil.model.response;

import bee.basket.commonutil.model.Product;

import java.util.List;

public record ProductsRes<T>(List<Product> body, T message) {
}
