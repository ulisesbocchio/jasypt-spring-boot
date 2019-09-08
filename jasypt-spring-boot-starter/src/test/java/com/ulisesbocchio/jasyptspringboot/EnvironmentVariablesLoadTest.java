package com.ulisesbocchio.jasyptspringboot;

import java.util.List;
import java.util.Map;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EnvironmentVariablesLoadTest.TestConfig.class)
public class EnvironmentVariablesLoadTest {

    @Autowired
    private MyProperties myProperties;

    @EnableEncryptableProperties
    @EnableConfigurationProperties(EnvironmentVariablesLoadTest.MyProperties.class)
    public static class TestConfig {
    }

    @Test
    public void environmentVariables_shouldBeLoaded_whenSetInBothFormats() {
        assertEquals("test1", myProperties.getFooString());

        assertEquals("test2", myProperties.getBarString());

        assertNotNull(myProperties.getFooList());
        assertEquals("[a1, b1]", myProperties.getFooList().toString());

        assertNotNull(myProperties.getBarList());
        assertEquals("[a2, b2]", myProperties.getBarList().toString());

        assertNotNull(myProperties.getStringMap());
        assertEquals("{k1=v1}", myProperties.getStringMap().toString());

        assertNotNull(myProperties.getListMap());
        assertEquals("{k2=[v2]}", myProperties.getListMap().toString());
    }

    @ConfigurationProperties(prefix = "test.env")
    static class MyProperties {

        private String fooString;
        private String barString;
        private List<String> fooList;
        private List<String> barList;
        private Map<String, String> stringMap;
        private Map<String, List<String>> listMap;

        public String getFooString() {
            return fooString;
        }

        public void setFooString(String fooString) {
            this.fooString = fooString;
        }

        public String getBarString() {
            return barString;
        }

        public void setBarString(String barString) {
            this.barString = barString;
        }

        public List<String> getFooList() {
            return fooList;
        }

        public void setFooList(List<String> fooList) {
            this.fooList = fooList;
        }

        public List<String> getBarList() {
            return barList;
        }

        public void setBarList(List<String> barList) {
            this.barList = barList;
        }

        public Map<String, String> getStringMap() {
            return stringMap;
        }

        public void setStringMap(Map<String, String> stringMap) {
            this.stringMap = stringMap;
        }

        public Map<String, List<String>> getListMap() {
            return listMap;
        }

        public void setListMap(Map<String, List<String>> listMap) {
            this.listMap = listMap;
        }
    }
}
