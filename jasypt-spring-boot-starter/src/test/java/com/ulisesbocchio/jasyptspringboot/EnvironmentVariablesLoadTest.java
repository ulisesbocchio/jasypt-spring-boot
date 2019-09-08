package com.ulisesbocchio.jasyptspringboot;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EnvironmentVariablesLoadTest.TestConfig.class)
public class EnvironmentVariablesLoadTest {

    @Autowired
    private MyProperties myProperties;

    @ClassRule
    public static final EnvironmentVariables environmentVariables
            = new EnvironmentVariables();


    @BeforeClass
    public static void setup() {
        environmentVariables.set("TEST_ENV_FOOSTRING", "test1");
        environmentVariables.set("TEST_ENV_BAR_STRING", "test2");
        environmentVariables.set("TEST_ENV_FOOLIST", "a1,b1");
        environmentVariables.set("TEST_ENV_BAR_LIST", "a2,b2");
        environmentVariables.set("TEST_ENV_STRINGMAP_K1", "v1");
        environmentVariables.set("TEST_ENV_LISTMAP_K2", "v2");
    }

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
