package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ulises Bocchio, Sergio.U.Bocchio@Disney.com (BOCCS002)
 */
public class SimpleBean {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleBean.class);

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
