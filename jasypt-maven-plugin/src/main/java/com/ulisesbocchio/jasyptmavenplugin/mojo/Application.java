package com.ulisesbocchio.jasyptmavenplugin.mojo;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class. Note that location is important, at the root package of this module, so that
 * the correct spring configuration can be located. No main method is required because this will be
 * initiated via a Maven Mojo. See {@link AbstractJasyptMojo}.
 */
@SpringBootApplication
public class Application {
}
