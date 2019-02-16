package monkey.event;

import monkey.Utils;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;
import java.util.Map;

public class Home extends Event {

	private String _appName;

	public Home(RemoteWebDriver driver,String appName) {
		super(driver);
		_appName = appName;
	}

	@Override
	public boolean execute() {
		Map<String, Object> params = new HashMap<>();
		params = new HashMap<>();
		params.put("keySequence", "HOME");
		_driver.executeScript("mobile:presskey", params);
		
		Utils.sleep(1000);
		
		Utils.startApp(_appName, _driver);
		
		Utils.sleep(1000);
		
		return true;
	}

}
