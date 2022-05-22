package com.bolingcavalry;

import com.bolingcavalry.db.entity.City;
import com.bolingcavalry.db.entity.Country;
import com.bolingcavalry.db.service.CityService;
import com.bolingcavalry.db.service.CountyService;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
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
     * 在City.java中，id字段的SequenceGenerator指定了initialValue等于10，
     * 表示自增ID从10开始
     */
    private static final int ID_SEQUENCE_INIT_VALUE = 10;

    /**
     * import.sql中，第一条记录的id
     */
    private static final int EXIST_FIRST_ID = 1;

    @Inject
    CityService cityService;

    @Inject
    CountyService countyService;



    @Test
    @DisplayName("list")
    @Order(1)
    public void testGet() {
        List<City> list = cityService.get();
        // 判定非空
        Assertions.assertNotNull(list);
        // import.sql中新增3条记录
        Assertions.assertEquals(EXIST_CITY_RECORDS_SIZE, list.size());
    }

    @Test
    @DisplayName("getSingle")
    @Order(2)
    public void testGetSingle() {
        City city = cityService.getSingle(EXIST_FIRST_ID);
        // 判定非空
        Assertions.assertNotNull(city);
        // import.sql中的第一条记录
        Assertions.assertEquals("BeiJing", city.getName());
    }

    @Test
    @DisplayName("update")
    @Order(3)
    public void testUpdate() {
        String newName = LocalDateTime.now().toString();

        cityService.update(EXIST_FIRST_ID, new City(newName));

        // 从数据库取出的对象，其名称应该等于修改的名称
        Assertions.assertEquals(newName, cityService.getSingle(EXIST_FIRST_ID).getName());
    }

    @Test
    @DisplayName("create")
    @Order(4)
    public void testCreate() {
        int numBeforeDelete = cityService.get().size();
        City city = new City("ShenZhen");
        cityService.create(city);

        // 由于是第一次新增，所以ID应该等于自增ID的起始值
        Assertions.assertEquals(ID_SEQUENCE_INIT_VALUE, city.getId());

        // 记录总数应该等于已有记录数+1
        Assertions.assertEquals(numBeforeDelete + 1, cityService.get().size());
    }

    @Test
    @DisplayName("delete")
    @Order(5)
    public void testDelete() {
        // 先记删除前的总数
        int numBeforeDelete = cityService.get().size();

        // 删除testCreate方法中新增的记录，此记录的是第一次使用自增主键，所以id等于自增主键的起始id
        cityService.delete(ID_SEQUENCE_INIT_VALUE);

        // 记录数应该应该等于删除前的数量减一
        Assertions.assertEquals(numBeforeDelete-1, cityService.get().size());
    }

    @DisplayName("cacheEntity")
    @Order(6)
    @RepeatedTest(10000)
    public void testCacheEntity() {
        City city = cityService.getSingle(EXIST_FIRST_ID);
        // 判定非空
        Assertions.assertNotNull(city);
    }

    @DisplayName("cacheSQL")
    @Order(7)
    @RepeatedTest(10000)
    public void testCacheSQL() {
        List<City> cities = cityService.get();
        // 判定非空
        Assertions.assertNotNull(cities);
        // import.sql中新增3条city记录
        Assertions.assertEquals(EXIST_CITY_RECORDS_SIZE, cities.size());
    }

    @DisplayName("cacheOne2Many")
    @Order(8)
    @RepeatedTest(10000)
    public void testCacheOne2Many() {
        Country country = countyService.getSingle(EXIST_FIRST_ID);
        // 判定非空
        Assertions.assertNotNull(country);
        // import.sql中新增3条city记录
        Assertions.assertEquals(EXIST_CITY_RECORDS_SIZE, country.getCities().size());
    }
}
