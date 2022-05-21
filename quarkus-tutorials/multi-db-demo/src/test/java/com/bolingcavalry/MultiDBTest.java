package com.bolingcavalry;

import com.bolingcavalry.multidb.entity.seconddb.Buyer;
import com.bolingcavalry.multidb.entity.firstdb.Seller;
import com.bolingcavalry.multidb.service.BuyerService;
import com.bolingcavalry.multidb.service.SellerService;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
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
        // seller表初始化时新增了3条记录
        Assertions.assertEquals(FIRST_DB_EXIST_RECORDS_SIZE, sellerList.size());

        List<Buyer> buyerList = buyerService.get();
        // 判定非空
        Assertions.assertNotNull(buyerList);
        // buyer表初始化时新增了2条记录
        Assertions.assertEquals(SECOND_DB_EXIST_RECORDS_SIZE, buyerList.size());
    }

    @Test
    @DisplayName("getSingle")
    @Order(2)
    public void testGetSingle() {
        // 用第二条记录吧，第一条在执行testUpdate方法时被更改了
        Seller seller = sellerService.getSingle(EXIST_FIRST_ID+1);
        // 判定非空
        Assertions.assertNotNull(seller);
        // buyer表的第一条记录
        Assertions.assertEquals("seller2", seller.getName());

        // 用第二条记录吧，第一条在执行testUpdate方法时被更改了
        Buyer buyer = buyerService.getSingle(EXIST_FIRST_ID+1);
        // 判定非空
        Assertions.assertNotNull(buyer);
        // buyer表的第二条记录
        Assertions.assertEquals("buyer2", buyer.getName());
    }

    @Test
    @DisplayName("update")
    @Order(3)
    public void testUpdate() {
        // 验证first_db的操作
        String newName = LocalDateTime.now().toString();

        Seller seller = new Seller();
        seller.setName(newName);

        // 更新数据库
        sellerService.update(EXIST_FIRST_ID, seller);

        Seller sellerFromDB = sellerService.getSingle(EXIST_FIRST_ID);
        // 从数据库取出的对象，其名称应该等于修改的名称
        Assertions.assertEquals(newName, sellerFromDB.getName());

        // 验证second_db的操作
        Buyer buyer = new Buyer();
        buyer.setName(newName);

        // 更新数据库
        buyerService.update(EXIST_FIRST_ID, buyer);

        Buyer buyerFromDB = buyerService.getSingle(EXIST_FIRST_ID);
        // 从数据库取出的对象，其名称应该等于修改的名称
        Assertions.assertEquals(newName, buyerFromDB.getName());
    }

    @Test
    @DisplayName("create")
    @Order(3)
    public void testCreate() {
        Seller seller = new Seller();
        seller.setName("seller4");
        sellerService.create(seller);
        // 创建成功后，记录主键肯定是大于3的
        Assertions.assertTrue(seller.getId()>FIRST_DB_EXIST_RECORDS_SIZE);
        // 记录总数应该等于已有记录数+1
        Assertions.assertEquals(FIRST_DB_EXIST_RECORDS_SIZE+1, sellerService.get().size());

        Buyer buyer = new Buyer();
        buyer.setName("buyer3");
        buyerService.create(buyer);
        // 创建成功后，记录主键肯定是大于3的
        Assertions.assertTrue(buyer.getId()>SECOND_DB_EXIST_RECORDS_SIZE);
        // 记录总数应该等于已有记录数+1
        Assertions.assertEquals(SECOND_DB_EXIST_RECORDS_SIZE+1, buyerService.get().size());
    }

    @Test
    @DisplayName("delete")
    @Order(5)
    public void testDelete() {
        List<Seller> sellers = sellerService.get();
        // 先记删除前的总数
        int numBeforeDelete = sellers.size();

        // 删除最后一条记录
        sellerService.delete(sellers.get(numBeforeDelete-1).getId());

        // 记录数应该应该等于删除前的数量减一
        Assertions.assertEquals(numBeforeDelete-1, sellerService.get().size());

        List<Buyer> buyers = buyerService.get();

        // 先记删除前的总数
        numBeforeDelete = buyers.size();

        // 删除最后一条记录
        buyerService.delete(buyers.get(numBeforeDelete-1).getId());

        // 记录数应该应该等于删除前的数量减一
        Assertions.assertEquals(numBeforeDelete-1, buyerService.get().size());
    }

}
