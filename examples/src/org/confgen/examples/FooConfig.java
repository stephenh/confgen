package org.confgen.examples;

import org.confgen.Conf;

public interface FooConfig {

	@Conf(key = "a", dev = "adev")
	String getA();
	
}
