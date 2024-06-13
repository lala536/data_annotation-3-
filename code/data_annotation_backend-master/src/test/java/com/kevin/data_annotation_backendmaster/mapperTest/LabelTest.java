package com.kevin.data_annotation_backendmaster.mapperTest;

import com.kevin.data_annotation_backendmaster.entity.Label;
import com.kevin.data_annotation_backendmaster.mapper.LabelMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class LabelTest {

    @Autowired
    private LabelMapper labelMapper;

    @Test
    void test() {
        List<Label> allLabel = labelMapper.getAllLabel();
        allLabel.forEach(System.out::println);
    }
}
