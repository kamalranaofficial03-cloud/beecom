package bee.basket.commonutil.model.response;

import bee.basket.commonutil.contract.ResponseBody;
import bee.basket.commonutil.model.User;

public record UserRes<T>(User body, T message) implements ResponseBody<T> {
}
