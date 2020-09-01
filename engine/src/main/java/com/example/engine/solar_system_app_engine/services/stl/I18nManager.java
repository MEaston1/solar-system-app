package com.example.engine.solar_system_app_engine.services.stl;

public class I18nManager {

	private static final I18nManager manager = new I18nManager();

	public static I18nManager getManager() {
		return manager;
	}

	public String getString(String unknownKeywordMsgProp) {
		return unknownKeywordMsgProp;
	}
}
