package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.AbstractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste White-Box (Decision/Branch Coverage) pentru metoda
 * {@link OrderService#computeTotal(Order)}.
 *
 * <p><b>Cerința F02:</b> Procesare Comandă – calcularea totalului unei comenzi.</p>
 *
 * <p>Metoda refactorizată conține <b>7 puncte decizionale</b> (CC = 8):</p>
 * <ul>
 *   <li>D1: o == null</li>
 *   <li>D2: items == null || items.isEmpty()</li>
 *   <li>D3: for-each item (structură repetitivă)</li>
 *   <li>D4: item == null || item.getProduct() == null</li>
 *   <li>D5: item.getQuantity() &le; 0</li>
 *   <li>D6: productRepo.findOne(id) == null</li>
 *   <li>D7: total &gt; 100 → discount 10%</li>
 * </ul>
 *
 * <p>14 branch-uri (7×True + 7×False) acoperite cu 8 teste → <b>100% Branch Coverage</b>.</p>
 *
 * <p>Pattern: AAA (Arrange – Act – Assert).</p>
 * <p>Adnotări distincte: {@code @ParameterizedTest}, {@code @Tag}, {@code @DisplayName},
 * {@code @Timeout}, {@code @Test}.</p>
 */
@DisplayName("WB – OrderService.computeTotal() – Branch Coverage (F02)")
public class OrderServiceTest {

    private static class InMemoryProductRepository extends AbstractRepository<Integer, Product> {
        @Override
        protected Integer getId(Product entity) {
            return entity.getId();
        }
    }

    private static class InMemoryOrderRepository extends AbstractRepository<Integer, Order> {
        @Override
        protected Integer getId(Order entity) {
            return entity.getId();
        }
    }

    private InMemoryProductRepository productRepo;
    private InMemoryOrderRepository orderRepo;
    private OrderService service;

    @BeforeEach
    void setUp() {
        productRepo = new InMemoryProductRepository();
        orderRepo = new InMemoryOrderRepository();
        service = new OrderService(orderRepo, productRepo);
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("TC01_WB_ValidOrder_NoDiscount")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc01_validOrder_totalUnder100_noDiscount() {
        Product p1 = new Product(1, "Cafea", 15.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        Product p2 = new Product(2, "Ceai", 10.0, CategorieBautura.TEA, TipBautura.WATER_BASED);
        productRepo.save(p1);
        productRepo.save(p2);

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(p1, 2));  // 15 * 2 = 30
        items.add(new OrderItem(p2, 3));  // 10 * 3 = 30
        Order order = new Order(1, items, 0.0);

        double total = service.computeTotal(order);

        assertEquals(60.0, total, 0.001,
                "Totalul trebuie să fie 60.0 (fără discount).");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("TC02_WB_NullOrder_D1True")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc02_nullOrder_returnsZero() {

        double total = service.computeTotal(null);

        assertEquals(0.0, total, 0.001,
                "Totalul pentru o comandă null trebuie să fie 0.0.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("TC03_WB_NullItems_D2True")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc03_nullItems_returnsZero() {
        Order order = new Order(1, new ArrayList<>(), 0.0);
        order.setItems(null);  // forțăm items = null

        double total = service.computeTotal(order);

        assertEquals(0.0, total, 0.001,
                "Totalul pentru items=null trebuie să fie 0.0.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("TC04_WB_EmptyItems_D2True")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc04_emptyItems_returnsZero() {
        Order order = new Order(1, new ArrayList<>(), 0.0);

        double total = service.computeTotal(order);

        assertEquals(0.0, total, 0.001,
                "Totalul pentru o comandă fără items trebuie să fie 0.0.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("TC05_WB_NullItem_D4True")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc05_nullItem_throwsException() {
        List<OrderItem> items = new ArrayList<>();
        items.add(null);
        Order order = new Order(1, items, 0.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.computeTotal(order));
        assertTrue(ex.getMessage().contains("produs lipsa"),
                "Mesajul trebuie să menționeze 'produs lipsa'.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("TC05b_WB_NullProduct_D4True_RightSide")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc05b_nullProduct_throwsException() {
        // item nu e null, dar item.getProduct() == null
        OrderItem itemWithNullProduct = new OrderItem(null, 2);
        List<OrderItem> items = new ArrayList<>();
        items.add(itemWithNullProduct);
        Order order = new Order(1, items, 0.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.computeTotal(order));
        assertTrue(ex.getMessage().contains("produs lipsa"),
                "Mesajul trebuie să menționeze 'produs lipsa'.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("TC06_WB_InvalidQuantity_D5True")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc06_invalidQuantity_throwsException() {
        Product p1 = new Product(1, "Cafea", 15.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        productRepo.save(p1);

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(p1, 0));  // cantitate = 0
        Order order = new Order(1, items, 0.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.computeTotal(order));
        assertTrue(ex.getMessage().contains("cantitate <= 0"),
                "Mesajul trebuie să menționeze 'cantitate <= 0'.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("TC07_WB_ProductNotInRepo_D6True")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc07_productNotInRepo_throwsException() {
        Product p1 = new Product(99, "Inexistent", 10.0, CategorieBautura.JUICE, TipBautura.WATER_BASED);

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(p1, 2));
        Order order = new Order(1, items, 0.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.computeTotal(order));
        assertTrue(ex.getMessage().contains("Produs inexistent"),
                "Mesajul trebuie să menționeze 'Produs inexistent'.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("TC08_WB_DiscountApplied_D7True")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc08_totalOver100_discountApplied() {
        Product p1 = new Product(1, "Special", 50.0, CategorieBautura.SPECIAL_COFFEE, TipBautura.BASIC);
        productRepo.save(p1);

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(p1, 3));  // 50 * 3 = 150
        Order order = new Order(1, items, 0.0);

        double total = service.computeTotal(order);

        assertEquals(135.0, total, 0.001,
                "Totalul trebuie să fie 135.0 (150 - 10% discount).");
    }


    static Stream<org.junit.jupiter.params.provider.Arguments> boundaryScenarios() {
        Product p1 = new Product(1, "Cafea", 20.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        Product p2 = new Product(2, "Latte", 25.0, CategorieBautura.MILK_COFFEE, TipBautura.DAIRY);

        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        "1 item, total=100, prag exact, fara discount",
                        List.of(new OrderItem(p1, 5)),
                        new Product[]{p1},
                        100.0
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "2 items, total=105, discount 10%",
                        List.of(new OrderItem(p1, 4), new OrderItem(p2, 1)),
                        new Product[]{p1, p2},
                        94.5
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "1 item, total=20, fara discount",
                        List.of(new OrderItem(p1, 1)),
                        new Product[]{p1},
                        20.0
                )
        );
    }

    @ParameterizedTest(name = "TC09[{index}]: {0}")
    @MethodSource("boundaryScenarios")
    @Tag("WhiteBox")
    @DisplayName("TC09_WB_Parameterized_BoundaryScenarios")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void tc09_parameterizedBoundaryScenarios(String description, List<OrderItem> items,
                                              Product[] products, double expected) {
        for (Product p : products) {
            productRepo.save(p);
        }
        Order order = new Order(1, items, 0.0);

        double total = service.computeTotal(order);

        assertEquals(expected, total, 0.001,
                "Scenariu: " + description);
    }
}


