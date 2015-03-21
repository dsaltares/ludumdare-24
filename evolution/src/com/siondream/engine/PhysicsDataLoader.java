package com.siondream.engine;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.utils.Array;

public class PhysicsDataLoader extends AsynchronousAssetLoader<PhysicsData, PhysicsDataLoader.PhysicsParameter > {
	
	static public class PhysicsParameter extends AssetLoaderParameters<PhysicsData> {}
	
	private PhysicsData m_physicsData = null;
	
	public PhysicsDataLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, PhysicsParameter parameter) {
		m_physicsData = new PhysicsData(fileName);
		m_physicsData.loadData();
	}

	@Override
	public PhysicsData loadSync(AssetManager manager, String fileName, PhysicsParameter parameter) {
		return m_physicsData;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, PhysicsParameter parameter) {
		return new Array<AssetDescriptor>();
	}
}
