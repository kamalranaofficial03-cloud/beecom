package bee.basket.commonutil.model.response;

import bee.basket.commonutil.contract.ResponseBody;
import bee.basket.commonutil.model.Product;

public record ProductRes<T>(Product body, T message) implements ResponseBody<T> {
}