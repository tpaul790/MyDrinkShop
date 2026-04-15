package drinkshop.service;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;
import drinkshop.repository.Repository;

import java.util.List;

public class OrderService {

    private final Repository<Integer, Order> orderRepo;
    private final Repository<Integer, Product> productRepo;

    public OrderService(Repository<Integer, Order> orderRepo, Repository<Integer, Product> productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;

    }

    public void addOrder(Order o) {
        orderRepo.save(o);
    }

    public void updateOrder(Order o) {
        orderRepo.update(o);
    }

    public void deleteOrder(int id) {
        orderRepo.delete(id);
    }

    public List<Order> getAllOrders() {
//        return StreamSupport.stream(orderRepo.findAll().spliterator(), false)
//                .collect(Collectors.toList());
        return orderRepo.findAll();
    }

    public Order findById(int id) {
        return orderRepo.findOne(id);
    }

    /**
     * Calculează totalul unei comenzi aplicând reguli de validare și discount.
     *
     * <p>Decizii (pentru analiza White-Box):</p>
     * <ul>
     *   <li>D1: order == null</li>
     *   <li>D2: items == null || items.isEmpty()</li>
     *   <li>D3: for-each item (structură repetitivă)</li>
     *   <li>D4: item == null || item.getProduct() == null</li>
     *   <li>D5: item.getQuantity() &lt;= 0</li>
     *   <li>D6: product not found in repository</li>
     *   <li>D7: total > 100 → discount 10%</li>
     * </ul>
     *
     * @param o comanda pentru care se calculează totalul
     * @return totalul comenzii (cu discount dacă este cazul)
     */
    public double computeTotal(Order o) {
        if (o == null) {                                                          // D1
            return 0.0;
        }

        List<OrderItem> items = o.getItems();
        if (items == null || items.isEmpty()) {                                   // D2
            return 0.0;
        }

        double total = 0.0;

        for (OrderItem item : items) {                                            // D3 (structură repetitivă)s
            if (item == null || item.getProduct() == null) {                      // D4
                throw new IllegalArgumentException("Order item invalid: produs lipsa.");
            }

            if (item.getQuantity() <= 0) {                                       // D5
                throw new IllegalArgumentException("Order item invalid: cantitate <= 0.");
            }

            Product product = productRepo.findOne(item.getProduct().getId());
            if (product == null) {                                                // D6
                throw new IllegalArgumentException(
                        "Produs inexistent pentru id=" + item.getProduct().getId());
            }

            total += product.getPret() * item.getQuantity();
        }

        if (total > 100.0) {                                                     // D7 – discount volum 10%
            total *= 0.9;
        }

        return total;
    }

    public void addItem(Order o, OrderItem item) {
        o.getItems().add(item);
        orderRepo.update(o);
    }

    public void removeItem(Order o, OrderItem item) {
        o.getItems().remove(item);
        orderRepo.update(o);
    }
}