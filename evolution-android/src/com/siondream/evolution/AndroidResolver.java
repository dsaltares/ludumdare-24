package com.siondream.evolution;

import com.siondream.engine.PlatformResolver;

public class AndroidResolver implements PlatformResolver {

	@Override
	public String getDefaultLanguage() {
		return java.util.Locale.getDefault().toString();
	}
}
