package org.confgen.examples;

import org.confgen.Conf;

public interface BarConfig {
	
	@Conf(key = "b")
	String getB();

}
