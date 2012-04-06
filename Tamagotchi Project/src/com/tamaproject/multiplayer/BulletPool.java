package com.tamaproject.multiplayer;

import org.anddev.andengine.entity.modifier.ColorModifier;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.pool.GenericPool;

public class BulletPool extends GenericPool<Sprite>
{
    private TextureRegion mTextureRegion;

    public BulletPool(TextureRegion pTextureRegion)
    {
	if (pTextureRegion == null)
	{
	    // Need to be able to create a Sprite so the Pool needs to have a TextureRegion
	    throw new IllegalArgumentException("The texture region must not be NULL");
	}
	mTextureRegion = pTextureRegion;
    }

    /**
     * Called when a Bullet is required but there isn't one in the pool
     */
    @Override
    protected Sprite onAllocatePoolItem()
    {
	return new Sprite(0, 0, mTextureRegion);
    }

    /**
     * Called when a Bullet is sent to the pool
     */
    @Override
    protected void onHandleRecycleItem(final Sprite pBullet)
    {
	pBullet.setIgnoreUpdate(true);
	pBullet.setVisible(false);
    }

    /**
     * Called just before a Bullet is returned to the caller, this is where you write your initialize code i.e. set location, rotation, etc.
     */
    @Override
    protected void onHandleObtainItem(final Sprite pBullet)
    {
	pBullet.reset();
    }

}