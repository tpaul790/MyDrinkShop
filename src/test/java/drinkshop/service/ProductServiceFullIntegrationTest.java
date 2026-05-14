package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.repository.file.FileProductRepository;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import drinkshop.service.validator.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductServiceFullIntegrationTest {

    private ProductService productService;
    private Repository<Integer, Product> productRepository;
    private Validator<Product> productValidator;
    private Product product;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // cream folder-ul temporar
        Path filePath = tempDir.resolve("products.txt");
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
        String absolutePath = filePath.toAbsolutePath().toString();

        // instantiem componentele testate
        productRepository = new FileProductRepository(absolutePath);
        productValidator = new ProductValidator();
        productService = new ProductService(productRepository, productValidator);

        // salvam un produs pentru a respecta preconditiile
        product = new Product(1, "cafea", 20.99, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        productRepository.save(product);
    }

    @DisplayName("Testare integrativa completa pentru actualizare produs valid")
    @Test
    void testUpdateProduct_Valid() {
        productService.updateProduct(product.getId(), product.getNume(), 25.00, product.getCategorie(), product.getTip()); // actualizam pretul produsului

        Product productUpdated = productRepository.findOne(product.getId()); // extragem produsul actualizat din fisier
        assertEquals(25.00, productUpdated.getPret(), "Pretul ar trebuie sa fie actualizat la 25.00"); // verificam actualizarea pretului
    }

    @DisplayName("Testare integrativa completa pentru actualizare produs invalid")
    @Test
    void testUpdateProduct_Invalid() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.updateProduct(product.getId(), product.getNume(), -10.00, product.getCategorie(), product.getTip());
        });

        String expectedMessage = "Pret invalid!\n";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage, "Mesajul de eroare nu corespunde!");
    }
}
