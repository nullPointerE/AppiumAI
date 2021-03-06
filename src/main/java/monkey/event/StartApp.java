package monkey.event;

import monkey.Utils;
import org.openqa.selenium.remote.RemoteWebDriver;

public class StartApp extends Event {

	private String _appName;

	public StartApp(RemoteWebDriver driver,String appName) {
		super(driver);
		_appName = appName;
	}

	@Override
	public boolean execute() {
		
		Utils.startApp(_appName, _driver);
		Utils.sleep(1000);
		
		return true;
	}


	
	
}
