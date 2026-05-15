package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import drinkshop.service.validator.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceValidatorIntegrationTest {
    @Mock
    private Repository<Integer, Product> productRepository;

    private Validator<Product> productValidator;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productValidator = new ProductValidator();
        productService = new ProductService(productRepository, productValidator);
    }

    @DisplayName("Testare pentru actualizarea unui produs valid")
    @Test
    void testUpdateProduct_Valid() {
        Product product = new Product(1, "Cafea cu lapte", 25.00, CategorieBautura.MILK_COFFEE, TipBautura.BASIC);
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        assertDoesNotThrow(() -> {
            productService.updateProduct(product.getId(), product.getNume(), product.getPret(), product.getCategorie(), product.getTip());
        }, "Actualizarea ar trebui sa se execute fara erori.");

        verify(productRepository, times(1)).update(productCaptor.capture()); // verificam ca s-a apelat update din repo
        Product productUpdated = productCaptor.getValue();

        // verificam ca repo a primit argumentele corecte
        assertEquals(product.getId(), productUpdated.getId());
        assertEquals(product.getNume(), productUpdated.getNume());
        assertEquals(product.getPret(), productUpdated.getPret());
        assertEquals(product.getCategorie(), productUpdated.getCategorie());
        assertEquals(product.getTip(), productUpdated.getTip());
    }

    @DisplayName("Testare pentru actualizare produs invalid")
    @Test
    void testUpdateProduct_Invalid() {
        Product invalidProduct = new Product(1, "Cafea cu lapte", -25.00, CategorieBautura.MILK_COFFEE, TipBautura.BASIC); // pret negativ

        // verificam si salvam exceptia aruncata
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.updateProduct(invalidProduct.getId(), invalidProduct.getNume(), invalidProduct.getPret(), invalidProduct.getCategorie(), invalidProduct.getTip());
        });

        String expectedMessage = "Pret invalid!\n";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage, "Mesajul de eroare nu corespunde!");

        verify(productRepository, never()).update(any(Product.class)); // verificam ca nu s-a apelat update in repo
    }
}
