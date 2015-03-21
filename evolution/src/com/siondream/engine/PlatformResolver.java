package com.siondream.engine;

/**
 * @class PlatformResolver
 * @author David
 * @date 02/09/2012
 *
 * @brief Allow to abstract platform dependent behavior
 */
public interface PlatformResolver {
	
	/**
	 * @return string representing the device default language to use with LanguageManager
	 */
	public String getDefaultLanguage();
}
