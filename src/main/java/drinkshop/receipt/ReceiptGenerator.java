package drinkshop.receipt;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiptGenerator {
    public static String generate(Order o, List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== BON FISCAL =====\n").append("Comanda #").append(o.getId()).append("\n");

        Map<Integer, Product> productsById = new HashMap<>();
        for (Product p : products) {
            productsById.put(p.getId(), p);
        }

        for (OrderItem i : o.getItems()) {
            Product p = productsById.getOrDefault(i.getProduct().getId(), i.getProduct());
            sb.append(p.getNume())
                    .append(": ")
                    .append(p.getPret())
                    .append(" x ")
                    .append(i.getQuantity())
                    .append(" = ")
                    .append(i.getTotal())
                    .append(" RON\n");
        }
        sb.append("---------------------\nTOTAL: ").append(o.getTotalPrice()).append(" RON\n=====================\n");
        return sb.toString();
    }
}