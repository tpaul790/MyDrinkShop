package drinkshop.repository.file;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;
import drinkshop.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class FileOrderRepository
        extends FileAbstractRepository<Integer, Order> {

    private Repository<Integer, Product> productRepository;

    public FileOrderRepository(String fileName, Repository<Integer, Product> productRepository) {
        super(fileName);
        this.productRepository = productRepository;
        loadFromFile();
    }

    @Override
    protected Integer getId(Order entity) {
        return entity.getId();
    }

    @Override
    protected Order extractEntity(String line) {

        // Format: id,productId:qty|productId:qty,total
        String[] parts = line.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Linie comanda invalida: " + line);
        }

        int id;
        double totalPrice;
        try {
            id = Integer.parseInt(parts[0].trim());
            totalPrice = Double.parseDouble(parts[2].trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Date numerice invalide in comanda: " + line, ex);
        }

        List<OrderItem> items = new ArrayList<>();
        String[] products = parts[1].split("\\|");

        for (String product : products) {
            String[] prodParts = product.split(":");
            if (prodParts.length != 2) {
                throw new IllegalArgumentException("Item comanda invalid: " + product + " in linia: " + line);
            }

            int productId;
            int quantity;
            try {
                productId = Integer.parseInt(prodParts[0].trim());
                quantity = Integer.parseInt(prodParts[1].trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Date invalide pentru item: " + product + " in linia: " + line, ex);
            }

            Product foundProduct = productRepository.findOne(productId);
            if (foundProduct == null) {
                throw new IllegalArgumentException("Produs inexistent in comanda: productId=" + productId);
            }

            items.add(new OrderItem(foundProduct, quantity));
        }

        return new Order(id, items, totalPrice);
    }

    @Override
    protected String createEntityAsString(Order entity) {

        StringBuilder sb = new StringBuilder();

        for (OrderItem item : entity.getItems()) {

            if (sb.length() > 0) {
                sb.append("|");
            }

            sb.append(item.getProduct().getId())
                    .append(":")
                    .append(item.getQuantity());
        }

        return entity.getId() + "," +
                sb + "," +
                entity.getTotalPrice();
    }
}
