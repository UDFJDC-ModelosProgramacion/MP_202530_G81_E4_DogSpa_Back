package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ProductService.class) // carga solo el bean del servicio
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private OrderDetailRepository orderDetailRepository;

    private ProductEntity sample;

    @BeforeEach
    void init() {
        sample = new ProductEntity();
        sample.setId(1L);
        sample.setName("Sample");
        sample.setPrice(12.34);
        sample.setStock(10);
    }

    @Test
    void findAll_ok() {
        when(productRepository.findAll()).thenReturn(List.of(sample));

        var result = productService.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(productRepository).findAll();
        verifyNoMoreInteractions(productRepository, orderDetailRepository);
    }

    @Test
    void findById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sample));

        var result = productService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository, orderDetailRepository);
    }

    @Nested
    class Save {

        @Test
        void save_validPrice_roundsToTwoDecimals() {
            var p = new ProductEntity();
            p.setName("A");
            p.setPrice(12.3); // válido (<= 2 decimales)
            p.setStock(5);

            when(productRepository.save(any(ProductEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var saved = productService.save(p);

            assertEquals(12.3, saved.getPrice());
            verify(productRepository).save(any(ProductEntity.class));
            verifyNoMoreInteractions(productRepository, orderDetailRepository);
        }

        @Test
        void save_nullOrNegativePrice_throws() {
            var pNull = new ProductEntity();
            pNull.setPrice(null);
            assertThrows(IllegalArgumentException.class, () -> productService.save(pNull));

            var pNeg = new ProductEntity();
            pNeg.setPrice(-1.0);
            assertThrows(IllegalArgumentException.class, () -> productService.save(pNeg));

            verifyNoInteractions(productRepository, orderDetailRepository);
        }

        @Test
        void save_moreThanTwoDecimals_throws() {
            var p = new ProductEntity();
            p.setPrice(12.345); // 3 decimales -> inválido por validatePrice()

            assertThrows(IllegalArgumentException.class, () -> productService.save(p));
            verifyNoInteractions(productRepository, orderDetailRepository);
        }
    }

    @Nested
    class UpdateStock {

        @Test
        void updateStock_notFound_throws() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> productService.updateStock(99L, 10));

            verify(productRepository).findById(99L);
            verifyNoMoreInteractions(productRepository);
            verifyNoInteractions(orderDetailRepository);
        }

        @Test
        void updateStock_lessThanReserved_throws() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sample));
            when(orderDetailRepository.countReservedForProduct(1L)).thenReturn(5);

            assertThrows(IllegalArgumentException.class, () -> productService.updateStock(1L, 4));

            verify(productRepository).findById(1L);
            verify(orderDetailRepository).countReservedForProduct(1L);
            verifyNoMoreInteractions(productRepository, orderDetailRepository);
        }

        @Test
        void updateStock_ok() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sample));
            when(orderDetailRepository.countReservedForProduct(1L)).thenReturn(3);
            when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            var updated = productService.updateStock(1L, 10);

            assertEquals(10, updated.getStock());
            verify(productRepository).findById(1L);
            verify(orderDetailRepository).countReservedForProduct(1L);
            verify(productRepository).save(sample);
            verifyNoMoreInteractions(productRepository, orderDetailRepository);
        }
    }

    @Nested
    class DeleteById {

        @Test
        void deleteById_hasOrderDetails_throws() {
            when(orderDetailRepository.countByProductId(1L)).thenReturn(2);

            assertThrows(IllegalStateException.class, () -> productService.deleteById(1L));

            verify(orderDetailRepository).countByProductId(1L);
            verifyNoMoreInteractions(orderDetailRepository);
            verifyNoInteractions(productRepository);
        }

        @Test
        void deleteById_ok() {
            when(orderDetailRepository.countByProductId(1L)).thenReturn(0);

            productService.deleteById(1L);

            verify(orderDetailRepository).countByProductId(1L);
            verify(productRepository).deleteById(1L);
            verifyNoMoreInteractions(productRepository, orderDetailRepository);
        }
    }
    @Test
    @DisplayName("save lanza IllegalArgumentException si el precio es negativo")
    void save_invalidPrice_throwsException() {
        ProductEntity p = new ProductEntity();
        p.setPrice(-5.0);
        assertThrows(IllegalArgumentException.class, () -> productService.save(p));
    }

    @Test
    @DisplayName("updateStock lanza IllegalArgumentException si stock < reservado")
    void updateStock_reservedGreaterThanNewStock() {
        ProductEntity p = new ProductEntity();
        p.setId(1L);
        p.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(orderDetailRepository.countReservedForProduct(1L)).thenReturn(20);
        assertThrows(IllegalArgumentException.class, 
            () -> productService.updateStock(1L, 5));
    }

    @Test
    @DisplayName("deleteById lanza IllegalStateException si tiene detalles de orden")
    void deleteById_withOrders_throwsException() {
        when(orderDetailRepository.countByProductId(1L)).thenReturn(2);
        assertThrows(IllegalStateException.class, () -> productService.deleteById(1L));
    }

    @Test
    @DisplayName("getByIdOrThrow lanza IllegalArgumentException si producto no existe")
    void getByIdOrThrow_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> productService.getByIdOrThrow(99L));
        verify(productRepository).findById(99L);
    }

    @Test
    @DisplayName("save lanza IllegalArgumentException si el precio es null")
    void save_nullPrice_throwsException() {
        ProductEntity p = new ProductEntity();
        p.setPrice(null);
        assertThrows(IllegalArgumentException.class, () -> productService.save(p));
    }

    @Test
    @DisplayName("update actualiza nombre, descripción y precio correctamente")
    void update_validFields_ok() {
        ProductEntity existing = new ProductEntity();
        existing.setId(1L);
        existing.setName("Old");
        existing.setDescription("Old desc");
        existing.setPrice(10.0);
        existing.setStock(5);

        ProductEntity incoming = new ProductEntity();
        incoming.setName("New");
        incoming.setDescription("New desc");
        incoming.setPrice(20.5);
        incoming.setStock(8);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(orderDetailRepository.countReservedForProduct(1L)).thenReturn(0);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = productService.update(1L, incoming);

        assertEquals("New", result.getName());
        assertEquals("New desc", result.getDescription());
        assertEquals(20.5, result.getPrice());
        assertEquals(8, result.getStock());
    }

    @Test
    @DisplayName("update lanza IllegalArgumentException si stock < reservado")
    void update_stockLessThanReserved_throwsException() {
        ProductEntity existing = new ProductEntity();
        existing.setId(1L);
        existing.setStock(10);

        ProductEntity incoming = new ProductEntity();
        incoming.setStock(2);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(orderDetailRepository.countReservedForProduct(1L)).thenReturn(5);

        assertThrows(IllegalArgumentException.class, () -> productService.update(1L, incoming));
    }

    @Test
    @DisplayName("roundToTwoDecimals devuelve null si el valor es null")
    void roundToTwoDecimals_null_returnsNull() {
        try {
            var method = ProductService.class.getDeclaredMethod("roundToTwoDecimals", Double.class);
            method.setAccessible(true);
            Double result = (Double) method.invoke(productService, new Object[]{null});
            assertNull(result);
        } catch (Exception e) {
            fail(e);
        }
    }

}
