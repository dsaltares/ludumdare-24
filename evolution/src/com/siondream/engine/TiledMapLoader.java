package com.siondream.engine;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class TiledMapLoader extends AsynchronousAssetLoader<TiledMap, TiledMapLoader.TiledMapParameter >{

	static public class TiledMapParameter extends AssetLoaderParameters<TiledMap> {
	}

	TiledMap m_map = null;
	
	public TiledMapLoader(FileHandleResolver resolver) {
		super(resolver);
	}
	
	@Override
	public void loadAsync(AssetManager manager, String fileName, TiledMapParameter parameter) {
		// Go over all the tilesets retrieving the texture and loading the regions
		Array<TileSet> tilesets = m_map.getTileSets();
		int numTilesets = tilesets.size;
		
		for (int i = 0; i < numTilesets; ++i) {
			TileSet tileset = tilesets.get(i);
			tileset.setTexture(manager.get(tileset.getTextureFileName(), Texture.class));
		}
	}

	@Override
	public TiledMap loadSync(AssetManager manager, String fileName, TiledMapParameter parameter) {
		return m_map;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, TiledMapParameter parameter) {
		// Create map
		m_map = new TiledMap(fileName, true);
		
		// Get dependencies
		Array<TileSet> tilesets = m_map.getTileSets();
		int numTilesets = tilesets.size;
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		
		for (int i = 0; i < numTilesets; ++i) {
			TextureLoader.TextureParameter params = new TextureLoader.TextureParameter();
			params.magFilter = Texture.TextureFilter.Nearest;
			params.minFilter = Texture.TextureFilter.Nearest;
			params.wrapU = Texture.TextureWrap.ClampToEdge;
			params.wrapU = Texture.TextureWrap.ClampToEdge;
			AssetDescriptor<Texture> descriptor = new AssetDescriptor<Texture>(tilesets.get(i).getTextureFileName(), Texture.class, params);
			dependencies.add(descriptor);
		}
		
		return dependencies;
	}
}
