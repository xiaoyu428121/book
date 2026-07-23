package com.bookcycle.backend.service.impl;

import com.bookcycle.backend.entity.Book;
import com.bookcycle.backend.mapper.BookMapper;
import com.bookcycle.backend.mapper.CategoryMapper;
import com.bookcycle.backend.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * RecommendServiceImpl 单元测试。
 * 通过 Mockito 桩接 Mapper，无需真实数据库即可验证推荐降级逻辑。
 * 运行：mvn test -Dtest=RecommendServiceImplTest
 */
@ExtendWith(MockitoExtension.class)
class RecommendServiceImplTest {

    @Mock
    private BookMapper bookMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private RecommendServiceImpl service;

    /** 无订单数据时，基于物品的协同过滤应降级到"同分类书籍"。 */
    @Test
    void itemCF_noOrders_fallbacksToSameCategory() {
        Book current = new Book();
        current.setId(1);
        current.setCategoryId(117);

        Book b1 = new Book(); b1.setId(10); b1.setStatus(1);
        Book b2 = new Book(); b2.setId(11); b2.setStatus(1);

        when(orderMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(bookMapper.selectList(any())).thenReturn(List.of(b1, b2));

        RecommendServiceImpl spy = spy(service);
        doReturn(current).when(spy).getById(1);

        List<Book> result = spy.recommendByItemCF(1, 6);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getStatus() == 1));
    }

    /** 无订单数据时，基于用户的协同过滤应降级到"热门书籍"。 */
    @Test
    void userCF_noOrders_fallbacksToHot() {
        when(orderMapper.selectList(any())).thenReturn(Collections.emptyList());

        Book hot = new Book();
        hot.setId(99);
        hot.setStatus(1);
        when(bookMapper.selectList(any())).thenReturn(List.of(hot));

        List<Book> result = service.recommendByUserCF(5, 8);

        assertEquals(1, result.size());
        assertEquals(99, result.get(0).getId());
    }
}
