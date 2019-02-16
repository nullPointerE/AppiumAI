package monkey.event;

import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;
import java.util.Map;

public class Back extends Event {

	public Back(RemoteWebDriver driver) {
		super(driver);
	}

	@Override
	public boolean execute() {
		Map<String, Object> params = new HashMap<>();
		params = new HashMap<>();
		params.put("keySequence", "BACK");
		_driver.executeScript("mobile:presskey", params);

		
		return true;
	}

}
