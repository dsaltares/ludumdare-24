package com.siondream.engine;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class AnimationLoader extends AsynchronousAssetLoader<AnimationData, AnimationLoader.AnimationParameter > {

	static public class AnimationParameter extends AssetLoaderParameters<AnimationData> {
	}
	
	AnimationData m_animationData = null;

	public AnimationLoader(FileHandleResolver resolver) {
		super(resolver);
		
		m_animationData = null;
	}
	
	@Override
	public void loadAsync(AssetManager manager, String fileName, AnimationParameter parameter) {
		m_animationData.loadAnimations();
	}

	@Override
	public AnimationData loadSync(AssetManager manager, String fileName, AnimationParameter parameter) {
		return m_animationData;
	}
	
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, AnimationParameter parameter) {
		// Create animation data
		m_animationData = new AnimationData(fileName);
		
		// Load basic info
		m_animationData.loadBasicInfo();
		
		// Return texture dependency
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		dependencies.add(new AssetDescriptor<Texture>(m_animationData.getTextureName(), Texture.class));
		
		return dependencies;
	}
}
