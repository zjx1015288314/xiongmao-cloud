package com.itzjx.item.service;


import com.itzjx.common.dto.CartDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsServiceTest {

    @Autowired
    private GoodsService goodsService;

    @Test
    public void decreaseStock(){
        List<CartDTO> cartDTOS = Arrays.asList(new CartDTO(2600242L, 2),
                new CartDTO(2600248L, 2));
        goodsService.decreaseStock(cartDTOS);
    }

}