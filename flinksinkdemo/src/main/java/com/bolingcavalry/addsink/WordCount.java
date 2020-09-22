package com.bolingcavalry.addsink;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;

/**
 * @Description: 带有datastax注解的bean
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/4/6 21:04
 */
@Table(keyspace = "example", name = "wordcount")
public class WordCount {

    @Column(name = "word")
    private String word = "";

    @Column(name = "count")
    private long count = 0;

    public WordCount() {
    }

    public WordCount(String word, long count) {
        this.setWord(word);
        this.setCount(count);
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return getWord() + " : " + getCount();
    }
}
