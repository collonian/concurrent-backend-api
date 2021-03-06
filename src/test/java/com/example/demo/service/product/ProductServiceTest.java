package com.example.demo.service.product;

import com.example.demo.service.Page;
import com.example.demo.service.product.vo.Product;
import com.example.demo.service.product.vo.ProductList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    public void shouldNotReadProductList_whenFindInvestable_givenEmptyProduct() {
        // when
        ProductList result = productService.findInvestable(new Page(10, 20));

        // then
        assertEquals(0, result.getCount());
        assertTrue(result.getProducts().isEmpty());
        verify(productRepository).countInvestable(any());
        verify(productRepository, times(0)).findInvestable(any(), any());
    }

    @Test
    public void shouldPassSameTime_whenFindInvestable() {
        // given
        Product product = new Product();
        when(productRepository.countInvestable(any()))
                .thenReturn(1);
        when(productRepository.findInvestable(any(), any()))
                .thenReturn(Collections.singletonList(product));

        // when
        ProductList result = productService.findInvestable(new Page(0, 10));

        // then
        assertEquals(1, result.getCount());
        assertEquals(product, result.getProducts().get(0));

        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(productRepository).countInvestable(nowCaptor.capture());
        verify(productRepository).findInvestable(eq(nowCaptor.getValue()), any());
    }
}