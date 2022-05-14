package com.bolingcavalry;

import com.bolingcavalry.multidb.entity.seconddb.Buyer;
import com.bolingcavalry.multidb.entity.firstdb.Seller;
import com.bolingcavalry.multidb.service.BuyerService;
import com.bolingcavalry.multidb.service.SellerService;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiDBTest {

    /**
     * first_db的seller表中，初始记录数
     */
    private static final int FIRST_DB_EXIST_RECORDS_SIZE = 3;

    /**
     * second_db的buyer表中，初始记录数
     */
    private static final int SECOND_DB_EXIST_RECORDS_SIZE = 2;

    /**
     * import.sql中，第一条记录的id
     */
    private static final int EXIST_FIRST_ID = 1;

    /**
     * 在Fruit.java中，id字段的SequenceGenerator指定了initialValue等于10，
     * 表示自增ID从10开始
     */
    private static final int ID_SEQUENCE_INIT_VALUE = 10;

    @Inject
    SellerService sellerService;

    @Inject
    BuyerService buyerService;

    @Test
    @DisplayName("list")
    @Order(1)
    public void testGet() {
        List<Seller> sellerList = sellerService.get();
        // 判定非空
        Assertions.assertNotNull(sellerList);
        // 初始化时新增了3条记录
        Assertions.assertEquals(FIRST_DB_EXIST_RECORDS_SIZE, sellerList.size());

        List<Buyer> buyerList = buyerService.get();
        // 判定非空
        Assertions.assertNotNull(buyerList);
        // 初始化时新增了3条记录
        Assertions.assertEquals(SECOND_DB_EXIST_RECORDS_SIZE, buyerList.size());
    }

    @Test
    @DisplayName("getSingle")
    @Order(2)
    public void testGetSingle() {
        Seller seller = sellerService.getSingle(EXIST_FIRST_ID);
        // 判定非空
        Assertions.assertNotNull(seller);
        // import.sql中的第一条记录
        Assertions.assertEquals("seller1", seller.getName());

        Buyer buyer = buyerService.getSingle(EXIST_FIRST_ID);
        // 判定非空
        Assertions.assertNotNull(buyer);
    }
    /*
    @Test
    @DisplayName("update")
    @Order(3)
    public void testUpdate() {
        String newName = "seller1-update";

        Seller seller = new Seller();
        seller.setName(newName);

        sellerService.update(EXIST_FIRST_ID, seller);

        Seller sellerFromDB = sellerService.getSingle(EXIST_FIRST_ID);
        // 从数据库取出的对象，其名称应该等于修改的名称
        Assertions.assertEquals(newName, sellerFromDB.getName());
    }

    @Test
    @DisplayName("create")
    @Order(4)
    public void testCreate() {
        Seller seller = new Seller();
        seller.setName("seller4");
        sellerService.create(seller);
        Assertions.assertTrue(seller.getId()>EXIST_RECORDS_SIZE);
        // 记录总数应该等于已有记录数+1
        Assertions.assertEquals(EXIST_RECORDS_SIZE+1, sellerService.get().size());
    }

    @Test
    @DisplayName("delete")
    @Order(5)
    public void testDelete() {
        List<Seller> list = sellerService.get();
        // 先记删除前的总数
        int numBeforeDelete = list.size();

        // 删除最后一条记录
        sellerService.delete(list.get(numBeforeDelete-1).getId());

        // 记录数应该应该等于删除前的数量减一
        Assertions.assertEquals(numBeforeDelete-1, sellerService.get().size());
    }
    */
}
