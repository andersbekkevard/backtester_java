package accounts;

import java.util.Map;

import engine.Bar;

public interface BarListener {
	void acceptBars(Map<String, Bar> barMap);
}
