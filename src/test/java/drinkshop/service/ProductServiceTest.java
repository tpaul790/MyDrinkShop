package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.repository.file.FileProductRepository;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {

    private ProductService productService;
    private final String TEST_FILE_PATH = "data/products_test.txt";

    @BeforeEach
    void setUp() throws IOException {
        // ARRANGE: curatam mediul de test
        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }

        boolean created = file.createNewFile();
        if (created) {
            System.out.println("Fisierul de test a fost creat cu succes!");
        }

        Repository<Integer, Product> productRepository = new FileProductRepository(TEST_FILE_PATH);
        productService = new ProductService(productRepository);

        // preconditie pentru actualizare: existenta unui produs cu id = 100
        productRepository.save(new Product(100, "cafea", 25, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC));
    }

    // TC1
    @Test
    @Tag("ECP")
    @DisplayName("TC1_ECP_VALID: Actualizare nume valid")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testUpdateNameValidECP() {
        // ARRANGE
        int id = 100;
        String name = "cafea_noua";
        double price = 25;
        CategorieBautura cat = CategorieBautura.CLASSIC_COFFEE;
        TipBautura tip = TipBautura.BASIC;

        // ACT + ASSERT
        assertDoesNotThrow(() -> {
            productService.updateProduct(id, name, price, cat, tip);
        }, "Actualizarea ar trebui sa se execute fara erori.");
    }

    // TC2
    @Test
    @Tag("ECP")
    @DisplayName("TC2_ECP_INVALID: Actualizare pret invalid")
    void testUpdatePriceInvalidECP() {
        // ARRANGE
        int id = 100;
        String name = "cafea_noua";
        double price = -12.25;
        CategorieBautura cat = CategorieBautura.CLASSIC_COFFEE;
        TipBautura tip = TipBautura.BASIC;

        // ACT
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.updateProduct(id, name, price, cat, tip);
        });

        // ASSERT
        String expectedMessage = "Pret invalid!\n";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage, "Mesajul de eroare nu corespunde!");
    }

    // TC3
    @Test
    @Tag("ECP")
    @DisplayName("TC3_ECP_INVALID: Actualizare nume invalid")
    void testUpdateNameInvalidECP() {
        // ARRANGE
        int id = 100;
        String name = "ca";
        double price = 25.5;
        CategorieBautura cat = CategorieBautura.CLASSIC_COFFEE;
        TipBautura tip = TipBautura.BASIC;

        // ACT
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.updateProduct(id, name, price, cat, tip);
        });

        // ASSERT
        String expectedMessage = "Numele trebuie sa contina cel putin 3 caractere!\n";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage, "Mesajul de eroare nu corespunde!");
    }

    // TC4
    @Test
    @Tag("BVA")
    @DisplayName("TC1_BVA_VALID: Actualizare nume valid")
    void testUpdateNameValidBVA() {
        // ARRANGE
        int id = 100;
        String name = "nou";
        double price = 10;
        CategorieBautura cat = CategorieBautura.CLASSIC_COFFEE;
        TipBautura tip = TipBautura.BASIC;

        // ACT + ASSERT
        assertDoesNotThrow(() -> {
            productService.updateProduct(id, name, price, cat, tip);
        }, "Actualizarea ar trebui sa se execute fara erori.");
    }

    // TC5
    @Test
    @Tag("BVA")
    @DisplayName("TC2_BVA_INVALID: Actualizare nume invalid")
    void testUpdateNameInvalidBVA() {
        // ARRANGE
        int id = 100;
        String name = "ca";
        double price = 10;
        CategorieBautura cat = CategorieBautura.CLASSIC_COFFEE;
        TipBautura tip = TipBautura.BASIC;

        // ACT
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.updateProduct(id, name, price, cat, tip);
        });

        // ASSERT
        String expectedMessage = "Numele trebuie sa contina cel putin 3 caractere!\n";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage, "Mesajul de eroare nu corespunde!");
    }

    // TC6
    @Test
    @Tag("BVA")
    @DisplayName("TC4_BVA_VALID: Actualizare pret valid")
    void testUpdatePriceValidBVA() {
        // ARRANGE
        int id = 100;
        String name = "cafea";
        double price = 0.01;
        CategorieBautura cat = CategorieBautura.CLASSIC_COFFEE;
        TipBautura tip = TipBautura.BASIC;

        // ACT + ASSERT
        assertDoesNotThrow(() -> {
            productService.updateProduct(id, name, price, cat, tip);
        }, "Actualizarea ar trebui sa se execute fara erori.");
    }

    // TC7
    @Test
    @Tag("BVA")
    @DisplayName("TC5_BVA_INVALID: Actualizare pret invalid")
    void testUpdatePriceInvalidBVA() {
        // ARRANGE
        int id = 100;
        String name = "cafea";
        double price = -0.01;
        CategorieBautura cat = CategorieBautura.CLASSIC_COFFEE;
        TipBautura tip = TipBautura.BASIC;

        // ACT
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.updateProduct(id, name, price, cat, tip);
        });

        // ASSERT
        String expectedMessage = "Pret invalid!\n";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage, "Mesajul de eroare nu corespunde!");
    }


    @AfterEach
    void tearDown() {
        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
}
