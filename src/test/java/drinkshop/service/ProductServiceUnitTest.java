package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ValidationException;
import drinkshop.service.validator.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock
    private Repository<Integer, Product> productRepository;

    @Mock
    private Validator<Product> productValidator;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(1, "cafea", 20.99, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
    }

    @DisplayName("Testare pentru adaugare produs valid")
    @Test
    void testAddProduct_Valid() {
        doNothing().when(productValidator).validate(product); // nu aruncam exceptie la validare
        productService.addProduct(product);

        verify(productRepository, times(1)).save(product); // verificam apelarea 1 singura data a metodei de save
        verify(productValidator, times(1)).validate(product); // la fel pentru validator
    }

    @DisplayName("Testare pentru adaugare produs invalid")
    @Test
    void testAddProduct_Invalid() {
        doThrow(new ValidationException("Eroare produs")).when(productValidator).validate(product);

        assertThrows(ValidationException.class, () -> productService.addProduct(product));

        verify(productValidator, times(1)).validate(product);
        verify(productRepository, never()).save(product);
    }

}
