package com.bolingcavalry;

import com.bolingcavalry.db.entity.Country;
import com.bolingcavalry.db.service.CountyService;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheTest {

    /**
     * import.sql中导入的记录数量，这些是应用启动是导入的
     */
    private static final int EXIST_CITY_RECORDS_SIZE = 3;
    private static final int EXIST_COUNTRY_RECORDS_SIZE = 1;


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
    CountyService countyService;

    @Test
    @DisplayName("list")
    @Order(2)
    public void testGetCity() {
        List<Country> countries = countyService.get();
        // 判定非空
        Assertions.assertNotNull(countries);
        // import.sql中新增1条country记录
        Assertions.assertEquals(EXIST_COUNTRY_RECORDS_SIZE, countries.size());
        // import.sql中新增1条city记录
        Assertions.assertEquals(EXIST_CITY_RECORDS_SIZE, countries.get(0).getCities().size());
    }

    @Test
    @DisplayName("testGet")
    @Order(2)
    public void testGetCountry() {
        List<Country> countries = countyService.get();
        // 判定非空
        Assertions.assertNotNull(countries);
        // import.sql中新增1条country记录
        Assertions.assertEquals(EXIST_COUNTRY_RECORDS_SIZE, countries.size());
        // import.sql中新增1条city记录
        Assertions.assertEquals(EXIST_CITY_RECORDS_SIZE, countries.get(0).getCities().size());
    }

    /*
    @Test
    @DisplayName("getSingle")
    @Order(2)
    public void testGetSingle() {
        Fruit fruit = fruitService.getSingle(EXIST_FIRST_ID);
        // 判定非空
        Assertions.assertNotNull(fruit);
        // import.sql中的第一条记录
        Assertions.assertEquals("Cherry", fruit.getName());
    }


    @Test
    @DisplayName("update")
    @Order(3)
    public void testUpdate() {
        String newName = "ShanDongBigCherry";

        fruitService.update(EXIST_FIRST_ID, new Fruit(newName));

        Fruit fruit = fruitService.getSingle(EXIST_FIRST_ID);
        // 从数据库取出的对象，其名称应该等于修改的名称
        Assertions.assertEquals(newName, fruit.getName());
    }

    @Test
    @DisplayName("create")
    @Order(4)
    public void testCreate() {
        Fruit fruit = new Fruit("Orange");
        fruitService.create(fruit);
        // 由于是第一次新增，所以ID应该等于自增ID的起始值
        Assertions.assertEquals(ID_SEQUENCE_INIT_VALUE, fruit.getId());
        // 记录总数应该等于已有记录数+1
        Assertions.assertEquals(EXIST_RECORDS_SIZE+1, fruitService.get().size());
    }

    @Test
    @DisplayName("delete")
    @Order(5)
    public void testDelete() {
        // 先记删除前的总数
        int numBeforeDelete = fruitService.get().size();

        // 删除第一条记录
        fruitService.delete(EXIST_FIRST_ID);

        // 记录数应该应该等于删除前的数量减一
        Assertions.assertEquals(numBeforeDelete-1, fruitService.get().size());
    }

*/
//    @DisplayName("cache")
//    @RepeatedTest(1)
//    public void testCache() {
//        Fruit fruit = fruitService.getSingle(EXIST_FIRST_ID);
//        // 判定非空
//        Assertions.assertNotNull(fruit);
//        // import.sql中的第一条记录
//        Assertions.assertEquals("Cherry", fruit.getName());
//    }
}
