package bee.basket.commonutil.model.response;

import bee.basket.commonutil.model.Account;
import bee.basket.commonutil.contract.ResponseBody;

public record AccountRes<T>(Account body, T message) implements ResponseBody<T> {
}
