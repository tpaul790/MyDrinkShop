package drinkshop.export;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvExporter {
    public static void exportOrders(List<Product> products, List<Order> orders, String path) {
        Map<Integer, Product> productsById = new HashMap<>();
        for (Product p : products) {
            productsById.put(p.getId(), p);
        }

        try (FileWriter w = new FileWriter(path)) {
            w.write("OrderId,Product,Quantity,LineTotal\n");
            double sum = 0.0;

            for (Order o : orders) {
                for (OrderItem i : o.getItems()) {
                    Product p = productsById.getOrDefault(i.getProduct().getId(), i.getProduct());
                    w.write(o.getId() + "," + escapeCsv(p.getNume()) + "," + i.getQuantity() + "," + i.getTotal() + "\n");
                }
                w.write(",Order Total,," + o.getTotal() + "\n");
                sum += o.getTotal();
            }

            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            w.write(",TOTAL " + date + ",," + sum + "\n");
        } catch (IOException e) {
            throw new RuntimeException("Eroare la exportul CSV: " + e.getMessage(), e);
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}