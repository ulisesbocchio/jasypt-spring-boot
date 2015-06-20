package demo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.style.ToStringCreator;

import java.util.List;

@ConfigurationProperties(prefix="itemConfig")
public class ItemConfig {

    private String configurationName;

    private String password;

    // should be populated from items nested collection
    // found in application.yml
    private List<Item> items;

    public String getConfigurationName() {
        return configurationName;
    }

    public String getPassword() {
        return password;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Maps an item entry in the application.yml.
    public static class Item {

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        private String name;

        private Integer value;

        @Override
        public String toString() {
            return new ToStringCreator(this)
                    .append("name", name)
                    .append("value", value)
                    .toString();
        }
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("configurationName", configurationName)
                .append("password", password)
                .append("items", items)
                .toString();
    }
}
