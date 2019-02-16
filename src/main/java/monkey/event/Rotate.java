package monkey.event;

import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;
import java.util.Map;

public class Rotate extends Event {

	public Rotate(RemoteWebDriver driver) {
		super(driver);
	}

	@Override
	public boolean execute() {
		
		Map<String, Object> params = new HashMap<>();
		params.put("operation", "next");
		
		Object result = _driver.executeScript("mobile:handset:rotate", params);
		
//		params.put("operation", "reset");
//		result = _driver.executeScript("mobile:handset:rotate", params);



		return true;
	}

}
